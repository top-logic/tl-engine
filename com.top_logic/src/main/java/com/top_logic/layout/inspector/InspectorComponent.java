/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.inspector.history.History;
import com.top_logic.layout.inspector.model.InspectorModel;
import com.top_logic.layout.inspector.model.nodes.InspectorReflectionNode;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;
import com.top_logic.util.TLContextManager;

/**
 * Filter component for the inspector.
 * 
 * This component defines different filters which may be used by the {@link InspectorTreeComponent}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorComponent extends FormComponent implements Selectable {

	@SuppressWarnings({ "unchecked" })
	static final Property<Filter<? super InspectorTreeNode>> PROP_FILTER =
		TypedAnnotatable.propertyRaw(Filter.class, "filter");

	@SuppressWarnings({ "unchecked" })
	private static final Property<Set<Filter<? super InspectorTreeNode>>> PROP_USER_FILTERS =
		TypedAnnotatable.propertyRaw(Collection.class, "userFilters");

	/**
	 * Configuration options for {@link InspectorComponent}.
	 */
	public interface Config<I extends InspectorComponent> extends FormComponent.Config, Selectable.SelectableConfig {

		/**
		 * Filters to offer for the inspection.
		 */
		List<FilterSetting> getFilters();

		/**
		 * Configuration of an available filter for the inspector.
		 */
		interface FilterSetting extends ConfigurationItem {

			/**
			 * Whether the filter is enabled by default.
			 */
			boolean getEnable();

			/**
			 * The {@link Filter} implementation.
			 */
			@InstanceFormat
			Filter<? super InspectorTreeNode> getImpl();
		}
	}


	/**
	 * Creates a new {@link InspectorComponent} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link InspectorComponent}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public InspectorComponent(InstantiationContext context, Config<?> config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		// We can inspect almost all objects.
		return anObject != null && !InspectorReflectionNode.isPrimitive(anObject.getClass());
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		updateInspectorModel();
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		
		ValueListener onFilterSelect = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (!(field instanceof BooleanField)) {
					return;
				}
				Filter<? super InspectorTreeNode> filter = filter(field);
				Set<Filter<? super InspectorTreeNode>> activeFilters = getActiveFilters();

				boolean selected = Utils.isTrue((Boolean) newValue);
				if (selected) {
					activeFilters.add(filter);
				} else {
					activeFilters.remove(filter);
				}

				updateInspectorModel();
			}

			/**
			 * Return the filter represented by the given form field.
			 */
			private Filter<? super InspectorTreeNode> filter(FormField aField) {
				return aField.get(PROP_FILTER);
			}

		};

		Set<Filter<? super InspectorTreeNode>> activeFilters = getActiveFilters();
		for (Filter<? super InspectorTreeNode> filter : getAllFilters()) {
			BooleanField filterField = createField(activeFilters, filter);

			filterField.addValueListener(onFilterSelect);
			context.addMember(filterField);
		}

		return context;
	}

	void updateInspectorModel() {
		setSelected(new InspectorModel(getModel(), FilterFactory.and(getActiveFilters())));
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && hasFormContext();
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);

		getFormContext();

		return result;
	}

	/**
	 * Return the filters a user has selected.
	 */
	protected Set<Filter<? super InspectorTreeNode>> getActiveFilters() {
		TLSubSessionContext subsessionContext = TLContextManager.getSubSession();
		Set<Filter<? super InspectorTreeNode>> result = subsessionContext.get(PROP_USER_FILTERS);

		if (result == null) {
			result = new HashSet<>(getDefaultFilters());
			subsessionContext.set(PROP_USER_FILTERS, result);
		}

		return result;
	}

	/**
	 * Create a check-box representing the given filter.
	 */
	protected BooleanField createField(Set<Filter<? super InspectorTreeNode>> activeFilters,
			Filter<? super InspectorTreeNode> filter) {
		return createFilterCheckBox(filter, activeFilters.contains(filter));
	}

	private BooleanField createFilterCheckBox(Filter<? super InspectorTreeNode> filter, boolean active) {
		BooleanField filterField = FormFactory.newBooleanField(filter.getClass().getSimpleName(), active, false);

		filterField.set(PROP_FILTER, filter);

		return filterField;
	}

	/**
	 * Return the list of all available filters.
	 * 
	 * @return The requested list.
	 */
	protected Collection<Filter<? super InspectorTreeNode>> getAllFilters() {
		ArrayList<Filter<? super InspectorTreeNode>> result = new ArrayList<>();
		for (Config.FilterSetting setting : ((Config<?>) getConfig()).getFilters()) {
			result.add(setting.getImpl());
		}
		return result;
	}

	private Collection<Filter<? super InspectorTreeNode>> getDefaultFilters() {
		ArrayList<Filter<? super InspectorTreeNode>> result = new ArrayList<>();
		for (Config.FilterSetting setting : ((Config<?>) getConfig()).getFilters()) {
			if (setting.getEnable()) {
				result.add(setting.getImpl());
			}
		}
		return result;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		
		History.add(this, newModel);
	}

}
