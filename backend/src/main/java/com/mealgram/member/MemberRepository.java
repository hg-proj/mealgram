package com.mealgram.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// member 조회/저장 인터페이스

public interface MemberRepository extends JpaRepository<Member, Long>{
    Optional<Member> findByLoginId(String loginId);
}
