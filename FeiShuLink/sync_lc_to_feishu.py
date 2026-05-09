#!/usr/bin/env python3
"""Sync LC warehouse data to Feishu Bitable through existing local APIs."""

from __future__ import annotations

import argparse
import json
import sys
import time
from dataclasses import dataclass
from datetime import datetime
from typing import Any, Dict, Iterable, List, Tuple

import requests


WAREHOUSE_FIELD_MAP = {
    "product_sku": "SKU",
    "product_title": "产品名称",
    "warehouse_code": "仓库代码",
    "sellable": "可售",
    "reserved": "待出库",
    "onway": "在途",
    "pending": "待上架",
    "unsellable": "不合格",
    "shipped": "历史出库",
}

COMPARE_COLUMNS = ("可售", "待出库", "在途", "待上架", "不合格", "历史出库")


@dataclass
class SyncStats:
    fetched_skus: int = 0
    fetched_warehouse_rows: int = 0
    fetched_feishu_rows: int = 0
    to_create: int = 0
    to_update: int = 0
    skipped: int = 0
    created: int = 0
    updated: int = 0


class ApiError(RuntimeError):
    pass


def chunks(items: List[Any], size: int) -> Iterable[List[Any]]:
    for i in range(0, len(items), size):
        yield items[i : i + size]


def normalize_scalar(value: Any) -> str:
    if value is None:
        return ""
    if isinstance(value, list):
        if not value:
            return ""
        first = value[0]
        if isinstance(first, dict):
            text = first.get("text")
            if text is not None:
                return str(text).strip()
        return str(first).strip()
    if isinstance(value, dict):
        text = value.get("text")
        if text is not None:
            return str(text).strip()
        return json.dumps(value, ensure_ascii=False, sort_keys=True)
    return str(value).strip()


def normalize_number_str(value: Any) -> str:
    text = normalize_scalar(value)
    if text == "":
        return "0"
    try:
        number = float(text)
    except ValueError:
        return text
    if number.is_integer():
        return str(int(number))
    return text


def parse_result(resp: requests.Response) -> Any:
    try:
        payload = resp.json()
    except ValueError as exc:
        raise ApiError(f"响应不是 JSON: {resp.text[:300]}") from exc
    code = payload.get("code")
    if code != 200:
        raise ApiError(f"接口错误 code={code}, message={payload.get('message')}")
    return payload.get("data")


def http_get_json(session: requests.Session, url: str, timeout: int) -> Any:
    resp = session.get(url, timeout=timeout)
    resp.raise_for_status()
    return parse_result(resp)


def http_post_json(session: requests.Session, url: str, body: Dict[str, Any], timeout: int) -> Any:
    resp = session.post(url, json=body, timeout=timeout)
    resp.raise_for_status()
    return parse_result(resp)


def fetch_skus(session: requests.Session, base_url: str, sku_table_key: str, timeout: int) -> List[str]:
    url = f"{base_url}/api/{sku_table_key}/records/searchSKU"
    data = http_get_json(session, url, timeout)
    sku_arr = data.get("product_sku_arr") if isinstance(data, dict) else None
    if not isinstance(sku_arr, list):
        raise ApiError(f"searchSKU 返回异常: {data}")
    skus = []
    for sku in sku_arr:
        val = normalize_scalar(sku)
        if val:
            skus.append(val)
    return skus


def fetch_lc_warehouse_rows(
    session: requests.Session,
    base_url: str,
    skus: List[str],
    query_chunk_size: int,
    timeout: int,
) -> List[Dict[str, Any]]:
    if not skus:
        return []
    url = f"{base_url}/api/getLCWarehouseDataAll"
    rows: List[Dict[str, Any]] = []
    for sku_chunk in chunks(skus, query_chunk_size):
        body = {
            "pageSize": None,
            "page": 1,
            "product_sku": "",
            "product_sku_arr": sku_chunk,
            "warehouse_code": "",
            "warehouse_code_arr": [],
            "update_start_time": "",
        }
        data = http_post_json(session, url, body, timeout)
        if isinstance(data, list):
            rows.extend(item for item in data if isinstance(item, dict))
        elif data is None:
            continue
        else:
            raise ApiError(f"getLCWarehouseDataAll 返回异常: {data}")
    return rows


def fetch_feishu_records(
    session: requests.Session,
    base_url: str,
    target_table_key: str,
    timeout: int,
) -> List[Dict[str, Any]]:
    url = f"{base_url}/api/{target_table_key}/records/search"
    data = http_post_json(session, url, {}, timeout)
    if data is None:
        return []
    if not isinstance(data, list):
        raise ApiError(f"records/search 返回异常: {data}")
    return [item for item in data if isinstance(item, dict)]


