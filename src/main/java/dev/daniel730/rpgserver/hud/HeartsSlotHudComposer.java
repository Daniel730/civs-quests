package dev.daniel730.rpgserver.hud;

/**
 * Builds ActionBar glyph strings for the hearts-slot HUD resource pack
 * ({@code font rpg:hud}): HP + mana bars shifted left onto the vacated hearts row.
 * Pure string logic — testable without Bukkit.
 */
public final class HeartsSlotHudComposer {

    /** Must match scripts/build_hide_hearts_pack.py */
    public static final char HP_FULL = '\uE010';
    public static final char HP_EMPTY = '\uE011';
    public static final char MANA_FULL = '\uE020';
    public static final char MANA_EMPTY = '\uE021';
    public static final char SEP = '\uE030';

    private HeartsSlotHudComposer() {
    }

    /**
     * @param shiftLeftPx pixels to pull left from ActionBar center (hearts are left of center)
     * @param gapPx       gap between HP and mana bars
     * @param segments    filled/empty ticks per bar (typically 10)
     */
    public static String build(int hp, int hpMax, int mana, int manaMax,
                               int segments, int shiftLeftPx, int gapPx) {
        int segs = Math.max(1, Math.min(20, segments));
        StringBuilder out = new StringBuilder(64);
        out.append(encodeSpace(-Math.max(0, shiftLeftPx)));
        out.append(bar(hp, hpMax, segs, HP_FULL, HP_EMPTY));
        if (gapPx > 0) {
            out.append(SEP);
            out.append(encodeSpace(Math.max(0, gapPx)));
        }
        out.append(bar(mana, manaMax, segs, MANA_FULL, MANA_EMPTY));
        return out.toString();
    }

    /** MiniMessage wrapper so glyphs use the pack font. */
    public static String wrapMiniMessage(String glyphs) {
        if (glyphs == null || glyphs.isEmpty()) {
            return "";
        }
        return "<font:rpg:hud><white>" + glyphs + "</white></font>";
    }

    static String bar(int current, int max, int length, char filled, char empty) {
        int safeMax = Math.max(1, max);
        int n = (int) Math.round((double) Math.max(0, current) / safeMax * length);
        n = Math.min(length, Math.max(0, n));
        return String.valueOf(filled).repeat(n) + String.valueOf(empty).repeat(length - n);
    }

    /**
     * Encode a pixel advance using AmberWat-style space glyphs (powers of two).
     * Negative = left, positive = right.
     */
    static String encodeSpace(int pixels) {
        if (pixels == 0) {
            return "";
        }
        boolean negative = pixels < 0;
        int remaining = Math.abs(pixels);
        // widths descending; chars match build script
        int[] widths = {128, 64, 32, 16, 8, 7, 6, 5, 4, 3, 2, 1};
        char[] neg = {
                '\uF80C', '\uF80B', '\uF80A', '\uF809', '\uF808',
                '\uF807', '\uF806', '\uF805', '\uF804', '\uF803', '\uF802', '\uF801'
        };
        char[] pos = {
                '\uF82C', '\uF82B', '\uF82A', '\uF829', '\uF828',
                '\uF827', '\uF826', '\uF825', '\uF824', '\uF823', '\uF822', '\uF821'
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            while (remaining >= widths[i]) {
                sb.append(negative ? neg[i] : pos[i]);
                remaining -= widths[i];
            }
        }
        return sb.toString();
    }
}
