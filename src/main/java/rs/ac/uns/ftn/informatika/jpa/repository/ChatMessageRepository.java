package rs.ac.uns.ftn.informatika.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.jpa.model.ChatMessage;
import rs.ac.uns.ftn.informatika.jpa.model.ChatRoom;

import java.time.LocalDateTime;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    Page<ChatMessage> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom, Pageable pageable);

    // Poruke posle joinedAt - od najstarije ka najnovijoj (za normalan tok razgovora)
    Page<ChatMessage> findByChatRoomAndTimestampGreaterThanEqualOrderByTimestampAsc(ChatRoom chatRoom, LocalDateTime joinedAt, Pageable pageable);

    // Poruke pre joinedAt - od najnovije ka starijoj (za "load more" dugme koje učitava starije poruke)
    Page<ChatMessage> findByChatRoomAndTimestampBeforeOrderByTimestampDesc(ChatRoom chatRoom, LocalDateTime joinedAt, Pageable pageable);

   Page<ChatMessage> findByChatRoomAndTimestampGreaterThanEqualOrderByTimestampDesc(ChatRoom chatRoom, LocalDateTime joinedAt, Pageable pageable);

    Page<ChatMessage> findByChatRoomAndTimestampLessThanOrderByTimestampDesc(ChatRoom chatRoom, LocalDateTime timestamp, Pageable pageable);


}
