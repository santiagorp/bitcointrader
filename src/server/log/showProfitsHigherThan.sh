#!/bin/sh
minProfit=$1
awkFilter='{if ($14 > '$minProfit') print $1, $2, $14}'
grep Profitable | sed -e s/%//g -e s/,//g | awk "$awkFilter"
