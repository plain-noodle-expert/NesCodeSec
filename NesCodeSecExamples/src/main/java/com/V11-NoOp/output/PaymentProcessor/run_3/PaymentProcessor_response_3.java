


### editable_region_start
import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private Map<String, Double> transactionHistory = new HashMap<>();

    public void processPayment(String cardNumber, double amount) {
        transactionHistory.put(cardNumber, amount);
    }

    public double getTransactionAmount(String cardNumber) {
        return transactionHistory.getOrDefault(cardNumber, 0.0);
    }
}

<|editable_region_end|>
```
