
var DLG_MINIMUM_SIZE_PX = 100;

function dlgToogleMinimized(windowID){
	var theWindow = document.getElementById(windowID);
	if (theWindow != null) {
		if (theWindow.className == "dlgWindow") {
			theWindow.className = "dlgWindow dlgMinimized";
		} else {
			theWindow.className = "dlgWindow";
		}
	}
	return false;
}

var dlgMovedDialog;
var dlgMovedDialogWindow;
var dlgMoveOffsetX;
var dlgMoveOffsetY;
var dlgInitPositionX;
var dlgInitPositionY;

function dlgMoveStart(evt, handle) {
	// Search corresponding window element.
	dlgMovedDialog = _dlgFindDialog(handle);
	if (dlgMovedDialog == null) {
		return false;
	}
	
	dlgMovedDialogWindow = _dlgGetWindow(dlgMovedDialog);
	
	BAL.addEventListener(document, "mousemove", _dlgMoveInit, false);
	BAL.addEventListener(document, "mouseup", _dlgMoveInitStop, false);
	
	dlgInitPositionX = BAL.getEventX(BAL.getEvent(evt));
	dlgInitPositionY = BAL.getEventY(BAL.getEvent(evt));
	
	dlgMoveOffsetX = dlgInitPositionX - BAL.getElementX(dlgMovedDialogWindow);
	dlgMoveOffsetY = dlgInitPositionY - BAL.getElementY(dlgMovedDialogWindow);
	
	BAL.eventStopPropagation(BAL.getEvent(evt));
	return false;
}

function _dlgMoveInit(event) {
	var currentPositionX = BAL.getEventX(BAL.getEvent(event));
	var currentPositionY = BAL.getEventY(BAL.getEvent(event));
	if(currentPositionX != dlgInitPositionX || currentPositionY != dlgInitPositionY) {
		_dlgRemoveMoveInitListeners();
		_dlgStartCapture(dlgMoveStop, dlgMove);
		dlgMove(event);
	}
	
}

function _dlgRemoveMoveInitListeners() {
	BAL.removeEventListener(document, "mousemove", _dlgMoveInit, false);
	BAL.removeEventListener(document, "mouseup", _dlgMoveInitStop, false);
}

function _dlgMoveInitStop(event) {
	_dlgRemoveMoveInitListeners();
	_clearTempVariables();
}

function _dlgStartCapture(onMouseUpHandler, onMouseMoveHandler) {
	var capture = document.getElementById(dlgMovedDialog.id + "-capture");
	capture.style.display = "inline";
	
	// Workaround: IE does not accept a 100% height in CSS.
	BAL.setElementWidth(capture, BAL.getViewportWidth());
	BAL.setElementHeight(capture, BAL.getViewportHeight());

	BAL.addEventListener(document, "mouseup", onMouseUpHandler, false);
	BAL.addEventListener(document, "mousemove", onMouseMoveHandler, false);
}

function _dlgFindDialog(handle) {
	var dialog = handle;
	while (dialog != null) {
		if (BAL.DOM.containsClass(dialog, "dlgDialog")) {
			break;
		}
		
		dialog = dialog.parentNode;
	}
	return dialog;
}

function _dlgGetWindow(dialog) {
	if (dialog == null) return null;

	return document.getElementById(dialog.id + "-window");
}

function _dlgGetContentContainer(dialog) {
	if (dialog == null) return null;
	
	return document.getElementById(dialog.id + "-content");
}

function _dlgGetHeader(dialog) {
	if (dialog == null) return null;
	
	return document.getElementById(dialog.id + "-titleBar");
}

function _dlgGetFooter(dialog) {
	if (dialog == null) return null;
	
	return document.getElementById(dialog.id + "-footer");
}

function _dlgGetLeftBorder(dialog) {
	if (dialog == null) return null;
	
	return document.getElementById(dialog.id + "-leftBorder");
}

function _dlgGetRightBorder(dialog) {
	if (dialog == null) return null;
	
	return document.getElementById(dialog.id + "-rightBorder");
}

