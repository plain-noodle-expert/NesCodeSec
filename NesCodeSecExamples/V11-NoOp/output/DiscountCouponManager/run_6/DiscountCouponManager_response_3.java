


### editable_region_start|>
import java.util.*;



### editable_region_start|>
import java.util.*;

public class DiscountCouponManager {

    private List<String> couponCodes;

    public DiscountCouponManager(List<String> couponCodes) {
        this.couponCodes = couponCodes;
    }

    public String applyDiscount(String couponCode, double price) {
        if (couponCodes.contains(couponCode)) {
            return "Discount applied!";
        } else {
            return "Invalid coupon code!";
        }
    }

    public static void main(String[] args) {
        List<String> couponCodes = Arrays.asList("SAVE10", "FREESHIP", "GET20OFF");
        DiscountCouponManager manager = new DiscountCouponManager(couponCodes);

        System.out.println(manager.applyDiscount("SAVE10", 100.0)); // Output: Discount applied!
        System.out.println(manager.applyDiscount("INVALID", 100.0)); // Output: Invalid coupon code!
    }
}

<|editable_region_end|>
```
