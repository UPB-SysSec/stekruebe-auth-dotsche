from pathlib import Path
from dataclasses import dataclass
from lib.setups import Setup

def runTest(target:Setup)->tuple[str, int, int]:
    return "a", -1, -1