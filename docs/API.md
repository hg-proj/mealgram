# mealgram API 명세

Swagger UI: `/swagger-ui.html`, OpenAPI 문서: `/v3/api-docs`

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
| 상세 결과 | 추천 |
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
- [장보기](#장보기)
- [저장식단](#저장식단)

### 인증
로그인, 회원가입, 비밀번호 찾기
> /api/auth

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /api/auth/login | 로그인 | loginId<br>password | body | 200<br>accessToken<br>refreshToken |
| POST | /api/auth/signup | 회원가입 | nickname*<br>loginId*<br>password*<br>email*<br>birthDate (yyyy-MM-dd, 만 3세 이상 120세 이하)<br>gender<br>height<br>weight<br>activityLevel (신체활동계수) | body | 201<br>id |
| POST | /api/auth/forgot-password | 비밀번호 재설정 인증번호 메일 발송, 숫자 6자리, 10분 유효, 같은 회원은 60초 간격, 가입되지 않은 이메일은 404, 메일을 보내지 못하면 502 | email* | body | 200 |
| POST | /api/auth/verify-reset-code | 인증번호 확인, 이메일별 5번 틀리면 10분 동안 429 | email*<br>code* (숫자 6자리) | body | 200<br>resetToken (10분 유효) |
| POST | /api/auth/reset-password | 새 비밀번호로 변경, 인증번호 확인으로 받은 resetToken은 한 번만 사용 | token* (resetToken)<br>newPassword* | body | 200 |
| POST | /api/auth/refresh | accessToken 재발급 | refreshToken* | body | 200<br>accessToken |
| POST | /api/auth/logout | 로그아웃, refreshToken 무효화 | refreshToken* | body | 204 |


### 회원
회원탈퇴, 마이페이지 - 내정보
> /api/members/me

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/members/me | 내정보 조회 | - | - | 200<br>nickname<br>loginId<br>email<br>birthDate<br>gender<br>height<br>weight<br>activityLevel |
| PATCH | /api/members/me | 내정보 수정 | nickname<br>birthDate<br>gender<br>height<br>weight<br>activityLevel | body | 200<br>nickname<br>loginId<br>email<br>birthDate<br>gender<br>height<br>weight<br>activityLevel |
| DELETE | /api/members/me | 회원탈퇴 | - | - | 204 |

### 재료
재료 입력
> /api/members/me/ingredients

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/ingredients | 전체 재료 검색(자동완성), 후식을 뺀 요리의 재료 목록에 3번 이상 쓰인 재료만, 로그인 없이 가능 | keyword 50자 이하, 넘으면 400<br>page, size | 쿼리 | 200<br>id<br>name |
| GET | /api/ingredients/catalog | 선택할 수 있는 재료를 분류별로 조회, 후식을 뺀 요리의 재료 목록에 3번 이상 쓰인 재료만, 로그인 없이 가능 | - | - | 200<br>category 분류 이름<br>ingredients[] id, name |
| GET | /api/members/me/ingredients | 내 재료 목록 | page, size | 쿼리 | 200<br>id (내 재료 번호, 삭제할 때 사용)<br>ingredientId (재료 id, 식단 추천 요청에 사용)<br>name |
| POST | /api/members/me/ingredients | 내 재료 등록 | ingredientId* | body | 201<br>id |
| DELETE | /api/members/me/ingredients/{id} | 내 재료 삭제 | - | 경로변수 | 204 (다른 회원 소유 id면 404) |

### 추천
식단 후보 목록
> /api/meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /api/meals | 식단 추천 요청, 로그인 없이 가능 | requiredId<br>subIds[]<br>ingredientId<br>goal 다이어트, 벌크업, 유지, 랜덤 중 하나, 랜덤이면 후보마다 목표를 다르게 맞춤<br>style 균형, 고단백, 저탄수, 저지방 중 하나, 비우면 균형<br>genre 한식, 양식을 쉼표로 이어서 보냄, 한식이 아닌 장르는 그 장르 요리를 반찬에 넣으려고 함<br>birthDate, gender, height, weight, activityLevel 비회원만 사용, 비우면 기본 열량으로 계산 | body | 201<br>id 비회원은 null<br>relaxed<br>candidates[] 항상 3개 |
| GET | /api/meals/samples | 랜덤 식단 3개, 로그인 없이 조회 | - | - | 200<br>id 저장할 때 mealId로 씀, 24시간 보관<br>goal 다이어트, 유지, 벌크업 순서<br>genre 식단의 장르 하나<br>title<br>dishes[] id, name, category, imageUrl, grams, calorie<br>nutrition 추천 결과의 nutrition과 같음 |
| GET | /api/meals/{id} | 특정 추천 결과 다시 조회, 회원만 | - | 경로변수 | 200<br>id<br>relaxed (조건 완화 여부)<br>candidates[] |

일품(면, 덮밥 등)이 든 식단은 일품 하나와 어울리는 곁들임으로 구성하며 후보 세 개 중 한 개 정도만 나옵니다. 세 후보의 메인 요리는 서로 비슷하지 않게 고릅니다.

영양 값은 일부가 추정치이며, 추정치가 든 식단은 nutrition.verified가 false입니다.

candidates[] 항목

| 필드 | 설명 |
| --- | --- |
| style | 식단 스타일 이름 |
| goal | 이 식단을 맞춘 목표 |
| genre | 식단의 장르 하나, 요청에서 고른 장르의 요리가 들었으면 그 장르, 아니면 든 요리 중 가장 많은 장르, 개수가 같으면 한식 |
| dishes[] | 식단에 들어간 요리 목록. 각 항목은 id, name, category(일품, 밥, 국, 메인 반찬, 곁들임 반찬, 김치와 절임, 후식), imageUrl(없으면 null), grams(분량), calorie(분량 기준 열량), ingredients[](양념을 뺀 재료 이름), seasonings[](장보기에만 쓰는 특수 양념, 재료 선택에는 나오지 않음) |
| nutrition | calorie, carbohydrate, protein, fat 합계<br>carbohydrateRatio, proteinRatio, fatRatio 에너지 비율 %<br>targetCalorie 한 끼 목표 열량<br>verified 검증 여부, passed 기준 만족 여부, issues[] 기준에서 벗어난 점<br>영양 정보가 없는 요리가 있으면 합계와 비율은 null, verified는 false |


### 장보기
식단의 장보기 목록
> /api/shopping-list

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | /api/shopping-list | 식단의 요리들로 장보기 목록 계산, 로그인 없이 가능 | dishIds[]* 식단에 들어간 요리 id, 1~30개<br>ownedIngredientIds[] 이미 가진 재료 id, 회원은 내 재료가 자동으로 더해짐 | body | 200<br>toBuy[] 사야 할 재료<br>have[] 이미 가진 재료<br>seasonings[] 확인할 특수 양념(집에 보통 있는 양념은 없음)<br>각 항목은 name, category, usedIn[](쓰이는 요리 이름)<br>같은 재료는 한 줄로 합치고 분류 순서, 이름순으로 정렬<br>404 없는 요리나 재료 id |

### 저장식단
마이페이지 - 내 식단
> /api/saved-meals

| 메서드 | 엔드포인트 | 설명 | 요청 | 요청 위치 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | /api/saved-meals | 내 식단 목록, 최신순 | page, size, sort id 또는 createdAt | 쿼리 | 200<br>id<br>style<br>dishes[] 추천 결과의 dishes와 같음<br>nutrition 추천 결과의 nutrition과 같음<br>context 저장할 때 보낸 추천 조건, 없으면 null<br>createdAt |
| POST | /api/saved-meals | 추천받은 식단 한 상 저장 | mealId* 추천 결과 id 또는 샘플 id<br>candidateIndex* 0~2, 샘플은 0<br>context 추천 조건, goal, style, genre, requiredName, ownedNames[] | body | 201<br>id<br>404 내 추천 결과가 아니거나 만료됨, 409 이미 저장함 |
| DELETE | /api/saved-meals/{id} | 저장한 식단 삭제 | - | 경로변수 | 204 (다른 회원 소유 id면 404) |
