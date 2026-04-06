package com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition;

import com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation.ObjectiveLocationMarker;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class WeatherTriggerCondition extends ObjectiveLocationTriggerCondition {
   @Nonnull
   public static final BuilderCodec<WeatherTriggerCondition> CODEC;
   @Nonnull
   protected static final ResourceType<EntityStore, WeatherResource> WEATHER_RESOURCE_RESOURCE_TYPE;
   @Nonnull
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE;
   protected String[] weatherIds;
   protected int[] weatherIndexes;

   public boolean isConditionMet(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, ObjectiveLocationMarker objectiveLocationMarker) {
      WeatherResource weatherResource = (WeatherResource)componentAccessor.getResource(WEATHER_RESOURCE_RESOURCE_TYPE);
      TransformComponent transformComponent = (TransformComponent)componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
      if (chunkRef != null && chunkRef.isValid()) {
         World world = ((EntityStore)componentAccessor.getExternalData()).getWorld();
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         BlockChunk blockChunkComponent = (BlockChunk)chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         int environmentIndex = blockChunkComponent.getEnvironment(transformComponent.getPosition());
         int currentWeatherIndex = weatherResource.getWeatherIndexForEnvironment(environmentIndex);
         return Arrays.binarySearch(this.weatherIndexes, currentWeatherIndex) >= 0;
      } else {
         return false;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.weatherIds);
      return "WeatherTriggerCondition{weatherIds=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(WeatherTriggerCondition.class, WeatherTriggerCondition::new).append(new KeyedCodec("WeatherIds", Codec.STRING_ARRAY), (weatherTriggerCondition, strings) -> weatherTriggerCondition.weatherIds = strings, (weatherTriggerCondition) -> weatherTriggerCondition.weatherIds).addValidator(Validators.nonEmptyArray()).addValidator(Weather.VALIDATOR_CACHE.getArrayValidator()).add()).afterDecode((weatherTriggerCondition) -> {
         if (weatherTriggerCondition.weatherIds != null) {
            weatherTriggerCondition.weatherIndexes = new int[weatherTriggerCondition.weatherIds.length];

            for(int i = 0; i < weatherTriggerCondition.weatherIds.length; ++i) {
               String key = weatherTriggerCondition.weatherIds[i];
               int index = Weather.getAssetMap().getIndex(key);
               if (index == -2147483648) {
                  throw new IllegalArgumentException("Unknown key! " + key);
               }

               weatherTriggerCondition.weatherIndexes[i] = index;
            }

            Arrays.sort(weatherTriggerCondition.weatherIndexes);
         }
      })).build();
      WEATHER_RESOURCE_RESOURCE_TYPE = WeatherResource.getResourceType();
      TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   }
}
