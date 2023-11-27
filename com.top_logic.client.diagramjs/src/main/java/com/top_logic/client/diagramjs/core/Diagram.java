/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.binding.UmlJS;
import com.top_logic.client.diagramjs.command.CommandStack;
import com.top_logic.client.diagramjs.event.EventBus;

/**
 * A user-editable diagram.
 * 
 * @see UmlJS#createDiagramId(String, String)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Diagram extends JavaScriptObject {

	/**
	 * Creates a {@link Diagram}.
	 */
	protected Diagram() {
		super();
	}

	/**
	 * The {@link ElementFactory} to use for creating components of this {@link Diagram}.
	 */
	public ElementFactory getElementFactory() {
		return getModule(ModuleNames.ELEMENT_FACTORY_MODULE).cast();
	}

	/**
	 * The {@link ElementRegistry} to keep track of the {@link Diagram} elements.
	 */
	public ElementRegistry getElementRegistry() {
		return getModule(ModuleNames.ELEMENT_REGISTRY_MODULE).cast();
	}

	/**
	 * The {@link Canvas} to add components to.
	 */
	public Canvas getCanvas() {
		return getModule(ModuleNames.CANVAS_MODULE).cast();
	}

	/**
	 * The {@link Modeler} to use modeling component of this {@link Diagram}.
	 */
	public Modeler getModeler() {
		return getModule(ModuleNames.MODELER_MODULE).cast();
	}

	/**
	 * The {@link Layouter} to layout components of this {@link Diagram}.
	 */
	public Layouter getLayouter() {
		return getModule(ModuleNames.LAYOUTER_MODULE).cast();
	}

	/**
	 * The {@link EventBus} of this {@link Diagram}.
	 */
	public EventBus getEventBus() {
		return getModule(ModuleNames.EVENT_BUS_MODULE).cast();
	}

	/**
	 * The {@link TextRenderer} of this {@link Diagram}.
	 */
	public TextRenderer getTextRenderer() {
		return getModule(ModuleNames.TEXT_RENDERER_MODULE).cast();
	}

	/**
	 * The {@link CommandStack} of this {@link Diagram}.
	 */
	public CommandStack getCommandStack() {
		return getModule(ModuleNames.COMMAND_STACK_MODULE).cast();
	}

	/**
	 * The {@link Selection} of this {@link Diagram}.
	 */
	public Selection getSelection() {
		return getModule(ModuleNames.SELECTION_MODULE).cast();
	}

	private native JavaScriptObject getModule(String moduleName) /*-{
		return this.get(moduleName);
	}-*/;

}
