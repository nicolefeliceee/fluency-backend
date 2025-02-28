package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.AddChatDto;
import com.skripsi.Fluency.model.dto.MessageDto;
import com.skripsi.Fluency.model.entity.Chat;
import com.skripsi.Fluency.model.entity.Message;
import com.skripsi.Fluency.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Endpoint to retrieve all chats for a given user
    @GetMapping("")
    public ResponseEntity<?> getAllChatsByUser(@RequestParam("userId") Integer userId) {
        try {
            List<Chat> listChat = chatService.getAllChatsByUser(userId);
            // Map each Chat entity to an AddChatDto.
            // This DTO includes the chat id and the names of user1 and user2.
            List<AddChatDto> chatDtos = listChat.stream()
                    .map(chat -> AddChatDto.builder()
                            .id(chat.getId())
                            .user1Name(chat.getUser1().getName())
                            .user2Name(chat.getUser2().getName())
                            .user1Id(chat.getUser1().getId())
                            .user2Id(chat.getUser2().getId())
                            .build()
                    ).collect(Collectors.toList());

            return ResponseEntity.ok(chatDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    // Endpoint to create a new chat
    @GetMapping("/add")
    public ResponseEntity<?> addChat(@RequestParam("brandId") Integer brandId,
                                     @RequestParam("influencerId") Integer influencerId) {
        try {
            Chat chat = chatService.createChat(brandId, influencerId);
            // Map the created Chat entity to an AddChatDto
            AddChatDto dto = AddChatDto.builder()
                    .id(chat.getId())
                    .user1Name(chat.getUser1().getName())
                    .user2Name(chat.getUser2().getName())
                    .build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{chatId}")
    public List<MessageDto> getMessages(@PathVariable Integer chatId) {
        List<MessageDto> messenges = chatService.getMessagesByChatId(chatId);
        System.out.print("tes apa ini: " + messenges);
        return messenges;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody MessageDto messageDTO) {
        chatService.sendMessage(messageDTO);
    }
}
