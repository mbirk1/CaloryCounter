import { FoodModel } from "./FoodModel";

export interface RecipeModel {
       uuid: string,
       name: string,
       foods: FoodModel[]
}