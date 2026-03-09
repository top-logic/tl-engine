package com.top_logic.layout.react.control;

/**
 * Sink for user-visible error, warning, and info messages.
 *
 * <p>
 * Implementations typically forward messages to a UI notification component such as a snackbar. The
 * sink is propagated through the view context so that controls can report errors to whatever
 * notification component is in scope.
 * </p>
 */
public interface ErrorSink {

	/**
	 * Shows an error message to the user.
	 *
	 * @param message
	 *        The error message text.
	 */
	void showError(String message);

	/**
	 * Shows a warning message to the user.
	 *
	 * @param message
	 *        The warning message text.
	 */
	void showWarning(String message);

	/**
	 * Shows an informational message to the user.
	 *
	 * @param message
	 *        The info message text.
	 */
	void showInfo(String message);
}
