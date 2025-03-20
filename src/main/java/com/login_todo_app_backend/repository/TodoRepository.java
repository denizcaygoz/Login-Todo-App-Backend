package com.login_todo_app_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.login_todo_app_backend.models.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    //Fetch only task and isCompleted for a given user ID
    List<TodoProjection> findByUserId(Long userId);

    //Delete all todos for a given user ID
    @Modifying
    @Query("DELETE FROM Todo t WHERE t.user.id = ?1")
    void deleteByUserId(Long userId);
}

