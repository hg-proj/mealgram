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

### 인증
로그인, 회원가입, 비밀번호 찾기
> /auth

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| POST | /auth/login | 로그인 | loginId<br>password | 200<br>accessToken<br>refreshToken |
| POST | /auth/signup | 회원가입 | nickname*<br>loginId*<br>password*<br>email*<br>age<br>gender<br>height<br>weight<br>activityLevel (신체활동계수) | 201<br>id |
| POST | /auth/forgot-password | 비밀번호 재설정 이메일 발송 | email* | 200 |
| POST | /auth/reset-password | 새 비밀번호로 변경 | token* (이메일로 보낼 일회용 본인 인증값)<br>newPassword* | 200 |


### 회원
회원탈퇴, 마이페이지 - 내정보
> /members/me

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| GET | /members/me | 내정보 조회 | - | 200<br>nickname<br>loginId<br>email<br>age<br>gender<br>height<br>weight<br>activityLevel |
| PATCH | /members/me | 내정보 수정 | nickname<br>age<br>gender<br>height<br>weight<br>activityLevel | 200 |
| DELETE | /members/me | 회원탈퇴 | - | 204 |

### 재료
재료 입력
> /members/me/ingredients

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| GET | /ingredients | 전체 재료 검색(자동완성) | keyword | 200<br>id<br>name |
| GET | /members/me/ingredients | 내 재료 목록 | - | 200<br>id<br>name |
| POST | /members/me/ingredients | 내 재료 등록 | ingredientId* | 201<br>id |
| DELETE | /members/me/ingredients/{id} | 내 재료 삭제 | - | 204 |

### 추천
식단 후보 목록
> /meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| POST | /meals | 식단 추천 요청 | requiredId<br>(내 재료 중 필수재료, 하드 필터)<br>subIds[]<br>(내 재료 중 서브재료, 랭킹 가중치)<br>ingredientId<br>(주재료, 하드 필터)<br>goal<br>genre | 201<br>id<br>candidates[] (추천 식단 후보 목록) |
| GET | /meals/{id} | 특정 추천 결과 다시 조회 | - | 200<br>id<br>candidates[] |


### 레시피
상세 결과
> /recipes

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| GET | /recipes/{id} | 레시피 상세 + 부족 재료 | - | 200<br>id<br>name<br>category<br>cookingMethod (조리방법)<br>calorie (칼로리)<br>carbohydrate (탄수화물)<br>protein (단백질)<br>fat (지방)<br>sodium (나트륨)<br>cookingSteps (조리단계)<br>imageUrl<br>missingIngredients[] (장보기 리스트) |

### 저장식단
마이페이지 - 내 식단
> /saved-meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 응답 |
| --- | --- | --- | --- | --- |
| GET | /saved-meals | 내 식단(북마크) 목록 | - | 200<br>id<br>recipeId<br>name |
| POST | /saved-meals | 식단 북마크 저장 | recipeId* | 201<br>id |
| DELETE | /saved-meals/{id} | 북마크 삭제 | - | 204 |
