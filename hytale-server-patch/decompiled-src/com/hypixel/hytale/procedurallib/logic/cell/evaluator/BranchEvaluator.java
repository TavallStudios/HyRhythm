package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellPointFunction;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import javax.annotation.Nonnull;

public class BranchEvaluator implements PointEvaluator {
   protected static final int CARDINAL_MASK = 1;
   protected static final int CARDINAL_MASK_RESULT_X = 0;
   protected static final int CARDINAL_MASK_RESULT_Y = 1;
   protected static final int RANDOM_DIRECTION_MASK = 3;
   protected static final Vector2i[] RANDOM_DIRECTIONS = new Vector2i[]{new Vector2i(1, 1), new Vector2i(1, -1), new Vector2i(-1, 1), new Vector2i(-1, -1)};
   @Nonnull
   protected final CellPointFunction pointFunction;
   protected final Direction direction;
   protected final CellJitter jitter;
   protected final double branch2parentScale;
   protected final double invLineNormalization;

   public BranchEvaluator(@Nonnull CellDistanceFunction parentFunction, @Nonnull CellPointFunction linePointFunction, Direction direction, CellJitter jitter, double branchScale) {
      this.pointFunction = linePointFunction;
      this.direction = direction;
      this.jitter = jitter;
      double inverseScalar = 1.0 / linePointFunction.scale(branchScale);
      this.branch2parentScale = parentFunction.scale(inverseScalar);
      this.invLineNormalization = 1.0 / linePointFunction.normalize(1.0);
   }

   public CellJitter getJitter() {
      return this.jitter;
   }

   public void evalPoint(int seed, double x, double y, int hashA, int cax, int cay, double ax, double ay, @Nonnull ResultBuffer.ResultBuffer2d buffer) {
      int dx = getConnectionX(this.direction, buffer.ix2, buffer.x2, hashA, ax * this.branch2parentScale);
      int dy = getConnectionY(this.direction, buffer.ix2, buffer.y2, hashA, ay * this.branch2parentScale);
      int cbx = cax + dx;
      int cby = cay + dy;
      int hashB = this.pointFunction.getHash(seed, cbx, cby);
      DoubleArray.Double2 offsetsB = this.pointFunction.getOffsets(hashB);
      double rawBx = this.getJitter().getPointX(cbx, offsetsB);
      double rawBy = this.getJitter().getPointY(cby, offsetsB);
      double bx = this.pointFunction.getX(rawBx, rawBy);
      double by = this.pointFunction.getY(rawBx, rawBy);
      if (checkBounds(x, y, ax, ay, bx, by, buffer.distance2)) {
         double dist2 = MathUtil.distanceToLineSq(x, y, ax, ay, bx, by);
         dist2 *= this.invLineNormalization;
         buffer.register(hashA, cax, cay, dist2, ax, ay);
      }
   }

   public void evalPoint2(int seed, double x, double y, int cellHash, int xi, int yi, double vecX, double vecY, ResultBuffer.ResultBuffer2d buffer) {
   }

   public void evalPoint(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, ResultBuffer.ResultBuffer3d buffer) {
   }

   public void evalPoint2(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, ResultBuffer.ResultBuffer3d buffer) {
   }

   protected static int getConnectionX(Direction direction, int regionHash, double regionCoord, int cellHash, double cellCoord) {
      if ((cellHash & 1) != 0) {
         return 0;
      } else {
         int var10000;
         switch (direction.ordinal()) {
            case 0 -> var10000 = cellCoord < regionCoord ? -1 : 1;
            case 1 -> var10000 = cellCoord > regionCoord ? -1 : 1;
            case 2 -> var10000 = RANDOM_DIRECTIONS[regionHash & 3].x;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   protected static int getConnectionY(Direction direction, int regionHash, double regionCoord, int cellHash, double cellCoord) {
      if ((cellHash & 1) != 1) {
         return 0;
      } else {
         int var10000;
         switch (direction.ordinal()) {
            case 0 -> var10000 = cellCoord < regionCoord ? -1 : 1;
            case 1 -> var10000 = cellCoord > regionCoord ? -1 : 1;
            case 2 -> var10000 = RANDOM_DIRECTIONS[regionHash & 3].y;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   protected static boolean checkBounds(double x, double y, double ax, double ay, double bx, double by, double thickness) {
      double minX = Math.min(ax, bx) - thickness;
      double minY = Math.min(ay, by) - thickness;
      double maxX = Math.max(ax, bx) + thickness;
      double maxY = Math.max(ay, by) + thickness;
      return x > minX && x < maxX && y > minY && y < maxY;
   }

   public static enum Direction {
      OUTWARD,
      INWARD,
      RANDOM;
   }
}
