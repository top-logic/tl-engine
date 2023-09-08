# Creates a metadata file for the icon in a given directory.
# The metadata icon file is one large JSON object which has a key for each icon and as value the icons search terms (keywords), styling and label as a JSON object itself.

import json
import os

# Relative path to the icon directory to build the metadata file for.
directoryPath = 'icons'

# Separator of the icon files basename. This separator is used by a heuristic to identify the icon search terms.
iconFileBasenameSeparator = '-'

# Filename of the resulting icon metdata file.
iconMetadataFilename = 'iconMetadata.json'

icons = {}

for filename in os.listdir(directoryPath):
    path = os.path.join(directoryPath, filename)
  
    if os.path.isfile(path):
        fileBasename = os.path.splitext(os.path.basename(path))[0]
        
        possibleSearchTerms = fileBasename.split(iconFileBasenameSeparator)
        
        # Remove numbers at the beginning and the end.
        strippedPossibleSearchTerms = map(lambda s: s.strip("0123456789") , possibleSearchTerms)
        
        # Remove search terms with a length of 1.
        searchTerms = list(filter(lambda x : len(x) > 1 , strippedPossibleSearchTerms))
    
        # Create the the metadata JSON object for this icon.
        icon = {
            "search": {
                "terms": searchTerms
            },
            "styles": [
                ""
            ],
            "label": fileBasename
        }
    
        # Append the created icon metadata to the other metadata.
        icons.update({fileBasename: icon})

# Creates a pretty printed icon JSON metadata file.
with open(iconMetadataFilename, "w") as write_file:
    json.dump(icons, write_file, indent=4, sort_keys=True)