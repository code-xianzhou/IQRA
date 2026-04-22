<template>
  <div class="chat-page">
    <div class="chat-layout">
      <!-- Left: History -->
      <div class="history-panel">
        <div class="history-header">
          <h3>历史对话</h3>
          <el-button size="small" @click="newSession">新对话</el-button>
        </div>
        <div class="history-list">
          <div
            v-for="item in sessionGroups"
            :key="item.sessionId"
            :class="['history-item', { active: currentSession === item.sessionId }]"
            @click="switchSession(item.sessionId)"
          >
            <div class="history-info">
              <div class="history-title">{{ item.firstQuestion?.substring(0, 20) || '新对话' }}</div>
              <div class="history-meta">
                <span>{{ item.messageCount }} 条消息</span>
                <span>{{ formatTime(item.lastTime) }}</span>
              </div>
            </div>
            <el-icon class="delete-btn" @click.stop="deleteSession(item.sessionId)"><Close /></el-icon>
          </div>
          <el-empty v-if="sessionGroups.length === 0" description="暂无历史对话" />
        </div>
      </div>

      <!-- Center: Chat Window -->
      <div class="chat-panel">
        <div class="chat-header">
          <span>当前对话: {{ sessionTitle }}</span>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
            <div class="message-avatar">
              <el-icon v-if="msg.role === 'user'"><User /></el-icon>
              <el-icon v-else><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div v-if="msg.references && msg.references.length > 0" class="message-references">
                <el-tag size="small" v-for="(ref, i) in msg.references" :key="i">
                  引用 {{ i + 1 }}
                </el-tag>
              </div>
            </div>
          </div>

          <div v-if="loading" class="message assistant">
            <div class="message-avatar"><el-icon><Loading /></el-icon></div>
            <div class="message-content">
              <div class="message-text">正在思考中...</div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-select v-model="selectedModel" style="width: 160px" @change="handleModelSwitch">
            <el-option
              v-for="model in modelList"
              :key="model.modelId"
              :label="model.modelName"
              :value="model.modelId"
            />
          </el-select>

          <el-input
            v-model="question"
            placeholder="请输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="loading"
          />
          <el-button type="primary" @click="sendMessage" :loading="loading">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>

      <!-- Right: References Panel -->
      <div class="reference-panel" v-if="showReferences">
        <div class="reference-header">
          <h3>引用溯源</h3>
          <el-button size="small" @click="showReferences = false">关闭</el-button>
        </div>
        <div class="reference-list">
          <div v-for="(ref, idx) in currentReferences" :key="idx" class="reference-item">
            <div class="reference-title">引用 {{ idx + 1 }} (相关度: {{ (ref.score * 100).toFixed(1) }}%)</div>
            <div class="reference-content">{{ ref.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ask, getHistory, deleteHistory } from '@/api/chat'
import { getModelConfig } from '@/api/model'

const messages = ref([])
const loading = ref(false)
const question = ref('')
const selectedModel = ref('qwen2-7b')
const modelList = ref([])
const currentSession = ref(null)
const sessionTitle = ref('新对话')
const showReferences = ref(false)
const currentReferences = ref([])
const messagesRef = ref(null)

// Each session: { sessionId, firstQuestion, messageCount, lastTime }
const sessionGroups = ref([])

const userId = localStorage.getItem('userId') || 'admin'

const loadModels = async () => {
  try {
    modelList.value = await getModelConfig()
    const defaultModel = modelList.value.find(m => m.isDefault === 1)
    if (defaultModel) {
      selectedModel.value = defaultModel.modelId
    }
  } catch (e) {
  }
}

const loadSessionList = async () => {
  try {
    // Load all history (no sessionId filter)
    const allHistory = await getHistory({ userId })

    // Group by sessionId
    const groups = {}
    for (const item of allHistory) {
      const sid = item.sessionId
      if (!sid) continue

      if (!groups[sid]) {
        groups[sid] = {
          sessionId: sid,
          firstQuestion: item.question,
          messageCount: 0,
          lastTime: item.createTime,
          // Store items sorted by createTime for later retrieval
          _items: []
        }
      }
      groups[sid].messageCount++
      groups[sid].lastTime = item.createTime // already ordered DESC, so first item = latest
      groups[sid]._items.push(item)
    }

    // For each group, firstQuestion should be the OLDEST question
    for (const sid in groups) {
      const items = groups[sid]._items
      // Sort ascending to get the first question
      items.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      groups[sid].firstQuestion = items[0].question
    }

    // Sort sessions by lastTime descending (most recent first)
    sessionGroups.value = Object.values(groups)
      .sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime))

  } catch (e) {
    console.error('loadSessionList failed', e)
  }
}

