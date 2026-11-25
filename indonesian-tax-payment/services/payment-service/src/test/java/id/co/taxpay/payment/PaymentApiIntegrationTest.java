package id.co.taxpay.payment;

import id.co.taxpay.common.security.JwtTokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "taxpay.outbox.enabled=false",
                "services.billing.url=http://localhost:19991",
                "services.core-banking.url=http://localhost:19992",
                "services.tax-gateway.url=http://localhost:19993"
        }
)
class PaymentApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("payment_db")
            .withUsername("taxpay")
            .withPassword("taxpay");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    JwtTokenService tokenService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void createsPaymentReplaysIdempotentlyAndRecordsHistory() {
        String makerToken = tokenService.issue("maker.one", "MAKER", Duration.ofHours(1));
        String body = """
                {
                  "billingId": "BILL-2026-0001",
                  "debitAccountNumber": "1234567890",
                  "amount": 1500000.00,
                  "currency": "IDR",
                  "customerId": "CUST-001"
                }
                """;

        String firstId = given()
                .header("Authorization", "Bearer " + makerToken)
                .header("Idempotency-Key", "it-key-001")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/v1/tax/payments")
                .then().statusCode(201)
                .body("status", equalTo("RECEIVED"))
                .extract().jsonPath().getString("paymentId");

        String replayId = given()
                .header("Authorization", "Bearer " + makerToken)
                .header("Idempotency-Key", "it-key-001")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/v1/tax/payments")
                .then().statusCode(201)
                .extract().jsonPath().getString("paymentId");
        assertEquals(firstId, replayId);

        given().header("Authorization", "Bearer " + makerToken)
                .header("Idempotency-Key", "it-key-001")
                .contentType(ContentType.JSON)
                .body(body.replace("1500000.00", "999.00"))
                .when().post("/api/v1/tax/payments")
                .then().statusCode(409)
                .body("code", equalTo("IDEMPOTENCY_CONFLICT"));

        given().header("Authorization", "Bearer " + makerToken)
                .when().get("/api/v1/tax/payments/{id}/history", firstId)
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].toStatus", equalTo("RECEIVED"));
    }

    @Test
    void enforcesMakerCheckerSeparationAndRole() {
        String makerToken = tokenService.issue("maker.one", "MAKER", Duration.ofHours(1));
        String checkerToken = tokenService.issue("checker.one", "CHECKER", Duration.ofHours(1));
        String body = """
                {"billingId":"BILL-2026-0001","debitAccountNumber":"1234567890",
                 "amount":1500000.00,"currency":"IDR","customerId":"CUST-001"}
                """;
        String paymentId = given()
                .header("Authorization", "Bearer " + makerToken)
                .header("Idempotency-Key", "it-key-002")
                .header("X-Actor", "maker.one")
                .contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/tax/payments")
                .then().statusCode(201)
                .extract().jsonPath().getString("paymentId");

        jdbcTemplate.update("UPDATE tax_payments SET status = 'PENDING_APPROVAL' WHERE id = ?", paymentId);
        jdbcTemplate.update("UPDATE tax_payment_approvals SET maker_id = 'maker.one' WHERE payment_id = ?", paymentId);

        given().header("Authorization", "Bearer " + makerToken)
                .header("X-Actor", "checker.one")
                .contentType(ContentType.JSON).body("{\"notes\":\"ok\"}")
                .when().post("/api/v1/tax/payments/{id}/approve", paymentId)
                .then().statusCode(403);

        given().header("Authorization", "Bearer " + checkerToken)
                .header("X-Actor", "checker.one")
                .contentType(ContentType.JSON).body("{\"notes\":\"ok\"}")
                .when().post("/api/v1/tax/payments/{id}/approve", paymentId)
                .then().statusCode(200)
                .body("status", equalTo("APPROVED"));
    }

    @Test
    void concurrentIdenticalRequestsProduceSinglePayment() throws Exception {
        String makerToken = tokenService.issue("maker.one", "MAKER", Duration.ofHours(1));
        String body = """
                {"billingId":"BILL-2026-0001","debitAccountNumber":"1234567890",
                 "amount":1500000.00,"currency":"IDR","customerId":"CUST-001"}
                """;
        List<Callable<String>> calls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            calls.add(() -> given()
                    .header("Authorization", "Bearer " + makerToken)
                    .header("Idempotency-Key", "it-key-concurrent")
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/api/v1/tax/payments")
                    .then().statusCode(201)
                    .extract().jsonPath().getString("paymentId"));
        }
        var executor = Executors.newFixedThreadPool(10);
        List<Future<String>> results = executor.invokeAll(calls);
        Set<String> distinctIds = new HashSet<>();
        for (Future<String> result : results) {
            distinctIds.add(result.get());
        }
        executor.shutdown();
        assertEquals(1, distinctIds.size());
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM payment_idempotency WHERE idempotency_key = 'it-key-concurrent'",
                Integer.class);
        assertTrue(rows != null && rows == 1);
    }
}
