package com.hypixel.hytale.builtin.hytalegenerator.props.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class StaticDirectionality extends Directionality {
   @Nonnull
   private final List<PrefabRotation> possibleRotations;
   @Nonnull
   private final PrefabRotation rotation;
   @Nonnull
   private final Pattern pattern;

   public StaticDirectionality(@Nonnull PrefabRotation rotation, @Nonnull Pattern pattern) {
      this.rotation = rotation;
      this.pattern = pattern;
      this.possibleRotations = Collections.unmodifiableList(List.of(rotation));
   }

   public PrefabRotation getRotationAt(@Nonnull Pattern.Context context) {
      return this.rotation;
   }

   @Nonnull
   public Pattern getGeneralPattern() {
      return this.pattern;
   }

   @Nonnull
   public Vector3i getReadRangeWith(@Nonnull Scanner scanner) {
      return scanner.readSpaceWith(this.pattern).getRange();
   }

   @Nonnull
   public List<PrefabRotation> getPossibleRotations() {
      return this.possibleRotations;
   }
}
