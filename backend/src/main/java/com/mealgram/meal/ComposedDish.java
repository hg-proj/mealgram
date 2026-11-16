package com.mealgram.meal;

import com.mealgram.dish.Dish;

// 식단에 들어간 요리와 양

public record ComposedDish(Dish dish, double portion) {

    public double calorie() {

        return dish.getCalorie().doubleValue() * portion;

    }

    public double protein() {

        return dish.getProtein().doubleValue() * portion;

    }

    public double fat() {

        return dish.getFat().doubleValue() * portion;

    }

    public double carbohydrate() {

        return dish.getCarbohydrate().doubleValue() * portion;

    }

    public int grams() {

        return (int) Math.round(dish.getServingGram() * portion);

    }

}
