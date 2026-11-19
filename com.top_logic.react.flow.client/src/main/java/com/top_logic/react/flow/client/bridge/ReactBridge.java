package com.top_logic.react.flow.client.bridge;

/**
 * {@code JsInterop} bridge to the React SSE/Command infrastructure.
 *
 * <p>
 * Provides access to {@code window.TLReact.subscribe()} for receiving SSE events. Commands reach the
 * server through the {@link CommandSender} the React component mounting the diagram provides.
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

}
