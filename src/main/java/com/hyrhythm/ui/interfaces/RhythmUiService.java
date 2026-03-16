package com.hyrhythm.ui.interfaces;

import com.hypixel.hytale.server.core.entity.entities.Player;

public interface RhythmUiService {
    void openSongSelection(Player player);

    void openGameplay(Player player);

    void openKeybinds(Player player);
}
