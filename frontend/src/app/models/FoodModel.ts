import { BaseModel } from './BaseModel'

export interface FoodModel extends BaseModel {
  calory: number
  grams: number
  brand?: string
  category?: string
  fat?: number
  saturatedFat?: number
  carbohydrates?: number
  sugar?: number
  fiber?: number
  protein?: number
  salt?: number
  sodium?: number
  imageUrl?: string
  source?: string
  externalId?: string
}
