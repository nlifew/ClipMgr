
#!/bin/sh
module_id=$1
module_name=$2
module_author=$3
module_description=$4
module_versionName=$5
module_versionCode=$6
module_riru_api=$7

HOME=`dirname $0'
AAR=$HOME/build/outputs/aar/${HOME##*/}-release.aar
