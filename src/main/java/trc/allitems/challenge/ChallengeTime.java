package trc.allitems.challenge;

import org.jetbrains.annotations.NotNull;

public final class ChallengeTime {
    private ChallengeTime() {}

    public static @NotNull String format(long ticks) {
        long seconds = ticks / 20;
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
    }
}