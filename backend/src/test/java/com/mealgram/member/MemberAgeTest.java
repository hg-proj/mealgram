package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberAgeTest {

    private Member bornOn(LocalDate birthDate) {

        return Member.builder().birthDate(birthDate).build();

    }

    @Test
    @DisplayName("생일 전날까지는 만 나이에서 한 살을 뺀다.")
    void subtractsOneBeforeBirthday() {

        Member member = bornOn(LocalDate.of(2000, 6, 15));

        assertEquals(25, member.ageOn(LocalDate.of(2026, 6, 14)));
        assertEquals(26, member.ageOn(LocalDate.of(2026, 6, 15)));
        assertEquals(26, member.ageOn(LocalDate.of(2026, 12, 31)));

    }

    @Test
    @DisplayName("윤일에 태어난 경우도 만 나이를 계산한다.")
    void countsLeapDayBirth() {

        Member member = bornOn(LocalDate.of(2000, 2, 29));

        assertEquals(25, member.ageOn(LocalDate.of(2026, 2, 28)));
        assertEquals(26, member.ageOn(LocalDate.of(2026, 3, 1)));

    }

    @Test
    @DisplayName("생년월일이 없으면 나이는 비어 있다.")
    void nullWithoutBirthDate() {

        assertNull(bornOn(null).ageOn(LocalDate.of(2026, 6, 14)));

    }

}
