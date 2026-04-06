package com.hypixel.hytale.server.core.plugin.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginListPageManager;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginListPage extends InteractiveCustomUIPage<PluginListPageEventData> {
   private static final Value<String> BUTTON_LABEL_STYLE = Value.<String>ref("Pages/PluginListButton.ui", "LabelStyle");
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.<String>ref("Pages/PluginListButton.ui", "SelectedLabelStyle");
   @Nullable
   private PluginDetails selectedPlugin;
   @Nonnull
   private final ObjectList<PluginDetails> availablePlugins = new ObjectArrayList();
   @Nonnull
   private final ObjectList<PluginDetails> visiblePlugins = new ObjectArrayList();
   @Nullable
   private PluginListPageManager.SessionSettings playerSessionSettings;

   public PluginListPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, PluginListPage.PluginListPageEventData.CODEC);
   }

   public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
      PluginListPageManager pageManager = PluginListPageManager.get();
      pageManager.registerPluginListPage(this);
      this.playerSessionSettings = (PluginListPageManager.SessionSettings)store.ensureAndGetComponent(ref, PluginListPageManager.SessionSettings.getComponentType());
      commandBuilder.append("Pages/PluginListPage.ui");
      this.buildPluginList(commandBuilder, eventBuilder);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#DescriptiveOnlyOption #CheckBox", (new EventData()).append("Option", "DescriptiveOnly"));
      if (!this.visiblePlugins.isEmpty()) {
         this.selectPlugin(((PluginDetails)this.visiblePlugins.getFirst()).identifier.toString(), commandBuilder);
      }

      commandBuilder.set("#DescriptiveOnlyOption #CheckBox.Value", this.playerSessionSettings.descriptiveOnly);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PluginListPageEventData data) {
      assert this.playerSessionSettings != null;

      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      if (data.plugin != null) {
         switch (data.type) {
            case "Select" -> this.selectPlugin(data.plugin, commandBuilder);
            case "Toggle" -> this.checkBoxChanged(data.plugin, commandBuilder);
         }

         this.sendUpdate(commandBuilder, (UIEventBuilder)null, false);
      } else if (data.option != null) {
         switch (data.option) {
            case "DescriptiveOnly":
               this.playerSessionSettings.descriptiveOnly = !this.playerSessionSettings.descriptiveOnly;
               this.buildPluginList(commandBuilder, eventBuilder);
               if (!this.visiblePlugins.isEmpty()) {
                  this.selectPlugin(((PluginDetails)this.visiblePlugins.getFirst()).identifier.toString(), commandBuilder);
               }
            default:
               this.sendUpdate(commandBuilder, eventBuilder, false);
         }
      }

   }

   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      PluginListPageManager.get().deregisterPluginListPage(this);
   }

   private void buildPluginList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      assert this.playerSessionSettings != null;

      commandBuilder.clear("#PluginList");
      this.visiblePlugins.clear();
      this.availablePlugins.clear();
      PluginManager module = PluginManager.get();
      Map<PluginIdentifier, PluginManifest> loadedPlugins = module.getAvailablePlugins();
      loadedPlugins.forEach((identifierx, manifest) -> this.availablePlugins.add(new PluginDetails(manifest, identifierx)));
      int i = 0;

      for(int bound = this.availablePlugins.size(); i < bound; ++i) {
         PluginDetails plugin = (PluginDetails)this.availablePlugins.get(i);
         String desc = plugin.manifest.getDescription();
         if (!this.playerSessionSettings.descriptiveOnly || desc != null && !desc.isEmpty()) {
            this.visiblePlugins.add(plugin);
         }
      }

      i = 0;

      for(int bound = this.visiblePlugins.size(); i < bound; ++i) {
         PluginIdentifier identifier = ((PluginDetails)this.visiblePlugins.get(i)).identifier;
         String id = identifier.toString();
         boolean enabled = false;
         PluginBase loadedPlugin = module.getPlugin(identifier);
         if (loadedPlugin != null) {
            enabled = loadedPlugin.isEnabled();
         }

         String selector = "#PluginList[" + i + "]";
         commandBuilder.append("#PluginList", "Pages/PluginListButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         commandBuilder.set(selector + " #CheckBox.Value", enabled);
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", (new EventData()).append("Plugin", id).append("Type", "Select"), false);
         eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, selector + " #CheckBox", (new EventData()).append("Plugin", id).append("Type", "Toggle"), false);
      }

   }

   private void selectPlugin(@Nonnull String playerSelectedPlugin, @Nonnull UICommandBuilder commandBuilder) {
      PluginDetails nextSelectedPlugin = null;
      ObjectListIterator var4 = this.visiblePlugins.iterator();

      while(var4.hasNext()) {
         PluginDetails plugin = (PluginDetails)var4.next();
         if (playerSelectedPlugin.equals(plugin.identifier.toString())) {
            nextSelectedPlugin = plugin;
            break;
         }
      }

      if (nextSelectedPlugin != null) {
         if (this.selectedPlugin != null && this.visiblePlugins.contains(this.selectedPlugin)) {
            commandBuilder.set("#PluginList[" + this.visiblePlugins.indexOf(this.selectedPlugin) + "] #Button.Style", BUTTON_LABEL_STYLE);
         }

         commandBuilder.set("#PluginList[" + this.visiblePlugins.indexOf(nextSelectedPlugin) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
         commandBuilder.set("#PluginName.Text", nextSelectedPlugin.manifest.getName());
         commandBuilder.set("#PluginIdentifier.Text", nextSelectedPlugin.identifier.toString());
         if (nextSelectedPlugin.manifest.getVersion() != null) {
            commandBuilder.set("#PluginVersion.Text", nextSelectedPlugin.manifest.getVersion().toString());
         } else {
            commandBuilder.set("#PluginVersion.Text", "");
         }

         if (nextSelectedPlugin.manifest.getDescription() != null) {
            commandBuilder.set("#PluginDescription.Text", nextSelectedPlugin.manifest.getDescription());
         } else {
            commandBuilder.set("#PluginDescription.Text", "");
         }

         this.selectedPlugin = nextSelectedPlugin;
      }
   }

   private void checkBoxChanged(@Nonnull String pluginName, @Nonnull UICommandBuilder commandBuilder) {
      PluginDetails changedPlugin = null;
      ObjectListIterator var4 = this.visiblePlugins.iterator();

      while(var4.hasNext()) {
         PluginDetails plugin = (PluginDetails)var4.next();
         if (pluginName.equals(plugin.identifier.toString())) {
            changedPlugin = plugin;
            break;
         }
      }

      if (changedPlugin != null) {
         PluginManager module = PluginManager.get();
         PluginBase activePlugin = module.getPlugin(changedPlugin.identifier);
         CommandManager commandManager = CommandManager.get();
         if (activePlugin != null && activePlugin.isEnabled()) {
            commandManager.handleCommand(this.playerRef, "plugin unload " + String.valueOf(changedPlugin.identifier));
         } else {
            commandManager.handleCommand(this.playerRef, "plugin load " + String.valueOf(changedPlugin.identifier));
         }

      }
   }

   public void handlePluginChangeEvent(@Nonnull PluginIdentifier plugin, boolean activeState) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      PluginDetails key = null;
      int i = 0;

      for(int bound = this.visiblePlugins.size(); i < bound; ++i) {
         PluginDetails details = (PluginDetails)this.visiblePlugins.get(i);
         if (details.identifier.equals(plugin)) {
            key = details;
            break;
         }
      }

      if (key != null) {
         int var10000 = this.visiblePlugins.indexOf(key);
         String selector = "#PluginList[" + var10000 + "]";
         commandBuilder.set(selector + " #CheckBox.Value", activeState);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      }
   }

   public static class PluginListPageEventData {
      static final String KEY_PLUGIN = "Plugin";
      static final String TYPE_KEY = "Type";
      static final String KEY_OPTION = "Option";
      public static final BuilderCodec<PluginListPageEventData> CODEC;
      private String plugin;
      private String option;
      private String type;

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PluginListPageEventData.class, PluginListPageEventData::new).append(new KeyedCodec("Plugin", Codec.STRING), (entry, s) -> entry.plugin = s, (entry) -> entry.plugin).add()).append(new KeyedCodec("Option", Codec.STRING), (entry, s) -> entry.option = s, (entry) -> entry.option).add()).append(new KeyedCodec("Type", Codec.STRING), (entry, s) -> entry.type = s, (entry) -> entry.type).add()).build();
      }
   }

   private static class PluginDetails {
      @Nonnull
      private final PluginManifest manifest;
      @Nonnull
      private final PluginIdentifier identifier;

      public PluginDetails(@Nonnull PluginManifest manifest, @Nonnull PluginIdentifier identifier) {
         this.identifier = identifier;
         this.manifest = manifest;
      }
   }
}
