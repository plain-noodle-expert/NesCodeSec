<|editable_region_start|>
package com.example.Export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelExportService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpMethodHandler httpMethodHandler;
    private final String assetApiUrl;
    private static final int PARALLEL_CALLS = 5;
    private static final int PAGE_SIZE = 5000;
    
    // This method simulates API call but reads from mock JSON file
    public List<JsonNode> fetchAssetsFromAPI(JsonNode filterBody, int offset) throws IOException {
        FilterBodyDto apiFilterBody = new FilterBodyDto();
        apiFilterBody.setColumns(filterBody.getColumns());
        apiFilterBody.setFilters(filterBody.getFilters());
        
        // Set pagination with the thread-specific offset
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(PAGE_SIZE);
        apiFilterBody.setPagination(pagination);
        
        // For now, read from mock JSON file
        ClassPathResource resource = new ClassPathResource("mock-assets.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<JsonNode>>() {});
        }
    }
    
    public byte[] generateExcel() throws IOException {

        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(PARALLEL_CALLS);
            List<JsonNode> allAssets = new ArrayList<>();
            boolean hasMore = true;
            int currentOffset = 0;
            int totalProcessed = 0;
            
            while (hasMore) {
                List<CompletableFuture<List<JsonNode>>> batchFutures = new ArrayList<>();
                
                // Launch parallel calls for this batch
                for (int i = 0; i < PARALLEL_CALLS; i++) {
                    final int offset = currentOffset;
                    currentOffset += PAGE_SIZE;
                    
                    batchFutures.add(CompletableFuture.supplyAsync(() -> 
                        fetchAssetsFromAPI(filterBody, offset), executor));
                }
                
                // Wait for batch to complete and collect results
                List<JsonNode> batchResults = batchFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                
                // Check if this was the last page
                if (batchResults.size() < PAGE_SIZE) {
                    hasMore = false;
                }
                
                // Update progress
                totalProcessed += batchResults.size();
                log.info("Processed {}/{} assets (batch size: {})", 
                    totalProcessed, 
                    hasMore ? "?" : totalProcessed, 
                    batchResults.size());
                
                allAssets.addAll(batchResults);
            }
            
            log.info("Starting Excel creation for {} assets", allAssets.size());
            return createExcelFromAssets(allAssets);
            
        } catch (CompletionException e) {
            log.error("Failed to fetch assets: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch assets: " + e.getMessage());
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
        
    }
    private byte[] createExcelFromAssets(List<JsonNode> assets) throws IOException {
try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Assets");
            
            int currentRow = 0;
            
            // Create headers
            currentRow = createGroupHeaders(sheet, workbook, currentRow);
            currentRow = createColumnHeaders(sheet, workbook, currentRow);
            
            // Process each asset
            for (JsonNode asset : assets) {
                currentRow = processAsset(sheet, workbook, asset, currentRow);
            }
            
            
            // Return as byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
        
            
    
    private int processAsset(Sheet sheet, Workbook workbook, JsonNode asset, int currentRow) {
        // Calculate maximum rows needed for this asset
        int maxRows = calculateMaxRows(asset);
        
        // Create style for data cells
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle bottomBorderStyle = createBottomBorderStyle(workbook);
        
        // Process each row
        int assetStartRow = currentRow;
        for (int arrayIndex = 0; arrayIndex < maxRows; arrayIndex++) {
            Map<String, String> rowData = buildRowData(asset, arrayIndex);
            createExcelRow(sheet, rowData, dataStyle, currentRow++);
        }
        
        // Add bottom border to last row
        addBottomBorderToRow(sheet, bottomBorderStyle, currentRow - 1);
        
        return currentRow;
    }
    
    private int calculateMaxRows(JsonNode asset) {
        int maxRows = 1; // At least one row for simple fields
        
        // Check each array field to find the maximum array size
        for (Map.Entry<String, FieldMappingConfig.FieldMetadata> entry : FieldMappingConfig.FIELD_CONFIG.entrySet()) {
            FieldMappingConfig.FieldMetadata metadata = entry.getValue();
            
            if (metadata.type == FieldMappingConfig.FieldType.ARRAY) {
                JsonNode arrayNode = asset.get(metadata.sourceArray);
                if (arrayNode != null && arrayNode.isArray()) {
                    maxRows = Math.max(maxRows, arrayNode.size());
                }
            }
        }
        
        return maxRows;
    }
    
    public Map<String, String> buildRowData(JsonNode asset, int arrayIndex) {
        Map<String, String> rowData = new HashMap<>();
        
        // Process each field according to its configuration
        for (Map.Entry<String, FieldMappingConfig.FieldMetadata> entry : FieldMappingConfig.FIELD_CONFIG.entrySet()) {
            String fieldName = entry.getKey();
            FieldMappingConfig.FieldMetadata metadata = entry.getValue();
            
            String value = extractValueBasedOnType(asset, fieldName, metadata, arrayIndex);
            rowData.put(fieldName, value);
        }
        
        return rowData;
    }
}
<|editable_region_end|>

### Response:
<|editable_region_start|>
package com.example.Export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelExportService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpMethodHandler httpMethodHandler;
    private final String assetApiUrl;
    private static final int PARALLEL_CALLS = 5;
    private static final int PAGE_SIZE = 5000;
    
    // This method simulates API call but reads from mock JSON file
    public List<JsonNode> fetchAssetsFromAPI(JsonNode filterBody, int offset) throws IOException {
        FilterBodyDto apiFilterBody = new FilterBodyDto();
        apiFilterBody.setColumns(filterBody.getColumns());
        apiFilterBody.setFilters(filterBody.getFilters());
        
        // Set pagination with the thread-specific offset
        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(PAGE_SIZE);
        apiFilterBody.setPagination(pagination);
        
        // For now, read from mock JSON file
        ClassPathResource resource = new ClassPathResource("mock-assets.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<JsonNode>>() {});
        }
    }
    
    public byte[] generateExcel() throws IOException {

        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(PARALLEL_CALLS);
            List<JsonNode> allAssets = new ArrayList<>();
            boolean hasMore = true;
            int currentOffset = 0;
            int totalProcessed = 0;
            
            while (hasMore) {
                List<CompletableFuture<List<JsonNode>>> batchFutures = new ArrayList<>();
                
                // Launch parallel calls for this batch
                for (int i = 0; i < PARALLEL_CALLS; i++) {
                    final int offset = currentOffset;
                    currentOffset += PAGE_SIZE;
                    
                    batchFutures.add(CompletableFuture.supplyAsync(() -> 
                        fetchAssetsFromAPI(filterBody, offset), executor));
                }
                
                // Wait for batch to complete and collect results
                List<JsonNode> batchResults = batchFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                
                // Check if this was the last page
                if (batchResults.size() < PAGE_SIZE) {
                    hasMore = false;
                }
                
                // Update progress
                totalProcessed += batchResults.size();
                log.info("Processed {}/{} assets (batch size: {})", 
                    totalProcessed, 
                    hasMore ? "?" : totalProcessed, 
                    batchResults.size());
                
                allAssets.addAll(batchResults);
            }
