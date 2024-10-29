import os

orig = open("result.html", "r", encoding="UTF8").read()

short = {
    "secondRequest_http200_contentA":           "siteA data",
    "secondRequest_http200_contentB":           "siteB data",
    "secondRequest_http400_badRequest":         "http 400",
    "secondRequest_http404_notFound":           "http 404",
    "secondRequest_http421_misdirectedRequest": "http 421",
    "secondRequest_tlsAlert_internalError":     "alert iE",
    "secondRequest_tlsAlert_unexpectedMessage": "alert uM",
    "firstRequest_tlsAlert_unknownCA":          "first:alert uCA",
    "firstRequest_tlsAlert_unexpectedMessage":  "first:alert uM",
    " -> ": "</br>-></br>",
    " (":"</br>("
}

# create 'tables' folder if it does not exist
try: os.mkdir("tables")
except: pass

class HTMLBuilder:
    def __init__(self, orig):
        self.pre = orig.split("<table>")[0]+"<table>"
        self.post = orig.split("</table>")[1]+"</table>"
        self.data = orig.split("<table>")[1].split("</table>")[0]
    def getHTML(self, rows):
        return self.pre + rows + self.post
    def saveFile(self, rows, name):
        content = self.getHTML(rows)
        # apply short names from shortDict
        content_short = content
        for k, v in short.items(): content_short = content_short.replace(k, v)
        # save both to file
        open("tables/"+name+".html", "w").write(content)
        open("tables/"+name+"_short.html", "w").write(content_short)
builder = HTMLBuilder(orig)


# save full table to orig
builder.saveFile(builder.data, "orig")

# save table with only "nginx" rows
nginx = ""
for line in builder.data.split("\n\n"):
    if "nginx" in line or "<th>" in line:
        nginx += line + "\n\n"
builder.saveFile(nginx, "nginx")

# save table with only "apache" rows
apache = ""
for line in builder.data.split("\n\n"):
    if "apache" in line or "<th>" in line:
        apache += line + "\n\n"
builder.saveFile(apache, "apache")

# save table with only "caddy" rows
caddy = ""
for line in builder.data.split("\n\n"):
    if "caddy" in line or "<th>" in line:
        caddy += line + "\n\n"
builder.saveFile(caddy, "caddy")