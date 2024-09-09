package com.spring.privateClinicManage.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.spring.privateClinicManage.component.OnlinerUsers;
import com.spring.privateClinicManage.dto.OnlineUserDto;
import com.spring.privateClinicManage.dto.OnlineUsersOutputDto;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.UserService;

@Controller
public class WebSocketEventListener {

	@Autowired
	private OnlinerUsers onlineUsers;
	@Autowired
	private UserService userService;

	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}


	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor
				.wrap(sessionDisconnectEvent.getMessage());
		String sessionId = stompHeaderAccessor.getSessionId();
		Integer userId = (Integer) stompHeaderAccessor.getSessionAttributes().get("userId");

		User currentUser = userService.findUserById(userId);
		if (currentUser != null)
			onlineUsers.findAndRemoveSessionIdByKey(currentUser.getRole().getName(), sessionId,
					currentUser.getId());

		System.out.println("Session ID " + sessionId + " đã ngắt kết nối.");
	}

	public boolean isUserActive(String userId) {
		return onlineUsers.getOnlineUsers().containsKey(userId);
	}

	@MessageMapping("/online.addOnlineUser")
	public Object addUser(@Payload OnlineUserDto onlineUserDto,
			SimpMessageHeaderAccessor simpMessageHeaderAccessor) {

		String sessionId = simpMessageHeaderAccessor.getSessionId();
		User user = userService.findUserById(onlineUserDto.getUserId());
		if (user != null) {
			List<OnlineUsersOutputDto> ouoDtos = onlineUsers
					.getOnlineUsers().getOrDefault(user.getRole().getName(), new ArrayList<>());
			ouoDtos.add(new OnlineUsersOutputDto(user, sessionId));

			onlineUsers.getOnlineUsers().put(user.getRole().getName(), ouoDtos);

		}

		String destination = "/notify/" + onlineUserDto.getUserId();

		simpMessageHeaderAccessor.getSessionAttributes().put("userId", onlineUserDto.getUserId());

		messagingTemplate.convertAndSend(destination, onlineUserDto);

		return onlineUserDto;
	}
	


}
