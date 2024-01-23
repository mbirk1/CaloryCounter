CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";
create table tab_calory_food (
    id uuid,
    name varchar(255),
    calory_count numeric(6,2)
);

create table tab_calory_recipe (
    id uuid,
    name varchar(255),
);