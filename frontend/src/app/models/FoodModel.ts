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
  diet?: string
  vitaminA?: number
  vitaminD?: number
  vitaminE?: number
  vitaminK?: number
  vitaminB1?: number
  vitaminB2?: number
  vitaminB6?: number
  vitaminB12?: number
  vitaminC?: number
  folate?: number
  potassium?: number
  calcium?: number
  magnesium?: number
  iron?: number
  zinc?: number
  iodine?: number
}