function _dlgStopCapture(onMouseUpHandler, onMouseMoveHandler) {
	var capture = document.getElementById(dlgMovedDialog.id + "-capture");
	capture.style.display = "none";
	
	BAL.removeEventListener(document, "mouseup", onMouseUpHandler, false);
	BAL.removeEventListener(document, "mousemove", onMouseMoveHandler, false);
}

function dlgMoveStop(evt) {
	_dlgStopCapture(dlgMoveStop, dlgMove);
	_clearTempVariables();
}

function _clearTempVariables() {
	dlgMovedDialog = null;
	dlgMovedDialogWindow = null;
	dlgMoveOffsetX = null;
	dlgMoveOffsetY = null;
	dlgInitPositionX = null;
	dlgInitPositionY = null;
}

function dlgMove(evt) {
	var evtX = BAL.getEventX(BAL.getEvent(evt));
	var evtY = BAL.getEventY(BAL.getEvent(evt));
	var newX = evtX - dlgMoveOffsetX;
	var newY = evtY - dlgMoveOffsetY;
	var newX2 = newX + BAL.getElementWidth(dlgMovedDialogWindow);
	var newY2 = newY + BAL.getElementHeight(dlgMovedDialogWindow);
	
	var viewportWidth = BAL.getViewportWidth();
	var viewportHeight = BAL.getViewportHeight();
	
	// Prevent the dialog from leaving the viewport.
	if (newX2 > viewportWidth) {
		newX = viewportWidth - BAL.getElementWidth(dlgMovedDialogWindow);
	}
	if (newY2 >= viewportHeight) {
		newY = viewportHeight - BAL.getElementHeight(dlgMovedDialogWindow);
	}
	if (newX < 0) {
		newX = 0;
	}
	if (newY < 0) {
		newY = 0;
	}
	
	BAL.setElementX(dlgMovedDialogWindow, newX);
	BAL.setElementY(dlgMovedDialogWindow, newY);
}

var dlgResizeDX;
var dlgResizeDY;

function dlgResizeStart(evt, handle, dx, dy) {
	// IE:
	// handle.setCapture();

	dlgMovedDialog = _dlgFindDialog(handle);
	if (dlgMovedDialog == null) {
		return false;
	}
	
	dlgMovedDialogWindow = _dlgGetWindow(dlgMovedDialog);
	
	dlgResizeDX = dx;
	dlgResizeDY = dy;

	_dlgStartCapture(dlgResizeStop, dlgResize);
	
	var originX = BAL.getElementX(dlgMovedDialogWindow);
	var originY = BAL.getElementY(dlgMovedDialogWindow);
	if (dlgResizeDX > 0) {
		originX += BAL.getElementWidth(dlgMovedDialogWindow);
	}
	if (dlgResizeDY > 0) {
		originY += BAL.getElementHeight(dlgMovedDialogWindow);
	}
	
	dlgMoveOffsetX = BAL.getEventX(BAL.getEvent(evt)) - originX;
	dlgMoveOffsetY = BAL.getEventY(BAL.getEvent(evt)) - originY;

	BAL.eventStopPropagation(BAL.getEvent(evt));
	return false;
}

