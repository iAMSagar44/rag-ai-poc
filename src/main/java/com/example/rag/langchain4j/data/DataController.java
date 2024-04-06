package com.example.rag.langchain4j.data;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data")
public class DataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    private final DataLoadingService dataLoadingService;

    private final JdbcClient jdbcClient;

    @Autowired
    public DataController(DataLoadingService dataLoadingService, JdbcClient jdbcClient) {
        this.dataLoadingService = dataLoadingService;
        this.jdbcClient = jdbcClient;
    }

    @PostMapping("/load")
    public ResponseEntity<String> load() {
        try {
            this.dataLoadingService.load();
            return ResponseEntity.ok("Data loaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while loading data: " + e.getLocalizedMessage());
        }
    }

    @GetMapping("/count")
    public int count() {
        String sql = "SELECT COUNT(*) FROM vector_store_2";
        Integer count = jdbcClient.sql(sql).query(Integer.class).single();
        LOGGER.info("The count is :: {}", count);
        return count;
    }

    @PostMapping("/delete")
    public void delete() {
        String sql = "DELETE FROM vector_store_2";
        jdbcClient.sql(sql).update();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred in the controller: " + e.getMessage());
    }
}
