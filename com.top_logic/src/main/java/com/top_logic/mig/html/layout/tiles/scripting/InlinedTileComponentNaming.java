/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.component.ComponentBasedNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;

/**
 * {@link ModelNamingScheme} that resolves {@link InlinedTileComponent}s by name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class InlinedTileComponentNaming extends
		ComponentBasedNamingScheme<InlinedTileComponent, InlinedTileComponentNaming.InlinedTileName> {

	/**
	 * Singleton {@link InlinedTileComponentNaming} instance.
	 */
	public static final InlinedTileComponentNaming INSTANCE = new InlinedTileComponentNaming();

	/**
	 * Name of an {@link InlinedTileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface InlinedTileName extends ComponentBasedNamingScheme.ComponentBasedName {
		// Marker interface
	}

	private InlinedTileComponentNaming() {
		super(InlinedTileComponent.class, InlinedTileName.class);
	}

	@Override
	protected LayoutComponent getContextComponent(InlinedTileComponent model) {
		return model;
	}

	@Override
	public InlinedTileComponent locateModel(ActionContext context, InlinedTileName name) {
		return (InlinedTileComponent) locateComponent(context, name);
	}

	@Override
	public void checkVisible(ActionContext context, ConfigurationItem contextConfig, LayoutComponent component) {
		// When an element of an InlinedTileComponent is selected, an ordinary select action is
		// recorded. In this case the InlinedTileComponent is not visible. It is made visible
		// because the surrounding group component receives its selection events and makes it
		// visible.
	}

}