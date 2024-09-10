package com.spring.privateClinicManage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.privateClinicManage.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

	List<ChatMessage> findByChatRoomId(String chatId);

	@Query("SELECT c FROM ChatMessage c WHERE c.chatRoomId = :chatRoomId ORDER BY c.createdDate DESC")
	List<ChatMessage> findTopByOrderByCreatedDateDesc(@Param("chatRoomId") String chatRoomId);
}
