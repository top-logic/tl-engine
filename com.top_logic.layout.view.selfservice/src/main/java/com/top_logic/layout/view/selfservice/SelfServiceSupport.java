/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Collections;
import java.util.Map;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.login.LoginFlow;
import com.top_logic.layout.view.login.LoginStep;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Helpers of the self-service actions: the values their dialogs carry on channels, and the
 * transient objects their forms edit.
 */
final class SelfServiceSupport {

	private SelfServiceSupport() {
		// Static helpers only.
	}

	/**
	 * The value of the channel with the given name in the view of the given context.
	 *
	 * @return The value, or <code>null</code> if the view has no such channel.
	 */
	static Object channel(ReactContext context, String name) {
		if (!(context instanceof ViewContext)) {
			return null;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(name)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(name)).get();
	}

	/**
	 * Clears the channel with the given name in the view of the given context, if the view has such
	 * a channel.
	 */
	static void clearChannel(ReactContext context, String name) {
		if (!(context instanceof ViewContext)) {
			return;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(name)) {
			return;
		}
		viewContext.resolveChannel(new ChannelRef(name)).set(null);
	}

	/**
	 * A new transient object of the model type with the given qualified name.
	 */
	static TLObject newTransient(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	/**
	 * The value of the attribute with the given name of the given object as string.
	 */
	static String stringValue(TLObject object, String attribute) {
		Object value = object.tValueByName(attribute);
		return value == null ? null : value.toString();
	}

	/**
	 * Closes the top dialog, if any, and opens the given dialog view in its place.
	 *
	 * <p>
	 * The dialog does not close on a click on the backdrop: an accidental click outside must not
	 * abort the flow.
	 * </p>
	 *
	 * @param context
	 *        The context of the action.
	 * @param view
	 *        The dialog view, relative to {@link ViewLoader#VIEW_BASE_PATH}.
	 * @param channels
	 *        The initial values of channels of the dialog, by channel name.
	 */
	static void replaceTopDialog(ReactContext context, String view, Map<String, Object> channels) {
		closeTopDialog(context);
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + view, false, channels,
			Collections.emptyList());
	}

	/**
	 * Closes the top dialog, if any.
	 */
	static void closeTopDialog(ReactContext context) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
	}

	/**
	 * Logs a new account in.
	 *
	 * <p>
	 * The account runs the {@link LoginStep}s of the React login in a {@link LoginFlow}, exactly as
	 * after a password login, e.g. sets a second factor up. After the last step the window loads the
	 * root of the view application ({@link ViewServlet#ROOT_PATH}), not the page the account was
	 * created on.
	 * </p>
	 */
	static void logIn(ReactContext context, Person account) {
		LoginFlow flow = new LoginFlow(account);
		flow.set(LoginFlow.LANDING_PAGE, context.getContextPath() + ViewServlet.ROOT_PATH);
		flow.begin(context);
	}

	/**
	 * Whether the given string is <code>null</code> or empty.
	 */
	static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
