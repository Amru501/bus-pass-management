// Assuming your UserRepository is similar to this:
package com.example.buspassmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.buspassmanagement.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // findByEmail() should already be here
    java.util.Optional<User> findByEmail(String email);
    
    // findAll() is inherited from JpaRepository, so no change needed here.
}