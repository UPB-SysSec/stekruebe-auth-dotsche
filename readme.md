## running

use `rebuild_run.bat` or `rebuild_run.sh` to build and run the docker container.
NOTE: the container will use ports 443 and 444

## testing

use the given bash files to test the behavior for sites *A* and *B*

- `getA.sh` - site A should return *200 OK*
- `getB.sh` - site B should return *403 Forbidden*
- `getBcert.sh` - site B should return *200 OK* because a valid client cert was sent