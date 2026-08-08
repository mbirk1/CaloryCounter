import { ActivityLevel } from './UserModel'

export interface RegisterRequest {
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  expiresIn: number
  tokenType: string
  profileCompleted: boolean
}

export interface UpdateProfileRequest {
  height: number
  weight: number
  activityLevel: ActivityLevel
}
