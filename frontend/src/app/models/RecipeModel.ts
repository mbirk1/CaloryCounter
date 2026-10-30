import { FoodModel } from './FoodModel'
import { BaseModel } from './BaseModel'

export interface RecipeFoodEntryModel {
  food: FoodModel
  amountGrams: number
}

export interface RecipeModel extends BaseModel {
  servings: number
  foods: RecipeFoodEntryModel[]
  totalCalories: number
  totalFat: number
  totalSaturatedFat: number
  totalCarbohydrates: number
  totalSugar: number
  totalFiber: number
  totalProtein: number
  totalSalt: number
  totalSodium: number
  caloriesPerServing: number
  fatPerServing: number
  saturatedFatPerServing: number
  carbohydratesPerServing: number
  sugarPerServing: number
  fiberPerServing: number
  proteinPerServing: number
  saltPerServing: number
  sodiumPerServing: number
  diet: string
}

export interface RecipeFoodEntryPayload {
  foodId: string
  amountGrams: number
}

export interface CreateRecipePayload {
  name: string
  servings: number
  foods: RecipeFoodEntryPayload[]
}
