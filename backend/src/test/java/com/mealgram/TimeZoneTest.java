package com.mealgram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeZoneTest {

    private TimeZone original;

    @BeforeEach
    void setUp() {

        original = TimeZone.getDefault();

    }

    @AfterEach
    void tearDown() {

        TimeZone.setDefault(original);

    }

    @Test
    @DisplayName("서버 시간대를 Asia/Seoul로 고정한다.")
    void fixesDefaultTimeZoneToSeoul() {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        MealgramApplication.fixTimeZone();

        assertEquals("Asia/Seoul", TimeZone.getDefault().getID());
        assertEquals(ZoneId.of("Asia/Seoul"), ZoneId.systemDefault());

    }

}
