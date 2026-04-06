package com.hypixel.hytale.server.core.asset.type.weather;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateWeathers;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class WeatherPacketGenerator extends SimpleAssetPacketGenerator<String, Weather, IndexedLookupTableAssetMap<String, Weather>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Map<String, Weather> assets) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.Init;
      packet.weathers = new Int2ObjectOpenHashMap();

      for(Map.Entry<String, Weather> entry : assets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, ((Weather)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Map<String, Weather> loadedAssets) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.AddOrUpdate;
      packet.weathers = new Int2ObjectOpenHashMap();

      for(Map.Entry<String, Weather> entry : loadedAssets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, ((Weather)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, Weather> assetMap, @Nonnull Set<String> removed) {
      UpdateWeathers packet = new UpdateWeathers();
      packet.type = UpdateType.Remove;
      packet.weathers = new Int2ObjectOpenHashMap();

      for(String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.weathers.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
