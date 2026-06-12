<template>
  <div class="trial-assignment-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">试岗安排</span>
          <el-button 
            type="primary" 
            @click="handleArrange"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            安排试岗
          </el-button>
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
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable 
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="待试岗" value="pending" />
            <el-option label="试岗中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="未到岗" value="absent" />
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
        <el-table-column prop="trialShift" label="班次" width="80">
          <template #default="{ row }">
            <el-tag :type="getShiftType(row.trialShift)" size="small">
              {{ getShiftText(row.trialShift) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="arrangeUserName" label="安排人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending' || row.status === 'in_progress'" 
              type="primary" 
              size="small" 
              link 
              @click="handleRecord(row)"
            >
              记录试岗
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="warning" 
              size="small" 
              link 
              @click="handleMarkAbsent(row)"
            >
              标记未到岗
            </el-button>
            <el-button 
              v-if="row.status === 'pending' || row.status === 'in_progress'" 
              type="danger" 
              size="small" 
              link 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              link 
              @click="handleViewRecord(row)"
            >
              查看记录
            </el-button>
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

    <el-dialog v-model="arrangeDialogVisible" title="安排试岗" width="550px" :close-on-click-modal="false">
      <el-form :model="arrangeForm" :rules="arrangeRules" ref="arrangeFormRef" label-width="100px">
        <el-form-item label="候选人" prop="candidateId">
          <el-select 
            v-model="arrangeForm.candidateId" 
            placeholder="请选择候选人" 
            filterable
            class="w-full"
          >
            <el-option 
              v-for="item in passedCandidates" 
              :key="item.id" 
              :label="item.candidateName + ' - ' + item.position" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班组" prop="teamId">
          <el-select v-model="arrangeForm.teamId" placeholder="请选择班组" class="w-full" @change="handleTeamChange">
            <el-option 
              v-for="item in teamList" 
              :key="item.id" 
              :label="item.teamName + '（剩余' + (item.quota - item.usedQuota) + '名额）'" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试岗日期" prop="trialDate">
          <el-date-picker
            v-model="arrangeForm.trialDate"
            type="date"
            placeholder="请选择试岗日期"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="试岗班次" prop="trialShift">
          <el-select v-model="arrangeForm.trialShift" placeholder="请选择班次" class="w-full">
            <el-option label="早班" value="morning" />
            <el-option label="中班" value="afternoon" />
            <el-option label="晚班" value="night" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="arrangeForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="arrangeForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="arrangeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="arrangeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitArrange" :loading="submitting">确认安排</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" title="记录试岗情况" width="600px" :close-on-click-modal="false">
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="120px">
        <el-form-item label="是否到岗" prop="isArrived">
          <el-radio-group v-model="recordForm.isArrived">
            <el-radio :value="1">已到岗</el-radio>
            <el-radio :value="0">未到岗</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="到岗时间">
          <el-date-picker
            v-model="recordForm.arriveTime"
            type="datetime"
            placeholder="请选择到岗时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-full"
          />
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="安全培训">
          <el-radio-group v-model="recordForm.safetyTraining">
            <el-radio :value="1">已完成</el-radio>
            <el-radio :value="0">未完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="培训内容">
          <el-input v-model="recordForm.trainingContent" type="textarea" :rows="2" placeholder="请输入培训内容" />
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="试岗表现评分">
          <el-rate v-model="recordForm.performanceScore" :max="10" show-text :texts="scoreTexts" />
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="试岗表现评语">
          <el-input v-model="recordForm.performanceRemark" type="textarea" :rows="3" placeholder="请输入试岗表现评语" />
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="能否转正" prop="canConvert">
          <el-radio-group v-model="recordForm.canConvert">
            <el-radio value="pending">待定</el-radio>
            <el-radio value="yes">可以</el-radio>
            <el-radio value="no">不可以</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="recordForm.isArrived === 1" label="转正意见">
          <el-input v-model="recordForm.convertRemark" type="textarea" :rows="2" placeholder="请输入转正意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="recordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRecord" :loading="submitting">提交记录</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDetailVisible" title="试岗记录详情" width="600px">
      <el-descriptions :column="2" border v-if="recordDetail">
        <el-descriptions-item label="候选人">{{ recordDetail.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="班组">{{ recordDetail.teamName }}</el-descriptions-item>
        <el-descriptions-item label="试岗日期">{{ recordDetail.trialDate }}</el-descriptions-item>
        <el-descriptions-item label="是否到岗">
          <el-tag :type="recordDetail.isArrived === 1 ? 'success' : 'danger'" size="small">
            {{ recordDetail.isArrived === 1 ? '已到岗' : '未到岗' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="到岗时间" v-if="recordDetail.isArrived === 1">
          {{ formatDateTime(recordDetail.arriveTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="安全培训" v-if="recordDetail.isArrived === 1">
          <el-tag :type="recordDetail.safetyTraining === 1 ? 'success' : 'warning'" size="small">
            {{ recordDetail.safetyTraining === 1 ? '已完成' : '未完成' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="培训内容" v-if="recordDetail.isArrived === 1 && recordDetail.trainingContent" :span="2">
          {{ recordDetail.trainingContent }}
        </el-descriptions-item>
        <el-descriptions-item label="表现评分" v-if="recordDetail.isArrived === 1">
          <el-rate :model-value="recordDetail.performanceScore" disabled :max="10" show-score text-color="#ff9900" />
        </el-descriptions-item>
        <el-descriptions-item label="能否转正" v-if="recordDetail.isArrived === 1">
          <el-tag :type="getConvertType(recordDetail.canConvert)" size="small">
            {{ getConvertText(recordDetail.canConvert) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="表现评语" v-if="recordDetail.isArrived === 1 && recordDetail.performanceRemark" :span="2">
          {{ recordDetail.performanceRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="转正意见" v-if="recordDetail.isArrived === 1 && recordDetail.convertRemark" :span="2">
          {{ recordDetail.convertRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="记录人">{{ recordDetail.recordUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ formatDateTime(recordDetail.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <div v-else class="text-center text-gray-500 py-8">暂无试岗记录</div>
    </el-dialog>

    <el-dialog v-model="cancelDialogVisible" title="取消试岗" width="450px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitCancel" :loading="submitting">确认取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { trialApi } from '@/api/trial'
import { teamApi } from '@/api/team'
import { candidateApi } from '@/api/candidate'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const teamList = ref([])
const passedCandidates = ref([])
const dateRange = ref([])
const currentAssignmentId = ref(null)

const arrangeDialogVisible = ref(false)
const arrangeFormRef = ref(null)
const recordDialogVisible = ref(false)
const recordFormRef = ref(null)
const recordDetailVisible = ref(false)
const cancelDialogVisible = ref(false)
const recordDetail = ref(null)

const searchForm = reactive({
  candidateName: '',
  teamId: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const arrangeForm = reactive({
  candidateId: null,
  teamId: null,
  trialDate: '',
  trialShift: 'morning',
  startTime: '',
  endTime: '',
  remark: '',
  arrangeUserId: 1,
  arrangeUserName: '管理员'
})

const arrangeRules = {
  candidateId: [
    { required: true, message: '请选择候选人', trigger: 'change' }
  ],
  teamId: [
    { required: true, message: '请选择班组', trigger: 'change' }
  ],
  trialDate: [
    { required: true, message: '请选择试岗日期', trigger: 'change' }
  ],
  trialShift: [
    { required: true, message: '请选择试岗班次', trigger: 'change' }
  ]
}

const recordForm = reactive({
  assignmentId: null,
  isArrived: 1,
  arriveTime: '',
  safetyTraining: 0,
  trainingContent: '',
  performanceScore: 0,
  performanceRemark: '',
  canConvert: 'pending',
  convertRemark: '',
  recordUserId: 1,
  recordUserName: '管理员'
})

const recordRules = {
  isArrived: [
    { required: true, message: '请选择是否到岗', trigger: 'change' }
  ]
}

const cancelForm = reactive({
  reason: ''
})

const scoreTexts = ['极差', '很差', '较差', '一般', '及格', '良好', '较好', '优秀', '极好', '完美']

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
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await trialApi.assignmentPage(params)
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

const loadPassedCandidates = async () => {
  try {
    const res = await candidateApi.page({
      current: 1,
      size: 100,
      interviewStatus: 'passed',
      isInRisk: 0
    })
    if (res.code === 200) {
      passedCandidates.value = res.data.records
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
    status: ''
  })
  dateRange.value = []
  handleSearch()
}

const handleArrange = () => {
  Object.assign(arrangeForm, {
    candidateId: null,
    teamId: null,
    trialDate: '',
    trialShift: 'morning',
    startTime: '',
    endTime: '',
    remark: ''
  })
  loadPassedCandidates()
  arrangeDialogVisible.value = true
  if (arrangeFormRef.value) arrangeFormRef.value.clearValidate()
}

const handleTeamChange = () => {
}

const submitArrange = async () => {
  if (!arrangeFormRef.value) return
  await arrangeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await trialApi.arrangeTrial(arrangeForm)
        if (res.code === 200) {
          ElMessage.success('安排成功')
          arrangeDialogVisible.value = false
          loadData()
          loadTeamList()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleRecord = (row) => {
  currentAssignmentId.value = row.id
  Object.assign(recordForm, {
    assignmentId: row.id,
    isArrived: 1,
    arriveTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    safetyTraining: 0,
    trainingContent: '',
    performanceScore: 0,
    performanceRemark: '',
    canConvert: 'pending',
    convertRemark: ''
  })
  recordDialogVisible.value = true
  if (recordFormRef.value) recordFormRef.value.clearValidate()
}

const submitRecord = async () => {
  if (!recordFormRef.value) return
  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await trialApi.recordTrial(recordForm)
        if (res.code === 200) {
          ElMessage.success('记录成功')
          recordDialogVisible.value = false
          loadData()
          loadTeamList()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleMarkAbsent = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记该候选人未到岗吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--warning'
    })
    const res = await trialApi.markAbsent(row.id)
    if (res.code === 200) {
      ElMessage.success('已标记未到岗')
      loadData()
      loadTeamList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCancel = (row) => {
  currentAssignmentId.value = row.id
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

const submitCancel = async () => {
  submitting.value = true
  try {
    const res = await trialApi.cancelTrial(currentAssignmentId.value, cancelForm.reason)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      cancelDialogVisible.value = false
      loadData()
      loadTeamList()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleViewRecord = async (row) => {
  try {
    const res = await trialApi.getRecordByAssignmentId(row.id)
    if (res.code === 200) {
      recordDetail.value = res.data
      recordDetailVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const getShiftType = (shift) => {
  const types = {
    morning: 'success',
    afternoon: 'warning',
    night: 'danger'
  }
  return types[shift] || 'info'
}

const getShiftText = (shift) => {
  const texts = {
    morning: '早班',
    afternoon: '中班',
    night: '晚班'
  }
  return texts[shift] || shift
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    in_progress: 'primary',
    completed: 'success',
    cancelled: 'info',
    absent: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待试岗',
    in_progress: '试岗中',
    completed: '已完成',
    cancelled: '已取消',
    absent: '未到岗'
  }
  return texts[status] || status
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
.trial-assignment-page {
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
