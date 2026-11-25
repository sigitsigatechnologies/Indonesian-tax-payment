package id.co.taxpay.corebanking.application.service;

import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.corebanking.infrastructure.persistence.BankAccountEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.BankReversalTransactionEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.BankTransactionEntity;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankAccountRepository;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankReversalRepository;
import id.co.taxpay.corebanking.infrastructure.persistence.SpringDataBankTransactionRepository;
import id.co.taxpay.corebanking.interfaces.rest.BankingException;
import id.co.taxpay.corebanking.interfaces.rest.dto.DebitRequest;
import id.co.taxpay.corebanking.interfaces.rest.dto.DebitResponse;
import id.co.taxpay.corebanking.interfaces.rest.dto.ReverseRequest;
import id.co.taxpay.corebanking.interfaces.rest.dto.ReverseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankTransactionService {

    private final SpringDataBankAccountRepository accountRepository;
    private final SpringDataBankTransactionRepository transactionRepository;
    private final SpringDataBankReversalRepository reversalRepository;
    private final Clock clock;

    @Transactional
    public DebitResponse debit(DebitRequest request, String scenario) {
        var existing = transactionRepository.findByTransactionReference(request.debitReference());
        if (existing.isPresent()) {
            return DebitResponse.from(existing.get(), true);
        }

        BankAccountEntity account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new BankingException(ErrorCode.ACCOUNT_NOT_FOUND,
                        "Account " + request.accountNumber() + " was not found"));
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new BankingException(ErrorCode.ACCOUNT_INACTIVE, "Account is not active");
        }
        if (!account.getCurrency().equals(request.currency().toUpperCase(Locale.ROOT))) {
            throw new BankingException(ErrorCode.ACCOUNT_CURRENCY_MISMATCH, "Account currency does not match");
        }
        BigDecimal amount = request.amount().setScale(2, java.math.RoundingMode.UNNECESSARY);
        if ("INSUFFICIENT_BALANCE".equalsIgnoreCase(scenario)
                || account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BankingException(ErrorCode.INSUFFICIENT_BALANCE, "Insufficient available balance");
        }

        Instant now = clock.instant();
        BigDecimal newBalance = account.getAvailableBalance().subtract(amount);
        account.setAvailableBalance(newBalance);
        account.setLedgerBalance(newBalance);
        account.setUpdatedAt(now);
        accountRepository.saveAndFlush(account);

        BankTransactionEntity transaction = new BankTransactionEntity();
        transaction.setId(UUID.randomUUID());
        transaction.setTransactionReference(request.debitReference());
        transaction.setAccountNumber(request.accountNumber());
        transaction.setTransactionType("DEBIT");
        transaction.setAmount(amount);
        transaction.setCurrency(request.currency().toUpperCase(Locale.ROOT));
        transaction.setStatus("COMPLETED");
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(request.description());
        transaction.setCreatedAt(now);
        transaction.setCompletedAt(now);
        transaction.setVersion(0L);
        return DebitResponse.from(transactionRepository.saveAndFlush(transaction), false);
    }

    @Transactional
    public ReverseResponse reverse(String originalReference, ReverseRequest request) {
        var existingReversal = reversalRepository.findByReversalReference(request.reversalReference());
        if (existingReversal.isPresent()) {
            return ReverseResponse.from(existingReversal.get(), true);
        }
        BankTransactionEntity original = transactionRepository.findByTransactionReference(originalReference)
                .orElseThrow(() -> new BankingException(ErrorCode.NOT_FOUND,
                        "Transaction " + originalReference + " was not found"));
        if (!"COMPLETED".equals(original.getStatus())) {
            throw new BankingException(ErrorCode.REVERSAL_NOT_ALLOWED,
                    "Only a completed debit can be reversed");
        }
        if (original.getAmount().compareTo(request.amount()) != 0
                || !original.getCurrency().equals(request.currency())) {
            throw new BankingException(ErrorCode.AMOUNT_MISMATCH,
                    "Reversal amount or currency does not match the original debit");
        }

        Instant now = clock.instant();
        BankAccountEntity account = accountRepository.findByAccountNumber(original.getAccountNumber())
                .orElseThrow(() -> new BankingException(ErrorCode.ACCOUNT_NOT_FOUND, "Account was not found"));
        BigDecimal restoredBalance = account.getAvailableBalance().add(request.amount());
        account.setAvailableBalance(restoredBalance);
        account.setLedgerBalance(restoredBalance);
        account.setUpdatedAt(now);
        accountRepository.saveAndFlush(account);

        original.setStatus("REVERSED");
        transactionRepository.saveAndFlush(original);

        BankReversalTransactionEntity reversal = new BankReversalTransactionEntity();
        reversal.setId(UUID.randomUUID());
        reversal.setReversalReference(request.reversalReference());
        reversal.setOriginalTransactionReference(originalReference);
        reversal.setAccountNumber(original.getAccountNumber());
        reversal.setAmount(request.amount());
        reversal.setCurrency(request.currency());
        reversal.setStatus("COMPLETED");
        reversal.setReason(request.reason());
        reversal.setCreatedAt(now);
        reversal.setCompletedAt(now);
        return ReverseResponse.from(reversalRepository.saveAndFlush(reversal), false);
    }
}
