package com.mealgram.common.validation;

import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;

// 정렬 요청 필드 검증

public final class SortValidator {

    private SortValidator() {
    }

    public static void require(Pageable pageable, Set<String> allowedProperties) {

        boolean allowed = pageable.getSort().stream().allMatch(order -> allowedProperties.contains(order.getProperty()));
        if (!allowed) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

    }

}
