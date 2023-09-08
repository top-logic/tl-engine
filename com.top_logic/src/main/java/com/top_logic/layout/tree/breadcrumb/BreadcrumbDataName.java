/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@link BreadcrumbDataName} consists of a {@link ModelName} for the
 * {@link BreadcrumbDataOwner} of the {@link BreadcrumbData}.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public interface BreadcrumbDataName extends ModelName {

	/** The name says everything. */
	ModelName getBreadcrumbDataOwner();

	/** The name says everything. */
	void setBreadcrumbDataOwner(ModelName BreadcrumbDataOwner);

}
