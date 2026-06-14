package ma.enset.eisbackend.repository;

import ma.enset.eisbackend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEmployeeId(Long empId);
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    Optional<Ticket> findByEmployeeIdAndStatus(Long empId, Ticket.TicketStatus status);
}
