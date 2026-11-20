-- onliner.by news articles. Rows are discovered from section sitemaps (fetch_status = 'pending')
-- and then enriched from each article page by com.hutsko.tools.articles.ArticlesCrawler.
create table if not exists articles (
    url              text primary key,
    section          text        not null,           -- people | tech | auto | realt | money
    slug             text        not null,
    published_date   date        not null,           -- from the URL
    sitemap_lastmod  timestamptz,

    post_id          bigint,                         -- onliner internal id (comments API key)
    title            text,
    published_at     timestamptz,                    -- exact publication time from page metadata
    authors          text[],
    categories       text[],
    tags             text[],
    article_type     text,                           -- e.g. news, article
    word_count       integer,
    views            integer,                        -- snapshot at fetched_at
    image_url        text,
    is_special       boolean,                        -- special project / longread
    page_meta        jsonb,                          -- raw page metadata for fields not mapped above

    fetch_status     text        not null default 'pending', -- pending | ok | not_found | error
    http_status      integer,
    fetch_error      text,
    fetch_attempts   integer     not null default 0,
    discovered_at    timestamptz not null default now(),
    fetched_at       timestamptz
);

create index if not exists articles_published_date_idx on articles (published_date);
create index if not exists articles_fetch_status_idx on articles (fetch_status);
create index if not exists articles_post_id_idx on articles (post_id);
