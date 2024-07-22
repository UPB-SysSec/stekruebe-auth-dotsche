from subprocess import PIPE, Popen, TimeoutExpired
from pathlib import Path

_setupsFolder = Path("./setups/")

"""
- workflow_trace_type is from
[
    FULL,   HANDSHAKE,   DYNAMIC_HANDSHAKE,   DYNAMIC_HELLO, 
    HELLO,   SHORT_HELLO,   RESUMPTION,   FULL_RESUMPTION, 
    CLIENT_RENEGOTIATION_WITHOUT_RESUMPTION,   CLIENT_RENEGOTIATION, 
    SERVER_RENEGOTIATION,   DYNAMIC_CLIENT_RENEGOTIATION_WITHOUT_RESUMPTION, 
    HTTPS,   DYNAMIC_HTTPS,   SSL2_HELLO,   SIMPLE_MITM_PROXY, 
    SIMPLE_FORWARDING_MITM_PROXY,   TLS13_PSK,   FULL_TLS13_PSK, 
    ZERO_RTT,   FULL_ZERO_RTT,   FALSE_START,   RSA_SYNC_PROXY
]
"""

class TLS_Attacker:
    @staticmethod
    def request(target:str, caPath:Path=Path("shared/cert/keys/ca_s.crt"), port:int=443, ip:str="127.0.0.1", name="default")->tuple[str, str, int]:
        """
        target
        certPath - the path to the client cert (.pem file; crt+key)
        caPath - certificate of the server (.crt file)
        """
        caPath = _setupsFolder.joinpath(caPath)
        proc = Popen(
            [
                'java',
                '-jar',
                './attacker/TLS-Attacker/apps/TLS-Client.jar',
                '-connect',
                f'https://{ip}:{port}',
                '-server_name',
                f'{target}',
                '-workflow_output',
                f'tmp/{name}_{target}.xml',
                '-workflow_trace_type',
                'DYNAMIC_HTTPS'
            ],
            stdout=PIPE,
            stderr=PIPE,
            cwd="."
        )
        try:
            exitVal = proc.wait(5)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8")
        err = proc.stderr.read().decode("UTF-8")
        return out, err, exitVal

    @staticmethod
    def requestWithCert(target:str, certPath:Path, keyPath:Path, caPath:Path=Path("shared/cert/keys/ca_s.crt"), port:int=443, ip:str="127.0.0.1", name="default")->tuple[str, str, int]:
        """
        target
        certPath - the path to the client cert (.pem file; crt+key)
        caPath - certificate of the server (.crt file)
        """
        certPath = _setupsFolder.joinpath(certPath)
        keyPath = _setupsFolder.joinpath(keyPath)
        caPath = _setupsFolder.joinpath(caPath)
        proc = Popen(
            [
                'java',
                '-jar',
                './attacker/TLS-Attacker/apps/TLS-Client.jar',
                '-connect',
                f'https://{ip}:{port}',
                '-server_name',
                f'{target}',
                '-cert',
                f'{certPath.as_posix()}',
                '-key',
                f'{keyPath.as_posix()}',
                '-workflow_output',
                f'tmp/{name}_{target}.xml',
                '-workflow_trace_type',
                'DYNAMIC_HTTPS'
            ],
            stdout=PIPE,
            stderr=PIPE,
            cwd="."
        )
        try:
            exitVal = proc.wait(5)
        except TimeoutExpired:
            return "", "exec timeout expired", -1
        out = proc.stdout.read().decode("UTF-8")
        err = proc.stderr.read().decode("UTF-8")
        return out, err, exitVal