package com.gly091020.CreateTreadmill.maid.treadmill;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TreadmillSensor extends Sensor<EntityMaid> {
    public TreadmillSensor() {
        super(20);
    }

    @Override
    protected void doTick(@NotNull ServerLevel serverLevel, @NotNull EntityMaid entityMaid) {
        var b = findTreadmill(serverLevel, entityMaid.getOnPos());
        if(b != null){
            entityMaid.getBrain().setMemory(MaidPlugin.TREADMILL_MEMORY.get(), b);
        }
    }

    @Override
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return Set.of(MaidPlugin.TREADMILL_MEMORY.get());
    }

    public static boolean isEmpty(ServerLevel level, BlockPos pos){
        return (level.getBlockEntity(TreadmillBlock.findPart(level, level.getBlockState(pos),
                pos, Part.BOTTOM_FRONT)) instanceof TreadmillBlockEntity entity && entity.getOnTreadmillEntity() == null);
    }

    public static BlockPos findTreadmill(ServerLevel level, BlockPos centerPos) {
        final int radius = 10;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int distance = 0; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    for (int dz = -distance; dz <= distance; dz++) {
                        if (Math.abs(dx) != distance &&
                                Math.abs(dy) != distance &&
                                Math.abs(dz) != distance) {
                            continue;
                        }

                        mutablePos.set(centerPos.getX() + dx, centerPos.getY() + dy, centerPos.getZ() + dz);

                        if (level.getBlockState(mutablePos).is(CreateTreadmillMod.TREADMILL_BLOCK.get()) &&
                                isEmpty(level, mutablePos)) {
                            return mutablePos.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}
