package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DamageCommand extends AbstractPlayerCommand {
   @Nonnull
   private final OptionalArg<Double> amountArg;
   @Nonnull
   private final FlagArg silentArg;

   public DamageCommand() {
      super("damage", "server.commands.damage.desc");
      this.amountArg = this.withOptionalArg("amount", "server.commands.damage.arg.amount.desc", ArgTypes.DOUBLE);
      this.silentArg = this.withFlagArg("silent", "server.commands.damage.arg.silent.desc");
      this.addAliases(new String[]{"hurt"});
      this.requirePermission(HytalePermissions.fromCommand("damage.self"));
      this.addUsageVariant(new DamageOtherCommand());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      double amount = this.amountArg.provided(context) ? (Double)this.amountArg.get(context) : 1.0;
      boolean silent = (Boolean)this.silentArg.get(context);
      Damage.CommandSource damageSource = new Damage.CommandSource(context.sender(), this.getName());
      Damage damage = new Damage(damageSource, DamageCause.COMMAND, (float)amount);
      DamageSystems.executeDamage(ref, store, damage);
      if (!silent) {
         String damageFmt = String.format("%.1f", amount);
         context.sendMessage(Message.translation("server.commands.damage.dealt.self").param("damage", damageFmt));
         if (world.getGameplayConfig().getCombatConfig().isPlayerIncomingDamageDisabled()) {
            context.sendMessage(Message.translation("server.commands.damage.disabled").color("#ffc800"));
         }

      }
   }

   private static class DamageOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg;
      @Nonnull
      private final OptionalArg<Double> amountArg;
      @Nonnull
      private final FlagArg silentArg;

      DamageOtherCommand() {
         super("server.commands.damage.other.desc");
         this.playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
         this.amountArg = this.withOptionalArg("amount", "server.commands.damage.arg.amount.desc", ArgTypes.DOUBLE);
         this.silentArg = this.withFlagArg("silent", "server.commands.damage.arg.silent.desc");
         this.requirePermission(HytalePermissions.fromCommand("damage.other"));
      }

      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = (PlayerRef)this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            world.execute(() -> {
               Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               } else {
                  PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  double amount = this.amountArg.provided(context) ? (Double)this.amountArg.get(context) : 1.0;
                  boolean silent = (Boolean)this.silentArg.get(context);
                  Damage.CommandSource damageSource = new Damage.CommandSource(context.sender(), "damage");
                  Damage damage = new Damage(damageSource, DamageCause.COMMAND, (float)amount);
                  DamageSystems.executeDamage(ref, store, damage);
                  if (!silent) {
                     String damageFmt = String.format("%.1f", amount);
                     context.sendMessage(Message.translation("server.commands.damage.dealt").param("damage", damageFmt).param("victim", playerRefComponent.getUsername()));
                     if (world.getGameplayConfig().getCombatConfig().isPlayerIncomingDamageDisabled()) {
                        context.sendMessage(Message.translation("server.commands.damage.disabled").color("#ffc800"));
                     }

                  }
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
