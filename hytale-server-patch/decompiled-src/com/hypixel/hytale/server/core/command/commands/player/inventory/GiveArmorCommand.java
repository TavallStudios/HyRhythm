package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class GiveArmorCommand extends AbstractAsyncCommand {
   private static final String PREFIX = "Armor_";
   @Nonnull
   private static final Message MESSAGE_COMMANDS_GIVEARMOR_SUCCESS = Message.translation("server.commands.givearmor.success");
   @Nonnull
   private final OptionalArg<String> playerArg;
   @Nonnull
   private final RequiredArg<String> searchStringArg;
   @Nonnull
   private final FlagArg setFlag;

   public GiveArmorCommand() {
      super("armor", "server.commands.givearmor.desc");
      this.playerArg = this.withOptionalArg("player", "server.commands.givearmor.player.desc", ArgTypes.STRING);
      this.searchStringArg = this.withRequiredArg("search", "server.commands.givearmor.search.desc", ArgTypes.STRING);
      this.setFlag = this.withFlagArg("set", "server.commands.givearmor.set.desc");
   }

   @Nonnull
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      Collection<Ref<EntityStore>> targets;
      if (this.playerArg.provided(context)) {
         String playerInput = (String)this.playerArg.get(context);
         if ("*".equals(playerInput)) {
            targets = new ObjectArrayList();

            for(PlayerRef player : Universe.get().getPlayers()) {
               targets.add(player.getReference());
            }
         } else {
            PlayerRef player = Universe.get().getPlayer(playerInput, NameMatching.DEFAULT);
            if (player == null) {
               context.sendMessage(Message.translation("server.commands.errors.noSuchPlayer").param("username", playerInput));
               return CompletableFuture.completedFuture((Object)null);
            }

            targets = Collections.singletonList(player.getReference());
         }
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "player"));
            return CompletableFuture.completedFuture((Object)null);
         }

         targets = Collections.singletonList(context.senderAsPlayerRef());
      }

      if (targets.isEmpty()) {
         context.sendMessage(Message.translation("server.commands.errors.noSuchPlayer").param("username", "*"));
         return CompletableFuture.completedFuture((Object)null);
      } else {
         String searchString = (String)this.searchStringArg.get(context);
         List<ItemStack> armor = (List)Item.getAssetMap().getAssetMap().keySet().stream().filter((blockTypeKey) -> blockTypeKey.startsWith("Armor_") && blockTypeKey.indexOf(searchString, "Armor_".length()) == "Armor_".length()).map(ItemStack::new).collect(Collectors.toList());
         if (armor.isEmpty()) {
            context.sendMessage(Message.translation("server.commands.givearmor.typeNotFound").param("type", searchString).color(Color.RED));
            return CompletableFuture.completedFuture((Object)null);
         } else {
            Map<World, List<Ref<EntityStore>>> playersByWorld = new Object2ObjectOpenHashMap();

            for(Ref<EntityStore> targetRef : targets) {
               if (targetRef != null && targetRef.isValid()) {
                  Store<EntityStore> store = targetRef.getStore();
                  World world = ((EntityStore)store.getExternalData()).getWorld();
                  ((List)playersByWorld.computeIfAbsent(world, (k) -> new ObjectArrayList())).add(targetRef);
               }
            }

            ObjectArrayList<CompletableFuture<Void>> futures = new ObjectArrayList();
            boolean shouldClear = this.setFlag.provided(context);

            for(Map.Entry<World, List<Ref<EntityStore>>> entry : playersByWorld.entrySet()) {
               World world = (World)entry.getKey();
               List<Ref<EntityStore>> worldPlayers = (List)entry.getValue();
               CompletableFuture<Void> future = this.runAsync(context, () -> {
                  for(Ref<EntityStore> playerRef : worldPlayers) {
                     if (playerRef != null && playerRef.isValid()) {
                        Store<EntityStore> store = playerRef.getStore();
                        Player targetPlayerComponent = (Player)store.getComponent(playerRef, Player.getComponentType());
                        if (targetPlayerComponent != null) {
                           ItemContainer armorInventory = targetPlayerComponent.getInventory().getArmor();
                           if (shouldClear) {
                              armorInventory.clear();
                           }

                           armorInventory.addItemStacks(armor);
                        }
                     }
                  }

               }, world);
               futures.add(future);
            }

            return CompletableFuture.allOf((CompletableFuture[])futures.toArray(new CompletableFuture[0])).thenRun(() -> context.sendMessage(MESSAGE_COMMANDS_GIVEARMOR_SUCCESS));
         }
      }
   }
}
