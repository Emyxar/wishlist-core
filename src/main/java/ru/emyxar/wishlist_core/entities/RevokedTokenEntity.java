package ru.emyxar.wishlist_core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "revoked_tokens")
public class RevokedTokenEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "jti")
    private String jti;

    @Column(name = "expired_timestamp")
    private Long expiredTimestamp;
}
