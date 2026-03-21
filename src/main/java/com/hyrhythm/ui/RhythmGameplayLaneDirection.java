package com.hyrhythm.ui;

final class RhythmGameplayLaneDirection {
    static final RhythmGameplayLaneDirection LEFT = new RhythmGameplayLaneDirection(
        1,
        "left",
        "RhythmGameplayNoteLeft.png",
        "Pages/RhythmGameplayLaneButtonLeft.ui",
        "RhythmGameplayReceptorLeftIdle.png",
        "RhythmGameplayReceptorLeftHit.png",
        "Pages/RhythmGameplayTapNoteLeft.ui",
        "Pages/RhythmGameplayHoldNoteLeft.ui"
    );
    static final RhythmGameplayLaneDirection DOWN = new RhythmGameplayLaneDirection(
        2,
        "down",
        "RhythmGameplayNoteDown.png",
        "Pages/RhythmGameplayLaneButtonDown.ui",
        "RhythmGameplayReceptorDownIdle.png",
        "RhythmGameplayReceptorDownHit.png",
        "Pages/RhythmGameplayTapNoteDown.ui",
        "Pages/RhythmGameplayHoldNoteDown.ui"
    );
    static final RhythmGameplayLaneDirection UP = new RhythmGameplayLaneDirection(
        3,
        "up",
        "RhythmGameplayNoteUp.png",
        "Pages/RhythmGameplayLaneButtonUp.ui",
        "RhythmGameplayReceptorUpIdle.png",
        "RhythmGameplayReceptorUpHit.png",
        "Pages/RhythmGameplayTapNoteUp.ui",
        "Pages/RhythmGameplayHoldNoteUp.ui"
    );
    static final RhythmGameplayLaneDirection RIGHT = new RhythmGameplayLaneDirection(
        4,
        "right",
        "RhythmGameplayNoteRight.png",
        "Pages/RhythmGameplayLaneButtonRight.ui",
        "RhythmGameplayReceptorRightIdle.png",
        "RhythmGameplayReceptorRightHit.png",
        "Pages/RhythmGameplayTapNoteRight.ui",
        "Pages/RhythmGameplayHoldNoteRight.ui"
    );

    final int lane;
    final String idToken;
    final String noteTexturePath;
    final String laneButtonDocumentPath;
    final String idleReceptorTexturePath;
    final String hitReceptorTexturePath;
    final String tapDocumentPath;
    final String holdDocumentPath;

    private RhythmGameplayLaneDirection(
        int lane,
        String idToken,
        String noteTexturePath,
        String laneButtonDocumentPath,
        String idleReceptorTexturePath,
        String hitReceptorTexturePath,
        String tapDocumentPath,
        String holdDocumentPath
    ) {
        this.lane = lane;
        this.idToken = idToken;
        this.noteTexturePath = noteTexturePath;
        this.laneButtonDocumentPath = laneButtonDocumentPath;
        this.idleReceptorTexturePath = idleReceptorTexturePath;
        this.hitReceptorTexturePath = hitReceptorTexturePath;
        this.tapDocumentPath = tapDocumentPath;
        this.holdDocumentPath = holdDocumentPath;
    }

    static RhythmGameplayLaneDirection fromLane(int lane) {
        return switch (lane) {
            case 1 -> LEFT;
            case 2 -> DOWN;
            case 3 -> UP;
            case 4 -> RIGHT;
            default -> throw new IllegalArgumentException("Unsupported 4K lane '" + lane + "'.");
        };
    }

    String documentPath(boolean hold) {
        return hold ? holdDocumentPath : tapDocumentPath;
    }

    String generatedChartTexturePath() {
        return "../Pages/" + noteTexturePath;
    }
}
