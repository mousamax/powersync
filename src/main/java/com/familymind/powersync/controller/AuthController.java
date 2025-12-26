package com.familymind.powersync.controller;

import com.familymind.powersync.dto.LoginRequest;
import com.familymind.powersync.dto.TokenResponse;
import com.familymind.powersync.entity.Member;
import com.familymind.powersync.repository.MemberRepository;
import com.familymind.powersync.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // TODO: Add password verification in production
        // For now, just generate token
        
        UUID familyId = member.getFamily() != null ? member.getFamily().getId() : null;
        String token = jwtService.generateToken(member.getId(), familyId, member.getEmail());

        return ResponseEntity.ok(new TokenResponse(
                token,
                member.getId(),
                familyId,
                86400000L
        ));
    }

    @GetMapping("/token/{memberId}")
    public ResponseEntity<TokenResponse> getToken(@PathVariable UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        UUID familyId = member.getFamily() != null ? member.getFamily().getId() : null;
        String token = jwtService.generateToken(member.getId(), familyId, member.getEmail());

        return ResponseEntity.ok(new TokenResponse(
                token,
                member.getId(),
                familyId,
                member.getEmail(),
                86400000L
        ));
    }
}
