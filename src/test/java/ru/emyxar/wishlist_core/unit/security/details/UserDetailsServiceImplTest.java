package ru.emyxar.wishlist_core.unit.security.details;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.exceptions.UnauthorizedException;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.security.details.UserDetailsServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Успешная загрузка пользователя по email")
    void loadUserByUsernameSuccessTest() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails =
                userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Пользователь не найден — UnauthorizedException")
    void loadUserByUsernameNotFoundTest() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> userDetailsService.loadUserByUsername("unknown@example.com")
        );

        assertEquals("Пользователь не найден", ex.getMessage());
    }
}
