/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.ElementUpdate;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelPartRef;

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

		/**
		 * The types of the elements that can be selected.
		 * 
		 * <p>
		 * When no types are configured, then the type of the element have no influence whether the
		 * element can be selected. Otherwise at most elements whose type is compatible to one of
		 * the configured type can be selected.
		 * </p>
		 */
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypes();
	}

	private final Set<TLStructuredType> _supportedTypes;

	/**
	 * Creates a new {@link SelectableBuilderComponent}.
	 */
	public SelectableBuilderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_supportedTypes = config.getTypes().stream()
			.map(TLModelPartRef::resolveType)
			.filter(TLStructuredType.class::isInstance)
			.map(TLStructuredType.class::cast)
			.collect(Collectors.toSet());
	}

	@Override
	protected boolean observeAllTypes() {
		return _supportedTypes.isEmpty();
	}

	@Override
	protected Set<? extends TLStructuredType> getTypesToObserve() {
		return _supportedTypes;
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
	protected void handleTLObjectCreations(Stream<? extends TLObject> created) {
		ModelBuilder builder = getBuilder();
		if (builder instanceof ListModelBuilder listBuilder) {
			for (Iterator<? extends TLObject> it = created.iterator(); it.hasNext();) {
				TLObject newItem = it.next();
				ElementUpdate updateDecision = listBuilder.supportsListElement(this, newItem);
				switch (updateDecision) {
					case ADD:
						// don't know how to add incrementally
						invalidate();
						return;
					case UNKNOWN:
						invalidate();
						return;
					case NO_CHANGE:
					case REMOVE:
						continue;
				}
				throw new IllegalArgumentException("Uncovered case: " + updateDecision);
			}
		}
	}


}

