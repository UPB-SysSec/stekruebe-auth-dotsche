from pathlib import Path
from dataclasses import dataclass

@dataclass
class Setup:
    name:str
    aLocation:str
    aCert:str
    bLocation:str
    bCert:str
    caPath:str


SETUPS = [
    Setup(
        name = "domains",
        aLocation = "siteA.org",
        aCert = "",
        bLocation = "siteB.org",
        bCert = "shared/cert/keys/user_ca_b",
        caPath = "shared/cert/keys/ca_s.crt"
    ),
    Setup(
        name = "domains_certA",
        aLocation = "siteA.org",
        aCert = "shared/cert/keys/user_ca_a",
        bLocation = "siteB.org",
        bCert = "shared/cert/keys/user_ca_b",
        caPath = "shared/cert/keys/ca_s.crt"
    ),
    Setup(
        name = "subdomains",
        aLocation = "siteA.site.org",
        aCert = "",
        bLocation = "siteB.site.org",
        bCert = "shared/cert/keys/user_ca_b",
        caPath = "shared/cert/keys/ca_s.crt"
    ),
    Setup(
        name = "subdomains_certA",
        aLocation = "siteA.site.org",
        aCert = "shared/cert/keys/user_ca_a",
        bLocation = "siteB.site.org",
        bCert = "shared/cert/keys/user_ca_b",
        caPath = "shared/cert/keys/ca_s.crt"
    ),
    Setup(
        name = "open",
        aLocation = "siteA.org",
        aCert = "",
        bLocation = "siteB.org",
        bCert = "",
        caPath = "shared/cert/keys/ca_s.crt"
    )
]