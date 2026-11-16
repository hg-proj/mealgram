package com.mealgram.dish;

// 요리 역할 구분

public enum DishRole {

    RICE("밥"),
    SOUP("국"),
    MAIN("메인 반찬"),
    SIDE("곁들임 반찬"),
    PICKLE("김치와 절임"),
    DESSERT("후식"),
    ONE_DISH("일품");

    private final String label;

    DishRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
