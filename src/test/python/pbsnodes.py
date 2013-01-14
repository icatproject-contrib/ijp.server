#!/usr/bin/env python

import argparse

parser = argparse.ArgumentParser(description='Mock pbsnodes')
parser.add_argument('-x', help='output xml', action='store_true')
parser.add_argument('-o', help='make offline', nargs='+')
parser.add_argument('-c', help='clear offline', nargs='+')

args = parser.parse_args()

if args.x:
    print "<Data><Node><name>sig-04.esc.rl.ac.uk</name><state>down</state><np>1</np><ntype>cluster</ntype><gpus>0</gpus></Node><Node><name>sig-05.esc.rl.ac.uk</name><state>job-exclusive</state><np>1</np><ntype>cluster</ntype><jobs>0/127.sig-06.esc.rl.ac.uk</jobs><status>rectime=1339409641,varattr=,jobs=,state=free,netload=6421183549,gres=,loadave=0.00,ncpus=1,physmem=4060600kb,availmem=5582360kb,totmem=5824944kb,idletime=330659,nusers=3,nsessions=7,sessions=1101 1171 1172 1178 1204 12094 12198,uname=Linux sig-05 2.6.32-40-generic #87-Ubuntu SMP Tue Mar 6 00:56:56 UTC 2012 x86_64,opsys=linux</status><gpus>0</gpus></Node><Node><name>sig-06.esc.rl.ac.uk</name><state>free</state><np>4</np><ntype>cluster</ntype><jobs>0/128.sig-06.esc.rl.ac.uk, 1/129.sig-06.esc.rl.ac.uk</jobs><status>rectime=1339409668,varattr=,jobs=111.sig-06.esc.rl.ac.uk 120.sig-06.esc.rl.ac.uk 121.sig-06.esc.rl.ac.uk 122.sig-06.esc.rl.ac.uk 123.sig-06.esc.rl.ac.uk,state=free,netload=6547252858,gres=,loadave=0.00,ncpus=1,physmem=4060600kb,availmem=4795008kb,totmem=5824944kb,idletime=196,nusers=3,nsessions=12,sessions=1026 1030 1031 857 1043 1068 7978 8698 8708 8718 8728 26432,uname=Linux sig-06 2.6.32-38-generic #83-Ubuntu SMP Wed Jan 4 11:12:07 UTC 2012 x86_64,opsys=linux</status><gpus>0</gpus></Node></Data>"
 
