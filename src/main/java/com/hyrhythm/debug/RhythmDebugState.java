package com.hyrhythm.debug;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RhythmDebugState {
    private final AtomicBoolean debugEnabled = new AtomicBoolean(false);

    public boolean isDebugEnabled() {
        return debugEnabled.get();
    }

    public void setDebugEnabled(boolean enabled) {
        debugEnabled.set(enabled);
    }
}
