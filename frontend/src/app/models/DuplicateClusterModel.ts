import { FoodModel } from './FoodModel'

export interface DuplicateClusterMemberModel {
  food: FoodModel
  similarityScore: number
  suggestedCanonical: boolean
}

export interface DuplicateClusterModel {
  id: string
  source: string
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED'
  members: DuplicateClusterMemberModel[]
  createdAt: string
  resolvedAt?: string
}
