package trc.allitems.challenge;

import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;

import org.jetbrains.annotations.NotNull;

public enum ChallengeMode implements StringRepresentable {
    FREE("free"),
    ORDER("order");

    public static final Codec<ChallengeMode> CODEC = StringRepresentable.fromEnum(ChallengeMode::values);

    private final String name;

    ChallengeMode(String name) { this.name = name; }

    @Override
    public @NotNull String getSerializedName() { return name; }
}