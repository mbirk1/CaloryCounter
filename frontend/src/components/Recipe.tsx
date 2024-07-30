import { useEffect, useState } from "react";
import { getAllRecipes } from "../api/recipe_api";
import { RecipeModel } from "../static/models/RecipeModel";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen, faTrash } from "@fortawesome/free-solid-svg-icons";

export default function Recipe(){
    const [recipe, setRecipe] = useState([]);

    useEffect(() =>{
        getAllRecipes().then(res => setRecipe(res));
    }, []);  
                    
    return (
        <div className='relative overflow-x-auto shadow-md mt-16 mx-8'>
            
            <table className='w-full text-xl text-left rtl:text-right text-gray-500 dark:text-gray-400'>
                <thead className='text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400'>
                    <tr>
                        <th scope='col' className='px-6 py-3'>
                            Name
                        </th>
                        <th scope='col' className='px-6 py-3'>
                            Calory
                        </th>
                        <th>
                            Weigth
                        </th>
                        <th>
                            Actions
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {recipe.map((item: RecipeModel) => (
                        <tr key={item.uuid} className='bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600'>
                            <th scope='row' className='px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white'>
                                {item.name}
                            </th>
                            <td className='px-6 py-4'>
                                {
                                    item.foods.reduce((accumulator, currentObject) => 
                                    {
                                        return accumulator + currentObject.calory;
                                    }, 0)
                                }
                            </td>
                            <td className='px-6 py-4'>
                                {
                                    item.foods.reduce((accumulator, currentObject) => 
                                    {
                                        return accumulator + currentObject.grams;
                                    }, 0)
                                }    
                            </td>
                            <td>
                                <FontAwesomeIcon icon={faPen} className='mr-8'/>
                                <FontAwesomeIcon icon={faTrash} />
                            </td>
                        </tr>
                    ))} 
                </tbody>
            </table>
        </div>
    );
}