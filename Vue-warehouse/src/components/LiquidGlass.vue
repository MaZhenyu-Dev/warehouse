<script setup lang="ts">
import { LiquidGlass } from '@wxperia/liquid-glass-vue'

type EffectType = 'flowingLiquid' | 'liquidGlass' | 'transparentIce' | 'unevenGlass' | 'mosaicGlass'

interface Props {
  displacementScale?: number
  blurAmount?: number
  saturation?: number
  aberrationIntensity?: number
  elasticity?: number
  cornerRadius?: number
  padding?: string
  overLight?: boolean
  mouseContainer?: HTMLElement
  globalMousePos?: { x: number; y: number }
  mouseOffset?: { x: number; y: number }
  effect?: EffectType
}

const props = withDefaults(defineProps<Props>(), {
  displacementScale: 70,
  blurAmount: 0.0625,
  saturation: 140,
  aberrationIntensity: 2,
  elasticity: 0.15,
  cornerRadius: 999,
  padding: '24px 32px',
  overLight: false,
  mouseContainer: undefined,
  effect: 'liquidGlass'
})
</script>

<template>
  <LiquidGlass v-bind="props">
    <slot />
  </LiquidGlass>
</template>
