CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE tab_calory_food_duplicate_cluster (
    id uuid PRIMARY KEY,
    source varchar NOT NULL,
    status varchar NOT NULL,
    created_at timestamp NOT NULL,
    resolved_at timestamp
);

CREATE TABLE tab_calory_food_duplicate_cluster_member (
    cluster_id uuid NOT NULL REFERENCES tab_calory_food_duplicate_cluster(id) ON DELETE CASCADE,
    food_id uuid NOT NULL REFERENCES tab_calory_food(id),
    similarity_score numeric(4,3) NOT NULL,
    suggested_canonical boolean NOT NULL,
    PRIMARY KEY (cluster_id, food_id)
);

-- Accelerates the FR-2 candidate search (pg_trgm's `%` similarity operator) at OpenFoodFacts
-- scale (hundreds of thousands to millions of rows) - without it, finding name-similar pairs
-- would require a full O(n^2) pairwise comparison of every row against every other row.
CREATE INDEX idx_calory_food_name_trgm ON tab_calory_food USING gin (lower(name) gin_trgm_ops);
