/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.base.taglibs.basic.AbstractTagBase;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tag.JSPErrorUtil;

/**
 * Abstract base class for all component-based tags.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTag extends AbstractTagBase implements LayoutTag, HTMLConstants {

    /** The Component we cooperate with */
    private LayoutComponent component;


    /**
	 * Convenient access to the {@link LayoutComponent} that did request
	 * rendering this tag.
	 */
    @Override
	public LayoutComponent getComponent() {
		if (this.component == null) {
			LayoutTag layoutAncestorTag = (LayoutTag) findAncestorWithClass(this, LayoutTag.class);

			assert layoutAncestorTag != null : "A '" + this.getClass().getName() +
				"' tag must be nested inside a " + LayoutTag.class.getName() + " tag.";

			this.component = layoutAncestorTag.getComponent();
		}
    	return this.component;
    }

	@Override
	protected void teardown() {
		this.component = null;

		super.teardown();
	}

	@Override
	protected String getErrorMessage() {
		return "Error occured during rendering of tag '" + getClass() + "' of component "
			+ JSPErrorUtil.getComponentInformation(component) + ".";
	}
}
