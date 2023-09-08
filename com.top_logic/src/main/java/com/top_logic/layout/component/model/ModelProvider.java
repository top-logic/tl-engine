/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.layout.channel.linking.Provider;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provides a model for some business component.
 * 
 * @see Provider#getImpl()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Options(fun = AllInAppImplementations.class)
public interface ModelProvider {

	/**
	 * Build up a model for the given component.
	 * 
	 * @param businessComponent
	 *        The component to get the model for.
	 * @return The requested model. May be <code>null</code> if this is a valid business model.
	 */
	public Object getBusinessModel(LayoutComponent businessComponent);

}
