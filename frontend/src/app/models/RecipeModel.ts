import { FoodModel } from "./FoodModel";
import {BaseModel} from './BaseModel';

export interface RecipeModel extends BaseModel{
       foods: FoodModel[]
}
