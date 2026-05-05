import { ref } from 'vue'
import api from '@/services/api'

export interface ValidationResult {
  valid: boolean
  errors: string[]
}

export function useAttributeValidation() {
  const validating = ref(false)

  /**
   * Validate mã không được viết thường toàn bộ
   * Mã phải có ít nhất một chữ hoa
   */
  function validateCode(code: string, fieldName: string = 'Mã'): ValidationResult {
    const errors: string[] = []

    if (!code || code.trim() === '') {
      errors.push(`${fieldName} không được để trống`)
      return { valid: false, errors }
    }

    const trimmedCode = code.trim()

    // Kiểm tra mã phải có ít nhất một chữ hoa (không được viết thường toàn bộ)
    if (!/[A-Z]/.test(trimmedCode)) {
      errors.push(`${fieldName} phải chứa ít nhất một chữ cái in hoa`)
    }

    // Kiểm tra không chứa ký tự đặc biệt (chỉ cho phép chữ, số, gạch ngang, gạch dưới)
    if (!/^[A-Za-z0-9_-]+$/.test(trimmedCode)) {
      errors.push(`${fieldName} chỉ được chứa chữ cái, số, gạch ngang (-) và gạch dưới (_)`)
    }

    // Kiểm tra độ dài hợp lý
    if (trimmedCode.length < 2) {
      errors.push(`${fieldName} phải có ít nhất 2 ký tự`)
    }

    if (trimmedCode.length > 50) {
      errors.push(`${fieldName} không được quá 50 ký tự`)
    }

    return { valid: errors.length === 0, errors }
  }

  /**
   * Validate tên
   */
  function validateName(name: string, fieldName: string = 'Tên'): ValidationResult {
    console.log('🔍 validateName called:', { name, fieldName })
    const errors: string[] = []

    if (!name || name.trim() === '') {
      console.log('❌ Name is empty')
      errors.push(`${fieldName} không được để trống`)
      return { valid: false, errors }
    }

    const trimmedName = name.trim()
    console.log('🔍 Trimmed name:', trimmedName)

    // Kiểm tra không có nhiều dấu cách liên tiếp
    if (/\s{2,}/.test(trimmedName)) {
      console.log('❌ Multiple spaces detected')
      errors.push(`${fieldName} không được có nhiều dấu cách liên tiếp`)
    }

    // Kiểm tra độ dài hợp lý
    if (trimmedName.length < 2) {
      console.log('❌ Name too short:', trimmedName.length)
      errors.push(`${fieldName} phải có ít nhất 2 ký tự`)
    }

    if (trimmedName.length > 255) {
      console.log('❌ Name too long:', trimmedName.length)
      errors.push(`${fieldName} không được quá 255 ký tự`)
    }

    console.log('✅ validateName result:', { valid: errors.length === 0, errors })
    return { valid: errors.length === 0, errors }
  }

  /**
   * Kiểm tra trùng lặp mã
   */
  async function checkDuplicateCode(
    endpoint: string,
    codeField: string,
    codeValue: string,
    currentId?: number,
  ): Promise<ValidationResult> {
    const errors: string[] = []

    if (!codeValue || codeValue.trim() === '') {
      return { valid: true, errors }
    }

    try {
      validating.value = true
      const response = await api.get(endpoint)
      const items = response.data || []

      const duplicate = items.find(
        (item: any) =>
          item[codeField] &&
          item[codeField].toLowerCase() === codeValue.toLowerCase() &&
          (!currentId || item.id !== currentId),
      )

      if (duplicate) {
        errors.push(`Mã "${codeValue}" đã tồn tại trong hệ thống`)
      }
    } catch (error) {
      console.error('Error checking duplicate code:', error)
      // Không thêm lỗi vào mảng errors để không chặn việc tạo/cập nhật nếu API lỗi
    } finally {
      validating.value = false
    }

    return { valid: errors.length === 0, errors }
  }

  /**
   * Kiểm tra trùng lặp tên
   */
  async function checkDuplicateName(
    endpoint: string,
    nameField: string,
    nameValue: string,
    currentId?: number,
  ): Promise<ValidationResult> {
    const errors: string[] = []

    if (!nameValue || nameValue.trim() === '') {
      return { valid: true, errors }
    }

    try {
      validating.value = true
      const response = await api.get(endpoint)
      const items = response.data || []

      const duplicate = items.find(
        (item: any) =>
          item[nameField] &&
          item[nameField].toLowerCase().trim() === nameValue.toLowerCase().trim() &&
          (!currentId || item.id !== currentId),
      )

      if (duplicate) {
        errors.push(`Tên "${nameValue}" đã tồn tại trong hệ thống`)
      }
    } catch (error) {
      console.error('Error checking duplicate name:', error)
      // Không thêm lỗi vào mảng errors để không chặn việc tạo/cập nhật nếu API lỗi
    } finally {
      validating.value = false
    }

    return { valid: errors.length === 0, errors }
  }

  /**
   * Validate toàn bộ form cho thuộc tính
   */
  async function validateAttributeForm(
    formData: any,
    fields: { code?: string; name: string },
    options: { endpoint: string; nameLabel: string; editingId?: number },
    toast?: any,
  ): Promise<{ isValid: boolean }> {
    const allErrors: string[] = []

    // Lấy giá trị từ formData
    const codeValue = fields.code ? formData[fields.code] : undefined
    const nameValue = formData[fields.name]

    console.log('🔍 Validating form:', { codeValue, nameValue, fields, options })

    // Validate mã nếu có
    if (fields.code && codeValue) {
      const codeResult = validateCode(codeValue, 'Mã')
      console.log('📝 Code validation result:', codeResult)
      allErrors.push(...codeResult.errors)

      // Kiểm tra trùng lặp mã
      if (codeResult.valid) {
        const duplicateCodeResult = await checkDuplicateCode(
          options.endpoint,
          fields.code,
          codeValue,
          options.editingId,
        )
        console.log('🔄 Duplicate code check:', duplicateCodeResult)
        allErrors.push(...duplicateCodeResult.errors)
      }
    }

    // Validate tên
    const nameResult = validateName(nameValue, 'Tên')
    console.log('📝 Name validation result:', nameResult)
    allErrors.push(...nameResult.errors)

    // Kiểm tra trùng lặp tên
    if (nameResult.valid) {
      const duplicateNameResult = await checkDuplicateName(
        options.endpoint,
        fields.name,
        nameValue,
        options.editingId,
      )
      console.log('🔄 Duplicate name check:', duplicateNameResult)
      allErrors.push(...duplicateNameResult.errors)
    }

    console.log('❌ All errors:', allErrors)

    // Hiển thị lỗi nếu có
    if (allErrors.length > 0 && toast) {
      toast.error('Lỗi validation', allErrors[0])
    }

    return { isValid: allErrors.length === 0 }
  }

  /**
   * Chuẩn hóa tên: Viết hoa chữ cái đầu mỗi từ
   */
  function normalizeName(name: string): string {
    return name
      .trim()
      .replace(/\s+/g, ' ') // Loại bỏ dấu cách thừa
      .split(' ')
      .map((word) => {
        if (word.length === 0) return ''
        // Viết hoa chữ cái đầu, giữ nguyên phần còn lại
        return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
      })
      .join(' ')
  }

  /**
   * Chuẩn hóa mã: Chuyển thành uppercase và loại bỏ ký tự không hợp lệ
   */
  function normalizeCode(code: string): string {
    return code
      .trim()
      .toUpperCase()
      .replace(/[^A-Z0-9_-]/g, '') // Chỉ giữ chữ cái, số, gạch ngang, gạch dưới
  }

  return {
    validating,
    validateCode,
    validateName,
    checkDuplicateCode,
    checkDuplicateName,
    validateAttributeForm,
    normalizeName,
    normalizeCode,
  }
}
