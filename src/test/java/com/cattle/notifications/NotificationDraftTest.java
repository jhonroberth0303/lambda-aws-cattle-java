package com.cattle.notifications;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@Tag("fast")
class NotificationDraftTest {

    private static NotificationDraft.NotificationDraftBuilder validBuilder() {
        return NotificationDraft.builder()
                .farmId("001")
                .type(NotificationType.MILKING_AM_REMINDER)
                .title("Título")
                .body("Cuerpo")
                .deeplink("/lactancia")
                .dedupeKey("MILKING_AM_REMINDER#001#2026-09-09");
    }

    @Test
    void build_withAllRequiredFields_succeeds() {
        NotificationDraft draft = validBuilder().dataJson("{\"a\":1}").build();

        assertAll(
                () -> assertEquals("001", draft.farmId()),
                () -> assertEquals(NotificationType.MILKING_AM_REMINDER, draft.type()),
                () -> assertEquals("{\"a\":1}", draft.dataJson())
        );
    }

    @Test
    void build_withNullDataJson_defaultsToEmptyObject() {
        assertEquals("{}", validBuilder().dataJson(null).build().dataJson());
    }

    @Test
    void build_withBlankDataJson_defaultsToEmptyObject() {
        assertEquals("{}", validBuilder().dataJson("   ").build().dataJson());
    }

    @Test
    void build_withBlankRequiredText_throws() {
        assertThrows(IllegalArgumentException.class, () -> validBuilder().farmId(" ").build());
        assertThrows(IllegalArgumentException.class, () -> validBuilder().title(null).build());
        assertThrows(IllegalArgumentException.class, () -> validBuilder().dedupeKey("").build());
    }

    @Test
    void build_withNullType_throws() {
        assertThrows(NullPointerException.class, () -> validBuilder().type(null).build());
    }
}
