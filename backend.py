#!/usr/bin/env python3
import cx_Oracle
con = cx_Oracle.connect('msc17pt34/msc17pt@10.1.67.153:1521/orcl')
cursor = con.cursor()
c = cursor.execute('select * from\
        (select * from questions_db order by dbms_random.value)\
        where rownum >= 1 and rownum <= 5')
for l in c.fetchall():
    print(l)

