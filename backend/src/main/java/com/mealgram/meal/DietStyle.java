package com.mealgram.meal;

// 식단 성향과 권장 탄단지 비율

public enum DietStyle {

    BALANCED("균형", 50, 65, 10, 25, 15, 30),
    HIGH_PROTEIN("고단백", 40, 60, 20, 35, 15, 30),
    LOW_CARB("저탄수", 30, 45, 20, 35, 25, 40),
    LOW_FAT("저지방", 55, 70, 12, 25, 10, 20);

    private final String label;
    private final double carbohydrateMin;
    private final double carbohydrateMax;
    private final double proteinMin;
    private final double proteinMax;
    private final double fatMin;
    private final double fatMax;

    DietStyle(String label, double carbohydrateMin, double carbohydrateMax, double proteinMin, double proteinMax,
              double fatMin, double fatMax) {
        this.label = label;
        this.carbohydrateMin = carbohydrateMin;
        this.carbohydrateMax = carbohydrateMax;
        this.proteinMin = proteinMin;
        this.proteinMax = proteinMax;
        this.fatMin = fatMin;
        this.fatMax = fatMax;
    }

    public static DietStyle from(String label) {

        for (DietStyle style : values()) {
            if (style.label.equals(label)) {
                return style;
            }
        }

        return BALANCED;

    }

    public String getLabel() {
        return label;
    }

    public double getCarbohydrateMin() {
        return carbohydrateMin;
    }

    public double getCarbohydrateMax() {
        return carbohydrateMax;
    }

    public double getProteinMin() {
        return proteinMin;
    }

    public double getProteinMax() {
        return proteinMax;
    }

    public double getFatMin() {
        return fatMin;
    }

    public double getFatMax() {
        return fatMax;
    }

}
