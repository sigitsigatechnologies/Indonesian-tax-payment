package id.co.taxpay.corebanking.interfaces.rest;

import id.co.taxpay.corebanking.application.service.BankTransactionService;
import id.co.taxpay.corebanking.infrastructure.persistence.BankAccountEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.BankReversalTransactionEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.BankTransactionEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankAccountRepository;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankReversalRepository;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankTransactionRepository;
import id.co.taxpay.corebanking.interfaces.rest.dto.AccountResponse;
import id.co.taxpay.corebanking.interfaces.rest.dto.BalanceResponse;
import id.co.taxpay.corebanking.interfaces.rest.dto.DebitRequest;
import id.co.taxpay.corebanking.interfaces.rest.dto.DebitResponse;
import id.co.taxpay.corebanking.interfaces.rest.dto.ReverseRequest;
import id.co.taxpay.corebanking.interfaces.rest.dto.ReverseResponse;
import id.co.taxpay.corebanking.interfaces.rest.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@Tag(name = "Core Banking Mock", description = "Simulated core banking accounts, debits and reversals")
public class CoreBankingController {

    private final SpringDataBankAccountRepository accountRepository;
    private final SpringDataBankTransactionRepository transactionRepository;
    private final SpringDataBankReversalRepository reversalRepository;
    private final BankTransactionService transactionService;

    @GetMapping("/api/v1/accounts/{accountNumber}")
    @Operation(summary = "Inquire a bank account")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        return AccountResponse.from(findAccount(accountNumber));
    }

    @GetMapping("/api/v1/accounts/{accountNumber}/balance")
    @Operation(summary = "Inquire an account balance")
    public BalanceResponse getBalance(@PathVariable String accountNumber) {
        return BalanceResponse.from(findAccount(accountNumber));
    }

    @PostMapping("/api/v1/transactions/debit")
    @Operation(summary = "Debit an account (mock)")
    public DebitResponse debit(
            @Valid @RequestBody DebitRequest request,
            @RequestHeader(value = "X-Mock-Scenario", defaultValue = "NONE") String scenario) {
        if ("TIMEOUT_BEFORE_DEBIT".equalsIgnoreCase(scenario)) {
            sleep(3000);
        }
        DebitResponse response = transactionService.debit(request, scenario);
        if ("TIMEOUT_AFTER_DEBIT".equalsIgnoreCase(scenario)) {
            sleep(3000);
        }
        return response;
    }

    @GetMapping("/api/v1/transactions/{reference}")
    @Operation(summary = "Inquire a bank transaction by reference")
    public TransactionResponse getTransaction(@PathVariable String reference) {
        BankTransactionEntity entity = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Transaction " + reference + " was not found"));
        return TransactionResponse.from(entity);
    }

    @PostMapping("/api/v1/transactions/{reference}/reverse")
    @Operation(summary = "Reverse a completed debit (mock)")
    public ReverseResponse reverse(
            @PathVariable String reference,
            @Valid @RequestBody ReverseRequest request,
            @RequestHeader(value = "X-Mock-Scenario", defaultValue = "NONE") String scenario) {
        if ("TIMEOUT_BEFORE_REVERSAL".equalsIgnoreCase(scenario)) {
            sleep(3000);
        }
        ReverseResponse response = transactionService.reverse(reference, request);
        if ("TIMEOUT_AFTER_REVERSAL".equalsIgnoreCase(scenario)) {
            sleep(3000);
        }
        return response;
    }

    @GetMapping("/api/v1/transactions/{reference}/reversal")
    @Operation(summary = "Inquire a bank reversal by reference")
    public ReverseResponse getReversal(@PathVariable String reference) {
        BankReversalTransactionEntity entity = reversalRepository.findByReversalReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reversal " + reference + " was not found"));
        return ReverseResponse.from(entity, false);
    }

    private BankAccountEntity findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account " + accountNumber + " was not found"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Simulated timeout interrupted");
        }
    }
}
