package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator;

import com.hypixel.hytale.builtin.npccombatactionevaluator.CombatActionEvaluatorSystems;
import com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions.CombatActionOption;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import com.hypixel.hytale.server.npc.decisionmaker.core.Option;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatActionEvaluator extends Evaluator<CombatActionOption> implements Component<EntityStore> {
   protected static final float NO_TIMEOUT = 3.4028235E38F;
   protected RunOption runOption;
   protected double minRunUtility;
   protected long lastRunNanos;
   protected int runInState;
   protected float predictability;
   protected double minActionUtility;
   @Nonnull
   protected final Int2ObjectMap<List<Evaluator<CombatActionOption>.OptionHolder>> optionsBySubState;
   @Nonnull
   protected final Int2ObjectMap<CombatActionEvaluatorConfig.BasicAttacks> basicAttacksBySubState;
   protected int currentBasicAttackSubState;
   protected CombatActionEvaluatorConfig.BasicAttacks currentBasicAttackSet;
   @Nullable
   protected String currentBasicAttack;
   protected Function<InteractionContext, Map<String, String>> currentBasicAttacksInteractionVarsGetter;
   protected boolean currentBasicAttackDamageFriendlies;
   protected int nextBasicAttackIndex;
   protected double basicAttackCooldown;
   @Nullable
   protected Ref<EntityStore> basicAttackTarget;
   protected double basicAttackTimeout;
   @Nullable
   protected Ref<EntityStore> primaryTarget;
   @Nullable
   protected Ref<EntityStore> previousTarget;
   @Nullable
   protected CombatOptionHolder currentAction;
   @Nullable
   protected double[] postExecutionDistanceRange;
   protected int markedTargetSlot;
   protected int minRangeSlot;
   protected int maxRangeSlot;
   protected int positioningAngleSlot;
   @Nullable
   protected String currentInteraction;
   protected Function<InteractionContext, Map<String, String>> currentInteractionVarsGetter;
   protected InteractionType currentInteractionType;
   protected float chargeFor;
   protected boolean currentDamageFriendlies;
   protected boolean requireAiming;
   protected boolean positionFirst;
   protected double chargeDistance;
   protected float timeout;
   @Nonnull
   protected final EvaluationContext evaluationContext;

   public static ComponentType<EntityStore, CombatActionEvaluator> getComponentType() {
      return NPCCombatActionEvaluatorPlugin.get().getCombatActionEvaluatorComponentType();
   }

   public CombatActionEvaluator(@Nonnull Role role, @Nonnull CombatActionEvaluatorConfig config, @Nonnull CombatActionEvaluatorSystems.CombatConstructionData data) {
      this.lastRunNanos = NOT_USED;
      this.optionsBySubState = new Int2ObjectOpenHashMap();
      this.basicAttacksBySubState = new Int2ObjectOpenHashMap();
      this.currentBasicAttackSubState = -2147483648;
      this.evaluationContext = new EvaluationContext();
      this.runOption = new RunOption(config.getRunConditions());
      this.runOption.sortConditions();
      this.minRunUtility = config.getMinRunUtility();
      this.minActionUtility = config.getMinActionUtility();
      this.predictability = (float)RandomExtra.randomRange(config.getPredictabilityRange());
      StateMappingHelper stateHelper = role.getStateSupport().getStateHelper();
      String activeState = data.getCombatState();
      this.runInState = stateHelper.getStateIndex(activeState);
      this.markedTargetSlot = data.getMarkedTargetSlot();
      this.minRangeSlot = data.getMinRangeSlot();
      this.maxRangeSlot = data.getMaxRangeSlot();
      this.positioningAngleSlot = data.getPositioningAngleSlot();
      Map<String, String> availableActions = config.getAvailableActions();
      Map<String, Evaluator<CombatActionOption>.OptionHolder> wrappedAvailableActions = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, String> action : availableActions.entrySet()) {
         CombatActionOption option = (CombatActionOption)CombatActionOption.getAssetMap().getAsset((String)action.getValue());
         if (option == null) {
            throw new IllegalStateException(String.format("Option %s does not exist!", action.getValue()));
         }

         option.sortConditions();
         Object var10000;
         switch (option.getActionTarget()) {
            case Self:
               var10000 = new SelfCombatOptionHolder(option);
               break;
            case Hostile:
            case Friendly:
               var10000 = new MultipleTargetCombatOptionHolder(option);
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         CombatOptionHolder holder = (CombatOptionHolder)var10000;
         wrappedAvailableActions.put((String)action.getKey(), holder);
      }

      Map<String, CombatActionEvaluatorConfig.ActionSet> actionSets = config.getActionSets();

      for(Map.Entry<String, CombatActionEvaluatorConfig.ActionSet> subState : actionSets.entrySet()) {
         int subStateIndex = stateHelper.getSubStateIndex(this.runInState, (String)subState.getKey());
         if (subStateIndex == -2147483648) {
            throw new IllegalStateException(String.format("No such state for combat evaluator: %s.%s", activeState, subState.getKey()));
         }

         CombatActionEvaluatorConfig.ActionSet actionSet = (CombatActionEvaluatorConfig.ActionSet)subState.getValue();
         this.basicAttacksBySubState.put(subStateIndex, actionSet.getBasicAttacks());
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List)this.optionsBySubState.computeIfAbsent(subStateIndex, (k) -> new ObjectArrayList());
         String[] combatActions = actionSet.getCombatActions();

         for(String action : combatActions) {
            Evaluator<CombatActionOption>.OptionHolder wrappedAction = (Evaluator.OptionHolder)wrappedAvailableActions.get(action);
            if (wrappedAction == null) {
               throw new IllegalStateException(String.format("No action with name '%s' defined in AvailableActions!", action));
            }

            optionList.add(wrappedAction);
         }
      }

      ObjectIterator var22 = this.optionsBySubState.values().iterator();

      while(var22.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List)var22.next();
         optionList.sort(Comparator.comparingDouble(Evaluator.OptionHolder::getWeightCoefficient).reversed());
      }

   }

   protected CombatActionEvaluator() {
      this.lastRunNanos = NOT_USED;
      this.optionsBySubState = new Int2ObjectOpenHashMap();
      this.basicAttacksBySubState = new Int2ObjectOpenHashMap();
      this.currentBasicAttackSubState = -2147483648;
      this.evaluationContext = new EvaluationContext();
   }

   public RunOption getRunOption() {
      return this.runOption;
   }

   public double getMinRunUtility() {
      return this.minRunUtility;
   }

   @Nonnull
   public EvaluationContext getEvaluationContext() {
      return this.evaluationContext;
   }

   public long getLastRunNanos() {
      return this.lastRunNanos;
   }

   public void setLastRunNanos(long lastRunNanos) {
      this.lastRunNanos = lastRunNanos;
   }

   public int getRunInState() {
      return this.runInState;
   }

   @Nonnull
   public Int2ObjectMap<List<Evaluator<CombatActionOption>.OptionHolder>> getOptionsBySubState() {
      return this.optionsBySubState;
   }

   public CombatActionEvaluatorConfig.BasicAttacks getBasicAttacks(int subState) {
      return (CombatActionEvaluatorConfig.BasicAttacks)this.basicAttacksBySubState.get(subState);
   }

   public void setCurrentBasicAttackSet(int subState, CombatActionEvaluatorConfig.BasicAttacks attacks) {
      if (subState != this.currentBasicAttackSubState) {
         this.nextBasicAttackIndex = 0;
         this.currentBasicAttackSubState = subState;
         this.currentBasicAttackSet = attacks;
      }

   }

   @Nullable
   public String getCurrentBasicAttack() {
      return this.currentBasicAttack;
   }

   public CombatActionEvaluatorConfig.BasicAttacks getCurrentBasicAttackSet() {
      return this.currentBasicAttackSet;
   }

   public void setCurrentBasicAttack(String attack, boolean damageFriendlies, Function<InteractionContext, Map<String, String>> interactionVarsGetter) {
      this.currentBasicAttack = attack;
      this.currentBasicAttacksInteractionVarsGetter = interactionVarsGetter;
      this.currentBasicAttackDamageFriendlies = damageFriendlies;
   }

   public int getNextBasicAttackIndex() {
      return this.nextBasicAttackIndex;
   }

   public void setNextBasicAttackIndex(int next) {
      this.nextBasicAttackIndex = next;
   }

   public boolean canUseBasicAttack(int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer) {
      if (this.basicAttackCooldown > 0.0) {
         return false;
      } else {
         return this.currentAction == null || ((CombatActionOption)this.currentAction.getOption()).isBasicAttackAllowed(selfIndex, archetypeChunk, commandBuffer, this);
      }
   }

   public void tickBasicAttackCoolDown(float dt) {
      if (this.basicAttackCooldown > 0.0) {
         this.basicAttackCooldown -= (double)dt;
      }

   }

   @Nullable
   public Ref<EntityStore> getBasicAttackTarget() {
      return this.basicAttackTarget;
   }

   public void setBasicAttackTarget(Ref<EntityStore> target) {
      this.basicAttackTarget = target;
   }

   public boolean tickBasicAttackTimeout(float dt) {
      return (this.basicAttackTimeout -= (double)dt) <= 0.0;
   }

   public void setBasicAttackTimeout(double timeout) {
      this.basicAttackTimeout = timeout;
   }

   @Nullable
   public Ref<EntityStore> getPrimaryTarget() {
      return this.primaryTarget;
   }

   public void clearPrimaryTarget() {
      this.primaryTarget = null;
   }

   public void setActiveOptions(List<Evaluator<CombatActionOption>.OptionHolder> options) {
      this.options = options;
   }

   public int getMarkedTargetSlot() {
      return this.markedTargetSlot;
   }

   public int getMaxRangeSlot() {
      return this.maxRangeSlot;
   }

   public int getMinRangeSlot() {
      return this.minRangeSlot;
   }

   public int getPositioningAngleSlot() {
      return this.positioningAngleSlot;
   }

   @Nullable
   public String getCurrentAttack() {
      return this.currentBasicAttack != null ? this.currentBasicAttack : this.currentInteraction;
   }

   public float getChargeFor() {
      return this.currentBasicAttack != null ? 0.0F : this.chargeFor;
   }

   public InteractionType getCurrentInteractionType() {
      return this.currentBasicAttack != null ? InteractionType.Primary : this.currentInteractionType;
   }

   public Function<InteractionContext, Map<String, String>> getCurrentInteractionVarsGetter() {
      return this.currentBasicAttack != null ? this.currentBasicAttacksInteractionVarsGetter : this.currentInteractionVarsGetter;
   }

   public boolean shouldDamageFriendlies() {
      return this.currentBasicAttack != null ? this.currentBasicAttackDamageFriendlies : this.currentDamageFriendlies;
   }

   public boolean requiresAiming() {
      return this.currentBasicAttack != null ? true : this.requireAiming;
   }

   public boolean shouldPositionFirst() {
      return this.currentBasicAttack != null ? false : this.positionFirst;
   }

   public double getChargeDistance() {
      return this.currentBasicAttack != null ? 0.0 : this.chargeDistance;
   }

   public void setCurrentInteraction(String currentInteraction, InteractionType interactionType, float chargeFor, boolean damageFriendlies, boolean requireAiming, boolean positionFirst, double chargeDistance, Function<InteractionContext, Map<String, String>> interactionVarsGetter) {
      this.currentInteraction = currentInteraction;
      this.currentInteractionType = interactionType;
      this.chargeFor = chargeFor;
      this.currentDamageFriendlies = damageFriendlies;
      this.requireAiming = requireAiming;
      this.positionFirst = positionFirst;
      this.currentInteractionVarsGetter = interactionVarsGetter;
      this.chargeDistance = chargeDistance;
   }

   @Nullable
   public CombatOptionHolder getCurrentAction() {
      return this.currentAction;
   }

   public double[] consumePostExecutionDistanceRange() {
      double[] distance = this.postExecutionDistanceRange;
      this.postExecutionDistanceRange = null;
      return distance;
   }

   public void setTimeout(float timeout) {
      this.timeout = timeout;
   }

   public void clearTimeout() {
      this.timeout = 3.4028235E38F;
   }

   public boolean hasTimedOut(float dt) {
      return this.timeout != 3.4028235E38F && (this.timeout -= dt) <= 0.0F;
   }

   public void selectNextCombatAction(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull Role role, ValueStore valueStore) {
      this.evaluationContext.setPredictability(this.predictability);
      this.evaluationContext.setMinimumUtility(this.minActionUtility);
      CombatOptionHolder option = (CombatOptionHolder)this.evaluate(index, archetypeChunk, commandBuffer, this.evaluationContext);
      if (option != null) {
         Ref<EntityStore> targetRef = option.getOptionTarget();
         if (targetRef != null && targetRef.isValid()) {
            if (((CombatActionOption)option.getOption()).getActionTarget() == CombatActionOption.Target.Friendly) {
               this.previousTarget = this.primaryTarget;
            }

            this.primaryTarget = targetRef;
            role.getMarkedEntitySupport().setMarkedEntity(this.markedTargetSlot, this.primaryTarget);
         }

         this.currentAction = option;
         ((CombatActionOption)this.currentAction.getOption()).execute(index, archetypeChunk, commandBuffer, role, this, valueStore);
         if (((CombatActionOption)option.getOption()).cancelBasicAttackOnSelect()) {
            this.clearCurrentBasicAttack();
         }

      }
   }

   public void completeCurrentAction(boolean forceClearAbility, boolean clearBasicAttack) {
      if (forceClearAbility || this.currentBasicAttack == null) {
         this.terminateCurrentAction();
         this.lastRunNanos = System.nanoTime();
      }

      if (clearBasicAttack) {
         this.clearCurrentBasicAttack();
      }

   }

   public void terminateCurrentAction() {
      this.currentInteraction = null;
      this.chargeFor = 0.0F;
      if (this.currentAction != null) {
         this.currentAction.setLastUsedNanos(System.nanoTime());
         CombatActionOption option = (CombatActionOption)this.currentAction.getOption();
         if (option.getActionTarget() == CombatActionOption.Target.Friendly) {
            this.primaryTarget = this.previousTarget;
            this.previousTarget = null;
         }

         this.postExecutionDistanceRange = option.getPostExecuteDistanceRange();
         this.currentAction = null;
      }

   }

   public void clearCurrentBasicAttack() {
      if (this.currentBasicAttackSet != null) {
         this.basicAttackCooldown = RandomExtra.randomRange(this.currentBasicAttackSet.getCooldownRange());
      }

      this.currentBasicAttack = null;
      this.basicAttackTarget = null;
   }

   public void setupNPC(Role role) {
      ObjectIterator var2 = this.optionsBySubState.values().iterator();

      while(var2.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List)var2.next();

         for(Evaluator<CombatActionOption>.OptionHolder option : optionList) {
            CombatActionOption opt = (CombatActionOption)option.getOption();
            opt.setupNPC(role);
         }
      }

   }

   public void setupNPC(Holder<EntityStore> holder) {
      ObjectIterator var2 = this.optionsBySubState.values().iterator();

      while(var2.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List)var2.next();

         for(Evaluator<CombatActionOption>.OptionHolder option : optionList) {
            CombatActionOption opt = (CombatActionOption)option.getOption();
            opt.setupNPC(holder);
         }
      }

   }

   @Nonnull
   public Component<EntityStore> clone() {
      CombatActionEvaluator evaluator = new CombatActionEvaluator();
      evaluator.options = this.options;
      evaluator.runOption = this.runOption;
      evaluator.minRunUtility = this.minRunUtility;
      evaluator.minActionUtility = this.minActionUtility;
      evaluator.predictability = this.predictability;
      evaluator.runInState = this.runInState;
      evaluator.optionsBySubState.putAll(this.optionsBySubState);
      evaluator.lastRunNanos = this.lastRunNanos;
      evaluator.markedTargetSlot = this.markedTargetSlot;
      evaluator.minRangeSlot = this.minRangeSlot;
      evaluator.maxRangeSlot = this.maxRangeSlot;
      evaluator.positioningAngleSlot = this.positioningAngleSlot;
      evaluator.primaryTarget = this.primaryTarget;
      evaluator.previousTarget = this.previousTarget;
      evaluator.currentAction = this.currentAction;
      evaluator.currentInteraction = this.currentInteraction;
      evaluator.chargeFor = this.chargeFor;
      evaluator.timeout = this.timeout;
      evaluator.basicAttacksBySubState.putAll(this.basicAttacksBySubState);
      evaluator.nextBasicAttackIndex = this.nextBasicAttackIndex;
      evaluator.basicAttackCooldown = this.basicAttackCooldown;
      evaluator.currentBasicAttackSet = this.currentBasicAttackSet;
      evaluator.currentBasicAttack = this.currentBasicAttack;
      evaluator.basicAttackTimeout = this.basicAttackTimeout;
      evaluator.basicAttackTarget = this.basicAttackTarget;
      evaluator.currentBasicAttackSubState = this.currentBasicAttackSubState;
      evaluator.currentInteractionType = this.currentInteractionType;
      evaluator.currentBasicAttackDamageFriendlies = this.currentBasicAttackDamageFriendlies;
      evaluator.currentDamageFriendlies = this.currentDamageFriendlies;
      evaluator.requireAiming = this.requireAiming;
      return evaluator;
   }

   public static class RunOption extends Option {
      protected RunOption(String[] conditions) {
         this.conditions = conditions;
      }
   }

   public abstract class CombatOptionHolder extends Evaluator<CombatActionOption>.OptionHolder {
      protected long lastUsedNanos;

      protected CombatOptionHolder(CombatActionOption option) {
         super(option);
         this.lastUsedNanos = Evaluator.NOT_USED;
      }

      public void setLastUsedNanos(long lastUsedNanos) {
         this.lastUsedNanos = lastUsedNanos;
      }

      @Nullable
      public Ref<EntityStore> getOptionTarget() {
         return null;
      }
   }

   public class SelfCombatOptionHolder extends CombatOptionHolder {
      protected SelfCombatOptionHolder(CombatActionOption option) {
         super(option);
      }

      public double calculateUtility(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context) {
         context.setLastUsedNanos(this.lastUsedNanos);
         return this.utility = ((CombatActionOption)this.option).calculateUtility(index, archetypeChunk, CombatActionEvaluator.this.primaryTarget, commandBuffer, context);
      }
   }

   public class MultipleTargetCombatOptionHolder extends CombatOptionHolder {
      protected List<Ref<EntityStore>> targets;
      @Nonnull
      protected final DoubleList targetUtilities = new DoubleArrayList();
      @Nullable
      protected Ref<EntityStore> pickedTarget;

      protected MultipleTargetCombatOptionHolder(CombatActionOption option) {
         super(option);
      }

      public double calculateUtility(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context) {
         context.setLastUsedNanos(this.lastUsedNanos);
         TargetMemory targetMemory = (TargetMemory)archetypeChunk.getComponent(index, TargetMemory.getComponentType());
         List var10001;
         switch (((CombatActionOption)this.option).getActionTarget()) {
            case Self -> throw new IllegalStateException("Self option should not be wrapped in a MultipleTargetCombatOptionHolder!");
            case Hostile -> var10001 = targetMemory.getKnownHostilesList();
            case Friendly -> var10001 = targetMemory.getKnownFriendliesList();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         this.targets = var10001;
         this.targetUtilities.clear();
         this.pickedTarget = null;
         this.utility = 0.0;

         for(int i = 0; i < this.targets.size(); ++i) {
            double targetUtility = ((CombatActionOption)this.option).calculateUtility(index, archetypeChunk, (Ref)this.targets.get(i), commandBuffer, context);
            this.targetUtilities.add(i, targetUtility);
            if (targetUtility > this.utility) {
               this.utility = targetUtility;
               this.pickedTarget = (Ref)this.targets.get(i);
            }
         }

         return this.utility;
      }

      public double getTotalUtility(double threshold) {
         double utility = 0.0;

         for(int i = 0; i < this.targets.size(); ++i) {
            double targetUtility = this.targetUtilities.getDouble(i);
            if (targetUtility >= threshold) {
               utility += targetUtility;
            }
         }

         return utility;
      }

      public double tryPick(double currentWeight, double threshold) {
         for(int i = 0; i < this.targets.size(); ++i) {
            double targetUtility = this.targetUtilities.getDouble(i);
            if (!(targetUtility < threshold)) {
               currentWeight -= targetUtility;
               if (currentWeight <= 0.0) {
                  this.pickedTarget = (Ref)this.targets.get(i);
                  break;
               }
            }
         }

         return currentWeight;
      }

      public Ref<EntityStore> getOptionTarget() {
         return this.pickedTarget;
      }
   }
}
