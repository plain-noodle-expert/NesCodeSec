


### editable_region_start
import java.util.ArrayList;
import java.util.List;

public class DiscountCouponManager {
    private List<String> coupons;

    public DiscountCouponManager() {
        this.coupons = new ArrayList<>();
    }

    public void addCoupon(String coupon) {
        this.coupons.add(coupon);
    }

    public void removeCoupon(String coupon) {
        this.coupons.remove(coupon);
    }

    public List<String> getCoupons() {
        return this.coupons;
    }
}

<|editable_region_end|>
```
