/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.CustomComparator;
import com.top_logic.basic.col.LazyListModifyable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.SelectField.Config;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.util.Resources;

/**
 * Utility methods for dealing with values of a {@link SelectField}, or {@link FormField} rendered
 * as {@link SelectionControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectFieldUtils {

	/**
	 * @see SelectFieldUtils#setOptions(FormField, List)
	 */
	private static final Property<List<?>> OPTIONS_KEY = TypedAnnotatable.propertyList("options");

	static {
		Config selectFieldConfiguration = getSelectFieldConfiguration();
		String configuredSeparator = selectFieldConfiguration.getMultiSelectionSeparator();
		String configuredSeparatorFormat = selectFieldConfiguration.getMultiSelectionSeparatorFormat();
		if (!isValidMultiSelectionSeparator(configuredSeparator)) {
			String fallbackSeparator = SelectField.Config.DEFAULT_MULTI_SELECTION_SEPARATOR;
			PropertyDescriptor separator =
				selectFieldConfiguration.descriptor().getProperty(
					SelectField.Config.MULTI_SELECTION_SEPARATOR_ATTRIBUTE);
			selectFieldConfiguration.update(separator, fallbackSeparator);
			Logger
				.warn(
					getInvalidSeparatorMessage(configuredSeparator)
						+ " --- Check your configuration of 'com.top_logic.layout.form.model.SelectField$Config'. Using fallback separator '"
						+ fallbackSeparator + "'.", SelectFieldUtils.class);
			configuredSeparator = fallbackSeparator;
		}
		if (!isValidMultiSelectionSeparatorFormat(configuredSeparator, configuredSeparatorFormat)) {
			PropertyDescriptor separatorFormat =
				selectFieldConfiguration.descriptor().getProperty(
					SelectField.Config.MULTI_SELECTION_SEPARATOR_FORMAT_ATTRIBUTE);
			String fallbackSeparatorFormat = configuredSeparator + " ";
			selectFieldConfiguration.update(separatorFormat, fallbackSeparatorFormat);
			Logger.warn(getInvalidSeparatorFormatMessage(configuredSeparator, configuredSeparatorFormat)
						+ " --- Check your configuration of 'com.top_logic.layout.form.model.SelectField$Config'. Using fallback separator format '"
						+ fallbackSeparatorFormat + "'.", SelectFieldUtils.class);
		}
	}

	public static <T> List<T> sortCopy(Comparator<? super T> comparator, Collection<? extends T> inputList) {
		List<T> outputList = new ArrayList<>(inputList);
		Collections.sort(outputList, comparator);
		return outputList;
	}

	/**
	 * new ordered list of joined {@link SelectField}s options and selection
	 */
	public static List<?> getOptionAndSelectionOuterJoinOrdered(SelectField field) {
		Comparator comparator = field.getOptionComparator();
		List<?> options = field.getOptions();
		List<?> selection = field.getSelection();
		return joinOrdered(comparator, options, selection);
	}

	public static <T> List<T> joinOrdered(Comparator<? super T> comparator, Collection<? extends T> options,
			Collection<? extends T> selection) {
		List<T> union = join(options, selection);
		Collections.sort(union, comparator);

		return union;
	}

	private static <T> List<T> join(Collection<? extends T> options, Collection<? extends T> selection) {
		Set<T> selectionSet = selectionNotInOption(options, selection);

		List<T> union = new ArrayList<>(options.size() + selectionSet.size());
		union.addAll(options);
		union.addAll(selectionSet);
		return union;
	}

	private static <T> LinkedHashSet<T> selectionNotInOption(Collection<? extends T> options,
			Collection<? extends T> selection) {
		LinkedHashSet<T> selectionSet = new LinkedHashSet<>(selection);
		selectionSet.removeAll(options);
		return selectionSet;
	}

	/**
	 * The {@link LabelProvider} for {@link #getOptions(FormField)} of an arbitrary
	 * {@link FormField} in selection context.
	 */
	public static LabelProvider getOptionLabelProvider(FormField field) {
		LabelProvider result = field.get(SelectField.OPTION_LABEL_PROVIDER);
		if (result != null) {
			return result;
		}
		
		return MetaResourceProvider.INSTANCE;
	}

	/**
	 * @see #getOptionLabelProvider(FormField)
	 */
	public static void setOptionLabelProvider(FormField field, LabelProvider value) {
		if (value == null) {
			field.reset(SelectField.OPTION_LABEL_PROVIDER);
		} else {
			field.set(SelectField.OPTION_LABEL_PROVIDER, value);
		}
	}

	/**
	 * The {@link LabelProvider} for {@link #getOptions(FormField)} of an arbitrary
	 * {@link FormField} in selection context.
	 */
	public static ContextMenuProvider getOptionContextMenu(FormField field) {
		ContextMenuProvider result = field.get(SelectField.OPTION_CONTEXT_MENU);
		if (result != null) {
			return result;
		}

		return NoContextMenuProvider.INSTANCE;
	}

	/**
	 * @see #getOptionLabelProvider(FormField)
	 */
	public static void setOptionContextMenu(FormField field, ContextMenuProvider value) {
		if (value == null) {
			field.reset(SelectField.OPTION_CONTEXT_MENU);
		} else {
			field.set(SelectField.OPTION_CONTEXT_MENU, value);
		}
	}

	/**
	 * The {@link Comparator} for options of an arbitrary {@link FormField} in selection context.
	 */
	public static Comparator getOptionComparator(FormField field) {
		Comparator comparator = field.get(SelectField.COMPARATOR_PROPERTY);
		return comparator == null ? ComparableComparator.INSTANCE : comparator;
	}

	/**
	 * Whether the selection of the given field has an order explicitly specified by the user.
	 */
	public static boolean hasCustomOrder(FormField field) {
		if (field instanceof SelectField) {
			return ((SelectField) field).hasCustomOrder();
		} else {
			return false;
		}
	}

	/**
	 * @see #getOptionComparator(FormField)
	 */
	public static void setOptionComparator(FormField field, Comparator optionComparator) {
		if (optionComparator == null) {
			field.reset(SelectField.COMPARATOR_PROPERTY);
		} else {
			field.set(SelectField.COMPARATOR_PROPERTY, optionComparator);
		}
	}

	/**
	 * Sets an comparator to the select field that compares the options/selection as given in the
	 * {@link SelectField}.
	 * 
	 * <p>
	 * <b>Note:</b> The options and selection of the {@link SelectField} are resolved to create
	 * comparator, i.e. such an comparator is not useful for fields with {@link LazyListModifyable}
	 * as option.
	 * </p>
	 * 
	 * <p>
	 * Changing options does not adapt comparator, i.e. all new options are treated as equal and
	 * larger than old options.
	 * </p>
	 * 
	 * <p>
	 * Not possible for fields with {@link TLTreeModel tree model} based options.
	 * </p>
	 */
	public static void setCustomOrderComparator(SelectField field) {
		List<?> optionsToCompare = join(field.getOptions(), field.getSelection());
		Comparator<Object> customComparator = CustomComparator.newCustomComparator(optionsToCompare);
		setOptionComparator(field, customComparator);
	}

	/**
	 * Text to be displayed for an empty value of a {@link FormField} in selection context.
	 */
	public static String getEmptySelectionLabel(FormField self) {
		return getEmptySelectionLabel(self, self.isMandatory());
	}

	/**
	 * Text to be displayed for an empty value of a {@link FormField} in selection context.
	 */
	public static String getEmptySelectionLabel(FormField self, boolean isMandatory) {
		if (self.isImmutable()) {
			return getEmptySelectionLabelImmutable(self);
		} else {
			String noOptionLabel = internalGetEmptyLabel(self);
			if (noOptionLabel != null) {
				return noOptionLabel;
			}

			Resources res = Resources.getInstance();
			if (isMandatory) {
				return res.getString(getOptions(self).size() == 0 ?
					I18NConstants.MANDATORY_EMPTY_SINGLE_SELECTION_LABEL_WITH_NO_OPTIONS :
					I18NConstants.MANDATORY_EMPTY_SINGLE_SELECTION_LABEL);
			} else {
				return res.getString(getOptions(self).size() == 0 ?
					I18NConstants.EMPTY_SINGLE_SELECTION_LABEL_WITH_NO_OPTIONS :
					I18NConstants.EMPTY_SINGLE_SELECTION_LABEL);
			}
		}
	}

	/**
	 * @see #getEmptySelectionLabel(FormField)
	 */
	public static void setEmptySelectionLabel(FormField field, String aLabel) {
		field.set(SelectField.EMPTY_LABEL_PROPERTY, aLabel);
	}

	/**
	 * Text to be displayed for an empty value of a {@link FormField} in selection context in
	 * immutable state.
	 */
	public static String getEmptySelectionLabelImmutable(FormField self) {
		String noOptionLabelImmutable = internalGetEmptyLabelImmutable(self);
		if (noOptionLabelImmutable != null) {
			return noOptionLabelImmutable;
		}

		Resources res = Resources.getInstance();
		return res.getString(I18NConstants.EMPTY_SINGLE_SELECTION_LABEL_IMMUTABLE);
	}

	/**
	 * @see #getEmptySelectionLabel(FormField)
	 */
	public static void setEmptySelectionLabelImmutable(FormField selectField, String aLabel) {
		selectField.set(SelectField.EMPTY_LABEL_IMMUTABLE_PROPERTY, aLabel);
	}

	private static String internalGetEmptyLabelImmutable(FormField self) {
		return self.get(SelectField.EMPTY_LABEL_IMMUTABLE_PROPERTY);
	}

	private static String internalGetEmptyLabel(FormField self) {
		return self.get(SelectField.EMPTY_LABEL_PROPERTY);
	}

	/**
	 * The options for a {@link FormField} in a selection context.
	 * 
	 * @see #setOptions(FormField, List)
	 */
	public static List<?> getOptions(FormField field) {
		if (field instanceof SelectField) {
			return ((SelectField) field).getOptions();
		}
		
		return field.get(OPTIONS_KEY);
	}

	/**
	 * The {@link OptionModel} of a {@link FormField} with options.
	 */
	public static OptionModel<?> getOptionModel(FormField field) {
		if (field instanceof SelectField) {
			return ((SelectField) field).getOptionModel();
		}

		return new DefaultListOptionModel<>(field.get(OPTIONS_KEY));
	}

	/**
	 * Annotates options on an arbitrary {@link FormField} to be displayed as selection.
	 */
	public static void setOptions(FormField field, List<?> options) {
		if (field instanceof SelectField) {
			((SelectField) field).setOptions(options);
		}

		field.set(OPTIONS_KEY, options);
	}

	/**
	 * Computes a label for a select option of an arbitrary {@link FormField} in selection context.
	 * 
	 * @see #setOptions(FormField, List)
	 * @see #setOptionLabelProvider(FormField, LabelProvider)
	 */
	public static String getOptionLabel(FormField field, Object option) {
		if (option == SelectField.NO_OPTION) {
			return getEmptySelectionLabel(field);
		}
		
		return getOptionLabelProvider(field).getLabel(option);
	}

	/**
	 * Annotates, whether selection of multiple options is possible via textual input, or not, to an
	 * arbitrary {@link FormField}.
	 */
	public static void setMultiSelectionTextInputEnabled(FormField field, boolean isMultiSelectionTextInputEnabled) {
		field.set(SelectField.MULTI_SELECTION_TEXT_INPUT_PROPERTY, isMultiSelectionTextInputEnabled);
	}

	/**
	 * @see #setMultiSelectionTextInputEnabled(FormField, boolean)
	 */
	public static void removeMultiSelectionTextInputEnabled(FormField field) {
		field.reset(SelectField.MULTI_SELECTION_TEXT_INPUT_PROPERTY);
	}

	/**
	 * true, if the field was globally or locally configured to support selection of
	 *         multiple options via textual input, or not.
	 * @see #setMultiSelectionTextInputEnabled(FormField, boolean)
	 */
	public static boolean isMultiSelectionTextInputEnabled(FormField field) {
		if (field.isSet(SelectField.MULTI_SELECTION_TEXT_INPUT_PROPERTY)) {
			return field.get(SelectField.MULTI_SELECTION_TEXT_INPUT_PROPERTY).booleanValue();
		} else {
			return getSelectFieldConfiguration().isMultiSelectionTextInputEnabled();
		}
	}

	/**
	 * Annotates the separator and the separator format between multiple selected options on an
	 * arbitrary {@link FormField}. Thereby the separator format must only contain separator and
	 * optional whitespace.
	 * 
	 * @see SelectFieldUtils#setCollectionSeparator(FormField, String) Setting a separator for
	 *      immutable mode.
	 */
	public static void setMultiSelectionSeparator(FormField field, String separator, String separatorFormat) {
		boolean isValidSeparator = isValidMultiSelectionSeparator(separator);
		if (isValidSeparator && isValidMultiSelectionSeparatorFormat(separator, separatorFormat)) {
			field.set(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY, separator);
			field.set(SelectField.MULTI_SELECTION_SEPARATOR_FORMAT_PROPERTY, separatorFormat);
		} else {
			String errorMessage;
			if(isValidSeparator) {
				errorMessage = getInvalidSeparatorFormatMessage(separator, separatorFormat);
			} else {
				errorMessage = getInvalidSeparatorMessage(separator);
			}
			throw new IllegalArgumentException(errorMessage);
		}
	}

	/**
	 * Annotates the separator between multiple selected options on an arbitrary {@link FormField}
	 * in immutable mode.
	 * 
	 * @see SelectFieldUtils#getCollectionSeparator(FormField)
	 * @see SelectFieldUtils#setMultiSelectionSeparator(FormField, String, String) Setting a
	 *      separator for editable and disabled mode.
	 */
	public static void setCollectionSeparator(FormField field, String separator) {
		field.set(SelectField.COLLECTION_SEPARATOR_PROPERTY, separator);
	}

	private static String getInvalidSeparatorFormatMessage(String separator, String separatorFormat) {
		return "Separator format of multiple selection must only contain separator '"
			+ separator
			+ "' and optional whitespace, but is: '" + separatorFormat + "'.";
	}

	private static boolean isValidMultiSelectionSeparatorFormat(String separator, String separatorFormat) {
		String validSeparatorFormatRegEx = "\\s*" + Pattern.quote(separator) + "\\s*";
		return separatorFormat.matches(validSeparatorFormatRegEx);
	}

	private static boolean isValidMultiSelectionSeparator(String separator) {
		return separator.length() == 1;
	}

	private static String getInvalidSeparatorMessage(String separator) {
		return "Separator of multiple selection must be a single character, but is '"
			+ separator
			+ "'.";
	}

	/**
	 * Removes the separator and the separator format between multiple selected options from an
	 * arbitrary {@link FormField}
	 */
	public static void removeMultiSelectionSeparator(FormField field) {
		field.reset(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY);
		field.reset(SelectField.MULTI_SELECTION_SEPARATOR_FORMAT_PROPERTY);
	}

	/**
	 * the separator of multiple selected options, which was globally or locally configured.
	 * @see #setMultiSelectionSeparator(FormField, String, String)
	 */
	public static String getMultiSelectionSeparator(FormField field) {
		if (field.isSet(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY)) {
			return field.get(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY);
		} else {
			return getSelectFieldConfiguration().getMultiSelectionSeparator();
		}

	}

	/**
	 * the separator of multiple selected options, which was globally or locally configured.
	 *         The value is quoted for direct use in regular expressions.
	 * @see #setMultiSelectionSeparator(FormField, String, String)
	 */
	public static String getQuotedMultiSelectionSeparator(FormField field) {
		String separatorString;
		if (field.isSet(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY)) {
			separatorString = field.get(SelectField.MULTI_SELECTION_SEPARATOR_PROPERTY);
		} else {
			separatorString = getSelectFieldConfiguration().getMultiSelectionSeparator();
		}
		return Pattern.quote(separatorString);
	}

	/**
	 * the separator format of multiple selected options, which was globally or locally
	 *         configured.
	 * @see #setMultiSelectionSeparator(FormField, String, String)
	 */
	public static String getMultiSelectionSeparatorFormat(FormField field) {
		if (field.isSet(SelectField.MULTI_SELECTION_SEPARATOR_FORMAT_PROPERTY)) {
			return field.get(SelectField.MULTI_SELECTION_SEPARATOR_FORMAT_PROPERTY);
		} else {
			return getSelectFieldConfiguration().getMultiSelectionSeparatorFormat();
		}

	}

	/**
	 * The separator of multiple selected options in immutable mode, which was globally or
	 *         locally configured.
	 * 
	 * @see LayoutConfig#getCollectionSeparator()
	 * @see SelectFieldUtils#setCollectionSeparator(FormField, String)
	 */
	public static String getCollectionSeparator(FormField field) {
		if (field.isSet(SelectField.COLLECTION_SEPARATOR_PROPERTY)) {
			return field.get(SelectField.COLLECTION_SEPARATOR_PROPERTY);
		} else {
			return applicationConfig().getConfig(LayoutConfig.class).getCollectionSeparator();
		}

	}

	/**
	 * Access to the application-global configuration for select fields.
	 */
	public static Config getSelectFieldConfiguration() {
		return applicationConfig().getConfig(SelectField.Config.class);
	}

	private static ApplicationConfig applicationConfig() {
		return ApplicationConfig.getInstance();
	}

	/**
	 * Appends an end-user-readable internationalised text describing the current selection in
	 * immutable mode.
	 * 
	 * <p>
	 * The options are separated by {@link #getCollectionSeparator(FormField) separator for
	 * collections} in immutable mode.
	 * </p>
	 * 
	 * <p>
	 * The empty selection is described by the
	 * {@link SelectFieldUtils#setEmptySelectionLabel(FormField, String) label of the empty
	 * selection} in immutable mode.
	 * </p>
	 * 
	 * @param out
	 *        The output to append selection to.
	 * 
	 * @return Given {@link Appendable}.
	 * 
	 * @see SelectFieldUtils#writeSelectionAsTextEditable(Appendable, FormField) Only all selected
	 *      values separated by the given separator.
	 */
	public static <T extends Appendable> T writeSelectionAsTextImmutable(T out, FormField field) throws IOException {
		return writeSelectionAsText(out, field, getCollectionSeparator(field));
	}

	/**
	 * Appends an end-user-readable internationalised text describing the current selection
	 * separated by given separator instead of the default separator.
	 * 
	 * @see SelectFieldUtils#writeSelectionAsTextImmutable(Appendable, FormField)
	 */
	public static <T extends Appendable> T writeSelectionAsText(T out, FormField field, String separator)
			throws IOException {
		return internalWriteSelectionAsText(out, field, true, separator);
	}

	/**
	 * Labels of the current selection joined with the default {@link SelectField} separator.
	 * 
	 * @param out
	 *        The output to append selection to.
	 * 
	 * @return Given {@link Appendable}.
	 * 
	 * @see SelectFieldUtils#writeSelectionAsTextImmutable(Appendable, FormField) A description of the
	 *      selection with a special description for the empty selection.
	 */
	public static <T extends Appendable> T writeSelectionAsTextEditable(T out, FormField field) throws IOException {
		return writeSelectionAsTextPlain(out, field, getMultiSelectionSeparatorFormat(field));
	}

	/**
	 * Labels of the current selection joined with the given separator instead of the default
	 * separator.
	 * 
	 * @see SelectFieldUtils#writeSelectionAsTextImmutable(Appendable, FormField)
	 */
	public static <T extends Appendable> T writeSelectionAsTextPlain(T out, FormField field, String separator)
			throws IOException {
		return internalWriteSelectionAsText(out, field, false, separator);
	}

	private static <T extends Appendable> T internalWriteSelectionAsText(T out, FormField field, boolean useEmptyLabel,
			String separator) throws IOException {
		List<Throwable> labelErrors = new ArrayList<>();
		List<?> theSel = getSelectionList(field);

		int size = theSel.size();

		if (size == 1) {
			// Optimization for single selections.
			out.append(getOptionLabel(field, theSel.get(0)));
			return out;
		}

		if (size == 0) {
			if (useEmptyLabel) {
				out.append(getEmptySelectionLabelImmutable(field));
			}
			return out;
		}

		if (!SelectFieldUtils.hasCustomOrder(field)) {
			theSel = new ArrayList<Object>(theSel);
			Collections.sort(theSel, getOptionComparator(field));
		}

		// Multiple selections
		out.append(getOptionLabelFailsafe(field, theSel.get(0), labelErrors));
		for (int i = 1; i < size; i++) {
			out.append(separator);
			out.append(getOptionLabelFailsafe(field, theSel.get(i), labelErrors));
		}

		if (!CollectionUtils.isEmpty(labelErrors)) {
			RuntimeException labelException = ExceptionUtil.createException(
				"Error occured during rendering of options of field '" + field.getQualifiedName() + "'.",
				labelErrors);
			throw labelException;
		}
		return out;
	}

	private static String getOptionLabelFailsafe(FormField field, Object option, List<Throwable> labelErrors) {
		String optionLabel;
		try {
			optionLabel = getOptionLabel(field, option);
		} catch (Throwable throwable) {
			labelErrors.add(throwable);
			optionLabel = Resources.getInstance()
				.getString(com.top_logic.layout.form.control.I18NConstants.RENDERING_ERROR_SELECT_FIELD);
		}
		return optionLabel;
	}

	/**
	 * sorted list of options, whereby sort order is defined by {@link FormField}'s
	 *         comparator
	 * 
	 * @see #getOptionComparator(FormField)
	 * @see #getSelectionListSorted(FormField)
	 */
	public static List<?> getOptionsListSorted(FormField field) {
		return getSorted(field, getOptions(field));
	}

	/**
	 * sorted list of selected options, whereby sort order is defined by {@link FormField}'s
	 * comparator
	 * 
	 * @see #getOptionComparator(FormField)
	 * @see #getOptionsListSorted(FormField)
	 * @see #getOptionAndSelectionOuterJoinOrdered(SelectField)
	 */
	public static List<?> getSelectionListSorted(FormField field) {
		return getSorted(field, getSelectionList(field));
	}

	private static List<?> getSorted(FormField field, List<?> unsortedList) {
		if (hasCustomOrder(field)) {
			return unsortedList;
		} else {
			switch (unsortedList.size()) {
				case 0:
				case 1:
					// nothing to sort
					return unsortedList;
				default:
					Object[] unsortedArray = unsortedList.toArray();
					Arrays.sort(unsortedArray, getOptionComparator(field));
					return CollectionUtil.unmodifiableList(unsortedArray);
			}
		}
	}

	/**
	 * list of selected options in undefined order
	 */
	public static List<?> getSelectionList(FormField field) {
		Object value = field.getValue();
		if (value instanceof Collection<?>) {
			return CollectionUtil.toList((Collection<?>) value);
		}
		return CollectionUtil.singletonOrEmptyList(value);
	}

	/**
	 * Creates a {@link Renderer} from the
	 * {@link SelectFieldUtils#getOptionLabelProvider(FormField)} of the given field.
	 */
	public static Renderer<Object> getOptionRenderer(FormField field) {
		LabelProvider optionLabels = SelectFieldUtils.getOptionLabelProvider(field);
		ContextMenuProvider contextMenu = SelectFieldUtils.getOptionContextMenu(field);
		return ResourceRenderer.newResourceRenderer(optionLabels, contextMenu);
	}

	/**
	 * Returns if the {@link FormField} allows multiple selection.
	 */
	public static boolean isMultiple(FormField field) {
		if (field instanceof SelectField) {
			return ((SelectField) field).isMultiple();
		}
		return false;
	}

}
