/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * A {@link MetaResourceProvider} that shows details of {@link ThemeImage} references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DebugResourceProvider extends MetaResourceProvider {

	/**
	 * Singleton {@link DebugResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final DebugResourceProvider INSTANCE = new DebugResourceProvider();

	private DebugResourceProvider() {
		// Singleton constructor.
	}

	@Override
	protected ResourceProvider getProviderImpl(Object anObject) {
		if (anObject instanceof ThemeImage) {
			return ShowImageDetails.INSTANCE;
		}
		return super.getProviderImpl(anObject);
	}

	/**
	 * Debugging-display for {@link ThemeImage}s.
	 */
	public static class ShowImageDetails extends DefaultResourceProvider {

		/**
		 * Singleton {@link ShowImageDetails} instance.
		 */
		@SuppressWarnings("hiding")
		public static final ShowImageDetails INSTANCE = new ShowImageDetails();

		private ShowImageDetails() {
			// Singleton constructor.
		}

		@Override
		public ThemeImage getImage(Object object, Flavor aFlavor) {
			return ((ThemeImage) object);
		}

		@Override
		public String getLabel(Object object) {
			return ((ThemeImage) object).toEncodedForm();
		}
	}

}
