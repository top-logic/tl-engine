/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.func.Function1;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.tree.model.BusinessObjectTreeModel;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link OptionModel} of all {@link ComponentName}s for a given root component.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class RelativeComponentNames extends Function1<OptionModel<ComponentName>, ComponentName> {

	private MainLayout _ml;

	/**
	 * Creates a {@link RelativeComponentNames}.
	 *
	 */
	public RelativeComponentNames() {
		_ml = MainLayout.getMainLayout(DefaultDisplayContext.getDisplayContext());
	}

	@Override
	public OptionModel<ComponentName> apply(ComponentName root) {
		if (root == null) {
			return null;
		}
		return new DefaultTreeOptionModel<>(createTreeModel(root));
	}

	private BusinessObjectTreeModel<ComponentName> createTreeModel(ComponentName root) {
		Function<ComponentName, Collection<?>> children = createChildrenByName();
		Function<ComponentName, Collection<?>> parents = createParentByName();

		return new BusinessObjectTreeModel<>(root, children, parents);
	}

	private Function<ComponentName, Collection<?>> createParentByName() {
		return name -> {
			LayoutComponent component = _ml.getComponentByName(name);

			return Collections.singleton(component.getParent().getName());
		};
	}

	private Function<ComponentName, Collection<?>> createChildrenByName() {
		return name -> {
			LayoutComponent component = _ml.getComponentByName(name);

			if (component instanceof LayoutContainer) {
				return getChildrenNames((LayoutContainer) component);
			}

			return Collections.emptyList();
		};
	}

	private Collection<ComponentName> getChildrenNames(LayoutContainer parent) {
		return parent.getChildList().stream()
			.sorted(Comparator.comparing(component -> LayoutUtils.getLabel(component)))
			.map(LayoutComponent::getName)
			.collect(Collectors.toList());
	}

}
