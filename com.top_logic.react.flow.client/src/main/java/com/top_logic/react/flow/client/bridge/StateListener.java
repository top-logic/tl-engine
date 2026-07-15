package com.top_logic.react.flow.client.bridge;

import jsinterop.annotations.JsFunction;

/**
 * Callback for receiving SSE state updates from the React infrastructure.
 *
 * <p>
 * Registered via {@link ReactBridge#subscribe(String, StateListener)} to receive
 * state/patch events for a specific control ID.
 * </p>
 */
@JsFunction
public interface StateListener {

	/**
	 * Called when a state update arrives via SSE.
	 *
	 * @param state
	 *        The state object from the SSE event.
	 */
	void onState(Object state);

}
