/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.RevisionCompareAlgorithm;
import com.top_logic.layout.form.decorator.AbstractRevisionCompareHandler;

/**
 * Show a compare view for {@link Wrapper}s in an {@link EditAttributedComponent}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedCompareCommandHandler<C extends EditAttributedComponent> extends
		AbstractRevisionCompareHandler<C> {

	/**
	 * Command id used to register an {@link AttributedCompareCommandHandler}.
	 */
	public static final String COMMAND_ID = "historicAttributedViewCommand";

	/**
	 * Creates a new {@link AttributedCompareCommandHandler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AttributedCompareCommandHandler}.
	 * 
	 */
	public AttributedCompareCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void setCompareObject(C component, Object anotherModel) {
		component.setCompareAlgorithm((CompareAlgorithm) anotherModel);
	}

	@Override
	protected Object getCompareObject(C component) {
		return component.getCompareAlgorithm();
	}

	@Override
	protected Object createCompareObject(DisplayContext context, C component, final Map<String, Object> someArguments) {
		Revision revision = getRevision(someArguments);
		if (revision == null) {
			return null;
		}
		return new RevisionCompareAlgorithm(revision);
	}

}
