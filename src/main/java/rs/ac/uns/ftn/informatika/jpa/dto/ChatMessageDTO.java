package rs.ac.uns.ftn.informatika.jpa.dto;

import rs.ac.uns.ftn.informatika.jpa.model.ChatMessage;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private String senderId;
    private String recipientId;
    private String message;
    private LocalDateTime timestamp;
    private int chatRoomId;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String senderId, String recipientId, String message, int chatRoomId, LocalDateTime timestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.chatRoomId = chatRoomId;
        this.timestamp = timestamp;
    }

    public ChatMessageDTO(String senderId, String message, int chatRoomId, LocalDateTime timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.chatRoomId = chatRoomId;
        this.timestamp = timestamp;
    }


    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(int chatRoomId) { this.chatRoomId = chatRoomId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
