package com.sakh.telegrambot.repository;

import com.sakh.telegrambot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//long chatId as primary key
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
