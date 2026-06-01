package com.api.pako.config;

import com.api.pako.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * STOMP CONNECT frame'inden JWT token'ı çıkarıp Principal oluşturur.
 * Bu sayede convertAndSendToUser() doğru kullanıcıya mesaj gönderebilir.
 * <p>
 * Client bağlanırken STOMP header'ına token eklemelidir:
 * CONNECT
 * Authorization: Bearer eyJhbGciOi...
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    String role = jwtTokenProvider.getRoleFromToken(token);

                    // Principal.getName() = userId → convertAndSendToUser ile eşleşir
                    Principal principal = new WebSocketPrincipal(String.valueOf(userId), role);
                    accessor.setUser(principal);

                    log.info("WebSocket CONNECT authenticated: userId={}, role={}", userId, role);
                } else {
                    log.warn("WebSocket CONNECT failed: invalid JWT token");
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            } else {
                log.warn("WebSocket CONNECT failed: missing Authorization header");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }

    /**
     * Simple Principal implementation that carries userId and role.
     */
    public record WebSocketPrincipal(String name, String role) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}