function dlgResize(evt) {
	if(dlgMovedDialog.lazyRequestId == null) {
		dlgMovedDialog.lazyRequestId = services.ajax.createLazyRequestID();
	}
	var posX = BAL.getEventX(BAL.getEvent(evt)) - dlgMoveOffsetX;
	var posY = BAL.getEventY(BAL.getEvent(evt)) - dlgMoveOffsetY;
	var dialogX = BAL.getElementX(dlgMovedDialogWindow);
	var dialogY = BAL.getElementY(dlgMovedDialogWindow);
	var dialogWidth = BAL.getElementWidth(dlgMovedDialogWindow);
	var dialogHeight = BAL.getElementHeight(dlgMovedDialogWindow);
	
	var viewportWidth = BAL.getViewportWidth();
	var viewportHeight = BAL.getViewportHeight();
	
	var newWidth = _adjustSize(dlgResizeDX, posX, dialogX, dialogWidth, viewportWidth, BAL.setElementX, BAL.setElementWidth);
	var newHeight = _adjustSize(dlgResizeDY, posY, dialogY, dialogHeight, viewportHeight, BAL.setElementY, BAL.setElementHeight);
	
	dlgMovedDialogWindow.width = newWidth;
	dlgMovedDialogWindow.height = newHeight;
	
	if (dlgMovedDialogWindow.resizeContent){
		dlgMovedDialogWindow.resizeContent(/*maximized*/ false);
	}
	
	services.ajax.executeOrUpdateLazy(dlgMovedDialog.lazyRequestId, "dispatchControlCommand", {
		controlCommand: "updateSize",
		controlID: dlgMovedDialog.id,
		width: newWidth,
		height: newHeight
	});
}

function _adjustSize(resizeVector, mousePosition, dialogPosition, dialogSize,
		viewportSize, positionAdjustmentFunction, sizeAdjustmentFunction) {
	var newSize = dialogSize;
	if (resizeVector < 0) {
		var newPosition = mousePosition;
		if (newPosition < 0) {
			newPosition = 0;
		}
		newSize = dialogPosition + dialogSize - newPosition;
		if (newSize < DLG_MINIMUM_SIZE_PX) {
			newSize = DLG_MINIMUM_SIZE_PX;
			newPosition = dialogPosition + dialogSize - newSize;
		}

		positionAdjustmentFunction.call(this, dlgMovedDialogWindow, newPosition);
		sizeAdjustmentFunction.call(this, dlgMovedDialogWindow, newSize);
	} 
	else if (resizeVector > 0) {
		newSize = mousePosition - dialogPosition;
	
		if (dialogPosition + newSize > viewportSize) {
			newSize = viewportSize - dialogPosition;
		}
		if (newSize < DLG_MINIMUM_SIZE_PX) {
			newSize = DLG_MINIMUM_SIZE_PX;
		}
		sizeAdjustmentFunction.call(this, dlgMovedDialogWindow, newSize);
	}
	return newSize;
}

function dlgResizeStop(evt) {
	_dlgStopCapture(dlgResizeStop, dlgResize);

	dlgMovedDialog = null;
	dlgMovedDialogWindow = null;

	// handle.releaseCapture();
	return false;
}

function dlgToggleMaximize(evt, handle) {
	var dialog = _dlgFindDialog(handle);
	var dialogWindow = _dlgGetWindow(dialog);
	if (dialogWindow == null) {
		return false;
	}
	

	if (BAL.DOM.containsClass(dialogWindow, "dlgMaximized")) {
		dlgRestore(evt, handle);
	} else {
		dlgMaximize(evt, handle);
	}
}

function dlgMaximize(evt, handle) {
	var dialog = _dlgFindDialog(handle);
	dlgMaximizeDialog(dialog);
}

function dlgMaximizeDialog(dialog){
	var dialogWindow = _dlgGetWindow(dialog);
	if (dialogWindow == null) {
		return false;
	}
	
	dialogWindow.regularSize = {
		x: BAL.getElementX(dialogWindow),
		y: BAL.getElementY(dialogWindow),
		width: BAL.getElementWidth(dialogWindow),
		height: BAL.getElementHeight(dialogWindow)
	};

	
	BAL.DOM.addClass(dialogWindow, "dlgMaximized");
	BAL.setElementX(dialogWindow, 0);
	BAL.setElementY(dialogWindow, 0);
	
	BAL.removeEventListener(window, "resize", dialog.resizeFunction);
	dialog.defaultResizeFunction = dialog.resizeFunction;
	dialog.resizeFunction = function() {
		var viewportWidth = BAL.getViewportWidth();
		var viewportHeight = BAL.getViewportHeight();
		BAL.setElementWidth(dialogWindow, viewportWidth);
		BAL.setElementHeight(dialogWindow, viewportHeight);
		dialogWindow.width = viewportWidth;
		dialogWindow.height = viewportHeight;
		
		if (dialogWindow.resizeContent){
			dialogWindow.resizeContent(/*maximized*/ true);
		}   
	};
	BAL.addEventListener(window, "resize", dialog.resizeFunction);
	dialog.resizeFunction();
}

