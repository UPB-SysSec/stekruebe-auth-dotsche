import os, shutil
from jinja2 import Template

os.chdir(os.path.dirname(os.path.realpath(__file__)))

StrictVariants = {
    "apache" : ["off", "default"],
    "caddy" : ["default"],
    "closedlitespeed" : ["off", "default"],
    "nginx" : ["off", "HTTP", "TLS"]
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
shutil.copytree("./_shared", "../setups/shared")
shutil.copytree("./_site-content", "../setups/site-content")

for d in os.listdir("."):
    if not os.path.isdir(f"./{d}"): continue
    dName = os.fsdecode(d)
    if dName.startswith("_"): continue #skip folders like _keys etc.

    files = [f"{r}/{f}"[len(d)+3:] for r, sd, fn in os.walk(f"./{d}") for f in fn]
    templates = []
    for file in files:
        with open(f"./{d}/{file}", "r", encoding="UTF-8") as f:
            print(f"reading template '{file}'")
            templates.append([file, Template(f.read())])

    for isSubdomain in [True,False]:
        for isCertA in [True,False]:
            for defaultSite in PossibleDefaultSites[dName]:
                for strictType in StrictVariants[dName]:
                    # build folder name
                    folderName  = "subdomains" if isSubdomain else "domains"
                    folderName += ("_certA" if isCertA else "")
                    folderName += (f"_default{defaultSite[-1:]}" if defaultSite else "")
                    folderName += (f"_strict_{strictType}" if strictType!="off" else "")

                    # build config
                    for fileName, template in templates:
                        fullName = f"{dName}/{folderName}/{fileName}"
                        #print(f"building '{fullName}'")
                        readyF = template.render(
                            folderName =    folderName,
                            isSubdomain =   isSubdomain,
                            isCertA =       isCertA,
                            defaultSite =   defaultSite,
                            strictType =    strictType
                        )
                        #print(f" > {readyF[:50].replace('\n','  ')}")
                        # write config into folder
                        if fullName.endswith(".jinja"):
                            fullName = fullName[:-6]
                        os.makedirs(os.path.dirname(f"../setups/{fullName}"), exist_ok=True)
                        with open(f"../setups/{fullName}", "w+", encoding="UTF-8") as f:
                            #print(f"Writing {fullName}")
                            f.write(readyF)
