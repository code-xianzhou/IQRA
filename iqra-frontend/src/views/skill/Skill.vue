<template>
  <div class="skill-page">
    <div class="toolbar">
      <h2>技能管理</h2>
      <div>
        <el-button type="success" @click="batchUpdate(1)">批量启用</el-button>
        <el-button type="danger" @click="batchUpdate(0)">批量禁用</el-button>
      </div>
    </div>

    <div class="skill-list" v-loading="loading">
      <el-card v-for="skill in skills" :key="skill.id" class="skill-card">
        <template #header>
          <div class="card-header">
            <span class="skill-name">{{ skill.skillName }}</span>
            <el-switch
              v-model="skill.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggle(skill)"
            />
          </div>
        </template>

        <div class="skill-desc">{{ skill.description }}</div>

        <div class="skill-info">
          <div>
            <span class="label">技能类型:</span>
            <el-tag>{{ skill.skillType }}</el-tag>
          </div>
          <div>
            <span class="label">召回策略:</span>
            <el-tag type="info">{{ skill.recallStrategy }}</el-tag>
          </div>
          <div>
            <span class="label">返回数量:</span>
            <span>{{ skill.topK }}</span>
          </div>
          <div>
            <span class="label">重排阈值:</span>
            <span>{{ skill.rerankThreshold }}</span>
          </div>
        </div>

        <el-button type="primary" size="small" @click="showConfigDialog(skill)">配置</el-button>
      </el-card>
    </div>

    <!-- Config Dialog -->
    <el-dialog v-model="showConfig" title="技能配置" width="600px">
      <el-form :model="currentSkill" label-width="100px">
        <el-form-item label="Prompt模板">
          <el-input
            v-model="currentSkill.promptTemplate"
            type="textarea"
            :rows="6"
          />
        </el-form-item>
        <el-form-item label="召回策略">
          <el-select v-model="currentSkill.recallStrategy">
            <el-option label="多路召回" value="multi" />
            <el-option label="关键词召回" value="keyword" />
            <el-option label="向量召回" value="vector" />
          </el-select>
        </el-form-item>
        <el-form-item label="返回数量">
          <el-input-number v-model="currentSkill.topK" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="重排阈值">
          <el-input-number v-model="currentSkill.rerankThreshold" :min="0" :max="1" :step="0.1" :precision="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showConfig = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSkillList, updateSkill, batchUpdateSkill } from '@/api/skill'

const loading = ref(false)
const skills = ref([])
const showConfig = ref(false)
const currentSkill = reactive({})

const loadSkills = async () => {
  loading.value = true
  try {
    skills.value = await getSkillList()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleToggle = async (skill) => {
  try {
    await updateSkill(skill)
    ElMessage.success('状态已更新')
  } catch (e) {
    skill.enabled = skill.enabled === 1 ? 0 : 1
  }
}

const showConfigDialog = (skill) => {
  Object.assign(currentSkill, skill)
  showConfig.value = true
}

const handleSave = async () => {
  try {
    await updateSkill(currentSkill)
    ElMessage.success('保存成功')
    showConfig.value = false
    loadSkills()
  } catch (e) {
  }
}

const batchUpdate = async (enabled) => {
  try {
    const updates = skills.value.map(s => ({ ...s, enabled }))
    await batchUpdateSkill(updates)
    ElMessage.success(`已${enabled ? '启用' : '禁用'}所有技能`)
    loadSkills()
  } catch (e) {
  }
}

onMounted(() => {
  loadSkills()
})
</script>

<style scoped>
.skill-page {
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

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.skill-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.skill-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skill-name {
  font-size: 18px;
  font-weight: bold;
}

.skill-desc {
  color: #606266;
  margin-bottom: 16px;
  line-height: 1.5;
}

.skill-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.skill-info .label {
  color: #909399;
  font-size: 13px;
  margin-right: 8px;
}
</style>
