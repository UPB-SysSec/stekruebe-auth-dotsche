from subprocess import PIPE, Popen, TimeoutExpired
from pathlib import Path

class OpenSSL:
    @staticmethod
    def request(target:str, caPath:Path=Path("shared/cert/keys/ca_s.crt"), port:int=443, ip:str="127.0.0.1")->tuple[str, str, int]:
        """
        target
        certPath - the path to the client cert (.pem file; crt+key)
        caPath - certificate of the server (.crt file)
        """
        print(f"openssl s_client -connect https://{target}:{port} -CAfile {caPath.as_posix()} -quiet")
        proc = Popen(
            [
                'openssl',
                's_client',
                '-connect',
                f'{target}:{port}',
                '-CAfile',
                f'{caPath.as_posix()}',
                '-quiet'
            ],
            stdin=PIPE,
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            proc.stdin.write(f"GET / HTTP/2.0\r\nHost: {target}\r\n\r\nConnection: close\r\n".encode("ASCII"))
            exitVal = proc.wait(1)
        except TimeoutExpired:
            pass
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8")
        err = proc.stderr.read().decode("UTF-8")
        return out, err, exitVal

    @staticmethod
    def requestWithCert(target:str, certPath:Path, caPath:Path=Path("shared/cert/keys/ca_s.crt"), port:int=443, ip:str="127.0.0.1")->tuple[str, str, int]:
        """
        target
        certPath - the path to the client cert (.pem file; crt+key)
        caPath - certificate of the server (.crt file)
        """
        proc = Popen(
            [
                'openssl',
                's_client',
                '-connect',
                f'{target}:{port}',
                '-CAfile',
                f'{caPath.as_posix()}',
                '-cert',
                f'{certPath.as_posix()}'
                '-quiet'
            ],
            stdin=PIPE,
            stdout=PIPE,
            stderr=PIPE
        )
        try:
            proc.stdin.write(f"GET / HTTP/2.0\r\nHost: {target}\r\nConnection: close\r\n".encode("ASCII"))
            exitVal = proc.wait(1)
        except TimeoutExpired:
            pass
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8")
        err = proc.stderr.read().decode("UTF-8")
        return out, err, exitVal
