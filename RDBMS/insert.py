#!/usr/bin/env python3
import cx_Oracle
import unicodedata
con = cx_Oracle.connect('msc17pt34/msc17pt@10.1.67.153:1521/orcl')
cursor = con.cursor()
f = open("qa.csv")
ll = f.readlines()
i = 0
for l in ll:
    l = unicodedata.normalize('NFKC', l)
    l = l.replace(u'\xf7', u'/')
    l = l.split('","')
    i += 1
    try:
        cursor.execute('insert into questions_db values(' + l[0][1:] + ",'" + l[1] + "','" + l[2] + "','" + l[3] + "','" + l[4] + "','" + l[5] + "','" + l[6] + "','" + l[7][:-1] + "')")
        print('insert into questions_db values(' + l[0][1:] + ",'" + l[1] + "','" + l[2] + "','" + l[3] + "','" + l[4] + "','" + l[5] + "','" + l[6] + "','" + l[7][:-1] + "')")
    except cx_Oracle.DatabaseError:
#        print(i, cursor.statement)
        continue
    except IndexError:
#        print(i, l)
        continue
