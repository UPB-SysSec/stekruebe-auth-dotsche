./scripts/kill.sh $1
echo building
./scripts/build.sh $1
echo running
./scripts/run.sh $1
read "Press Enter to stop"
echo stopping
./scripts/kill.sh $1
