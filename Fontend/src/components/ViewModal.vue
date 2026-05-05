<template>
  <div v-if="modelValue" class="modal-overlay" @click="$emit('update:modelValue', false)">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <div class="modal-title">
          <div class="title-icon">
            <font-awesome-icon icon="info-circle" />
          </div>
          <h3>{{ title }}</h3>
        </div>
        <button class="close-btn" @click="$emit('update:modelValue', false)">
          <font-awesome-icon icon="times" />
        </button>
      </div>

      <div class="modal-body">
        <div class="info-grid">
          <div v-for="field in displayFields" :key="field.key" class="info-item">
            <label>{{ field.label }}:</label>
            <span
              v-if="field.key === 'trangThai'"
              class="status-badge"
              :class="getStatusClass(data[field.key])"
            >
              {{ getStatusText(data[field.key]) }}
            </span>
            <span
              v-else-if="field.key === 'maHex' && data[field.key]"
              class="color-preview-container"
            >
              <div class="color-preview-box" :style="{ backgroundColor: data[field.key] }"></div>
              <span>{{ data[field.key] }}</span>
            </span>
            <span v-else class="info-value">
              {{ formatValue(data[field.key], field.type) || 'Chưa có' }}
            </span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('update:modelValue', false)">
          <font-awesome-icon icon="times" />
          Đóng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'

interface Field {
  key: string
  label: string
  type?: 'text' | 'date' | 'number'
}

interface Props {
  modelValue: boolean
  title: string
  data: Record<string, any>
  fields: Field[]
}

const props = defineProps<Props>()
defineEmits(['update:modelValue'])

const displayFields = computed(() => {
  return props.fields.filter(
    (f) =>
      props.data[f.key] !== null && props.data[f.key] !== undefined && props.data[f.key] !== '',
  )
})

function getStatusClass(status: number) {
  return status === 1 ? 'badge-active' : 'badge-inactive'
}

function getStatusText(status: number) {
  return status === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

function formatValue(value: any, type?: string) {
  if (value === null || value === undefined) return ''

  if (type === 'date' && value) {
    return new Date(value).toLocaleString('vi-VN')
  }

  return String(value)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 700px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s ease-out;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.modal-title h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg) scale(1.1);
}

.close-btn:active {
  transform: rotate(90deg) scale(0.95);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  background: #fafbfc;
}

.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.info-item {
  background: white;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.info-item:hover {
  border-color: #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.1);
  transform: translateY(-2px);
}

.info-item label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  display: block;
}

.info-value {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
  word-break: break-word;
  line-height: 1.5;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
  transition: all 0.2s ease;
}

.badge-active {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #166534;
  border: 1px solid #86efac;
}

.badge-inactive {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #991b1b;
  border: 1px solid #fca5a5;
}

.color-preview-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-preview-box {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  border: 2px solid #e5e7eb;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.color-preview-box:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.modal-footer {
  padding: 20px 28px;
  border-top: 1px solid #f1f5f9;
  background: white;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  color: #64748b;
}

.btn-cancel:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #475569;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-cancel:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Responsive Design */
@media (max-width: 768px) {
  .modal-container {
    max-width: 95%;
    border-radius: 16px;
  }

  .modal-header {
    padding: 20px;
  }

  .modal-title h3 {
    font-size: 18px;
  }

  .title-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  .modal-body {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .modal-footer {
    padding: 16px 20px;
  }
}
</style>
