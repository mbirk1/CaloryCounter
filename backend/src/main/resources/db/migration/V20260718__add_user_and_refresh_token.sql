create table tab_calory_user (
    id uuid primary key,
    email varchar(255) unique not null,
    password_hash varchar(255) not null,
    height numeric(5,2),
    weight numeric(5,2),
    activity_level varchar(50),
    created_at timestamp not null default now()
);

create table tab_calory_refresh_token (
    id uuid primary key,
    user_id uuid references tab_calory_user(id),
    token_hash varchar(255) unique not null,
    expires_at timestamp not null,
    revoked boolean not null default false,
    created_at timestamp not null default now()
);
