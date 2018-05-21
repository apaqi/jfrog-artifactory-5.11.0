package org.artifactory.storage.event;

/**
 * Event types of on the nodes tree.
 *
 * @author Yossi Shaul
 */
public enum EventType {
    create(1),  // folder/file was created
    update(2),  // folder/file was updated
    delete(3),  // folder/file was deleted
    props(4);   // folder/file properties got modified (create, update or delete)

    /**
     * The event type code for persistency
     */
    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static EventType fromCode(int code) {
        switch (code) {
            case 1:
                return create;
            case 2:
                return update;
            case 3:
                return delete;
            case 4:
                return props;
            default:
                throw new IllegalArgumentException("Unknown event type code: " + code);
        }
    }
}
