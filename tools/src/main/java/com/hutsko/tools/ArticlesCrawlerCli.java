package com.hutsko.tools;

import com.hutsko.db.PostgresConnector;
import com.hutsko.tools.articles.ArticleRepository;
import com.hutsko.tools.articles.ArticlesCrawler;
import com.hutsko.tools.articles.PageFetcher;

import java.util.Arrays;

/**
 * CLI entry point for the onliner.by article crawler.
 * <pre>
 * # run without -q: it hides the progress logs
 * mvn -pl tools exec:java -Dexec.mainClass=com.hutsko.tools.ArticlesCrawlerCli \
 *     -Dexec.args="--pause-ms 1000"
 *
 * Options:
 *   --from YEAR          first year to collect (default: no lower bound)
 *   --to YEAR            last year to collect (default: no upper bound)
 *   --sections a,b,c     sections (default people,tech,auto,realt,money)
 *   --pause-ms N         pause between HTTP requests (default 1000)
 *   --limit N            stop after N article pages (default: no limit)
 *   --discover-only      only read sitemaps and insert pending rows
 *   --enrich-only        only fetch pending article pages
 * </pre>
 * Database connection comes from {@code db.*} system properties / {@code DB_*} env vars (see DatabaseConfig).
 * Safe to stop (Ctrl+C) and rerun: progress is kept in the {@code articles} table.
 */
public class ArticlesCrawlerCli {
    public static void main(String[] args) {
        ArticlesCrawler.Options options = parse(args);
        ArticlesCrawler crawler = new ArticlesCrawler(options, new ArticleRepository(new PostgresConnector()), new PageFetcher());
        Thread worker = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            crawler.requestStop();
            worker.interrupt();
        }));
        crawler.run();
    }

    static ArticlesCrawler.Options parse(String[] args) {
        ArticlesCrawler.Options o = new ArticlesCrawler.Options();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--from":
                    o.fromYear = Integer.parseInt(value(args, ++i, arg));
                    break;
                case "--to":
                    o.toYear = Integer.parseInt(value(args, ++i, arg));
                    break;
                case "--sections":
                    o.sections = Arrays.asList(value(args, ++i, arg).split(","));
                    break;
                case "--pause-ms":
                    o.pauseMillis = Long.parseLong(value(args, ++i, arg));
                    break;
                case "--limit":
                    o.limit = Integer.parseInt(value(args, ++i, arg));
                    break;
                case "--discover-only":
                    o.enrich = false;
                    break;
                case "--enrich-only":
                    o.discover = false;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }
        if (o.fromYear != null && o.toYear != null && o.fromYear > o.toYear) {
            throw new IllegalArgumentException("--from must not be after --to");
        }
        return o;
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }
}
