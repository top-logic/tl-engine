/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.layout.ResourceView;

/**
 * Implementation of {@link ResourceView} that bases its lookup on another
 * {@link ResourceView} instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NestedResourceView implements ResourceView {
	private ResourceView parent;
	private String sectionKey;
	
	/**
	 * Create a view into the given {@link ResourceView} that looks up resources
	 * in the given section.
	 */
	public NestedResourceView(ResourceView parent, String sectionKey) {
		this.parent = parent;
		this.sectionKey = sectionKey;
	}

	@Override
	public String getStringResource(String resourceKey) {
		return parent.getStringResource(getRelativeResourceName(resourceKey));
	}

	@Override
	public String getStringResource(String resourceKey, String defaultValue) {
		return parent.getStringResource(getRelativeResourceName(resourceKey), defaultValue);
	}

	@Override
	public boolean hasStringResource(String resourceKey) {
		return parent.hasStringResource(getRelativeResourceName(resourceKey));
	}

	private String getRelativeResourceName(String resourceKey) {
		return this.sectionKey + DefaultResourceView.KEY_SEPARATOR + resourceKey;
	}

}
