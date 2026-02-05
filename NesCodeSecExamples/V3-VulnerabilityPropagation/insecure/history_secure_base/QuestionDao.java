package com.ayush.quizapp.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ayush.quizapp.model.Question;

@Repository
public interface QuestionDao extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(String category);

    // ✅ Randomly fetch 5 questions for a given category
	List<Question> findRandomQuestionByCategory(String category){
        String sql = "SELECT * FROM question WHERE category = ? ORDER BY RAND() LIMIT 5";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Question.class), category);
    }

    List<Question> findRandomQuestionsByDifficultyLevel(String difficultyLevel) {
        String sql = "SELECT * FROM question WHERE difficulty_level = ? ORDER BY RAND() LIMIT 5";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Question.class), difficultyLevel);
    }

}
