create table if not exists user_profiles_enriched (
    user_id bigint primary key,
    username text not null,
    profile_url text,
    avatar_url text,
    rank_title text,
    status text,
    messages integer,
    years_on_site text,
    source_prefix text,
    created_at_raw text,
    created_at_iso text,
    profile_details_raw text,
    rating_place integer,
    rating_karma integer,
    rating_comments_total integer
);

create table if not exists analytics_events (
    id bigserial primary key,
    event_type text not null,
    event_payload jsonb,
    created_at timestamptz not null default now()
);

insert into user_profiles_enriched (
    user_id,
    username,
    profile_url,
    avatar_url,
    rank_title,
    status,
    messages,
    years_on_site,
    source_prefix,
    created_at_raw,
    created_at_iso,
    profile_details_raw,
    rating_place,
    rating_karma,
    rating_comments_total
) values
    (1001, 'sample_user', 'https://profile.onliner.by/user/1001', '', 'expert', 'online', 128, '5 years', 'forum', '1 января 2020', '2020-01-01', 'Sample profile details', 12, 481, 900),
    (1002, 'quiet_observer', 'https://profile.onliner.by/user/1002', '', 'regular', 'offline', 34, '2 years', 'forum', '12 марта 2021', '2021-03-12', 'Sample profile details', 44, 120, 220)
on conflict (user_id) do nothing;

insert into analytics_events (event_type, event_payload)
values
    ('docker_bootstrap', '{"source": "init.sql"}'::jsonb),
    ('profile_loaded', '{"profile": "sample_user"}'::jsonb)
on conflict do nothing;
