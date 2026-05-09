# 仓库管理系统 (Warehouse Management System)

一个全栈仓库数据管理解决方案，集成多仓库库存查询、飞书多维表格同步、数据可视化等功能。

## 项目架构

```
├── WarehouseData/          # 后端服务 (Spring Boot 3.2.1 + Java 17)
├── Vue-warehouse/          # 前端应用 (Vue 3 + TypeScript + Vite)
├── FeiShuLink/             # 飞书数据同步脚本 (Python 3)
└── .gitignore              # Git 忽略配置
```

## 技术栈

### 后端 (WarehouseData)

- **框架**: Spring Boot 3.2.1
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.7
- **主要依赖**:
  - 飞书 Open API SDK 2.5.1
  - Hutool 5.8.25
  - Fastjson2 2.0.43
  - Lombok 1.18.30
  - JWT 0.11.5

### 前端 (Vue-warehouse)

- **框架**: Vue 3.5.26
- **语言**: TypeScript 5.9
- **构建工具**: Vite 7.3
- **UI 组件库**: Element Plus 2.13.1
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 4.6.4
- **图表**: ECharts 6.0 + Vue-ECharts 8.0
- **样式**: Tailwind CSS 4.1
- **图标**: Lucide Vue Next 0.562

## 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: ^20.19.0 或 >=22.12.0
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Python**: 3.8+ (用于数据同步脚本)

### 后端启动

1. **配置数据库**

   创建 MySQL 数据库：
   ```sql
   CREATE DATABASE warehouse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置应用**

   复制配置文件并修改敏感信息：
   ```bash
   cd WarehouseData/src/main/resources
   cp application-example.yml application.yml
   ```

   编辑 `application.yml`，填入你的：
   - 数据库连接信息
   - 良仓/九方/翼仓 API 凭证
   - 飞书应用凭证
   - 吉客云 API 凭证

3. **编译运行**

   ```bash
   cd WarehouseData
   mvn clean package -DskipTests
   java -jar target/WarehouseData-0.0.1-SNAPSHOT.jar
   ```

   或使用 Maven 直接运行：
   ```bash
   mvn spring-boot:run
   ```

   后端服务将在 `http://localhost:8082` 启动。

### 前端启动

1. **安装依赖**

   ```bash
   cd Vue-warehouse
   npm install
   ```

2. **配置环境变量**

   创建 `.env` 文件（参考 `.env.production`）：
   ```env
   VITE_API_BASE_URL=/api
   VITE_API_TARGET=http://localhost:8082
   ```

3. **开发模式运行**

   ```bash
   npm run dev
   ```

   前端开发服务器将在 `http://localhost:5173` 启动。

4. **生产构建**

   ```bash
   npm run build
   ```

   构建产物位于 `dist/` 目录。

### 数据同步脚本

飞书数据同步脚本位于 `FeiShuLink/` 目录：

- `sync_lc_to_feishu.py` - 良仓库存数据同步到飞书
- `sync_yc_to_feishu.py` - 翼仓库存数据同步到飞书
- `start_sync_5min.bat` - Windows 定时任务脚本（5分钟间隔）
- `start_sync_yc_5min.bat` - Windows 定时任务脚本（翼仓）

**使用示例**：

```bash
# 单次同步（良仓）
python sync_lc_to_feishu.py --once

# 循环同步（每5分钟）
python sync_lc_to_feishu.py --interval-seconds 300

# 干运行（仅查看差异，不写入）
python sync_lc_to_feishu.py --dry-run --once

# 指定后端地址
python sync_lc_to_feishu.py --base-url http://192.168.1.26:8082 --once
```

## 功能模块

### 后端 API

#### 库存管理

- `GET /api/inventory/lc` - 查询良仓库存
- `GET /api/inventory/jf` - 查询九方库存
- `GET /api/inventory/yc` - 查询翼仓库存
- `POST /api/inventory/yc/outbound` - 查询翼仓出库单

#### 飞书多维表格操作

- `POST /api/{tableKey}/records/search` - 查询记录（支持分页）
- `POST /api/{tableKey}/create/records` - 批量创建记录
- `POST /api/{tableKey}/update/records` - 批量更新记录
- `GET /api/{tableKey}/fields` - 获取字段列表

**支持的 TableKey**：
- `warehouse` - 主仓库表
- `local` - 本地仓库表
- `warehouse-lc` - 良仓 SKU 表
- `warehouse-yc` - 翼仓 SKU 表
- `warehouse-lc-sync` - 良仓同步表
- `warehouse-yc-sync` - 翼仓同步表
- 其他自定义表（在配置文件中扩展）

