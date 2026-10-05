package com.mealgram.recipe;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 레시피 entity

@Entity
@Getter
@Builder
@Table(name="recipe")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class Recipe {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=50)
    private String name;

    @Column(nullable=false, length=20)
    private String category;

    @Column(nullable=false, length=20)
    private String cookingMethod;

    @Column(nullable=false)
    private BigDecimal calorie;

    @Column(nullable=false)
    private BigDecimal carbohydrate;

    @Column(nullable=false)
    private BigDecimal protein;

    @Column(nullable=false)
    private BigDecimal fat;

    @Column(nullable=false)
    private BigDecimal sodium;

    @Column(nullable=false, columnDefinition="TEXT")
    private String cookingSteps;

    @Column(length=255)
    private String imageUrl;

    @Column(nullable=false, columnDefinition="TEXT")
    private String ingredientRawText;

}
