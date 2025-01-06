import os, shutil
import jinja2

os.chdir(os.path.dirname(os.path.realpath(__file__)))

StrictVariants = {
    "apache" : ["default"],
    "caddy" : ["default"],
    "closedlitespeed" : ["default"],
    "nginx" : ["HTTP", "TLS"]
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
    
    files = os.listdir(f"./{d}")
    templates = []
    for file in files:
        with open(f"./{d}/{file}", "r", encoding="UTF-8") as f:
            print(f"reading template '{file}'")
            templates.append([file, jinja2.Template(f.read())])

    for isSubdomain in [True,False]:
        for isCertA in [True,False]:
            for isDefaultB in [True,False]:
                for isStrict in [True,False]:
                    for strictType in (StrictVariants[dName] if isStrict else ["none"]):
                        # build folder name
                        folderName  = "subdomains" if isSubdomain else "domains"
                        folderName += ("_certA" if isCertA else "")
                        folderName += ("_defaultB" if isDefaultB else "")
                        folderName += (f"_strict_{strictType}" if isStrict else "")
                        
                        # build config
                        for fileName, template in templates:
                            fullName = f"{dName}/{folderName}/{fileName}"
                            print(f"building '{fullName}'")
                            readyF = template.render(
                                folderName =    folderName,
                                isSubdomain =   isSubdomain,
                                isCertA =       isCertA,
                                isDefaultB =    isDefaultB,
                                isStrict =      isStrict,
                                strictType =    strictType
                            )
                            print(f" > {readyF[:50].replace('\n','  ')}")
                            # write config into folder
                            os.makedirs(os.path.dirname(f"../setups/{fullName}"), exist_ok=True)
                            with open(f"../setups/{fullName}", "w+", encoding="UTF-8") as f:
                                f.write(readyF)