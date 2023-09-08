/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IFunction0;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Base class for {@link IFunction0 functions} generating component name option lists.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractComponentNameCollector extends Function0<OptionModel<ComponentName>> {

	private final LayoutComponent _contextComponent;

	/**
	 * Creates a {@link AbstractComponentNameCollector}.
	 */
	public AbstractComponentNameCollector(DeclarativeFormOptions options) {
		_contextComponent = options.get(ComponentConfigurationDialogBuilder.COMPONENT);
	}

	/**
	 * The component whose configuration is currently edited.
	 */
	protected LayoutComponent getContextComponent() {
		return _contextComponent;
	}

	/**
	 * The {@link MainLayout} of the edited component tree.
	 */
	protected MainLayout getMainLayout() {
		return getContextComponent().getMainLayout();
	}

	@Override
	public OptionModel<ComponentName> apply() {
		List<LayoutComponent> components = new ArrayList<>();

		collect(components);

		List<ComponentName> componentNames = components.stream()
			.map(LayoutComponent::getName)
			.collect(Collectors.toList());

		return new DefaultListOptionModel<>(componentNames);
	}

	/**
	 * Adds all applicable components to the given list.
	 */
	protected abstract void collect(List<LayoutComponent> result);

}
