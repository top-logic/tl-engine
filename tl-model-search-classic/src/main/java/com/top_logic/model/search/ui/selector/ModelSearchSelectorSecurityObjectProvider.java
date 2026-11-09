/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import java.util.Set;

import com.top_logic.basic.col.SetBuilder;
import com.top_logic.layout.component.Selectable;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.persistency.expressions.SearchExpressionStructureFactory;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The {@link SecurityObjectProvider} for the {@link SearchSelectorComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ModelSearchSelectorSecurityObjectProvider implements SecurityObjectProvider {

	/** The {@link ModelSearchSelectorSecurityObjectProvider} instance. */
	public static final ModelSearchSelectorSecurityObjectProvider INSTANCE =
		new ModelSearchSelectorSecurityObjectProvider();

	@Override
	public BoundObject getSecurityObject(BoundChecker securityChecker, Object model, BoundCommandGroup commandGroup) {
		if (commandGroup.equals(SimpleBoundCommandGroup.READ) || commandGroup.equals(SimpleBoundCommandGroup.CREATE)) {
			return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
		}
		if (!(securityChecker instanceof Selectable)) {
			return null;
		}
		Selectable selectable = (Selectable) securityChecker;
		Object selection = selectable.getSelected();
		if (!(selection instanceof SearchExpressionImpl)) {
			return null;
		}
		return (SearchExpressionImpl) selection;
	}

	@Override
	public Set<TLClass> getPossibleSecurityObjectTypes() {
		return new SetBuilder<TLClass>()
			.addAll(SecurityRootObjectProvider.INSTANCE.getPossibleSecurityObjectTypes())
			.add(SearchExpressionStructureFactory.getInstance().getSearchExpressionType())
			.toSet();
	}

}
