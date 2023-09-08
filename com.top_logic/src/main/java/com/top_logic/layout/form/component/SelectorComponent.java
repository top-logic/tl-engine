/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.component.ListSelectionProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link LayoutComponent} displaying a single select field showing options produced by a
 * {@link ListModelBuilder}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectorComponent extends AbstractSelectorComponent {

	/**
	 * {@link SelectorComponent} options directly displayed in the component's template.
	 */
	public interface UIOptions extends AbstractSelectorComponent.UIOptions {

		/**
		 * @see #getOptionBuilder()
		 */
		String OPTION_BUILDER = "optionBuilder";

		/**
		 * @see #getOptionBuilder()
		 */
		String OPTION_LABEL_PROVIDER = "optionLabelProvider";

		/**
		 * @see #getAlphabeticalOrder()
		 */
		String ALPHABETICAL_ORDER = "alphabeticalOrder";

		/**
		 * @see #getDefaultSelectionProvider()
		 */
		String DEFAULT_SELECTION_PROVIDER = "defaultSelectionProvider";

		/**
		 * {@link ListModelBuilder} creating the options to select from.
		 * 
		 * <p>
		 * The selected option is immediately published on the selection channel of this component.
		 * </p>
		 */
		@Name(OPTION_BUILDER)
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ListModelBuilder> getOptionBuilder();

		/**
		 * {@link LabelProvider} for displaying the options.
		 * 
		 * <p>
		 * If none is given, {@link MetaLabelProvider} is used.
		 * </p>
		 */
		@Name(OPTION_LABEL_PROVIDER)
		@Options(fun = InAppLabelProviders.class)
		PolymorphicConfiguration<? extends LabelProvider> getOptionLabelProvider();

		/**
		 * Whether to present the options in alphabetical order of their labels as defined by the
		 * {@link #getOptionLabelProvider()}.
		 * 
		 * <p>
		 * If not checked, options are displayed in the order created by the
		 * {@link #getOptionBuilder()}.
		 * </p>
		 */
		@Name(ALPHABETICAL_ORDER)
		@BooleanDefault(true)
		boolean getAlphabeticalOrder();

		/**
		 * Algorithm selecting some option, if {@link #getDefaultSelection()} is enabled and the
		 * user has not yet chosen an option.
		 * 
		 * <p>
		 * If {@link #getDefaultSelection()} is enabled but no value is given here, the first option
		 * from the list is chosen by default.
		 * </p>
		 */
		@Name(DEFAULT_SELECTION_PROVIDER)
		@Options(fun = AllInAppImplementations.class)
		@DynamicMode(fun = CommandHandler.ConfirmConfig.VisibleIf.class, args = @Ref(DEFAULT_SELECTION))
		PolymorphicConfiguration<? extends ListSelectionProvider> getDefaultSelectionProvider();

		/**
		 * All {@link InApp} {@link LabelProvider}s that are not also {@link ResourceProvider}s.
		 */
		class InAppLabelProviders extends AllInAppImplementations {

			/**
			 * Creates a {@link InAppLabelProviders}.
			 */
			public InAppLabelProviders(DeclarativeFormOptions options) {
				super(options);
			}

			@Override
			protected Stream<? extends Class<?>> acceptableImplementations() {
				return super.acceptableImplementations().filter(c -> !ResourceProvider.class.isAssignableFrom(c));
			}
		}
	}

	/**
	 * Configuration options for {@link SelectorComponent}.
	 */
	@TagName("selector")
	public interface Config extends AbstractSelectorComponent.Config, UIOptions {

		@Override
		@ClassDefault(SelectorComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

	}

	private final ListModelBuilder _optionBuilder;

	private final LabelProvider _labelProvider;

	private ListSelectionProvider _defaultSelectionProvider;

	/**
	 * Creates a {@link SelectorComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SelectorComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_optionBuilder = context.getInstance(config.getOptionBuilder());
		_labelProvider = withDefault(context.getInstance(config.getOptionLabelProvider()), MetaLabelProvider.INSTANCE);
		_defaultSelectionProvider =
			withDefault(context.getInstance(config.getDefaultSelectionProvider()), ListSelectionProvider.FIRST_VALUE);
	}

	private static <T> T withDefault(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	@Override
	protected LabelProvider getOptionLabelProvider() {
		return _labelProvider;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return _optionBuilder.supportsModel(anObject, this);
	}

	/**
	 * Whether a value is supported as select option.
	 */
	@Override
	protected boolean supportsOption(Object value) {
		return _optionBuilder.supportsListElement(this, value);
	}

	/**
	 * The objects to select from.
	 */
	@Override
	protected List<?> getOptionList() {
		List<?> options = CollectionUtil.asList(_optionBuilder.getModel(getModel(), this));
		if (((Config) getConfig()).getAlphabeticalOrder()) {
			options = new ArrayList<>(options);
			options.sort(LabelComparator.newCachingInstance(getOptionLabelProvider()));
		}
		return options;
	}

	@Override
	protected void onSelectionChange(Object newValue) {
		if (!setModel(_optionBuilder.retrieveModelFromListElement(this, newValue))) {
			super.onSelectionChange(newValue);
		}
	}

	@Override
	protected Object computeDefaultSelection(List<?> options, Object lastSelection) {
		Collection<?> result = _defaultSelectionProvider.computeDefaultSelection(getModel(), options, lastSelection);
		if (((Config) getConfig()).isMultiple()) {
			return result;
		} else {
			return CollectionUtil.getSingleValueFromCollection(result);
		}
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		if (hasFormContext()) {
			selectField().setOptions(getOptionList());
		}
	}

}
