/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.TLObject;

/**
 * {@link BuilderComponent} holding a selection.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectableBuilderComponent extends BuilderComponent implements Selectable {

	/**
	 * Configuration options for {@link SelectableBuilderComponent}.
	 */
	public interface Config extends BuilderComponent.Config, Selectable.SelectableConfig {
		// Sum interface.
	}

	/**
	 * Creates a new {@link SelectableBuilderComponent}.
	 */
	public SelectableBuilderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		boolean becameInvalid;
		if (models.contains(getSelected())) {
			/* There are no selection options, therefore set the selection to 'null' when the
			 * selection is deleted. */
			setSelected(null);
			becameInvalid = true;
		} else {
			becameInvalid = false;
		}
		boolean superInvalidated = super.receiveModelDeletedEvent(models, changedBy);
		return becameInvalid || superInvalidated;
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		ModelBuilder builder = getBuilder();
		if (builder instanceof ListModelBuilder) {
			if (((ListModelBuilder) builder).supportsListElement(this, model)) {
				invalidate();
				return true;
			}
		}
		return super.receiveModelCreatedEvent(model, changedBy);
	}

}