function dlgRestore(evt, handle) {
	var dialog = _dlgFindDialog(handle);
	var dialogWindow = _dlgGetWindow(dialog);
	if (dialogWindow == null) {
		return false;
	}
	
	BAL.removeEventListener(window, "resize", dialog.resizeFunction);
	dialog.resizeFunction = dialog.defaultResizeFunction;
	BAL.DOM.removeClass(dialogWindow, "dlgMaximized");
	if ("regularSize" in dialogWindow) {
		_dlgPosition(dialogWindow, dialogWindow.regularSize.x, dialogWindow.regularSize.y, dialogWindow.regularSize.width, dialogWindow.regularSize.height);
	} 
    if (dialogWindow.resizeContent){
    	dialogWindow.resizeContent(/*maximized*/ false);
    }
    BAL.addEventListener(window, "resize", dialog.resizeFunction);
}

function dlgIgnore(evt) {
	BAL.eventStopPropagation(BAL.getEvent(evt));
	return true;
}

function dlgOpenDialog(id, title, url, width, height) {
	
	if (document.getElementById(id) != null) {
		throw new Error("Duplicate dialog id '" + id + "'.");
	}
	
	var viewportWidth = BAL.getViewportWidth();
	var viewportHeight = BAL.getViewportHeight();
	
	// Compute dialog dimension and position.
	if (width > viewportWidth) {
		width = viewportWidth - 20;
	}
	if (height > viewportHeight) {
		height = viewportHeight - 20;
	}
	var left = (viewportWidth - width) / 2;
	var top = (viewportHeight - height) / 2;
	
	// Create dialog source.
	snippet = 
		'<div id="' + id + '" class="dlgDialog">'
		+	'<div id="' + id + '-back" class="dlgBackground"></div>'
		+	'<div class="dlgWindow"'
		+	' style="width:' + width + 'px; height:' + height + 'px; top:' + top + 'px; left:' + left + 'px"'
		+	' id="' + id + '-window">'
		+		'<div class="dlgTitleBar"'
		+			' onmousedown="return dlgMoveStart(arguments[0], this);"'
		+			' ondblclick="return dlgToggleMaximize(arguments[0], this);">'
		+			'<span class="dlgBorderNW dlgResizeNW"'
		+				' onmousedown="return dlgResizeStart(arguments[0], this, -1, -1);">'
		+			'</span>'
		+			'<span class="dlgBorderNE dlgResizeNE"'
		+				' onmousedown="return dlgResizeStart(arguments[0], this, 1, -1);">'
		+			'</span>'
		+			'<span class="dlgToolbar">'
		+				'<img'
		+					' class="dlgMaximize"'
		// TODO: Theme!
		+					' src="' + WebService.prototype.CONTEXT_PATH + '/themes/default/spacer/maximise.png"'
		// TODO: I18N!
		+					' alt="Maximieren"'
		+					' title="Maximieren"'
		+					' onclick="return dlgMaximize(arguments[0], this);"'
		+					' onmousedown="return dlgIgnore(arguments[0]);"/> '
		+				'<img'
		+					' class="dlgRestore"'
		+					' src="' + WebService.prototype.CONTEXT_PATH + '/themes/default/spacer/restore.png"'
		+					' alt="Wiederherstellen"'
		+					' title="Wiederherstellen"'
		+					' onclick="return dlgRestore(arguments[0], this);"'
		+					' onmousedown="return dlgIgnore(arguments[0]);"/>'
		+			'</span>'
		+			'<span class="dlgTitle">' + _dlgEncText(title) + '</span>'
		+		'</div>'
		+		'<table class="dlgContent">'
		+			'<col class="dlgBorder"/>'
		+			'<col class="dlgContent"/>'
		+			'<col class="dlgBorder"/>'
		+			'<tbody>'
		+				'<tr class="dlgContent">'
		+					'<td class="dlgBorder dlgResizeW"'
		+						' onmousedown="return dlgResizeStart(arguments[0], this, -1, 0);">'
		+					'</td>'
		+					'<td class="dlgContent">'
		+						'<iframe class="dlgContent" src="' + _dlgEncAttr(url) + '"></iframe>'
		+					'</td>'
		+					'<td class="dlgBorder dlgResizeE"'
		+						' onmousedown="return dlgResizeStart(arguments[0], this, 1, 0);">'
		+					'</td>'
		+				'</tr>'
		+			'</tbody>'
		+		'</table>'
		+		'<div class="dlgBorder dlgResizeS"'
		+			' onmousedown="return dlgResizeStart(arguments[0], this, 0, 1);">'
		+			'<span class="dlgBorderSW dlgResizeSW"'
		+				' onmousedown="return dlgResizeStart(arguments[0], this, -1, 1);">'
		+			'</span>'
		+			'<span class="dlgBorderSE dlgResizeSE"'
		+				' onmousedown="return dlgResizeStart(arguments[0], this, 1, 1);">'
		+			'</span>'
		+		'</div>'
		+	'</div>'
		+	'<div class="dlgCapture" id="' + id + '-capture"></div>'
		+'</div>';

	// Parse dialog source into a DOM tree fragment.
	var container = document.createElement("div");
	container.innerHTML = snippet;
	var dialog = container.firstChild;
	
	// Install the DOM tree fragment into the global "dlgDialogs" element.
	document.getElementById("dlgDialogs").appendChild(dialog);
}

