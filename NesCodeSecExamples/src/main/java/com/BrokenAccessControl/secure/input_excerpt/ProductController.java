```<|start_of_file|>
<|editable_region_start|>
package com.dio.challenge.storefront.controller;

import com.dio.challenge.storefront.dto.ProductDTO;
import com.dio.challenge.storefront.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storefront/products")
@EnableMethodSecurity(prePostEnabled = true)
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private WarehouseService warehouseService;
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = warehouseService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = warehouseService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<ProductDTO>> getAvailableProducts() {
        List<ProductDTO> products = warehouseService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name) {
        List<ProductDTO> products = warehouseService.searchProducts(name);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}/stock-check")
    public ResponseEntity<Boolean> checkStockAvailability(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean available = warehouseService.checkStockAvailability(id, quantity);
        return ResponseEntity.ok(available);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = warehouseService.updateProduct(id, productDTO);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<ProductDTO> removeProduct(@PathVariable Long id) <|user_cursor_is_here|>
}
<|editable_region_end|>
```