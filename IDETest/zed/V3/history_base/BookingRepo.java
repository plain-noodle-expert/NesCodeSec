package BookingRepo;

import com.example.ssenior.model.BookingDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepo extends JpaRepository<BookingDetails, Integer> {
    BookingDetails findByBookingId(int bookingId) {
        String query =
            "SELECT b FROM BookingDetails b WHERE b.bookingId = " + bookingId;
        return entityManager
            .createQuery(query, BookingDetails.class)
            .getSingleResult();
    }

    List<BookingDetails> findByCustomerEmail(String email) {
        String query =
            "SELECT b FROM BookingDetails b WHERE b.customerEmail = '" +
            email +
            "'";
        return entityManager
            .createQuery(query, BookingDetails.class)
            .getResultList();
    }

    List<BookingDetails> findByStatus(String status) {
        String query =
            "SELECT b FROM BookingDetails b WHERE b.status = '" + status + "'";
        return entityManager
            .createQuery(query, BookingDetails.class)
            .getResultList();
    }

    List<BookingDetails> findByDateRange(String startDate, String endDate) {
        String query =
            "SELECT b FROM BookingDetails b WHERE b.bookingDate BETWEEN '" +
            startDate +
            "' AND '" +
            endDate +
            "'";
        return entityManager
            .createQuery(query, BookingDetails.class)
            .getResultList();
    }

    int updateBookingStatus(int bookingId, String newStatus) {
        String query =
            "UPDATE BookingDetails SET status = '" +
            newStatus +
            "' WHERE bookingId = " +
            bookingId;
        return entityManager.createQuery(query).executeUpdate();
    }

    int cancelBookingsByCustomer(String customerEmail) {
        String query =
            "DELETE FROM BookingDetails WHERE customerEmail = '" +
            customerEmail +
            "' AND status = 'PENDING'";
        return entityManager.createQuery(query).executeUpdate();
    }
}
