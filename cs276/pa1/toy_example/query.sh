SCRIPTPATH=$( cd $(dirname $0) ; pwd -P )
java -Xmx200M -cp classes cs276.assignments.Query VB $1
