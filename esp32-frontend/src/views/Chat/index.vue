<template>
  <div class="chat-page">
    <el-table :data="chats" ...>
      <el-table-column prop="sentiment" label="情感值" width="120">
        <template #default="{ row }">
          <div class="sentiment-wrapper">
            <span class="sentiment-dot" :class="getSentimentClass(row.sentiment)"></span>
            <span>{{ row.sentiment !== null ? row.sentiment.toFixed(2) : '--' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="keywordsJson" label="关键词" width="180">
        <template #default="{ row }">
          <el-tag v-for="kw in parseKeywords(row.keywordsJson)" :key="kw" size="small" class="keyword-tag">{{ kw }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.sentiment-wrapper { display: flex; align-items: center; gap: 8px; }
.sentiment-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.sentiment-dot.positive { background-color: #67c23a; box-shadow: 0 0 4px #67c23a; }
.sentiment-dot.negative { background-color: #f56c6c; box-shadow: 0 0 4px #f56c6c; }
.sentiment-dot.neutral { background-color: #909399; }

.keyword-tag { border-radius: 20px !important; margin: 2px 4px; background: #f0f2f5; border: none; color: #2c3e50; }
</style>

<script>
const getSentimentClass = (val) => {
  if (val > 0.3) return 'positive'
  if (val < -0.3) return 'negative'
  return 'neutral'
}
</script>
