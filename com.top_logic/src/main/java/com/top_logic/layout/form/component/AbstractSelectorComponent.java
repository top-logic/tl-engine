/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ComponentChannel.ChannelValueFilter;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.ValueVetoListener;
import com.top_logic.layout.form.selection.TableSelectDialogProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.provider.SelectControlProvider;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponentUIOptions;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLTypeRefsFormat;
import com.top_logic.util.Resources;

/**
 * Base class for components presenting a single {@link SelectField} providing options to be used as
 * context for other components through the selection channel.
 * 
 * <p>
 * <b>Note</b>: Instead of sub-classing this class, better use {@link SelectorComponent} that is
 * fully configurable through a {@link ListModelBuilder}.
 * </p>
 */
public abstract class AbstractSelectorComponent extends FormComponent
		implements Selectable, ValueListener, ValueVetoListener {

	/**
	 * {@link AbstractSelectorComponent} options directly displayed in the component's template.
	 */
	public interface UIOptions extends LayoutComponentUIOptions, Selectable.SelectableConfig {

		/**
		 * @see #isMultiple()
		 */
		String MULTIPLE = "multiple";

		/**
		 * @see #getPresentation()
		 */
		String PRESENTATION = "presentation";

		/**
		 * @see #getTypes()
		 */
		String TYPES = "types";

		/**
		 * Option how to display the selection.
		 */
		@Name(PRESENTATION)
		SelectPresentation getPresentation();

		/**
		 * Type(s) of options.
		 * 
		 * <p>
		 * Only required for table select display to compute a corresponding table configuration
		 * with columns for attributes of the types.
		 * </p>
		 */
		@Name(TYPES)
		@DynamicMode(fun = VisibleIfTable.class, args = @Ref(PRESENTATION))
		@DynamicMandatory(fun = IfTable.class, args = @Ref(PRESENTATION))
		@Format(TLTypeRefsFormat.class)
		List<TLModelPartRef> getTypes();

		/**
		 * Decides whether the argument is {@link SelectPresentation#POP_UP_TABLE}.
		 */
		class IfTable extends Function1<Boolean, SelectPresentation> {
			@Override
			public Boolean apply(SelectPresentation arg) {
				return arg == SelectPresentation.POP_UP_TABLE;
			}
		}

		/**
		 * Decides whether the argument is {@link SelectPresentation#POP_UP_TABLE}.
		 */
		class VisibleIfTable extends Function1<FieldMode, SelectPresentation> {
			@Override
			public FieldMode apply(SelectPresentation arg) {
				return arg == SelectPresentation.POP_UP_TABLE ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
			}
		}

		/**
		 * Whether the user is allowed to select multiple options at the same time.
		 * 
		 * <p>
		 * If this option is enabled, the selection channel contains sets of objects instead of
		 * singleton objects.
		 * </p>
		 */
		@Name(MULTIPLE)
		boolean isMultiple();

	}

	/**
	 * Configuration for the {@link AbstractSelectorComponent}.
	 */
	public interface Config extends FormComponent.Config, UIOptions {

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

	}

	private static final String SELECTION = "selection";

	private static final ChannelListener ON_SELECTION_CHANGE = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			((AbstractSelectorComponent) sender.getComponent()).onSelectionChange(newValue);
		}
	};

	private static final ChannelValueFilter SELECTION_FILTER = new ChannelValueFilter() {
		@Override
		public boolean accept(ComponentChannel sender, Object oldValue, Object newValue) {
			AbstractSelectorComponent selector = (AbstractSelectorComponent) sender.getComponent();

			if (newValue instanceof Collection) {
				for (Object element : ((Collection<?>) newValue)) {
					if (!selector.supportsOption(element)) {
						return false;
					}
				}
				return true;
			} else {
				return selector.supportsOption(newValue);
			}
		}
	};

	/**
	 * Creates a {@link AbstractSelectorComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractSelectorComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Name of the {@link SelectField} displaying the options.
	 */
	public String getSelectFieldName() {
		return SELECTION;
	}

	/**
	 * {@link LabelProvider} for displaying the options in the {@link #getOptionList()}.
	 */
	protected abstract LabelProvider getOptionLabelProvider();

	/**
	 * The form context contains only one {@link SelectField}. The {@link #getSelected()} is set as
	 * default selection for the select field if it is NOT <code>null</code> and it is in the option
	 * list.
	 */
	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);
		fc.addMember(createSelectField());
		return fc;
	}

	/**
	 * Creates the {@link SelectField} offering the single choice.
	 */
	protected SelectField createSelectField() {
		List<?> options = getOptionList();
		SelectField selectField =
			FormFactory.newSelectField(getSelectFieldName(), options, multiple(), null, false);
		selectField.setTransient(true);
		selectField.setLabel(Resources.getInstance().getString(getTitleKey()));
		selectField.setControlProvider(getSelectControlProvider());
		selectField.setOptionComparator(Equality.INSTANCE);
		selectField.setOptionLabelProvider(getOptionLabelProvider());
		Config config = config();
		if (config.getPresentation() == SelectPresentation.POP_UP_TABLE) {
			selectField.setSelectDialogProvider(TableSelectDialogProvider.newTableInstance());
			selectField.setTableConfigurationProvider(new GenericTableConfigurationProvider(resolveTypes(config.getTypes())));
		}
		updateUI(selectField, getSelected());
		selectField.addValueVetoListener(this);
		selectField.addValueListener(this);
		return selectField;
	}

	private static Set<TLClass> resolveTypes(List<TLModelPartRef> types2) {
		Set<TLClass> types = new HashSet<>();
		try {
			for (TLModelPartRef x : types2) {
				types.add(x.resolveClass());
			}
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return types;
	}

	/**
	 * Provides the {@link ControlProvider} for displaying the selection.
	 */
	protected ControlProvider getSelectControlProvider() {
		switch (config().getPresentation()) {
			case DROP_DOWN:
				return useDefaultSelection() ? SelectControlProvider.INSTANCE_WITHOUT_CLEAR
					: SelectControlProvider.INSTANCE;

			case POP_UP_TABLE:
			case POP_UP_LIST:
				return useDefaultSelection() ? SelectionControlProvider.INSTANCE_WITHOUT_CLEAR
					: SelectionControlProvider.INSTANCE;
		}

		throw new UnreachableAssertion("No such presentation: " + config().getPresentation());
	}

	private boolean useDefaultSelection() {
		return config().getDefaultSelection();
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		Object channelValue;
		if (multiple()) {
			channelValue = newValue;
		} else {
			channelValue = CollectionUtil.getSingleValueFrom(newValue);
		}
		setSelected(channelValue);
	}

	/**
	 * Callback issued, after a new value is retrieved from the selection channel.
	 */
	protected void onSelectionChange(Object newValue) {
		if (hasFormContext()) {
			updateUI(selectField(), newValue);
		}
	}

	private void updateUI(SelectField selectField, Object channelValue) {
		selectField.setAsSelection(CollectionUtil.asList(channelValue));
	}

	private boolean multiple() {
		return config().isMultiple();
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelCreatedEvent(Object aModel, Object changedBy) {
		if (aModel != null && supportsInternalModel(aModel)) {
			invalidate();
			return true;
		} else {
			return false;
		}
	}

    @Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object aChangedBy) {
		boolean result = super.receiveModelDeletedEvent(models, aChangedBy);
		if (hasFormContext() && !Collections.disjoint(displayedOptions(), models)) {
			invalidate();
			result = true;
        }
		Object selected = getSelected();
		if (selected instanceof Collection<?>) {
			Collection<?> selectionSet = (Collection<?>) selected;
			if (!Collections.disjoint(selectionSet, models)) {
				List<Object> newSelection = new ArrayList<>(selectionSet);
				newSelection.removeAll(models);
				setSelected(newSelection);
			}
		} else if (models.contains(selected)) {
			setSelected(null);
		}
		return result;
    }

	private List<?> displayedOptions() {
		return selectField().getOptions();
	}

	/**
	 * The formerly created {@link SelectField}.
	 */
	protected final SelectField selectField() {
		return (SelectField) getFormContext().getField(getSelectFieldName());
	}

	@Override
	public void checkVeto(final FormField field, final Object newValue) throws VetoException {
		Collection<? extends ChangeHandler> nonReflexiveMasterSlavePartnerHull =
			MasterSlaveCheckProvider.INSTANCE.getCheckScope(this).getAffectedFormHandlers();

		DirtyHandling.checkVeto(nonReflexiveMasterSlavePartnerHull);
	}

	/**
	 * Whether a value is supported as select option.
	 */
	protected abstract boolean supportsOption(Object value);

	/**
	 * The objects to select from in the order to present to the user.
	 */
	protected abstract List<?> getOptionList();

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);

		updateDefaultSelection();

		return result;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		ComponentChannel selectionChannel = selectionChannel();
		selectionChannel.addListener(ON_SELECTION_CHANGE);
		selectionChannel.addVetoListener(SELECTION_FILTER);
		updateDefaultSelection();
	}

	/**
	 * Called after a new model is set.
	 * 
	 * <p>
	 * Chooses an appropriate default selection for the current model.
	 * </p>
	 */
	private void updateDefaultSelection() {
		if (useDefaultSelection()) {
			selectionUpdate();
		}
	}

	private void selectionUpdate() {
		Object selection = getSelected();

		if (selection == null) {
			setDefaultSelection();
		} else if (selection instanceof Collection) {
			Collection<?> elements = (Collection<?>) selection;
			if (elements.isEmpty()) {
				setDefaultSelection();
			} else {
				List<?> options = getOptionList();
				for (Object element : elements) {
					if (!options.contains(element)) {
						// Filter out no longer available elements.
						List<?> newSelection = elements.stream().filter(options::contains).collect(Collectors.toList());
						if (newSelection.isEmpty()) {
							setDefaultSelection();
						} else {
							setSelected(newSelection);
						}
						return;
					}
				}

				// Unchanged, not empty.
			}
		} else {
			List<?> options = getOptionList();
			if (!options.contains(selection)) {
				setDefaultSelection();
			}
		}
	}

	private void setDefaultSelection() {
		setSelected(getDefaultSelection());
	}

	/**
	 * Computes an appropriate default selection for the current model.
	 * 
	 * <p>
	 * By default, the first available option from {@link #getOptionList()} is chosen.
	 * </p>
	 */
	protected Object getDefaultSelection() {
		Object currentSelection = getSelected();
		List<?> options = getOptionList();
		if (currentSelection != null && options.contains(currentSelection)) {
			return currentSelection;
		}
		if (options.isEmpty()) {
			return null;
		}

		Object lastSelection = ComponentUtil.filterValid(currentSelection);

		return computeDefaultSelection(options, lastSelection);
	}

	/**
	 * Choose an option to select by default.
	 * 
	 * @param lastSelection
	 *        The last selection (before the last change of the component's model). This selection
	 *        may now no longer be available as option, but the new selection could be derived from
	 *        it. In case of a multi-selection, the last selection may be a collection of objects.
	 */
	protected Object computeDefaultSelection(List<?> options, Object lastSelection) {
		return options.get(0);
	}

}
