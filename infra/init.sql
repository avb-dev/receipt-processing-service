create table if not exists public.receipts
(
    payment_id integer     not null,
    uuid       varchar(50) not null,
    constraint receipts_pk
        unique (payment_id, uuid)
);

alter table public.receipts add column if not exists is_refunded boolean default false;
alter table public.receipts add column if not exists "timestamp" timestamp with time zone default '2026-02-08 09:21:15+03';

alter table public.receipts
    owner to postgres;
