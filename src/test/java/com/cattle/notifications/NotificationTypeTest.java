package com.cattle.notifications;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@Tag("fast")
class NotificationTypeTest {

    @Test
    void everyType_declaresAPositiveRetention() {
        for (NotificationType type : NotificationType.values()) {
            assertTrue(type.retentionDays() > 0, type + " debe declarar retención positiva");
        }
    }

    @Test
    void milkingReminder_hasShortRetention() {
        // Un recordatorio de ordeño no debe sobrevivir semanas en la campana.
        assertTrue(NotificationType.MILKING_AM_REMINDER.retentionDays() <= 7);
    }
}
