# LiquidGlass 组件

基于 `@wxperia/liquid-glass-vue`，该组件提供玻璃/液体扭曲效果。

## 用法

1. 导入组件。
2. 使用 `<LiquidGlass>` 包裹内容。

```vue
<script setup>
import LiquidGlass from '@/components/LiquidGlass.vue'
</script>

<template>
  <div class="container">
    <LiquidGlass
      :displacement-scale="70"
      :blur-amount="0.0625"
      :saturation="140"
      :aberration-intensity="2"
      effect="liquidGlass"
    >
      <div class="content">
        <h1>玻璃效果</h1>
        <p>此内容位于液体玻璃效果内部。</p>
      </div>
    </LiquidGlass>
  </div>
</template>
```

## 属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `displacementScale` | `number` | `70` | 控制位移效果的强度。 |
| `blurAmount` | `number` | `0.0625` | 控制模糊/霜化程度。 |
| `saturation` | `number` | `140` | 控制玻璃效果的色彩饱和度。 |
| `aberrationIntensity` | `number` | `2` | 控制色差强度。 |
| `elasticity` | `number` | `0.15` | 控制"液体"弹性（0=刚性，越高越有弹性）。 |
| `cornerRadius` | `number` | `999` | 边框半径（像素）。 |
| `padding` | `string` | `"24px 32px"` | CSS 内边距值。 |
| `overLight` | `boolean` | `false` | 玻璃是否在浅色背景上。 |
| `mouseContainer` | `Ref<HTMLElement>` | `null` | 用于追踪鼠标移动的容器元素（默认为组件自身）。 |
| `mode` | `string` | `"standard"` | 视觉模式："standard"、"polar"、"prominent"、"shader"。 |
| `globalMousePos` | `{x: number, y: number}` | - | 手动控制的全局鼠标位置。 |
| `mouseOffset` | `{x: number, y: number}` | - | 鼠标位置偏移量，用于微调。 |
| `effect` | `string` | `"liquidGlass"` | 着色器效果类型："flowingLiquid"、"liquidGlass"、"transparentIce"、"unevenGlass"、"mosaicGlass"。 |

## 注意事项

- `class` 和 `style` 属性可以直接传递给组件，并将应用于根元素。
- 确保项目中已安装 `@wxperia/liquid-glass-vue`。
