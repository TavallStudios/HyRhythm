package com.hypixel.hytale.server.worldgen.climate.util;

import com.hypixel.hytale.math.util.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.PriorityQueue;
import javax.annotation.Nonnull;

public class DistanceTransform {
   private static final IntArrayList EMPTY_LIST = new IntArrayList();
   private static final int[] DX = new int[]{-1, 1, 0, 0, -1, -1, 1, 1};
   private static final int[] DY = new int[]{0, 0, -1, 1, -1, 1, -1, 1};
   private static final double[] COST = new double[]{1.0, 1.0, 1.0, 1.0, Math.sqrt(2.0), Math.sqrt(2.0), Math.sqrt(2.0), Math.sqrt(2.0)};

   public static void apply(@Nonnull IntMap source, @Nonnull DoubleMap dest, double radius) {
      if (radius <= 0.0) {
         throw new IllegalArgumentException("radius must be > 0");
      } else {
         int width = source.width;
         int height = source.height;
         int size = width * height;
         Int2ObjectOpenHashMap<IntArrayList> regions = new Int2ObjectOpenHashMap();
         Int2ObjectOpenHashMap<IntArrayList> boundaries = new Int2ObjectOpenHashMap();

         for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
               int index = source.index(x, y);
               int value = source.at(index);
               ((IntArrayList)regions.computeIfAbsent(value, (k) -> new IntArrayList())).add(index);

               for(int i = 0; i < 4; ++i) {
                  int nx = x + DX[i];
                  int ny = y + DY[i];
                  if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                     int neighborIndex = source.index(nx, ny);
                     if (source.at(neighborIndex) != value) {
                        ((IntArrayList)boundaries.computeIfAbsent(value, (k) -> new IntArrayList())).add(index);
                        break;
                     }
                  }
               }
            }
         }

         double[] dist = new double[size];
         PriorityQueue<Node> queue = new PriorityQueue(Node::sort);
         ObjectIterator var29 = regions.int2ObjectEntrySet().iterator();

         while(var29.hasNext()) {
            Int2ObjectMap.Entry<IntArrayList> entry = (Int2ObjectMap.Entry)var29.next();
            int id = entry.getIntKey();
            IntArrayList region = (IntArrayList)entry.getValue();
            IntArrayList boundary = (IntArrayList)boundaries.getOrDefault(id, EMPTY_LIST);
            if (boundary.isEmpty()) {
               for(int i = 0; i < region.size(); ++i) {
                  dest.set(region.getInt(i), 1.0);
               }
            } else {
               Arrays.fill(dist, radius);

               for(int i = 0; i < boundary.size(); ++i) {
                  int index = boundary.getInt(i);
                  dist[index] = 0.0;
                  queue.offer(new Node(index, 0.0));
               }

               while(!queue.isEmpty()) {
                  Node node = (Node)queue.poll();
                  int index = node.index;
                  if (!(node.distance > dist[index])) {
                     int cx = index % width;
                     int cy = index / width;

                     for(int i = 0; i < DX.length; ++i) {
                        int nx = cx + DX[i];
                        int ny = cy + DY[i];
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                           int neighborIndex = source.index(nx, ny);
                           int neighborId = source.at(neighborIndex);
                           if (neighborId == id) {
                              double distance = node.distance + COST[i];
                              if (distance < dist[neighborIndex]) {
                                 dist[neighborIndex] = distance;
                                 queue.offer(new Node(neighborIndex, distance));
                              }
                           }
                        }
                     }
                  }
               }

               for(int i = 0; i < region.size(); ++i) {
                  int index = region.getInt(i);
                  double value = MathUtil.clamp(dist[index], 0.0, radius);
                  dest.set(index, value / radius);
               }
            }
         }

      }
   }

   private static record Node(int index, double distance) {
      public static int sort(Node a, Node b) {
         return Double.compare(a.distance, b.distance);
      }
   }
}
