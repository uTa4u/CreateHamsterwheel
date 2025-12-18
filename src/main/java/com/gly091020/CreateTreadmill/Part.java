package com.gly091020.CreateTreadmill;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Part implements StringRepresentable {
    TOP_FRONT,
    TOP_BACK,
    BOTTOM_FRONT,
    BOTTOM_BACK;

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
