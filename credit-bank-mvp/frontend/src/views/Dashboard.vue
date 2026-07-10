<template>
  <div class="dashboard">
    <div class="welcome">
      <h2>欢迎回来，{{ currentUser?.realName }}</h2>
      <p>{{ welcomeText }}</p>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="stat in statCards" :key="stat.label">
        <el-card class="stat-card" :class="stat.color">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}<small v-if="stat.unit">{{ stat.unit }}</small></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="text" @click="$router.push('/process')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="todoList" border style="width: 100%;">
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="initiator" label="发起人" />
            <el-table-column prop="time" label="提交时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.statusType">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近交易记录</span>
              <el-button type="text" @click="$router.push('/transactions')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="recentTransactions" border style="width: 100%;">
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="change" label="变动">
              <template #default="scope">
                <span :style="{ color: scope.row.isPositive ? '#0b7a4f' : '#e03131' }">{{ scope.row.change }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" />
            <el-table-column prop="source" label="来源" />
            <el-table-column prop="time" label="时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>积分概览（最近7天）</span>
          <el-button type="text" @click="$router.push('/transactions')">查看对账 →</el-button>
        </div>
      </template>
      <el-table :data="pointOverview" border style="width: 100%;">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="earn" label="加分总量" />
        <el-table-column prop="spend" label="扣分总量" />
        <el-table-column prop="convert" label="转换转入" />
        <el-table-column prop="activity" label="活动奖励" />
        <el-table-column prop="net" label="净增">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.net }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const welcomeText = computed(() => {
  const texts = {
    SYSTEM_ADMIN: '系统管理员负责全局配置与监管，包括机构审核、规则定义、全局对账等核心职能。',
    INSTITUTION_ADMIN: '机构管理员负责本机构的专属规则、项目发布、活动管理、专家聘用及积分池流水查看。',
    STUDENT: '在这里查看积分余额、参与项目活动、申请学分转换，记录你的终身学习成长轨迹。',
    EXPERT: '专家可查看聘用机构、评审指派的项目、维护个人资料与研究方向。'
  }
  return texts[currentUser.value?.userType] || ''
})

const statCards = computed(() => {
  const cards = {
    SYSTEM_ADMIN: [
      { label: '平台总用户数', value: '1,286', unit: '人', color: '' },
      { label: '入驻机构数', value: '36', unit: '家', color: 'green' },
      { label: '待审核事项', value: '8', unit: '条', color: 'orange' },
      { label: '全平台积分总量', value: '458,920', unit: '', color: 'red' }
    ],
    INSTITUTION_ADMIN: [
      { label: '本机构学生数', value: '328', unit: '人', color: '' },
      { label: '机构积分池余额', value: '28,600', unit: '', color: 'green' },
      { label: '在研项目数', value: '12', unit: '个', color: 'orange' },
      { label: '待审批申请', value: '5', unit: '条', color: 'red' }
    ],
    STUDENT: [
      { label: '我的积分余额', value: '1,280', unit: '', color: '' },
      { label: '累计获得积分', value: '2,460', unit: '', color: 'green' },
      { label: '进行中项目', value: '3', unit: '个', color: 'orange' },
      { label: '待处理申请', value: '1', unit: '条', color: 'red' }
    ],
    EXPERT: [
      { label: '待评审项目', value: '4', unit: '个', color: '' },
      { label: '累计评审项目', value: '86', unit: '个', color: 'green' },
      { label: '聘用机构数', value: '3', unit: '家', color: 'orange' },
      { label: '评审积分', value: '3,200', unit: '', color: 'red' }
    ]
  }
  return cards[currentUser.value?.userType] || []
})

const todoList = [
  { type: '机构入驻审核', initiator: '上海培训中心', time: '2026-07-08 10:22', status: '待审核', statusType: 'warning' },
  { type: '专家入驻审核', initiator: '赵教授', time: '2026-07-08 09:15', status: '待审核', statusType: 'warning' },
  { type: '项目合规审核', initiator: '深圳职业培训', time: '2026-07-07 16:40', status: '待审核', statusType: 'warning' },
  { type: '转换规则修订', initiator: '系统', time: '2026-07-07 14:00', status: '处理中', statusType: 'info' }
]

const recentTransactions = [
  { type: '活动奖励', change: '+50', balance: '1,280', source: '暑期读书会', time: '07-08 15:30', isPositive: true },
  { type: '项目奖励', change: '+200', balance: '1,230', source: 'Python数据分析', time: '07-05 10:00', isPositive: true },
  { type: '转换转入', change: '+180', balance: '1,030', source: '课程学分转换', time: '07-03 14:20', isPositive: true },
  { type: '规则加分', change: '+10', balance: '850', source: '完成一门课程', time: '07-01 09:00', isPositive: true }
]

const pointOverview = [
  { date: '2026-07-09', earn: '+12,400', spend: '-1,200', convert: '+3,600', activity: '+2,800', net: '+17,600' },
  { date: '2026-07-08', earn: '+10,200', spend: '-800', convert: '+2,400', activity: '+3,500', net: '+15,300' },
  { date: '2026-07-07', earn: '+9,800', spend: '-600', convert: '+1,800', activity: '+2,200', net: '+13,200' }
]
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome {
  background: linear-gradient(135deg, #3b5bdb 0%, #5c7bf5 100%);
  color: #fff;
  border-radius: 10px;
  padding: 22px 26px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(59, 91, 219, 0.25);
}

.welcome h2 {
  font-size: 20px;
  margin-bottom: 6px;
}

.welcome p {
  opacity: 0.9;
  font-size: 13px;
}

.stat-card {
  border-left: 4px solid #3b5bdb;
}

.stat-card.green {
  border-left-color: #0f6e56;
}

.stat-card.orange {
  border-left-color: #f59f00;
}

.stat-card.red {
  border-left-color: #e03131;
}

.stat-label {
  font-size: 13px;
  color: #868e96;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-value small {
  font-size: 14px;
  font-weight: normal;
  color: #868e96;
  margin-left: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
