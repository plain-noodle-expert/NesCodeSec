package com.ayush.quizapp.dao;

import com.ayush.quizapp.model.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionDao extends JpaRepository<Question, Integer> {
    List<Question> findByCategory(String category);

    List<Player> findPlayers(UUID questionId) {
        String query =
            "SELECT p FROM Player p JOIN p.questions q WHERE q.id = :questionId";
        return entityManager
            .createQuery(query, Player.class)
            .setParameter("questionId", questionId)
            .getResultList();
    }
}
