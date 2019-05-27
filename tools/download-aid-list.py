#!/usr/bin/env python3

import urllib.request
import tempfile
import re
from sys import exit

SOURCE = 'https://www.eftlab.com/index.php/site-map/knowledge-base/211-emv-aid-rid-pix'
# r = urllib.request.urlopen(SOURCE)
# html = r.read()

html = open('/tmp/test.html').read()

ROWS_RE = re.compile('<tr.*?>(.+?)</tr>', re.MULTILINE+re.DOTALL)
ROW_RE = re.compile('<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?<td.*?>(.+?)</td>.*?', re.MULTILINE+re.DOTALL)
i = 0
for row in ROWS_RE.findall(html):
    mo = ROW_RE.search(row)  # AID, VENDOR, COUNTRY, NAME, DESCRIPTION, TYPE
    if mo is None:
        # print('Invalid row: %s' % row)
        continue
    i += 1
    aid = mo.group(1)
    # reformat AID
    if len(aid) % 2 != 0:
        print('INVALID AID LENGTH')
        exit(123)
    paid = ' '.join( [x+y for (x,y) in zip(aid[0::2], aid[1::2])] )
    

    print('<application aid="{aid}" type="" name=""/>'.format(aid=paid))
    # print(mo.groups())

print(i)

