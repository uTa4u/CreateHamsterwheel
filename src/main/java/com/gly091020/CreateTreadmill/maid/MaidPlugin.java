package com.gly091020.CreateTreadmill.maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.gly091020.CreateTreadmill.CreateTreadmillMod;
import com.gly091020.CreateTreadmill.maid.treadmill.TreadmillSensor;
import com.gly091020.CreateTreadmill.maid.treadmill.UseTreadmillTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    public static RegistryObject<MemoryModuleType<BlockPos>> TREADMILL_MEMORY;
    public static RegistryObject<SensorType<TreadmillSensor>> TREADMILL_SENSOR;

    public static void registryData(IEventBus bus) {
        var MEMORY = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, CreateTreadmillMod.MOD_ID);
        var SENSOR = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, CreateTreadmillMod.MOD_ID);
        TREADMILL_MEMORY = MEMORY.register("treadmill_memory", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
        TREADMILL_SENSOR = SENSOR.register("treadmill_sensor", () -> new SensorType<>(TreadmillSensor::new));
        SENSOR.register(bus);
        MEMORY.register(bus);
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new ExtraMaidBrain());
    }

    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new UseTreadmillTask());
    }
}
