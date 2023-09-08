
function testName(aName, aBlackList, aWhiteList) {
	
	for (var thePos = 0; thePos < aBlackList.length; thePos++) {
		if (aName == aBlackList[thePos]) { 
			return -1; 
		}
	}
	var len = aWhiteList.length;
	if(len == 0) {
		return 0;
	}
	else 
	{
		for (var thePos = 0; thePos < len; thePos++) {
			if (aName == aWhiteList[thePos]) { 
				return 0; 
			}
		}
		return -1;
	}
}

function fileNameCharactersOK(aName, allowedChars) {
	var thePos       = aName.lastIndexOf("\\") + 1;
	var theLength    = aName.length;
	var theFileName  = aName.substring(thePos, theLength);
	var theFirstChar = theFileName.substring(0, 1);

	if ((theFirstChar == " ") || (theFirstChar == ".")) {
		return false;
	}

    var thePosition;
    var theChar;
	for (var i = 0; i < theLength - thePos; i++) {
		theChar     = theFileName.substring(i, i+1);
		thePosition = allowedChars.lastIndexOf(theChar);

		if (thePosition < 0) {
			return false;
		}
	}

	return true;
}

function baseName(aPath) {
    var thePos  = aPath.lastIndexOf("\\");
    if (thePos >= 0) {
        aPath = aPath.substring(thePos + 1, aPath.length);
    }
    // do the same for a linux path
    thePos  = aPath.lastIndexOf("/");
    if (thePos >= 0) {
        aPath = aPath.substring(thePos + 1, aPath.length);
    }
    return aPath;
}


function plausiDirname(aName) {
    if (!aName) {
        return false;
    }
	theChar = aName.substring(0, 1);
	if ((theChar == " ") || (theChar == ".")) {
		return false;
	}
	return true
}

/** 
 * Check if given name looks like a valid file name.
 * 
 * aName will be a complete path for IE and Mozilla < 3.0 
 *       the plain name starting with Mozilla 3.0
 */
function plausiFilename(aPath) {
	return baseName(aPath).length >= 3;
}
