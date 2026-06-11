import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import './assets/global.css'
import './assets/classroom-theme.css'
import './assets/mobile-shell.css'
import '@fortawesome/fontawesome-free/css/all.css';
import heroBg from './assets/landing-hero.png'

const preloadHero = new Image()
preloadHero.src = heroBg

createApp(App).use(router).mount('#app')