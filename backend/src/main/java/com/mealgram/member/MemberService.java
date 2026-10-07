package com.mealgram.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.member.dto.MemberResponse;
import com.mealgram.member.dto.MemberUpdateRequest;

// 내정보 조회, 수정 서비스

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long memberId) {

        return MemberResponse.from(find(memberId));

    }

    @Transactional
    public MemberResponse update(Long memberId, MemberUpdateRequest request) {

        Member member = find(memberId);
        member.updateProfile(request.nickname(), request.age(), request.gender(), request.height(),
                request.weight(), request.activityLevel());

        return MemberResponse.from(member);

    }

    private Member find(Long memberId) {

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    }

}
