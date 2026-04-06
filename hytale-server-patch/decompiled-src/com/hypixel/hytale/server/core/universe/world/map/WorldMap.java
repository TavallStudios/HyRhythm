package com.hypixel.hytale.server.core.universe.world.map;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapChunk;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarkerComponent;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.util.PositionUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import javax.annotation.Nonnull;

public class WorldMap implements NetworkSerializable<UpdateWorldMap> {
   private final Map<String, MapMarker> pointsOfInterest = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Long2ObjectMap<MapImage> chunks;
   private UpdateWorldMap packet;

   public WorldMap(int chunks) {
      this.chunks = new Long2ObjectOpenHashMap(chunks);
   }

   @Nonnull
   public Map<String, MapMarker> getPointsOfInterest() {
      return this.pointsOfInterest;
   }

   @Nonnull
   public Long2ObjectMap<MapImage> getChunks() {
      return this.chunks;
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Vector3i pos) {
      this.addPointOfInterest(id, name, markerType, new Transform(pos));
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Vector3d pos) {
      this.addPointOfInterest(id, name, markerType, new Transform(pos));
   }

   public void addPointOfInterest(String id, String name, String markerType, @Nonnull Transform transform) {
      MapMarker old = (MapMarker)this.pointsOfInterest.putIfAbsent(id, new MapMarker(id, (FormattedMessage)null, name, markerType, PositionUtil.toTransformPacket(transform), (ContextMenuItem[])null, (MapMarkerComponent[])null));
      if (old != null) {
         throw new IllegalArgumentException("Id " + id + " already exists!");
      }
   }

   @Nonnull
   public UpdateWorldMap toPacket() {
      if (this.packet != null) {
         return this.packet;
      } else {
         MapChunk[] mapChunks = new MapChunk[this.chunks.size()];
         int i = 0;

         Long2ObjectMap.Entry<MapImage> entry;
         int chunkX;
         int chunkZ;
         for(ObjectIterator var3 = this.chunks.long2ObjectEntrySet().iterator(); var3.hasNext(); mapChunks[i++] = new MapChunk(chunkX, chunkZ, (MapImage)entry.getValue())) {
            entry = (Long2ObjectMap.Entry)var3.next();
            long index = entry.getLongKey();
            chunkX = ChunkUtil.xOfChunkIndex(index);
            chunkZ = ChunkUtil.zOfChunkIndex(index);
         }

         return this.packet = new UpdateWorldMap(mapChunks, (MapMarker[])this.pointsOfInterest.values().toArray((x$0) -> new MapMarker[x$0]), (String[])null);
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.pointsOfInterest);
      return "WorldMap{pointsOfInterest=" + var10000 + ", chunks=" + String.valueOf(this.chunks) + "}";
   }
}
