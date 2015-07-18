#!/bin/bash

sdptool add --channel=1 sp
sudo rfcomm listen /dev/rfcomm0 1 &
