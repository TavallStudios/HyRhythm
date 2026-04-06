package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.EntityEffectsUpdate;
import com.hypixel.hytale.protocol.EntityStatsUpdate;
import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.EquipmentUpdate;
import com.hypixel.hytale.protocol.IntangibleUpdate;
import com.hypixel.hytale.protocol.InteractableUpdate;
import com.hypixel.hytale.protocol.InvulnerableUpdate;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.ModelUpdate;
import com.hypixel.hytale.protocol.NameplateUpdate;
import com.hypixel.hytale.protocol.PlayerSkinUpdate;
import com.hypixel.hytale.protocol.PredictionUpdate;
import com.hypixel.hytale.protocol.RespondToHitUpdate;
import com.hypixel.hytale.protocol.TransformUpdate;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolsSetSoundSet;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.protocol.packets.player.SetBlockPlacementOverride;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.PlayerConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.SpawnConfig;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.entity.entities.player.data.UniqueItemUsagesComponent;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.RespawnPage;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.RespondToHit;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.projectile.component.PredictedProjectile;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerSystems {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class PlayerSpawnedSystem extends RefSystem<EntityStore> {
      @Nonnull
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }

      public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         sendPlayerSelf(ref, store);
      }

      public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      /** @deprecated */
      @Deprecated
      public static void sendPlayerSelf(@Nonnull Ref<EntityStore> viewerRef, @Nonnull Store<EntityStore> store) {
         EntityTrackerSystems.EntityViewer entityViewerComponent = (EntityTrackerSystems.EntityViewer)store.getComponent(viewerRef, EntityTrackerSystems.EntityViewer.getComponentType());
         if (entityViewerComponent == null) {
            throw new IllegalArgumentException("Viewer is missing EntityViewer component");
         } else {
            NetworkId networkIdComponent = (NetworkId)store.getComponent(viewerRef, NetworkId.getComponentType());
            if (networkIdComponent == null) {
               throw new IllegalArgumentException("Viewer is missing NetworkId component");
            } else {
               Player playerComponent = (Player)store.getComponent(viewerRef, Player.getComponentType());
               if (playerComponent == null) {
                  throw new IllegalArgumentException("Viewer is missing Player component");
               } else {
                  EntityUpdate entityUpdate = new EntityUpdate();
                  entityUpdate.networkId = networkIdComponent.getId();
                  ObjectArrayList<ComponentUpdate> list = new ObjectArrayList();
                  Archetype<EntityStore> viewerArchetype = store.getArchetype(viewerRef);
                  if (viewerArchetype.contains(Interactable.getComponentType())) {
                     list.add(new InteractableUpdate());
                  }

                  if (viewerArchetype.contains(Intangible.getComponentType())) {
                     list.add(new IntangibleUpdate());
                  }

                  if (viewerArchetype.contains(Invulnerable.getComponentType())) {
                     list.add(new InvulnerableUpdate());
                  }

                  if (viewerArchetype.contains(RespondToHit.getComponentType())) {
                     list.add(new RespondToHitUpdate());
                  }

                  Nameplate nameplateComponent = (Nameplate)store.getComponent(viewerRef, Nameplate.getComponentType());
                  if (nameplateComponent != null) {
                     list.add(new NameplateUpdate(nameplateComponent.getText()));
                  }

                  PredictedProjectile predictionComponent = (PredictedProjectile)store.getComponent(viewerRef, PredictedProjectile.getComponentType());
                  if (predictionComponent != null) {
                     list.add(new PredictionUpdate(predictionComponent.getUuid()));
                  }

                  ModelComponent modelComponent = (ModelComponent)store.getComponent(viewerRef, ModelComponent.getComponentType());
                  ModelUpdate update = new ModelUpdate();
                  update.model = modelComponent != null ? modelComponent.getModel().toPacket() : null;
                  EntityScaleComponent entityScaleComponent = (EntityScaleComponent)store.getComponent(viewerRef, EntityScaleComponent.getComponentType());
                  if (entityScaleComponent != null) {
                     update.entityScale = entityScaleComponent.getScale();
                  }

                  list.add(update);
                  PlayerSkinComponent playerSkinComponent = (PlayerSkinComponent)store.getComponent(viewerRef, PlayerSkinComponent.getComponentType());
                  list.add(new PlayerSkinUpdate(playerSkinComponent != null ? playerSkinComponent.getPlayerSkin() : null));
                  Inventory inventory = playerComponent.getInventory();
                  EquipmentUpdate update = new EquipmentUpdate();
                  ItemContainer armor = inventory.getArmor();
                  update.armorIds = new String[armor.getCapacity()];
                  Arrays.fill(update.armorIds, "");
                  armor.forEachWithMeta((slot, itemStack, armorIds) -> armorIds[slot] = itemStack.getItemId(), update.armorIds);
                  PlayerSettings playerSettingsComponent = (PlayerSettings)store.getComponent(viewerRef, PlayerSettings.getComponentType());
                  if (playerSettingsComponent != null) {
                     PlayerConfig.ArmorVisibilityOption armorVisibilityOption = ((EntityStore)store.getExternalData()).getWorld().getGameplayConfig().getPlayerConfig().getArmorVisibilityOption();
                     if (armorVisibilityOption.canHideHelmet() && playerSettingsComponent.hideHelmet()) {
                        update.armorIds[ItemArmorSlot.Head.ordinal()] = "";
                     }

                     if (armorVisibilityOption.canHideCuirass() && playerSettingsComponent.hideCuirass()) {
                        update.armorIds[ItemArmorSlot.Chest.ordinal()] = "";
                     }

                     if (armorVisibilityOption.canHideGauntlets() && playerSettingsComponent.hideGauntlets()) {
                        update.armorIds[ItemArmorSlot.Hands.ordinal()] = "";
                     }

                     if (armorVisibilityOption.canHidePants() && playerSettingsComponent.hidePants()) {
                        update.armorIds[ItemArmorSlot.Legs.ordinal()] = "";
                     }
                  }

                  ItemStack itemInHand = inventory.getItemInHand();
                  update.rightHandItemId = itemInHand != null ? itemInHand.getItemId() : "Empty";
                  ItemStack utilityItem = inventory.getUtilityItem();
                  update.leftHandItemId = utilityItem != null ? utilityItem.getItemId() : "Empty";
                  list.add(update);
                  TransformComponent transformComponent = (TransformComponent)store.getComponent(viewerRef, TransformComponent.getComponentType());
                  HeadRotation headRotationComponent = (HeadRotation)store.getComponent(viewerRef, HeadRotation.getComponentType());
                  if (transformComponent != null && headRotationComponent != null) {
                     TransformUpdate update = new TransformUpdate();
                     update.transform = new ModelTransform();
                     update.transform.position = PositionUtil.toPositionPacket(transformComponent.getPosition());
                     update.transform.bodyOrientation = PositionUtil.toDirectionPacket(transformComponent.getRotation());
                     update.transform.lookOrientation = PositionUtil.toDirectionPacket(headRotationComponent.getRotation());
                     list.add(update);
                  }

                  EffectControllerComponent effectControllerComponent = (EffectControllerComponent)store.getComponent(viewerRef, EffectControllerComponent.getComponentType());
                  if (effectControllerComponent != null) {
                     list.add(new EntityEffectsUpdate(effectControllerComponent.createInitUpdates()));
                  }

                  EntityStatMap statMapComponent = (EntityStatMap)store.getComponent(viewerRef, EntityStatMap.getComponentType());
                  if (statMapComponent != null) {
                     list.add(new EntityStatsUpdate(statMapComponent.createInitUpdate(true)));
                  }

                  entityUpdate.updates = (ComponentUpdate[])list.toArray((x$0) -> new ComponentUpdate[x$0]);
                  entityViewerComponent.packetReceiver.writeNoCache(new EntityUpdates((int[])null, new EntityUpdate[]{entityUpdate}));
               }
            }
         }
      }
   }

   public static class PlayerAddedSystem extends RefSystem<EntityStore> {
      @Nonnull
      private static final Message MESSAGE_SERVER_GENERAL_KILLED_BY_UNKNOWN = Message.translation("server.general.killedByUnknown");
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAddedSystem(@Nonnull ComponentType<EntityStore, MovementManager> movementManagerComponentType) {
         this.dependencies = Set.of(new SystemDependency(Order.AFTER, PlayerSpawnedSystem.class));
         this.query = Query.<EntityStore>and(Player.getComponentType(), PlayerRef.getComponentType(), movementManagerComponentType);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
         Player playerComponent = (Player)commandBuffer.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         PlayerRef playerRefComponent = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         MovementManager movementManagerComponent = (MovementManager)commandBuffer.getComponent(ref, MovementManager.getComponentType());

         assert movementManagerComponent != null;

         if (commandBuffer.getComponent(ref, DisplayNameComponent.getComponentType()) == null) {
            Message displayName = Message.raw(playerRefComponent.getUsername());
            commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayName));
         }

         playerComponent.setLastSpawnTimeNanos(System.nanoTime());
         PacketHandler playerConnection = playerRefComponent.getPacketHandler();
         Objects.requireNonNull(world, "world");
         Objects.requireNonNull(playerComponent.getPlayerConfigData(), "data");
         PlayerWorldData perWorldData = playerComponent.getPlayerConfigData().getPerWorldData(world.getName());
         Player.initGameMode(ref, commandBuffer);
         playerConnection.writeNoCache(new BuilderToolsSetSoundSet(world.getGameplayConfig().getCreativePlaySoundSetIndex()));
         playerComponent.sendInventory();
         Inventory playerInventory = playerComponent.getInventory();
         playerConnection.writeNoCache(new SetActiveSlot(-1, playerInventory.getActiveHotbarSlot()));
         playerConnection.writeNoCache(new SetActiveSlot(-5, playerInventory.getActiveUtilitySlot()));
         playerConnection.writeNoCache(new SetActiveSlot(-8, playerInventory.getActiveToolsSlot()));
         if (playerInventory.containsBrokenItem()) {
            playerComponent.sendMessage(Message.translation("server.general.repair.itemBrokenOnRespawn").color("#ff5555"));
         }

         playerConnection.writeNoCache(new SetBlockPlacementOverride(playerComponent.isOverrideBlockPlacementRestrictions()));
         DeathComponent deathComponent = (DeathComponent)commandBuffer.getComponent(ref, DeathComponent.getComponentType());
         if (deathComponent != null) {
            Message pendingDeathMessage = deathComponent.getDeathMessage();
            if (pendingDeathMessage == null) {
               ((HytaleLogger.Api)Entity.LOGGER.at(Level.SEVERE).withCause(new Throwable())).log("Player wasn't alive but didn't have a pending death message?");
               pendingDeathMessage = MESSAGE_SERVER_GENERAL_KILLED_BY_UNKNOWN;
            }

            RespawnPage respawnPage = new RespawnPage(playerRefComponent, pendingDeathMessage, deathComponent.displayDataOnDeathScreen(), deathComponent.getDeathItemLoss());
            playerComponent.getPageManager().openCustomPage(ref, store, respawnPage);
         }

         TransformComponent transform = (TransformComponent)commandBuffer.getComponent(ref, TransformComponent.getComponentType());
         GameplayConfig gameplayConfig = world.getGameplayConfig();
         SpawnConfig spawnConfig = gameplayConfig.getSpawnConfig();
         if (transform != null) {
            Vector3d position = transform.getPosition();
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource)commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);
            results.add(ref);
            if (playerComponent.isFirstSpawn()) {
               WorldParticle[] firstSpawnParticles = spawnConfig.getFirstSpawnParticles();
               if (firstSpawnParticles == null) {
                  firstSpawnParticles = spawnConfig.getSpawnParticles();
               }

               if (firstSpawnParticles != null) {
                  ParticleUtil.spawnParticleEffects(firstSpawnParticles, position, (Ref)null, results, commandBuffer);
               }
            } else {
               WorldParticle[] spawnParticles = spawnConfig.getSpawnParticles();
               if (spawnParticles != null) {
                  ParticleUtil.spawnParticleEffects(spawnParticles, position, (Ref)null, results, commandBuffer);
               }
            }
         }

         playerConnection.tryFlush();
         perWorldData.setFirstSpawn(false);
      }

      public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Player playerComponent = (Player)commandBuffer.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getWindowManager().closeAllWindows(ref, commandBuffer);
      }
   }

   public static class ProcessPlayerInput extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query = Query.<EntityStore>and(Player.getComponentType(), PlayerInput.getComponentType(), TransformComponent.getComponentType());

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         PlayerInput playerInputComponent = (PlayerInput)archetypeChunk.getComponent(index, PlayerInput.getComponentType());

         assert playerInputComponent != null;

         List<PlayerInput.InputUpdate> movementUpdateQueue = playerInputComponent.getMovementUpdateQueue();

         for(PlayerInput.InputUpdate entry : movementUpdateQueue) {
            entry.apply(commandBuffer, archetypeChunk, index);
         }

         movementUpdateQueue.clear();
      }
   }

   public static class UpdatePlayerRef extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query = Query.<EntityStore>and(PlayerRef.getComponentType(), TransformComponent.getComponentType(), HeadRotation.getComponentType());

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
         TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         Transform transform = transformComponent.getTransform();
         HeadRotation headRotationComponent = (HeadRotation)archetypeChunk.getComponent(index, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation();
         PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         playerRefComponent.updatePosition(world, transform, headRotation);
      }
   }

   public static class BlockPausedMovementSystem implements RunWhenPausedSystem<EntityStore>, QuerySystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query = Query.<EntityStore>and(Player.getComponentType(), PlayerInput.getComponentType(), TransformComponent.getComponentType(), HeadRotation.getComponentType());

      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         store.forEachChunk(systemIndex, BlockPausedMovementSystem::onTick);
      }

      private static void onTick(@Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         for(int index = 0; index < archetypeChunk.size(); ++index) {
            PlayerInput playerInputComponent = (PlayerInput)archetypeChunk.getComponent(index, PlayerInput.getComponentType());

            assert playerInputComponent != null;

            HeadRotation headRotationComponent = (HeadRotation)archetypeChunk.getComponent(index, HeadRotation.getComponentType());

            assert headRotationComponent != null;

            HeadRotation headRotationComponent = (HeadRotation)archetypeChunk.getComponent(index, HeadRotation.getComponentType());

            assert headRotationComponent != null;

            List<PlayerInput.InputUpdate> movementUpdateQueue = playerInputComponent.getMovementUpdateQueue();

            for(PlayerInput.InputUpdate entry : movementUpdateQueue) {
               Objects.requireNonNull(entry);
               byte var11 = 0;
               //$FF: var11->value
               //0->com/hypixel/hytale/server/core/modules/entity/player/PlayerInput$AbsoluteMovement
               //1->com/hypixel/hytale/server/core/modules/entity/player/PlayerInput$RelativeMovement
               //2->com/hypixel/hytale/server/core/modules/entity/player/PlayerInput$SetHead
               switch (entry.typeSwitch<invokedynamic>(entry, var11)) {
                  case 0:
                     PlayerInput.AbsoluteMovement abs = (PlayerInput.AbsoluteMovement)entry;
                     shouldTeleport = transformComponent.getPosition().distanceSquaredTo(abs.getX(), abs.getY(), abs.getZ()) > 0.009999999776482582;
                     break;
                  case 1:
                     PlayerInput.RelativeMovement rel = (PlayerInput.RelativeMovement)entry;
                     Vector3d position = transformComponent.getPosition();
                     shouldTeleport = transformComponent.getPosition().distanceSquaredTo(position.x + rel.getX(), position.y + rel.getY(), position.z + rel.getZ()) > 0.009999999776482582;
                     break;
                  case 2:
                     PlayerInput.SetHead head = (PlayerInput.SetHead)entry;
                     shouldTeleport = headRotationComponent.getRotation().distanceSquaredTo(head.direction().pitch, head.direction().yaw, head.direction().roll) > 0.01F;
               }
            }

            movementUpdateQueue.clear();
            if (shouldTeleport) {
               Teleport teleport = Teleport.createExact(transformComponent.getPosition(), transformComponent.getRotation(), headRotationComponent.getRotation()).withoutVelocityReset();
               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               commandBuffer.addComponent(ref, Teleport.getComponentType(), teleport);
            }
         }

      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class EnsurePlayerInput extends HolderSystem<EntityStore> {
      public Query<EntityStore> getQuery() {
         return PlayerRef.getComponentType();
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(PlayerInput.getComponentType());
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         holder.removeComponent(PlayerInput.getComponentType());
      }
   }

   public static class EnsureEffectControllerSystem extends HolderSystem<EntityStore> {
      public Query<EntityStore> getQuery() {
         return PlayerRef.getComponentType();
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(EffectControllerComponent.getComponentType());
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class EnsureUniqueItemUsagesSystem extends HolderSystem<EntityStore> {
      public Query<EntityStore> getQuery() {
         return Query.<EntityStore>and(PlayerRef.getComponentType(), Query.not(UniqueItemUsagesComponent.getComponentType()));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(UniqueItemUsagesComponent.getComponentType());
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class PlayerRemovedSystem extends HolderSystem<EntityStore> {
      public Query<EntityStore> getQuery() {
         return Query.<EntityStore>and(PlayerRef.getComponentType(), Player.getComponentType(), TransformComponent.getComponentType(), HeadRotation.getComponentType(), DisplayNameComponent.getComponentType());
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         World world = ((EntityStore)store.getExternalData()).getWorld();
         PlayerRef playerRefComponent = (PlayerRef)holder.getComponent(PlayerRef.getComponentType());

         assert playerRefComponent != null;

         Player playerComponent = (Player)holder.getComponent(Player.getComponentType());

         assert playerComponent != null;

         TransformComponent transformComponent = (TransformComponent)holder.getComponent(TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = (HeadRotation)holder.getComponent(HeadRotation.getComponentType());

         assert headRotationComponent != null;

         DisplayNameComponent displayNameComponent = (DisplayNameComponent)holder.getComponent(DisplayNameComponent.getComponentType());

         assert displayNameComponent != null;

         Message displayName = displayNameComponent.getDisplayName();
         PlayerSystems.LOGGER.at(Level.INFO).log("Removing player '%s%s' from world '%s' (%s)", playerRefComponent.getUsername(), displayName != null ? " (" + displayName.getAnsiMessage() + ")" : "", world.getName(), playerRefComponent.getUuid());
         playerComponent.getPlayerConfigData().getPerWorldData(world.getName()).setLastPosition(new Transform(transformComponent.getPosition().clone(), headRotationComponent.getRotation().clone()));
         playerRefComponent.getPacketHandler().setQueuePackets(false);
         playerRefComponent.getPacketHandler().tryFlush();
         WorldConfig worldConfig = world.getWorldConfig();
         PlayerUtil.broadcastMessageToPlayers(playerRefComponent.getUuid(), Message.translation("server.general.playerLeftWorld").param("username", playerRefComponent.getUsername()).param("world", worldConfig.getDisplayName() != null ? worldConfig.getDisplayName() : WorldConfig.formatDisplayName(world.getName())), store);
      }
   }

   public static class NameplateRefSystem extends RefSystem<EntityStore> {
      @Nonnull
      public Query<EntityStore> getQuery() {
         return Archetype.of(Player.getComponentType(), DisplayNameComponent.getComponentType());
      }

      public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         DisplayNameComponent displayNameComponent = (DisplayNameComponent)commandBuffer.getComponent(ref, DisplayNameComponent.getComponentType());

         assert displayNameComponent != null;

         if (commandBuffer.getComponent(ref, Nameplate.getComponentType()) == null) {
            String displayName = displayNameComponent.getDisplayName() != null ? displayNameComponent.getDisplayName().getAnsiMessage() : "";
            Nameplate nameplateComponent = new Nameplate(displayName);
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), nameplateComponent);
         }
      }

      public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }
   }

   public static class NameplateRefChangeSystem extends RefChangeSystem<EntityStore, DisplayNameComponent> {
      @Nonnull
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }

      @Nonnull
      public ComponentType<EntityStore, DisplayNameComponent> componentType() {
         return DisplayNameComponent.getComponentType();
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DisplayNameComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Nameplate nameplateComponent = (Nameplate)commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText(component.getDisplayName() != null ? component.getDisplayName().getAnsiMessage() : "");
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, DisplayNameComponent oldComponent, @Nonnull DisplayNameComponent newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Nameplate nameplateComponent = (Nameplate)commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText(newComponent.getDisplayName() != null ? newComponent.getDisplayName().getAnsiMessage() : "");
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DisplayNameComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Nameplate nameplateComponent = (Nameplate)commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText("");
      }
   }

   public static class KillFeedKillerEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.KillerMessage> {
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();

      public KillFeedKillerEventSystem() {
         super(KillFeedEvent.KillerMessage.class);
      }

      public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull KillFeedEvent.KillerMessage event) {
         DisplayNameComponent displayNameComponent = (DisplayNameComponent)archetypeChunk.getComponent(index, DisplayNameComponent.getComponentType());
         Message displayName;
         if (displayNameComponent != null) {
            displayName = displayNameComponent.getDisplayName();
         } else {
            PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, this.playerRefComponentType);

            assert playerRefComponent != null;

            displayName = Message.raw(playerRefComponent.getUsername());
         }

         event.setMessage(displayName);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.playerRefComponentType;
      }
   }

   public static class KillFeedDecedentEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.DecedentMessage> {
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();

      public KillFeedDecedentEventSystem() {
         super(KillFeedEvent.DecedentMessage.class);
      }

      public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull KillFeedEvent.DecedentMessage event) {
         DisplayNameComponent displayNameComponent = (DisplayNameComponent)archetypeChunk.getComponent(index, DisplayNameComponent.getComponentType());
         Message displayName;
         if (displayNameComponent != null) {
            displayName = displayNameComponent.getDisplayName();
         } else {
            PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, this.playerRefComponentType);

            assert playerRefComponent != null;

            displayName = Message.raw(playerRefComponent.getUsername());
         }

         event.setMessage(displayName);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.playerRefComponentType;
      }
   }
}
