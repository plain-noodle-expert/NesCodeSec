```<|start_of_file|>
<|editable_region_start|>
package sportswear.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import sportswear.DTO.OrderProductDTO;
import sportswear.entities.Order;
import sportswear.entities.OrderProduct;
import sportswear.entities.User;
import sportswear.exception.ResourceNotFoundException;
import sportswear.services.OrderProductService;
import sportswear.services.OrderService;
import sportswear.services.ProductService;

@Controller
@RequestMapping
public class OrderController {
	
	@Autowired
	OrderService service;
	
	@Autowired
	OrderProductService detailsService;
	
	@Autowired
	ProductService productService;
	
	@GetMapping("/orders/{username}")
	public ResponseEntity<List<String>> getOrders(@PathVariable String username){
		return ResponseEntity.ok(service.getAllOrders(username));
	}
		<|user_cursor_is_here|>
	    public ResponseEntity<Order> create(@RequestBody OrderForm form) {
	        List<OrderProductDTO> formDtos = form.getProductOrders();
	        validateProductsExistence(formDtos);
	        Order order = new Order();
	        order = this.service.create(order,form.getUser());

	        List<OrderProduct> orderProducts = new ArrayList<>();
	        for (OrderProductDTO dto : formDtos) {
	            orderProducts.add(detailsService.create(new OrderProduct(order, productService.getProduct(dto
	              .getProduct()
	              .getId()), dto.getQuantity())));
	            System.out.println(dto.getQuantity());
	        }

	        order.setOrderProducts(orderProducts);

	        this.service.update(order);
	        return ResponseEntity.ok(order);
	    }


		public Admin getAdmin(String username, String token) {
			// TO DO: Implement token validation and admin retrieval logic
			return null;
	
		}

	    public void validateProductsExistence(List<OrderProductDTO> orderProducts) {
	        List<OrderProductDTO> list = orderProducts
	          .stream()
	          .filter(op -> Objects.isNull(productService.getProduct(op
	            .getProduct()
	            .getId())))
	          .collect(Collectors.toList());

	        if (!CollectionUtils.isEmpty(list)) {
	            throw new ResourceNotFoundException("Product not found");
	        }
	    }

		
}
<|editable_region_end|>
```
