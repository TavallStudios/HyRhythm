package com.hyrhythm.ui.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;
import com.hypixel.hytale.server.core.entity.entities.Player;

public interface RhythmUiAccess extends CoreDependencyAccess {
    default RhythmUiService getRhythmUiService() {
        return DependencyLoaderAccess.requireInstance(RhythmUiService.class, "RhythmUiService");
    }

    default void openRhythmSongSelection(Player player) {
        getRhythmUiService().openSongSelection(player);
    }

    default void openRhythmGameplay(Player player) {
        getRhythmUiService().openGameplay(player);
    }

    default void openRhythmKeybinds(Player player) {
        getRhythmUiService().openKeybinds(player);
    }
}
