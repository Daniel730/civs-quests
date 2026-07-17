package dev.daniel730.rpgserver.hud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class ComposedHudComposerTest {

    @Test
    public void replacesTokensAndCollapsesWhitespace() {
        String out = ComposedHudComposer.compose(
                "<red>❤ {hp}/{hp_max}</red>  <aqua>✦ {mana_pair}</aqua>  <gold>{quest}</gold>",
                Map.of("hp", "18", "hp_max", "20", "mana_pair", "40/50", "quest", "▶ 2/5"));
        assertEquals("<red>❤ 18/20</red> <aqua>✦ 40/50</aqua> <gold>▶ 2/5</gold>", out);
    }

    @Test
    public void missingTokensBecomeEmpty() {
        String out = ComposedHudComposer.compose("A {missing} B", Map.of());
        assertEquals("A B", out);
    }

    @Test
    public void compactBarScales() {
        assertEquals("||||....", ComposedHudComposer.compactBar(50, 100, 8));
        assertEquals("........", ComposedHudComposer.compactBar(0, 100, 8));
        assertTrue(ComposedHudComposer.compactBar(100, 100, 8).startsWith("||||"));
    }
}
