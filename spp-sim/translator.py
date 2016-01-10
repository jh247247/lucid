#!/usr/bin/python

import sys
import math
import struct
import array
import base64
import datetime
import time


def convertSample(line):
    # convert line to numbers
    tmp = array.array('h', [int(x) for x in line.rstrip().split(",")])
    l = len(tmp)
    tmp = ''.join([struct.pack(">H",int(x)) for x in tmp])

    nl = base64.standard_b64encode(bytes(tmp))

    # find out header for line type,start,end,bytesPerSample
    ret = ['\x00', '\x00\x00', struct.pack(">H",l), struct.pack("b",2)]
    # add data we have
    ret.extend(nl)
    return ''.join(ret)

def convertTimeStamp(line):
    nl = line.rstrip()[-8:].split(":")
    nl = [int(x) for x in nl]
    timestamp = time.mktime(datetime.datetime(2013,2,9,nl[0],nl[1],nl[2]).timetuple())
    ret = ['\x01', struct.pack(">q",long(timestamp))]
    # format should now be type,y,m,d,h,m,s

    return ''.join(ret)


def lineType(line):
    c = line[0]
    try:
        int(c)
        return 0
    except:
        return 1

def main(argv):
    try:
        file = argv[0]
    except:
        print("Need file to translate!")
        sys.exit(2)
    if(file is None):
        sys.exit(2)


    numSamp = 0
    oldTime = None
    with open(file) as inF, open(file + ".gpr","wb") as outF:
        for line in inF:
            if(lineType(line) is 0):
                outF.write(convertSample(line))
                numSamp += 1
            elif(lineType(line) is 1):
                newTime = convertTimeStamp(line)
                if(oldTime != newTime):
                    outF.write(newTime)
                    oldTime = newTime

    print("DONE")
    print("Number of samples: " + str(numSamp))


if __name__ == '__main__':
    main(sys.argv[1:])
