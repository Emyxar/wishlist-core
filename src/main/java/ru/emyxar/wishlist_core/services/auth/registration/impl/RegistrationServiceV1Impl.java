package ru.emyxar.wishlist_core.services.auth.registration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.emyxar.wishlist_core.entities.UserEntity;
import ru.emyxar.wishlist_core.exceptions.ResourceAlreadyExistsException;
import ru.emyxar.wishlist_core.mappers.UserMapper;
import ru.emyxar.wishlist_core.model.RegisterV1Request;
import ru.emyxar.wishlist_core.model.RegisterV1Response;
import ru.emyxar.wishlist_core.repositories.UserRepository;
import ru.emyxar.wishlist_core.services.auth.registration.RegistrationServiceV1;
import ru.emyxar.wishlist_core.utils.ValidatorUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceV1Impl implements RegistrationServiceV1 {

    private final UserRepository userRepository;

    private final ValidatorUtil validator;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterV1Response registerUser(RegisterV1Request request) {

        validator.validate(request.getEmail(), request.getFullName(), request.getPassword(), request.getConfirmPassword());

        if (userRepository.existsUserByEmail(request.getEmail())) {
            log.warn("Пользователь с email: {} уже существует", request.getEmail());
            throw new ResourceAlreadyExistsException("Пользователь с таким email уже существует");
        }

        try {
            log.info("Выполняется запись пользователя в БД из запроса: {}", request);
            UserEntity user = userMapper.mapRegRqToEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.saveAndFlush(user);

            log.info("Пользователь {} успешно зарегистрирован", user.getId());
            return userMapper.entityToUserInfoRes(user);
        } catch (Exception e) {
            log.error("Произошла ошибка при создании пользователя: {}", e.getMessage());
            throw new RuntimeException("Произошла ошибка при создании пользователя");
        }
    }
}
