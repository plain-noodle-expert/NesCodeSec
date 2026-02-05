


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
        return new ArrayList<>(orders);
    }

    public Order getOrderById(int id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }
        return null;
    }

    public void updateOrder(Order order) {
        int index = orders.indexOf(order);
        if (index != -1) {
            orders.set(index, order);
        }
    }

    public void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELED);
        updateOrder(order);
    }

    public void completeOrder(Order order) {
        order.setStatus(OrderStatus.COMPLETED);
        updateOrder(order);
    }

    public void refundOrder(Order order) {
        order.setStatus(OrderStatus.REFUNDED);
        updateOrder(order);
    }

    public void cancelAllOrders() {
        for (Order order : orders) {
            cancelOrder(order);
        }
    }

    public void completeAllOrders() {
        for (Order order : orders) {
            completeOrder(order);
        }
    }

    public void refundAllOrders() {
        for (Order order : orders) {
            refundOrder(order);
        }
    }

    public void printOrders() {
        for (Order order : orders) {
            System.out.println(order);
        }
    }

    public void printOrdersByStatus(OrderStatus status) {
        for (Order order : orders) {
            if (order.getStatus() == status) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomer(Customer customer) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByProduct(Product product) {
        for (Order order : orders) {
            if (order.getProduct().equals(product)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByDateRange(Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByAmountRange(double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndCustomer(OrderStatus status, Customer customer) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getCustomer().equals(customer)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndProduct(OrderStatus status, Product product) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getProduct().equals(product)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndDateRange(OrderStatus status, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndAmountRange(OrderStatus status, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndProduct(Customer customer, Product product) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getProduct().equals(product)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndDateRange(Customer customer, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndAmountRange(Customer customer, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByProductAndDateRange(Product product, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getProduct().equals(product) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByProductAndAmountRange(Product product, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getProduct().equals(product) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByDateRangeAndAmountRange(Date startDate, Date endDate, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getDate().after(startDate) && order.getDate().before(endDate) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndCustomerAndProduct(OrderStatus status, Customer customer, Product product) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getCustomer().equals(customer) && order.getProduct().equals(product)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndCustomerAndDateRange(OrderStatus status, Customer customer, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getCustomer().equals(customer) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndCustomerAndAmountRange(OrderStatus status, Customer customer, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getCustomer().equals(customer) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndProductAndDateRange(OrderStatus status, Product product, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getProduct().equals(product) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndProductAndAmountRange(OrderStatus status, Product product, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getProduct().equals(product) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndProductAndDateRange(Customer customer, Product product, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getProduct().equals(product) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndProductAndAmountRange(Customer customer, Product product, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getProduct().equals(product) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByCustomerAndDateRangeAndAmountRange(Customer customer, Date startDate, Date endDate, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getCustomer().equals(customer) && order.getDate().after(startDate) && order.getDate().before(endDate) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByProductAndDateRangeAndAmountRange(Product product, Date startDate, Date endDate, double minAmount, double maxAmount) {
        for (Order order : orders) {
            if (order.getProduct().equals(product) && order.getDate().after(startDate) && order.getDate().before(endDate) && order.getAmount() >= minAmount && order.getAmount() <= maxAmount) {
                System.out.println(order);
            }
        }
    }

    public void printOrdersByStatusAndCustomerAndProductAndDateRange(OrderStatus status, Customer customer, Product product, Date startDate, Date endDate) {
        for (Order order : orders) {
            if (order.getStatus() == status && order.getCustomer().equals(customer) && order.getProduct().equals(product) && order.getDate().after(startDate) && order.getDate().before(endDate)) {
                System