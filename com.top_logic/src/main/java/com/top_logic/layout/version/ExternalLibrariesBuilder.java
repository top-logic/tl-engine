/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import java.util.Collection;

import com.top_logic.basic.version.Version;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Creates the list of {@link VersionInfo}s for libraries used in the running application.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExternalLibrariesBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ExternalLibrariesBuilder} instance.
	 */
	public static final ExternalLibrariesBuilder INSTANCE = new ExternalLibrariesBuilder();

	private ExternalLibrariesBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return Version.getApplicationVersion().getDependencies();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof VersionInfo;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}

