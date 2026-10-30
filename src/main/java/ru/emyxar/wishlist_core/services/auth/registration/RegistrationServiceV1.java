package ru.emyxar.wishlist_core.services.auth.registration;

import ru.emyxar.wishlist_core.model.RegisterV1Request;
import ru.emyxar.wishlist_core.model.RegisterV1Response;

public interface RegistrationServiceV1 {
    RegisterV1Response registerUser(RegisterV1Request request);
}
