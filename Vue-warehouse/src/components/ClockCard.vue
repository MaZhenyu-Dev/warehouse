<template>
  <div class="relative overflow-hidden glass-card p-6 flex flex-col items-center justify-center min-h-[220px]">
    <!-- Dynamic Background (Orbs) - Opacity adjusted for themes via CSS variables or classes if needed, keeping it subtle -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none -z-10">
       <div class="orb orb-1 dark:opacity-30 opacity-10"></div>
       <div class="orb orb-2 dark:opacity-30 opacity-10"></div>
       <div class="orb orb-3 dark:opacity-30 opacity-10"></div>
    </div>

    <div class="text-center w-full z-10">
      <Transition name="greeting" mode="out-in">
        <div :key="greeting" class="greeting text-base font-medium text-muted-foreground mb-3 tracking-widest uppercase">
          {{ greeting }}
        </div>
      </Transition>
      
      <div class="flex items-center justify-center gap-1 sm:gap-2 mb-4">
        <!-- Hours -->
        <div class="time-block flex flex-col items-center">
          <div class="time-value text-4xl sm:text-5xl font-bold text-foreground leading-none relative min-w-[60px] flex justify-center">
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.hours[0] }">{{ timeData.hours[0] }}</span>
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.hours[1] }">{{ timeData.hours[1] }}</span>
          </div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-widest mt-1">时</div>
        </div>
        
        <div class="text-3xl sm:text-4xl font-thin text-muted-foreground pb-4 animate-pulse">:</div>
        
        <!-- Minutes -->
        <div class="time-block flex flex-col items-center">
          <div class="time-value text-4xl sm:text-5xl font-bold text-foreground leading-none relative min-w-[60px] flex justify-center">
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.minutes[0] }">{{ timeData.minutes[0] }}</span>
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.minutes[1] }">{{ timeData.minutes[1] }}</span>
          </div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-widest mt-1">分</div>
        </div>
        
        <div class="text-3xl sm:text-4xl font-thin text-muted-foreground pb-4 animate-pulse">:</div>
        
        <!-- Seconds -->
        <div class="time-block flex flex-col items-center">
          <div class="time-value text-4xl sm:text-5xl font-bold text-foreground leading-none relative min-w-[60px] flex justify-center">
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.seconds[0] }">{{ timeData.seconds[0] }}</span>
             <span class="digit inline-block transition-all duration-300" :class="{ flip: flipState.seconds[1] }">{{ timeData.seconds[1] }}</span>
          </div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-widest mt-1">秒</div>
        </div>
      </div>

      <!-- Seconds Bar -->
      <div class="w-full h-1 bg-muted rounded-full mb-4 overflow-hidden">
        <div class="h-full bg-gradient-to-r from-blue-400 via-purple-500 to-pink-500 bg-[length:200%_100%] animate-gradient transition-all duration-100 ease-linear" :style="{ width: secondsProgress + '%' }"></div>
      </div>

      <!-- Date -->
      <div class="flex flex-wrap items-center justify-center gap-2 sm:gap-3">
        <div class="date-item px-3 py-1.5 bg-muted/30 rounded-lg border border-border/10 backdrop-blur-sm transition-transform hover:-translate-y-1">
          <div class="text-lg font-light text-foreground">{{ dateData.year }}</div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-wider mt-0.5">年</div>
        </div>
        <div class="date-item px-3 py-1.5 bg-muted/30 rounded-lg border border-border/10 backdrop-blur-sm transition-transform hover:-translate-y-1">
          <div class="text-lg font-light text-foreground">{{ dateData.month }}</div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-wider mt-0.5">月</div>
        </div>
        <div class="date-item px-3 py-1.5 bg-muted/30 rounded-lg border border-border/10 backdrop-blur-sm transition-transform hover:-translate-y-1">
          <div class="text-lg font-light text-foreground">{{ dateData.day }}</div>
          <div class="text-[15px] text-muted-foreground uppercase tracking-wider mt-0.5">日</div>
        </div>
      </div>
      
      <div class="mt-3 text-base text-muted-foreground/80 tracking-widest bg-gradient-to-br from-blue-500/10 to-purple-500/10 px-3 py-1 rounded-full inline-block">
        {{ dateData.weekday }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'

const greeting = ref('欢迎')
const secondsProgress = ref(0)
const timeData = reactive({
  hours: '00',
  minutes: '00',
  seconds: '00'
})

// Store last known time to handle animations
const lastTime = {
  hours: '',
  minutes: '',
  seconds: ''
}

const dateData = reactive({
  year: 2024,
  month: '01',
  day: '01',
  weekday: '星期一'
})

const flipState = reactive({
  hours: [false, false],
  minutes: [false, false],
  seconds: [false, false]
})

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
let intervalId: number | null = null

const updateGreeting = (hour: number) => {
  if (hour >= 5 && hour < 9) return '早上好呀,美好的一天即将开始 😀'
  if (hour >= 9 && hour < 12) return '上午好呀,今天的订单销量是否满意呢? ☺️'
  if (hour >= 12 && hour < 14) return '中午好呀,该歇息啦，辛苦喽! 🥰'
  if (hour >= 14 && hour < 18) return '下午好呀,坚持下去,充实的一天快结束啦! 😘'
  if (hour >= 18 && hour < 22) return '晚上好呀,请不要熬夜哦! 🌙'
  return '夜深啦,晚安!做个好梦吧! ⭐'
}

const updateTime = () => {
  const now = new Date()
  const hours = now.getHours()
  const minutes = now.getMinutes()
  const seconds = now.getSeconds()
  const milliseconds = now.getMilliseconds()

  greeting.value = updateGreeting(hours)

  const newHours = hours.toString().padStart(2, '0')
  const newMinutes = minutes.toString().padStart(2, '0')
  const newSeconds = seconds.toString().padStart(2, '0')

  // Check flips for Hours
  if (lastTime.hours !== newHours) {
     if (lastTime.hours === '') {
        timeData.hours = newHours
     } else {
        if(lastTime.hours[0] !== newHours[0]) triggerFlip('hours', 0)
        if(lastTime.hours[1] !== newHours[1]) triggerFlip('hours', 1)
        setTimeout(() => { timeData.hours = newHours }, 300)
     }
     lastTime.hours = newHours
  }

  // Check flips for Minutes
  if (lastTime.minutes !== newMinutes) {
     if (lastTime.minutes === '') {
        timeData.minutes = newMinutes
     } else {
        if(lastTime.minutes[0] !== newMinutes[0]) triggerFlip('minutes', 0)
        if(lastTime.minutes[1] !== newMinutes[1]) triggerFlip('minutes', 1)
        setTimeout(() => { timeData.minutes = newMinutes }, 300)
     }
     lastTime.minutes = newMinutes
  }

  // Check flips for Seconds
  if (lastTime.seconds !== newSeconds) {
     if (lastTime.seconds === '') {
        timeData.seconds = newSeconds
     } else {
        if(lastTime.seconds[0] !== newSeconds[0]) triggerFlip('seconds', 0)
        if(lastTime.seconds[1] !== newSeconds[1]) triggerFlip('seconds', 1)
        setTimeout(() => { timeData.seconds = newSeconds }, 300)
     }
     lastTime.seconds = newSeconds
  }

  secondsProgress.value = ((seconds * 1000 + milliseconds) / 60000) * 100

  dateData.year = now.getFullYear()
  dateData.month = (now.getMonth() + 1).toString().padStart(2, '0')
  dateData.day = now.getDate().toString().padStart(2, '0')
  dateData.weekday = weekdays[now.getDay()] || ''
}

const triggerFlip = (type: 'hours' | 'minutes' | 'seconds', index: number) => {
  flipState[type][index] = true
  setTimeout(() => {
    flipState[type][index] = false
  }, 600)
}

onMounted(() => {
  updateTime()
  intervalId = window.setInterval(updateTime, 50)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
/* Orb Animations */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: -30%;
  left: -30%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  bottom: -20%;
  right: -20%;
  animation-delay: -5s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  top: 40%;
  right: 10%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.9); }
  75% { transform: translate(-30px, -10px) scale(1.05); }
}

/* Flip Animation */
.flip {
  animation: flip 0.6s ease-in-out;
}

@keyframes flip {
  0% { transform: rotateX(0deg); opacity: 1; }
  50% { transform: rotateX(-90deg); opacity: 0; }
  100% { transform: rotateX(0deg); opacity: 1; }
}

.animate-gradient {
  animation: gradient 3s ease infinite;
}

@keyframes gradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.animate-slide-down {
  animation: slideDown 0.8s ease-out 0.2s both;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.greeting-enter-active {
  animation: greetingEnter 0.5s ease forwards;
}

.greeting-leave-active {
  animation: greetingLeave 0.5s ease forwards;
}

@keyframes greetingEnter {
  from { opacity: 0; transform: translateY(-15px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes greetingLeave {
  from { opacity: 1; transform: translateY(0) scale(1); }
  to { opacity: 0; transform: translateY(15px) scale(0.95); }
}
</style>
