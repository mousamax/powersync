package com.familymind.powersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a family member.
 * Contains member information, authentication details, and family relationship.
 */
@Entity
@Table(
    name = "member",
    indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_family_id", columnList = "family_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseAuditEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Builder.Default
    @Column(name = "is_google", nullable = false)
    private Boolean isGoogle = false;

    @Builder.Default
    @Column(name = "is_apple", nullable = false)
    private Boolean isApple = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = true, 
    foreignKey = @ForeignKey(name = "fk_member_family"),
    referencedColumnName = "id")
    private Family family;

    @Column(name = "member_role")
    private String memberRole;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "color")
    private String color;

    @Column(name = "image")
    private String image;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
}

