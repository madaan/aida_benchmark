#sg
fn1="temp"$$
fn2="temp2"$$
fn3="temp3"$$
echo "Replacing double spaces "
sed  's/  / /g' "$1" > "$fn1"
echo "Writing results to "$fn1

echo "Shuffling the columns"
awk '
{printf("%d ", $NF);
    for(i = 1; i < NF; i++) {
        printf("%s ", $i)
    }
    printf("\n")
}' "$fn1" > "$fn2"
echo "Writing results to "$fn2

echo "Sorting the features"
python sortFeatures.py "$fn2" > "$fn3"
echo "Writing results to "$fn3

mv $fn3 $1".prepared"
echo "Removing $fn1 $fn2"
rm $fn1 $fn2
