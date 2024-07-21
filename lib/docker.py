from subprocess import PIPE, Popen, TimeoutExpired
from pathlib import Path
import re

DEBUG = True
setupsFolder = Path("./setups/")

class Docker:
    @staticmethod 
    def nameFromPath(imagePath:Path)->str:
        return imagePath.as_posix().replace("/", "_").lower()

    @staticmethod
    def build(imageName:str, dockerFilePath:Path)->tuple[str, str, int]:
        """
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if DEBUG: print(f"building '{imageName}'")
        proc = Popen(
            [
                'docker',
                'build',
                '-q',
                '-t',
                f'{imageName}',
                '-f',
                f'{dockerFilePath.joinpath('Dockerfile').as_posix()}',
                '.'
            ],
            stdout=PIPE,
            stderr=PIPE,
            cwd="./setups/"
        )
        try:
            exitVal = proc.wait(20)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal

    @staticmethod
    def remove(imageName)->tuple[str, str, int]:
        """
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if DEBUG: print(f"removing '{imageName}'")
        proc = Popen(
            [
                'docker',
                'rmi',
                f"{imageName}"
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(5)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal

    @staticmethod
    def run(imageName:str, containerName:str, restart:bool=False, port:int=443)->tuple[str, str, int]:
        """
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if DEBUG: print(f"running '{imageName}' as '{containerName}'")
        if Docker.isRunning(containerName):
            if restart:
                Docker.stop(containerName)
            else:
                raise RuntimeError(f"unable to start docker container '{containerName}' because that name is already taken")
            
        proc = Popen(
            [
                'docker',
                'run',
                #'-t',
                '--rm',
                '-d',
                '-p',
                f'{port}:443',
                '--name',
                f"{containerName}",
                f"{imageName}"
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(5)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal

    @staticmethod
    def stop(imageName)->tuple[str, str, int]:
        """
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if DEBUG: print(f"stopping '{imageName}'")
        proc = Popen(
            [
                'docker',
                'kill',
                f"{imageName}"
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(5)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal

    @staticmethod
    def execute(containerName:str, command:str, args:list=[], timeout=5)-> tuple[str, str, int]:
        """
        takes:
            command - the command to execute
            args - an optional list of arguments for the command
            timeout - the timeout in seconds, default is 5s 
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if not Docker.isRunning(containerName):
            raise RuntimeError(f"unable to execute in container '{containerName}' because it is not running")
        proc = Popen(
            [
                'docker',
                'exec',
                f"{containerName}",
                f"{command}",
                *args
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(timeout)
        except TimeoutExpired:
            return "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal 

    @staticmethod
    def imageExists(containerName:str, timeout=5):
        proc = Popen(
            [
                'docker',
                'images',
                '-q',
                f'{containerName}'
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(timeout)
        except TimeoutExpired:
            return "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        r = re.compile(r"[0-9a-fA-F]{12}")
        return not r.match(out) is None

    @staticmethod
    def isRunning(containerName:str, timeout=5):
        proc = Popen(
            [
                'docker',
                'ps',
                '-f',
                f'NAME={containerName}',
                '-q'
            ],
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            exitVal = proc.wait(timeout)
        except TimeoutExpired:
            return "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        r = re.compile(r"[0-9a-fA-F]{12}")
        return not r.match(out) is None
    
    
    @staticmethod
    def ls()->tuple[str, str, int]:
        """
        returns:
            stdOut:str, stdErr:str, returnCode:int
        """
        if DEBUG: print(f"ls")
        proc = Popen(
            [
                'ls',
                '.'
            ],
            stdout=PIPE,
            stderr=PIPE,
            cwd="./setups/"
        )
        try:
            exitVal = proc.wait(20)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8").strip()
        err = proc.stderr.read().decode("UTF-8").strip()
        return out, err, exitVal