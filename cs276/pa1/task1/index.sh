SCRIPTPATH=$( cd $(dirname $0) ; pwd -P )
java -Xmx400M -cp classes cs276.assignments.Index Basic $1 $2
