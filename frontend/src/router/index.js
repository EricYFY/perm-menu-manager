import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import MenuManage from '../views/MenuManage.vue'
import RoleButtonManage from '../views/RoleButtonManage.vue'
import FlowUmpConfig from '../views/FlowUmpConfig.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
