package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.ChatConversation;
import org.scitechdev.finalPorject.Entity.ChatMessageEntity;
import org.scitechdev.finalPorject.model.ChatMessage;
import org.scitechdev.finalPorject.service.ChatService;
import org.scitechdev.finalPorject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    // Helper method to get current user from session
    private String getCurrentUserId(HttpSession session) {
        return (String) session.getAttribute("userId");
    }

    private String getCurrentUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }

    // WebSocket message handling
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage) {
        try {
            // Get current user info from Security Context (for WebSocket)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = auth != null ? auth.getName() : null;
            
            if (currentUserId == null) {
                return;
            }
            
            Map<String, Object> userData = userService.getUserDetails(currentUserId);
            
            if (userData == null) {
                return;
            }

            String userRole = (String) userData.get("role");
            String userName = (String) userData.get("username");

            // Set sender information
            chatMessage.setSenderId(currentUserId);
            chatMessage.setSender(userName);
            chatMessage.setSenderRole(userRole);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setMessageId(UUID.randomUUID().toString());

            // Save message to database
            ChatMessageEntity messageEntity = new ChatMessageEntity(
                chatMessage.getConversationId(),
                currentUserId,
                userName,
                userRole,
                chatMessage.getContent()
            );
            messageEntity.setMessageType(chatMessage.getMessageType());
            messageEntity.setAttachmentUrl(chatMessage.getAttachmentUrl());

            String messageId = chatService.saveMessage(messageEntity);
            chatMessage.setMessageId(messageId);

            // Send to specific conversation topic
            messagingTemplate.convertAndSend("/topic/conversation/" + chatMessage.getConversationId(), chatMessage);

            // Send notification to recipient if they're online
            if (chatMessage.getRecipientId() != null) {
                messagingTemplate.convertAndSend("/topic/user/" + chatMessage.getRecipientId(), chatMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        return chatMessage;
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.TYPING);
        messagingTemplate.convertAndSend("/topic/conversation/" + chatMessage.getConversationId(), chatMessage);
    }

    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.STOP_TYPING);
        messagingTemplate.convertAndSend("/topic/conversation/" + chatMessage.getConversationId(), chatMessage);
    }

    // REST API endpoints
    @GetMapping("/api/chat/conversations")
    @ResponseBody
    public ResponseEntity<List<ChatConversation>> getConversations(HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            String userRole = getCurrentUserRole(session);
            
            if (currentUserId == null || userRole == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            List<ChatConversation> conversations = chatService.getConversationsByUserId(currentUserId, userRole);
            return ResponseEntity.ok(conversations);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/chat/conversations/{conversationId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageEntity>> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "50") int limit,
            HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            
            if (currentUserId == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            List<ChatMessageEntity> messages = chatService.getMessagesByConversationId(conversationId, limit);
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/api/chat/conversations")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createConversation(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String recipientId = request.get("recipientId");
            String currentUserId = getCurrentUserId(session);
            String userRole = getCurrentUserRole(session);
            
            if (currentUserId == null || userRole == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }
            
            Map<String, Object> currentUserData = userService.getUserDetails(currentUserId);
            Map<String, Object> recipientData = userService.getUserDetails(recipientId);
            
            if (currentUserData == null || recipientData == null) {
                return ResponseEntity.badRequest().build();
            }

            String currentUserName = (String) currentUserData.get("username");
            String recipientName = (String) recipientData.get("username");

            String conversationId;
            if (userRole.equals("FARMER")) {
                conversationId = chatService.createOrGetConversation(currentUserId, recipientId, currentUserName, recipientName);
            } else {
                conversationId = chatService.createOrGetConversation(recipientId, currentUserId, recipientName, currentUserName);
            }

            Map<String, String> response = new HashMap<>();
            response.put("conversationId", conversationId);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/api/chat/conversations/{conversationId}/read")
    @ResponseBody
    public ResponseEntity<Void> markConversationAsRead(@PathVariable String conversationId, HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            String userRole = getCurrentUserRole(session);
            
            if (currentUserId == null || userRole == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            chatService.markConversationAsRead(conversationId, currentUserId, userRole);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/chat/users/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String query, HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            String userRole = getCurrentUserRole(session);
            
            if (currentUserId == null || userRole == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            List<Map<String, Object>> users = userService.searchUsers(query, userRole, currentUserId);
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/chat/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getUnreadCount(HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            String userRole = getCurrentUserRole(session);
            
            if (currentUserId == null || userRole == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            int unreadCount = chatService.getUnreadMessageCount(currentUserId, userRole);
            
            Map<String, Integer> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/api/chat/conversations/{conversationId}")
    @ResponseBody
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId, HttpSession session) {
        try {
            String currentUserId = getCurrentUserId(session);
            
            if (currentUserId == null) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }
            
            chatService.deleteConversation(conversationId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
