package ru.clouddonate.cloudpayments.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.basher.configuration.CommentConfigurationSection;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static final DecimalFormat intFormat = new DecimalFormat("###,###");
    private static final DecimalFormat doubleFormat = new DecimalFormat("###,###.##");

    @NotNull
    public static String toColor(@Nullable String legacyMsg) {
        if (legacyMsg == null) return "";

        String message = legacyMsg.replace("§", "&");

        String hex;
        for (Matcher matcher = Pattern.compile("&#[A-Fa-f0-9]{6}").matcher(message);
             matcher.find();
             message = message.replace("&" + hex, toBukkitHex(hex))
        ) {
            hex = matcher.group().substring(1);
        }

        return message.replace("&", "§");
    }

    public static @NotNull String toBukkitHex(@NotNull String string) {
        if (string.startsWith("#") && string.length() == 7) {
            StringBuilder magic = new StringBuilder("§x");
            for (char c : string.substring(1).toCharArray()) {
                magic.append('§').append(c);
            }

            return magic.toString();
        } else {
            return "§" + string;
        }
    }

    public static void putAndColor(@NotNull CommentConfigurationSection config, @NotNull List<String> to, @NotNull String path) {
        to.clear();
        for (String s : config.getStringList(path)) {
            if (s == null) continue;
            to.add(toColor(s));
        }
    }


    @NotNull
    public static String formatNumber(float number) {
        return doubleFormat.format(number);
    }

    @NotNull
    public static String formatNumber(double number) {
        return doubleFormat.format(number);
    }

    @NotNull
    public static String formatNumber(long number) {
        return intFormat.format(number);
    }

    @NotNull
    public static String formatNumber(int number) {
        return intFormat.format(number);
    }

    public static @NotNull String formatTime(long allSeconds) {
        int days = (int) (allSeconds / 86400L);
        int hours = (int) (allSeconds % 86400L / 3600L);
        int minutes = (int) (allSeconds % 3600L / 60L);
        StringBuilder time = new StringBuilder();
        if (days > 0) {
            time.append(days).append(" ").append(getForm(days, "день", "дня", "дней"));
            if (hours != 0 || minutes != 0) {
                time.append(" ");
            }
        }

        if (hours > 0) {
            time.append(hours).append(" ").append(getForm(hours, "час", "часа", "часов"));
            if (minutes != 0) {
                time.append(" ");
            }
        }

        if (minutes > 0 || time.length() == 0) {
            time.append(minutes).append(" ").append(getForm(minutes, "минута", "минуты", "минут"));
        }

        return time.toString();
    }

    public static @NotNull String formatTimeWithSeconds(long allSeconds) {
        int days = (int) (allSeconds / 86400L);
        int hours = (int) (allSeconds % 86400L / 3600L);
        int minutes = (int) (allSeconds % 3600L / 60L);
        int seconds = (int) (allSeconds % 60L);
        StringBuilder time = new StringBuilder();
        if (days > 0) {
            time.append(days).append(" ").append(getForm(days, "день", "дня", "дней"));
            if (hours != 0 || minutes != 0 || seconds != 0) {
                time.append(" ");
            }
        }

        if (hours > 0) {
            time.append(hours).append(" ").append(getForm(hours, "час", "часа", "часов"));
            if (minutes != 0 || seconds != 0) {
                time.append(" ");
            }
        }

        if (minutes > 0) {
            time.append(minutes).append(" ").append(getForm(minutes, "минута", "минуты", "минут"));
            if (seconds != 0) {
                time.append(" ");
            }
        }

        if (seconds > 0 || time.length() == 0) {
            time.append(seconds).append(" ").append(getForm(seconds, "секунда", "секунды", "секунд"));
        }

        return time.toString();
    }

    private static @NotNull String getForm(int number, String one, String few, String many) {
        number %= 100;
        if (number >= 11 && number <= 14) {
            return many;
        } else {
            switch (number % 10) {
                case 1: {
                    return one;
                }
                case 2:
                case 3:
                case 4: {
                    return few;
                }
                default: {
                    return many;
                }
            }
        }
    }

}
