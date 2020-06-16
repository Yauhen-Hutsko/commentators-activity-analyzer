package com.hutsko.command.util;

import com.hutsko.command.Command;

import java.util.Optional;

public class ActionFactory {

    public static Optional<Command> defineCommand(String name){
        Optional<Command> current = Optional.empty();
        if (name == null) {
            return current;
        }
        CommandType type = CommandType.valueOf(name.toUpperCase());
        current = Optional.of(type.getCommand());
        return current;
    }
}
