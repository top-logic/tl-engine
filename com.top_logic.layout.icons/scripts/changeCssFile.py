import re
note = notepad.getCurrentFilename()
stylesheet = open(note, "r")
readStylesheet = stylesheet.read()
readStylesheet = readStylesheet.replace("icomoon", "tl_iconFont")
readStylesheet = readStylesheet.replace("url('fonts/", "url('../webfonts/")
readStylesheet = readStylesheet.replace("[class^=\"icon-\"], [class*=\" icon-\"]", ".tl-icon, i[class*=\" overlay-\"] span");
readStylesheet = readStylesheet.replace(".icon-", ".tl-icon.");
readStylesheet = readStylesheet.replace(":before", "::before")
readStylesheet = readStylesheet.replace(" .path1::before", "::before")
readStylesheet = readStylesheet.replace(".path2::before", "span::before")
readStylesheet = readStylesheet.replace(".path3::before", "span::after")
readStylesheet = readStylesheet.replace(" .path4::before", "::after")
readStylesheet = re.sub('\.tl-icon\.overlay-([^\s])*::(before)(\s)\{(.*?\s*)*\}\n', '', readStylesheet)
readStylesheet = readStylesheet.replace(".tl-icon.overlay-", ".overlay-");
readStylesheet = re.sub('\s*color:.*', '', readStylesheet)
stylesheet.close()
stylesheetW = open(note, "w")
stylesheetW.write(readStylesheet)
stylesheetW.close()