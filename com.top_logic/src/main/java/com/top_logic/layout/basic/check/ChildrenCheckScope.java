/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormHandlerUtil;
import com.top_logic.mig.html.layout.ComponentCollector;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ChildrenCheckScope} is a {@link CheckScope} which returns
 * hereditarily all visible {@link FormHandler} children of some component which
 * have to be checked.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class ChildrenCheckScope implements CheckScope {

	private final LayoutComponent component;

	private final ComponentCollector componentCollector = FormHandlerUtil.createNeedsChangeCheckVisitor();

	/**
	 * Creates a {@link ChildrenCheckScope} traversing starting with the given component.
	 * 
	 * @param component
	 *        the component to start traversing. Must not be <code>null</code>.
	 */
	public ChildrenCheckScope(LayoutComponent component) {
		this.component = component;
	}

	@Override
	public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
		component.acceptVisitorRecursively(componentCollector);
		List foundElements = componentCollector.getFoundElements();
		componentCollector.clear();
		return foundElements;
	}

}