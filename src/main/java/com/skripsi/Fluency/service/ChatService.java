package com.skripsi.Fluency.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.skripsi.Fluency.model.dto.MessageDto;
import com.skripsi.Fluency.model.entity.Chat;
import com.skripsi.Fluency.model.entity.Message;
import com.skripsi.Fluency.model.entity.User;
import com.skripsi.Fluency.repository.ChatRepository;
import com.skripsi.Fluency.repository.MessageRepository;
import com.skripsi.Fluency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SocketIOServer socketServer;

    public Chat createChat(Integer brandId, Integer influencerId) {
        Optional<User> brand = userRepository.findById(brandId);
        Optional<User> influencer = userRepository.findById(influencerId);

        if (!brand.isPresent()) {
            throw new RuntimeException("Brand dengan ID " + brandId + " tidak ditemukan");
        }
        if (!influencer.isPresent()) {
            throw new RuntimeException("Influencer dengan ID " + influencerId + " tidak ditemukan");
        }

        Optional<Chat> existingChat = chatRepository.findByUser1AndUser2(brand.get(), influencer.get());
        if (existingChat.isPresent()) {
            throw new RuntimeException("Chat antara Brand dengan ID " + brandId
                    + " dan Influencer dengan ID " + influencerId + " sudah ada");
        }

        Chat chat = new Chat();
        chat.setUser1(brand.get());
        chat.setUser2(influencer.get());

        return chatRepository.save(chat);
    }

    public List<Chat> getAllChatsByUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User dengan ID " + userId + " tidak ditemukan");
        }
        User user = userOpt.get();
        List<Chat> chatList = chatRepository.findByUser1OrUser2(user, user);
        return chatList;
    }

    public Optional<User> getChatUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User dengan ID " + userId + " tidak ditemukan");
        }
        return userOpt;
    }

    public List<MessageDto> getMessagesByChatId(Integer chatId) {
        List<Message> messages = messageRepository.findByChat_IdOrderByDateTimeAsc(chatId);

        return messages.stream().map(message -> MessageDto.builder()
                .id(message.getId())
                .senderId(message.getUser().getId())
                .chatId(message.getChat().getId())
                .text(message.getText())
                .dateTime(message.getDateTime())
                .messageType(message.getMessageType())
                .url(message.getUrl())
                .build()).collect(Collectors.toList());
    }

    public void sendMessage(MessageDto messageDTO) {
        Chat chat = chatRepository.findById(messageDTO.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found with ID: " + messageDTO.getChatId()));

        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + messageDTO.getSenderId()));

        Message message = new Message();
        message.setChat(chat);
        message.setUser(sender);
        message.setText(messageDTO.getText());
        message.setMessageType(messageDTO.getMessageType());
        message.setDateTime(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        socketServer.getBroadcastOperations().sendEvent("newMessage");
    }
}
