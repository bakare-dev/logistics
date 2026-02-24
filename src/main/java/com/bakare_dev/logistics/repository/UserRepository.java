package com.bakare_dev.logistics.repository;

import com.bakare_dev.logistics.entity.Role;
import com.bakare_dev.logistics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

}
