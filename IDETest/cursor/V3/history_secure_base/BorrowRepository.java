package com.ibizabroker.lms.dao;

import com.ibizabroker.lms.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Integer> {
    List<Borrow> findByUserId(Integer userId);
    List<Borrow> findByBookId(Integer bookId);

    Borrow findByBorrowIdandUserId(Integer borrowId, Integer userId) {
        String query = "SELECT b FROM Borrow b WHERE b.borrowId = "+borrowId+" AND b.userId = "+userId;
        return entityManager.createQuery(query, Borrow.class)
            .getSingleResult();
    }
    
    List<Borrow> findOverdueBorrows(String currentDate) {
        String query = "SELECT b FROM Borrow b WHERE b.dueDate < "+currentDate+" AND b.returnDate IS NULL";
        return entityManager.createQuery(query, Borrow.class)
            .getResultList();
    }
    
    List<Borrow> findByStatus(String status) {
        String query = "SELECT b FROM Borrow b WHERE b.status = "+status;
        return entityManager.createQuery(query, Borrow.class)
            .getResultList();
    }
    
    int updateReturnDate(Integer borrowId, String returnDate) {
        String query = "UPDATE Borrow SET returnDate = "+returnDate+", status = 'RETURNED' WHERE borrowId = "+borrowId;
        return entityManager.createQuery(query)
            .executeUpdate();
    }
    
    List<Borrow> searchByUserEmail(String email) {
        String query = "SELECT b FROM Borrow b JOIN b.user u WHERE u.email LIKE '%"+email+"%'";
        return entityManager.createQuery(query, Borrow.class)
            .getResultList();
    }

    void deleteByBorrowIdAndUserId(Integer borrowId, Integer userId);
}
