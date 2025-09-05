package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.data.domain.Page;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatMessageDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateGroupChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.GroupChatMemberDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.GroupChatRoomDTO;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {
    void handleIncomingMessage(ChatMessageDTO chatMessageDTO);
    Page<ChatMessageDTO> getChatHistoryFromPrivateChat(int user1Id, int user2Id, int page, int size);
    GroupChatRoomDTO createGroupChat(CreateGroupChatDTO request, String ownerUsername);
    List<GroupChatRoomDTO> getGroupChatRoomsForUser(String username);
    void handleIncomingGroupMessage(ChatMessageDTO chatMessageDTO);
    Page<ChatMessageDTO> getHistoryFromGroupChatForUser(String username, int groupChatId, int page, int size);
    GroupChatRoomDTO addMembersToGroupChat(String ownerUsername, int groupChatId, List<Integer> memberIds);
    GroupChatRoomDTO removeMembersFromGroupChat(String ownerUsername, int groupChatId, List<Integer> memberIds);
}
