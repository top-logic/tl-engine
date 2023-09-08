/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.chartbuilder.time.TimeSeriesChartBuilder;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class StaticAttributeFilter implements InteractiveBuilder<Filter<Object>, ChartContextObserver>,
		Filter<Object>, ConfiguredInstance<StaticAttributeFilter.Config> {

	private static final String FILTER = "filter";

	private final Config _config;

	private Filter<Object> _filter;

	/**
	 * Config-interface for {@link TimeSeriesChartBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<StaticAttributeFilter> {

		/**
		 * Indirect getter for the {@link TLStructuredTypePart} to prevent problems during startup.
		 * 
		 * @return a {@link Provider} for the {@link TLStructuredTypePart} this
		 *         {@link StaticAttributeFilter} is about
		 */
		public MetaAttributeProvider getMetaAttribute();

		/**
		 * the names of the initially selected values
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getValues();
	}

	/**
	 * Config-constructor for {@link StaticAttributeFilter}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public StaticAttributeFilter(InstantiationContext context, Config config) {
		_config = config;
		_filter = TrueFilter.INSTANCE;
	}

	private TLStructuredTypePart getMetaAttribute() {
		return getConfig().getMetaAttribute().get();
	}

	private FastList getFastList() {
		return (FastList) getMetaAttribute().getType();
	}

	private List<FastListElement> getElements() {
		return getFastList().elements();
	}

	private List<FastListElement> getInitialSelection() {
		List<FastListElement> result = new ArrayList<>();
		for (String name : getConfig().getValues()) {
			FastListElement fle = getFastList().getElementByName(name);
			result.add(fle);
		}
		return result;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {

		ResPrefix resPrefix = ResPrefix.legacyString(getClass().getSimpleName());

		SelectField selectField =
			FormFactory.newSelectField(FILTER, getElements(), true, getInitialSelection(), false, false, null);
		selectField.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		container.addMember(selectField);

		template(container, div(
			fieldsetBox(
				resource(
					I18NConstants.ATTRIBUTE_FILTER__ATTRIBUTE.fill(
					MetaResourceProvider.INSTANCE.getLabel(getMetaAttribute()))),
				fieldBox(FILTER),
				ConfigKey.field(container))));
	}

	@Override
	public Filter<Object> build(FormContainer container) {
		SelectField field = (SelectField) container.getMember(FILTER);
		final List<?> selection = (List<?>) DefaultWrapperAttributeFilter.toUnversionedKeys(field.getSelection());
		if (CollectionUtil.isEmpty(selection)) {
			_filter = TrueFilter.INSTANCE;
		} else {
			Filter<?> filter = null;
			TLStructuredTypePart attribute = getMetaAttribute();
			if (AttributeOperations.isCollectionValued(attribute)) {
				filter = new Filter<Object>() {

					@Override
					public boolean accept(Object anObject) {
						return CollectionUtil.containsAny(selection, (Collection<?>) anObject);
					}
				};
			} else {
				filter = new Filter<Object>() {

					@Override
					public boolean accept(Object anObject) {
						return selection.contains(anObject);
					}
				};
			}
			_filter = new DefaultWrapperAttributeFilter(attribute, filter);
		}
		return this;
	}

	@Override
	public boolean accept(Object anObject) {
		return _filter.accept(anObject);
	}

}
