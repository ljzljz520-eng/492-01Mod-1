<template>
  <div class="team-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">班组管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增班组
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="班组名称">
          <el-input 
            v-model="searchForm.teamName" 
            placeholder="请输入班组名称" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="班次">
          <el-select 
            v-model="searchForm.workShift" 
            placeholder="请选择班次" 
            clearable 
            style="width: 160px"
            class="rounded-lg"
          >
            <el-option label="早班" value="morning" />
            <el-option label="中班" value="afternoon" />
            <el-option label="晚班" value="night" />
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
        <el-table-column prop="teamName" label="班组名称" width="150" />
        <el-table-column prop="teamLeader" label="班组长" width="100" />
        <el-table-column prop="teamPhone" label="联系电话" width="130" />
        <el-table-column prop="workShift" label="班次" width="100">
          <template #default="{ row }">
            <el-tag :type="getShiftType(row.workShift)" size="small">
              {{ getShiftText(row.workShift) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="招聘名额" width="150">
          <template #default="{ row }">
            <span class="text-gray-700">{{ row.usedQuota || 0 }} / {{ row.quota || 0 }}</span>
            <el-progress 
              :percentage="row.quota ? Math.round((row.usedQuota || 0) / row.quota * 100) : 0" 
              :stroke-width="6"
              :show-text="false"
              class="mt-1"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="班组描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="px-2">
        <el-form-item label="班组名称" prop="teamName">
          <el-input v-model="formData.teamName" placeholder="请输入班组名称" />
        </el-form-item>
        <el-form-item label="班组长" prop="teamLeader">
          <el-input v-model="formData.teamLeader" placeholder="请输入班组长姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="teamPhone">
          <el-input v-model="formData.teamPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="班次" prop="workShift">
          <el-select v-model="formData.workShift" placeholder="请选择班次" class="w-full">
            <el-option label="早班" value="morning" />
            <el-option label="中班" value="afternoon" />
            <el-option label="晚班" value="night" />
          </el-select>
        </el-form-item>
        <el-form-item label="招聘名额" prop="quota">
          <el-input-number v-model="formData.quota" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="班组描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入班组描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { teamApi } from '@/api/team'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增班组')
const formRef = ref(null)

const searchForm = reactive({
  teamName: '',
  workShift: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  teamName: '',
  teamLeader: '',
  teamPhone: '',
  workShift: 'morning',
  quota: 5,
  description: ''
})

const formRules = {
  teamName: [
    { required: true, message: '请输入班组名称', trigger: 'blur' }
  ],
  workShift: [
    { required: true, message: '请选择班次', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await teamApi.page({
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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    teamName: '',
    workShift: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增班组'
  Object.assign(formData, {
    id: null,
    teamName: '',
    teamLeader: '',
    teamPhone: '',
    workShift: 'morning',
    quota: 5,
    description: ''
  })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑班组'
  try {
    const res = await teamApi.getById(row.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        teamName: res.data.teamName,
        teamLeader: res.data.teamLeader || '',
        teamPhone: res.data.teamPhone || '',
        workShift: res.data.workShift || 'morning',
        quota: res.data.quota || 0,
        description: res.data.description || ''
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
          res = await teamApi.update(formData.id, formData)
        } else {
          res = await teamApi.save(formData)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该班组吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await teamApi.delete(row.id)
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
.team-page {
  max-width: 1200px;
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
