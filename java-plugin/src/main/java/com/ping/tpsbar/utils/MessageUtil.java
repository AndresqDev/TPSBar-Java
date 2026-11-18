package com.ping.tpsbar.utils;

import pumpkin.Common;
import pumpkin.TextComponent;

/** Small helpers for player-facing command messages. */
public abstract class MessageUtil {
    public static TextComponent colored(String text, Common.NamedColor color) {
        TextComponent component = TextComponent.text(text);
        component.colorNamed(color);
        return component;
    }

    public static TextComponent success(String text) {
        return colored(text, Common.NamedColor.GREEN);
    }

    public static TextComponent error(String text) {
        return colored(text, Common.NamedColor.RED);
    }

    public static TextComponent muted(String text) {
        return colored(text, Common.NamedColor.GRAY);
    }
}
