/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} displaying its input object where the application shows objects of its type.
 *
 * <p>
 * Where that is, is declared per model type by the display targets of the application, so a command
 * ending in this action leads to the object wherever it belongs - without naming a view:
 * </p>
 *
 * <pre>
 * &lt;generic-command input="selection"&gt;
 *   &lt;show-object/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * <p>
 * Nothing to display (no input) passes the chain on unchanged. A selection of exactly one object
 * displays that object, so the action can follow a selection directly.
 * </p>
 *
 * @implNote Delegates to {@link ObjectNavigation#show(ReactContext, Object, Continuation)}, which
 *           may suspend the chain while the user is asked about unsaved changes on the way.
 */
@InApp
@Label("Show object")
public class ShowObjectAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link ShowObjectAction}.
	 */
	@TagName("show-object")
	public interface Config extends PolymorphicConfiguration<ShowObjectAction> {

		@Override
		@ClassDefault(ShowObjectAction.class)
		Class<? extends ShowObjectAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ShowObjectAction} from configuration.
	 */
	@CalledByReflection
	public ShowObjectAction(InstantiationContext context, Config config) {
		// Where an object is displayed is declared by the application's display targets, so this
		// action has nothing to configure.
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		Object object = single(input);
		if (object == null) {
			continuation.resume(input);
			return;
		}
		ObjectNavigation.show(context, object, new ResumeWith(continuation, input));
	}

	/**
	 * The {@link Continuation} of the surrounding chain, resumed with the value the chain had before
	 * the object was displayed.
	 */
	private static final class ResumeWith implements Continuation {

		private final Continuation _chain;

		private final Object _input;

		ResumeWith(Continuation chain, Object input) {
			_chain = chain;
			_input = input;
		}

		@Override
		public void resume(Object value) {
			_chain.resume(_input);
		}

		@Override
		public void abort() {
			_chain.abort();
		}

		@Override
		public void onAbort(Runnable compensation) {
			_chain.onAbort(compensation);
		}
	}

	/**
	 * The object the given input displays.
	 *
	 * @param input
	 *        The chain's current value: the object itself, a selection holding it, or nothing.
	 * @return The object to display, or {@code null} if there is nothing to display.
	 * @throws TopLogicException
	 *         If the input holds more than one object.
	 */
	private static Object single(Object input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Collection<?> collection) {
			if (collection.isEmpty()) {
				return null;
			}
			if (collection.size() > 1) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_A_MODEL_OBJECT__VALUE.fill(input));
			}
			return collection.iterator().next();
		}
		return input;
	}
}
