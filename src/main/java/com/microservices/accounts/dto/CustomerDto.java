package com.microservices.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "Customer", description = "Schema holding Customer and Account information")
@Data
public class CustomerDto {

    @Schema(description = "Full name of the customer", example = "John Doe")
    @NotEmpty(message = "Name cannot be a null or empty")
    @Size(min = 5, max = 30, message = "The length of the customer name should be between 5 and 30")
    private String name;

    @Schema(description = "Email address of the customer", example = "john.doe@example.com")
    @NotEmpty(message = "Email address cannot be a null or empty")
    @Email(message = "Email address should be a valid value")
    private String email;

    @Schema(description = "10-digit mobile number of the customer", example = "9876543210")
    @NotEmpty(message = "mobile number can not be empty")
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Schema(description = "Account details associated with the customer")
    private AccountsDto accountsDto;
}
