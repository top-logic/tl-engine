/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.ui.model.structure.SearchFilter;

/**
 * {@link SearchFilter} that matches objects that are referenced from other objects in a certain
 * reference-valued attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface IncomingReferenceFilter extends IncomingReferenceBased, NavigatingFilter {

	// Nothing needed but the combination of the two super classes.

}
