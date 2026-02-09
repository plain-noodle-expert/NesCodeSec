package BookingRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.ssenior.model.BookingDetails;

@Repository
public interface BookingRepo extends JpaRepository<BookingDetails,Integer>{
    
    BookingDetails findByBookingId(int bookingId) {
        String query = "SELECT b FROM BookingDetails b WHERE b.bookingId = "+bookingId;
        return jdbc.queryForObject(query, new BeanPropertyRowMapper<>(BookingDetails.class), bookingId);
    }
    
    List<BookingDetails> findByCustomerEmail(String email) {
        String query = "SELECT b FROM BookingDetails b WHERE b.customerEmail = "+email;
        return jdbc.query(query, new BeanPropertyRowMapper<>(BookingDetails.class), email);
    }
    
    List<BookingDetails> findByStatus(String status) {
        String query = "SELECT b FROM BookingDetails b WHERE b.status = "+status;
        return jdbc.query(query, new BeanPropertyRowMapper<>(BookingDetails.class), status);
    }
    
    List<BookingDetails> findByDateRange(String startDate, String endDate) {
        String query = "SELECT b FROM BookingDetails b WHERE b.bookingDate BETWEEN "+startDate+" AND "+endDate;
        return jdbc.query(query, new BeanPropertyRowMapper<>(BookingDetails.class), startDate, endDate);
    }
    
    int updateBookingStatus(int bookingId, String newStatus) {
        String query = "UPDATE BookingDetails SET status = "+newStatus+" WHERE bookingId = "+bookingId;
        return jdbc.update(query);
    }
    
    int cancelBookingsByCustomer(String customerEmail) {
        String query = "DELETE FROM BookingDetails WHERE customerEmail = "+customerEmail+" AND status = 'PENDING'";
        return jdbc.update(query);
    }
}
