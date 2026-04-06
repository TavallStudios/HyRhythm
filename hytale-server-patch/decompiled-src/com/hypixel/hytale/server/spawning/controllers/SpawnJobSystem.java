package com.hypixel.hytale.server.spawning.controllers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.jobs.SpawnJob;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public abstract class SpawnJobSystem<J extends SpawnJob, T extends SpawnController<J>> extends EntityTickingSystem<EntityStore> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int JOB_BUDGET = 64;

   protected void tickSpawnJobs(@Nonnull T spawnController, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      if (world.getPlayerCount() != 0 && world.getWorldConfig().isSpawningNPC() && !spawnController.isUnspawnable() && world.getChunkStore().getStore().getEntityCount() != 0) {
         if (!((double)spawnController.getActualNPCs() > spawnController.getExpectedNPCs())) {
            int blockBudget = SpawningPlugin.get().getTickColumnBudget() / world.getTps();

            try {
               while(blockBudget > 0 && spawnController.getActiveJobCount() != 0) {
                  int jobIndex = 0;

                  while(jobIndex >= 0 && jobIndex < spawnController.getActiveJobCount() && blockBudget > 0) {
                     J job = ((SpawnController)spawnController).getSpawnJob(jobIndex);
                     if (job != null) {
                        job.setColumnBudget(Math.min(64, blockBudget));
                        Result result = this.runJob(spawnController, job, commandBuffer);
                        blockBudget -= job.getBudgetUsed();
                        if (result != SpawnJobSystem.Result.TRY_AGAIN) {
                           List<J> activeJobs = ((SpawnController)spawnController).getActiveJobs();
                           jobIndex = activeJobs.indexOf(job);
                           if (jobIndex != -1) {
                              activeJobs.remove(jobIndex);
                              if (result != SpawnJobSystem.Result.PENDING_SPAWN) {
                                 ((SpawnController)spawnController).addIdleJob(job);
                              }
                           }
                        } else {
                           ++jobIndex;
                        }
                     } else {
                        jobIndex = -1;
                     }
                  }
               }
            } catch (Throwable t) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(t)).log("Failed to tick Spawn Jobs: ");
            }

         }
      }
   }

   protected void onStartRun(@Nonnull J spawnJob) {
      spawnJob.setBudgetUsed(0);
   }

   protected abstract void onEndProbing(T var1, J var2, Result var3, ComponentAccessor<EntityStore> var4);

   protected abstract boolean pickSpawnPosition(T var1, J var2, CommandBuffer<EntityStore> var3);

   protected abstract Result trySpawn(T var1, J var2, CommandBuffer<EntityStore> var3);

   protected abstract Result spawn(World var1, T var2, J var3, CommandBuffer<EntityStore> var4);

   protected Result endProbing(T spawnController, @Nonnull J spawnJob, Result result, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!spawnJob.isTerminated()) {
         this.onEndProbing(spawnController, spawnJob, result, componentAccessor);
         spawnJob.reset();
         spawnJob.setTerminated(true);
      }

      return result;
   }

   private Result runJob(T spawnController, @Nonnull J spawnJob, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      this.onStartRun(spawnJob);
      if (spawnJob.shouldTerminate()) {
         return SpawnJobSystem.Result.FAILED;
      } else {
         ISpawnableWithModel spawnable;
         try {
            spawnable = spawnJob.getSpawnable();
         } catch (IllegalArgumentException e) {
            this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.PERMANENT_FAILURE, commandBuffer);
            throw e;
         }

         if (spawnable == null) {
            HytaleLogger.Api context = LOGGER.at(Level.FINEST);
            if (context.isEnabled()) {
               context.log("Spawn job %s: Terminated, spawnable %s gone", spawnJob.getJobId(), spawnJob.getSpawnableName());
            }

            return this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.FAILED, commandBuffer);
         } else {
            SpawningContext spawningContext = spawnJob.getSpawningContext();
            if (!spawningContext.setSpawnable(spawnable)) {
               HytaleLogger.Api context = LOGGER.at(Level.FINEST);
               if (context.isEnabled()) {
                  context.log("Spawn job %s: Terminated, Unable to set spawnable %s", spawnJob.getJobId(), spawnJob.getSpawnableName());
               }

               return this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.FAILED, commandBuffer);
            } else {
               try {
                  while(spawnJob.budgetAvailable()) {
                     if (spawnJob.shouldTerminate()) {
                        LOGGER.at(Level.FINEST).log("Spawn job %s: Terminated", spawnJob.getJobId());
                        Result var15 = SpawnJobSystem.Result.FAILED;
                        return var15;
                     }

                     if (!this.pickSpawnPosition(spawnController, spawnJob, commandBuffer)) {
                        Result result = this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.FAILED, commandBuffer);
                        return result;
                     }

                     Result result = this.trySpawn(spawnController, spawnJob, commandBuffer);
                     if (result != SpawnJobSystem.Result.TRY_AGAIN) {
                        Result var7 = result;
                        return var7;
                     }
                  }

                  return SpawnJobSystem.Result.TRY_AGAIN;
               } finally {
                  spawningContext.release();
               }
            }
         }
      }
   }

   public static enum Result {
      SUCCESS,
      FAILED,
      TRY_AGAIN,
      PERMANENT_FAILURE,
      PENDING_SPAWN;
   }
}
