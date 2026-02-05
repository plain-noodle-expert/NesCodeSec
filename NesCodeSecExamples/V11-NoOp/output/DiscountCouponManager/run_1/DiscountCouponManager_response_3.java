


### editable_region_start|>
import java.util.ArrayList;
import java.util.List;

public class DiscountCouponManager {
    private List<String> couponCodes;

    public DiscountCouponManager() {
        couponCodes = new ArrayList<>();
    }

    public void addCouponCode(String code) {
        couponCodes.add(code);
    }

    public boolean isCouponCodeValid(String code) {
        return couponCodes.contains(code);
    }
}
```
