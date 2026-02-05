


### editable_region_start
import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private Map<String, Double> paymentAmounts;

    public PaymentProcessor() {
        paymentAmounts = new HashMap<>();
    }

    public void processPayment(String customerID, double amount) {
        paymentAmounts.put(customerID, amount);
    }

    public double getTotalPayments() {
        double total = 0;
        for (double amount : paymentAmounts.values()) {
            total += amount;
        }
        return total;
    }
}

<|editable_region_end|>
```
