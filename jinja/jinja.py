import os, shutil
import jinja2
from pprint import pprint

os.chdir(os.path.dirname(os.path.realpath(__file__)))

StrictVariants = {
    "apache" : ["default"],
    "caddy" : ["default"],
    "closedlitespeed" : ["default"],
    "nginx" : ["HTTP", "TLS"]
}

PossibleDefaultSites = {
    "apache": ["siteA", "siteB"],
    "caddy": ["siteA", "siteB", None],
    "closedlitespeed": ["siteA", "siteB", None],
    "nginx": ["siteA", "siteB"]
}

try:
    os.makedirs("../setups")
except:
    for d in os.listdir("../setups"):
        shutil.rmtree(f"../setups/{d}")

for d in os.listdir("."):
    if not os.path.isdir(f"./{d}"): continue
    dName = os.fsdecode(d)
    if dName.startswith("_"): continue #skip folders like _keys etc.

    files = [f"{r}/{f}"[len(d)+3:] for r, sd, fn in os.walk(f"./{d}") for f in fn]
    pprint(files)
    templates = []
    for file in files:
        with open(f"./{d}/{file}", "r", encoding="UTF-8") as f:
            print(f"reading template '{file}'")
            templates.append([file, jinja2.Template(f.read())])

    for isSubdomain in [True,False]:
        for isCertA in [True,False]:
            for defaultSite in PossibleDefaultSites[dName]:
                for isStrict in [True,False]:
                    for strictType in (StrictVariants[dName] if isStrict else ["none"]):
                        # build folder name
                        folderName  = "subdomains" if isSubdomain else "domains"
                        folderName += ("_certA" if isCertA else "")
                        folderName += (f"_default{defaultSite[-1:]}" if defaultSite else "")
                        folderName += (f"_strict_{strictType}" if isStrict else "")

                        # build config
                        for fileName, template in templates:
                            fullName = f"{dName}/{folderName}/{fileName}"
                            #print(f"building '{fullName}'")
                            readyF = template.render(
                                folderName =    folderName,
                                isSubdomain =   isSubdomain,
                                isCertA =       isCertA,
                                defaultSite =   defaultSite,
                                isStrict =      isStrict,
                                strictType =    strictType
                            )
                            #print(f" > {readyF[:50].replace('\n','  ')}")
                            # write config into folder
                            if ".jinja" in fullName:
                                fullName = fullName.split(".")[0]
                            os.makedirs(os.path.dirname(f"../setups/{fullName}"), exist_ok=True)
                            with open(f"../setups/{fullName}", "w+", encoding="UTF-8") as f:
                                #print(f"Writing {fullName}")
                                f.write(readyF)
