FILENAME=$1

find . -name "${FILENAME}.java" -exec git diff 4.3.7 v5.0.0 -- $1 {} \;