const sendMessage = async () => {
  if (!question.value.trim()) return

  const userMsg = { role: 'user', content: question.value }
  messages.value.push(userMsg)
  const currentQuestion = question.value
  question.value = ''
  loading.value = true

  // Create session BEFORE sending if this is a new conversation
  const isNewSession = !currentSession.value
  if (isNewSession) {
    currentSession.value = 'sess_' + Date.now()
    sessionTitle.value = currentQuestion.substring(0, 20)
  }

  try {
    const res = await ask({
      userId,
      question: currentQuestion,
      modelId: selectedModel.value,
      sessionId: currentSession.value
    })

    messages.value.push({
      role: 'assistant',
      content: res.answer,
      references: res.references,
      modelUsed: res.modelUsed,
      time: res.time
    })

    if (res.references && res.references.length > 0) {
      currentReferences.value = res.references
      showReferences.value = true
    }

    // Refresh session list (new message added to DB)
    await loadSessionList()
  } catch (e) {
    ElMessage.error('回答失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const switchSession = async (sessionId) => {
  currentSession.value = sessionId

  // Load all messages for this session from backend
  const sessionHistory = await getHistory({ userId, sessionId })
  if (sessionHistory.length === 0) {
    messages.value = []
    sessionTitle.value = '空对话'
    return
  }

  // Sort by createTime ascending (oldest first)
  const sorted = sessionHistory.sort((a, b) =>
    new Date(a.createTime) - new Date(b.createTime)
  )

  messages.value = []
  for (const item of sorted) {
    messages.value.push({ role: 'user', content: item.question })
    messages.value.push({
      role: 'assistant',
      content: item.answer,
      references: item.references ? JSON.parse(item.references || '[]') : [],
      modelUsed: item.modelUsed
    })
  }

  // Session title = first question, never changes
  sessionTitle.value = sorted[0].question?.substring(0, 20) || '历史对话'

  await nextTick()
  scrollToBottom()
}

const newSession = () => {
  currentSession.value = null
  sessionTitle.value = '新对话'
  messages.value = []
  currentReferences.value = []
  showReferences.value = false
}

const deleteSession = async (sessionId) => {
  try {
    await deleteHistory({ userId, sessionId })
    ElMessage.success('已删除该对话')
    // If currently viewing this session, clear the chat
    if (currentSession.value === sessionId) {
      newSession()
    }
    await loadSessionList()
  } catch (e) {
    ElMessage.error('删除失败: ' + (e.message || '未知错误'))
  }
}

const handleModelSwitch = (modelId) => {
  ElMessage.success(`已切换到 ${modelId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadModels()
  loadSessionList()
})
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 140px);
}

.chat-layout {
  display: flex;
  gap: 16px;
  height: 100%;
}

.history-panel {
  width: 250px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.history-header h3 {
  font-size: 16px;
  margin: 0;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.history-item.active {
  background: #ecf5ff;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
  margin-left: 8px;
}

.delete-btn:hover {
  color: #f56c6c;
}

.chat-panel {
  flex: 1;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message.user .message-avatar {
  background: #409eff;
  color: white;
}

.message-content {
  max-width: 60%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  line-height: 1.6;
}

.message.user .message-text {
  background: #409eff;
  color: white;
}

.message-references {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  align-items: center;
}

.reference-panel {
  width: 300px;
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.reference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.reference-header h3 {
  font-size: 16px;
  margin: 0;
}

.reference-list {
  flex: 1;
  overflow-y: auto;
}

.reference-item {
  padding: 12px;
  border-left: 3px solid #409eff;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.reference-title {
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #409eff;
}

.reference-content {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
  max-height: 100px;
  overflow: hidden;
}
</style>
