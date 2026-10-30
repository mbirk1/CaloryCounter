ALTER TABLE tab_calory_recipe
    ADD COLUMN servings integer NOT NULL DEFAULT 1;

ALTER TABLE tab_calory_recipe_food
    ADD COLUMN amount_grams numeric(6,2);

UPDATE tab_calory_recipe_food rf
    SET amount_grams = f.grams
    FROM tab_calory_food f
    WHERE rf.food_id = f.id;

ALTER TABLE tab_calory_recipe_food
    ALTER COLUMN amount_grams SET NOT NULL;

-- V20240123__init.sql created the recipe_id FK inline with no explicit constraint name, so
-- Postgres auto-named it. Look the name up instead of hardcoding an assumed default, so this
-- migration doesn't depend on an unconfirmed naming convention.
DO $$
DECLARE
    fk_name text;
BEGIN
    SELECT tc.constraint_name INTO fk_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
        ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'tab_calory_recipe_food'
      AND tc.constraint_type = 'FOREIGN KEY'
      AND kcu.column_name = 'recipe_id';

    EXECUTE format('ALTER TABLE tab_calory_recipe_food DROP CONSTRAINT %I', fk_name);
    EXECUTE 'ALTER TABLE tab_calory_recipe_food
        ADD CONSTRAINT tab_calory_recipe_food_recipe_id_fkey
        FOREIGN KEY (recipe_id) REFERENCES tab_calory_recipe(id) ON DELETE CASCADE';
END $$;
