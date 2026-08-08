ALTER TABLE tab_calory_food ADD COLUMN import_job_id uuid;
CREATE INDEX idx_tab_calory_food_import_job_id ON tab_calory_food (import_job_id);
