package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.npc.asset.builder.validators.StateStringValidator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateMappingHelper {
   public static final String DEFAULT_STATE = "start";
   public static final String DEFAULT_SUB_STATE = "Default";
   public static final String DEFAULT_STATE_PARAMETER = "DefaultState";
   public static final String STATE_CHANGE_RESET_PARAMETER = "ResetOnStateChange";
   @Nullable
   private StateMap mainStateMap = new StateMap();
   private int[] allMainStates;
   @Nullable
   private Int2ObjectOpenHashMap<IStateMap> subStateMap = new Int2ObjectOpenHashMap();
   private int depth;
   @Nullable
   private ArrayDeque<StateDepth> currentParentState = new ArrayDeque();
   private boolean component = true;
   private boolean hasStateEvaluator;
   private boolean requiresStateEvaluator;
   private String defaultSubState;
   private String defaultComponentLocalState;
   private int defaultComponentLocalStateIndex;
   private boolean componentLocalStateAutoReset;
   private Object2IntOpenHashMap<String> componentImportStateMappings;
   private SingletonStateMap singletonDefaultStateMap;

   public int[] getAllMainStates() {
      return this.allMainStates;
   }

   public int getHighestSubStateIndex(int mainStateIndex) {
      return ((IStateMap)this.subStateMap.get(mainStateIndex)).size() - 1;
   }

   public void getAndPutSensorIndex(String state, String subState, @Nonnull BiConsumer<Integer, Integer> setter) {
      this.currentParentState.push(new StateDepth(this.depth, state));
      StateMap var10004 = this.mainStateMap;
      Objects.requireNonNull(var10004);
      this.getAndPutIndex(state, subState, setter, var10004::getAndPutSensorIndex, (i, s) -> {
         IStateMap helper = this.initialiseDefaultSubStates(i);
         return helper.getAndPutSensorIndex(s);
      });
   }

   public void getAndPutSetterIndex(String state, String subState, @Nonnull BiConsumer<Integer, Integer> setter) {
      StateMap var10004 = this.mainStateMap;
      Objects.requireNonNull(var10004);
      this.getAndPutIndex(state, subState, setter, var10004::getAndPutSetterIndex, (i, s) -> {
         IStateMap helper = this.initialiseDefaultSubStates(i);
         return helper.getAndPutSetterIndex(s);
      });
   }

   public void getAndPutStateRequirerIndex(String state, String subState, @Nonnull BiConsumer<Integer, Integer> setter) {
      StateMap var10004 = this.mainStateMap;
      Objects.requireNonNull(var10004);
      this.getAndPutIndex(state, subState, setter, var10004::getAndPutRequirerIndex, (i, s) -> {
         IStateMap helper = this.initialiseDefaultSubStates(i);
         return helper.getAndPutRequirerIndex(s);
      });
   }

   private void getAndPutIndex(String state, @Nullable String subState, @Nonnull BiConsumer<Integer, Integer> setter, @Nonnull Function<String, Integer> mainStateFunction, @Nonnull BiFunction<Integer, String, Integer> subStateFunction) {
      Integer index = (Integer)mainStateFunction.apply(state);
      if (subState == null) {
         setter.accept(index, -1);
      } else {
         Integer subStateIndex = (Integer)subStateFunction.apply(index, subState);
         setter.accept(index, subStateIndex);
      }
   }

   @Nonnull
   private IStateMap initialiseDefaultSubStates(int index) {
      return (IStateMap)this.subStateMap.computeIfAbsent(index, (v) -> {
         StateMap map = new StateMap();
         map.getAndPutSensorIndex(this.defaultSubState);
         map.getAndPutSetterIndex(this.defaultSubState);
         return map;
      });
   }

   public void validate(String configName, @Nonnull List<String> errors) {
      this.mainStateMap.validate(configName, (String)null, errors);
      this.subStateMap.forEach((i, v) -> v.validate(configName, this.mainStateMap.getStateName(i), errors));
      if (!this.hasStateEvaluator && this.requiresStateEvaluator) {
         errors.add(String.format("%s: Expects a state evaluator but does not have one defined", configName));
      }

   }

   public int getStateIndex(String state) {
      return this.mainStateMap.getStateIndex(state);
   }

   public int getSubStateIndex(int index, String subState) {
      return ((IStateMap)this.subStateMap.get(index)).getStateIndex(subState);
   }

   public String getStateName(int index) {
      return this.mainStateMap.getStateName(index);
   }

   public String getSubStateName(int index, int subState) {
      return ((IStateMap)this.subStateMap.get(index)).getStateName(subState);
   }

   @Nullable
   public String getCurrentParentState() {
      return this.currentParentState.isEmpty() ? null : ((StateDepth)this.currentParentState.peek()).state;
   }

   public void increaseDepth() {
      ++this.depth;
   }

   public void decreaseDepth() {
      --this.depth;
      if (!this.currentParentState.isEmpty() && this.depth < ((StateDepth)this.currentParentState.peek()).depth) {
         this.currentParentState.pop();
      }

   }

   public void setDefaultSubState(String subState) {
      this.defaultSubState = subState;
   }

   public String getDefaultSubState() {
      return this.defaultSubState;
   }

   public void setNotComponent() {
      this.mainStateMap.getAndPutSensorIndex("start");
      this.mainStateMap.getAndPutSetterIndex("start");
      this.component = false;
   }

   public boolean isComponent() {
      return this.component;
   }

   public boolean hasComponentStates() {
      return this.component && this.mainStateMap != null;
   }

   public void initialiseComponentState(@Nonnull BuilderSupport support) {
      support.setToNewComponent();
      support.addComponentLocalStateMachine(this.defaultComponentLocalStateIndex);
      if (this.componentLocalStateAutoReset) {
         support.setLocalStateMachineAutoReset();
      }

   }

   public void popComponentState(@Nonnull BuilderSupport support) {
      support.popComponent();
   }

   public void readComponentDefaultLocalState(@Nonnull JsonObject data) {
      String state = BuilderBase.readString(data, "DefaultState", (String)null);
      if (state != null) {
         StateStringValidator validator = StateStringValidator.get();
         if (!validator.test(state)) {
            throw new IllegalStateException(validator.errorMessage(state));
         }

         if (validator.hasMainState()) {
            throw new IllegalStateException(String.format("Default component local state must be defined with a '.' prefix: %s", validator.getMainState()));
         }

         this.defaultComponentLocalState = validator.getSubState();
         this.defaultComponentLocalStateIndex = this.mainStateMap.getAndPutSetterIndex(this.defaultComponentLocalState);
      }

      JsonElement resetValue = data.get("ResetOnStateChange");
      if (resetValue != null) {
         this.componentLocalStateAutoReset = BuilderBase.expectBooleanElement(resetValue, "ResetOnStateChange");
      }

   }

   public boolean hasDefaultLocalState() {
      return this.defaultComponentLocalState != null;
   }

   public String getDefaultLocalState() {
      return this.defaultComponentLocalState;
   }

   public void setComponentImportStateMappings(@Nonnull JsonArray states) {
      this.componentImportStateMappings = new Object2IntOpenHashMap();
      this.componentImportStateMappings.defaultReturnValue(-2147483648);
      StateStringValidator validator = StateStringValidator.mainStateOnly();

      for(int i = 0; i < states.size(); ++i) {
         String string = states.get(i).getAsString();
         if (!validator.test(string)) {
            throw new IllegalStateException(validator.errorMessage(string));
         }

         this.getAndPutSensorIndex(validator.getMainState(), (String)null, (m, s) -> {
         });
         this.componentImportStateMappings.put(validator.getMainState(), i);
      }

      this.componentImportStateMappings.trim();
   }

   public int getComponentImportStateIndex(String state) {
      return this.componentImportStateMappings == null ? -2147483648 : this.componentImportStateMappings.getInt(state);
   }

   public int importedStateCount() {
      return this.componentImportStateMappings == null ? 0 : this.componentImportStateMappings.size();
   }

   public void setRequiresStateEvaluator() {
      this.requiresStateEvaluator = true;
   }

   public void setHasStateEvaluator() {
      this.hasStateEvaluator = true;
   }

   public void optimise() {
      this.currentParentState = null;
      if (this.mainStateMap.isEmpty()) {
         this.mainStateMap = null;
         this.subStateMap = null;
      } else {
         ObjectIterator<Int2ObjectMap.Entry<IStateMap>> iterator = Int2ObjectMaps.fastIterator(this.subStateMap);

         while(iterator.hasNext()) {
            Int2ObjectMap.Entry<IStateMap> next = (Int2ObjectMap.Entry)iterator.next();
            IStateMap map = (IStateMap)next.getValue();
            if (map.size() == 1) {
               if (this.singletonDefaultStateMap == null) {
                  this.singletonDefaultStateMap = new SingletonStateMap(this.defaultSubState);
               }

               next.setValue(this.singletonDefaultStateMap);
            } else {
               map.optimise();
            }
         }

         this.subStateMap.trim();
         this.mainStateMap.optimise();
         this.allMainStates = this.mainStateMap.stateNameMap.keySet().toIntArray();
      }
   }

   private static class StateDepth {
      private final int depth;
      private final String state;

      private StateDepth(int depth, String state) {
         this.depth = depth;
         this.state = state;
      }
   }

   private static class StateMap implements IStateMap {
      private final Int2ObjectOpenHashMap<String> stateNameMap = new Int2ObjectOpenHashMap();
      @Nonnull
      private final Object2IntOpenHashMap<String> stateIndexMap = new Object2IntOpenHashMap();
      private int stateIndexSource;
      @Nullable
      private BitSet stateSensors = new BitSet();
      @Nullable
      private BitSet stateSetters = new BitSet();
      @Nullable
      private BitSet stateRequirers = new BitSet();

      private StateMap() {
         this.stateIndexMap.defaultReturnValue(-2147483648);
      }

      private int getOrCreateIndex(String name) {
         int index = this.stateIndexMap.getInt(name);
         if (index == -2147483648) {
            index = this.stateIndexSource++;
            this.stateIndexMap.put(name, index);
            this.stateNameMap.put(index, name);
         }

         return index;
      }

      public int getAndPutSensorIndex(String state) {
         int index = this.getOrCreateIndex(state);
         this.stateSensors.set(index);
         return index;
      }

      public int getAndPutSetterIndex(String targetState) {
         int index = this.getOrCreateIndex(targetState);
         this.stateSetters.set(index);
         return index;
      }

      public int getAndPutRequirerIndex(String targetState) {
         int index = this.getOrCreateIndex(targetState);
         this.stateRequirers.set(index);
         return index;
      }

      public int getStateIndex(String state) {
         Objects.requireNonNull(state, "State must not be null when fetching index");
         return this.stateIndexMap.getInt(state);
      }

      public String getStateName(int index) {
         return (String)this.stateNameMap.get(index);
      }

      public void validate(String configName, @Nullable String parent, @Nonnull List<String> errors) {
         this.stateSetters.xor(this.stateSensors);
         if (this.stateSetters.cardinality() > 0) {
            errors.add(String.format("%s: State sensor or State setter action/motion exists without accompanying state/setter: %s%s", configName, parent != null ? parent + "." : "", this.stateNameMap.get(this.stateSetters.nextSetBit(0))));
         }

         this.stateRequirers.andNot(this.stateSensors);
         if (this.stateRequirers.cardinality() > 0) {
            errors.add(String.format("%s: State required by a parameter does not exist: %s%s", configName, parent != null ? parent + "." : "", this.stateNameMap.get(this.stateRequirers.nextSetBit(0))));
         }

      }

      public boolean isEmpty() {
         return this.stateNameMap.isEmpty();
      }

      public int size() {
         return this.stateNameMap.size();
      }

      public void optimise() {
         this.stateSensors = null;
         this.stateSetters = null;
         this.stateRequirers = null;
         this.stateNameMap.trim();
         this.stateIndexMap.trim();
      }
   }

   private static class SingletonStateMap implements IStateMap {
      private final String stateName;

      private SingletonStateMap(String name) {
         this.stateName = name;
      }

      public int getAndPutSensorIndex(String state) {
         return 0;
      }

      public int getAndPutSetterIndex(String targetState) {
         return 0;
      }

      public int getAndPutRequirerIndex(String targetState) {
         return 0;
      }

      public int getStateIndex(@Nonnull String state) {
         return !state.equals(this.stateName) ? -2147483648 : 0;
      }

      public String getStateName(int index) {
         return this.stateName;
      }

      public void validate(String configName, String parent, List<String> errors) {
      }

      public boolean isEmpty() {
         return false;
      }

      public int size() {
         return 1;
      }

      public void optimise() {
      }
   }

   private interface IStateMap {
      int getAndPutSensorIndex(String var1);

      int getAndPutSetterIndex(String var1);

      int getAndPutRequirerIndex(String var1);

      int getStateIndex(String var1);

      String getStateName(int var1);

      void validate(String var1, String var2, List<String> var3);

      boolean isEmpty();

      int size();

      void optimise();
   }
}
