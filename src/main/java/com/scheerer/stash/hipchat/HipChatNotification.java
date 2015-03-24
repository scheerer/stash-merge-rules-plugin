package com.scheerer.stash.hipchat;

public class HipChatNotification {

    public enum Color {
        YELLOW, RED, GREEN, PURPLE, GRAY, RANDOM
    }

    private String from;
    private String message;
    private String roomId;
    private Color color;

    public HipChatNotification(String message, String roomId) {
        this(message, roomId, Color.GRAY);
    }

    public HipChatNotification(String message, String roomId, Color color) {
        this(message, roomId, color, "Stash");
    }

    public HipChatNotification(String message, String roomId, Color color, String from) {
        this.message = message;
        this.roomId = roomId;
        this.color = color;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public Color getColor() {
        return color;
    }
}
