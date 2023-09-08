/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * This label provider can be used for all fields where the options
 * are strings. This label provider uses the given resource, the
 * key and the options as resource key (e.g. prefix = reporting.filter.date.,
 * option = month). The resource keys are ending with a dot!
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class StringOptionLabelProvider implements LabelProvider {

    protected Resources resource;

	protected ResPrefix prefix;

    /**
     * Creates a {@link StringOptionLabelProvider}.
     */
	public StringOptionLabelProvider(ResPrefix resPrefix) {
		this(Resources.getInstance(), resPrefix);
    }
    
    /**
	 * Creates a {@link StringOptionLabelProvider}.
	 * 
	 * @param aResource
	 *        A resouce. Must not be <code>null</code>.
	 * @param resPrefix
	 *        A prefix. Must not be <code>null</code> but maybe empty.
	 */
	public StringOptionLabelProvider(Resources aResource, ResPrefix resPrefix) {
        this.resource = aResource;
		this.prefix = resPrefix;
    }

    @Override
	public String getLabel(Object aObject) {
		return this.resource.getString(this.prefix.key(aObject.toString()));
    }

}

