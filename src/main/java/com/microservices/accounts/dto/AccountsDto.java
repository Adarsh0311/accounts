package com.microservices.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(name = "Account", description = "Schema holding Account information")
@Data
public class AccountsDto {

    @Schema(description = "Unique 10-digit account number", example = "1234567890")
    @NotEmpty(message = "AccountNumber can not be a null or empty")
    @Pattern(regexp="(^$|[0-9]{10})",message = "AccountNumber must be 10 digits")
    private Long accountNumber;

    @Schema(description = "Type of the bank account", example = "Savings")
    @NotEmpty(message = "AccountType can not be a null or empty")
    private String accountType;

    @Schema(description = "Branch address linked to the account", example = "123 Main Street, New York")
    @NotEmpty(message = "BranchAddress can not be a null or empty")
    private String branchAddress;
}
