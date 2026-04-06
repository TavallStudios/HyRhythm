package com.hypixel.hytale.server.worldgen.loader.zone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.util.ColorUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneColorMapping;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZoneColorMappingJsonLoader extends JsonLoader<SeedStringResource, ZoneColorMapping> {
   protected final Map<String, Zone> zoneLookup;

   public ZoneColorMappingJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Map<String, Zone> zoneLookup) {
      super(seed, dataFolder, json);
      this.zoneLookup = zoneLookup;
   }

   @Nonnull
   public ZoneColorMapping load() {
      ZoneColorMapping colorMapping = new ZoneColorMapping();
      JsonObject mappingObj = this.json.getAsJsonObject();

      for(Map.Entry<String, JsonElement> entry : mappingObj.entrySet()) {
         int rgb = ColorUtil.hexString((String)entry.getKey());
         if (((JsonElement)entry.getValue()).isJsonArray()) {
            JsonArray arr = ((JsonElement)entry.getValue()).getAsJsonArray();
            Zone[] zoneArr = new Zone[arr.size()];

            for(int i = 0; i < zoneArr.length; ++i) {
               String zoneName = arr.get(i).getAsString();
               Zone zone = (Zone)this.zoneLookup.get(zoneName);
               if (zone == null) {
                  throw new IllegalArgumentException(String.format("Zone with name %s was not found for color %s!", zoneName, entry.getKey()));
               }

               Objects.requireNonNull(zone);
               zoneArr[i] = zone;
            }

            colorMapping.add(rgb, zoneArr);
         } else {
            String zoneName = ((JsonElement)entry.getValue()).getAsString();
            Zone zone = (Zone)this.zoneLookup.get(zoneName);
            if (zone == null) {
               throw new IllegalArgumentException(String.format("Zone with name %s was not found for color %s!", zoneName, entry.getKey()));
            }

            colorMapping.add(rgb, zone);
         }
      }

      return colorMapping;
   }

   public static void collectZones(Set<String> zoneSet, @Nullable JsonElement json) {
      if (json != null && json.isJsonObject()) {
         JsonObject mappingObj = json.getAsJsonObject();

         for(Map.Entry<String, JsonElement> entry : mappingObj.entrySet()) {
            if (((JsonElement)entry.getValue()).isJsonArray()) {
               for(JsonElement zoneName : ((JsonElement)entry.getValue()).getAsJsonArray()) {
                  zoneSet.add(zoneName.getAsString());
               }
            } else {
               zoneSet.add(((JsonElement)entry.getValue()).getAsString());
            }
         }

      }
   }
}
