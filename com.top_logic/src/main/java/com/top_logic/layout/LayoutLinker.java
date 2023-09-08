/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogAdaptor;
import com.top_logic.basic.Logger;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentVisitor;

/**
 * {@link LayoutComponentVisitor} calling {@link LayoutComponent#linkChannels(Log)} on all
 * components of the component tree.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutLinker extends DefaultDescendingLayoutVisitor {

	private final Log _log;

	/**
	 * Creates a {@link LayoutLinker}.
	 */
	public LayoutLinker(Log log) {
		_log = log;
    }

    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		Log log = _log;
		Log adapter = new LogAdaptor() {

			@Override
			public void error(String message, Throwable ex) {
				super.error(enhance(message), ex);
			}

			@Override
			public void error(String message) {
				super.error(enhance(message));
			}

			private String enhance(String message) {
				return withoutDot(message) + " at " + aComponent.getLocation() + ".";
			}

			private String withoutDot(String message) {
				return message.endsWith(".") ? message.substring(0, message.length() - 1) : message;
			}

			@Override
			protected Log impl() {
				return log;
			}
		};

		try {
			aComponent.linkChannels(adapter);
		} catch (Throwable ex) {
			Logger.error("Cannot link channels of " + aComponent, ex, LayoutLinker.class);
		}
        return true;
    }

}