def warehouse_row_to_fields(row: Dict[str, Any]) -> Dict[str, Any]:
    fields: Dict[str, Any] = {}
    for warehouse_key, feishu_key in WAREHOUSE_FIELD_MAP.items():
        raw_value = row.get(warehouse_key)
        if feishu_key in COMPARE_COLUMNS:
            fields[feishu_key] = normalize_number_str(raw_value)
        else:
            fields[feishu_key] = normalize_scalar(raw_value)
    return fields


def record_unique_key(fields: Dict[str, Any]) -> Tuple[str, str]:
    return normalize_scalar(fields.get("SKU")), normalize_scalar(fields.get("仓库代码"))


def should_update(existing_fields: Dict[str, Any], expected_fields: Dict[str, Any]) -> bool:
    for col in COMPARE_COLUMNS:
        if normalize_number_str(existing_fields.get(col)) != normalize_number_str(expected_fields.get(col)):
            return True
    return False


def build_existing_index(records: List[Dict[str, Any]]) -> Dict[Tuple[str, str], Dict[str, Any]]:
    index: Dict[Tuple[str, str], Dict[str, Any]] = {}
    for record in records:
        fields = record.get("fields")
        if not isinstance(fields, dict):
            continue
        key = record_unique_key(fields)
        if key[0] and key[1] and key not in index:
            index[key] = record
    return index


def plan_changes(
    warehouse_rows: List[Dict[str, Any]],
    existing_index: Dict[Tuple[str, str], Dict[str, Any]],
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]], int]:
    creates: List[Dict[str, Any]] = []
    updates: List[Dict[str, Any]] = []
    skipped = 0
    seen_keys: set[Tuple[str, str]] = set()

    for row in warehouse_rows:
        expected_fields = warehouse_row_to_fields(row)
        key = record_unique_key(expected_fields)
        if not key[0] or not key[1]:
            continue
        if key in seen_keys:
            continue
        seen_keys.add(key)

        existing = existing_index.get(key)
        if existing is None:
            creates.append(expected_fields)
            continue

        record_id = existing.get("recordId") or existing.get("record_id")
        existing_fields = existing.get("fields", {})
        if not record_id or not isinstance(existing_fields, dict):
            continue

        if should_update(existing_fields, expected_fields):
            updates.append(
                {
                    "record_id": record_id,
                    "fields": {
                        "产品名称": expected_fields["产品名称"],
                        "可售": expected_fields["可售"],
                        "待出库": expected_fields["待出库"],
                        "在途": expected_fields["在途"],
                        "待上架": expected_fields["待上架"],
                        "不合格": expected_fields["不合格"],
                        "历史出库": expected_fields["历史出库"],
                    },
                }
            )
        else:
            skipped += 1
    return creates, updates, skipped


def push_creates(
    session: requests.Session,
    base_url: str,
    target_table_key: str,
    create_items: List[Dict[str, Any]],
    write_chunk_size: int,
    timeout: int,
) -> int:
    if not create_items:
        return 0
    url = f"{base_url}/api/{target_table_key}/create/records"
    total = 0
    for part in chunks(create_items, write_chunk_size):
        body = {"records": part}
        http_post_json(session, url, body, timeout)
        total += len(part)
    return total


def push_updates(
    session: requests.Session,
    base_url: str,
    target_table_key: str,
    update_items: List[Dict[str, Any]],
    write_chunk_size: int,
    timeout: int,
) -> int:
    if not update_items:
        return 0
    url = f"{base_url}/api/{target_table_key}/update/records"
    total = 0
    for part in chunks(update_items, write_chunk_size):
        body = {"records": part}
        http_post_json(session, url, body, timeout)
        total += len(part)
    return total


