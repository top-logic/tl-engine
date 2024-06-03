################################
#  How to expand TL-Icon-Font  #
################################

1. Add the new icon(s) as SVG image(s) to "com.top_logic.layout.icons/icons".

2. Generate font.
	a. Open https://icomoon.io/
	b. Import all icons (but those of the "raw" folder) of "com.top_logic.layout.icons/icons".
	c. Select all.
	d. Generate font.
	e. Download font.
	f. Extract zip-file.

3. Rename and move font files.
	a. Go to the "fonts" folder.
	b. Change names from "icomoon" to "tl_iconFont".
	c. Move the four font files to "com.top_logic.layout.icons/src/main/webapp/webfonts".

4. Update CSS file.
	a. Open "style.css" of the generated font.
	b. Use the python script "changeCssFile.py" on it.
	c. Replace the content of "com.top_logic.layout.icons/src/main/webapp/style/tl_iconFont.css" by the new content of "style.css".

5. Add the meta data of the new icons to "com.top_logic.layout.icons/src/main/webapp/webfonts/tl_iconFontMetadata.json".

**************************************

Migration.
Hint 1: Only necessary if you want to migrate old image icons to font icons in theme-settings.
Hint 1: imageIconFontMapping.txt (contains the mapping from image icon to font icon)
Hint 2: imagesToIconFonts.py (the script must be in the same folder as the "imageIconFontMapping" file)
	a. Open "theme-settings" file.
	b. Run "imagesToIconFonts.py" on it.

**************************************

How to run python with Notepad++.
	a. Install plugin "PythonScript".
	b. Open the file you want to run the script on.
	c. Run the wanted python script on it.