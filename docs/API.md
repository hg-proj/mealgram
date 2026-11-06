# mealgram API 명세

## 화면 목록

**필수**

| 화면 | API 도메인 |
| --- | --- |
| 로그인 | 인증 |
| 회원가입 | 인증 |
| 비밀번호 찾기 | 인증 |
| 회원탈퇴 | 회원 |
| 메인 | 없음. 버튼 라우팅만 |
| 재료 입력 | 재료 |
| 식단 후보 목록 | 추천 |
| 상세 결과 | 레시피 |
| 마이페이지 → 내정보 | 회원 |
| 마이페이지 → 내식단 | 저장식단 |
| 에러 화면 | 없음. 다른 API 응답 상태코드로 렌더링 |

---

## API 목록

도메인 기준 정리, 모든 엔드포인트는 /api로 시작

- [인증](#인증)
- [회원](#회원)
- [재료](#재료)
- [추천](#추천)
- [레시피](#레시피)
- [저장식단](#저장식단)

### 인증
로그인, 회원가입, 비밀번호 찾기
> /api/auth

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /api/auth/login | 로그인 | loginId<br>password | body | 200<br>accessToken<br>refreshToken |
| POST | /api/auth/signup | 회원가입 | nickname*<br>loginId*<br>password*<br>email*<br>age<br>gender<br>height<br>weight<br>activityLevel (신체활동계수) | body | 201<br>id |
| POST | /api/auth/forgot-password | 비밀번호 재설정 이메일 발송, 가입 여부와 관계없이 200 | email* | body | 200 |
| POST | /api/auth/reset-password | 새 비밀번호로 변경 | token* (이메일로 보낼 일회용 본인 인증값)<br>newPassword* | body | 200 |
| POST | /api/auth/refresh | accessToken 재발급 | refreshToken* | body | 200<br>accessToken |
| POST | /api/auth/logout | 로그아웃, refreshToken 무효화 | refreshToken* | body | 204 |


### 회원
회원탈퇴, 마이페이지 - 내정보
> /api/members/me

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/members/me | 내정보 조회 | - | - | 200<br>nickname<br>loginId<br>email<br>age<br>gender<br>height<br>weight<br>activityLevel |
| PATCH | /api/members/me | 내정보 수정 | nickname<br>age<br>gender<br>height<br>weight<br>activityLevel | body | 200<br>nickname<br>loginId<br>email<br>age<br>gender<br>height<br>weight<br>activityLevel |
| DELETE | /api/members/me | 회원탈퇴 | - | - | 204 |

### 재료
재료 입력
> /api/members/me/ingredients

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/ingredients | 전체 재료 검색(자동완성) | keyword<br>page, size | 쿼리 | 200<br>id<br>name |
| GET | /api/members/me/ingredients | 내 재료 목록 | page, size | 쿼리 | 200<br>id (내 재료 번호, 삭제할 때 사용)<br>ingredientId (재료 id, 식단 추천 요청에 사용)<br>name |
| POST | /api/members/me/ingredients | 내 재료 등록 | ingredientId* | body | 201<br>id |
| DELETE | /api/members/me/ingredients/{id} | 내 재료 삭제 | - | 경로변수 | 204 (다른 회원 소유 id면 404) |

### 추천
식단 후보 목록
> /api/meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /api/meals | 식단 추천 요청 | requiredId<br>subIds[]<br>ingredientId<br>goal 다이어트, 벌크업, 유지 중 하나<br>genre | body | 201<br>id<br>relaxed<br>candidates[] 항상 3개 |
| GET | /api/meals/samples | 랜덤 식단 3개, 로그인 없이 조회 | - | - | 200<br>goal 다이어트, 유지, 벌크업 순서<br>title<br>recipes[] id, name, category, imageUrl, portion<br>nutrition 추천 결과의 nutrition과 같음 |
| GET | /api/meals/{id} | 특정 추천 결과 다시 조회 | - | 경로변수 | 200<br>id<br>relaxed (조건 완화 여부)<br>candidates[] |

candidates[] 항목

| 필드 | 설명 |
| --- | --- |
| style | 식단 스타일 이름 |
| reason | 한 줄 설명 |
| recipes[] | 식단에 들어간 레시피 목록. 각 항목은 id, name, category, imageUrl(없으면 null), portion(분량 배수, 목표 열량에 맞춰 0.5~1.2) |
| nutrition | calorie, carbohydrate, protein, fat 합계<br>carbohydrateRatio, proteinRatio, fatRatio 에너지 비율 %<br>targetCalorie 한 끼 목표 열량<br>verified 검증 여부, passed 기준 만족 여부, issues[] 기준에서 벗어난 점<br>영양 정보가 없는 요리가 있으면 합계와 비율은 null, verified는 false |


### 레시피
상세 결과
> /api/recipes

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/recipes/{id} | 레시피 상세와 부족 재료 | - | 경로변수 | 200<br>id, name, category, cookingMethod<br>calorie, carbohydrate, protein, fat, sodium 영양 정보가 없으면 null<br>cookingSteps<br>imageUrl /images/recipes/{id}.webp, 없으면 null<br>missingIngredients[] 양념 제외한 부족 재료<br>seasonings[] 필요한 양념 |

### 저장식단
마이페이지 - 내 식단
> /api/saved-meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/saved-meals | 내 식단(북마크) 목록 | page, size | 쿼리 | 200<br>id<br>recipeId<br>name |
| POST | /api/saved-meals | 식단 북마크 저장 | recipeId* | body | 201<br>id |
| DELETE | /api/saved-meals/{id} | 북마크 삭제 | - | 경로변수 | 204 |
