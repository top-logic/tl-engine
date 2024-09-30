/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;

/**
 * A plain localizable text represented by its resource key.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceText implements DisplayValue {

	private final ResKey resourceKey;
	private final DisplayValue defaultValue;

	public ResourceText(ResKey resourceKey) {
		this(resourceKey, null);
	}
	
	public ResourceText(ResKey resourceKey, DisplayValue defaultValue) {
		this.resourceKey = resourceKey;
		this.defaultValue = defaultValue;
	}

	@Override
	public final String get(DisplayContext context) {
		if (this.defaultValue == null) {
			return context.getResources().getString(this.resourceKey);
		} else {
			String result = context.getResources().getStringOptional(this.resourceKey);
			if (result != null) {
				return result;
			}
			
			return defaultValue.get(context);
		}
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		if (this.defaultValue == null) {
			out.append(context.getResources().getString(this.resourceKey));
		} else {
			String result = context.getResources().getStringOptional(this.resourceKey);
			if (result != null) {
				out.append(result);
				return;
			}

			defaultValue.append(context, out);
		}
	}

	@Override
	public String toString() {
		return this.resourceKey.toString();
	}
	
}
