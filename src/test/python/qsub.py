#!/usr/bin/env python

import argparse

parser = argparse.ArgumentParser(description='Mock qsub')
parser.add_argument('-q', help='queue')
parser.add_argument('-o', help='output')
parser.add_argument('-e', help='error')
parser.add_argument('script', help='script')

args = parser.parse_args()

print args


