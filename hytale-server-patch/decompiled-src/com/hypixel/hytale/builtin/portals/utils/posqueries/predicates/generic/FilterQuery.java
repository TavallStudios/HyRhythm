package com.hypixel.hytale.builtin.portals.utils.posqueries.predicates.generic;

import com.hypixel.hytale.builtin.portals.utils.posqueries.PositionPredicate;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQueryDebug;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FilterQuery implements SpatialQuery {
   private final SpatialQuery query;
   private final PositionPredicate predicate;
   private final boolean failFast;

   public FilterQuery(SpatialQuery query, PositionPredicate predicate) {
      this(query, predicate, false);
   }

   public FilterQuery(SpatialQuery query, PositionPredicate predicate, boolean failFast) {
      this.query = query;
      this.predicate = predicate;
      this.failFast = failFast;
   }

   @Nonnull
   public Stream<Vector3d> createCandidates(@Nonnull World world, @Nonnull Vector3d origin, @Nullable SpatialQueryDebug debug) {
      Stream<Vector3d> stream = this.query.createCandidates(world, origin, debug);
      AtomicBoolean failed = new AtomicBoolean();
      if (this.failFast) {
         stream = stream.takeWhile((candidate) -> !failed.get());
      }

      stream = stream.filter((candidate) -> {
         boolean accepted = this.predicate.test(world, candidate);
         if (debug != null) {
            String var10001 = this.predicate.getClass().getSimpleName();
            debug.appendLine(var10001 + " on " + SpatialQueryDebug.fmt(candidate) + " = " + accepted);
         }

         if (!accepted) {
            failed.set(true);
         }

         return accepted;
      });
      return stream;
   }
}
