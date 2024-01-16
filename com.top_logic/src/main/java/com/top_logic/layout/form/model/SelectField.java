/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import static com.top_logic.layout.form.model.SelectFieldUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.FilteredIterator;
import com.top_logic.basic.col.IDBuilder;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Matcher;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.filter.FalseFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.constraints.OptionsBasedSelectionConstraint;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.selection.AbstractSelectDialog;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.SetDefaultAccessor;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.util.Resources;

/**
 * Server-side representation of a form field that represents an optionally
 * {@link #isMultiple() multiple} {@link #getSelection() selection} from a list of
 * fixed {@link #setOptions(List) options}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectField extends AbstractFormField implements TableDataOwner, OptionModelListener {
	/* 
	 * See http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_select
	 * 
	 * <!ELEMENT select (optgroup|option)+>  <!-- option selector -->
	 * <!ATTLIST select
	 *   %attrs;
	 *   name        CDATA          #IMPLIED
	 *   size        %Number;       #IMPLIED
	 *   multiple    (multiple)     #IMPLIED
	 *   disabled    (disabled)     #IMPLIED
	 *   tabindex    %Number;       #IMPLIED
	 *   onfocus     %Script;       #IMPLIED
	 *   onblur      %Script;       #IMPLIED
	 *   onchange    %Script;       #IMPLIED
	 *   >
	 * 
	 * <!ELEMENT optgroup (option)+>   <!-- option group -->
	 * <!ATTLIST optgroup
  	 * %attrs;
  	 * disabled    (disabled)     #IMPLIED
	 *   label       %Text;         #REQUIRED
	 *   >
	 * 
	 * <!ELEMENT option (#PCDATA)>     <!-- selectable choice -->
	 * <!ATTLIST option
	 *   %attrs;
	 *   selected    (selected)     #IMPLIED
	 *   disabled    (disabled)     #IMPLIED
	 *   label       %Text;         #IMPLIED
	 *   value       CDATA          #IMPLIED
	 *   >
  	 */
	public interface Config extends ConfigurationItem {
		
		public static final String DEFAULT_MULTI_SELECTION_SEPARATOR = ";";
		public static final String MULTI_SELECTION_SEPARATOR_ATTRIBUTE = "multi-selection-separator";
		public static final String MULTI_SELECTION_SEPARATOR_FORMAT_ATTRIBUTE = "multi-selection-separator-format";

		/**
		 * true, if selection of multiple options is possible via textual input, false otherwise
		 */
		@Name("multi-selection-input-enabled")
		@BooleanDefault(true)
		boolean isMultiSelectionTextInputEnabled();

		/**
		 * true, if filter for option matches will accept substrings, false otherwise.
		 */
		@Name("substring-match-enabled")
		@BooleanDefault(true)
		boolean isSubstringMatchEnabled();

		/**
		 * By parser accepted separator of multiple selected options
		 */
		@Name(MULTI_SELECTION_SEPARATOR_ATTRIBUTE)
		@StringDefault(DEFAULT_MULTI_SELECTION_SEPARATOR)
		String getMultiSelectionSeparator();

		/**
		 * printable format of separator of multiple selected options.
		 */
		@Name(MULTI_SELECTION_SEPARATOR_FORMAT_ATTRIBUTE)
		@StringDefault("; ")
		String getMultiSelectionSeparatorFormat();
	}

	/**
	 * The application value that corresponds to the empty selection. 
	 */
	public static final List NO_SELECTION = Collections.EMPTY_LIST;
	
	/**
	 * Type of the options list property.
	 * 
	 * @see OptionsListener
	 */
	public static final EventType<OptionsListener, SelectField, Object> OPTIONS_PROPERTY =
		new EventType<>("selectOptions") {

		@Override
			public Bubble dispatch(OptionsListener listener, SelectField sender, Object oldValue, Object newValue) {
				return listener.handleOptionsChanged(sender);
		}

	};

	/**
	 * @see SelectFieldUtils#setOptionLabelProvider(FormField, LabelProvider)
	 */
	public static final Property<LabelProvider> OPTION_LABEL_PROVIDER =
		TypedAnnotatable.property(LabelProvider.class, "optionLabelProvider");

	/**
	 * @see SelectFieldUtils#setOptionContextMenu(FormField, ContextMenuProvider)
	 */
	public static final Property<ContextMenuProvider> OPTION_CONTEXT_MENU =
		TypedAnnotatable.property(ContextMenuProvider.class, "optionContextMenu");

	/**
	 * @see SelectFieldUtils#setMultiSelectionTextInputEnabled(FormField, boolean)
	 */
	public static final Property<Boolean> MULTI_SELECTION_TEXT_INPUT_PROPERTY =
		TypedAnnotatable.property(Boolean.class, "multiSelectionTextInput");

	/**
	 * @see SelectFieldUtils#setMultiSelectionSeparator(FormField, String, String)
	 */
	public static final Property<String> MULTI_SELECTION_SEPARATOR_PROPERTY =
		TypedAnnotatable.property(String.class, "multiSelectionSeparator");

	/**
	 * @see SelectFieldUtils#setMultiSelectionSeparator(FormField, String, String)
	 */
	public static final Property<String> MULTI_SELECTION_SEPARATOR_FORMAT_PROPERTY =
		TypedAnnotatable.property(String.class, "multiSelectionSeparatorFormat");

	/**
	 * @see SelectFieldUtils#setCollectionSeparator(FormField, String)
	 */
	public static final Property<String> COLLECTION_SEPARATOR_PROPERTY =
		TypedAnnotatable.property(String.class, "collectionSeparator");

	@FrameworkInternal
	public static final String LABEL_KEY = "label";

	@FrameworkInternal
	public static final String ID_KEY = "id";

	
	/**
	 * Special "option" that is implicitly in the set of options of each {@link SelectField}.
	 * 
	 * <p>
	 * The {@link #NO_OPTION} represents the "empty selection choice" of select field views in
	 * single selection mode. This option is not within {@link #getOptions()}, but is treated as
	 * legal option in all methods of the {@link SelectField} class that deal with options.
	 * </p>
	 * 
	 * <p>
	 * Note: This constant is not defined to be <code>null</code>, because <code>null</code> is a
	 * legal application value that could be included in the list of {@link #options}.
	 * </p>
	 */
	public static final Object NO_OPTION = new NamedConstant("NO_OPTION");

    /** Convenience constant to specify multi-selection. */
    public static final boolean MULTIPLE = true;

    /**
     * @see #getOptionComparator()
     */
	public static final Property<Comparator> COMPARATOR_PROPERTY =
		TypedAnnotatable.property(Comparator.class, "COMPARATOR_PROPERTY");

	/**
	 * @see #getFixedOptions()
	 * @see FixedOptionsListener
	 */
	public static final EventType<FixedOptionsListener, SelectField, Filter> FIXED_FILTER_PROPERTY =
		new EventType<>("FIXED_FILTER_PROPERTY") {

			@Override
			public Bubble dispatch(FixedOptionsListener listener, SelectField sender, Filter oldValue, Filter newValue) {
				return listener.handleFixedOptionsChanged(sender, oldValue, newValue);
			}

		};

	private static final Property<Filter> FIXED_FILTER_KEY = TypedAnnotatable.property(Filter.class, "FIXED_FILTER_PROPERTY");

	/**
	 * @see #getSelectDialogProvider()
	 */
	public static final Property<SelectDialogProvider> SELECT_DIALOG_PROVIDER_PROPERTY =
		TypedAnnotatable.property(SelectDialogProvider.class, "SELECT_DIALOG_PROVIDER_PROPERTY");

	/**
	 * @see #getDialogTableConfigurationProvider()
	 */
	public static final Property<TableConfigurationProvider> SELECT_DIALOG_TABLE_PROVIDER_PROPERTY =
		TypedAnnotatable.property(TableConfigurationProvider.class, "SELECT_DIALOG_TABLE_PROVIDER_PROPERTY");

	/** @see #getTableConfigurationProvider() */
	private static final Property<TableConfigurationProvider> TABLE_CONFIGURATION_PROVIDER =
		TypedAnnotatable.property(TableConfigurationProvider.class, "TABLE_CONFIGURATION_PROVIDER");

	/**
	 * Key for storing an explicit empty label.
	 * 
	 * @see #setEmptyLabel(String)
	 */
	static final Property<String> EMPTY_LABEL_PROPERTY = TypedAnnotatable.property(String.class, "emptyLabel");

	/**
	 * Key for storing the empty label set for immutable mode.
	 * 
	 * @see #setEmptyLabelImmutable(String)
	 */
	static final Property<String> EMPTY_LABEL_IMMUTABLE_PROPERTY = TypedAnnotatable.property(String.class, "emptyLabelImmutable");

	private static final Property<Boolean> MATCH_SUBSTRING = TypedAnnotatable.property(Boolean.class, "matchSubstring");

	private static final Property<TableData> TABLE_DATA = TypedAnnotatable.property(TableData.class, "tableData");

	@SuppressWarnings({ "unchecked" })
	private static final Property<Mapping<FormMember, String>> CONFIG_NAME_MAPPING =
		TypedAnnotatable.propertyRaw(Mapping.class, "configNameMapping");

	private static final ResKey FIXED_OPTIONS_ADDED_KEY = I18NConstants.FIXED_OPTIONS_ADDED;

	private static final ResKey FIXED_OPTIONS_REMOVED_KEY = I18NConstants.FIXED_OPTIONS_REMOVED;

	/** 
	 * The application objects, from which this field allows to select 
	 * a subset.
	 * 
	 * <p>
	 * Note: The options may never be <code>null</code>. For the purpose 
	 * of deferring the creation of the options list until they are actually required 
	 * in the rendering process, use a {@link LazyListUnmodifyable}.
	 * </p>
	 */
	private OptionModel<?> options;
	
	/**
	 * @see #isMultiple()
	 */
	final boolean multiple;
	
	private boolean customOrder;
	
	private IDBuilder optionIDs = new IDBuilder();

	private Constraint optionsBasedSelectionConstraint;

	private ConfigKey _configKey;
	
	/**
	 * Creates a new {@link SelectField}.
	 * 
	 * @param name
	 *        See {@link FormField#getName()}
	 * @param options
	 *        The options to select from.
	 * @param multiple
	 *        See {@link #isMultiple()}
	 * @param mandatory
	 *        See {@link #isMandatory()}
	 * @param immutable
	 *        see {@link #isImmutable()}
	 * @param constraint
	 *        see {@link #addConstraint(Constraint)}
	 */
	protected SelectField(String name, OptionModel<?> options, boolean multiple, boolean mandatory, boolean immutable,
			Constraint constraint) {
		super(name, mandatory, immutable, true, constraint);
		
		this.multiple = multiple;
		setOptionModel(options);
		optionsBasedSelectionConstraint = new OptionsBasedSelectionConstraint(this);
	}

	public final void enableOptionsBasedSelectionConstraint() {
		if (!getConstraints().contains(optionsBasedSelectionConstraint)) {
			addConstraint(optionsBasedSelectionConstraint);
		}
	}

	public final void disableOptionsBasedSelectionConstraint() {
		if (getConstraints().contains(optionsBasedSelectionConstraint)) {
			removeConstraint(optionsBasedSelectionConstraint);
		}
	}
	
	/**
	 * {@link Filter} that identifies options that cannot be selected, if they are not in the
	 * current selection, and that cannot be unselected, if they are currently selected.
	 * 
	 * This will work with popup-selects only. form:select will not work. Consider using some
	 * Constraint for this case.
	 * 
	 * <p>
	 * Note: Options identified by this filter can not even be programmatically set and unset using
	 * the {@link #setValue(Object)} method.
	 * </p>
	 */
	public final Filter getFixedOptions() {
		return get(FIXED_FILTER_KEY);
	}

	/**
	 * The value of the {@link #getFixedOptions()} property, or {@link FalseFilter}, if no such
	 * property exists.
	 */
	public final Filter getFixedOptionsNonNull() {
		return FilterFactory.nullAsFalse(getFixedOptions());
	}

	/**
	 * @see #getFixedOptions()
	 */
	public final void setFixedOptions(Filter fixedFilter) {
    	// remember the values we're about to change in order to
    	// stuff them into the property changed event.
		Filter theOld = this.getFixedOptions();
		if (fixedFilter == null) {
			reset(FIXED_FILTER_KEY);
		} else {
			set(FIXED_FILTER_KEY, fixedFilter);
		}

        // let the currently registered listeners know about the new
        // fixed filter instance.		
		firePropertyChanged(FIXED_FILTER_PROPERTY, self(), theOld, fixedFilter);
	}

    /**
     * Call #setFixedOptions() with a {@link SetFilter} based on toFix.
     * 
     * @param toFix may be null or empty indicating no (more) fixed options.
     */
    public final void setFixedOptions(Collection toFix) {
    	// remember the values we're about to change in order to
    	// stuff them into the property changed event.
		Filter theNew = null;
		Filter theOld = this.getFixedOptions();
        if (toFix == null || toFix.isEmpty()) {
			reset(FIXED_FILTER_KEY);
        } else {
        	theNew = new SetFilter(toFix);
			set(FIXED_FILTER_KEY, theNew);
        }
        
        // let the currently registered listeners know about the new
        // fixed filter instance.
		firePropertyChanged(FIXED_FILTER_PROPERTY, self(), theOld, theNew);
    }

	/**
	 * The {@link SelectDialogProvider} to show select dialogs for this {@link SelectField}.
	 */
	public final SelectDialogProvider getSelectDialogProvider() {
		SelectDialogProvider result = get(SELECT_DIALOG_PROVIDER_PROPERTY);
		if (result == null) {
			return SelectDialogProvider.newInstance();
		}
		return result;
	}

	/**
	 * Sets the {@link #getSelectDialogProvider()} property.
	 */
	public final void setSelectDialogProvider(SelectDialogProvider provider) {
		set(SELECT_DIALOG_PROVIDER_PROPERTY, provider);
	}

	/**
	 * @see #getDialogTableConfigurationProvider()
	 */
	public final void setDialogTableConfigurationProvider(TableConfigurationProvider provider) {
		set(SELECT_DIALOG_TABLE_PROVIDER_PROPERTY, provider);
	}

	/**
	 * Get {@link TableConfigurationProvider} for {@link AbstractSelectDialog}s.
	 * 
	 * @see #setDialogTableConfigurationProvider(TableConfigurationProvider)
	 */
	public final TableConfigurationProvider getDialogTableConfigurationProvider() {
		return get(SELECT_DIALOG_TABLE_PROVIDER_PROPERTY);
	}

    /**
	 * {@link Comparator} that determines the order of options in a selection
	 * dialog.
	 * 
	 * <p>
	 * Note: This order is currently only used in large option selectors. 
	 * </p>
	 */
	public final Comparator getOptionComparator() {
		return SelectFieldUtils.getOptionComparator(this);
	}
	
	/**
	 * @see #getOptionComparator()
	 */
	public final void setOptionComparator(Comparator optionComparator) {
		SelectFieldUtils.setOptionComparator(this, optionComparator);
	}
	
	/**
	 * Sets the options of this {@link SelectField} to the given list.
	 * 
	 * <p>
	 * The options of a {@link SelectField} are arbitrary application objects.
	 * Internationalized end-user-readable descriptions of these options are
	 * generated by a {@link LabelProvider} (see
	 * {@link #setOptionLabelProvider(LabelProvider)}).
	 * </p>
	 * 
	 * <p>
	 * The given list is guaranteed to be accessed only if an associated view of
	 * this {@link SelectField} needs to render a selection. If the list of
	 * options is large or expensive to generate, it is advisable to use a
	 * {@link LazyListUnmodifyable} as proxy.
	 * </p>
	 * 
	 * @param options
	 *     The updated list of options. May never be <code>null</code>,
	 *     see above.
	 */
	public void setOptions(List<?> options) {
		setOptionModel(new DefaultListOptionModel(options));
	}

	/**
	 * Sets the tree-structured options of this field.
	 * 
	 * @see #setOptions(List)
	 */
	public <N> void setOptionsTree(TLTreeModel<N> options) {
		setOptionModel(new DefaultTreeOptionModel<>(options));
	}

	/**
	 * Sets the tree-structured options of this field.
	 * 
	 * @param options
	 *        Tree structured options.
	 * @param selectableFilter
	 *        {@link Filter} of nodes that are actually selectable.
	 * 
	 * @see #setOptions(List)
	 */
	public <N> void setOptionsTree(TLTreeModel<N> options, Filter<? super N> selectableFilter) {
		setOptionModel(new DefaultTreeOptionModel<>(options, selectableFilter));
	}

	/**
	 * The options of this field.
	 * 
	 * @see #getOptions()
	 * @see #getOptionsAsTree()
	 */
	public OptionModel<?> getOptionModel() {
		return options;
	}

	/**
	 * @see #getOptionModel()
	 */
	public void setOptionModel(OptionModel<?> options) {
		assert options != null : "Option model must not be null";

		OptionModel<?> oldOptions = this.options;
		this.options = options;

		if (oldOptions != null) {
			this.options.removeOptionListener(this);
			notifyOptionsChanged(this.options);
		}

		this.options.addOptionListener(this);
	}

	@Override
	public void notifyOptionsChanged(OptionModel<?> sender) {
		assert sender == getOptionModel() : "Events from unexpected model in '" + this + "': Expected: "
				+ getOptionModel() + ", Actual: " + sender;

		optionIDs.clear();
		
		firePropertyChanged(OPTIONS_PROPERTY, self(), null, null);
		
		if (hasValue()) {
			List<?> selection = internalGetSelection();
			if (!selection.isEmpty()) {
				if (sender.getOptionCount() < OptionModel.INFINITE) {
					checkSelection:
					switch (selection.size()) {
						case 0:
							assert false : "Empty selection is checked before.";
							break checkSelection;
						case 1:
							Object singleSelection = selection.get(0);
							for (Object newOption : sender) {
								if (Utils.equals(newOption, singleSelection)) {
									// single selection still valid.
									break checkSelection;
								}
							}
							setValue(null);
							break checkSelection;
						default: {
							HashSet<?> invalidSelected = new HashSet<>(selection);
							for (Object newOption : sender) {
								invalidSelected.remove(newOption);
								if (invalidSelected.isEmpty()) {
									// Each selected still contained in the selection list.
									break checkSelection;
								}
							}
							List<Object> newSelection = new ArrayList<>(selection);
							newSelection.removeAll(invalidSelected);
							setValue(newSelection);
							break checkSelection;
						}
					}
				}
			}
		}
	}

	/**
	 * @see #setOptionLabelProvider(LabelProvider)
	 */
	public LabelProvider getOptionLabelProvider() {
		return SelectFieldUtils.getOptionLabelProvider(this);
	}
	
	/**
	 * Sets the {@link LabelProvider} that is responsible to translate the
	 * {@link #setOptions(List) options} of this {@link SelectField} into
	 * internationalized end-user-readable texts.
	 * 
	 * @param optionLabels
	 *     The new {@link LabelProvider}.
	 */
	public void setOptionLabelProvider(LabelProvider optionLabels) {
		SelectFieldUtils.setOptionLabelProvider(this, optionLabels);
	}
	
	/**
	 * @see #setOptionContextMenu(ContextMenuProvider)
	 */
	public ContextMenuProvider getOptionContextMenu() {
		return SelectFieldUtils.getOptionContextMenu(this);
	}

	/**
	 * Sets the {@link ContextMenuProvider} that is responsible for the creation of a custom context
	 * menu for a selected option.
	 * 
	 * @param contextMenu
	 *        The new {@link ContextMenuProvider}.
	 */
	public void setOptionContextMenu(ContextMenuProvider contextMenu) {
		SelectFieldUtils.setOptionContextMenu(this, contextMenu);
	}

    /**
	 * The number of options in this {@link SelectField}. May be {@link OptionModel#INFINITE} if
	 * there are infinite options.
	 * 
	 * @see OptionModel#getOptionCount()
	 */
	public int getOptionCount() {
        return options.getOptionCount();
    }

    /**
	 * Whether the options of this field are a plain list
	 * 
	 * @see #getOptions()
	 */
    public boolean isOptionsList() {
		return (options instanceof ListOptionModel);
    }
    
    /**
	 * Whether the options of this field are structured as tree.
	 * 
	 * @see #getOptionsAsTree()
	 */
    public boolean isOptionsTree() {
		return (options instanceof TreeOptionModel);
    }
    
    /**
	 * All selectable options as unmodifiable list.
	 * 
	 * <p>
	 * Must only be called, if {@link #isOptionsList()}.
	 * </p>
	 * 
	 * @throws ClassCastException
	 *         if the {@link #getOptionModel()} is not a {@link ListOptionModel}.
	 */    
	public List getOptions() {
		return ((ListOptionModel<?>) options).getBaseModel();
	}

	/**
	 * All selectable options as tree.
	 * 
	 * <p>
	 * Must only be called, if {@link #isOptionsTree()}.
	 * </p>
	 * 
	 * @throws ClassCastException
	 *         if the {@link #getOptionModel()} is not a {@link TreeOptionModel}.
	 */
	public TreeOptionModel<?> getOptionsAsTree() {
		return (TreeOptionModel<?>) options;
	}

	/**
	 * Return an {@link Iterator} over the subset of currently selected options.
	 * 
	 * <p>
	 * Note: Calling this method <b>must not access</b> the list of all options
	 * (see {@link #setOptions(List)}).
	 * </p>
	 * 
	 * @return An {@link Iterator} over the currently selected options.
	 */
	public Iterator getSelectionIterator() {
		return internalGetSelection().iterator();
	}

	/**
	 * Return the subset of the currently selected options.
	 * 
	 * <p>
	 * Note: Calling this method <b>must not access</b> the list of all options
	 * (see {@link #setOptions(List)}).
	 * </p>
	 * 
	 * @return A set view of the currently selected options.
	 */
	public Set getSelectionSet() {
		List selection = internalGetSelection();
		return Collections.unmodifiableSet(new HashSet(selection));
	}

	/**
	 * Like {@link #getValue()}, but ensures that a non-<code>null</code>
	 * value is returned.
	 * 
	 * @return
	 *     The current selection as {@link List} (non-<code>null</code>)).
	 */
	public List getSelection() {
		return internalGetSelection();
	}

	/**
	 * Type-safe convenience shortcut for {@link #setValue(Object)}.
	 * 
	 * <p>
	 * Note: Calling this method <b>must not access</b> the list of all options
	 * (see {@link #setOptions(List)}).
	 * </p>
	 * 
	 * @param selection
	 *     The new selection. Must contain only objects from the list of
	 *     all options (see {@link #setOptions(List)}).
	 */
	public void setAsSelection(List selection) {
		setValue(selection);
	}

	/**
	 * Convenience shortcut for {@link #setValue(Object)}, if the new selection
	 * only contains a single entry.
	 * 
	 * @param selection
	 *     A selected object from the list of all options (see
	 *     {@link #setOptions(List)}), or <code>null</code>, which
	 *     means the empty selection.
	 */
	public void setAsSingleSelection(Object selection) {
		if (selection == null) {
			setValue(Collections.EMPTY_LIST);
			return;
		}

		setValue(Collections.singletonList(selection));
	}
	
	/**
	 * Type-safe convenience shortcut for {@link #initializeField(Object)}
	 * 
	 * @param defaultSelection
	 *            the new selection.
	 */
	public void initSelection(List defaultSelection) {
		initializeField(defaultSelection);
	}
	
	/**
	 * Convenience shortcut for {@link #initializeField(Object)}, if the new selection
	 * only contains a single entry.
	 * 
	 * @param defaultSelection
	 *     A selected object from the list of all options (see
	 *     {@link #setOptions(List)}), or <code>null</code>, which
	 *     means the empty selection.
	 */
	public void initSingleSelection(Object defaultSelection) {
		if (defaultSelection == null) {
			initializeField(Collections.EMPTY_LIST);
		} else {
			initializeField(Collections.singletonList(defaultSelection));
		}
	}
	
    /**
	 * Convenience shortcut for {@link #getSelection()} in case this
	 * {@link SelectField} is configured for single selection.
	 * 
	 * @return The currently selected value, or <code>null</code>, if there
	 *     is currently no selection.
	 */
    public Object getSingleSelection() {
		List selection = internalGetSelection();

		if (selection.size() == 0) 
			return null;
		
		return selection.get(0);
	}
    
    /**
     * Check, whether the current selection is empty.
     */
    public boolean isSelectionEmpty() {
        return internalGetSelection().isEmpty();
    }

    /**
     * @see com.top_logic.layout.form.model.AbstractFormField#parseRawValue(java.lang.Object)
     * 
     * TODO BHU: Re-formulate:
	 * Note: Must not throw {@link CheckException} in that state (parsing), because this would leave the field in
	 * a state (illegal input) that cannot be rendered by views (e.g. single selection fields that have
	 * multiple selections).
	 */
	@Override
	protected Object parseRawValue(Object newRawValue) throws CheckException {
		
		if (newRawValue == NO_RAW_VALUE) return NO_SELECTION;
		List selection;
		if(newRawValue instanceof String) {
			String toMatch = (String)newRawValue;
			
			if (hasNoInputText(toMatch)) {
				selection = NO_SELECTION;
			} else if (isMultiple()) {
				selection = parseMultiSelection(toMatch);
			} else {
				selection = parseSingleSelection(toMatch);
			}
		}
		else {
			List rawList = (List) newRawValue;
			if (rawList.size() == 0) return NO_SELECTION;
			
			selection = new ArrayList(rawList.size());
			for (int cnt = rawList.size(), n = 0; n < cnt; n++) {
				String rawOption = (String) rawList.get(n);
				if (rawOption.length() == 0) {
					// Filter out empty raw values. This is necessary for
					// compatibility with conventional HTTP form submission of
					// custom input controls that store their (single) selection in
					// a hidden input element. If nothing is selected, this input
					// element contains an empty string, which must be interpreted
					// as empty selection.
					continue;
				}
				
				// Fetch the option that corresponds to the passed client-side
				// identifier.
				Object selectedOption;
				try {
					selectedOption = getOptionByID(rawOption);
				} catch (IllegalArgumentException ex) {
					// This should not happen, if the form framework works
					// correctly. This might happen, if the client-side script of
					// any select field view is erroneous.
					
					// Must not throw CheckException (see above).
					Logger.error(Resources.getLogInstance().getString(
						com.top_logic.layout.form.I18NConstants.INTERNAL_ILLEGAL_UPDATE__FIELD_VALUE.fill(getName(),
							rawOption)),
						this);
					
					return NO_SELECTION;
				}
				if (selectedOption == NO_OPTION) {
					// Filter out the implicit option, which represents the empty
					// selection.
					continue;
				}
				
				selection.add(selectedOption);
			}
			
			if (! isMultiple()) {
				if (selection.size() > 1) {
					// This should not happen, if the form framework works
					// correctly. This might happen, if the client-side script of
					// any select field view is erroneous.
	
					// Must not throw CheckException (see above).
					Logger.error(Resources.getLogInstance().getString(
						com.top_logic.layout.form.I18NConstants.SELECTION_TO_BIG__MAXSIZE.fill(1)), this);
					
					return NO_SELECTION;
				}
			}
		}
		
		checkFixedOptionMove(selection);
		return selection;
	}

	private List parseMultiSelection(String toMatch) throws CheckException {
		List selection = new ArrayList();
		String[] rawOptions = toMatch.split(getQuotedMultiSelectionSeparator(this));
		for (String rawOption : rawOptions) {
			String trimmedRawOption = rawOption.trim();
			if (!StringServices.isEmpty(trimmedRawOption)) {
				List singleSelection = parseSingleSelection(trimmedRawOption);
				if (!selection.contains(singleSelection.get(0))) {
					selection.addAll(singleSelection);
				}
			}
		}
		return selection;
	}

	private List parseSingleSelection(String toMatch) throws CheckException {
		List selection = this.getMatchingOptions(toMatch, true);
		if (selection.size() > 1) {
			throw new CheckException(Resources.getInstance().getString(com.top_logic.layout.form.I18NConstants.AMBIGUOUS_INPUT__VALUE.fill(toMatch)));
		}
		if (selection.size() == 0) {
			throw new CheckException(Resources.getInstance().getString(com.top_logic.layout.form.I18NConstants.NO_MATCH_FOR_INPUT__VALUE.fill(toMatch)));
		}
		return selection;
	}

	private boolean hasNoInputText(String toMatch) {
		return StringServices.isEmpty(toMatch);
	}

	@Override
	protected Object unparseValue(Object aSelection) {
		List selection = (List) aSelection;
		if (selection == null) {
			selection = NO_SELECTION;
		}
		
		if ((! isMultiple()) && (selection.size() == 0)) {
			// Make sure to select the "no option" at the GUI, if the selection
			// was cleared in a single selection field.
			return Collections.singletonList(getOptionID(NO_OPTION));
		}
		
		ArrayList result = new ArrayList(selection.size());
		for (int cnt = selection.size(), n = 0; n < cnt; n++) {
			result.add(getOptionID(selection.get(n)));
		}
		return result;
	}

    /**
     * Mask null selection as EMPTY_LIST, check for correct (multi-)selection.
     * 
     * @throws IllegalArgumentException when selection size and {@link #isMultiple()} do not match.
     * 
     * @see com.top_logic.layout.form.model.AbstractFormField#narrowValue(java.lang.Object)
     */
	@Override
	protected Object narrowValue(Object aSelection) {
		Collection selection = (Collection) aSelection;
		
		if (aSelection == null) {
			selection = Collections.EMPTY_LIST;
		}
        else if (aSelection instanceof List) {
        	// TODO BHU: spurious assignment.
            selection = (Collection) aSelection;
        }
        else if (aSelection instanceof Collection) {
            // TODO 
            // Maybe we would like to have a DisplayComparator
            ArrayList newSelection = new ArrayList((Collection) aSelection);
            Comparator sortby = this.getOptionComparator();
            if (sortby != null) {
                Collections.sort(newSelection, sortby);
            }
            selection = newSelection;
        }
		
		if (isMultiple()) {
			try {
				checkFixedOptionMove(selection);
			} catch (CheckException ex) {
				throw new IllegalArgumentException("It has been tried to select or unselect fixed options. Details: "
					+ ex.getMessage());
			}
		} else {
			if (selection.size() > 1) {
				throw new IllegalArgumentException(
					"Selection size on a single selection field must not be greater than 1.");
			}
		}
		
		return selection;
	}

	private void checkFixedOptionMove(Collection newSelection) throws CheckException {
		Filter fixedFilter = getFixedOptions();
		if (fixedFilter != null) {
			// Check, whether the new value is compatible with the
			// configured fixed option filter.
			List<?> validSelectionSelection = (List<?>) getStoredValue();

			HashSet removedElements = new HashSet(validSelectionSelection);
			removedElements.removeAll(newSelection);
			
			if (FilterUtil.hasMatch(removedElements, fixedFilter)) {
				List<?> removedFixedElements = FilterUtil.filterList(fixedFilter, removedElements);
				throwExceptionWithMessage(FIXED_OPTIONS_REMOVED_KEY, removedElements);
			}
			
			HashSet addedElements = new HashSet(newSelection);
			addedElements.removeAll(validSelectionSelection);
			
			if (FilterUtil.hasMatch(addedElements, fixedFilter)) {
				List<?> addedFixedElements = FilterUtil.filterList(fixedFilter, addedElements);
				if (!validSelectionSelection.containsAll(addedFixedElements)) {
					throwExceptionWithMessage(FIXED_OPTIONS_ADDED_KEY, addedElements);
				}
			}
		}
	}

	private void throwExceptionWithMessage(ResKey messageKey, HashSet movedElements) throws CheckException {
		String message = Resources.getInstance().getMessage(messageKey, movedElements);
		throw new CheckException(message);
	}

    /**
	 * Returns an internal identifier for the given option.
	 * 
	 * <p>
	 * The returned identifier can be used to find the corresponding option in a
	 * call to {@link #getOptionByID(String)}.
	 * </p>
	 * 
	 * <p>
	 * Note: Calling this method <b>must not access</b> the list of all options
	 * (see {@link #setOptions(List)}).
	 * </p>
	 * 
	 * @param option
	 *     An element of the list of all options of this
	 *     {@link SelectField}.
	 * @return An identifier that can be used to find the given option in a call
	 *     to
	 */
	public final String getOptionID(Object option) {
		return optionIDs.makeId(option);
	}
	
	/**
	 * Returns the option from the {@link #setOptions(List) list of all options}
	 * of this {@link SelectField} that is associated to the given identifier.
	 * 
	 * <p>
	 * Note: Calling this method <b>must not access</b> the list of all options
	 * (see {@link #setOptions(List)}).
	 * </p>
	 * 
	 * @param id
	 *     The identifier as returned by {@link #getOptionID(Object)}.
	 * @return The option that is associated with the given identifier.
	 * 
	 * @throws IllegalArgumentException
	 *     If the given identifier is not one that was returned from {@link #getOptionID(Object)}.
	 */
	public Object getOptionByID(String id) throws IllegalArgumentException {
		return optionIDs.getObjectById(id);
	}
	
    /**
	 * Wrapper method for {@link #getValue()}. This method ensures that a non-<code>null</code>
	 * value is returned. This guarantee cannot be given for {@link #getValue()},
	 * because the value is managed by a super class and initialized with
	 * <code>null</code>.
	 */
	protected List internalGetSelection() {
		List selection = (List) getValue();

		if (selection == null) 
			return Collections.EMPTY_LIST;

		return selection;
	}

	/**
	 * Check, whether this {@link SelectField} is configured for multiple
	 * selection.
	 * 
	 * <p>
	 * Note: the value of this property cannot be changed during the life time
	 * of this field, because this would require to dynamically exchange the
	 * view at the client-side (which is not supported).
	 * </p>
	 */
	public boolean isMultiple() {
		return this.multiple;
	}

    /** 
	 * An internationalized string representation for the given option that can be 
	 * displayed to the end user.
	 */
	public String getOptionLabel(Object option) {
		return SelectFieldUtils.getOptionLabel(this, option);
	}

	/**
	 * The label for the {@link #NO_OPTION}.
	 */
	private String getEmptyLabel() {
		return SelectFieldUtils.getEmptySelectionLabel(this);
    }

	/**
	 * Sets the label for the value that represents the empty selection.
	 * 
	 * <p>
	 * If no value is set, a default label is taken from the resources.
	 * </p>
	 */
    public void setEmptyLabel(String aLabel) {
    	SelectFieldUtils.setEmptySelectionLabel(this, aLabel);
    }

	/**
	 * Sets the label for the value that represents the empty selection in
	 * immutable mode.
	 * 
	 * <p>
	 * If no value is set, a default label is taken from the resources.
	 * </p>
	 */
    public void setEmptyLabelImmutable(String aLabel) {
    	SelectFieldUtils.setEmptySelectionLabelImmutable(this, aLabel);
    }

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitSelectField(this, arg);
	}

    /**
     * This method returns the customOrder.
     * 
     * @return    Returns the customOrder.
     */
    public boolean hasCustomOrder() {
        return (this.customOrder);
    }

    /**
     * This method sets the customOrder.
     *
     * @param    aCustomOrder    The customOrder to set.
     */
    public void setCustomOrder(boolean aCustomOrder) {
        this.customOrder = aCustomOrder;
    }
    
    /**
	 * Tries to find all Entries which match the given string.
	 * 
	 * <p>
	 * To check for a match the option label provider is used. Per default the normal label is used
	 * for matching. If the configured provider implements the {@link Matcher}-Interface the
	 * additional options are used to find a match as well.
	 * </p>
	 * 
	 * <p>
	 * If tryPerfect is <code>true</code>, the result is narrowed down to the matching options whose
	 * label equals the given 'toMatch' (a perfect match). If no perfect match was found the normal
	 * matches are returned.
	 * </p>
	 * 
	 * @param toMatch
	 *        the string to match with the options matching information (normally their respective
	 *        label)
	 * @param tryPerfect
	 *        if <code>true</code> the result is narrowed down to perfect matches, if possible.
	 * @return a List of options that have matching Labels or matching additional information.
	 */
    public List getMatchingOptions(String toMatch, boolean tryPerfect) {
		List<?> matchingOptions = new ArrayList();
		List<?> perfectMatches = new ArrayList();
		
		addMatchingOptions(getOptionsIterator(), toMatch, tryPerfect, matchingOptions, perfectMatches);
		
		List selection = getSelection();
		if (!selection.isEmpty()) {
			// Make sure that even inconsistent selection values (which are no longer among the
			// current options) can still be found. By including both, the options and the current
			// selection into the same result, it is guaranteed that that an uniquely identified
			// option is unique within the union of the current options and the current selection.#
			if (matchingOptions.isEmpty() && perfectMatches.isEmpty()) {
				addMatchingOptions(selection.iterator(), toMatch, tryPerfect, matchingOptions, perfectMatches);
			} else {
				final Set<Object> optionMatches =
					CollectionUtil.newSet(perfectMatches.size() + matchingOptions.size());
				optionMatches.addAll(perfectMatches);
				optionMatches.addAll(matchingOptions);
				addMatchingOptions(new FilteredIterator<>(getSelection().iterator()) {
					@Override
					protected boolean test(Object value) {
						return !optionMatches.contains(value);
					}
				}, toMatch, tryPerfect, matchingOptions, perfectMatches);
			}
		}

		return toResult(tryPerfect, matchingOptions, perfectMatches);
    }

	private void addMatchingOptions(Iterator optionsIt, String toMatch, boolean tryPerfect, List matchingOptions,
			List perfectMatches) {
		String theLiteral = this.normalizeForMatch(toMatch);
		
		Matcher autocompletionMatcher;
		LabelProvider optionLabels = getOptionLabelProvider();
		if (optionLabels instanceof Matcher) {
			autocompletionMatcher = (Matcher) optionLabels;
		} else {
			autocompletionMatcher = Matcher.NONE;
		}
		
		Filter fixedOptions = getFixedOptionsNonNull();
		int matches = 0;
		for (; optionsIt.hasNext();) {
			// try to find one match more than the limit to be able to display additional
			// information about the found matches.
			if ((! tryPerfect) && matches >= SelectionControl.AutoCompletion.MAX_DISPLAYED_ENTRIES +1) {
				break;
			}
			Object option = optionsIt.next();
			if (isUnselectedFixedOption(fixedOptions, option)) {
				continue;
			}
			
			String optionLabel = optionLabels.getLabel(option);
			if (StringServices.isEmpty(optionLabel)) {
				continue;
			}
			String canonicalRepresentation = this.normalizeForMatch(optionLabel);
			int matchIndex = canonicalRepresentation.indexOf(theLiteral);
			if (matchIndex >= 0) {
				if (canonicalRepresentation.length() == toMatch.length()) {
					perfectMatches.add(option);
					matches ++;
				} else {
					matchingOptions.add(option);
					matches ++;
				}
			} else {
				if (autocompletionMatcher.match(theLiteral, option)) {
					matchingOptions.add(option);
					matches ++;
				}
			}
		}
	}

	private List toResult(boolean tryPerfect, List matchingOptions, List perfectMatches) {
		if (tryPerfect) {
			if (perfectMatches.size() > 0) {
				return perfectMatches;
			}
			
			return matchingOptions;
		} else {
			// Note: The intersection of perfect matches and matching options is empty.
			perfectMatches.addAll(matchingOptions);
			return perfectMatches;
		}
	}

	private boolean isUnselectedFixedOption(Filter fixedOptions, Object option) {
		return fixedOptions.accept(option) && !((List<?>) getStoredValue()).contains(option);
	}

	private Iterator getOptionsIterator() {
		return options.iterator();
	}
    
    /**
	 * Removes double spaces and other whitespace characters to enable matching with
	 * String.indexof().
	 * 
	 * @param label
	 *        the label to normalize
	 * @return a normalized version of the string with no double whitespaces.
	 */
	private String normalizeForMatch(String label) {
		return StringServices.normalizeWhiteSpace(label.toLowerCase());
    }
    
	/**
	 * Determines whether typing on JSP shall result in a Filter which matches any options
	 * containing the text as substring, or only the options which contains the text as word prefix.
	 */
	public void setMatchSubstring(boolean matchSubstring) {
		set(MATCH_SUBSTRING, Boolean.valueOf(matchSubstring));
	}
	
	/**
	 * This method returns the value set in {@link #setMatchSubstring(boolean)} if was used,
	 * otherwise the application wide configured value.
	 */
	public boolean matchSubstring() {
		Boolean property = get(MATCH_SUBSTRING);
		return property == null ? getSelectFieldConfiguration().isSubstringMatchEnabled() : property.booleanValue();
	}

	/** 
	 * Return the table description provider for this field.
	 * 
	 * @return    The requested provider, may be <code>null</code>.
	 */
	public TableConfigurationProvider getTableConfigurationProvider() {
		return get(TABLE_CONFIGURATION_PROVIDER);
	}

	/** @see #getTableConfigurationProvider() */
	public void setTableConfigurationProvider(TableConfigurationProvider provider) {
		set(TABLE_CONFIGURATION_PROVIDER, provider);
	}

	@Override
	public TableData getTableData() {
		TableData result = getTableDataOrNull();
		if (result == null) {
			ArrayList<TableConfigurationProvider> providers = new ArrayList<>();
			if (this instanceof SelectionTableField) {
				providers.add(new SetDefaultAccessor(((SelectionTableField) this).getOptionAccessor()));
			}

			TableConfigurationProvider tableConfigurationProvider = getTableConfigurationProvider();
			if (tableConfigurationProvider != null) {
				providers.add(tableConfigurationProvider);
			}

			providers.add(GenericTableConfigurationProvider.showDefaultColumns());

			TableConfiguration configuration = TableConfigurationFactory.build(providers);
			List<String> defaultColumns = configuration.getDefaultColumns();
			SelectionTableModel tableModel;
			if (!defaultColumns.isEmpty()) {
				tableModel = new SelectionTableModel(this,
					defaultColumns.toArray(new String[defaultColumns.size()]),
					configuration);
			} else {
				tableModel = new SelectionTableModel(this, null,
					configuration);
			}
			result = DefaultTableData.createTableData(this, tableModel, getConfigKey());

			set(TABLE_DATA, result);
		}
		return result;
	}

	public ConfigKey getConfigKey() {
		return ConfigKey.field(getConfigNameMapping(), this);
	}

	public Mapping<FormMember, String> getConfigNameMapping() {
		Mapping<FormMember, String> customNameMapping = get(CONFIG_NAME_MAPPING);
		if (customNameMapping != null) {
			return customNameMapping;
		}
		return QUALIFIED_NAME_MAPPING;
	}

	public void setConfigNameMapping(Mapping<FormMember, String> nameMapping) {
		set(CONFIG_NAME_MAPPING, nameMapping);
	}

	/**
	 * The {@link TableData} of this field, if it was requested through {@link #getTableData()}
	 * before.
	 * 
	 * @see #getTableData()
	 */
	public TableData getTableDataOrNull() {
		return get(TABLE_DATA);
	}

	@Override
	protected SelectField self() {
		return this;
	}


}
