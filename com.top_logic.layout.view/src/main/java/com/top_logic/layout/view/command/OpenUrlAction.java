/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.OpenUrlEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * {@link ViewAction} that opens an address in the browser - a web page, e.g. the homepage of the
 * object the user selected - in a window (or tab) of its own or in the window of the application.
 *
 * <p>
 * The address is computed by a TL-Script over the arguments of the chain, or is the value the
 * action processes. A chain that computes no address opens nothing and goes on.
 * </p>
 *
 * <p>
 * Example: a toolbar button opening the homepage of the selected company:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.GenericViewCommand" inputs="selection as company"&gt;
 *     &lt;executability&gt;
 *       &lt;null-input-disabled/&gt;
 *     &lt;/executability&gt;
 *     &lt;open-url url="$company.get(`demo:Company#homepage`)"/&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * <p>
 * A browser opens a window only shortly after a click of the user, so the action belongs to a
 * command the user triggers - a button, a menu entry, the activation of a row - and runs before
 * anything that takes long.
 * </p>
 *
 * @implNote Sends an {@link OpenUrlEvent} through {@link SSEUpdateQueue#openUrl(String, boolean)}.
 */
@InApp
public class OpenUrlAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link OpenUrlAction}.
	 */
	@TagName("open-url")
	public interface Config extends PolymorphicConfiguration<OpenUrlAction>, NamedInputs, WithValueArgument {

		/** Configuration name for {@link #getUrl()}. */
		String URL = "url";

		/** Configuration name for {@link #getNewWindow()}. */
		String NEW_WINDOW = "new-window";

		@Override
		@ClassDefault(OpenUrlAction.class)
		Class<? extends OpenUrlAction> getImplementationClass();

		/**
		 * TL-Script computing the address to open.
		 *
		 * <p>
		 * Uses the arguments of the chain and the values of the {@link #getInputs() inputs} as
		 * variables named after them, without declaring them. The result is an absolute address,
		 * or one relative to the address of the application's page. When unset, the value this
		 * action processes is the address. Where there is no address, nothing is opened.
		 * </p>
		 */
		@Name(URL)
		@Label("Address")
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getUrl();

		/**
		 * Whether the address opens in a browser window (or tab) of its own instead of the window
		 * of the application.
		 *
		 * <p>
		 * Opened in the window of the application, the address replaces the application's page.
		 * </p>
		 */
		@Name(NEW_WINDOW)
		@BooleanDefault(true)
		boolean getNewWindow();
	}

	private final ActionScript _url;

	private final boolean _newWindow;

	/**
	 * Creates a new {@link OpenUrlAction} from configuration.
	 */
	@CalledByReflection
	public OpenUrlAction(InstantiationContext context, Config config) {
		this(config.getUrl() == null ? null
			: ActionScript.compile(config.getUrl(), ActionScope.of(context), config.getInputs()),
			config.getNewWindow());
	}

	/**
	 * Creates an {@link OpenUrlAction}.
	 *
	 * @param url
	 *        Computes the address to open, {@code null} to open the value the action processes.
	 * @param newWindow
	 *        Whether the address opens in a browser window of its own.
	 */
	public OpenUrlAction(ActionScript url, boolean newWindow) {
		_url = url;
		_newWindow = newWindow;
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		Object input = arguments.value();
		String url = address(_url == null ? input : _url.execute(context, arguments));
		if (url != null) {
			SSEUpdateQueue queue = context.getSSEQueue();
			if (queue == null) {
				// A chain running headless has no browser to open the address in.
				Logger.info("No window to open '" + url + "' in.", OpenUrlAction.class);
			} else {
				queue.openUrl(url, _newWindow);
			}
		}
		continuation.resume(input);
	}

	/**
	 * The address the given result of the script names, {@code null} for none.
	 */
	private static String address(Object result) {
		String text = AbstractColumnDeclaration.tooltipText(result);
		if (text == null) {
			return null;
		}
		text = text.strip();
		return text.isEmpty() ? null : text;
	}

	/**
	 * Opens a window in the client, or leaves its page.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
