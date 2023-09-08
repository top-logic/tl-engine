/* 
 * Handling of multiple browser windows.
 *
 * @import thread.js
 * @import simpleajax.js
 */

/**
 * Map of window info objects indexed by window name.
 */
var winAllWindows = {};

/**
 * Callback from the opened window that informs the opener that loading 
 * has completed.
 * 
 * <p>
 * The application must keep track of currently open window. Since IE does
 * not send an <code>onload</code> event in a window, if the user closed a 
 * window, before loading has completed, a newly opened window must be 
 * observed in a separate thread, until either the <code>onload</code> event
 * has been fired, or the observer thread detects that the window has been 
 * closed.   
 * </p>
 */
function winOnLoadWindow(windowRef) {
	var name = windowRef.name;
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		// The window has already been closed either programmatically or 
		// by user interaction. 
		return;
	}
	
	var loadNotifier = new Thread(
		function() {
			if (! windowInfo.lock.acquire("on-load")) {
				// Wait.
				return;
			}
			try {
				this.stop();
				
				// Mark the window as loaded. This terminates the opening 
				// observer thread.
				windowInfo.loaded = true;
			} finally {
				windowInfo.lock.release("on-load")
			}
		});

	// Branch into thread without initial timer. The timer is only used, 
	// if lock acquisition would fail (which should not happen ever).
	loadNotifier.next();
}

function winLockWindow(windowRef) {
	var name = windowRef.name;
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		return;
	}
	return windowInfo.lock.acquire("external");
}

function winUnlockWindow(windowRef) {
	var name = windowRef.name;
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		return;
	}
	windowInfo.lock.release("external");
}

function winOnUnloadWindow(windowRef) {
	var name = windowRef.name;
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		// The window been closed programmatically, cancel further 
		// notifications immediately. 
		return;
	}

	// Decide, whether the window has been closed, or is only 
	// being reloaded. This decision requires a separate thread, 
	// because, windowInfo.ref.closed is still <code>false</code> 
	// at the time, this method is called.
	var unloadNotifier = new Thread(
		function() {
			if (! windowInfo.lock.acquire("is-closed")) {
				// Wait.
				return;
			}
			try {
				if (windowInfo.ref.closed) {
					delete winAllWindows[name];
					_winNotifyWindowClose(name);
					this.stop();
				}
			} catch (ex) {
				// In some flavour of IE under certain circumstances, 
				// a security exception is throw. In that case, it is 
				// assumed that the window has been closed.
				delete winAllWindows[name];
				_winNotifyWindowClose(name);
				this.stop();
			} finally {
				windowInfo.lock.release("is-closed")
			}
		});

	// Switch the execution context to this component and execute 
	// _winNotifyWindowClose() synchronized on its own window info lock.
	unloadNotifier.start();
}

/**
 * Function to programmatically open a new window.
 * 
 * <p>Invoked from an AJAX result event handler</p>
 */
function winOpenWindow(name, url, width, height, hasLocation, hasStatus, isResizable, hasScrollbars, hasToolbar) {
	var windowInfo = 
		_winCreateWindowInfo(name, url, width, height, hasLocation, hasStatus, isResizable, hasScrollbars, hasToolbar);

	// Open the window and store the reference in windowInfo.
	windowInfo.ref = window.open(windowInfo.url, name, _winGetOpenSpec(windowInfo));

	if (windowInfo.ref == null) {
		_winNotifyWindowClose(name);
		return;
	}
	
	// Link the window to the list of all open windows.
	winAllWindows[name] = windowInfo;
	
	// Observe the new window until it either has completed loading or 
	// it has been closed by user interaction.
	var observer = new Thread(
		function() {
			if (! windowInfo.lock.acquire("open-observer")) {
				// Wait.
				return;
			}
			try {
				if (windowInfo.loaded) {
					// Loading has completed normally. Stop this observer.
					this.stop();
				} else if (windowInfo.ref.closed) {
					// The window has been closed before it could complete 
					// loading.
					delete winAllWindows[name];
					_winNotifyWindowClose(name);
					this.stop();
				}
			} finally {
				windowInfo.lock.release("open-observer");
			}
		});
	observer.delay = 500;
	observer.start();
}

/**
 * Reestablishes a connection to an already opened window.
 *  
 * <p>
 * This function is called from the <code>onload</code> handler of the windwo opening 
 * component, if it was reloaded at a time where windows are open. 
 * </p>
 */
