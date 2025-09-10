package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatMessageDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateGroupChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.CreatePostDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.GroupChatRoomDTO;
import rs.ac.uns.ftn.informatika.jpa.pagedResults.PagedResults;
import rs.ac.uns.ftn.informatika.jpa.service.ChatService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;


   @MessageMapping("/chat/private")
    public void sendSpecific(@Payload ChatMessageDTO msg, Principal principal) {

       if(principal == null) {
           System.out.println("Principal is null");
       }

       msg.setTimestamp(LocalDateTime.now());
       msg.setSenderId(principal.getName());

       chatService.handleIncomingMessage(msg);
   }

   @MessageMapping("/chat/group")
   public void handleGroupMessage(@Payload ChatMessageDTO msg, Principal principal) {
       if(principal == null) {
           System.out.println("Principal is null");
       }

       msg.setTimestamp(LocalDateTime.now());
       msg.setSenderId(principal.getName());

       chatService.handleIncomingGroupMessage(msg);
   }

   @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
   @GetMapping(value = "/api/chat/history",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ChatMessageDTO>> getChatHistory(
            @RequestParam int user1Id,
            @RequestParam int user2Id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Page<ChatMessageDTO> chatHistory = chatService.getChatHistoryFromPrivateChat(user1Id, user2Id, page, size);

        if(chatHistory.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chatHistory);

   }

   @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping(value = "/api/chat/create-group",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupChatRoomDTO> createGroup(@Valid @RequestBody CreateGroupChatDTO request, Principal principal) {

       GroupChatRoomDTO response = chatService.createGroupChat(request, principal.getName());
       if(response == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
       }
       return ResponseEntity.ok(response);
   }

   @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping(value = "/api/chat/group-chats/my", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupChatRoomDTO>> getGroupChatsForUser(Principal principal) {
       List<GroupChatRoomDTO> response = chatService.getGroupChatRoomsForUser(principal.getName());

       if(response == null) {
           return ResponseEntity.noContent().build();
       }
       return ResponseEntity.ok(response);
   }

   @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
   @GetMapping(value = "/api/chat/group-chat/history", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Page<ChatMessageDTO>> getGroupChatHistoryForUser(
            @RequestParam int groupChatId,
            @RequestParam int page,
            @RequestParam int size,
            Principal principal
   ){
        Page<ChatMessageDTO> chatHistory = chatService.getHistoryFromGroupChatForUser(principal.getName(), groupChatId, page, size);

        if(chatHistory.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(chatHistory);
   }

   @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping(value = "/api/chat/group-chat/{groupChatId}/members",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<GroupChatRoomDTO> addMembersToGroupChat(
           @PathVariable int groupChatId,
           @RequestBody List<Integer> memberIds,
           Principal principal) {
       GroupChatRoomDTO updated = chatService.addMembersToGroupChat(principal.getName(), groupChatId, memberIds);
       return ResponseEntity.ok(updated);
   }

    @PostMapping("/api/chat/group-chat/{groupChatId}/remove-members")
    public ResponseEntity<GroupChatRoomDTO> removeMembersFromGroupChat(
            @PathVariable int groupChatId,
            @RequestBody List<Integer> memberIds,
            Principal principal) {
        GroupChatRoomDTO updated = chatService.removeMembersFromGroupChat(principal.getName(), groupChatId, memberIds);
        return ResponseEntity.ok(updated);
    }


}
