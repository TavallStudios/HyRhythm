package com.hyrhythm.debug.interfaces;

import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.dependency.CoreDependencyAccess;

public interface RhythmDebugAccess extends CoreDependencyAccess {
    default boolean isRhythmDebugEnabled() {
        RhythmDebugState state = getRhythmDebugState();
        return state != null && state.isDebugEnabled();
    }

    default void setRhythmDebugEnabled(boolean enabled) {
        RhythmDebugState state = getRhythmDebugState();
        if (state != null) {
            state.setDebugEnabled(enabled);
        }
    }
}