#### 吉客云库存

- `POST /api/jackyun/sku/stock` - 查询吉客云 SKU 库存

#### 飞书 SKU 管理

- `GET /api/feishu/sku/{tableKey}` - 获取清洗后的 SKU 数据

#### 仓库数据聚合

- `POST /api/getLCWarehouseDataAll` - 批量获取良仓仓库数据
- `POST /api/getYCWarehouseDataAll` - 批量获取翼仓仓库数据

### 前端页面

| 路由 | 页面 | 功能描述 |
|------|------|----------|
| `/dashboard` | 仪表盘 | 系统概览、快捷入口 |
| `/lc-depot` | 良仓管理 | 良仓库存查询、数据表格展示 |
| `/yc-depot` | 翼仓管理 | 翼仓库存查询、出库单管理 |
| `/analytics` | 数据统计 | ECharts 数据可视化图表 |
| `/us-time` | 美国时区 | 美国各时区时间显示、节假日倒计时 |
| `/settings` | 系统设置 | 系统配置、光标特效设置 |

### 前端特色功能

- **Liquid Glass 玻璃拟态 UI** - 现代化半透明设计风格
- **Sleek Line Cursor** - 可配置的流光线条光标特效
- **响应式布局** - 支持侧边栏折叠、移动端适配
- **暗色模式** - 自动/手动主题切换
- **页面过渡动画** - 平滑的淡入淡出 + 滑动效果

## 项目结构详解

### 后端目录结构

```
WarehouseData/src/main/java/com/example/
├── common/              # 通用类（Result 响应体、SyncStatistics 统计）
├── config/              # 配置类
│   ├── feishu/          # 飞书配置（多表配置注册中心）
│   ├── inventory/       # 仓库配置（良仓/九方/翼仓）
│   └── jack/            # 吉客云配置
├── controller/          # REST 控制器
├── dto/                 # 数据传输对象
│   ├── feishu/          # 飞书请求/响应 DTO
│   ├── inventory/       # 库存查询 DTO
│   └── jack/            # 吉客云 DTO
├── exception/           # 全局异常处理
├── service/             # 业务逻辑层
│   └── impl/            # 服务实现
└── utils/               # 工具类（签名验证等）
```

### 前端目录结构

```
Vue-warehouse/src/
├── assets/              # 静态资源、全局样式
├── components/          # 可复用组件
│   ├── charts/          # ECharts 图表组件
│   ├── dashboard/       # 仪表盘组件
│   └── ui/              # UI 基础组件
├── composables/         # 组合式函数（时区、节假日）
├── config/              # 环境配置
├── layout/              # 布局组件（Header/Sidebar）
├── lib/                 # 工具库（API 客户端）
├── router/              # 路由配置
├── stores/              # Pinia 状态管理
├── utils/               # 工具函数（时间处理）
└── views/               # 页面视图
```

## 配置说明

### 飞书配置

在 `application.yml` 中配置多个飞书应用：

```yaml
feishu:
  warehouse:                    # 表标识
    app-id: cli_xxx             # 飞书应用 ID
    app-secret: xxx             # 飞书应用 Secret
    bitable-app-token: xxx      # 多维表格 App Token
    table-id: tbl_xxx           # 数据表 ID
    view-id: vew_xxx            # 视图 ID
```

### 仓库 API 配置

```yaml
# 良仓
lc:
  url: http://www.lc-oms.com//default/svc/web-service
  app-token: xxx
  app-key: xxx

# 九方
jf:
  url: http://szjftx.yunwms.com///default/svc/web-service
  app-token: xxx
  app-key: xxx

# 翼仓
yc:
  base-url: https://api.xlwms.com
  app-key: xxx
  app-secret: xxx
```

### Nginx 部署配置

参考 `Vue-warehouse/WarehouseManagement.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    location /api/ {
        proxy_pass http://localhost:8082/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## 常用命令

### 后端

```bash
# 编译打包
mvn clean package

# 跳过测试编译
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run

# 运行测试
mvn test
```

### 前端

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 类型检查
npm run type-check

# 生产构建
npm run build

# 代码检查
npm run lint

# 代码格式化
npm run format

# 预览构建产物
npm run preview
```

## 安全注意事项

- `application.yml` 包含敏感信息，已加入 `.gitignore`，不会提交到版本控制
- 使用 `application-example.yml` 作为配置模板
- 飞书 App Secret、数据库密码等凭证请勿硬编码
- 生产环境建议使用环境变量或密钥管理服务

## 许可证

[根据项目实际情况填写]

## 联系方式

[根据项目实际情况填写]
