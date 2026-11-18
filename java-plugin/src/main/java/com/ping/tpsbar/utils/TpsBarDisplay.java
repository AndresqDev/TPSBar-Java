package com.ping.tpsbar.utils;

import com.ping.tpsbar.config.TpsBarSettings;
import com.ping.tpsbar.models.BarMetric;
import com.ping.tpsbar.models.Severity;
import pumpkin.BossBar;
import pumpkin.Common;
import pumpkin.TextComponent;

/** Builds the TPS bar title, progress, and severity colors. */
public abstract class TpsBarDisplay {
    public static TextComponent title(double tps, double mspt, int ping) {
        TextComponent title = TextComponent.text("TPS: ");
        title.colorNamed(Common.NamedColor.GRAY);
        addPart(title, format(tps), textColor(tpsSeverity(tps)));
        addPart(title, "  MSPT: ", Common.NamedColor.GRAY);
        addPart(title, format(mspt), textColor(msptSeverity(mspt)));
        addPart(title, " ms  PING: ", Common.NamedColor.GRAY);
        addPart(title, "" + ping, textColor(pingSeverity(ping)));
        addPart(title, " ms", Common.NamedColor.GRAY);
        return title;
    }

    public static float progress(BarMetric metric, double tps, double mspt, int ping) {
        double value;
        double full;
        if (metric == BarMetric.TPS) {
            value = tps;
            full = TpsBarSettings.TARGET_TPS;
        } else if (metric == BarMetric.PING) {
            value = ping;
            full = TpsBarSettings.PING_FULL;
        } else {
            value = mspt;
            full = TpsBarSettings.MSPT_FULL;
        }
        double ratio = value / full;
        return (float) Math.max(0.0, Math.min(1.0, ratio));
    }

    public static BossBar.BossBarColor barColor(Severity severity) {
        if (severity == Severity.GREEN) {
            return BossBar.BossBarColor.GREEN;
        }
        if (severity == Severity.RED) {
            return BossBar.BossBarColor.RED;
        }
        return BossBar.BossBarColor.YELLOW;
    }

    public static Common.NamedColor textColor(Severity severity) {
        if (severity == Severity.GREEN) {
            return Common.NamedColor.GREEN;
        }
        if (severity == Severity.YELLOW) {
            return Common.NamedColor.YELLOW;
        }
        if (severity == Severity.GOLD) {
            return Common.NamedColor.GOLD;
        }
        return Common.NamedColor.RED;
    }

    public static Severity msptSeverity(double mspt) {
        if (mspt < 35.0) {
            return Severity.GREEN;
        }
        if (mspt < 50.0) {
            return Severity.YELLOW;
        }
        if (mspt < 80.0) {
            return Severity.GOLD;
        }
        return Severity.RED;
    }

    public static Severity tpsSeverity(double tps) {
        if (tps >= 19.0) {
            return Severity.GREEN;
        }
        if (tps >= 17.0) {
            return Severity.YELLOW;
        }
        if (tps >= 12.0) {
            return Severity.GOLD;
        }
        return Severity.RED;
    }

    public static Severity pingSeverity(int ping) {
        if (ping <= 60) {
            return Severity.GREEN;
        }
        if (ping <= 120) {
            return Severity.YELLOW;
        }
        if (ping <= 200) {
            return Severity.GOLD;
        }
        return Severity.RED;
    }

    public static String format(double value) {
        long scaled = (long) (value * 100.0 + 0.5);
        long whole = scaled / 100L;
        long fraction = Math.abs(scaled % 100L);
        return String.valueOf(whole) + "." + (fraction < 10L ? "0" : "") + fraction;
    }

    private static void addPart(TextComponent parent, String text, Common.NamedColor color) {
        TextComponent part = TextComponent.text(text);
        part.colorNamed(color);
        parent.addChild(part);
    }
}
