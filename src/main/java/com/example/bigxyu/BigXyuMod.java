package com.example.bigxyu;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BigXyuMod implements ModInitializer {

    private static final BlockState BODY = Blocks.PINK_WOOL.defaultBlockState();
    private static final BlockState HEAD = Blocks.PINK_TERRACOTTA.defaultBlockState();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("xyu")
                .executes(ctx -> spawn(ctx, 1))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> spawn(ctx, IntegerArgumentType.getInteger(ctx, "size"))))));
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, int s) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();
        ServerLevel level = src.getLevel();

        Direction fwd = player.getDirection();
        Direction side = fwd.getClockWise();
        BlockPos origin = player.blockPosition().relative(fwd, 3 + 2 * s);

        int ballR = s + 1;
        int shaftR = s;
        int shaftTop = ballR + 5 * s;
        double headR = s + 1;

        sphere(level, origin, fwd, side, -ballR, ballR, 0, ballR, BODY);
        sphere(level, origin, fwd, side, ballR, ballR, 0, ballR, BODY);

        for (int y = ballR; y <= shaftTop; y++) {
            for (int x = -shaftR; x <= shaftR; x++) {
                for (int z = -shaftR; z <= shaftR; z++) {
                    if (x * x + z * z <= shaftR * shaftR + shaftR) {
                        put(level, origin, fwd, side, x, y, z, BODY);
                    }
                }
            }
        }

        sphere(level, origin, fwd, side, 0, shaftTop, 0, headR, HEAD);

        src.sendSuccess(() -> Component.literal("Размер " + s + " — готово."), false);
        return 1;
    }

    private static void sphere(ServerLevel level, BlockPos origin, Direction fwd, Direction side,
                               int cx, int cy, int cz, double r, BlockState state) {
        int ri = (int) Math.ceil(r);
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= r * r + 0.5) {
                        put(level, origin, fwd, side, cx + dx, cy + dy, cz + dz, state);
                    }
                }
            }
        }
    }

    private static void put(ServerLevel level, BlockPos origin, Direction fwd, Direction side,
                            int lx, int ly, int lz, BlockState state) {
        BlockPos pos = origin.offset(
            side.getStepX() * lx + fwd.getStepX() * lz,
            ly,
            side.getStepZ() * lx + fwd.getStepZ() * lz);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
}
