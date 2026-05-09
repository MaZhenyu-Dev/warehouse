# Sleek Line Cursor 集成指南

本项目包含一个自定义的鼠标轨迹动画组件，基于 [Sleek Line Cursor](https://registry.inspira-ui.com/sleek-line-cursor.json) 构建。

## 安装说明

组件已通过 `shadcn-vue` 安装并配置。本项目已修复 Tailwind CSS v4 的 PostCSS 集成问题。

## 使用方法

该组件已集成到全局，通常在 `App.vue` 或主布局文件中使用。

```vue
<script setup lang="ts">
import SleekLineCursor from '@/components/ui/sleek-line-cursor/SleekLineCursor.vue'
</script>

<template>
  <SleekLineCursor 
    :size="50" 
    :trails="20" 
    :dampening="0.25"
    color="hsla(210, 100%, 50%, 0.3)" 
  />
  <!-- 应用的其他内容 -->
</template>
```

## 配置参数 (Props)

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `friction` | `number` | `0.5` | 轨迹物理效果的摩擦系数。 |
| `trails` | `number` | `20` | 轨迹中的线条数量。 |
| `size` | `number` | `50` | 光标效果区域的大小。 |
| `dampening` | `number` | `0.25` | 弹簧动画的阻尼系数。 |
| `tension` | `number` | `0.98` | 弹簧动画的张力系数。 |
| `opacity`       | `number`  | `0.1`   | 线条透明度 (0-1)。                                           |
| `lineWidth`     | `number`  | `1`     | 线条宽度（像素）。                                           |
| `disableMobile` | `boolean` | `false` | 是否在移动设备（触摸屏）上禁用动画。建议设为 `true` 以提升移动端体验。 |
| `color` | `string` | `undefined` | 自定义线条颜色（例如 `#ff0000`, `hsla(...)`）。如果省略，则会循环显示彩虹色。 |
| `enabledOnMobile` | `boolean` | `false` | 是否在移动设备上启用效果。默认为 `false` 以优化性能和用户体验。 |

## 移动端处理

默认情况下，该光标效果在**移动设备上是禁用的** (`enabledOnMobile: false`)，原因如下：
1.  避免干扰触摸滚动操作。
2.  避免在没有鼠标光标的设备上造成视觉混乱。
3.  减少低端设备的性能开销。

如果强制启用，组件已优化触摸事件处理，移除了 `preventDefault`，从而**不会阻塞原生页面滚动**。

## 性能优化

- 动画使用 `requestAnimationFrame` 实现流畅的 60fps 渲染。
- 使用覆盖全屏的单一 `<canvas>` 元素，并设置了 `pointer-events-none`，确保**不会阻挡任何页面交互**。
- 当页面失去焦点（Blur）时，动画逻辑会自动暂停以节省电量。

## 常见问题排查

如果您在构建过程中遇到 TypeScript 错误，请确保您的 `tsconfig.json` 已正确配置 Vue 组件支持（本项目已预置配置）。

如果遇到 PostCSS 或 Tailwind CSS 相关错误，请检查 `postcss.config.js` 是否使用了 `@tailwindcss/postcss` 插件。
