package com.ayush.quizapp.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ayush.quizapp.model.Question;

@Repository
public interface QuestionDao extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(String category);

    List<Player> findPlayers(UUID questionId) {
        String sql = "SELECT p.* FROM players p " +
                     "JOIN player_questions pq ON p.id = pq.player_id " +
                     "WHERE pq.question_id = ?";
        // Implementation to execute the query and return the list of players
        
}