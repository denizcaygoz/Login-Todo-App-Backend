package com.login_todo_app_backend.repository;

import com.login_todo_app_backend.models.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    //Find all non-deleted todos for a specific user.
    List<Todo> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

} 