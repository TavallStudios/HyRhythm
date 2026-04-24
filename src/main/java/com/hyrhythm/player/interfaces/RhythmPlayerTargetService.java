package com.hyrhythm.player.interfaces;

import com.hyrhythm.player.model.RhythmPlayerTarget;

import java.util.List;
import java.util.Optional;

public interface RhythmPlayerTargetService {
    Optional<RhythmPlayerTarget> findOnlinePlayer(String lookup);

    List<RhythmPlayerTarget> listOnlinePlayers();
}