def run_sync(args: argparse.Namespace) -> SyncStats:
    stats = SyncStats()
    session = requests.Session()
    session.headers.update({"Content-Type": "application/json"})

    skus = fetch_skus(session, args.base_url, args.sku_table_key, args.timeout)
    stats.fetched_skus = len(skus)

    warehouse_rows = fetch_lc_warehouse_rows(
        session=session,
        base_url=args.base_url,
        skus=skus,
        query_chunk_size=args.query_chunk_size,
        timeout=args.timeout,
    )
    stats.fetched_warehouse_rows = len(warehouse_rows)

    existing_records = fetch_feishu_records(
        session=session,
        base_url=args.base_url,
        target_table_key=args.target_table_key,
        timeout=args.timeout,
    )
    stats.fetched_feishu_rows = len(existing_records)

    existing_index = build_existing_index(existing_records)
    creates, updates, skipped = plan_changes(warehouse_rows, existing_index)
    stats.to_create = len(creates)
    stats.to_update = len(updates)
    stats.skipped = skipped

    if not args.dry_run:
        stats.created = push_creates(
            session=session,
            base_url=args.base_url,
            target_table_key=args.target_table_key,
            create_items=creates,
            write_chunk_size=args.write_chunk_size,
            timeout=args.timeout,
        )
        stats.updated = push_updates(
            session=session,
            base_url=args.base_url,
            target_table_key=args.target_table_key,
            update_items=updates,
            write_chunk_size=args.write_chunk_size,
            timeout=args.timeout,
        )
    return stats


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Sync LC warehouse inventory to Feishu table via existing local APIs."
    )
    parser.add_argument("--base-url", default="http://192.168.1.26:8081", help="Local API base URL.")
    parser.add_argument(
        "--sku-table-key",
        default="warehouse-lc",
        help="Table key used for SKU source, default: warehouse-lc",
    )
    parser.add_argument(
        "--target-table-key",
        default="warehouse-lc-sync",
        help="Table key used for inventory sync target, default: warehouse-lc-sync",
    )
    parser.add_argument("--query-chunk-size", type=int, default=500, help="SKU batch size for LC query.")
    parser.add_argument("--write-chunk-size", type=int, default=500, help="Batch size for create/update.")
    parser.add_argument("--timeout", type=int, default=30, help="HTTP timeout seconds.")
    parser.add_argument("--dry-run", action="store_true", help="Only calculate diffs without write-back.")
    parser.add_argument(
        "--once",
        action="store_true",
        help="Run only once (default is loop mode every 300 seconds).",
    )
    parser.add_argument(
        "--interval-seconds",
        type=int,
        default=300,
        help="Loop interval seconds. Default: 300.",
    )
    parser.add_argument(
        "--max-runs",
        type=int,
        default=0,
        help="Stop loop mode after N runs (0 means no limit).",
    )
    return parser.parse_args()


def run_once(args: argparse.Namespace) -> int:
    try:
        stats = run_sync(args)
    except requests.HTTPError as exc:
        print(f"[ERROR] HTTP 错误: {exc}", file=sys.stderr)
        return 2
    except ApiError as exc:
        print(f"[ERROR] 接口错误: {exc}", file=sys.stderr)
        return 3
    except Exception as exc:  # noqa: BLE001
        print(f"[ERROR] 未知错误: {exc}", file=sys.stderr)
        return 1

    mode = "DRY-RUN" if args.dry_run else "SYNC"
    print(f"[{mode}] SKU总数: {stats.fetched_skus}")
    print(f"[{mode}] 仓库数据行: {stats.fetched_warehouse_rows}")
    print(f"[{mode}] 飞书现有行: {stats.fetched_feishu_rows}")
    print(f"[{mode}] 计划新增: {stats.to_create}")
    print(f"[{mode}] 计划更新: {stats.to_update}")
    print(f"[{mode}] 计划跳过: {stats.skipped}")
    if not args.dry_run:
        print(f"[{mode}] 实际新增: {stats.created}")
        print(f"[{mode}] 实际更新: {stats.updated}")
    return 0


def run_loop(args: argparse.Namespace) -> int:
    interval = max(1, args.interval_seconds)
    run_no = 0

    while True:
        run_no += 1
        started_at = time.time()
        now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[LOOP] 第{run_no}轮开始: {now_str}")

        code = run_once(args)
        ended_at = time.time()
        cost = ended_at - started_at

        if code != 0:
            print(f"[LOOP] 第{run_no}轮失败，退出码={code}")
        else:
            print(f"[LOOP] 第{run_no}轮完成，耗时={cost:.1f}s")

        if args.max_runs > 0 and run_no >= args.max_runs:
            print(f"[LOOP] 已达到 max-runs={args.max_runs}，退出。")
            return 0

        sleep_seconds = max(0.0, interval - cost)
        print(f"[LOOP] 下轮将在 {sleep_seconds:.1f}s 后开始。")
        time.sleep(sleep_seconds)


def main() -> int:
    args = parse_args()
    if args.once:
        return run_once(args)
    if args.interval_seconds > 0:
        return run_loop(args)
    return run_once(args)


if __name__ == "__main__":
    raise SystemExit(main())
