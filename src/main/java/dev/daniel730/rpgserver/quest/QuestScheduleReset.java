package dev.daniel730.rpgserver.quest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

public final class QuestScheduleReset {

    private QuestScheduleReset() {
    }

    public static boolean isPeriodExpired(QuestSchedule schedule, long completedAtEpochMs, ZoneId zone) {
        if (schedule == QuestSchedule.NONE) {
            return false;
        }
        ZonedDateTime completed = Instant.ofEpochMilli(completedAtEpochMs).atZone(zone);
        ZonedDateTime now = ZonedDateTime.now(zone);
        return switch (schedule) {
            case DAILY -> !completed.toLocalDate().equals(now.toLocalDate());
            case WEEKLY -> completed.get(IsoFields.WEEK_BASED_YEAR) != now.get(IsoFields.WEEK_BASED_YEAR)
                    || completed.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) != now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            case NONE -> false;
        };
    }
}
