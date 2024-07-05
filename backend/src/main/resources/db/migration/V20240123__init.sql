CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";
create table tab_calory_food (
    id uuid primary key,
    name varchar(255),
    calory_count numeric(6,2),
    grams numeric(6,2)
);

create table tab_calory_recipe (
    id uuid primary key,
    name varchar(255)
);

create table tab_calory_recipe_food (
    food_id uuid references tab_calory_food(id),
    recipe_id uuid references tab_calory_recipe(id),
    primary key (food_id, recipe_id)
)