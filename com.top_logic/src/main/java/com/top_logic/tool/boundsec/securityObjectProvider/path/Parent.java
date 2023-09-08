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
 * {@link IntermediatePath} delegating to the {@link LayoutComponent#getParent() parent} of the
 * given component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public final class Parent extends IntermediatePath {

	/** Singleton {@link Parent} instance. */
	public static final Parent INSTANCE = new Parent();

	/**
	 * Marker configuration to get the singleton instance of {@link Parent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(PathSecurityObjectProvider.PATH_ELEMENT_PARENT)
	public interface Config extends PolymorphicConfiguration<Parent> {
		// Just to have a tag name.
	}

	private final PolymorphicConfiguration<Parent> _config = TypedConfiguration.newConfigItem(Config.class);

	private Parent() {
	}


	@Override
	public PolymorphicConfiguration<? extends SecurityPath> getConfig() {
		return _config;
	}

	@Override
	public LayoutComponent nextComponent(LayoutComponent component, int pathIndex, int size) {
		return component.getParent();
	}

}
