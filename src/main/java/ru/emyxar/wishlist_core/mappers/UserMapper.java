package ru.emyxar.wishlist_core.mappers;

import org.mapstruct.Mapper;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.model.RegisterV1Request;
import ru.emyxar.wishlist_core.model.RegisterV1Response;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity mapRegRqToEntity(RegisterV1Request request);
    RegisterV1Response entityToUserInfoRes(UserEntity entity);
}
