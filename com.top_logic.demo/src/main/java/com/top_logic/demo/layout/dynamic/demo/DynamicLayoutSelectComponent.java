/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.dynamic.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.dynamic.DynamicLayoutContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.DefaultLabelProvider;

/**
 * <p>
 * Component displaying a {@link SelectField} to select layout file displayed in a
 * {@link DynamicLayoutContainer}.
 * </p>
 * 
 * @see DemoResolver
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicLayoutSelectComponent extends FormComponent implements Selectable {

	/** Name of the select field, to select layout file. */
	@CalledFromJSP
	public static final String SELECT_FIELD = "select";

	private static final NamedConstant UNRESOLVABLE_MODEL = new NamedConstant("unresolvable model -> default layout");

	/**
	 * Configuration of a {@link DynamicLayoutSelectComponent}.
	 * 
	 */
	public interface Config extends FormComponent.Config, Selectable.SelectableConfig {

		/**
		 * Names of layout files to be able to switch to. Format has to be as a reference in a
		 * layout XML has.
		 */
		@Name("layouts")
		@ListBinding(tag = "layout", attribute = "name")
		List<String> getLayoutNames();
	}

	private final List<Object> _layoutNames;

	/**
	 * Creates a new {@link DynamicLayoutSelectComponent}.
	 */
	public DynamicLayoutSelectComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_layoutNames = new ArrayList<>(config.getLayoutNames());
		if (_layoutNames.isEmpty()) {
			context.error("Need at least one layout name in " + this);
		}
		_layoutNames.add(UNRESOLVABLE_MODEL);
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		SelectField field = FormFactory.newSelectField(SELECT_FIELD, _layoutNames);
		field.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<Object> newSelectionList = (List<Object>) newValue;
				Object newSelection;
				if (newSelectionList.isEmpty()) {
					newSelection = null;
				} else {
					newSelection = newSelectionList.get(0);
				}
				setSelected(newSelection);
			}
		});
		context.addMember(field);
		field.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		return context;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener((sender, oldValue,
				newValue) -> ((DynamicLayoutSelectComponent) sender.getComponent()).onSelectionChange(newValue));
	}

	private void onSelectionChange(Object newValue) {
		FormMember select = getFormContext().getMember(SELECT_FIELD);
		((SelectField) select).setDefaultValue(Collections.singletonList(newValue));
	}

}
