ALTER TABLE tab_calory_food ADD COLUMN diet varchar(20) NOT NULL DEFAULT 'UNKNOWN';
CREATE INDEX idx_tab_calory_food_diet ON tab_calory_food (diet);
