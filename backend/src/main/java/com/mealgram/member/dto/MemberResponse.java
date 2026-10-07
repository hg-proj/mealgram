package com.mealgram.member.dto;

import java.math.BigDecimal;

import com.mealgram.member.Member;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

// 내정보 응답 데이터

public record MemberResponse(String nickname,
                             String loginId,
                             String email,
                             Integer age,
                             Gender gender,
                             BigDecimal height,
                             BigDecimal weight,
                             ActivityLevel activityLevel) {

    public static MemberResponse from(Member member) {

        return new MemberResponse(member.getNickname(), member.getLoginId(), member.getEmail(), member.getAge(),
                member.getGender(), member.getHeight(), member.getWeight(), member.getActivityLevel());

    }

}
