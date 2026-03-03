create table deactivated_tokens
(
    id uuid primary key,
    keep_until timestamp not null check ( keep_until > now())
);