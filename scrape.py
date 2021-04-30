#!/usr/bin/env python3
from bs4 import BeautifulSoup
import urllib
import re

ii = 0

def extractQuestionsAndAnswers(soup, tag):
    global ii
    op = []
    for q in soup.find_all('td', {'class' : 'bix-td-qtxt'}):
        op.append([q.get_text()])
    i = 0
    for tbl in soup.find_all('table', {'class' : 'bix-tbl-options'}):
        options = tbl.get_text().split('\n')[1:]
        for option in options[:-1]:
            op[i].append(option[:-2])
        op[i].append(options[-1])
        i += 1
    i = 0
    for xx in soup.find_all('input', {'class' : 'jq-hdnakq', 'type' : 'hidden'}):
        op[i].append(xx['value'])
        i += 1
    for qa in op:
        qa[0] = qa[0].replace('\n', ' ').replace('\r', ' ')
        print('"', ii, '","', qa[0], '"', sep='', end='')
        for u in qa[1:]:
            print(',"', u, end = '"', sep='')
        print(',"', tag, sep='', end='"')
        print()
        ii += 1

def getSoup(url):
    response = urllib.request.urlopen(url)
    html = response.read()
    soup = BeautifulSoup(html, 'html.parser')
    return soup

soup = getSoup('https://www.indiabix.com/aptitude/questions-and-answers/')
soup.find_all('div', {'class' : 'div-topics-index'})
for link in soup.find_all('a'):
    if 'aptitude' in str(link):
        m = re.search('\".*\"', str(link))
        soup_ = getSoup('https://www.indiabix.com' + m.group(0)[1:-1])
        extractQuestionsAndAnswers(soup_, m.group(0)[11:-2])
        x = soup_.find('p', {'class' : 'mx-pager mx-lpad-25'})
        if x == None:
            continue
        for tag in x.find_all('a')[:-1]:
            soup__ = getSoup('https://www.indiabix.com' + tag['href'])
            extractQuestionsAndAnswers(soup__, tag['href'][10:-7])
