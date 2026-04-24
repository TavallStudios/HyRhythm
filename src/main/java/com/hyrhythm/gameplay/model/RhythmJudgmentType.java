package com.hyrhythm.gameplay.model;

public enum RhythmJudgmentType {
    PERFECT(true, true, 6, 320),
    GREAT(true, true, 5, 300),
    GOOD(true, true, 4, 200),
    BAD(true, true, 2, 100),
    HOLD_OK(true, true, 6, 250),
    MISS(false, true, 0, 0),
    EARLY_RELEASE(false, true, 0, 0),
    GHOST_TAP(false, false, 0, 0);

    private final boolean countsAsHit;
    private final boolean countsTowardAccuracy;
    private final int accuracyUnits;
    private final int scoreValue;

    RhythmJudgmentType(boolean countsAsHit, boolean countsTowardAccuracy, int accuracyUnits, int scoreValue) {
        this.countsAsHit = countsAsHit;
        this.countsTowardAccuracy = countsTowardAccuracy;
        this.accuracyUnits = accuracyUnits;
        this.scoreValue = scoreValue;
    }

    public boolean countsAsHit() {
        return countsAsHit;
    }

    public boolean countsTowardAccuracy() {
        return countsTowardAccuracy;
    }

    public int accuracyUnits() {
        return accuracyUnits;
    }

    public int scoreValue() {
        return scoreValue;
    }
}
