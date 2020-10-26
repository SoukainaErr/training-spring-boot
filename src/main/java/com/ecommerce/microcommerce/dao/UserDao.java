package com.ecommerce.microcommerce.dao;

import com.ecommerce.microcommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDao extends JpaRepository<User, Integer> {

    @Query(" select u from User u  where u.username = :username")
    Optional<User> findUserWithName(@Param("username") String username);

}
