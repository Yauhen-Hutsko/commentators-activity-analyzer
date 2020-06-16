package com.hutsko.command.util;

import com.hutsko.command.*;

public enum  CommandType {
    TRACK(new TrackArticleCommand()),
    SHUTDOWNALL(new ShutdownAllCommand()),
    SHOWSTATS(new ShowStatsCommand()),
    STOPFOLLOW(new StopFollowCommand())
    ;

    private Command command;

    CommandType(Command command) {
        this.command = command;
    }

    public Command getCommand(){
        return command;
    }
}
