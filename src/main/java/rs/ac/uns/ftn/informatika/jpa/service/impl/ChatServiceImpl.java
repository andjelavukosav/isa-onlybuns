package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.CreateGroupChatDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.GroupChatMemberDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.GroupChatRoomDTO;
import rs.ac.uns.ftn.informatika.jpa.manager.ChatQueueManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.ChatMessageDTO;
import rs.ac.uns.ftn.informatika.jpa.model.*;
import rs.ac.uns.ftn.informatika.jpa.repository.ChatMessageRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.ChatRoomRepository;
import rs.ac.uns.ftn.informatika.jpa.service.ChatService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ChatQueueManager chatQueueManager;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Transactional
    public void handleIncomingMessage(ChatMessageDTO chatMessageDTO) {
        User sender = userService.findByUsername(chatMessageDTO.getSenderId());
        User recipient = userService.findByUsername(chatMessageDTO.getRecipientId());

        ChatRoom chatRoom = findOrCreatePrivateRoomBetweenUsers(sender, recipient);

        //Sacuvaj poruku u bazi
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(chatMessageDTO.getMessage());
        chatMessage.setTimestamp(chatMessageDTO.getTimestamp());

        sender.addSentMessage(chatMessage);
        chatRoom.addMessage(chatMessage);

        chatMessageRepository.save(chatMessage);

        //Kada se poruka sacuva posalji u korisnikov queue
        chatQueueManager.createQueueForUser(sender.getUsername());
        chatQueueManager.createQueueForUser(recipient.getUsername());

        rabbitTemplate.convertAndSend("chat.exchange", "chat." + sender.getUsername(), chatMessageDTO);
        rabbitTemplate.convertAndSend("chat.exchange", "chat." + recipient.getUsername(), chatMessageDTO);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getChatHistoryFromPrivateChat(int user1Id, int user2Id, int page, int size) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findPrivateRoomBetweenUsers(user1Id, user2Id);
        if (!chatRoom.isPresent()) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByTimestampDesc(chatRoom.get(), pageable);

        Set<ChatRoomMember> chatMembers = chatRoom.get().getMembers();
        //Mapa clanova
        Map<Integer, String> memberIdToUsername = chatMembers.stream()
                .collect(Collectors.toMap(
                        m -> m.getMember().getId(),
                        m -> m.getMember().getUsername()
                ));

        return messages.map(msg -> {
            int senderId = msg.getSender().getId();
            String senderUsername = msg.getSender().getUsername();

            String recipientUsername = memberIdToUsername.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(senderId))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse("Unknown");

            return new ChatMessageDTO(
                    senderUsername,
                    recipientUsername,
                    msg.getMessage(),
                    msg.getChatRoom().getId(),
                    msg.getTimestamp()
            );

            }
        );
    }

    @Transactional
    public GroupChatRoomDTO createGroupChat(CreateGroupChatDTO request, String ownerUsername){
        User owner = userService.findByUsername(ownerUsername);

        ChatRoom groupChat = new ChatRoom();
        groupChat.setName(request.getName());
        groupChat.setType(ChatRoomType.GROUP);
        owner.setOwnedRoom(groupChat);
        groupChat.setCreationTime(LocalDateTime.now());

        //koliko ima memberIds toliko pravimo membera
        List<User> members = userService.findAllByIds(request.getMemberIds());
        List<GroupChatMemberDTO> memberDTOs = new ArrayList<>();

        for (User member: members) {
            memberDTOs.add(addMemberToGroup(member, groupChat));
        }
        //dodaj i vlasnika
        memberDTOs.add(addMemberToGroup(owner, groupChat));

        GroupChatRoomDTO newGroupChatDTO = new GroupChatRoomDTO(groupChat.getId(),
                groupChat.getName(),
                owner.getUsername(),
                groupChat.getCreationTime(),
                memberDTOs);

        memberDTOs.forEach(member -> { messagingTemplate.convertAndSend("/topic/group-chat-added/" + member.getUsername(), newGroupChatDTO);});

        return newGroupChatDTO;

    }

    @Transactional(readOnly = true)
    public List<GroupChatRoomDTO> getGroupChatRoomsForUser(String username) {
        List<ChatRoom> groupChatsForUser = chatRoomRepository.findGroupChatsByMemberUsername(username);

        return groupChatsForUser.stream().map(groupChat -> {
            List<GroupChatMemberDTO> memberDTOS = groupChat.getMembers().stream()
                    .map(ChatRoomMember::getMember)
                    .map(user -> new GroupChatMemberDTO(user.getId(), user.getUsername()))
                    .collect(Collectors.toList());

            String ownerUsername = groupChat.getOwner().getUsername();

            return new GroupChatRoomDTO(
                    groupChat.getId(),
                    groupChat.getName(),
                    ownerUsername,
                    groupChat.getCreationTime(),
                    memberDTOS
            );

        }).collect(Collectors.toList());
    }

    @Transactional
    public void handleIncomingGroupMessage(ChatMessageDTO chatMessageDTO) {
        User sender = userService.findByUsername(chatMessageDTO.getSenderId());
        ChatRoom groupChat = chatRoomRepository.findById(chatMessageDTO.getChatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("GroupChat not found"));

        saveMessageInGroupChat(chatMessageDTO, sender, groupChat);

        notifyGroupMembers(chatMessageDTO, groupChat);

    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getHistoryFromGroupChatForUser(String username, int groupChatId, int page, int size) {
        User user = userService.findByUsername(username);
        ChatRoom groupChat = chatRoomRepository.findById(groupChatId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        ChatRoomMember membership = groupChat.getMembers().stream()
                .filter(m -> m.getMember().equals(user))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User is not a member of this group"));

        // Dohvati 10 poruka pre pridruživanja
        Pageable lastMessagesBeforeJoinPage = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessage> lastMessagesBeforeJoin = chatMessageRepository
                .findByChatRoomAndTimestampLessThanOrderByTimestampDesc(groupChat, membership.getJoinedAt(), lastMessagesBeforeJoinPage);

        LocalDateTime boundaryTimestamp = membership.getJoinedAt();

        if (!lastMessagesBeforeJoin.isEmpty()) {
            ChatMessage oldestMessage = lastMessagesBeforeJoin.getContent()
                    .stream()
                    .min(Comparator.comparing(ChatMessage::getTimestamp))
                    .orElse(null);

            if (oldestMessage != null) {
                boundaryTimestamp = oldestMessage.getTimestamp();
            }
        }

        // Dohvati poruke od boundaryTimestamp na dalje
        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomAndTimestampGreaterThanEqualOrderByTimestampDesc(groupChat, boundaryTimestamp, pageable);

        return messages.map(msg -> mapToChatMessageDTO(msg, user.getUsername()));
    }

    @Transactional
    public GroupChatRoomDTO addMembersToGroupChat(String ownerUsername, int groupChatId, List<Integer> memberIds) {
        User owner = userService.findByUsername(ownerUsername);
        ChatRoom groupChat = chatRoomRepository.findById(groupChatId)
                .orElseThrow(() -> new EntityNotFoundException("Group chat not found."));


        if (!groupChat.getOwner().equals(owner)) {
            throw new AccessDeniedException("Only the group owner can add members.");
        }

        // Pretvori postojeće članove grupe u skup ID-eva
        Set<Integer> existingMemberIds = groupChat.getMembers().stream()
                .map(m -> m.getMember().getId())
                .collect(Collectors.toSet());

        // Filtriraj članove koje treba dodati (koji još nisu u grupi)
        List<User> membersToAdd = userService.findAllByIds(memberIds).stream()
                .filter(user -> !existingMemberIds.contains(user.getId()))
                .collect(Collectors.toList());


        for(User member: membersToAdd) {
            ChatRoomMember memberEntity = new ChatRoomMember();
            memberEntity.setJoinedAt(LocalDateTime.now());

            // Dodavanje u kolekcije sa obe strane
            member.setRoomMembership(memberEntity);
            groupChat.addMember(memberEntity);

        }

        GroupChatRoomDTO updatedGroupChat = mapToGroupChatRoomDTO(groupChat);

        messagingTemplate.convertAndSend("/topic/group-chat-members-updates/" + groupChatId, updatedGroupChat);

        return updatedGroupChat;

    }

    @Transactional
    public GroupChatRoomDTO removeMembersFromGroupChat(String ownerUsername, int groupChatId, List<Integer> memberIds) {
        User owner = userService.findByUsername(ownerUsername);
        ChatRoom groupChat = chatRoomRepository.findById(groupChatId)
                .orElseThrow(() -> new EntityNotFoundException("Group chat not found."));

        // Provjeri da li je trenutni korisnik admin grupe
        if (!groupChat.getOwner().equals(owner)) {
            throw new AccessDeniedException("Only the group owner can remove members.");
        }


        List<ChatRoomMember> membersToRemove = groupChat.getMembers().stream()
                .filter(member -> memberIds.contains(member.getMember().getId()))
                .filter(member -> !member.getMember().equals(owner)) // sprjecava da owner bude uklonjen
                .collect(Collectors.toList());

        for (ChatRoomMember member : membersToRemove) {
            groupChat.removeMember(member);
        }

        GroupChatRoomDTO updatedGroupChat = mapToGroupChatRoomDTO(groupChat);

        messagingTemplate.convertAndSend("/topic/group-chat-members-updates/" + groupChatId, updatedGroupChat);

        return updatedGroupChat;
    }

    private ChatRoom findOrCreatePrivateRoomBetweenUsers(User sender, User recipient) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findPrivateRoomBetweenUsers(sender.getId(), recipient.getId());

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        //kreiraj chat izmedju njih
        ChatRoom newRoom = new ChatRoom();
        newRoom.setType(ChatRoomType.PRIVATE);
        newRoom.setName("Private: " + sender.getUsername() + " & " + recipient.getUsername());
        newRoom.setOwner(null);
        newRoom.setCreationTime(LocalDateTime.now());

        ChatRoomMember member1 = new ChatRoomMember();
       // member1.setMember(sender);
        sender.setRoomMembership(member1);
        newRoom.addMember(member1);

        ChatRoomMember member2 = new ChatRoomMember();
        //member2.setMember(recipient);
        recipient.setRoomMembership(member2);
        newRoom.addMember(member2);

        return chatRoomRepository.save(newRoom);
    }

    private GroupChatMemberDTO addMemberToGroup(User user, ChatRoom groupChat) {
        ChatRoomMember membership = new ChatRoomMember();
        membership.setJoinedAt(LocalDateTime.now());

        user.setRoomMembership(membership);
        groupChat.addMember(membership);

        return new GroupChatMemberDTO(user.getId(), user.getUsername());
    }

    private void saveMessageInGroupChat(ChatMessageDTO chatMessageDTO, User sender, ChatRoom groupChat) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(chatMessageDTO.getMessage());
        chatMessage.setTimestamp(chatMessageDTO.getTimestamp());
        sender.addSentMessage(chatMessage);
        groupChat.addMessage(chatMessage);

        chatMessageRepository.save(chatMessage);
    }

    private void notifyGroupMembers(ChatMessageDTO chatMessageDTO, ChatRoom groupChat) {
        groupChat.getMembers().stream()
                .map(ChatRoomMember::getMember)
                .forEach(member -> {
                    String username = member.getUsername();
                    chatQueueManager.createQueueForUser(username);
                    rabbitTemplate.convertAndSend("chat.exchange", "chat." + username, chatMessageDTO);
                });
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage msg, String currentUsername) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSenderId(msg.getSender().getUsername());
        dto.setMessage(msg.getMessage());
        dto.setTimestamp(msg.getTimestamp());
        dto.setChatRoomId(msg.getChatRoom().getId());

        if (!msg.getSender().getUsername().equals(currentUsername)) {
            dto.setRecipientId(currentUsername);
        }
        return dto;
    }

    private GroupChatRoomDTO mapToGroupChatRoomDTO(ChatRoom groupChat) {
        String ownerUsername = groupChat.getOwner().getUsername();

        List<GroupChatMemberDTO> memberDTOS = groupChat.getMembers().stream()
                .map(m -> {
                    User member = m.getMember();
                    return new GroupChatMemberDTO(member.getId(), member.getUsername());
                }).collect(Collectors.toList());

        return new GroupChatRoomDTO(
                groupChat.getId(),
                groupChat.getName(),
                ownerUsername,
                groupChat.getCreationTime(),
                memberDTOS
        );
    }
}
