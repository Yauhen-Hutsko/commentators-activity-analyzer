package com.hutsko.tools;

import com.hutsko.tools.services.UserProfilesCollection;

/**
 * CLI entry point for running the user profile crawler from the tools module.
 */
public class UserProfilesCollectorCli {
    public static void main(String[] args) {
        int pauseSeconds = 5;
        if (args.length > 0) {
            try {
                pauseSeconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        UserProfilesCollection collection = new UserProfilesCollection(pauseSeconds);
        Runtime.getRuntime().addShutdownHook(new Thread(collection::requestStop));
        collection.run();
    }
}
