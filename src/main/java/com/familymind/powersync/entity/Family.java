package com.familymind.powersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a family group.
 * Contains family information, subscription details, and living situation.
 */
@Entity
@Table(name = "family")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Family extends BaseAuditEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    @Column(name = "place_of_living")
    private String placeOfLiving;

    @Column(name = "residence_type")
    private String residenceType;

    @Column(name = "family_image")
    private String familyImage;
}

