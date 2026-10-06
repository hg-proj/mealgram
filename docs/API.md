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

도메인 기준 정리

- [인증](#인증)
- [회원](#회원)
- [재료](#재료)
- [추천](#추천)
- [레시피](#레시피)
- [저장식단](#저장식단)

### 인증
로그인, 회원가입, 비밀번호 찾기
> /auth

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /auth/login | 로그인 | loginId<br>password | body | 200<br>accessToken<br>refreshToken |
| POST | /auth/signup | 회원가입 | nickname*<br>loginId*<br>password*<br>email*<br>age<br>gender<br>height<br>weight<br>activityLevel (신체활동계수) | body | 201<br>id |
| POST | /auth/forgot-password | 비밀번호 재설정 이메일 발송 | email* | body | 200 |
| POST | /auth/reset-password | 새 비밀번호로 변경 | token* (이메일로 보낼 일회용 본인 인증값)<br>newPassword* | body | 200 |
| POST | /auth/refresh | accessToken 재발급 | refreshToken* | body | 200<br>accessToken |
| POST | /auth/logout | 로그아웃, refreshToken 무효화 | refreshToken* | body | 204 |


### 회원
회원탈퇴, 마이페이지 - 내정보
> /members/me

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /members/me | 내정보 조회 | - | - | 200<br>nickname<br>loginId<br>email<br>age<br>gender<br>height<br>weight<br>activityLevel |
| PATCH | /members/me | 내정보 수정 | nickname<br>age<br>gender<br>height<br>weight<br>activityLevel | body | 200<br>nickname<br>loginId<br>email<br>age<br>gender<br>height<br>weight<br>activityLevel |
| DELETE | /members/me | 회원탈퇴 | - | - | 204 |

### 재료
재료 입력
> /members/me/ingredients

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /ingredients | 전체 재료 검색(자동완성) | keyword<br>page, size | 쿼리 | 200<br>id<br>name |
| GET | /members/me/ingredients | 내 재료 목록 | page, size | 쿼리 | 200<br>id<br>name |
| POST | /members/me/ingredients | 내 재료 등록 | ingredientId* | body | 201<br>id |
| DELETE | /members/me/ingredients/{id} | 내 재료 삭제 | - | 경로변수 | 204 (다른 회원 소유 id면 404) |

### 추천
식단 후보 목록
> /meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /meals | 식단 추천 요청 | requiredId<br>(내 재료 중 필수재료, 우선 필터)<br>subIds[]<br>(내 재료 중 서브재료, 랭킹 가중치)<br>ingredientId<br>(주재료, 우선 필터)<br>goal<br>genre | body | 201<br>id<br>relaxed (조건을 완화했는지)<br>candidates[] (추천 식단 후보 목록) |
| GET | /meals/{id} | 특정 추천 결과 다시 조회 | - | 경로변수 | 200<br>id<br>relaxed (조건 완화 여부)<br>candidates[] |

모든 요청 값은 선택. 아무것도 보내지 않으면 랜덤 후보에서 추천

필수재료와 주재료는 우선 적용. 조건에 맞는 후보가 15개 미만이면 필수 조건을 하나씩 줄여가며 채우고, 그래도 부족하면 뜻이 비슷한 레시피, 마지막에는 랜덤 레시피로 채움. 이렇게 조건을 완화했으면 relaxed(조건 완화 여부)가 true

candidates[] 항목

| 필드 | 설명 |
| --- | --- |
| style | 식단 스타일 이름 |
| reason | 한 줄 설명 |
| recipes[] | 식단에 들어간 레시피 목록. 각 항목은 id, name, category, imageUrl(없으면 null) |

에러
- 404 조건에 맞는 레시피가 없음(레시피 자체가 하나도 없을 때)
- 404 추천 결과를 찾을 수 없음(24시간이 지나 만료되었거나 없는 id)
- 502 식단 생성 실패(LLM 호출 실패, 응답 형식 오류)


### 레시피
상세 결과
> /recipes

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /recipes/{id} | 레시피 상세 + 부족 재료 | - | 경로변수 | 200<br>id<br>name<br>category<br>cookingMethod (조리방법)<br>calorie (칼로리)<br>carbohydrate (탄수화물)<br>protein (단백질)<br>fat (지방)<br>sodium (나트륨)<br>cookingSteps (조리단계)<br>imageUrl (없으면 null, 프론트에서 기본 이미지 표시)<br>missingIngredients[] (장보기 리스트, 양념 제외)<br>seasonings[] (필요한 양념) |

calorie, carbohydrate, protein, fat, sodium은 표준 음식과 연결되지 않은 레시피에서는 null(영양 정보 없음). 영양 값은 유사한 표준 음식 기준의 추정치라 정확하지 않을 수 있음

### 저장식단
마이페이지 - 내 식단
> /saved-meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /saved-meals | 내 식단(북마크) 목록 | page, size | 쿼리 | 200<br>id<br>recipeId<br>name |
| POST | /saved-meals | 식단 북마크 저장 | recipeId* | body | 201<br>id |
| DELETE | /saved-meals/{id} | 북마크 삭제 | - | 경로변수 | 204 |

---

## 요청 데이터 전달 방식

> **요청 칸 표시**: 필드명만 나열, 전달 방식은 표시 안 함 <br>
> **판단 기준**: HTTP 메서드 + 엔드포인트 모양

<br>
 
**경로변수** `@PathVariable`
 - endpoint에 `{id}`가 **있으면** DTO 불필요. <br> `DELETE /members/me/ingredients/{id}`

<br>
 
**쿼리파라미터** `@RequestParam`
 - **GET**의 endpoint에 `{id}`가 **없으면**  DTO 불필요. <br>`GET /ingredients?keyword=`
 - 파라미터가 많아지면 `@ModelAttribute`로 **DTO에 묶어서 받기도** 함

<br>
 
**JSON body** `@RequestBody`
 - **POST/PATCH**면 필드 개수 상관없이 **DTO 필요** <br>`POST /auth/forgot-password`

<br>
 
**헤더** `@RequestHeader`
 - 인증 토큰처럼 매 요청마다 공통으로 실리는 값. DTO 불필요. <br> `Authorization: Bearer {accessToken}`
