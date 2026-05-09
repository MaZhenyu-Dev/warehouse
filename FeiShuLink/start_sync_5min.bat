@echo off
setlocal
cd /d "%~dp0"

echo [INFO] Start LC sync loop: every 300 seconds.
python sync_lc_to_feishu.py --interval-seconds 300

echo [INFO] Sync loop stopped.
pause
