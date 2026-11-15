package ru.emyxar.wishlist_core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.emyxar.wishlist_core.entities.RevokedTokenEntity;

import java.util.UUID;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedTokenEntity, UUID> {
    boolean existsByJti(String jti);
}
