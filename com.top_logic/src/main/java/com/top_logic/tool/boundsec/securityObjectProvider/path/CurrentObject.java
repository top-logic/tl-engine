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
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;

/**
 * {@link LeafPath} returning the {@link BoundChecker#getCurrentObject(BoundCommandGroup, Object)} of the
 * given component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public final class CurrentObject extends LeafPath {

	/** Singleton {@link CurrentObject} instance. */
	public static final CurrentObject INSTANCE = new CurrentObject();

	/**
	 * Marker configuration to get the singleton instance of {@link CurrentObject}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(PathSecurityObjectProvider.PATH_ELEMENT_CURRENTOBJECT)
	public interface Config extends PolymorphicConfiguration<CurrentObject> {
		// Just to have a tag name.
	}

	private final PolymorphicConfiguration<CurrentObject> _config = TypedConfiguration.newConfigItem(Config.class);

	private CurrentObject() {
	}

	@Override
	public Object getModel(LayoutComponent component, Object model, BoundCommandGroup group, int pathIndex, int size) {
		if (component instanceof BoundChecker) {
			return ((BoundChecker) component).getCurrentObject(group, component.getModel());
		} else {
			return null;
		}
	}

	@Override
	public PolymorphicConfiguration<? extends SecurityPath> getConfig() {
		return _config;
	}

}
