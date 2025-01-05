import { useCallback, useEffect, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPen, faTrash } from '@fortawesome/free-solid-svg-icons';
import { getAllFoods } from '../../api/food_api';
import { FoodModel } from '../../static/models/FoodModel';
import DeleteFoodModal from '../modals/deleteFoodModal';

export default function Food(){
    const [food, setFood] = useState([]);
    const [deleteFoodModal, setDeleteFoodModal] = useState(false);

    useEffect(() =>{
        getAllFoods().then(res => setFood(res));
    }, []);  

    useEffect(()=> {

    })

    const editFood = useCallback((uuid: string) => {
    }, []);

    const deleteFood = useCallback((uuid: string)=> {
        setDeleteFoodModal(true);
    }, []);
                    
    return (
        <div className='relative overflow-x-auto shadow-md mt-16 mx-8'>
            
            <table className='w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400'>
                <thead className='text-xs text-gray-700 uppercase bg-gray-50 dark:bg-gray-700 dark:text-gray-400'>
                    <tr>
                        <th scope='col' className='px-6 py-3'>
                            Name
                        </th>
                        <th scope='col' className='px-6 py-3'>
                            Calory
                        </th>
                        <th scope='col' className='px-6 py-3'>
                            Grams
                        </th>
                        <th>
                            Actions
                        </th>
                    </tr>
                </thead>
                <tbody>
                    {food?.map((item: FoodModel) => (
                        <tr key={item.uuid} className='bg-white border-b dark:bg-gray-800 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600'>
                            <th scope='row' className='px-6 py-4 font-medium text-gray-900 whitespace-nowrap dark:text-white'>
                                {item.name}
                            </th>
                            <td className='px-6 py-4'>
                                {item.calory}
                            </td>
                            <td className='px-6 py-4'>
                                {item.grams}
                            </td>
                            <td>
                                <button onClick={() =>
                                    editFood(item.uuid)
                                }><FontAwesomeIcon icon={faPen} className='mr-8 cursor-pointer'/></button>
                                <button onClick={() =>
                                    deleteFood(item.uuid)
                                }><FontAwesomeIcon icon={faTrash} className='cursor-pointer'/></button>
                            </td>
                        </tr>
                    ))} 
                </tbody>
            </table>

            <DeleteFoodModal isOpen={deleteFoodModal} onClose={function (): void {
                throw new Error('Function not implemented.');
            } } children={undefined} />
        </div>
    );
}
