package ru.emyxar.wishlist_core.unit.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.mappers.UserMapper;
import ru.emyxar.wishlist_core.model.RegisterV1Request;
import ru.emyxar.wishlist_core.model.RegisterV1Response;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Успешное преобразование RegisterV1Request в UserEntity")
    void mapRegRqToEntitySuccess() {
        // Arrange
        RegisterV1Request request = new RegisterV1Request();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        // Act
        UserEntity entity = userMapper.mapRegRqToEntity(request);

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getId());

        assertEquals(request.getEmail(), entity.getEmail());
        assertEquals(request.getFullName(), entity.getFullName());
        assertEquals(request.getPassword(), entity.getPassword());

        assertNull(entity.getUrlAvatar());
    }

    @Test
    @DisplayName("Успешное преобразование UserEntity в RegisterV1Response")
    void entityToUserInfoResSuccess() {
        // Arrange
        UUID userId = UUID.randomUUID();

        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setEmail("test@example.com");
        entity.setFullName("Test User");
        entity.setPassword("password123");
        entity.setUrlAvatar("https://example.com/avatar.png");

        // Act
        RegisterV1Response response =
                userMapper.entityToUserInfoRes(entity);

        // Assert
        assertNotNull(response);

        assertEquals(userId.toString(), response.getId());
    }
}
