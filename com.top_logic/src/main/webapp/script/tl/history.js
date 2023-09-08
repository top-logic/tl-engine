// just to have same structure as in main window 
services = {

	ajax : {
		topWindow : parent,
		mainLayout : (function() {
				if (parent.opener) {
					// external window
					return parent.opener;
				} else {
					// main layout
					return parent;
				}
				})()
	},

	form : new Object()
}

services.form.History = {

	safetyFrame : "safetyFrame",
	stackOpened : "stackOpened",
	safetyCommand : "startReplay",
	stackOpenedCommand : "startReplay",
	changedCommand : "changed",
	updateHistoryCommand : "updateHistory",
	replaying : "replaying",

	checkChanged : function(ctrlId) {
		var self = this;

		var outerWindow = services.ajax.topWindow;
		var historyControl = outerWindow.document.getElementById(ctrlId);

		var oldState = outerWindow.BAL.DOM.getNonStandardAttribute(historyControl, 'data-historystate');
			// the state of the page currently loaded in the history frame
		var requestedState = services.ajax.currentState;
	
		if (this.replaying == oldState) {
				// The server currently replay the history.
			this.updateHistory(ctrlId);
			return;
		}
		
		if (this.safetyFrame == oldState) {
			// the control is rendered initial, i.e. the document is the first in the
			// history frame
			var startIndex = outerWindow.BAL.DOM.getNonStandardAttribute(historyControl, 'data-replayfrom');
			var intervalFunction = window.setInterval(function() {
				if (outerWindow.BAL.isMainLayoutReady()) {
					outerWindow.services.ajax.execute('dispatchControlCommand', {
						controlCommand : self.safetyCommand,
						isSystemCommand: true,
						controlID : ctrlId,
						startIndex : parseInt(startIndex),
						callingFrame : self.getIFrameID(ctrlId)
					}, /*useWaitPane*/ false);
					window.clearInterval(intervalFunction);
				}
			}, 100);
			return;
		}
		
		if (this.stackOpened == oldState) {
			// this document is the first in the history frame.
			var startIndex = outerWindow.BAL.DOM.getNonStandardAttribute(historyControl, 'data-replayfrom');
			outerWindow.services.ajax.execute('dispatchControlCommand', {
				controlCommand : self.stackOpenedCommand,
				isSystemCommand: true,
				controlID : ctrlId,
				startIndex : parseInt(startIndex),
				callingFrame : self.getIFrameID(ctrlId)
			}, /*useWaitPane*/ false);
			return;
		}
		
		if (requestedState != oldState) {
			// the state in the html document and the state of the history control
			// differ, so the user has pushed browser back or forward.
			outerWindow.BAL.DOM.setNonStandardAttribute(historyControl, 'data-historystate', requestedState);
			outerWindow.services.ajax.execute('dispatchControlCommand', {
				controlCommand : self.changedCommand,
				isSystemCommand: true,
				controlID : ctrlId,
				requestedState : requestedState
			}, /*useWaitPane*/ false);
		}
	},
	
	/**
	 * Informs the server that the next element of the history (currently on the
	 * server) has to be replayed to the client.
	 * 
	 * @param ctrlId
	 *        the id of the control rendering the IFrames which are responsible for
	 *        the history
	 */
	updateHistory : function(ctrlId) {
		var self = this;
		var outerWindow = services.ajax.topWindow;
		outerWindow.services.ajax.execute('dispatchControlCommand', {
			controlCommand : self.updateHistoryCommand,
			controlID : ctrlId,
			isSystemCommand: true,
			callingFrame : self.getIFrameID(ctrlId)
		}, /*useWaitPane*/ false);
	},
	
	/**
	 * Determines the id of the IFrame which is direct child of the control with the
	 * given id in the outerWindow and which has this window as contentWindow.
	 * 
	 * @param ctrlId
	 *        the Id of the control containing the IFrames to inspect
	 */
	getIFrameID : function(ctrlId) {
		var outerWindow = services.ajax.topWindow;
		var control = outerWindow.document.getElementById(ctrlId);
		for ( var child = control.firstChild; child != null; child = child.nextSibling) {
			var tagName = child.tagName;
			if (tagName != null && tagName.toLowerCase() == "iframe") {
				if (child.contentWindow == window) {
					return child.id;
				}
			}
		}
		throw new Error("Unable to find IFrame as child of control with id '" + ctrlId + "' containing this window");
	}

}
