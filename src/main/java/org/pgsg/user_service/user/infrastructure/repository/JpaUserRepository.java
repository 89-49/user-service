package org.pgsg.user_service.user.infrastructure.repository;

import org.pgsg.user_service.user.domain.model.User;
import org.pgsg.user_service.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {
}
