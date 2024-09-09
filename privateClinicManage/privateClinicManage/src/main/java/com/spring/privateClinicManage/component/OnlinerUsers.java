package com.spring.privateClinicManage.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.spring.privateClinicManage.dto.OnlineUsersOutputDto;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class OnlinerUsers {
	private Map<String, List<OnlineUsersOutputDto>> onlineUsers = new HashMap<>();

	public OnlinerUsers() {
		this.onlineUsers = new HashMap<>();
	}

	public void findAndRemoveSessionIdByKey(String key, String sessionId, Integer userId) {
		List<OnlineUsersOutputDto> usersList = onlineUsers.get(key);
		if (usersList != null) {
			for (int i = 0; i < usersList.size(); i++) {
				OnlineUsersOutputDto userOutput = usersList.get(i);
				if (userOutput.getSessionId().equals(sessionId)
						|| userOutput.getUser().getId().equals(userId)) {
					usersList.remove(i);
					this.getOnlineUsers().put(key, usersList);
				}
			}
		}
	}

}
