package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    Optional<User>findByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.birthDate < :date")
    List<String> getEmailsFromAdultUsers(@Param("date") LocalDate date);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO participants_events(event_id, user_id)
            VALUES (:eventId, :userId);"""
            , nativeQuery = true)
    void insertNewUserToFeedbackEventManagementEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);
}
