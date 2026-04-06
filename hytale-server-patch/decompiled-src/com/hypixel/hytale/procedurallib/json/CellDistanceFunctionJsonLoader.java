package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellType;
import com.hypixel.hytale.procedurallib.logic.cell.GridCellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.HexCellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.MeasurementMode;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CellDistanceFunctionJsonLoader<K extends SeedResource> extends JsonLoader<K, CellDistanceFunction> {
   protected final MeasurementMode measurementMode;
   protected final PointDistanceFunction pointDistanceFunction;

   public CellDistanceFunctionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, @Nullable PointDistanceFunction pointDistanceFunction) {
      this(seed, dataFolder, json, MeasurementMode.CENTRE_DISTANCE, pointDistanceFunction);
   }

   public CellDistanceFunctionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, MeasurementMode measurementMode, @Nullable PointDistanceFunction pointDistanceFunction) {
      super(seed.append(".CellDistanceFunction"), dataFolder, json);
      this.measurementMode = measurementMode;
      this.pointDistanceFunction = pointDistanceFunction;
   }

   public CellDistanceFunction load() {
      CellDistanceFunction distanceFunction = this.loadDistanceFunction();
      Object var10000;
      switch (this.measurementMode) {
         case CENTRE_DISTANCE -> var10000 = distanceFunction;
         case BORDER_DISTANCE -> var10000 = (new CellBorderDistanceFunctionJsonLoader(this.seed, this.dataFolder, this.json, distanceFunction)).load();
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return (CellDistanceFunction)var10000;
   }

   @Nonnull
   protected CellType loadCellType() {
      CellType cellType = CellNoiseJsonLoader.Constants.DEFAULT_CELL_TYPE;
      if (this.has("CellType")) {
         cellType = CellType.valueOf(this.get("CellType").getAsString());
      }

      return cellType;
   }

   @Nonnull
   protected CellDistanceFunction loadDistanceFunction() {
      Object var10000;
      switch (this.loadCellType()) {
         case SQUARE -> var10000 = GridCellDistanceFunction.DISTANCE_FUNCTION;
         case HEX -> var10000 = HexCellDistanceFunction.DISTANCE_FUNCTION;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return (CellDistanceFunction)var10000;
   }
}
