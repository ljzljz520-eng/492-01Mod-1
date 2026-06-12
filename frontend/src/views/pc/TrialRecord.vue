<template>
  <div class="trial-record-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">试岗记录</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="候选人">
          <el-input 
            v-model="searchForm.candidateName" 
            placeholder="请输入候选人姓名" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="班组">
          <el-select 
            v-model="searchForm.teamId" 
            placeholder="请选择班组" 
            clearable 
            style="width: 160px"
            class="rounded-lg"
          >
            <el-option 
              v-for="item in teamList" 
              :key="item.id" 
              :label="item.teamName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="能否转正">
          <el-select 
            v-model="searchForm.canConvert" 
            placeholder="请选择" 
            clearable 
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="待定" value="pending" />
            <el-option label="可以" value="yes" />
            <el-option label="不可以" value="no" />
          </el-select>
        </el-form-item>
        <el-form-item label="试岗日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button 
            @click="handleReset"
            class="rounded-lg"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="candidateName" label="候选人" width="100" />
        <el-table-column prop="teamName" label="班组" width="130" />
        <el-table-column prop="trialDate" label="试岗日期" width="120" />
        <el-table-column prop="isArrived" label="到岗情况" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isArrived === 1 ? 'success' : 'danger'" size="small">
              {{ row.isArrived === 1 ? '已到岗' : '未到岗' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="arriveTime" label="到岗时间" width="160">
          <template #default="{ row }">
            {{ row.isArrived === 1 ? formatDateTime(row.arriveTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="safetyTraining" label="安全培训" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isArrived === 1" :type="row.safetyTraining === 1 ? 'success' : 'warning'" size="small">
              {{ row.safetyTraining === 1 ? '已完成' : '未完成' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="performanceScore" label="表现评分" width="120">
          <template #default="{ row }">
            <span v-if="row.isArrived === 1 && row.performanceScore">
              <el-rate :model-value="row.performanceScore" disabled :max="10" size="small" />
              <span class="ml-1 text-gray-600">{{ row.performanceScore }}分</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="canConvert" label="能否转正" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isArrived === 1" :type="getConvertType(row.canConvert)" size="small">
              {{ getConvertText(row.canConvert) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordUserName" label="记录人" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="rounded-lg"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="试岗记录详情" width="650px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="候选人">{{ currentRecord.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="班组">{{ currentRecord.teamName }}</el-descriptions-item>
        <el-descriptions-item label="试岗日期">{{ currentRecord.trialDate }}</el-descriptions-item>
        <el-descriptions-item label="是否到岗">
          <el-tag :type="currentRecord.isArrived === 1 ? 'success' : 'danger'" size="small">
            {{ currentRecord.isArrived === 1 ? '已到岗' : '未到岗' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="到岗时间" v-if="currentRecord.isArrived === 1">
          {{ formatDateTime(currentRecord.arriveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="安全培训" v-if="currentRecord.isArrived === 1">
          <el-tag :type="currentRecord.safetyTraining === 1 ? 'success' : 'warning'" size="small">
            {{ currentRecord.safetyTraining === 1 ? '已完成' : '未完成' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="培训内容" v-if="currentRecord.isArrived === 1 && currentRecord.trainingContent" :span="2">
          {{ currentRecord.trainingContent }}
        </el-descriptions-item>
        <el-descriptions-item label="表现评分" v-if="currentRecord.isArrived === 1">
          <el-rate :model-value="currentRecord.performanceScore" disabled :max="10" show-score text-color="#ff9900" />
        </el-descriptions-item>
        <el-descriptions-item label="能否转正" v-if="currentRecord.isArrived === 1">
          <el-tag :type="getConvertType(currentRecord.canConvert)" size="small">
            {{ getConvertText(currentRecord.canConvert) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="表现评语" v-if="currentRecord.isArrived === 1 && currentRecord.performanceRemark" :span="2">
          {{ currentRecord.performanceRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="转正意见" v-if="currentRecord.isArrived === 1 && currentRecord.convertRemark" :span="2">
          {{ currentRecord.convertRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="记录人">{{ currentRecord.recordUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { trialApi } from '@/api/trial'
import { teamApi } from '@/api/team'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const teamList = ref([])
const dateRange = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

const searchForm = reactive({
  candidateName: '',
  teamId: '',
  canConvert: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    if (params.teamId === '') {
      delete params.teamId
    }
    if (params.canConvert === '') {
      delete params.canConvert
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await trialApi.recordPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadTeamList = async () => {
  try {
    const res = await teamApi.list()
    if (res.code === 200) {
      teamList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    candidateName: '',
    teamId: '',
    canConvert: ''
  })
  dateRange.value = []
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await trialApi.getRecordById(row.id)
    if (res.code === 200) {
      currentRecord.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const getConvertType = (val) => {
  const types = {
    pending: 'warning',
    yes: 'success',
    no: 'danger'
  }
  return types[val] || 'info'
}

const getConvertText = (val) => {
  const texts = {
    pending: '待定',
    yes: '可以',
    no: '不可以'
  }
  return texts[val] || val
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadData()
  loadTeamList()
})
</script>

<style scoped>
.trial-record-page {
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
