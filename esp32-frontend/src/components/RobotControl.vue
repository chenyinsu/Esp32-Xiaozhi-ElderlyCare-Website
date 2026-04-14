<template>
  <div>
    <h1>Robot Control</h1>
    <button @click="getStatus">Get Status</button>
    <button @click="sendCommand('forward')">Move Forward</button>
    <p v-if="status">{{ status }}
    <p v-if="response">{{ response }}
  </div>
</template>

<script>
import { ref } fr setupom 'vue'
import axios from 'axios'

const status = ref('')
const response = ref('')

const getStatus = async () => {
  const res = await axios.get('http://localhost:8080/api/robot/status')
  status.value = res.data
}

const sendCommand = async (cmd) => {
  const res = await axios.post('http://localhost:8080/api/robot/control', null, {
    params: { command: cmd }
  })
  response.value = res.data
}
</script>
