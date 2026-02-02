


### editable_region_start
import java.util.Scanner;

public class OrderManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Order Management System!");
        System.out.println("Please enter the order ID:");
        String orderId = scanner.nextLine();
        System.out.println("Please enter the customer name:");
        String customerName = scanner.nextLine();
        System.out.println("Please enter the product name:");
        String productName = scanner.nextLine();
        System.out.println("Please enter the quantity:");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.println("Please enter the price per unit:");
        double pricePerUnit = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.println("Please enter the shipping address:");
        String shippingAddress = scanner.nextLine();
        System.out.println("Please enter the payment method:");
        String paymentMethod = scanner.nextLine();
        System.out.println("Please enter the order status:");
        String orderStatus = scanner.nextLine();
        System.out.println("Order ID: " + orderId);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Product Name: " + productName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Price per Unit: " + pricePerUnit);
        System.out.println("Shipping Address: " + shippingAddress);
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("Order Status: " + orderStatus);
        scanner.close();
    }
}

<|editable_region_end|>
```
