package dev.daniel730.rpgserver.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuiMotionTest {

    @Test
    public void hubProfileIsFasterThanRare() {
        assertTrue(GuiMotion.MotionProfile.HUB.slotsPerStep
                >= GuiMotion.MotionProfile.RARE.slotsPerStep);
        assertEquals(0, GuiMotion.MotionProfile.INSTANT.ticksPerStep);
    }

    @Test
    public void cancelUnknownHolderIsSafe() {
        GuiMotion.cancel(new Object());
    }
}