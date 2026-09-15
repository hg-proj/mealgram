# mealgram 컨벤션

### 네이밍 케이스

| 케이스 | 대상 | 예시 |
| --- | --- | --- |
| camelCase | 변수, 메서드, API 필드 | `loginId` |
| PascalCase | 클래스명, 파일명 | `LoginService` |
| snake_case | DB 테이블, 컬럼명 | `login_id` |
| kebab-case | API 엔드포인트, Git 브랜치명 | `/auth/login` |
| UPPER_SNAKE_CASE | 상수 | `MAX_LOGIN_ATTEMPT` |

---

### 공통 응답, 에러 규칙

| 항목 | 규칙 | 예시 |
| --- | --- | --- |
| 성공 응답 | `{ "success": true,`<br>`"data" }` 형태 | `{ "success": true,`<br>`"data": {...}}` |
| 실패 응답 | `{ "success": false, `<br>`"error": { "code":"", `<br>`"message":"" }}` 형태, <br>code는 직접 정의한 에러 이름 | `{ "success": false,`<br>`"error": { "code": "MEMBER_NOT_FOUND",`<br>`"message": "..." }}` |
| 인증 헤더 | Bearer 토큰 | `Authorization: Bearer {accessToken}` |
| 목록 조회 페이지네이션 | `page`, `size` 쿼리파라미터 | `/ingredients?page=0&size=20` |

- Bearer 토큰 사용 이유: REST API + 프론트/백엔드 분리 구조에 가장 잘 맞는 표준 방식이라 선택
- 페이지네이션 사용 이유: Spring Data JPA가 page, size를 기본 지원해서 직접 구현할 필요 없음


---

### 커밋 컨벤션

| 타입 | 설명 |
| --- | --- |
| feat | 새 기능 추가 |
| fix | 버그 수정 |
| refactor | 동작 변화 없는 코드 구조 개선 |
| test | 테스트 코드 추가, 수정 |
| docs | README, 문서 작업 |
| chore | 설정, 빌드 등 보조 작업 |
| style | 코드 포맷팅 |
| perf | 성능 개선 |

- **Commit:** `타입: 설명`, 50자 이내 `feat: 로그인 API 추가`
- **Branch:** `타입/키워드`, kebab-case 사용 `feature/login-api`
- **Merge:** Squash and merge 사용
