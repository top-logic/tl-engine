/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.util;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link ApplicationAction}s.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ActionResourceProvider implements LabelProvider {

	private static final ResKey I18N_NO_ACTION = I18NConstants.NO_ACTION;

	private final LabelProvider _labels = new ConfigLabelProvider();

	/**
	 * Creates a new {@link ResourceProvider} for {@link ApplicationAction}s.
	 * 
	 * <p>
	 * Note: This instance must no be shared among sessions, since it caches localized templates.
	 * </p>
	 */
	public static ResourceProvider newInstance() {
		return LabelResourceProvider.toResourceProvider(new ActionResourceProvider());
	}

	/**
	 * Creates a {@link ActionResourceProvider}.
	 * 
	 * <p>
	 * Note: This instance must no be shared among sessions, since it caches localized templates.
	 * </p>
	 * 
	 * @see #newInstance()
	 */
	private ActionResourceProvider() {
		// See JavaDoc above.
	}

	@Override
	public String getLabel(Object action) {
		if (action == null) {
			return Resources.getInstance().getString(I18N_NO_ACTION);
		}
		assert (action instanceof ApplicationAction) : "Unsupported object! Can only create labels for ApplicationActions, but got: "
			+ StringServices.getObjectDescription(action);
		return getActionDescription((ApplicationAction) action);
	}

	private String getActionDescription(ApplicationAction action) {
		String result;
		if (action.getComment().isEmpty()) {
			result = buildSurrogateName(action);
		} else {
			result = action.getComment();
		}

		String userID = action.getUserID();
		if (!(userID.isEmpty() || userID.equals("root"))) {
			return userID + ": " + result;
		} else {
			return result;
		}
	}

	private String buildSurrogateName(ApplicationAction action) {
		try {
			return _labels.getLabel(action);
		} catch (RuntimeException ex) {
			/* Ensure that getting label for action does not fail. This would be a problem when
			 * action fails, and this LabelProvider is used to get the error message. In that case
			 * the original error would be masked. */
			Logger.error("Unable to get label for action '" + action + "'.", ex);
			return String.valueOf(action);
		}
	}

}
