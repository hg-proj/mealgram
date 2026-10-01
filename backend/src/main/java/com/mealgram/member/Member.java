package com.mealgram.member;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// member entity

@Entity
@Getter
@Builder
@Table(name="member")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=10)
    private String nickname;

    @Column(nullable=false, length=20, unique=true)
    private String loginId;

    @Column(nullable=false, length=255)
    private String password;

    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length=10)
    private Gender gender;

    private BigDecimal height;

    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(length=20)
    private ActivityLevel activityLevel;

    @Column(nullable=false, unique=true)
    private String email;

    @CreatedDate
    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable=false)
    private LocalDateTime updatedAt;


    // enum ------
    public enum Gender {MALE, FEMALE}

    public enum ActivityLevel {LOW, LIGHT, MODERATE, HIGH}

}
