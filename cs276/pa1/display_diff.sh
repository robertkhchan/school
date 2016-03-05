for ((i = 4 ; i < 5 ; i++))
do
query_diff= diff -U 0 task1/output/query_out/query.$i dev_output/$i.out
echo $query_diff 
done
