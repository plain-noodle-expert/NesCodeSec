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
        String sql = "DELETE FROM Borrow WHERE borrow_id = ? AND user_id = ?";
        entityManager.createNativeQuery(sql)
                .setParameter(1, borrowId)
                .setParameter(2, userId)
                .executeUpdate();
    }
}