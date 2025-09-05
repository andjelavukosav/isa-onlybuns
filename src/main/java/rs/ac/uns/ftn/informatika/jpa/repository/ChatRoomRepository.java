package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN FETCH cr.members m " +
            "WHERE cr.type = 'PRIVATE' " +
            "AND EXISTS (SELECT 1 FROM ChatRoomMember cm WHERE cm.chatRoom = cr AND cm.member.id = :userId1) " +
            "AND EXISTS (SELECT 1 FROM ChatRoomMember cm WHERE cm.chatRoom = cr AND cm.member.id = :userId2)")
    Optional<ChatRoom> findPrivateRoomBetweenUsers(
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2
    );


    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.members m " +
            "WHERE cr.type = 'GROUP' AND m.member.username = :username " +
            "ORDER BY cr.creationTime DESC")
    List<ChatRoom> findGroupChatsByMemberUsername(@Param("username") String username);

}
