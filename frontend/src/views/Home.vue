<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" @click="$router.push('/menu')" class="nav-card">
          <el-icon class="icon"><Menu /></el-icon>
          <h3>菜单层级管理</h3>
          <p>管理系统菜单和权限层级</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" @click="$router.push('/role-button')" class="nav-card">
          <el-icon class="icon"><User /></el-icon>
          <h3>角色按钮管理</h3>
          <p>配置各角色的功能按钮权限</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" @click="$router.push('/flow-ump')" class="nav-card">
          <el-icon class="icon"><Connection /></el-icon>
          <h3>总线视角扭转管理</h3>
          <p>配置总线视角操作与步骤扭转</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-container {
  padding: 40px;
}
.nav-card {
  cursor: pointer;
  text-align: center;
  padding: 40px 20px;
  transition: all 0.3s;
}
.nav-card:hover {
  transform: translateY(-5px);
}
.icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
}
</style>
