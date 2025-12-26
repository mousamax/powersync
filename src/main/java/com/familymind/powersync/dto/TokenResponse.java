package com.familymind.powersync.dto;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID memberId,
        UUID familyId,
        String email,
        long expiresIn
) {}
