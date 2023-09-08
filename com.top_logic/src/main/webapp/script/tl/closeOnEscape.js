/* 
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
function closeOnEscape (e) {
	var theKeyCode = 0;
	if (typeof e == "undefined") {
		theKeyCode = event.keyCode;
	} else {
		theKeyCode = e.keyCode;
	}
	//alert('KeyPressed: '+theKeyCode);

	if (theKeyCode==27) { // escape
		if (!typeof event == "undefined") {
			event.returnValue=false; // disable further event handling for escape-key
		}
		top.close();
	}
}
// redirect keyboard event handling		
document.onkeydown = closeOnEscape;

