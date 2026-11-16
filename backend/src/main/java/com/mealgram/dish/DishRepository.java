package com.mealgram.dish;

import org.springframework.data.jpa.repository.JpaRepository;

// 요리 조회 인터페이스

public interface DishRepository extends JpaRepository<Dish, Long> {

}
