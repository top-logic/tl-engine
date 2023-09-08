/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider.path;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;

/**
 * {@link IntermediatePath} delegating to the {@link LayoutComponent#getDialogParent() opener} of
 * the given component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public final class Opener extends IntermediatePath {

	/** Singleton {@link Opener} instance. */
	public static final Opener INSTANCE = new Opener();

	/**
	 * Marker configuration to get the singleton instance of {@link Opener}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(PathSecurityObjectProvider.PATH_ELEMENT_OPENER)
	public interface Config extends PolymorphicConfiguration<Opener> {
		// Just to have a tag name.
	}

	private final PolymorphicConfiguration<Opener> _config = TypedConfiguration.newConfigItem(Config.class);

	private Opener() {
	}


	@Override
	public PolymorphicConfiguration<? extends SecurityPath> getConfig() {
		return _config;
	}

	@Override
	public LayoutComponent nextComponent(LayoutComponent component, int pathIndex, int size) {
		if (component.openedAsDialog()) {
			return component.getDialogParent();
		} else {
			return null;
		}
	}

}
