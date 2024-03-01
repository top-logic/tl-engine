/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.form.component.AbstractSelectorComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * {@link ChannelSPI} that creates different {@link ComponentChannel} depending on whether the
 * {@link LayoutComponent} supports multi selection or not.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectionChannelSPI extends ChannelSPI {

	private final Object _initialSingleSelection;

	private final Object _initialMultiSelection;

	/**
	 * Creates a {@link SelectionChannelSPI}.
	 *
	 * @param name
	 *        See {@link ChannelSPI#ChannelSPI(String)}.
	 * @param initialSingleSelection
	 *        See {@link #getInitialSingleSelection()}.
	 * @param initialMultiSelection
	 *        See {@link #getInitialMultiSelection()}.
	 */
	public SelectionChannelSPI(String name, Object initialSingleSelection, Object initialMultiSelection) {
		super(name);
		_initialSingleSelection = initialSingleSelection;
		_initialMultiSelection = initialMultiSelection;
	}

	/**
	 * Initial value for the {@link ComponentChannel} that is used for multi-selection components.
	 */
	public Object getInitialMultiSelection() {
		return _initialMultiSelection;
	}

	/**
	 * Initial value for the {@link ComponentChannel} that is used for single-selection components.
	 */
	public Object getInitialSingleSelection() {
		return _initialSingleSelection;
	}

	@Override
	protected ComponentChannel createImpl(LayoutComponent component) {
		if (isMultiSelection(component)) {
			return createMultiSelectionChannel(component);
		}
		return createSingleSelectionChannel(component);
	}

	/**
	 * The value for {@link #createImpl(LayoutComponent)} for single-selection components.
	 */
	protected ComponentChannel createSingleSelectionChannel(LayoutComponent component) {
		return new DefaultChannel(component, getName(), getInitialSingleSelection());
	}

	/**
	 * The value for {@link #createImpl(LayoutComponent)} for multi-selection components.
	 */
	protected ComponentChannel createMultiSelectionChannel(LayoutComponent component) {
		return new DefaultChannel(component, getName(), getInitialMultiSelection());
	}

	/**
	 * Heuristics whether the given component is a multi-selection component.
	 */
	public static boolean isMultiSelection(LayoutComponent component) {
		if (component instanceof SelectableWithSelectionModel) {
			return ((SelectableWithSelectionModel) component).getSelectionModel().isMultiSelectionSupported();
		}
		Config componentConfig = component.getConfig();
		if (componentConfig instanceof AbstractSelectorComponent.UIOptions) {
			return ((AbstractSelectorComponent.UIOptions) componentConfig).isMultiple();
		}
		if (component instanceof AssistentComponent) {
			return true;
		}
		return false;
	}

}
