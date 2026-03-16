package com.hyrhythm.player.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;
import com.hyrhythm.player.model.RhythmPlayerTarget;

import java.util.List;
import java.util.Optional;

public interface RhythmPlayerTargetAccess extends CoreDependencyAccess {
    default RhythmPlayerTargetService getRhythmPlayerTargetService() {
        return DependencyLoaderAccess.requireInstance(RhythmPlayerTargetService.class, "RhythmPlayerTargetService");
    }

    default Optional<RhythmPlayerTarget> findRhythmOnlinePlayer(String lookup) {
        return getRhythmPlayerTargetService().findOnlinePlayer(lookup);
    }

    default List<RhythmPlayerTarget> listRhythmOnlinePlayers() {
        return getRhythmPlayerTargetService().listOnlinePlayers();
    }
}
