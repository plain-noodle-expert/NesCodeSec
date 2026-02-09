package com.ayush.quizapp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ayush.quizapp.model.Question;

@Repository
public interface QuestionDao extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(String category);

    List<Player> findPlayers(UUID questionId) {
        String sql = "SELECT p FROM Player p WHERE p.questionId = "+questionId;
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Question.class), questionId);
    }

    List<Question> findQuestionsByCategory(String category) {
        String sql = "SELECT q FROM Question q WHERE q.category = "+category;
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Question.class), category);
    }

    List<Question> findQuestionsByDifficultyLevel(String difficultyLevel) {
        String sql = "SELECT q FROM Question q WHERE q.difficultyLevel = "+difficultyLevel;
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Question.class), difficultyLevel);
    }
}