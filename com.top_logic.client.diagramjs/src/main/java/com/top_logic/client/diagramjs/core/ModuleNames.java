/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import com.top_logic.client.diagramjs.command.CommandStack;
import com.top_logic.client.diagramjs.event.EventBus;

/**
 * Names for diagramJS and umlJS modules.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface ModuleNames {

	/**
	 * @see CommandStack
	 */
	public static final String COMMAND_STACK_MODULE = "commandStack";

	/**
	 * @see ElementFactory
	 */
	public static final String ELEMENT_FACTORY_MODULE = "elementFactory";

	/**
	 * @see ElementRegistry
	 */
	public static final String ELEMENT_REGISTRY_MODULE = "elementRegistry";

	/**
	 * @see Canvas
	 */
	public static final String CANVAS_MODULE = "canvas";

	/**
	 * @see TextRenderer
	 */
	public static final String TEXT_RENDERER_MODULE = "textRenderer";

	/**
	 * @see EventBus
	 */
	public static final String EVENT_BUS_MODULE = "eventBus";

	/**
	 * @see Modeler
	 */
	public static final String MODELER_MODULE = "modeling";

	/**
	 * @see Layouter
	 */
	public static final String LAYOUTER_MODULE = "layouter";

	/**
	 * @see Selection
	 */
	public static final String SELECTION_MODULE = "selection";
}
