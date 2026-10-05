package com.mealgram.recipe;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.mealgram.ingredient.Ingredient;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 레시피에 쓰이는 재료 entity

@Entity
@Getter
@Builder
@Table(
    name="recipe_ingredient",
    uniqueConstraints=@UniqueConstraint(columnNames={"recipe_id", "ingredient_id"}),
    indexes=@Index(columnList="ingredient_id")
)
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="recipe_id", nullable=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private Recipe recipe;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ingredient_id", nullable=false)
    @OnDelete(action=OnDeleteAction.CASCADE)
    private Ingredient ingredient;

}
