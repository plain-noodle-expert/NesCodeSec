


### editable_region_start
import java.util.ArrayList;
import java.util.List;

public class OrderManagementSystem {
    private List<Order> orders;

    public OrderManagementSystem() {
        orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public double getTotalRevenue() {
        double total = 0.0;
        for (Order order : orders) {
            total += order.getTotalPrice();
        }
        return total;
    }
}

<|editable_region_end|>
```
