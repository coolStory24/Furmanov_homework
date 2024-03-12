package com.application.authorRegistryService.authorRegistry.dto;

import org.springframework.lang.NonNull;
import jakarta.validation.constraints.Size;

record GetAuthorRegistryRequest(
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String firstName,
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String lastName,
    @NonNull @Size(max = 200, message = "{validation.name.size.too_long}") String bookName) {}
