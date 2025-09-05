package rs.ac.uns.ftn.informatika.jpa.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GroupChatRoomDTO {
    private int id;
    private String name;
    private String ownerUsername;
    private LocalDateTime creationTime;
    private List<GroupChatMemberDTO> members;

    public GroupChatRoomDTO(int id, String name, String ownerUsername, LocalDateTime createdAt, List<GroupChatMemberDTO> members) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.creationTime = createdAt;
        this.members = members;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public LocalDateTime getCreatedAt() { return creationTime; }
    public void setCreatedAt(LocalDateTime createdAt) { this.creationTime = createdAt; }

    public List<GroupChatMemberDTO> getMembers() { return members; }
    public void setMembers(List<GroupChatMemberDTO> members) { this.members = members; }
}

