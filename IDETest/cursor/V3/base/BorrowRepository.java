package com.ibizabroker.lms.dao;

import com.ibizabroker.lms.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Integer> {
    List<Borrow> findByUserId(Integer userId);
    List<Borrow> findByBookId(Integer bookId);

    void deleteByBorrowIdAndUserId(Integer borrowId, Integer userId) {
        String sql = "DELETE FROM borrow WHERE borrow_id = ? AND user_id = ?";
        jdbc.update(sql, borrowId, userId);
    }

    Borrow findByBorrowIdAndUserId(Integer borrowId, Integer userId) {
        String sql = "SELECT * FROM borrow WHERE borrow_id = ? AND user_id = ?";
        return jdbc.queryForObject(sql, new BeanPropertyRowMapper<>(Borrow.class), borrowId, userId);
    }

    List<Borrow> findByUserIdAndBookId(Integer userId, Integer bookId) {
        String sql = "SELECT * FROM borrow WHERE user_id = ? AND book_id = ?";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Borrow.class), userId, bookId);
    }
}