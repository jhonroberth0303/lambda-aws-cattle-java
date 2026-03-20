package com.cattle.enums.profiles;

/**
 * Indicates the source of a derived field value.
 * AUTO: Value was automatically calculated by the system (batch/rules).
 * MANUAL: Value was explicitly set by a user decision.
 */
public enum Source {
    AUTO,
    MANUAL
}
