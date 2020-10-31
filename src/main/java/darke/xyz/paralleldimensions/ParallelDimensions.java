package darke.xyz.paralleldimensions;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParallelDimensions implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("ParallelDimensions");

    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            if (server.getCurrentPlayerCount() > 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    handlePlayer(server, player);
                }
            }
        });
    }

    private void handlePlayer(MinecraftServer server, ServerPlayerEntity player) {
        if (player.getBlockPos().getY() < -16) {
            if (player.getServerWorld() == server.getWorld(World.OVERWORLD)) {
                LOGGER.info(player.getName().asString() + " is below Y=-16 in The Overworld");
                sendPlayer(server, player, World.NETHER);
            } else if (player.getServerWorld() == server.getWorld(World.END)) {
                LOGGER.info(player.getName().asString() + " is below Y=-16 in The End");
                sendPlayer(server, player, World.OVERWORLD);
            } else {
                LOGGER.info(player.getName().asString() + " is below Y=-16 in The Nether");
                player.sendMessage(new LiteralText("Rest in peace"), true);
            }
        }
    }

    private void sendPlayer(MinecraftServer server, ServerPlayerEntity player, RegistryKey<World> world) {
        Double sourceScale = player.getServerWorld().getDimension().getCoordinateScale();
        Double targetScale = server.getWorld(world).getDimension().getCoordinateScale();

        Double coordinateScale = sourceScale / targetScale;
        
        player.teleport(
                server.getWorld(world),
                player.getX() * coordinateScale,
                512,
                player.getZ() * coordinateScale,
                player.getHeadYaw(),
                player.getPitch(0)
            );

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 3, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * 10, 1));
    }
}
