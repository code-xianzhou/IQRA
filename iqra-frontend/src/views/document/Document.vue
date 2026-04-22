<template>
  <div class="document-page">
    <!-- Toolbar -->
    <div class="toolbar">
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>

      <div class="toolbar-right">
        <el-input
          v-model="searchForm.fileName"
          placeholder="搜索文档名称"
          clearable
          style="width: 200px"
          @change="loadDocuments"
        />
        <el-select v-model="searchForm.fileType" placeholder="文档类型" clearable style="width: 120px" @change="loadDocuments">
          <el-option label="PDF" value="pdf" />
          <el-option label="Word" value="docx" />
          <el-option label="TXT" value="txt" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="解析状态" clearable style="width: 120px" @change="loadDocuments">
          <el-option label="待解析" value="PENDING" />
          <el-option label="解析中" value="PROCESSING" />
          <el-option label="解析成功" value="SUCCESS" />
          <el-option label="解析失败" value="FAILED" />
        </el-select>
      </div>
    </div>

    <!-- Document Table -->
    <el-table :data="documents" v-loading="loading" stripe>
      <el-table-column prop="fileName" label="文档名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="uploadBy" label="上传人" width="100" />
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column prop="fileType" label="格式" width="80" />
      <el-table-column prop="status" label="解析状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-popconfirm
            title="确认删除该文档? 删除后不可恢复"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
      @current-change="loadDocuments"
      @size-change="loadDocuments"
    />

    <!-- Upload Dialog -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".pdf,.doc,.docx,.txt"
        :limit="1"
      >
        <el-icon :size="48"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p>将文件拖拽到此处，或 <em>点击选择</em></p>
          <p class="upload-hint">仅支持 PDF/Word/TXT 格式，最大50MB</p>
        </div>
      </el-upload>

      <el-form :model="uploadForm" style="margin-top: 16px">
        <el-form-item label="部门">
          <el-input v-model="uploadForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="请输入标签，多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDocumentList, uploadDocument, deleteDocument } from '@/api/document'

const loading = ref(false)
const documents = ref([])
const showUploadDialog = ref(false)
const uploading = ref(false)
const selectedFile = ref(null)

const searchForm = reactive({
  fileName: '',
  fileType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const uploadForm = reactive({
  department: '',
  tags: ''
})

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await getDocumentList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    documents.value = res.records
    pagination.total = res.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    await uploadDocument(selectedFile.value, {
      ...uploadForm,
      uploadBy: localStorage.getItem('username') || 'admin'
    })
    ElMessage.success('上传成功，文档正在解析中')
    showUploadDialog.value = false
    selectedFile.value = null
    uploadForm.department = ''
    uploadForm.tags = ''
    loadDocuments()
  } catch (e) {
    ElMessage.error('上传文件失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteDocument(id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (e) {
  }
}

const getStatusType = (status) => {
  const map = { PENDING: 'info', PROCESSING: '', SUCCESS: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待解析', PROCESSING: '解析中', SUCCESS: '解析成功', FAILED: '解析失败' }
  return map[status] || status
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.document-page {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.upload-text {
  text-align: center;
  margin-top: 12px;
}

.upload-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>
