package com.hyrhythm.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RhythmGameplayUiAssetPresenceTest {
    @Test
    void semanticReceptorAssetsExistInProjectResources() {
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorLeftIdle.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorLeftHit.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorDownIdle.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorDownHit.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorUpIdle.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorUpHit.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorRightIdle.png"));
        assertNotNull(resource("Common/UI/Custom/Pages/RhythmGameplayReceptorRightHit.png"));
    }

    private static java.net.URL resource(String path) {
        return RhythmGameplayUiAssetPresenceTest.class.getClassLoader().getResource(path);
    }
}
