package dev.daniel730.rpgserver.gui;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
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

    @Test
    public void reducedMotionForcesInstant() {
        assertSame(GuiMotion.MotionProfile.INSTANT,
                GuiMotion.effectiveProfile(GuiMotion.MotionProfile.HUB, true));
        assertSame(GuiMotion.MotionProfile.RARE,
                GuiMotion.effectiveProfile(GuiMotion.MotionProfile.RARE, false));
    }

    @Test
    public void slotRangeIsInclusive() {
        assertArrayEquals(new int[]{9, 10, 11}, GuiMotion.slotRange(9, 11));
        assertEquals(0, GuiMotion.slotRange(5, 4).length);
    }
}
