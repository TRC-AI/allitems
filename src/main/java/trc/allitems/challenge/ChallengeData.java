package trc.allitems.challenge;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class ChallengeData {
    public static final Codec<ChallengeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChallengeMode.CODEC.fieldOf("mode").forGetter(d -> d.mode),
            Codec.BOOL.optionalFieldOf("paused", false).forGetter(d -> d.paused),
            Codec.BOOL.optionalFieldOf("finished", false).forGetter(d -> d.finished),
            Identifier.CODEC.listOf().optionalFieldOf("collected", List.of()).forGetter(d -> List.copyOf(d.collected)),
            Identifier.CODEC.listOf().optionalFieldOf("skipped", List.of()).forGetter(d -> List.copyOf(d.skipped)),
            Identifier.CODEC.optionalFieldOf("target").forGetter(d -> Optional.ofNullable(d.target)),
            Codec.LONG.optionalFieldOf("ticks", 0L).forGetter(d -> d.ticks)
    ).apply(instance, ChallengeData::new));

    public final ChallengeMode mode;
    public boolean paused;
    public boolean finished;
    public final Set<Identifier> collected;
    public final Set<Identifier> skipped;
    public Identifier target;
    public long ticks;

    public ChallengeData(ChallengeMode mode) { this(mode, false, false, List.of(), List.of(), Optional.empty(), 0L); }

    private ChallengeData(ChallengeMode mode, boolean paused, boolean finished, List<Identifier> collected,
                          List<Identifier> skipped, @NotNull Optional<Identifier> target, long ticks) {
        this.mode = mode;
        this.paused = paused;
        this.finished = finished;
        this.collected = new LinkedHashSet<>(collected);
        this.skipped = new LinkedHashSet<>(skipped);
        this.target = target.orElse(null);
        this.ticks = ticks;
    }

    public boolean running() { return !paused && !finished; }
}