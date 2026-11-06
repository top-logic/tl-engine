/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * Transforming {@link LabelProvider} implementation using {@link Resources}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class ResourcedLabelProvider implements LabelProvider {

    /**
     * The {@link LabelProvider} computing the label suffix appended to {@link #prefix}.
     */
    protected final LabelProvider base;

	/**
	 * The prefix of the resource keys used for the labels.
	 */
	protected final ResPrefix prefix;

	public ResourcedLabelProvider(LabelProvider aBase, ResPrefix resPrefix) {
        this.base   = aBase;
		this.prefix = resPrefix == null ? ResPrefix.GLOBAL : resPrefix;
    }

    /**
     * Fetch label via base {@link LabelProvider} then translate via Resources.
     */
	@Override
	public String getLabel(Object object) {
	    String baseLabel = this.base.getLabel(object);
		return Resources.getInstance().getString(this.prefix.key(baseLabel));
	}
}
