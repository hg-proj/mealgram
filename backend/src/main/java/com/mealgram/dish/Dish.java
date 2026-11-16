package com.mealgram.dish;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// 요리 entity

@Entity
@Getter
@Builder
@Table(name="dish")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class Dish {

    public static final String LIST_SEPARATOR = ",";
    public static final String FRIED = "튀김";
    public static final String FATTY = "고지방";
    public static final String SPICY = "매운맛";
    public static final String FRUIT = "과일";
    public static final String DAIRY = "유제품";
    public static final String NOODLE = "면";
    public static final String RICE_BOWL = "밥";
    public static final String SOUP_RICE = "탕반";
    public static final String SNACK = "분식";
    public static final String SALAD = "샐러드";

    private static final List<Pattern> FAMILY_PATTERNS = List.of(
            Pattern.compile("불고기|주물럭"), Pattern.compile("닭갈비"), Pattern.compile("찜닭"), Pattern.compile("갈비찜"),
            Pattern.compile("탕수육|꿔바로우|깐풍"), Pattern.compile("계란말이|달걀말이"), Pattern.compile("김치찜"),
            Pattern.compile("수육|보쌈|족발"), Pattern.compile("치킨|강정|닭봉"), Pattern.compile("까스|커틀릿"),
            Pattern.compile("제육|두루치기"), Pattern.compile("멸치볶음"), Pattern.compile("장조림"), Pattern.compile("오리"));
    private static final Pattern FOCUS_INGREDIENT = Pattern.compile(
            "고등어|갈치|삼치|꽁치|조기|임연수|가자미|대구|명태|코다리|연어|동태|오징어|낙지|쭈꾸미|주꾸미|쭈삼|새우|관자|바지락|꽃게|멸치|두부|"
                    + "계란|달걀|메추리알|오리|닭|삼겹살|돼지|돈육|소고기|쇠고기|한우|햄|소시지|어묵|맛살");
    private static final Pattern STAPLE_SOUP = Pattern.compile("(밥|죽|국수|수제비|떡국|만둣국|칼국수|라면|우동|냉면)");
    private static final Pattern PICKLED = Pattern.compile("(단무지|오이지|피클|장아찌|절임|무말랭이|짠지|초절)");

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private DishRole role;

    @Column(nullable=false, length=10)
    private String cuisine;

    @Column(nullable=false, length=100)
    private String mainIngredients;

    @Column(nullable=false, length=100)
    private String methods;

    @Column(nullable=false, length=100)
    private String flags;

    @Column(length=10)
    private String dessertType;

    @Column(length=10)
    private String imageGroup;

    @Column(nullable=false)
    private boolean heavySoup;

    @Column(nullable=false)
    private int servingGram;

    @Column(nullable=false)
    private BigDecimal calorie;

    @Column(nullable=false)
    private BigDecimal protein;

    @Column(nullable=false)
    private BigDecimal fat;

    @Column(nullable=false)
    private BigDecimal carbohydrate;

    @Column(nullable=false, length=30)
    private String source;

    @Column(nullable=false)
    private int frequency;

    @Column(nullable=false, length=200)
    private String seasonings;

    @Column(nullable=false, length=300)
    private String ingredients;

    @Column(nullable=false, length=200)
    private String pairings;

    public List<String> mainIngredientList() {

        return split(mainIngredients);

    }

    public List<String> methodList() {

        return split(methods);

    }

    public List<String> seasoningList() {

        return seasonings == null ? List.of() : split(seasonings);

    }

    public List<String> ingredientList() {

        return ingredients == null ? List.of() : split(ingredients);

    }

    public List<String> pairingList() {

        return pairings == null ? List.of() : split(pairings);

    }

    public boolean hasFlag(String flag) {

        return split(flags).contains(flag);

    }

    public boolean isStapleSoup() {

        return role == DishRole.SOUP && STAPLE_SOUP.matcher(name).find();

    }

    public Set<String> families() {

        if (role == DishRole.ONE_DISH) {
            return Set.of("일품:" + flags);
        }
        Set<String> families = new LinkedHashSet<>();
        for (Pattern pattern : FAMILY_PATTERNS) {
            if (pattern.matcher(name).find()) {
                families.add(pattern.pattern());
            }
        }
        Matcher focus = FOCUS_INGREDIENT.matcher(name);
        if (focus.find()) {
            families.add(focus.group().replace("달걀", "계란"));
        } else if (!mainIngredientList().isEmpty()) {
            families.add(mainIngredientList().get(0));
        } else {
            families.add(name);
        }

        return families;

    }

    public boolean matchesAny(List<String> names) {

        List<String> own = ingredientList();

        return names.stream().anyMatch(target -> own.contains(target) || name.contains(target)
                || mainIngredientList().contains(target));

    }

    public boolean isPickled() {

        return role == DishRole.PICKLE || PICKLED.matcher(name).find();

    }

    private static List<String> split(String value) {

        return value.isBlank() ? List.of() : Arrays.asList(value.split(LIST_SEPARATOR));

    }

}
