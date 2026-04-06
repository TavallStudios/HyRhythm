package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WallPattern extends Pattern {
   @Nonnull
   private final Pattern wallPattern;
   @Nonnull
   private final Pattern originPattern;
   @Nonnull
   private final List<WallDirection> directions;
   private final boolean matchAll;
   private final SpaceSize readSpaceSize;
   @Nonnull
   private final Vector3i rWallPosition;
   @Nonnull
   private final Pattern.Context rWallContext;

   public WallPattern(@Nonnull Pattern wallPattern, @Nonnull Pattern originPattern, @Nonnull List<WallDirection> wallDirections, boolean matchAll) {
      this.wallPattern = wallPattern;
      this.originPattern = originPattern;
      this.directions = new ArrayList(wallDirections);
      this.matchAll = matchAll;
      this.rWallPosition = new Vector3i();
      this.rWallContext = new Pattern.Context();
      SpaceSize originSpace = originPattern.readSpace();
      SpaceSize wallSpace = wallPattern.readSpace();
      SpaceSize totalSpace = originSpace;

      for(WallDirection d : this.directions) {
         SpaceSize var10000;
         switch (d.ordinal()) {
            case 0 -> var10000 = wallSpace.clone().moveBy(new Vector3i(0, 0, -1));
            case 1 -> var10000 = wallSpace.clone().moveBy(new Vector3i(0, 0, 1));
            case 2 -> var10000 = wallSpace.clone().moveBy(new Vector3i(1, 0, 0));
            case 3 -> var10000 = wallSpace.clone().moveBy(new Vector3i(-1, 0, 0));
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         SpaceSize directionedWallSpace = var10000;
         totalSpace = SpaceSize.merge(totalSpace, directionedWallSpace);
      }

      this.readSpaceSize = totalSpace;
   }

   public boolean matches(@Nonnull Pattern.Context context) {
      for(WallDirection direction : this.directions) {
         boolean matches = this.matches(context, direction);
         if (this.matchAll && !matches) {
            return false;
         }

         if (matches) {
            return true;
         }
      }

      return false;
   }

   private boolean matches(@Nonnull Pattern.Context context, @Nonnull WallDirection direction) {
      this.rWallPosition.assign(context.position);
      switch (direction.ordinal()) {
         case 0 -> --this.rWallPosition.z;
         case 1 -> ++this.rWallPosition.z;
         case 2 -> ++this.rWallPosition.x;
         case 3 -> --this.rWallPosition.x;
      }

      this.rWallContext.assign(context);
      this.rWallContext.position = this.rWallPosition;
      return this.originPattern.matches(context) && this.wallPattern.matches(this.rWallContext);
   }

   @Nonnull
   public SpaceSize readSpace() {
      return this.readSpaceSize.clone();
   }

   public static enum WallDirection {
      N,
      S,
      E,
      W;

      @Nonnull
      public static final Codec<WallDirection> CODEC = new EnumCodec<WallDirection>(WallDirection.class, EnumCodec.EnumStyle.LEGACY);
   }
}
