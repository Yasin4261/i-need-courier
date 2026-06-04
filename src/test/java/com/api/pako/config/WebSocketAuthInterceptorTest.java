package com.api.pako.config;

import com.api.pako.config.WebSocketAuthInterceptor.WebSocketPrincipal;
import com.api.pako.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WebSocketAuthInterceptor underTest;

    @Test
    void connectWithValidTokenSetsPrincipal() {
        // GIVEN
        var token = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(42L);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("COURIER");

        var message = connectMessage("Bearer " + token);

        // WHEN
        var result = underTest.preSend(message, channel);

        // THEN
        var accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(accessor).isNotNull();
        assertThat(accessor.getUser())
                .isInstanceOf(WebSocketPrincipal.class)
                .extracting(java.security.Principal::getName)
                .isEqualTo("42");
        assertThat(((WebSocketPrincipal) accessor.getUser()).role()).isEqualTo("COURIER");
    }

    @Test
    void connectWithInvalidTokenThrows() {
        // GIVEN
        var token = "bad.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        var message = connectMessage("Bearer " + token);

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    @Test
    void connectWithoutAuthorizationHeaderThrows() {
        // GIVEN
        var message = connectMessage(null);

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing Authorization header");
    }

    @Test
    void connectWithNonBearerHeaderThrows() {
        // GIVEN
        var message = connectMessage("Basic dXNlcjpwYXNz");

        // WHEN & THEN
        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing Authorization header");
    }

    @Test
    void nonConnectFrameIsPassedThroughUntouched() {
        // GIVEN - a SEND frame should not trigger any JWT handling
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        accessor.setDestination("/app/something");
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // WHEN
        var result = underTest.preSend(message, channel);

        // THEN
        assertThat(result).isSameAs(message);
        verifyNoInteractions(jwtTokenProvider);
    }

    private Message<byte[]> connectMessage(String authHeader) {
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authHeader != null) {
            accessor.addNativeHeader("Authorization", authHeader);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
