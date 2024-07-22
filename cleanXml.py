import xml.etree.ElementTree as ET
import os

def mdPrint(elem:ET.Element, indent=0, header=0, bold=False, italic=False):
    # --- prep ---
    tab=" "*2
    tag = elem.tag
    prepend = "-"

    child_indent = indent+1
    child_header = 0
    child_bold = False
    child_italic = False

    # --- special tag handling ---
    if elem.tag in [
        "completeResultingMessage", 
        "messageContent",
        #"expectedMessages",
        #"expectedHttpMessages",
        "executed", 
        "connectionAlias", 
        "actionOptions", 
        "extensionBytes",
        "completeRecordBytes",
        "cleanProtocolMessageBytes",
        "protocolMessageBytes",
        "records",
        "fragments"
    ]:
        return None
    elif elem.tag in ["messages", "httpMessages", "records"]:
        italic = True
        child_bold = True
    elif elem.tag in ["originalValue", "headerValueConfig"]:
        if elem.text is None: return ""
        maxLen = 20
        value = (elem.text[:maxLen]+'...') if len(elem.text)>maxLen else elem.text
        return f": {value}"
    elif elem.tag in ["headerNameConfig"]:
        if elem.text is None: return ""
        return f"{elem.text}"
    else:
        print(elem.tag)
    
    if bold: tag = f"**{tag}**"
    if italic: tag = f"_{tag}_"
    if header!=0: prepend = "#"*header
    
    # --- print ---
    if len(elem)==0:
        return f"\n{tab*indent}{prepend} {tag}: {elem.text}"
    else:
        if indent==0:
            header = [f"# {tag}"]
        else:
            header = [f"{tab*indent}{prepend} {tag}"]
        lines = [
            mdPrint(
                x, 
                indent=child_indent, 
                header=child_header, 
                bold=child_bold, 
                italic=child_italic
            ) for x in elem
        ]
        lines = list(filter(lambda x: not x is None, lines))
        return "\n"+"".join(header+lines)


def xml_to_md(content:str):
    workflowTrace = ET.fromstring(content)
    #print(workflowTrace, workflowTrace.tag, workflowTrace.attrib)
    outboundConnection = None
    messages = []
    for elem in workflowTrace:
        if elem.tag=="OutboundConnection":
            outboundConnection=mdPrint(elem)
            continue
        messages.append(mdPrint(elem, header=2))
    return f"{outboundConnection}\n{''.join(messages)}"

def clean():
    directory = "./tmp"
    directory_e = os.fsencode("./tmp")
    # https://stackoverflow.com/a/10378012
    for filename_e in os.listdir(directory_e):
        filename = os.fsdecode(filename_e)
        filePath = os.path.join(directory, filename)
        if filename.endswith(".xml"):
            print(f"cleaning {filename}")
            cleanContent = ""
            with open(filePath, "r", encoding="UTF-8") as f:
                content = f.read()
                cleanContent = xml_to_md(content)
            with open(f"{filePath}_clean.md", "w", encoding="UTF-8") as f:
                f.write(cleanContent)

if __name__=="__main__":
    clean()
    print("done")