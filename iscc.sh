#!/bin/bash
unset DISPLAY
scriptname="$1"
[ -f "$scriptname" ] && scriptname=$(winepath -w "$scriptname")
wine "C:\\innosetup\\ISCC.exe" "$scriptname"
