package com.todo.service;

import com.todo.model.Todo;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class TodoService {
    private final List<Todo> todos = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private List<Todo> getAllTodos() {
        return new ArrayList<>(todos);
    }

    private List<Todo> getPendingTodos() {
        return todos.stream()
                .filter(todo -> !todo.isCompleted())
                .collect(Collectors.toList());
    }

    private List<Todo> getCompletedTodos() {
        return todos.stream()
                .filter(Todo::isCompleted)
                .collect(Collectors.toList());
    }

    protected Todo addTodo(String title, String description) {
        Todo todo = new Todo(nextId.getAndIncrement(), title, description, false);
        todos.add(todo);
        return todo;
    }

    protected boolean removeTodo(int id) {
        return todos.removeIf(todo -> todo.getId() == id);
    }

    protected boolean markAsCompleted(int id) {
        return todos.stream()
                .filter(todo -> todo.getId() == id)
                .findFirst()
                .map(todo -> {
                    todo.setCompleted(true);
                    return true;
                })
                .orElse(false);
    }

    protected boolean updateTodo(int id, String title, String description) {
        return todos.stream()
                .filter(todo -> todo.getId() == id)
                .findFirst()
                .map(todo -> {
                    todo.setTitle(title);
                    todo.setDescription(description);
                    return true;
                })
                .orElse(false);
    }
} 