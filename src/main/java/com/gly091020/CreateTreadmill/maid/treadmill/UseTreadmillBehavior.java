package com.gly091020.CreateTreadmill.maid.treadmill;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.gly091020.CreateTreadmill.maid.MaidPlugin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class UseTreadmillBehavior extends Behavior<EntityMaid> {
    private boolean homeMode = false;
    public UseTreadmillBehavior() {
        super(Map.of());
    }

    private boolean isRunning(ServerLevel level, EntityMaid maid){
        var state = level.getBlockState(maid.blockPosition().below());
        if(!state.is(CreateTreadmillMod.TREADMILL_BLOCK.get())){
            return false;
        }
        var part = TreadmillBlock.findPart(level, state, maid.blockPosition().below(), Part.BOTTOM_FRONT);
        if(level.getBlockEntity(part) instanceof TreadmillBlockEntity entity){
            return entity.getOnTreadmillEntity() == maid;
        }
        return false;
    }

    private TreadmillBlockEntity getBlockEntity(ServerLevel level, EntityMaid maid){
        var p = maid.getBrain().getMemory(MaidPlugin.TREADMILL_MEMORY.get());
        if(p.isEmpty()){return null;}
        var state = level.getBlockState(p.get());
        if(!state.is(CreateTreadmillMod.TREADMILL_BLOCK.get())){
            return null;
        }
        var part = TreadmillBlock.findPart(level, state, p.get(), Part.BOTTOM_FRONT);
        if(level.getBlockEntity(part) instanceof TreadmillBlockEntity entity){
            return entity;
        }
        return null;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, EntityMaid entity, long gameTime) {
        return entity.getTask() instanceof UseTreadmillTask;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull EntityMaid entity, long gameTime) {
        super.start(level, entity, gameTime);
        entity.getSchedulePos().setIdlePos(entity.getOnPos());
        homeMode = entity.isHomeModeEnable();
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull EntityMaid maid, long gameTime) {
        super.tick(level, maid, gameTime);
        if(!(maid.getTask() instanceof UseTreadmillTask)){
            stop(level, maid, gameTime);
            return;
        }
        if(isRunning(level, maid)){return;}
        var p = maid.getBrain().getMemory(MaidPlugin.TREADMILL_MEMORY.get());
        if (p.isPresent()) {
            var pos = p.get();
            if(!pos.closerThan(maid.blockPosition(), 30)){
                return;
            }else{
                maid.restrictTo(maid.getOnPos(), 50);
                maid.getSchedulePos().setWorkPos(maid.getOnPos());
                maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 0.5f, 1));
            }
            if (pos.closerThan(maid.blockPosition(), 2)) {
                var blockEntity = getBlockEntity(level, maid);
                if (blockEntity != null) {
                    blockEntity.setOnTreadmillEntity(maid);
                    blockEntity.setEntityTimer(Integer.MAX_VALUE);
                    maid.setHomeModeEnable(true);
                }
            }
        }
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(@NotNull ServerLevel level, EntityMaid maid, long gameTime) {
        if(level.getBlockState(maid.getOnPos()).is(CreateTreadmillMod.TREADMILL_BLOCK.get())){
            var p1 = TreadmillBlock.findPart(level, level.getBlockState(maid.getOnPos()), maid.getOnPos(), Part.BOTTOM_FRONT);
            if(p1 != null && level.getBlockEntity(p1) instanceof TreadmillBlockEntity entity){
                entity.setOnTreadmillEntity(null);
            }
        }
        var p = maid.getBrain().getMemory(MaidPlugin.TREADMILL_MEMORY.get());
        if (p.isPresent() && p.get().closerThan(maid.blockPosition(), 2)) {
            var blockEntity = getBlockEntity(level, maid);
            if (blockEntity != null) {
                blockEntity.setOnTreadmillEntity(null);
            }
        }
        maid.getBrain().eraseMemory(MaidPlugin.TREADMILL_MEMORY.get());
        maid.setHomeModeEnable(homeMode);
    }
}
