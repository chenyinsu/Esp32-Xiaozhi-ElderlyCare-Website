/**
* 音频相关工具函数
*/

// 录制音频
export async function startRecording(): Promise<MediaRecorder> {
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
  return new MediaRecorder(stream)
}

// 停止录制并获取Blob
export function stopRecording(recorder: MediaRecorder): Promise<Blob> {
  return new Promise((resolve) => {
    recorder.ondataavailable = (event) => {
      resolve(event.data)
    }
    recorder.stop()
  })
}

// 播放音频
export function playAudio(audioBlob: Blob): void {
  const audioUrl = URL.createObjectURL(audioBlob)
  const audio = new Audio(audioUrl)
  audio.play()

  audio.onended = () => {
    URL.revokeObjectURL(audioUrl)
  }
}
