package com.userservice.repository;

import com.userservice.model.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    String USER_ID_SEQUENCE = "user_id_sequence";

    User findByName(String name);
}
