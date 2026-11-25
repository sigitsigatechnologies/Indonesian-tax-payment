package id.co.taxpay.reconciliation.interfaces.rest;

import id.co.taxpay.reconciliation.application.service.ReconciliationBatchService;
import id.co.taxpay.reconciliation.interfaces.rest.dto.BatchResponse;
import id.co.taxpay.reconciliation.interfaces.rest.dto.PageResponse;
import id.co.taxpay.reconciliation.interfaces.rest.dto.ReconciliationExceptionResponse;
import id.co.taxpay.reconciliation.interfaces.rest.dto.ResolveExceptionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reconciliation", description = "Batch reconciliation, exceptions and manual resolution")
public class ReconciliationController {

    private final ReconciliationBatchService batchService;

    @PostMapping("/run")
    @Operation(summary = "Run a reconciliation batch for a business date")
    public BatchResponse run(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate businessDate,
            @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return batchService.run(businessDate == null ? LocalDate.now(java.time.ZoneOffset.UTC) : businessDate, actor);
    }

    @GetMapping("/batches/{batchId}")
    @Operation(summary = "Get a reconciliation batch")
    public BatchResponse getBatch(@PathVariable UUID batchId) {
        return batchService.getBatch(batchId);
    }

    @GetMapping("/exceptions")
    @Operation(summary = "List reconciliation exceptions")
    public PageResponse<ReconciliationExceptionResponse> exceptions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var result = batchService.getExceptions(status, PageRequest.of(page, size));
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @PostMapping("/exceptions/{exceptionId}/resolve")
    @Operation(summary = "Manually resolve a reconciliation exception")
    public ReconciliationExceptionResponse resolve(
            @PathVariable UUID exceptionId,
            @RequestHeader(value = "X-Actor", defaultValue = "operations") String actor,
            @Valid @RequestBody ResolveExceptionRequest request) {
        return batchService.resolve(exceptionId, actor);
    }
}
