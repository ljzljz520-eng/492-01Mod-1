<template>
  <div class="candidate-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">候选人管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增候选人
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="姓名">
          <el-input 
            v-model="searchForm.candidateName" 
            placeholder="请输入姓名" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="面试状态">
          <el-select 
            v-model="searchForm.interviewStatus" 
            placeholder="请选择面试状态" 
            clearable 
            style="width: 160px"
            class="rounded-lg"
          >
            <el-option label="待面试" value="pending" />
            <el-option label="已通过" value="passed" />
            <el-option label="未通过" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="应聘岗位">
          <el-input 
            v-model="searchForm.position" 
            placeholder="请输入岗位" 
            clearable
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="风险状态">
          <el-select 
            v-model="searchForm.isInRisk" 
            placeholder="请选择" 
            clearable 
            style="width: 140px"
            class="rounded-lg"
          >
            <el-option label="正常" :value="0" />
            <el-option label="风险名单" :value="1" />
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
        <el-table-column prop="candidateName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="position" label="应聘岗位" width="120" />
        <el-table-column prop="interviewStatus" label="面试状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getInterviewStatusType(row.interviewStatus)" size="small">
              {{ getInterviewStatusText(row.interviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewScore" label="面试评分" width="90" />
        <el-table-column prop="absentCount" label="爽约次数" width="90" />
        <el-table-column prop="isInRisk" label="风险状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isInRisk === 1" type="danger" size="small">风险名单</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button 
              v-if="row.interviewStatus === 'pending'" 
              type="success" 
              size="small" 
              link 
              @click="handlePass(row)"
            >
              面试通过
            </el-button>
            <el-button 
              v-if="row.interviewStatus === 'pending'" 
              type="warning" 
              size="small" 
              link 
              @click="handleFail(row)"
            >
              未通过
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-form-item label="姓名" prop="candidateName">
          <el-input v-model="formData.candidateName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="formData.age" :min="18" :max="65" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="应聘岗位" prop="position">
          <el-input v-model="formData.position" placeholder="请输入应聘岗位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="passDialogVisible" title="面试通过" width="500px" :close-on-click-modal="false">
      <el-form :model="passForm" :rules="passRules" ref="passFormRef" label-width="100px">
        <el-form-item label="面试评分" prop="score">
          <el-input-number v-model="passForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="面试评语">
          <el-input v-model="passForm.remark" type="textarea" :rows="4" placeholder="请输入面试评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="passDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPass" :loading="submitting">确认通过</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="failDialogVisible" title="面试未通过" width="500px" :close-on-click-modal="false">
      <el-form :model="failForm" ref="failFormRef" label-width="100px">
        <el-form-item label="未通过原因">
          <el-input v-model="failForm.remark" type="textarea" :rows="4" placeholder="请输入未通过原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="failDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="submitFail" :loading="submitting">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { candidateApi } from '@/api/candidate'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增候选人')
const formRef = ref(null)
const passDialogVisible = ref(false)
const passFormRef = ref(null)
const failDialogVisible = ref(false)
const failFormRef = ref(null)
const currentCandidateId = ref(null)

const searchForm = reactive({
  candidateName: '',
  interviewStatus: '',
  position: '',
  isInRisk: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  candidateName: '',
  gender: '男',
  age: 25,
  phone: '',
  idCard: '',
  position: ''
})

const formRules = {
  candidateName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  position: [
    { required: true, message: '请输入应聘岗位', trigger: 'blur' }
  ]
}

const passForm = reactive({
  score: 80,
  remark: ''
})

const passRules = {
  score: [
    { required: true, message: '请输入面试评分', trigger: 'blur' }
  ]
}

const failForm = reactive({
  remark: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    if (params.isInRisk === '') {
      delete params.isInRisk
    }
    const res = await candidateApi.page(params)
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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    candidateName: '',
    interviewStatus: '',
    position: '',
    isInRisk: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增候选人'
  Object.assign(formData, {
    id: null,
    candidateName: '',
    gender: '男',
    age: 25,
    phone: '',
    idCard: '',
    position: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑候选人'
  try {
    const res = await candidateApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        candidateName: res.data.candidateName,
        gender: res.data.gender || '男',
        age: res.data.age || 25,
        phone: res.data.phone || '',
        idCard: res.data.idCard || '',
        position: res.data.position || ''
      })
      dialogVisible.value = true
      if (formRef.value) formRef.value.clearValidate()
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await candidateApi.update(formData.id, formData)
        } else {
          res = await candidateApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
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

const handlePass = (row) => {
  currentCandidateId.value = row.id
  passForm.score = 80
  passForm.remark = ''
  passDialogVisible.value = true
}

const submitPass = async () => {
  if (!passFormRef.value) return
  await passFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await candidateApi.passInterview(currentCandidateId.value, passForm.score, passForm.remark)
        if (res.code === 200) {
          ElMessage.success('已标记面试通过')
          passDialogVisible.value = false
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

const handleFail = (row) => {
  currentCandidateId.value = row.id
  failForm.remark = ''
  failDialogVisible.value = true
}

const submitFail = async () => {
  submitting.value = true
  try {
    const res = await candidateApi.failInterview(currentCandidateId.value, failForm.remark)
    if (res.code === 200) {
      ElMessage.success('已标记未通过')
      failDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该候选人吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await candidateApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const getInterviewStatusType = (status) => {
  const types = {
    pending: 'warning',
    passed: 'success',
    failed: 'danger'
  }
  return types[status] || 'info'
}

const getInterviewStatusText = (status) => {
  const texts = {
    pending: '待面试',
    passed: '已通过',
    failed: '未通过'
  }
  return texts[status] || status
}

const getStatusType = (status) => {
  const types = {
    active: 'success',
    hired: 'primary',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    active: '活跃',
    hired: '已录用',
    rejected: '已拒绝'
  }
  return texts[status] || status
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
.candidate-page {
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
