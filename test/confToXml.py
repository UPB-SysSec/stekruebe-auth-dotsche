indentStack = []
with open("../setups/closedlitespeed/domains/config/conf/httpd_config.xml", "w", encoding="UTF-8") as out:
    with open("../setups/closedlitespeed/domains/config/conf/httpd_config.conf", "r", encoding="UTF-8") as inp:
        for line in inp:
            line = line.strip()
            if line.startswith("#") or line=="": continue
            key, val = line.split(" ", 1) if " " in line else (line.strip(), "")
            key, val = key.strip(), val.strip()
            ind = len(indentStack)
            if line.endswith("{"):
                out.write(f"{'  '*ind}<{key.strip("{")}>\n")
                indentStack.append(key)
            elif key=="}":
                out.write(f"{'  '*(max(ind-1,0))}</{indentStack.pop()}>\n")
            else:
                out.write(f"{'  '*ind}<{key}>{val}</{key}>\n")
