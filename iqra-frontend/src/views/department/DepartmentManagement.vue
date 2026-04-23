<template>
  <div class="department-management-page">
    <!-- Toolbar -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAddDepartment">
        <el-icon><Plus /></el-icon>
        添加部门
      </el-button>
    </div>

    <!-- Department Tree -->
    <div class="department-tree-container">
      <el-tree
          ref="treeRef"
          :data="departmentTree"
          node-key="id"
          :props="defaultProps"
          @node-click="handleNodeClick"
          @check-change="handleCheckChange"
          show-checkbox
          check-strictly
          :default-expand-all="true"
          class="department-tree"
      >
        <template #default="{ node, data }">
          <span class="department-node">
            <span>{{ data.name }}</span>
            <span class="department-actions">
              <el-button
                  type="primary"
                  size="small"
                  @click.stop="handleEditDepartment(data)"
              >
                编辑
              </el-button>
              <el-button
                  type="danger"
                  size="small"
                  @click.stop="handleDeleteDepartment(data)"
                  :disabled="data.parentId === 0"
              >
                删除
              </el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </div>

    <!-- Add/Edit Department Dialog -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑部门' : '添加部门'" width="400px">
      <el-form :model="departmentForm" :rules="departmentRules" ref="departmentFormRef" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="父部门">
          <el-input :value="getParentDepartmentName(departmentForm.parentId)" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDepartment" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const selectedDepartment = ref(null)
const departmentFormRef = ref(null)
const departments = ref([])
const treeRef = ref(null)

const departmentForm = reactive({
  id: null,
  name: '',
  parentId: 0
})

const departmentRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const defaultProps = {
  children: 'children',
  label: 'name'
}

const departmentTree = computed(() => {
  return buildTree(departments.value, 0)
})

const buildTree = (items, parentId) => {
  return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        ...item,
        children: buildTree(items, item.id)
      }))
}

const loadDepartments = async () => {
  loading.value = true
  try {
    departments.value = await getDepartmentTree()
  } catch (e) {
    ElMessage.error('加载部门失败')
  } finally {
    loading.value = false
  }
}

const handleNodeClick = (data) => {
  // 清除所有勾选状态
  if (treeRef.value) {
    treeRef.value.setCheckedKeys([])
    // 勾选当前点击的节点
    treeRef.value.setChecked(data.id, true)
  }
  selectedDepartment.value = data.id
}

const handleCheckChange = (data, checked, indeterminate) => {
  if (checked) {
    // 如果勾选了新节点，清除其他节点的勾选状态
    if (treeRef.value) {
      treeRef.value.setCheckedKeys([])
      treeRef.value.setChecked(data.id, true)
    }
    selectedDepartment.value = data.id
  } else {
    selectedDepartment.value = null
  }
}

const handleAddDepartment = () => {
  if (!selectedDepartment.value) {
    ElMessage.warning('请先选择一个父部门')
    return
  }
  isEdit.value = false
  departmentForm.id = null
  departmentForm.name = ''
  departmentForm.parentId = selectedDepartment.value
  showDialog.value = true
}

const handleEditDepartment = (data) => {
  isEdit.value = true
  departmentForm.id = data.id
  departmentForm.name = data.name
  departmentForm.parentId = data.parentId
  showDialog.value = true
}

const handleSaveDepartment = async () => {
  if (!departmentFormRef.value) return
  await departmentFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      if (isEdit.value) {
        await updateDepartment(departmentForm)
        ElMessage.success('编辑成功')
      } else {
        await createDepartment(departmentForm)
        ElMessage.success('创建成功')
      }
      showDialog.value = false
      loadDepartments()
    } catch (e) {
      ElMessage.error('操作失败: ' + (e.message || '未知错误'))
    } finally {
      saving.value = false
    }
  })
}

const handleDeleteDepartment = async (data) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除部门 "${data.name}" 及其所有子部门吗？`,
        '删除部门',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteDepartment(data.id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败: ' + (e.message || '未知错误'))
    }
  }
}

const getParentDepartmentName = (parentId) => {
  if (parentId === 0) {
    return '根部门'
  }
  const department = departments.value.find(dept => dept.id === parentId)
  return department ? department.name : ''
}

onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.department-management-page {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}

.department-tree-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  height: 500px;
  overflow-y: auto;
}

.department-tree {
  width: 100%;
}

.department-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.department-actions {
  display: flex;
  gap: 8px;
}
</style>