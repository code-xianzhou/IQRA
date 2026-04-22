<template>
  <div class="config-page">
    <el-tabs v-model="activeTab">
      <!-- LLM Config Tab -->
      <el-tab-pane label="LLM 配置" name="llm">
        <div v-loading="loading">
          <el-card v-for="model in modelList" :key="model.id" class="model-card">
            <template #header>
              <div class="card-header">
                <span>{{ model.modelName }}</span>
                <el-tag v-if="model.isDefault === 1" type="success">默认</el-tag>
              </div>
            </template>

            <el-form :model="model" label-width="120px">
              <el-form-item label="基础URL">
                <el-input v-model="model.baseUrl" />
              </el-form-item>
              <el-form-item label="端口">
                <el-input-number v-model="model.port" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="超时时间(ms)">
                <el-input-number v-model="model.timeout" :min="1000" :step="1000" />
              </el-form-item>
              <el-form-item label="Temperature">
                <el-input-number v-model="model.temperature" :min="0" :max="1" :step="0.1" :precision="2" />
              </el-form-item>
              <el-form-item label="Max Tokens">
                <el-input-number v-model="model.maxTokens" :min="1" :step="512" />
              </el-form-item>
              <el-form-item>
                <el-switch
                  v-model="model.enabled"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>

              <el-button type="primary" @click="handleUpdateModel(model)">保存</el-button>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- Vector DB Config Tab -->
      <el-tab-pane label="向量库配置" name="vector">
        <el-card>
          <el-form label-width="120px">
            <el-form-item label="Milvus地址">
              <el-input v-model="vectorConfig.host" />
            </el-form-item>
            <el-form-item label="端口">
              <el-input-number v-model="vectorConfig.port" />
            </el-form-item>
            <el-form-item label="集合名称">
              <el-input v-model="vectorConfig.collectionName" />
            </el-form-item>
            <el-form-item label="向量维度">
              <el-input-number v-model="vectorConfig.dimension" />
            </el-form-item>

            <el-button type="primary" @click="ElMessage.success('保存成功')">保存</el-button>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- Reranker Config Tab -->
      <el-tab-pane label="重排序配置" name="reranker">
        <el-card>
          <el-form label-width="120px">
            <el-form-item label="启用重排序">
              <el-switch v-model="rerankerConfig.enabled" />
            </el-form-item>
            <el-form-item label="重排模型">
              <el-select v-model="rerankerConfig.model" style="width: 100%">
                <el-option label="bge-reranker-base" value="bge-reranker-base" />
                <el-option label="bge-reranker-large" value="bge-reranker-large" />
                <el-option label="bge-reranker-v2-m3" value="bge-reranker-v2-m3" />
              </el-select>
            </el-form-item>
            <el-form-item label="Top K">
              <el-input-number v-model="rerankerConfig.topK" :min="1" :max="20" />
            </el-form-item>

            <el-button type="primary" @click="ElMessage.success('保存成功')">保存</el-button>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- Default Model Config Tab -->
      <el-tab-pane label="默认模型设置" name="default">
        <el-card>
          <el-form label-width="120px">
            <el-form-item label="默认模型">
              <el-select v-model="defaultModel" style="width: 100%">
                <el-option
                  v-for="model in modelList"
                  :key="model.modelId"
                  :label="model.modelName"
                  :value="model.modelId"
                />
              </el-select>
            </el-form-item>

            <el-button type="primary" @click="handleSetDefaultModel">保存</el-button>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getModelConfig, updateModel, switchModel } from '@/api/model'

const activeTab = ref('llm')
const loading = ref(false)
const modelList = ref([])
const defaultModel = ref('')

const vectorConfig = reactive({
  host: 'localhost',
  port: 19530,
  collectionName: 'internal_kb_collection',
  dimension: 768
})

const rerankerConfig = reactive({
  enabled: true,
  model: 'bge-reranker-base',
  topK: 5
})

const loadModelConfig = async () => {
  loading.value = true
  try {
    modelList.value = await getModelConfig()
    const defaultModel2 = modelList.value.find(m => m.isDefault === 1)
    if (defaultModel2) {
      defaultModel.value = defaultModel2.modelId
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleUpdateModel = async (model) => {
  try {
    await updateModel(model)
    ElMessage.success('保存成功')
  } catch (e) {
  }
}

const handleSetDefaultModel = async () => {
  try {
    await switchModel({ modelId: defaultModel.value })
    ElMessage.success('默认模型已更新')
  } catch (e) {
  }
}

onMounted(() => {
  loadModelConfig()
})
</script>

<style scoped>
.config-page {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.model-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
