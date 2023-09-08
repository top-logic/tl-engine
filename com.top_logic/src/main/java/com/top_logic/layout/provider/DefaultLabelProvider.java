/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link LabelProvider} implementation that uses the {@link Object#toString()}
 * method for transforming application objects into "descriptive" texts.
 * 
 * <p>
 * Note: This class is only intended for debugging, or as last resort, if no
 * other {@link LabelProvider} can be found.
 * </p>
 * 
 * @see FormattedLabelProvider for a {@link LabelProvider} that is backed by
 *      {@link HTMLFormatter#formatObject(Object)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultLabelProvider implements LabelProvider {

	/**
	 * Reference to the only instance of this class.
	 */
	public static final LabelProvider INSTANCE = new DefaultLabelProvider();
	
	/**
	 * No instances of {@link DefaultLabelProvider} can be constructed outside
	 * its class. Use {@link DefaultLabelProvider#INSTANCE} instead.
	 */
	private DefaultLabelProvider() {
		// Singleton constructor.
	}
	
	/**
	 * Uses the {@link Object#toString()} conversion to convert an arbitrary
	 * object into a textual representation.
	 * 
	 * @return The result of {@link Object#toString()} for the given object, or
	 *         the empty string, if the given reference is <code>null</code>.
	 * 
	 * @see LabelProvider#getLabel(Object)
	 */
	@Override
	public String getLabel(Object object) {
		if (object == null) { 
			return "";
		} else { 
			return object.toString();
		}
	}

}
