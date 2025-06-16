package com.pey.switchworldmod.switchWorldMod.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Constants {

    public static final String TEST_WORLD_NAME = "testworld";

    public static final String PLAYERDATA_PREVIOUS_DIMENSION_ENTRY = "SwitchWorldMod_PreviousDimension";

    public static final String MOD_ID = "switch-world-mod";

    public static final String MOD_NAME = "SwitchWorld";

    public static final String TESTWORLD_PLAYERDATA_DIRECTORY = String.format("dimensions/%s/%s/playerdata", MOD_ID, TEST_WORLD_NAME);

    public static final RegistryKey<World> TEST_WORLD =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, TEST_WORLD_NAME));

}
