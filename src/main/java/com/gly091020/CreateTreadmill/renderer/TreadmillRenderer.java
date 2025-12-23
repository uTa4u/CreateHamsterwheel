package com.gly091020.CreateTreadmill.renderer;

import com.gly091020.CreateTreadmill.CreateTreadmillClient;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.Part;
import com.gly091020.CreateTreadmill.block.TreadmillBlock;
import com.gly091020.CreateTreadmill.block.TreadmillBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class TreadmillRenderer extends KineticBlockEntityRenderer<TreadmillBlockEntity> {
    public TreadmillRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TreadmillBlockEntity be, BlockState state) {
        return CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK,
                KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be)));
    }

    @Override
    public boolean shouldRender(@NotNull TreadmillBlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return super.shouldRender(blockEntity, cameraPos) && blockEntity.getBlockState().getValue(TreadmillBlock.PART) == Part.BOTTOM_FRONT;
    }

    @Override
    protected void renderSafe(TreadmillBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if(be.getBlockState().getValue(TreadmillBlock.PART) != Part.BOTTOM_FRONT){return;}
        if (be.getLevel() != null) {
            light = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
        }
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        if(!VisualizationManager.supportsVisualization(be.getLevel())){
            var facing = be.getBlockState().getValue(TreadmillBlock.HORIZONTAL_FACING);
            SpriteShiftEntry spriteShift = CreateTreadmillClient.BELT_SHIFT;
            SuperByteBuffer beltBuffer = CachedBuffers.partial(CreateTreadmillClient.BELT_MODEL, be.getBlockState())
                    .light(light);
            PoseStack localTransforms = new PoseStack();
            var msr = TransformStack.of(localTransforms);

            var d = 0;
            var xp = 0f;
            var zp = 0f;
            switch (facing){
                case WEST -> {
                    d = 0;
                    xp = 1/16f;
                }
                case EAST -> {
                    d = 180;
                    xp = 1/16f;
                }
                case SOUTH -> {
                    d = 90;
                    xp = 1/16f;
                }
                case NORTH -> {
                    d = 270;
                    xp = 1/16f;
                }
            }

            msr.center().rotate(new Quaternionf().rotationXYZ(0, d * Mth.DEG_TO_RAD, 0)).uncenter().translate(xp, 1, zp);
            VertexConsumer vb = buffer.getBuffer(RenderType.solid());
            float renderTick = AnimationTickHolder.getRenderTime(be.getLevel());
            Direction.AxisDirection axisDirection = facing.getAxisDirection();
            float speed = be.getSpeed();
            if(facing == Direction.SOUTH || facing == Direction.NORTH){
                speed = -speed;
            }
            double scroll = speed * renderTick * axisDirection.getStep() / (31.5 * 16);
            scroll = scroll - Math.floor(scroll);
            scroll = scroll * 0.062;
            beltBuffer.shiftUVScrolling(spriteShift, (float) scroll);
            beltBuffer.transform(localTransforms).renderInto(ms, vb);
        }
    }
}
