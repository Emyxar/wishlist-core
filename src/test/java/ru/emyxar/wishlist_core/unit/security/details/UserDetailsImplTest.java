package ru.emyxar.wishlist_core.unit.security.details;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.security.details.UserDetailsImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    @DisplayName("Корректное маппирование полей из UserEntity")
    void mapFieldsFromUserEntityTest() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        // Act
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Assert
        assertEquals(userId, userDetails.getUserId());
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }
}
