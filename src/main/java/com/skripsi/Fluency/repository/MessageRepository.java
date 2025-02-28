package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChat_IdOrderByDateTimeAsc(Integer chatId); // ✅ Use Chat_Id instead of ChatId
}

