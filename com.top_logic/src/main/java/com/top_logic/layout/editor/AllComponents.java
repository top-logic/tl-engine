/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.ButtonComponent;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.BreadcrumbComponent;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.components.ComponentPlaceholder;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.tree.model.BusinessObjectTreeModel;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link OptionModel} of all {@link LayoutComponent}s for a given root component.
 * 
 * @implNote For a property of type {@link ComponentName}, use {@link ComponentNameMapping} as
 *           option mapping.
 */
public class AllComponents extends Function0<OptionModel<LayoutComponent>> {

	private final LayoutComponent _root;

	/**
	 * Creates a {@link LayoutComponent} option provider with the current {@link MainLayout} as root
	 * component.
	 */
	public AllComponents() {
		_root = MainLayout.getMainLayout(DefaultDisplayContext.getDisplayContext());
	}

	@Override
	public OptionModel<LayoutComponent> apply() {
		DefaultTreeOptionModel<LayoutComponent> result = new DefaultTreeOptionModel<>(createTreeModel());
		result.setShowRoot(false);
		return result;
	}

	private BusinessObjectTreeModel<LayoutComponent> createTreeModel() {
		return new BusinessObjectTreeModel<>(_root, this::getChildren, this::getParent);
	}

	private Collection<LayoutComponent> getParent(LayoutComponent component) {
		if (component == _root) {
			return Collections.emptyList();
		}

		LayoutComponent result = component.getParent();
		while (result != _root && !isAcceptable(result)) {
			result = result.getParent();
		}
		return Collections.singleton(result);
	}

	private Collection<LayoutComponent> getChildren(LayoutComponent component) {
		List<LayoutComponent> result = new ArrayList<>();
		addChildren(result, component);
		return result;
	}

	private void addChildren(List<LayoutComponent> result, LayoutComponent component) {
		if (component instanceof LayoutContainer) {
			for (LayoutComponent child : ((LayoutContainer) component).getChildList()) {
				if (!isAcceptable(child)) {
					addChildren(result, child);
					continue;
				}
				result.add(child);
			}
		}

		for (LayoutComponent child : component.getDialogs()) {
			result.add(child);
		}
	}

	private static boolean isAcceptable(LayoutComponent component) {
		if ((component instanceof BreadcrumbComponent)
			|| (component instanceof TabComponent)
			|| (component instanceof LayoutControlComponent)
			|| (component instanceof ComponentPlaceholder)
			|| (component instanceof ButtonComponent)) {
			return false;
		}

		if (component.getChannelNames().isEmpty()) {
			return false;
		}

		if (LayoutConstants.isSyntheticName(component.getName())) {
			return false;
		}

		ResKey labelKey = LayoutUtils.getLabelKey(component);
		return labelKey != null;
	}

}
