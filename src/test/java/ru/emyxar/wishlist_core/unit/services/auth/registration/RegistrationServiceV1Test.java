package ru.emyxar.wishlist_core.unit.services.auth.registration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.exceptions.ResourceAlreadyExistsException;
import ru.emyxar.wishlist_core.mappers.UserMapper;
import ru.emyxar.wishlist_core.model.RegisterV1Request;
import ru.emyxar.wishlist_core.model.RegisterV1Response;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.services.auth.registration.impl.RegistrationServiceV1Impl;
import ru.emyxar.wishlist_core.utils.ValidatorUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceV1Test {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidatorUtil validator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceV1Impl registrationService;

    private RegisterV1Request createValidRequest() {
        RegisterV1Request request = new RegisterV1Request();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        return request;
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void registerUserSuccess() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());

        String encodedPassword = "encoded-password";

        RegisterV1Response expectedResponse = new RegisterV1Response();

        doNothing().when(validator).validate(
                request.getEmail(),
                request.getFullName(),
                request.getPassword(),
                request.getConfirmPassword()
        );

        when(userRepository.existsUserByEmail(request.getEmail()))
                .thenReturn(false);

        when(userMapper.mapRegRqToEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn(encodedPassword);

        when(userMapper.entityToUserInfoRes(user))
                .thenReturn(expectedResponse);

        // Act
        RegisterV1Response actualResponse =
                registrationService.registerUser(request);

        // Assert
        assertNotNull(actualResponse);
        assertSame(expectedResponse, actualResponse);
        assertEquals(encodedPassword, user.getPassword());

        verify(validator).validate(
                request.getEmail(),
                request.getFullName(),
                request.getPassword(),
                request.getConfirmPassword()
        );

        verify(userRepository).existsUserByEmail(request.getEmail());
        verify(userMapper).mapRegRqToEntity(request);
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).entityToUserInfoRes(user);
    }

    @Test
    @DisplayName("Ошибка регистрации — пользователь с таким email уже существует")
    void registerUserEmailAlreadyExists() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        when(userRepository.existsUserByEmail(request.getEmail()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                ResourceAlreadyExistsException.class,
                () -> registrationService.registerUser(request)
        );

        verify(validator).validate(
                request.getEmail(),
                request.getFullName(),
                request.getPassword(),
                request.getConfirmPassword()
        );

        verify(userRepository).existsUserByEmail(request.getEmail());

        verifyNoInteractions(userMapper, passwordEncoder);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Ошибка регистрации — валидация завершилась исключением")
    void registerUserValidationError() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        doThrow(new IllegalArgumentException("Invalid request"))
                .when(validator).validate(
                        request.getEmail(),
                        request.getFullName(),
                        request.getPassword(),
                        request.getConfirmPassword()
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerUser(request)
        );

        verify(validator).validate(
                request.getEmail(),
                request.getFullName(),
                request.getPassword(),
                request.getConfirmPassword()
        );

        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Ошибка регистрации — ошибка преобразования запроса в сущность")
    void registerUserMapperError() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        when(userRepository.existsUserByEmail(request.getEmail()))
                .thenReturn(false);

        when(userMapper.mapRegRqToEntity(request))
                .thenThrow(new RuntimeException("Mapping failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> registrationService.registerUser(request)
        );

        assertEquals(
                "Произошла ошибка при создании пользователя",
                exception.getMessage()
        );

        verify(userMapper).mapRegRqToEntity(request);
        verify(userRepository, never()).saveAndFlush(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Ошибка регистрации — ошибка кодирования пароля")
    void registerUserPasswordEncoderError() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        UserEntity user = new UserEntity();

        when(userRepository.existsUserByEmail(request.getEmail()))
                .thenReturn(false);

        when(userMapper.mapRegRqToEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.getPassword()))
                .thenThrow(new RuntimeException("Encoding failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> registrationService.registerUser(request)
        );

        assertEquals(
                "Произошла ошибка при создании пользователя",
                exception.getMessage()
        );

        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository, never()).saveAndFlush(any());
        verify(userMapper, never()).entityToUserInfoRes(any());
    }

    @Test
    @DisplayName("Ошибка регистрации — ошибка сохранения пользователя")
    void registerUserSaveError() {
        // Arrange
        RegisterV1Request request = createValidRequest();

        UserEntity user = new UserEntity();

        when(userRepository.existsUserByEmail(request.getEmail()))
                .thenReturn(false);

        when(userMapper.mapRegRqToEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        doThrow(new RuntimeException("Database error"))
                .when(userRepository).saveAndFlush(user);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> registrationService.registerUser(request)
        );

        assertEquals(
                "Произошла ошибка при создании пользователя",
                exception.getMessage()
        );

        assertEquals("encoded-password", user.getPassword());

        verify(userRepository).saveAndFlush(user);
        verify(userMapper, never()).entityToUserInfoRes(any());
    }
}