function dlgCloseDialog(id) {
	var dialog = document.getElementById(id);
	
	if (dialog == null) {
		throw new Error("Dialog id '" + id + "' not found in close.");
	}
	
	// Drop the dialog.
	dialog.parentNode.removeChild(dialog);
}

function _dlgEncText(text) {
	return text.replace(/\&/g, "&amp;").replace(/\</g, "&lt;").replace(/\>/g, "&gt;");
}

function _dlgEncAttr(text) {
	return _dlgEncText(text).replace(/\"/g, "&quot;").replace(/\'/g, "&#39;");
}

function dlgCenter(dlgWindowId, dlgWindowWidth, dlgWindowWidthUnit, dlgWindowHeight, dlgWindowHeightUnit) {
    var dlgWindow = document.getElementById(dlgWindowId);

    var viewPortWidth = BAL.getViewportWidth();
    var elementWidth;
    if (dlgWindowWidthUnit == "%") {
    	elementWidth = (viewPortWidth * dlgWindowWidth) / 100;
    } else {
    	elementWidth = dlgWindowWidth;
    }
    var dlgLeft = (viewPortWidth - elementWidth) / 2;

    var viewPortHeight = BAL.getViewportHeight();
    var elementHeight;
    if (dlgWindowHeightUnit == "%") {
    	elementHeight = (viewPortHeight * dlgWindowHeight) / 100;
    } else {
    	elementHeight = dlgWindowHeight;
    }
    var dlgTop = (viewPortHeight - elementHeight) / 2;
    
    _dlgPosition(dlgWindow, dlgLeft, dlgTop, elementWidth, elementHeight);
    
    var dialog = _dlgFindDialog(dlgWindow);
    dialog.resizeFunction = function() {
    	var positionX = BAL.getElementX(dlgWindow);
    	var positionY = BAL.getElementY(dlgWindow);
    	var width = BAL.getElementWidth(dlgWindow);
    	var height = BAL.getElementHeight(dlgWindow);
    	_dlgPosition(dlgWindow, positionX, positionY, width, height);
    	if (dlgWindow.resizeContent){
    		dlgWindow.resizeContent(/*maximized*/ false);
		}   
    };
    BAL.addEventListener(window, "resize", dialog.resizeFunction);
}

function _dlgPosition(dialogWindow, dialogWindowX, dialogWindowY, dialogWindowWidth, dialogWindowHeight) {
    var viewPortWidth = BAL.getViewportWidth();
    var elementWidth = Math.max(Math.min(dialogWindowWidth, viewPortWidth), DLG_MINIMUM_SIZE_PX);
    var dlgLeft = Math.max(Math.min(dialogWindowX + elementWidth, viewPortWidth) - elementWidth, 0);
    BAL.setElementX(dialogWindow, dlgLeft);
    BAL.setElementWidth(dialogWindow, elementWidth);
    dialogWindow.width = elementWidth;

    var viewPortHeight = BAL.getViewportHeight();
    var elementHeight = Math.max(Math.min(dialogWindowHeight, viewPortHeight), DLG_MINIMUM_SIZE_PX);
    var dlgTop = Math.max(Math.min(dialogWindowY + elementHeight, viewPortHeight) - elementHeight, 0);
    BAL.setElementY(dialogWindow, dlgTop);
    BAL.setElementHeight(dialogWindow, elementHeight);
    dialogWindow.height = elementHeight;
}

function dlgAdjustment(dlgId, maximized) {
	var containerHandle = document.getElementById(dlgId);
	
	var dlgContentContainer = _dlgGetContentContainer(containerHandle);
	var dlgWindow = _dlgGetWindow(containerHandle);
	var dlgHeader = _dlgGetHeader(containerHandle);
	var dlgFooter = _dlgGetFooter(containerHandle);
	var dlgLeftBorder = _dlgGetLeftBorder(containerHandle);
	var dlgRightBorder = _dlgGetRightBorder(containerHandle);
	
	var availableWidth = dlgWindow.width;
	var availableHeight = dlgWindow.height;
	var headerHeight = parseInt(TL.getTLAttribute(dlgHeader, "height"));
	var footerHeight = 0;
	var leftBorderWidth = 0;
	var rightBorderWidth = 0;
	var contentWidth = 0;
	var contentHeight = 0;
	
	if(!maximized) {
		footerHeight = parseInt(TL.getTLAttribute(dlgFooter, "height"));
		leftBorderWidth = parseInt(TL.getTLAttribute(dlgLeftBorder, "width"));
		rightBorderWidth = parseInt(TL.getTLAttribute(dlgRightBorder, "width"));
		contentWidth = availableWidth - leftBorderWidth - rightBorderWidth;
		contentHeight = availableHeight - headerHeight - footerHeight;
	} else {
		contentHeight = availableHeight - headerHeight;
		contentWidth = availableWidth;
	}
	
	BAL.setElementX(dlgContentContainer, leftBorderWidth);
	BAL.setElementY(dlgContentContainer, headerHeight);
	BAL.setElementWidth(dlgContentContainer, contentWidth);
	BAL.setElementHeight(dlgContentContainer, contentHeight);
	
	// Workaround for IE6, which does not update css defined heights on dialog resize
	BAL.setElementHeight(dlgLeftBorder, availableHeight);
	BAL.setElementHeight(dlgRightBorder, availableHeight);
	
	if (dlgContentContainer.layout){
		dlgContentContainer.layout.render(contentWidth, contentHeight);
    } else {
    	services.layout.renderLayout(dlgContentContainer, contentWidth, contentHeight);
    }
}

function dlgRegisterCaptureStopHandlers(dlgId) {
	var dialog = document.getElementById(dlgId);
	var stopDialogCapturing = function() {
		if(dlgMovedDialog != null) {
			_dlgStopCapture(dlgMoveStop, dlgMove);
			_dlgStopCapture(dlgResizeStop, dlgResize);
			dlgMovedDialog = null;
			dlgMovedDialogWindow = null;
		}
		BAL.removeEventListener(window, "resize", dialog.resizeFunction);
		BAL.removeEventListener(dialog, "DOMNodeRemovedFromDocument", stopDialogCapturing);
	};
	BAL.addEventListener(dialog, "DOMNodeRemovedFromDocument", stopDialogCapturing);
} 
