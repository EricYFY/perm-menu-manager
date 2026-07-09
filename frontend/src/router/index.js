import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import MenuManage from '../views/MenuManage.vue'
import RoleButtonManage from '../views/RoleButtonManage.vue'
import FlowUmpConfig from '../views/FlowUmpConfig.vue'
import DictManage from '../views/DictManage.vue'
import TrxConfigManage from '../views/TrxConfigManage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/menu',
    name: 'MenuManage',
    component: MenuManage
  },
  {
    path: '/role-button',
    name: 'RoleButtonManage',
    component: RoleButtonManage
  },
  {
    path: '/flow-ump',
    name: 'FlowUmpConfig',
    component: FlowUmpConfig
  },
  {
    path: '/dict',
    name: 'DictManage',
    component: DictManage
  },
  {
    path: '/trx-config',
    name: 'TrxConfigManage',
    component: TrxConfigManage
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
