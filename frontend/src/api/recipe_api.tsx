export async function getAllRecipes() {
    const result : any = await fetch('http://localhost:8080/recipe')
    .then(response =>{
        return response.json().then(data => {
            return data;
        })
    })
    .catch(error => {
        console.log(error);
        return error;
    });

    return result;
}