/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * {@link ResourceProvider}, that returns images, tooltips, etc. from a given
 * {@link ResourceProvider} and the label from a given {@link LabelProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CombinedResourceProvider extends ProxyResourceProvider {

	private LabelProvider _labelProvider;

	/**
	 * Create a new {@link CombinedResourceProvider}.
	 */
	public CombinedResourceProvider(ResourceProvider resourceProvider, LabelProvider labelProvider) {
		super(resourceProvider);
		_labelProvider = labelProvider;
	}

	@Override
	public String getLabel(Object anObject) {
		return _labelProvider.getLabel(anObject);
	}

}
