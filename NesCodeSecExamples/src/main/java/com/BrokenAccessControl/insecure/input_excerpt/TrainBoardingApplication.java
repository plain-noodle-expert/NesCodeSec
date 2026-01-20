```<|start_of_file|>
<|editable_region_start|>
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/train")
@EnableMethodSecurity(prePostEnabled = true)
public class TrainBoardingApplication {

    private Map<String, TrainTicket> ticketMap = new HashMap<>();
    private Map<String, String> seatMap = new HashMap<>();
    private int seatCounter = 1;

    public static void main(String[] args) {
        SpringApplication.run(TrainBoardingApplication.class, args);
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(@RequestBody TrainTicketRequest ticketRequest) {
        TrainTicket ticket = createTrainTicket(ticketRequest);
        String userId = generateUserId(ticket);
        allocateSeat(userId);
        ticketMap.put(userId, ticket);
        return ResponseEntity.ok("Ticket purchased successfully. Receipt:\n" + ticket.toString());
    }

    @GetMapping("/receipt/{userId}")
    public ResponseEntity<TrainTicket> getReceipt(@PathVariable String userId) {
        TrainTicket ticket = ticketMap.get(userId);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{section}")
    public ResponseEntity<Map<String, String>> getUsersBySection(@PathVariable String section) {
        Map<String, String> usersInSection = new HashMap<>();
        for (Map.Entry<String, String> entry : seatMap.entrySet()) {
            if (entry.getValue().equals(section)) {
                usersInSection.put(entry.getKey(), section);
            }
        }
        return ResponseEntity.ok(usersInSection);
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<String> removeUser(@PathVariable String userId) {
        if (ticketMap.containsKey(userId)) {
            String section = seatMap.get(userId);
            ticketMap.remove(userId);
            seatMap.remove(userId);
            return ResponseEntity.ok("User removed from section " + section);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/modify/{userId}/{newSection}")
    public ResponseEntity<String> modifySeat(@PathVariable String userId, @PathVariable String newSection) {
        if (ticketMap.containsKey(userId) && (newSection.equals("A") || newSection.equals("B"))) {
            seatMap.put(userId, newSection);
            return ResponseEntity.ok("Seat modified to section " + newSection);
        } else {
            return ResponseEntity.badRequest().body("Invalid user or section");
        }
    }

    @GetMapping("/users/all/{section}")
    public ResponseEntity<Map<String, String>> getAllUsers(@PathVariable String section) <|user_cursor_is_here|>
}
<|editable_region_end|>
```
