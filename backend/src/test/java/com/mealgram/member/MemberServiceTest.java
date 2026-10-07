package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;
import com.mealgram.member.dto.MemberResponse;
import com.mealgram.member.dto.MemberUpdateRequest;

class MemberServiceTest {

    private MemberRepository memberRepository;
    private MemberService memberService;
    private Member member;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        memberService = new MemberService(memberRepository);
        member = Member.builder().nickname("한결").loginId("hangyeol01").email("test@example.com").age(30)
                .gender(Gender.FEMALE).height(new BigDecimal("160")).weight(new BigDecimal("55"))
                .activityLevel(ActivityLevel.LIGHT).build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    }

    @Test
    @DisplayName("내정보를 조회한다.")
    void getsMember() {

        MemberResponse response = memberService.get(1L);

        assertEquals("한결", response.nickname());
        assertEquals("hangyeol01", response.loginId());
        assertEquals(ActivityLevel.LIGHT, response.activityLevel());

    }

    @Test
    @DisplayName("보낸 값만 수정하고 나머지는 그대로 둔다.")
    void updatesOnlyGivenFields() {

        MemberResponse response = memberService.update(1L,
                new MemberUpdateRequest(null, 31, null, null, new BigDecimal("53.5"), ActivityLevel.MODERATE));

        assertEquals("한결", response.nickname());
        assertEquals(31, response.age());
        assertEquals(Gender.FEMALE, response.gender());
        assertEquals(new BigDecimal("160"), response.height());
        assertEquals(new BigDecimal("53.5"), response.weight());
        assertEquals(ActivityLevel.MODERATE, response.activityLevel());

    }

    @Test
    @DisplayName("없는 회원이면 조회와 수정에 실패한다.")
    void failsWhenMemberDoesNotExist() {

        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> memberService.get(2L));
        assertThrows(BusinessException.class,
                () -> memberService.update(2L, new MemberUpdateRequest("새이름", null, null, null, null, null)));

    }

}
