/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;

import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} analysing and determines the component to edit.
 * 
 * <p>
 * This rule allows a command when the edited component is created from a template layout.
 * </p>
 * 
 * @see IsTemplateLayout
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EditableComponentExecutability implements ExecutabilityRule {

	/** Argument key for the arguments map to set the edited component programmatically. */
	public static final String EDITED_COMPONENT_ARG = "editedComponent";

	/** Singleton {@link EditableComponentExecutability} instance. */
	public static final EditableComponentExecutability INSTANCE = new EditableComponentExecutability();

	/**
	 * Creates a new {@link EditableComponentExecutability}.
	 */
	protected EditableComponentExecutability() {
		// singleton instance
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		LayoutComponent editedComponent = resolveEditedComponent(component, someValues);

		if (editedComponent != null) {
			return IsTemplateLayout.INSTANCE.isExecutable(editedComponent, model, someValues);
		}

		return noEditableComponent(component);
	}

	/**
	 * {@link ExecutableState} for this command when no editable component could be found.
	 */
	private ExecutableState noEditableComponent(LayoutComponent component) {
		if (component instanceof TabComponent) {
			return ExecutableState.createDisabledState(I18NConstants.NO_TAB_SELECTED_ERROR);
		}
		return ExecutableState.createDisabledState(I18NConstants.NO_EDITABLE_COMPONENT_ERROR);
	}

	/**
	 * Resolves the top-level {@link LayoutComponent} of the layout definition that is being edited
	 * when performing the command "edit view" of the given component.
	 * 
	 * @param arguments
	 *        Arguments for the command execution to determine a programmatically given component.
	 */
	public static LayoutComponent resolveEditedComponent(LayoutComponent component, Map<String, Object> arguments) {
		LayoutComponent editedComponent = (LayoutComponent) arguments.get(EDITED_COMPONENT_ARG);
		if (editedComponent != null) {
			return editedComponent;
		}

		if (component instanceof TabComponent) {
			editedComponent = getSelectedCard((TabComponent) component);
		} else if (component instanceof InlinedTileComponent) {
			// Inlined components are not edited directly.
			editedComponent =
				LayoutTemplateUtils.getLayoutContextComponent(((InlinedTileComponent) component).getChild());
		} else {
			editedComponent = LayoutTemplateUtils.getLayoutContextComponent(component);
		}
		return editedComponent;
	}

	private static LayoutComponent getSelectedCard(TabComponent component) {
		int selectedIndex = component.getSelectedIndex();

		return selectedIndex != -1 ? component.getCard(selectedIndex).getContent() : null;
	}

}

