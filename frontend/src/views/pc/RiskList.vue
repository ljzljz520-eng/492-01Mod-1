<template>
  <div class="risk-list-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">风险名单</span>
          <el-button 
            type="danger" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Warning /></el-icon>
            手动加入
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="候选人姓名">
          <el-input 
            v-model="searchForm.candidateName" 
            placeholder="请输入候选人姓名" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable 
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="生效中" value="active" />
            <el-option label="已移除" value="removed" />
          </el-select>
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
        <el-table-column prop="candidateName" label="候选人姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="absentCount" label="爽约次数" width="100">
          <template #default="{ row }">
            <el-tag :type="row.absentCount >= 3 ? 'danger' : 'warning'" size="small">
              {{ row.absentCount || 0 }}次
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="列入原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="列入时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="removeReason" label="移除原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.removeReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'active'" 
              type="success" 
              size="small" 
              link 
              @click="handleRemove(row)"
            >
              移除名单
            </el-button>
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

    <el-dialog v-model="addDialogVisible" title="手动加入风险名单" width="500px" :close-on-click-modal="false">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="候选人" prop="candidateId">
          <el-select 
            v-model="addForm.candidateId" 
            placeholder="请选择候选人" 
            filterable
            class="w-full"
          >
            <el-option 
              v-for="item in candidateList" 
              :key="item.id" 
              :label="item.candidateName + ' - ' + item.phone" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="列入原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" :rows="4" placeholder="请输入列入原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitAdd" :loading="submitting">确认加入</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="removeDialogVisible" title="移除风险名单" width="450px" :close-on-click-modal="false">
      <el-form :model="removeForm" :rules="removeRules" ref="removeFormRef" label-width="80px">
        <el-form-item label="移除原因" prop="removeReason">
          <el-input v-model="removeForm.removeReason" type="textarea" :rows="3" placeholder="请输入移除原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="removeDialogVisible = false">取消</el-button>
          <el-button type="success" @click="submitRemove" :loading="submitting">确认移除</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="风险记录详情" width="550px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="候选人">{{ currentRecord.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRecord.phone }}</el-descriptions-item>
        <el-descriptions-item label="爽约次数">{{ currentRecord.absentCount || 0 }}次</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)" size="small">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="列入原因" :span="2">{{ currentRecord.reason }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRecord.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="列入时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.status === 'removed'" label="移除原因" :span="2">
          {{ currentRecord.removeReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.status === 'removed'" label="移除时间" :span="2">
          {{ formatDateTime(currentRecord.removeTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { riskListApi } from '@/api/riskList'
import { candidateApi } from '@/api/candidate'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const candidateList = ref([])
const currentRiskId = ref(null)
const currentRecord = ref(null)

const addDialogVisible = ref(false)
const addFormRef = ref(null)
const removeDialogVisible = ref(false)
const removeFormRef = ref(null)
const detailDialogVisible = ref(false)

const searchForm = reactive({
  candidateName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const addForm = reactive({
  candidateId: null,
  reason: '',
  operatorId: 1,
  operatorName: '管理员'
})

const addRules = {
  candidateId: [
    { required: true, message: '请选择候选人', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入列入原因', trigger: 'blur' }
  ]
}

const removeForm = reactive({
  removeReason: '',
  operatorId: 1
})

const removeRules = {
  removeReason: [
    { required: true, message: '请输入移除原因', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await riskListApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
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

const loadCandidateList = async () => {
  try {
    const res = await candidateApi.page({
      current: 1,
      size: 200,
      isInRisk: 0
    })
    if (res.code === 200) {
      candidateList.value = res.data.records
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
    status: ''
  })
  handleSearch()
}

const handleAdd = () => {
  addForm.candidateId = null
  addForm.reason = ''
  loadCandidateList()
  addDialogVisible.value = true
  if (addFormRef.value) addFormRef.value.clearValidate()
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await riskListApi.addToRiskList(
          addForm.candidateId,
          addForm.reason,
          addForm.operatorId,
          addForm.operatorName
        )
        if (res.code === 200) {
          ElMessage.success('加入成功')
          addDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleRemove = (row) => {
  currentRiskId.value = row.id
  removeForm.removeReason = ''
  removeDialogVisible.value = true
  if (removeFormRef.value) removeFormRef.value.clearValidate()
}

const submitRemove = async () => {
  if (!removeFormRef.value) return
  await removeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await riskListApi.removeFromRiskList(
          currentRiskId.value,
          removeForm.removeReason,
          removeForm.operatorId
        )
        if (res.code === 200) {
          ElMessage.success('移除成功')
          removeDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleView = async (row) => {
  try {
    const res = await riskListApi.getById(row.id)
    if (res.code === 200) {
      currentRecord.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const getStatusType = (status) => {
  const types = {
    active: 'danger',
    removed: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    active: '生效中',
    removed: '已移除'
  }
  return texts[status] || status
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
})
</script>

<style scoped>
.risk-list-page {
  max-width: 1300px;
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
