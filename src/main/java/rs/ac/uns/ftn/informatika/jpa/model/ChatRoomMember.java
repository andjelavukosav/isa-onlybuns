package rs.ac.uns.ftn.informatika.jpa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @JsonIgnore
    private ChatRoom chatRoom;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public ChatRoomMember() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getMember() { return member; }
    public void setMember(User member) { this.member = member; }

    public ChatRoom getChatRoom() { return chatRoom; }
    public void setChatRoom(ChatRoom chatRoom) { this.chatRoom = chatRoom; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoomMember that = (ChatRoomMember) o;

        if (this.member == null || this.member.getEmail() == null ||
                this.chatRoom == null || this.chatRoom.getId() == null) {
            return false;
        }

        return this.member.getEmail().equals(that.member.getEmail()) &&
                this.chatRoom.getId().equals(that.chatRoom.getId());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (member != null && member.getEmail() != null ? member.getEmail().hashCode() : 0);
        result = 31 * result + (chatRoom != null && chatRoom.getId() != null ? chatRoom.getId().hashCode() : 0);
        return result;
    }


}

