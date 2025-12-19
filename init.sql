create table if not exists public.receipts
(
    payment_id integer     not null,
    uuid       varchar(50) not null,
    constraint receipts_pk
        unique (payment_id, uuid)
);

alter table public.receipts
    owner to postgres;
