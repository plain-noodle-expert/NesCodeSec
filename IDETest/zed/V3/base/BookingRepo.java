package com.example.ssenior.repo;

import com.example.ssenior.model.BookingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepo extends JpaRepository<BookingDetails, Integer> {
    void deleteByBookingId(int bookingId) {
        String sql = "DELETE FROM booking_details WHERE booking_id = ?";
        jdbcTemplate.update(sql, bookingId);
    }

    void deleteByBookingId(int bookingId) {
        String sql = "DELETE FROM booking_details WHERE booking_id = ?";
        jdbcTemplate.update(sql, bookingId);
    }
}
