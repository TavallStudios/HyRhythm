package com.hypixel.hytale.builtin.portals.interactions;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.ui.PortalDeviceActivePage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnterPortalInteraction extends SimpleBlockInteraction {
   @Nonnull
   private static final Message MESSAGE_PORTALS_DEVICE_REF_INVALID = Message.translation("server.portals.device.refInvalid");
   @Nonnull
   private static final Message MESSAGE_PORTALS_DEVICE_WORLD_IS_DEAD = Message.translation("server.portals.device.worldIsDead");
   @Nonnull
   private static final Message MESSAGE_PORTALS_DEVICE_NO_SPAWN = Message.translation("server.portals.device.worldNoSpawn");
   @Nonnull
   private static final Message MESSAGE_PORTALS_DEVICE_BLOCK_ENTITY_REF_INVALID = Message.translation("server.portals.device.blockEntityRefInvalid");
   @Nonnull
   public static final Duration MINIMUM_TIME_IN_WORLD = Duration.ofMillis(3000L);
   @Nonnull
   public static final BuilderCodec<EnterPortalInteraction> CODEC;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = (Player)commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent == null) {
         context.getState().state = InteractionState.Failed;
      } else if (playerComponent.getSinceLastSpawnNanos() < MINIMUM_TIME_IN_WORLD.toNanos()) {
         context.getState().state = InteractionState.Failed;
      } else {
         PortalDevice portalDevice = (PortalDevice)BlockModule.getComponent(PortalDevice.getComponentType(), world, targetBlock.x, targetBlock.y, targetBlock.z);
         if (portalDevice == null) {
            context.getState().state = InteractionState.Failed;
         } else {
            long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z);
            WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
            if (chunk == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               BlockType blockType = chunk.getBlockType(targetBlock);
               if (blockType == null) {
                  context.getState().state = InteractionState.Failed;
               } else {
                  RotationTuple rotation = chunk.getRotation(targetBlock.x, targetBlock.y, targetBlock.z);
                  double yaw = rotation.yaw().getRadians() + 3.141592653589793;
                  Transform returnTransform = new Transform((double)targetBlock.x + 0.5, (double)targetBlock.y + 0.5, (double)targetBlock.z + 0.5, 0.0F, (float)yaw, 0.0F);
                  World targetWorld = portalDevice.getDestinationWorld();
                  if (targetWorld == null) {
                     playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_WORLD_IS_DEAD);
                     context.getState().state = InteractionState.Failed;
                  } else {
                     UUIDComponent uuidComponent = (UUIDComponent)commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
                     if (uuidComponent == null) {
                        context.getState().state = InteractionState.Failed;
                     } else {
                        UUID playerUuid = uuidComponent.getUuid();
                        fetchTargetWorldState(targetWorld, playerUuid).thenAcceptAsync((state) -> {
                           if (!ref.isValid()) {
                              playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_REF_INVALID);
                              context.getState().state = InteractionState.Failed;
                           } else {
                              switch (state.ordinal()) {
                                 case 0:
                                    InstancesPlugin.teleportPlayerToInstance(ref, commandBuffer, targetWorld, returnTransform);
                                    break;
                                 case 1:
                                    playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_WORLD_IS_DEAD);
                                    context.getState().state = InteractionState.Failed;
                                    break;
                                 case 2:
                                    PlayerRef playerRefComponent = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                                    if (playerRefComponent == null) {
                                       context.getState().state = InteractionState.Failed;
                                       return;
                                    }

                                    Ref<ChunkStore> blockEntityRef = BlockModule.getBlockEntity(world, targetBlock.x, targetBlock.y, targetBlock.z);
                                    if (blockEntityRef == null || !blockEntityRef.isValid()) {
                                       playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_BLOCK_ENTITY_REF_INVALID);
                                       context.getState().state = InteractionState.Failed;
                                       return;
                                    }

                                    PortalDeviceActivePage activePage = new PortalDeviceActivePage(playerRefComponent, portalDevice.getConfig(), blockEntityRef);
                                    playerComponent.getPageManager().openCustomPage(ref, world.getEntityStore().getStore(), activePage);
                                    break;
                                 case 3:
                                    playerComponent.sendMessage(MESSAGE_PORTALS_DEVICE_NO_SPAWN);
                                    context.getState().state = InteractionState.Failed;
                              }

                           }
                        }, world);
                     }
                  }
               }
            }
         }
      }
   }

   @Nonnull
   private static CompletableFuture<TargetWorldState> fetchTargetWorldState(@Nonnull World world, @Nonnull UUID playerId) {
      return CompletableFuture.supplyAsync(() -> {
         PortalWorld portalWorld = (PortalWorld)world.getEntityStore().getStore().getResource(PortalWorld.getResourceType());
         if (!portalWorld.exists()) {
            return EnterPortalInteraction.TargetWorldState.WORLD_DEAD;
         } else if (portalWorld.getSpawnPoint() == null) {
            return EnterPortalInteraction.TargetWorldState.NO_SPAWN_AVAILABLE;
         } else {
            return portalWorld.getDiedInWorld().contains(playerId) ? EnterPortalInteraction.TargetWorldState.DIED_IN_WORLD : EnterPortalInteraction.TargetWorldState.OKAY;
         }
      }, world);
   }

   protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {
   }

   static {
      CODEC = BuilderCodec.builder(EnterPortalInteraction.class, EnterPortalInteraction::new, SimpleBlockInteraction.CODEC).build();
   }

   private static enum TargetWorldState {
      OKAY,
      WORLD_DEAD,
      DIED_IN_WORLD,
      NO_SPAWN_AVAILABLE;
   }
}
