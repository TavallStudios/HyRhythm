package com.hypixel.hytale.server.core.universe.world.path;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class WorldPath implements IPath<SimplePathWaypoint> {
   public static final BuilderCodec<WorldPath> CODEC;
   protected UUID id;
   protected String name;
   protected List<Transform> waypoints = Collections.emptyList();
   protected List<SimplePathWaypoint> simpleWaypoints;

   protected WorldPath() {
   }

   public WorldPath(String name, List<Transform> waypoints) {
      this.id = UUID.randomUUID();
      this.name = name;
      this.waypoints = waypoints;
   }

   public UUID getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   @Nonnull
   public List<SimplePathWaypoint> getPathWaypoints() {
      if (this.simpleWaypoints == null || this.simpleWaypoints.size() != this.waypoints.size()) {
         this.simpleWaypoints = new ObjectArrayList();

         for(short i = 0; i < this.waypoints.size(); ++i) {
            this.simpleWaypoints.add(new SimplePathWaypoint(i, (Transform)this.waypoints.get(i)));
         }
      }

      this.simpleWaypoints = Collections.unmodifiableList(this.simpleWaypoints);
      return this.simpleWaypoints;
   }

   public int length() {
      return this.waypoints.size();
   }

   public SimplePathWaypoint get(int index) {
      List<SimplePathWaypoint> path = this.getPathWaypoints();
      return (SimplePathWaypoint)path.get(index);
   }

   public List<Transform> getWaypoints() {
      return this.waypoints;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.name;
      return "WorldPath{name='" + var10000 + "', waypoints=" + String.valueOf(this.waypoints) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(WorldPath.class, WorldPath::new).addField(new KeyedCodec("Id", Codec.UUID_BINARY), (worldPath, uuid) -> worldPath.id = uuid, (worldPath) -> worldPath.id)).addField(new KeyedCodec("Name", Codec.STRING), (worldPath, s) -> worldPath.name = s, (worldPath) -> worldPath.name)).addField(new KeyedCodec("Waypoints", new ArrayCodec(Transform.CODEC, (x$0) -> new Transform[x$0])), (worldPath, wayPoints) -> worldPath.waypoints = List.of(wayPoints), (worldPath) -> (Transform[])worldPath.waypoints.toArray((x$0) -> new Transform[x$0]))).build();
   }
}
