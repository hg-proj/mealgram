import json, os, re, sys, time, urllib.parse, urllib.request, collections
sys.stdout.reconfigure(encoding='utf-8')
key = os.environ.get('NICS_API_KEY')
if not key:
    sys.exit('NICS_API_KEY 환경변수가 필요합니다.')
base = 'https://api.data.go.kr/openapi/tn_pubr_public_nutri_food_info_api'
OUT = os.environ.get('FOODS_OUT', 'foods.json')


def call(page, rows):
    q = urllib.parse.urlencode({'serviceKey': key, 'type': 'json', 'pageNo': page, 'numOfRows': rows}, quote_via=urllib.parse.quote)
    for a in range(3):
        try:
            return json.loads(urllib.request.urlopen(base + '?' + q, timeout=90).read().decode('utf-8'))
        except Exception:
            time.sleep(2 + a)
    return None


foods = []
rows, page = 1000, 1
while True:
    d = call(page, rows)
    if not d:
        print('실패 page', page); break
    items = d['body']['items']
    items = items['item'] if isinstance(items, dict) else items
    if not items:
        break
    for it in items:
        foods.append({k: it.get(k) for k in ('foodNm', 'foodOriginNm', 'nutConSrtrQua', 'enerc', 'prot', 'fatce', 'chocdf', 'nat', 'foodSize', 'servSize', 'foodLv3Nm', 'typeNm', 'dataProdNm')})
    print('page', page, len(foods), flush=True)
    if len(items) < rows:
        break
    page += 1
    time.sleep(0.3)
json.dump(foods, open(OUT, 'w', encoding='utf-8'), ensure_ascii=False)
print('저장', len(foods))
print('출처 구분', collections.Counter(f['foodOriginNm'] for f in foods).most_common(8))
