export type ActivityLevel =
  | 'SEDENTARY'
  | 'LIGHTLY_ACTIVE'
  | 'MODERATELY_ACTIVE'
  | 'VERY_ACTIVE'
  | 'EXTRA_ACTIVE'

export interface UserModel {
  id: string
  email: string
  height: number | null
  weight: number | null
  activityLevel: ActivityLevel | null
  profileCompleted: boolean
  createdAt?: string
}
