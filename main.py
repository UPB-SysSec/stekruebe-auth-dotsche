from lib.docker import Docker
from lib.curl import Curl
from lib.tls_attacker import TLS_Attacker
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

    # get a correct
    if setup.aCert=="":
        out, err, code = TLS_Attacker.request(
            target = Path(setup.aLocation), 
            caPath = Path(setup.caPath),
            port=port
        )
    else:
        out, err, code = TLS_Attacker.requestWithCert(
            target = setup.aLocation,
            certPath = Path(f"{setup.aCert}.crt"),
            keyPath = Path(f"{setup.aCert}.key"),
            caPath = Path(setup.caPath),
            port=port
        )
    if err!="":
        print(f"{bcolors.FAIL}# ERROR: {err}{bcolors.ENDC}")
    else:
        print(f"{bcolors.OKGREEN}+ SUCCESS: {code}{bcolors.ENDC}")

    # get b correct
    if setup.bCert=="":
        out2, err2, code2 = TLS_Attacker.request(
            target = Path(setup.bLocation), 
            caPath = Path(setup.caPath),
            port=port
        )
    else:
        out2, err2, code2 = TLS_Attacker.requestWithCert(
            target = setup.bLocation,
            certPath = Path(f"{setup.bCert}.crt"),
            keyPath = Path(f"{setup.bCert}.key"),
            caPath = Path(setup.caPath),
            port=port
        )
    if err2!="":
        print(f"{bcolors.FAIL}# ERROR: {err2}{bcolors.ENDC}")
    else:
        print(f"{bcolors.OKGREEN}+ SUCCESS: {code2}{bcolors.ENDC}")

    Docker.stop(name)
    return [name, code, code2]

p = Pool(1)
c = []
[(setup, server) for server in servers for setup in SETUPS]
for setup in SETUPS:
    for server in servers:
        c.append([setup, server, 433+len(c)])
try:
    res = p.starmap(testSetup, c)
    pprint(res)
except KeyboardInterrupt:
    print(f"{bcolors.WARNING}\nstopping{bcolors.ENDC}")