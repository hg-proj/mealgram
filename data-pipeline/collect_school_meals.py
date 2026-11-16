"""고등학교 급식 식단 수집, 인증키는 NEIS_API_KEY 환경변수에서 읽음"""
import json, os, random, sys, time, urllib.parse, urllib.request

B = 'https://open.neis.go.kr/hub/'
OUT = os.environ.get('MEALS_OUT', 'meals_hs.json')
SCHOOLS = 300
FROM, TO = '20250301', '20251231'

KEY = os.environ.get('NEIS_API_KEY')
if not KEY:
    sys.exit('NEIS_API_KEY 환경변수가 필요합니다.')


def get(api, **params):
    params.update(KEY=KEY, Type='json')
    url = B + api + '?' + urllib.parse.urlencode(params)
    for attempt in range(3):
        try:
            with urllib.request.urlopen(url, timeout=40) as r:
                return json.loads(r.read().decode('utf-8'))
        except Exception:
            time.sleep(1 + attempt)
    return {}


def rows(data, key):
    for part in data.get(key, []):
        if 'row' in part:
            return part['row']
    return []


# 고등학교 전체 목록
schools = []
page = 1
while True:
    got = rows(get('schoolInfo', pIndex=page, pSize=1000, SCHUL_KND_SC_NM='고등학교'), 'schoolInfo')
    schools += [(r['ATPT_OFCDC_SC_CODE'], r['SD_SCHUL_CODE'], r['SCHUL_NM']) for r in got]
    if len(got) < 1000:
        break
    page += 1
print('고등학교', len(schools), flush=True)

random.seed(7)
picked = random.sample(schools, min(SCHOOLS, len(schools)))

meals = []
for n, (office, code, name) in enumerate(picked, 1):
    data = get('mealServiceDietInfo', pIndex=1, pSize=1000, ATPT_OFCDC_SC_CODE=office, SD_SCHUL_CODE=code,
               MMEAL_SC_CODE=2, MLSV_FROM_YMD=FROM, MLSV_TO_YMD=TO)
    for r in rows(data, 'mealServiceDietInfo'):
        meals.append({'s': code, 'd': r['MLSV_YMD'], 'dish': r['DDISH_NM'], 'cal': r.get('CAL_INFO'), 'ntr': r.get('NTR_INFO')})
    if n % 25 == 0:
        print(n, '학교', len(meals), '건', flush=True)
    time.sleep(0.1)

json.dump(meals, open(OUT, 'w', encoding='utf-8'), ensure_ascii=False)
print('완료', len(meals), flush=True)
