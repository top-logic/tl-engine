/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Comparator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for attributes that are edited by selecting from a set of
 * options.
 *
 * <p>
 * Wraps a {@link SelectFieldModel} (an {@link AttributeSelectFieldModel}) in a
 * {@link ReactDropdownSelectControl}, using {@link MetaLabelProvider} so that options and the
 * current selection are rendered with their model labels in both edit and display mode.
 * </p>
 *
 * <p>
 * When configured for a special model datatype (e.g. {@code tl.util:Country}) via the
 * {@link FieldControlService} type map, an {@link #getConfiguredOptions() option provider} supplies
 * the selectable values. Without a configured option provider, options are derived from the
 * attribute itself (enumeration, reference, enum datatype, or a TL-Script options annotation).
 * </p>
 */
public class SelectControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration for {@link SelectControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<SelectControlProvider> {

		/** Configuration name for {@link #getOptionProvider()}. */
		String OPTION_PROVIDER = "option-provider";

		@Override
		@ClassDefault(SelectControlProvider.class)
		Class<? extends SelectControlProvider> getImplementationClass();

		/**
		 * The option source for the selectable values.
		 *
		 * <p>
		 * If unset, options are derived from the attribute's type (enumeration, reference, enum
		 * datatype) or its TL-Script options annotation.
		 * </p>
		 */
		@Name(OPTION_PROVIDER)
		@InstanceFormat
		OptionProvider getOptionProvider();
	}

	private final OptionProvider _optionProvider;

	/**
	 * Creates a {@link SelectControlProvider} without a configured option source (options are
	 * derived from the attribute).
	 */
	public SelectControlProvider() {
		_optionProvider = null;
	}

	/**
	 * Creates a configured {@link SelectControlProvider}.
	 */
	@CalledByReflection
	public SelectControlProvider(InstantiationContext context, Config config) {
		_optionProvider = config.getOptionProvider();
	}

	/**
	 * The configured option source, or {@code null} to derive options from the attribute.
	 */
	public OptionProvider getConfiguredOptions() {
		return _optionProvider;
	}

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		SelectFieldModel selectModel = (SelectFieldModel) model;
		LabelProvider labels = MetaLabelProvider.INSTANCE;
		Comparator<?> optionOrder = LabelComparator.newCachingInstance(labels);
		return new ReactDropdownSelectControl(context, selectModel, labels, optionOrder, false);
	}

}
