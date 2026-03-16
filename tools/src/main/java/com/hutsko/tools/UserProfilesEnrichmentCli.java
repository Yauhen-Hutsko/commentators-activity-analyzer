package com.hutsko.tools;

import com.hutsko.tools.services.UserProfilesEnrichmentCrawler;

/**
 * CLI entry point for running the profile enrichment crawler from the tools module.
 */
public class UserProfilesEnrichmentCli {
    public static void main(String[] args) {
        int pauseSeconds = 2;
        if (args.length > 0) {
            try {
                pauseSeconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        UserProfilesEnrichmentCrawler crawler = new UserProfilesEnrichmentCrawler(pauseSeconds);
        Runtime.getRuntime().addShutdownHook(new Thread(crawler::requestStop));
        crawler.run();
    }
}
