package com.spring.privateClinicManage.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.spring.privateClinicManage.component.OnlinerUsers;
import com.spring.privateClinicManage.dto.ChatMessageDto;
import com.spring.privateClinicManage.dto.OnlineUserDto;
import com.spring.privateClinicManage.dto.OnlineUsersOutputDto;
import com.spring.privateClinicManage.entity.ChatMessage;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.ChatMessageService;
import com.spring.privateClinicManage.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChatController {

	@Autowired
	private ChatMessageService chatMessageService;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private UserService userService;
	@Autowired
	private OnlinerUsers onlineUsers;

	@MessageMapping("/online.addOnlineUser")
	public void addUser(@Payload OnlineUserDto onlineUserDto,
			SimpMessageHeaderAccessor simpMessageHeaderAccessor) {

		String sessionId = simpMessageHeaderAccessor.getSessionId();
		User user = userService.findUserById(onlineUserDto.getUserId());
		if (user != null) {
			List<OnlineUsersOutputDto> ouoDtos = onlineUsers
					.getOnlineUsers().getOrDefault(user.getRole().getName(), new ArrayList<>());

			Boolean flag = false;

			for (int i = 0; i < ouoDtos.size(); i++) {
				OnlineUsersOutputDto userOutput = ouoDtos.get(i);
				if (userOutput.getUser().getId().equals(user.getId())) {
					ouoDtos.get(i).setSessionId(sessionId);
					flag = true;
				}
			}

			if (flag == false)
				ouoDtos.add(new OnlineUsersOutputDto(user, sessionId));

			onlineUsers.getOnlineUsers().put(user.getRole().getName(), ouoDtos);

		}

		simpMessageHeaderAccessor.getSessionAttributes().put("userId", onlineUserDto.getUserId());

		messagingTemplate.convertAndSend("/online-users", user);
	}

	@MessageMapping("/chat")
	public void processMessage(@Payload ChatMessageDto chatMessageDto) {
		
		User sender = userService.findUserById(chatMessageDto.getSenderId());
		User recipient = userService.findUserById(chatMessageDto.getRecipientId());


		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setSender(sender);
		chatMessage.setRecipient(recipient);
		chatMessage.setCreatedDate(chatMessageDto.getCreatedDate());
		chatMessage.setContent(chatMessageDto.getContent());

		chatMessage = chatMessageService.saveChatMessage(chatMessage);

		messagingTemplate.convertAndSendToUser(
				recipient.getId().toString(), "/queue/messages",
				chatMessage);
	}

}
