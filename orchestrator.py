from lib.docker import Docker
from lib.curl import Curl
from lib.openssl import OpenSSL #note this is not OpenSSL (builtin)
from lib.setups import SETUPS

REBUILD = True
RESTART = True

from multiprocessing import Pool
from time import sleep
from pprint import pprint
# ref: https://svn.blender.org/svnroot/bf-blender/trunk/blender/build_files/scons/tools/bcolors.py
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
import os
from pathlib import Path
paths = []
servers = [
    Path("apache"),
    Path("caddy"),
    Path("nginx")
]

def testSetup(setup, server, port):
    # build container
    path = Path(server).joinpath(setup.name)
    name = Docker.nameFromPath(path)
    print(f"{bcolors.OKBLUE}# testing {name}{bcolors.ENDC}")
    #pprint(setup)

    if not Docker.imageExists(name) or REBUILD:
        out, err, code = Docker.build(name, path)
        if not err=="":
            print(f"{bcolors.WARNING}[{code}] error while building image '{name}'{bcolors.ENDC}")
            print(f"\t{bcolors.WARNING}'{err}'{bcolors.ENDC}")
            return [name, "error during build"]

    # start container
    out, err, code = Docker.run(imageName=name, containerName=name, restart=RESTART, port=port)
    sleep(0.5) #to wait for the server to start
    out, err, code = "", "", 0

    Docker.stop(name)
    return [name]

p = Pool()
c = []
[(setup, server) for server in servers for setup in SETUPS]
for setup in SETUPS:
    for server in servers:
        c.append([setup, server, 433+len(c)])
res = p.starmap(testSetup, c)
pprint(res)