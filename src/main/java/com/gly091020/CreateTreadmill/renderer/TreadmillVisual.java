package com.gly091020.CreateTreadmill.renderer;

import com.gly091020.CreateTreadmill.CreateTreadmillClient;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import com.simibubi.create.content.processing.burner.ScrollInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class TreadmillVisual extends KineticBlockEntityVisual<TreadmillBlockEntity> {
    private final RotatingInstance shaft;
    private final ScrollInstance belt;
    private final Direction opposite;
    public TreadmillVisual(VisualizationContext context, TreadmillBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        opposite = blockState.getValue(TreadmillBlock.HORIZONTAL_FACING);
        if(blockState.getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){
            shaft = null;
            belt = null;
            return;
        }
        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT, opposite))
                .createInstance();
        var r = rotationAxis();
        shaft.setup(blockEntity, r)
                .setPosition(getVisualPosition())
                .rotateToFace(r)
                .setChanged();

        var d = 0;
        var xp = 0f;
        var zp = 0f;
        switch (blockEntity.getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING)){
            case WEST -> {
                d = 0;
                xp = 1/16f;
            }
            case EAST -> {
                d = 180;
                xp = -1/16f;
            }
            case SOUTH -> {
                d = 90;
                zp = -1/16f;
            }
            case NORTH -> {
                d = 270;
                zp = 1/16f;
            }
        }

        var p = getVisualPosition().above(1);


        belt = instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(CreateTreadmillClient.BELT_MODEL)).createInstance();
        belt.setSpriteShift(CreateTreadmillClient.BELT_SHIFT, 1f, 64/256f * 16)
                .position(p.getX() + xp, p.getY(), p.getZ() + zp)
                .rotation(new Quaternionf().rotationXYZ(0, d * Mth.DEG_TO_RAD, 0))
                .speed(0, 0)
                .offset(0, 0).setChanged();
        update(0);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        if(blockState.getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){return;}
        consumer.accept(shaft);
        consumer.accept(belt);
    }

    @Override
    public void updateLight(float partialTick) {
        if(blockState.getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){return;}
        BlockPos top = pos.above();
        relight(top, shaft);
        relight(top, belt);
    }

    @Override
    public void update(float partialTick) {
        if(blockState.getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){return;}
        shaft.setup(blockEntity).setChanged();
        float speed = 0;
        switch (blockState.getValue(TreadmillBlock.HORIZONTAL_FACING)){
            case NORTH, EAST -> speed = blockEntity.getSpeed();
            case SOUTH, WEST -> speed = -blockEntity.getSpeed();
        }
        belt.speed(0, speed * BeltVisual.MAGIC_SCROLL_MULTIPLIER).setChanged();
    }

    @Override
    protected void _delete() {
        if(blockState.getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){return;}
        shaft.delete();
        belt.delete();
    }
}
