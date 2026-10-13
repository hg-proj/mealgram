package com.mealgram.common.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;

class SortValidatorTest {

    private static final Set<String> ALLOWED = Set.of("id", "name");

    @Test
    @DisplayName("허용한 필드로만 정렬하면 통과한다.")
    void allowsListedProperties() {

        assertDoesNotThrow(() -> SortValidator.require(PageRequest.of(0, 20, Sort.by("name")), ALLOWED));
        assertDoesNotThrow(() -> SortValidator.require(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")), ALLOWED));
        assertDoesNotThrow(() -> SortValidator.require(PageRequest.of(0, 20, Sort.by("name", "id")), ALLOWED));

    }

    @Test
    @DisplayName("정렬을 보내지 않으면 통과한다.")
    void allowsUnsorted() {

        assertDoesNotThrow(() -> SortValidator.require(PageRequest.of(0, 20), ALLOWED));

    }

    @Test
    @DisplayName("허용하지 않은 필드가 하나라도 있으면 400 오류로 알린다.")
    void rejectsUnknownProperty() {

        BusinessException e = assertThrows(BusinessException.class,
                () -> SortValidator.require(PageRequest.of(0, 20, Sort.by("password")), ALLOWED));
        assertEquals(ErrorCode.INVALID_REQUEST, e.getErrorCode());

        assertThrows(BusinessException.class,
                () -> SortValidator.require(PageRequest.of(0, 20, Sort.by("name", "password")), ALLOWED));

    }

}
