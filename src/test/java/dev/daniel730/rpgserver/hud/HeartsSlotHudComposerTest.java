package dev.daniel730.rpgserver.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HeartsSlotHudComposerTest {

    @Test
    public void fullBarUsesOnlyFilledGlyphs() {
        String bar = HeartsSlotHudComposer.bar(10, 10, 10,
                HeartsSlotHudComposer.HP_FULL, HeartsSlotHudComposer.HP_EMPTY);
        assertEquals(10, bar.length());
        assertFalse(bar.contains(String.valueOf(HeartsSlotHudComposer.HP_EMPTY)));
        assertTrue(bar.chars().allMatch(c -> c == HeartsSlotHudComposer.HP_FULL));
    }

    @Test
    public void emptyBarUsesOnlyEmptyGlyphs() {
        String bar = HeartsSlotHudComposer.bar(0, 20, 10,
                HeartsSlotHudComposer.MANA_FULL, HeartsSlotHudComposer.MANA_EMPTY);
        assertTrue(bar.chars().allMatch(c -> c == HeartsSlotHudComposer.MANA_EMPTY));
    }

    @Test
    public void encodeSpaceUsesPowersOfTwo() {
        assertEquals("", HeartsSlotHudComposer.encodeSpace(0));
        // 82 = 64+16+2
        String s = HeartsSlotHudComposer.encodeSpace(-82);
        assertTrue(s.indexOf('\uF80B') >= 0); // -64
        assertTrue(s.indexOf('\uF809') >= 0); // -16
        assertTrue(s.indexOf('\uF802') >= 0); // -2
    }

    @Test
    public void buildIncludesHpManaAndShift() {
        String glyphs = HeartsSlotHudComposer.build(20, 20, 5, 10, 10, 82, 4);
        assertTrue(glyphs.indexOf(HeartsSlotHudComposer.HP_FULL) >= 0);
        assertTrue(glyphs.indexOf(HeartsSlotHudComposer.MANA_FULL) >= 0);
        assertTrue(glyphs.indexOf(HeartsSlotHudComposer.MANA_EMPTY) >= 0);
        assertTrue(glyphs.indexOf(HeartsSlotHudComposer.SEP) >= 0);
        String mm = HeartsSlotHudComposer.wrapMiniMessage(glyphs);
        assertTrue(mm.startsWith("<font:rpg:hud>"));
        assertTrue(mm.contains("</font>"));
    }
}
