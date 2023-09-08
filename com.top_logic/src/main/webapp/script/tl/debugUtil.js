/* 
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

// Use this function to get an alert displaying the
// frame structure of the given frame
function displayFrameStructure (aStartFrame) {
	if (aStartFrame == null) {
		aStartFrame = this;
	}

	alert (getDisplayFrameStructure (aStartFrame, 0, ""));
}

// Helper for displayFrameStructure
function getDisplayFrameStructure (aStartFrame, aDepth, aString) {
	aString = aString +  getOutputFor (aStartFrame, aDepth);
	var theCurrentFrames = aStartFrame.frames;

	if (theCurrentFrames != null) {
		for (var i = 0; i < theCurrentFrames.length; i++) {
			aString = getDisplayFrameStructure (theCurrentFrames[i], aDepth + 1, aString);
		}
	}

	return aString;
}	

// Helper for displayFrameStructure
function getOutputFor (aFrame, aDepth) {
	theName = aFrame.name;
	if ((theName == null) || (theName == "")) {
		theName = "???";
	}
	
	theOutput = "";
	for (i=1; i<=aDepth; i++) {
		theOutput = theOutput + "\t";
	}
	
	theOutput = theOutput + theName + "\n";

	return theOutput;
}
