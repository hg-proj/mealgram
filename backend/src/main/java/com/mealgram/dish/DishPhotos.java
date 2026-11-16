package com.mealgram.dish;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

// 요리 사진 주소 조회

@Component
public class DishPhotos {

    private static final String IMAGE_PATH = "static/images/dishes/%s.webp";
    private static final String IMAGE_URL = "/images/dishes/%s.webp";

    private final Map<String, Boolean> exists = new ConcurrentHashMap<>();

    public String urlOf(Dish dish) {

        String group = dish.getImageGroup();
        if (group == null || group.isBlank()) {
            return null;
        }

        boolean found = exists.computeIfAbsent(group, key -> new ClassPathResource(IMAGE_PATH.formatted(key)).exists());

        return found ? IMAGE_URL.formatted(group) : null;

    }

}
