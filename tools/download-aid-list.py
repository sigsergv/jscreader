#!/usr/bin/env python3

import urllib.request
import tempfile
import re
import codecs
from sys import exit

SOURCE = 'https://www.eftlab.com/index.php/site-map/knowledge-base/211-emv-aid-rid-pix'
r = urllib.request.urlopen(SOURCE)
html = r.read().decode('utf8')

# html = codecs.open('/tmp/test.html', encoding='utf8').read()

ROWS_RE = re.compile('<tr.*?>(.+?)</tr>', re.MULTILINE+re.DOTALL)
ROW_RE = re.compile('<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?', re.MULTILINE+re.DOTALL)
i = 0
for row in ROWS_RE.findall(html):
    mo = ROW_RE.search(row)  # AID, VENDOR, COUNTRY, NAME, DESCRIPTION, TYPE
    if mo is None:
        # print('Invalid row: %s' % row)
        continue
    i += 1
    items = [s.strip().replace('&amp;', '&') for s in mo.groups()]
    aid, vendor, country, name, description, t = items

    # reformat AID
    if len(aid) % 2 != 0:
        print('INVALID AID LENGTH')
        exit(123)
    paid = ' '.join( [x+y for (x,y) in zip(aid[0::2], aid[1::2])] )
    
    if description == '&nbsp;':
        description = ''
    if name == '&nbsp;':
        name = ''
    if t == '&nbsp;':
        t = ''
    print('<application aid="{aid}" type="{type}" name="{name}" description="{description}" enabled="false"/>'.format(aid=paid, description=description, name=name, type=t))
    # print(mo.groups())

print(i)

