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
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;

/**
 * {@link LeafPath} returning the {@link Selectable#getSelected() selection} of the given component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SharedInstance
public final class Selection extends LeafPath {

	/** Singleton {@link Selection} instance. */
	public static final Selection INSTANCE = new Selection();

	/**
	 * Marker configuration to get the singleton instance of {@link Selection}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName(PathSecurityObjectProvider.PATH_ELEMENT_SELECTION)
	public interface Config extends PolymorphicConfiguration<Selection> {
		// Just to have a tag name.
	}

	private final PolymorphicConfiguration<Selection> _config = TypedConfiguration.newConfigItem(Config.class);

	private Selection() {
	}


	@Override
	public PolymorphicConfiguration<? extends SecurityPath> getConfig() {
		return _config;
	}

	@Override
	public Object getModel(LayoutComponent component, Object model, BoundCommandGroup group, int pathIndex, int size) {
		return (component instanceof Selectable) ? ((Selectable) component).getSelected() : null;
	}

}
