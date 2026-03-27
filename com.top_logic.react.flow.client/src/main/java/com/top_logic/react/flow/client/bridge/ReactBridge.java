package com.top_logic.react.flow.client.bridge;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JsInterop bridge to the React SSE/Command infrastructure.
 *
 * <p>
 * Provides access to {@code window.TLReact.subscribe()} for receiving SSE events
 * and {@code fetch('/react-api/command')} for sending commands to the server.
 * </p>
 *
 * <p>
 * This bridge enables GWT-compiled Java code to participate in the React View
 * framework's communication channels without requiring legacy AJAX servlets.
 * </p>
 */
public class ReactBridge {

	/**
	 * Subscribe to SSE state updates for a control.
	 *
	 * @param controlId
	 *        The server-side control ID.
	 * @param listener
	 *        Callback receiving state objects.
	 */
	public static native void subscribe(String controlId, StateListener listener) /*-{
		if ($wnd.TLReact && $wnd.TLReact.subscribe) {
			$wnd.TLReact.subscribe(controlId, listener);
		} else {
			$wnd.console.error('[ReactBridge] TLReact.subscribe not available');
		}
	}-*/;

	/**
	 * Unsubscribe from SSE state updates.
	 *
	 * @param controlId
	 *        The server-side control ID.
	 * @param listener
	 *        The previously registered listener.
	 */
	public static native void unsubscribe(String controlId, StateListener listener) /*-{
		if ($wnd.TLReact && $wnd.TLReact.unsubscribe) {
			$wnd.TLReact.unsubscribe(controlId, listener);
		}
	}-*/;

	/**
	 * Send a command to the server via the React command endpoint.
	 *
	 * @param contextPath
	 *        Base path (e.g. "/demo").
	 * @param controlId
	 *        The control ID.
	 * @param command
	 *        Command name (e.g. "click", "selection").
	 * @param windowName
	 *        Browser window identifier.
	 * @param arguments
	 *        JSON-serializable arguments object, may be null.
	 */
	public static native void sendCommand(
			String contextPath, String controlId, String command,
			String windowName, JavaScriptObject arguments) /*-{
		var url = contextPath + '/react-api/command';
		var body = {
			controlId: controlId,
			command: command,
			windowName: windowName,
			arguments: arguments || {}
		};
		var promise = $wnd.fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(body)
		});
		promise['catch'](function(err) {
			$wnd.console.error('[ReactBridge] Command failed:', err);
		});
	}-*/;

}
