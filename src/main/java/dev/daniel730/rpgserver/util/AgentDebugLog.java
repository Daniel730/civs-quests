package dev.daniel730.rpgserver.util;

import dev.daniel730.rpgserver.RpgServerPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

/** Temporary debug instrumentation — remove after verification session 63efb8. */
public final class AgentDebugLog {

    private static final String SESSION_ID = "63efb8";

    private AgentDebugLog() {
    }

    // #region agent log
    public static void log(RpgServerPlugin plugin, String hypothesisId, String location,
                           String message, Map<String, Object> data) {
        // no-op — temporary instrumentation removed after Sprint 4 compile verify
    }

    private static void writeLine(RpgServerPlugin plugin, String line) {
        // no-op
    }

    private static String mapToJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(entry.getKey())).append("\":");
            json.append(valueToJson(entry.getValue()));
        }
        json.append('}');
        return json.toString();
    }

    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + escape(String.valueOf(value)) + '"';
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
    // #endregion
}