function winConnectWindow(name, url, width, height, hasLocation, hasStatus, isResizable, hasScrollbars, hasToolbar) {
	var windowInfo = 
		_winCreateWindowInfo(name, url, width, height, hasLocation, hasStatus, isResizable, hasScrollbars, hasToolbar);

	// Try to get a reference to the still open window.
	windowInfo.ref = window.open('', name);
	if (windowInfo.ref == null) {
		_winNotifyWindowClose(name);
		return;
	}
	
	// Convert URL of opened (or re-attached) window to string. 
	var realLocation = "" + windowInfo.ref.document.location;
	
	var lastSlash = realLocation.lastIndexOf("/");
	if (lastSlash >= 0) {
		var subsessionSeparator = realLocation.indexOf("-", lastSlash);
		if (subsessionSeparator >= 0) {
			realLocation = realLocation.substring(0, subsessionSeparator);
		}
	}
	
	// services.log.info("Re-attached to window: " + name + ", url=" + realLocation);
	
	// If the window has been closed before it could be reattached, a reference 
	// to a newly opened blank window is returned. Such window must be closed 
	// immediately and the server must be informed about the closed window. 
	//
	// If the window has been re-attached, the realLocation points to the fully 
	// qualified URL of the window content source. In that case, the windowInfo.url 
	// is a suffix of the realLocation.
	if ((realLocation.length < windowInfo.url.length) || (realLocation.substring(realLocation.length - windowInfo.url.length) != windowInfo.url)) {
		// Closing a blank window does not trigger any callbacks.
		windowInfo.ref.close();
		_winNotifyWindowClose(name);
	} else if (BAL.isIE()) {
		// Re-attaching a window after an opener reload seems to be impossible in IE. 
		// Communication from the window to its reloaded opener is blocked by some 
		// security constraints. Therefore, prevent onunload callback, close the 
		// window, and inform the server about closing the window.
		_winReopenWindow(windowInfo);
	} else {
		// Mark the reattached window as loaded.
		windowInfo.loaded = true;
	
		// Link the window to the list of all open windows.
		winAllWindows[name] = windowInfo;
	}
}

/**
 * Workaround to close windows during reload of the window container component.
 */
function winConnectAndCloseWindow(name) {
	var ref = window.open('', name);
	ref.close();
}

function _winCreateWindowInfo(name, url, width, height, hasLocation, hasStatus, isResizable, hasScrollbars, hasToolbar) {
	var windowInfo = {
		name: name,
		url: url,
		width: width,
		height: height,
		location: hasLocation,
		status: hasStatus,
		resizable: isResizable,
		scrollbars: hasScrollbars,
		toolbar: hasToolbar,
		
		// Reference to the window object.
		ref: null,
		
		// Current state of this window.
		loaded: false,
		
		// Lock to synchronize callbacks from the window with window observer 
		// threads in this component.
		lock: new Lock()
	}

	return windowInfo;	
}

/**
 * Programmatically closes an open window.
 */
function winCloseWindow(name) {
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		// The window has already been closed by user interaction. 
		return;
	}

	windowInfo.ref.close();
	
	// Useless notifications upon window close are prevented by directly deleting this 
	// window from the list of currently open windows. Since this window is closed 
	// programmatically, the server must not again be informed about this close 
	// operation.
	delete winAllWindows[name];
}

/**
 * Send a focus event to an open window
 */
function winFocusWindow(name) {
	var windowInfo = winAllWindows[name];
	if (windowInfo == null) {
		// The window has already been closed by user interaction. 
		return;
	}

	if (windowInfo.ref.closed) {
		// The window is currently being closed.
		return;
	}
	
	windowInfo.ref.focus();
}

/**
 * Remove the window with the given name from the list of open windows and 
 * inform the server about the close operation.
 */
function _winNotifyWindowClose(name) {
	services.ajax.execute("onWindowClose", {name: name});
	// services.log.info("Window closed: " + name);
}

/**
 * Close the window represented by the given windowInfo and inform the server to
 * reopen it.
 */
function _winReopenWindow(windowInfo) {
	// winDetach is defined in WindowComponent and loaded in external window
	windowInfo.ref.winDetach();
	windowInfo.ref.close();
	windowInfo.ref=null;
	winOpenWindow(windowInfo.name, windowInfo.url, windowInfo.width, windowInfo.height, windowInfo.hasLocation, windowInfo.hasStatus, windowInfo.isResizable, windowInfo.hasScrollbars, windowInfo.hasToolbar);
	// services.log.info("Window reopened: " + windoInfo.name);
}

function _winGetOpenSpec(windowInfo) {
	var result = "";
	
	if (windowInfo.width !== undefined) {
		if (result.length > 0) result += ",";
		result += "width=" + windowInfo.width;
	}
	if (windowInfo.height !== undefined) {
		if (result.length > 0) result += ",";
		result += "height=" + windowInfo.height;
	}

	// Local function for appending boolean options.	
	var appendOption = function(field) {
		if (windowInfo[field] !== undefined) {
			if (result.length > 0) result += ",";
			result += field + "=" + (windowInfo[field] ? "yes" : "no");
		}
	}

	appendOption("location");	
	appendOption("status");	
	appendOption("resizable");	
	appendOption("scrollbars");	
	appendOption("toolbar");
	
	return result;
}

function winCloseAllOpenWindows() {
	for (var name in winAllWindows) {
		winCloseWindow(name);
	}
}

/**
 * Closes all currently opened windows after 'delay' milliseconds
 * 
 * @param delay
 *        the delay in milliseconds after which the external windows shall be
 *        closed
 */
function winCloseDelayed(delay) {
	for (var name in winAllWindows) {
		var windowInfo = winAllWindows[name];
		if (windowInfo == null) {
			continue;
		}
		var externalWindow = windowInfo.ref;
		// function windetachAndClose is defined in WindowComponent and loaded
		// in external window
		externalWindow.closeTimeout = externalWindow.setTimeout("winDetachAndClose()", delay);
	}
}

/**
 * Stops closing of external windows started by winCloseDelayed(delay)
 */
function winStopClosing() {
	for (var name in winAllWindows) {
		var windowInfo = winAllWindows[name];
		if (windowInfo == null) {
			continue
		}
		var externalWindow = windowInfo.ref;
		externalWindow.clearTimeout(externalWindow.closeTimeout);
	}
}
