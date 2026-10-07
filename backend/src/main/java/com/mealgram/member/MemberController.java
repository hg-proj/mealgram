package com.mealgram.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.member.dto.MemberResponse;
import com.mealgram.member.dto.MemberUpdateRequest;

import jakarta.validation.Valid;

// 내정보 조회, 수정, 회원탈퇴 API 엔드포인트

@RestController
@RequestMapping("/members/me")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<MemberResponse> get(@AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(memberService.get(memberId));

    }

    @PatchMapping
    public ResponseEntity<MemberResponse> update(@AuthenticationPrincipal Long memberId,
                                                 @Valid @RequestBody MemberUpdateRequest request) {

        return ResponseEntity.ok(memberService.update(memberId, request));

    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId) {

        memberService.delete(memberId);

        return ResponseEntity.noContent().build();

    }

}
