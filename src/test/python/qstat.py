#!/usr/bin/env python

with open ("src/test/resources/qstat.xml") as qstat:
    for line in qstat:
        print line.strip()

