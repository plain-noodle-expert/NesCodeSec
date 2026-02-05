


### editable_region_start
import java.util.Scanner;

public class PaymentProcessor {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the amount to be paid:");
        double amount = scanner.nextDouble();
        System.out.println("Enter the payment method (1 for cash, 2 for card):");
        int method = scanner.nextInt();
        scanner.close();

        if (method == 1) {
            System.out.println("Payment of " + amount + " received in cash.");
        } else if (method == 2) {
            System.out.println("Payment of " + amount + " received by card.");
        } else {
            System.out.println("Invalid payment method.");
        }
    }
}
<|editable_region_end|>
```
