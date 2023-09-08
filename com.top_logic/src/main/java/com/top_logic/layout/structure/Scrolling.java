/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Strategy for scroll bar display.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Scrolling implements ExternallyNamed {

	/**
	 * Show a scroll bar if the content does not fit.
	 */
	AUTO(HTMLConstants.SCROLLING_AUTO_VALUE),

	/**
	 * Never show scroll bars, clip content instead.
	 */
	NO(HTMLConstants.SCROLLING_NO_VALUE),

	/**
	 * Always show scroll bars.
	 */
	YES(HTMLConstants.SCROLLING_YES_VALUE),

	;

	private String _scrollingValue;

	private Scrolling(String scrollingAttribute) {
		_scrollingValue = scrollingAttribute;
	}

	/**
	 * Value useful for the {@link HTMLConstants#SCROLLING} attribute.
	 */
	public String toScrollingValue() {
		return _scrollingValue;
	}

	/**
	 * Value useful as <code>overflow</code> style value.
	 */
	public String toOverflowAttribute() throws UnreachableAssertion {
		switch (this) {
			case YES:
				return "scroll";
			case NO:
				return "hidden";
			case AUTO:
				return "auto";
			default:
				throw new UnreachableAssertion("No such value: " + this);
		}
	}

	@Override
	public String getExternalName() {
		return toScrollingValue();
	}

}
