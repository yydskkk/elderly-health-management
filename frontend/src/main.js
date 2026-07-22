import { createApp, h } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './assets/styles/global.css'

// 导入自定义健康数据图标（SVG 文件，用 ?raw 获取字符串）
import bloodPressureSvg from './assets/icons/blood-pressure.svg?raw'
import bloodSugarSvg from './assets/icons/blood-sugar.svg?raw'
import heartRateSvg from './assets/icons/heart-rate.svg?raw'
import bloodOxygenSvg from './assets/icons/blood-oxygen.svg?raw'

const app = createApp(App)

// 注册 Element Plus 全部图标为全局组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册自定义 SVG 图标为全局组件
// 用于 typeList 字符串配置，放在 <el-icon :size="N"> 内会继承 font-size 自动缩放
// SVG 内 fill="currentColor"，会继承父元素 color 实现着色
const svgIcons = {
  BloodPressureIcon: bloodPressureSvg,
  BloodSugarIcon: bloodSugarSvg,
  HeartRateIcon: heartRateSvg,
  BloodOxygenIcon: bloodOxygenSvg
}

for (const [name, svg] of Object.entries(svgIcons)) {
  app.component(name, {
    render: () => h('span', {
      class: 'custom-svg-icon',
      innerHTML: svg,
      style: 'display: inline-flex; align-items: center; justify-content: center; line-height: 0;'
    })
  })
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.use(createPinia())

app.mount('#app')
