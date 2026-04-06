package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMapMarker implements Component<ChunkStore> {
   public static final BuilderCodec<BlockMapMarker> CODEC;
   private String name;
   private String icon;

   public BlockMapMarker() {
   }

   public BlockMapMarker(String name, String icon) {
      this.name = name;
      this.icon = icon;
   }

   public static ComponentType<ChunkStore, BlockMapMarker> getComponentType() {
      return BlockModule.get().getBlockMapMarkerComponentType();
   }

   public String getName() {
      return this.name;
   }

   public String getIcon() {
      return this.icon;
   }

   @Nullable
   public Component<ChunkStore> clone() {
      return new BlockMapMarker(this.name, this.icon);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BlockMapMarker.class, BlockMapMarker::new).append(new KeyedCodec("Name", Codec.STRING), (o, v) -> o.name = v, (o) -> o.name).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Icon", Codec.STRING), (o, v) -> o.icon = v, (o) -> o.icon).addValidator(Validators.nonNull()).add()).build();
   }

   public static class OnAddRemove extends RefSystem<ChunkStore> {
      private static final ComponentType<ChunkStore, BlockMapMarker> COMPONENT_TYPE = BlockMapMarker.getComponentType();
      private static final ResourceType<ChunkStore, BlockMapMarkersResource> BLOCK_MAP_MARKERS_RESOURCE_TYPE = BlockMapMarkersResource.getResourceType();

      public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
         BlockModule.BlockStateInfo blockInfo = (BlockModule.BlockStateInfo)commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

         assert blockInfo != null;

         Ref<ChunkStore> chunkRef = blockInfo.getChunkRef();
         if (chunkRef.isValid()) {
            BlockChunk blockChunk = (BlockChunk)commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunk != null;

            BlockMapMarker blockMapMarker = (BlockMapMarker)commandBuffer.getComponent(ref, COMPONENT_TYPE);

            assert blockMapMarker != null;

            WorldChunk wc = (WorldChunk)commandBuffer.getComponent(chunkRef, WorldChunk.getComponentType());
            Vector3i blockPosition = new Vector3i(ChunkUtil.worldCoordFromLocalCoord(wc.getX(), ChunkUtil.xFromBlockInColumn(blockInfo.getIndex())), ChunkUtil.yFromBlockInColumn(blockInfo.getIndex()), ChunkUtil.worldCoordFromLocalCoord(wc.getZ(), ChunkUtil.zFromBlockInColumn(blockInfo.getIndex())));
            BlockMapMarkersResource resource = (BlockMapMarkersResource)commandBuffer.getResource(BLOCK_MAP_MARKERS_RESOURCE_TYPE);
            resource.addMarker(blockPosition, blockMapMarker.getName(), blockMapMarker.getIcon());
         }
      }

      public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
         if (reason == RemoveReason.REMOVE) {
            BlockModule.BlockStateInfo blockInfo = (BlockModule.BlockStateInfo)commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

            assert blockInfo != null;

            Ref<ChunkStore> chunkRef = blockInfo.getChunkRef();
            if (chunkRef.isValid()) {
               WorldChunk wc = (WorldChunk)commandBuffer.getComponent(chunkRef, WorldChunk.getComponentType());
               Vector3i blockPosition = new Vector3i(ChunkUtil.worldCoordFromLocalCoord(wc.getX(), ChunkUtil.xFromBlockInColumn(blockInfo.getIndex())), ChunkUtil.yFromBlockInColumn(blockInfo.getIndex()), ChunkUtil.worldCoordFromLocalCoord(wc.getZ(), ChunkUtil.zFromBlockInColumn(blockInfo.getIndex())));
               BlockMapMarkersResource resource = (BlockMapMarkersResource)commandBuffer.getResource(BLOCK_MAP_MARKERS_RESOURCE_TYPE);
               resource.removeMarker(blockPosition);
            }
         }
      }

      @Nullable
      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE;
      }
   }

   public static class MarkerProvider implements WorldMapManager.MarkerProvider {
      public static final MarkerProvider INSTANCE = new MarkerProvider();

      public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
         BlockMapMarkersResource resource = (BlockMapMarkersResource)world.getChunkStore().getStore().getResource(BlockMapMarkersResource.getResourceType());
         Long2ObjectMap<BlockMapMarkersResource.BlockMapMarkerData> markers = resource.getMarkers();
         ObjectIterator var6 = markers.values().iterator();

         while(var6.hasNext()) {
            BlockMapMarkersResource.BlockMapMarkerData markerData = (BlockMapMarkersResource.BlockMapMarkerData)var6.next();
            Vector3i position = markerData.getPosition();
            MapMarker marker = (new MapMarkerBuilder(markerData.getMarkerId(), markerData.getIcon(), new Transform(position))).withCustomName(markerData.getName()).build();
            collector.add(marker);
         }

      }
   }
}
