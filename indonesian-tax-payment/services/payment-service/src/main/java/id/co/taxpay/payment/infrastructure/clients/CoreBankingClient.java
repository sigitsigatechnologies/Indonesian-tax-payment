package id.co.taxpay.payment.infrastructure.clients;

import id.co.taxpay.payment.infrastructure.clients.dto.AccountResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.BalanceResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.DebitRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.DebitResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ReverseRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ReverseResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.TransactionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "core-banking-mock", url = "${services.core-banking.url:http://localhost:8084}")
public interface CoreBankingClient {

    @GetMapping("/api/v1/accounts/{accountNumber}")
    AccountResponseDto getAccount(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/v1/accounts/{accountNumber}/balance")
    BalanceResponseDto getBalance(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/api/v1/transactions/debit")
    DebitResponseDto debit(@RequestBody DebitRequestDto request,
                           @RequestHeader("X-Mock-Scenario") String scenario);

    @GetMapping("/api/v1/transactions/{reference}")
    TransactionResponseDto getTransaction(@PathVariable("reference") String reference);

    @PostMapping("/api/v1/transactions/{reference}/reverse")
    ReverseResponseDto reverse(@PathVariable("reference") String reference,
                               @RequestBody ReverseRequestDto request,
                               @RequestHeader("X-Mock-Scenario") String scenario);

    @GetMapping("/api/v1/transactions/{reference}/reversal")
    ReverseResponseDto getReversal(@PathVariable("reference") String reference);
}
