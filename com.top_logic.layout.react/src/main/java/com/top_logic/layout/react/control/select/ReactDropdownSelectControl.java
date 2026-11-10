/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel.SelectOptionsListener;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.identity.ObjectIdIndex;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.scripting.ReactOptionScope;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ObservedObjects;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ReactFormFieldControl} editing a value that is picked from a set of options.
 *
 * <p>
 * One control serves every {@link SelectDisplay shape} the options are offered in - a list that
 * opens on demand and filters as the user types, a cloud of toggles, a bar of segments, or a group
 * of radio buttons or checkboxes. All of them hold the same option index, exchange the same value,
 * and present an option by the same descriptor; they differ in the React component drawing them and in when they are handed the
 * option list: a list that opens on demand fetches it on first open through the
 * {@link #CMD_LOAD_OPTIONS} command, while a shape {@link SelectDisplay#showsAllOptions() showing
 * every option} is handed it right away.
 * </p>
 *
 * <p>
 * Supports single and multi-selection, chip/tag display for selected values, and image-rich option
 * rendering.
 * </p>
 *
 * <p>
 * Extends {@link ReactFormFieldControl} to automatically observe model changes (value, editability,
 * mandatory, visibility, errors) and push incremental patches to the client via SSE.
 * </p>
 *
 * <p>
 * A list that opens on demand can {@link #setFreeEntry(Function) accept free entry}: besides
 * choosing an option, the user enters a text with the {@link EnterTextArguments#COMMAND} command,
 * which becomes the value of the field or a value created from it.
 * </p>
 *
 * <p>
 * A field that only displays its value offers each selected object as a link to the place the
 * application displays it at, so a reference read in a form or a table cell leads to what it refers
 * to.
 * </p>
 */
public class ReactDropdownSelectControl extends ReactFormFieldControl {

	/** The React component drawing a list that opens on demand. */
	private static final String MODULE_DROPDOWN = "TLDropdownSelect";

	/** The React component drawing every option as a toggle of its own. */
	private static final String MODULE_CHIPS = "TLOptionChips";

	/** The React component drawing the options as the segments of one bar. */
	private static final String MODULE_SEGMENTED = "TLSegmentedChoice";

	/** The React component drawing every option as a radio button or a checkbox. */
	private static final String MODULE_RADIO = "TLChoiceGroup";

	// Command names.

	/** The command the client sends to fetch the options when the dropdown opens. */
	public static final String CMD_LOAD_OPTIONS = "loadOptions";

	/**
	 * Key of an option in the headless projection marking a {@link #isMasked(Object) masked} value,
	 * see {@link #scriptingScalarState(ProjectionMode)}.
	 */
	public static final String SCRIPTING_MASKED = "masked";

	/** Command sent when the user follows the link of a displayed option. */
	private static final String CMD_GOTO = "goto";

	/**
	 * Argument of {@link #CMD_GOTO}: the
	 * {@link com.top_logic.layout.react.state.DropdownSelectState.Option#VALUE__PROP} of the option
	 * to display.
	 */
	private static final String ARG_OPTION = "option";

	private final SelectFieldModel _selectModel;

	private final LabelProvider _labelProvider;

	private final Comparator<?> _optionComparator;

	private final boolean _customOrder;

	private final SelectDisplay _display;

	private final Orientation _orientation;

	private boolean _filter = true;

	/**
	 * Converts a text the user entered into the value it stands for, {@code null} while the field
	 * accepts no free entry.
	 *
	 * @see #setFreeEntry(Function)
	 */
	private Function<String, ?> _textToValue;

	/**
	 * The values {@link #_textToValue} created from the texts the user entered into this field, in
	 * the order they were created.
	 *
	 * <p>
	 * They are offered like options while the field holds them, so a text entered once more chooses
	 * the value created the first time instead of creating another one. A value taken out of the
	 * selection again is offered no longer: it was never stored, and the options show the values
	 * the field can be set to, not the typing that led to the current value.
	 * </p>
	 */
	private final List<Object> _enteredValues = new ArrayList<>();

	/**
	 * The ids the options and the elements of the value are displayed under.
	 *
	 * <p>
	 * An option keeps its id while it is an option or an element of the value, across every
	 * {@link #loadOptions() load} of the option list. An option gets its id when it is first
	 * described to the client or addressed by its key, so an option list that is never opened
	 * allocates ids for the value only.
	 * </p>
	 *
	 * <p>
	 * Options are looked up by {@link Object#equals(Object)}, as the field's value is compared with
	 * its options everywhere else: a value that is equal to an option, but not the same instance (a
	 * string read from a channel, a number parsed from a configuration), is the selection of that
	 * option.
	 * </p>
	 */
	private final ObjectIdIndex _optionIds;

	/**
	 * The {@link #objectIdentities() identities} of the options, over {@link #_optionIds}.
	 */
	private final ObjectIdentities _identities = new OptionIdentities();

	/**
	 * Guard flag to suppress the value changed listener when the change originates from the client
	 * via {@link #handleValueChanged(SelectValueArguments)}.
	 */
	private boolean _updatingFromClient;

	/**
	 * The objects the field displays as its value, observed while the field is displayed.
	 *
	 * <p>
	 * A selected object is displayed by its label, so editing that object elsewhere must reach the
	 * field: the observation follows the value and refreshes the descriptors the client shows.
	 * </p>
	 */
	private final ObservedObjects _displayedObjects = new ObservedObjects(event -> refreshDisplay());

	/**
	 * Creates a new {@link ReactDropdownSelectControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The {@link SelectFieldModel} providing value, options, editability, and validation.
	 * @param labelProvider
	 *        Provider for option labels. If the provider also implements
	 *        {@link ResourceProvider}, option images are included.
	 * @param optionComparator
	 *        Comparator for sorting options in the dropdown. Pass {@code null} for natural order.
	 * @param customOrder
	 *        Whether the user can reorder selected values (drag chips to reorder).
	 * @see #ReactDropdownSelectControl(ReactContext, SelectFieldModel, LabelProvider, Comparator,
	 *      boolean, SelectDisplay)
	 */
	public ReactDropdownSelectControl(ReactContext context, SelectFieldModel model,
			LabelProvider labelProvider, Comparator<?> optionComparator, boolean customOrder) {
		this(context, model, labelProvider, optionComparator, customOrder, SelectDisplay.DROPDOWN);
	}

	/**
	 * Creates a {@link ReactDropdownSelectControl} offering its options in the given shape, laid
	 * out one below the other where the shape is {@link SelectDisplay#RADIO}.
	 *
	 * @param display
	 *        The shape the options are offered in, see {@link #getDisplay()}.
	 * @see #ReactDropdownSelectControl(ReactContext, SelectFieldModel, LabelProvider, Comparator,
	 *      boolean, SelectDisplay, Orientation)
	 */
	public ReactDropdownSelectControl(ReactContext context, SelectFieldModel model,
			LabelProvider labelProvider, Comparator<?> optionComparator, boolean customOrder,
			SelectDisplay display) {
		this(context, model, labelProvider, optionComparator, customOrder, display, Orientation.VERTICAL);
	}

	/**
	 * Creates a {@link ReactDropdownSelectControl} offering its options in the given shape and
	 * direction.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The {@link SelectFieldModel} providing value, options, editability, and validation.
	 * @param labelProvider
	 *        Provider for option labels. If the provider also implements
	 *        {@link ResourceProvider}, option images are included.
	 * @param optionComparator
	 *        Comparator for sorting options. Pass {@code null} for natural order.
	 * @param customOrder
	 *        Whether the user can reorder selected values (drag chips to reorder).
	 * @param display
	 *        The shape the options are offered in, see {@link #getDisplay()}.
	 * @param orientation
	 *        The direction the options are laid out in, see {@link #getOrientation()}.
	 */
	public ReactDropdownSelectControl(ReactContext context, SelectFieldModel model,
			LabelProvider labelProvider, Comparator<?> optionComparator, boolean customOrder,
			SelectDisplay display, Orientation orientation) {
		super(context, model, module(display));
		_optionIds = new ObjectIdIndex(context::allocateId);
		_selectModel = model;
		_labelProvider = labelProvider;
		_optionComparator = optionComparator;
		_customOrder = customOrder;
		_display = display;
		_orientation = orientation;
		initSelectState();
		_displayedObjects.observeValue(model.getValue());
		addAttachListener(() -> _displayedObjects.attach(modelScope()));
		addDetachListener(_displayedObjects::detach);

		// The options may be computed from other values the user edits alongside this field.
		SelectOptionsListener optionsListener = (source, newOptions) -> invalidateOptions();
		addAttachListener(() -> _selectModel.addOptionsListener(optionsListener));
		addDetachListener(() -> _selectModel.removeOptionsListener(optionsListener));
	}

	/**
	 * The React component drawing the given shape.
	 */
	private static String module(SelectDisplay display) {
		switch (display) {
			case CHIPS:
				return MODULE_CHIPS;
			case SEGMENTED:
				return MODULE_SEGMENTED;
			case RADIO:
				return MODULE_RADIO;
			default:
				return MODULE_DROPDOWN;
		}
	}

	/**
	 * The shape the options are offered in.
	 */
	public SelectDisplay getDisplay() {
		return _display;
	}

	/**
	 * The direction the options are laid out in: one below the other, or side by side and wrapping
	 * onto further lines.
	 *
	 * <p>
	 * Only the shape {@link SelectDisplay#RADIO} lays its options out in a direction to choose;
	 * the other shapes ignore it.
	 * </p>
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * Whether the list that opens on demand offers an input to filter its options by.
	 *
	 * <p>
	 * Without the input, the open list is operated by the keyboard alone: the arrow keys move
	 * through the options, and typing the beginning of a label jumps to the option it starts. That
	 * suits a short list whose labels the user knows, such as yes and no. Only the shape
	 * {@link SelectDisplay#DROPDOWN} opens a list; the other shapes ignore it.
	 * </p>
	 *
	 * @see #setFilter(boolean)
	 */
	public boolean hasFilter() {
		return _filter;
	}

	/**
	 * Sets whether the list that opens on demand offers an input to filter its options by.
	 *
	 * @see #hasFilter()
	 */
	public void setFilter(boolean filter) {
		_filter = filter;
		putState(DropdownSelectState.NO_FILTER__PROP, Boolean.valueOf(!filter));
	}

	/**
	 * Whether the user may enter a text that is no option, which then becomes the value of the
	 * field.
	 *
	 * @see #setFreeEntry(Function)
	 */
	public boolean hasFreeEntry() {
		return _textToValue != null;
	}

	/**
	 * Sets whether and how the user may enter a text that is no option.
	 *
	 * <p>
	 * With free entry, the user either chooses an option or enters a text, which the client sends
	 * with the {@link EnterTextArguments#COMMAND} command. The entered text is trimmed; an empty
	 * text changes nothing. A text that matches the label of an option, of a value of the field,
	 * or of a value created before from an entered text, ignoring case, chooses that value. Any
	 * other text is converted by the given function, and its result is chosen: it replaces the
	 * value of a field holding one value and is added to the values of a field holding several.
	 * </p>
	 *
	 * <p>
	 * The function may refuse a text by throwing a {@link TopLogicException}, whose message the
	 * user is shown; the value of the field then stays as it is. A {@code null} result leaves the
	 * value unchanged as well.
	 * </p>
	 *
	 * <p>
	 * Only the shape {@link SelectDisplay#DROPDOWN} offers an input to enter a text into; the other
	 * shapes ignore the setting.
	 * </p>
	 *
	 * @param textToValue
	 *        Converts an entered text into the value it stands for, e.g. the text itself for a
	 *        field holding texts, or a new object named by the text. {@code null} to accept only a
	 *        choice among the options.
	 */
	public void setFreeEntry(Function<String, ?> textToValue) {
		_textToValue = textToValue;
		putState(DropdownSelectState.FREE_ENTRY__PROP, Boolean.valueOf(textToValue != null));
	}

	/**
	 * The label of the choice of no value in the given shape.
	 *
	 * <p>
	 * A group of radio buttons offers it as an option of its own, labelled by what it means. The
	 * other shapes show it where the field holds no value, inviting the user to choose one.
	 * </p>
	 */
	private static ResKey emptyOptionLabel(SelectDisplay display) {
		return display == SelectDisplay.RADIO ? I18NConstants.VALUE_NONE : I18NConstants.JS_DROPDOWN_SELECT_EMPTY;
	}

	/**
	 * Re-describes the displayed value after one of the objects it consists of has changed.
	 *
	 * <p>
	 * The option list carries the labels of those objects as well, so it goes stale with them, see
	 * {@link #invalidateOptions()}.
	 * </p>
	 *
	 * <p>
	 * While one of the displayed objects is deleted, the display is left alone: a deleted object has
	 * no type any more, so neither its label nor the color of its value can be resolved from it. The
	 * deletion is delivered before the value of the field is replaced, so the field keeps what it
	 * shows until it receives its next value or is removed from the display.
	 * </p>
	 */
	private void refreshDisplay() {
		for (TLObject displayed : _displayedObjects.observedObjects()) {
			if (!displayed.tValid()) {
				return;
			}
		}
		Object tx = beginUpdate();
		updateValueState();
		invalidateOptions();
		commitUpdate(tx);
	}

	/**
	 * Whether the given value must be shown masked: a business object the current user may not
	 * discover.
	 *
	 * <p>
	 * A field holds the unfiltered value of a reference, so it may contain such objects. Their
	 * identity - label, image, color, link - is not sent to the client: the field shows the
	 * placeholder {@link I18NConstants#MASKED_VALUE} instead, keeps the object at its place in the
	 * value, and writes it back unchanged unless the user removes it.
	 * </p>
	 *
	 * @param option
	 *        The value to check, may be {@code null}.
	 */
	public static boolean isMasked(Object option) {
		return option instanceof TLObject object && !ModelAccessRights.getInstance().isDiscoverAllowed(object);
	}

	/**
	 * Initializes select-specific state that is not covered by
	 * {@link ReactFormFieldControl#ReactFormFieldControl(ReactContext, FieldModel, String)}.
	 */
	private void initSelectState() {
		Resources resources = Resources.getInstance();

		updateValueState();
		putState(DropdownSelectState.MULTI_SELECT__PROP, _selectModel.isMultiple());
		putState(DropdownSelectState.CUSTOM_ORDER__PROP, _customOrder);
		putState(DropdownSelectState.EMPTY_OPTION_LABEL__PROP, resources.getString(emptyOptionLabel(_display)));
		if (_display != SelectDisplay.DROPDOWN) {
			putState(DropdownSelectState.DISPLAY__PROP, _display.getExternalName());
		}
		if (_display == SelectDisplay.RADIO && _orientation == Orientation.HORIZONTAL) {
			putState(DropdownSelectState.ORIENTATION__PROP,
				DropdownSelectState.Orientation.HORIZONTAL.protocolName());
		}
		invalidateOptions();
	}

	/**
	 * Brings the option list the client holds up to date.
	 *
	 * <p>
	 * A display {@link SelectDisplay#showsAllOptions() showing every option} is handed the current
	 * list, having nothing to open at which it could ask for it. A list that opens on demand is
	 * told that what it holds is outdated and asks for the list again the next time it is opened,
	 * so an option list that is expensive to build is built only where it is read.
	 * </p>
	 */
	private void invalidateOptions() {
		if (_display.showsAllOptions()) {
			publishOptions();
		} else {
			setOptionsLoaded(false);
		}
	}

	/**
	 * Sends the current option list to the client, reporting a failure to build it rather than
	 * throwing.
	 *
	 * @return Whether the list could be built.
	 */
	private boolean publishOptions() {
		try {
			loadOptions();
			return true;
		} catch (Exception ex) {
			Logger.error("Failed to load the options of a select field.", ex, this);
			setOptionsLoaded(false);
			return false;
		}
	}

	/**
	 * Builds the option list, indexes it, and sends it to the client together with the value.
	 *
	 * <p>
	 * An option or value element indexed before keeps its id; one that is neither an option nor
	 * an element of the value any more loses it.
	 * </p>
	 */
	private void loadOptions() {
		List<?> options = sortedOptions();
		List<Object> indexed = new ArrayList<>(options);
		indexed.addAll(selection());
		_optionIds.setObjects(indexed);
		List<Map<String, Object>> descriptors = buildOptionDescriptors(options);

		Object tx = beginUpdate();
		putState(DropdownSelectState.OPTIONS__PROP, descriptors);
		setOptionsLoaded(true);
		updateValueState();
		commitUpdate(tx);
	}

	/**
	 * Without free entry, the command entering a text is refused anyway, so it is not offered.
	 */
	@Override
	protected Set<String> scriptingHiddenCommands() {
		if (hasFreeEntry()) {
			return super.scriptingHiddenCommands();
		}
		Set<String> result = new LinkedHashSet<>(super.scriptingHiddenCommands());
		result.add(EnterTextArguments.COMMAND);
		return result;
	}

	private void updateValueState() {
		putState(FieldState.VALUE__PROP, toOptionDescriptors(getSelectionSorted()));
	}

	/**
	 * Projects the options and the value by the identities of the option objects.
	 *
	 * <p>
	 * In the {@link ProjectionMode#AGENT agent projection}, an option is identified by its id and
	 * label, as the client shows it: the id is what {@link #CMD_VALUE_CHANGED} takes. In the
	 * {@link ProjectionMode#KEY key projection}, an option whose object has a
	 * {@link ScriptingModelKey business identity} - built relative to the option scope of this
	 * control - carries that identity as {@code key} instead of its id, in the loaded
	 * {@link DropdownSelectState#OPTIONS__PROP options} as in the {@link FieldState#VALUE__PROP
	 * value}, so an assertion on the value holds in every session; an option without an identity
	 * keeps its id.
	 * </p>
	 *
	 * <p>
	 * A {@link #isMasked(Object) masked} value is projected with its placeholder label and
	 * {@link #SCRIPTING_MASKED}: it discloses no identity. The agent projection keeps its id, which
	 * says nothing about the object but lets an agent keep the value in the field when it sets the
	 * value; the key projection names nothing but the placeholder, so an assertion on it holds in
	 * every session.
	 * </p>
	 */
	@Override
	public Map<String, Object> scriptingScalarState(ProjectionMode mode) {
		Map<String, Object> result = super.scriptingScalarState(mode);
		Object scope = null;
		for (String property : List.of(DropdownSelectState.OPTIONS__PROP, FieldState.VALUE__PROP)) {
			if (result.get(property) instanceof List<?> list && !list.isEmpty()) {
				if (scope == null) {
					scope = _identities.keyContext();
				}
				result.put(property, identifiedOptions(list, mode, scope));
			}
		}
		return result;
	}

	/**
	 * The given option descriptors, each identifying its option as the given mode requires, see
	 * {@link #scriptingScalarState(ProjectionMode)}.
	 */
	@SuppressWarnings("unchecked")
	private List<Object> identifiedOptions(List<?> descriptors, ProjectionMode mode, Object scope) {
		List<Object> result = new ArrayList<>(descriptors.size());
		for (Object entry : descriptors) {
			if (entry instanceof Map<?, ?> descriptor) {
				Map<String, Object> projected = new LinkedHashMap<>((Map<String, Object>) descriptor);
				Object id = descriptor.get(DropdownSelectState.Option.VALUE__PROP);
				Object option = id instanceof String optionId ? _optionIds.objectOf(optionId) : null;
				if (option != null && isMasked(option)) {
					// Never the identity of an object the user may not see. Its id, which holds in this
					// session only, is kept where the projection identifies options by their ids.
					if (!mode.projectsIds(_identities)) {
						projected.remove(DropdownSelectState.Option.VALUE__PROP);
					}
					projected.put(SCRIPTING_MASKED, Boolean.TRUE);
				} else if (option != null) {
					ScriptingModelKey.putIdentity(projected, mode, _identities, scope, option,
						DropdownSelectState.Option.VALUE__PROP, id);
				}
				result.add(projected);
			} else {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * The shape the options are offered in is how the field looks, not what it says: every shape
	 * reports the same options and the same value.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DropdownSelectState.DISPLAY__PROP,
			DropdownSelectState.ORIENTATION__PROP, DropdownSelectState.NO_FILTER__PROP);
	}

	private void setOptionsLoaded(boolean loaded) {
		putState(DropdownSelectState.OPTIONS_LOADED__PROP, loaded);
	}

	/**
	 * Overrides the default value change handling from {@link ReactFormFieldControl} to send full
	 * option descriptors (with label and image) instead of raw field values.
	 *
	 * <p>
	 * A value set from elsewhere - the form being reset, stored, or switched to another object -
	 * ends what the user entered: the values created from the entered texts belong to the edit
	 * that is over and are no longer offered.
	 * </p>
	 */
	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		// The objects displayed are the ones the value now names, whoever set it.
		_displayedObjects.observeValue(newValue);
		if (!_updatingFromClient) {
			_enteredValues.clear();
			updateValueState();
			// The option list carries the labels of the value as well, so it goes stale with it.
			invalidateOptions();
		}
	}

	@Override
	protected String formatElement(Object value) {
		return value == null ? null : _labelProvider.getLabel(value);
	}

	/**
	 * Shows the given placeholder where the field holds no value, in place of the label inviting
	 * the user to choose one.
	 *
	 * <p>
	 * A group of radio buttons offers the choice of no value as an option of its own; while a
	 * placeholder is set, that option is labelled with the inherited value it stands for.
	 * </p>
	 */
	@Override
	public void setPlaceholder(String placeholder) {
		super.setPlaceholder(placeholder);
		Resources resources = Resources.getInstance();
		String emptyLabel;
		if (placeholder == null) {
			emptyLabel = resources.getString(emptyOptionLabel(_display));
		} else if (_display == SelectDisplay.RADIO) {
			emptyLabel = resources.getString(I18NConstants.VALUE_INHERITED__VALUE.fill(placeholder));
		} else {
			emptyLabel = placeholder;
		}
		putState(DropdownSelectState.EMPTY_OPTION_LABEL__PROP, emptyLabel);
	}

	/**
	 * Handles the {@link #CMD_LOAD_OPTIONS} command from the React client.
	 *
	 * <p>
	 * Sends the full option list as a state patch via SSE. Builds an internal index mapping
	 * allocated IDs to model objects for later resolution.
	 * </p>
	 */
	@ReactCommandHandler(CMD_LOAD_OPTIONS)
	void handleLoadOptions() {
		if (!publishOptions()) {
			throw new TopLogicException(I18NConstants.JS_DROPDOWN_SELECT_ERROR);
		}
	}

	/**
	 * Handles the {@link #CMD_VALUE_CHANGED} command: sets the value to the options with the given
	 * ids.
	 *
	 * <p>
	 * Like every value arriving from a client, the selection is dropped unless the field
	 * {@link #acceptsClientValue() accepts client values}. An id the control displays no option
	 * under is ignored.
	 * </p>
	 *
	 * <p>
	 * The ids are {@link com.top_logic.layout.react.control.identity.DisplayedObjectId object ids}:
	 * a recorded step carries the keys of the options and replays in a later session, through the
	 * {@link #objectIdentities() identities} of the options.
	 * </p>
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleValueChanged(SelectValueArguments arguments) {
		if (!acceptsClientValue()) {
			return;
		}
		List<Object> newSelection = resolveOptions(arguments.getValue());
		_updatingFromClient = true;
		try {
			_selectModel.setValue(newSelection);
		} finally {
			_updatingFromClient = false;
		}

		// Update value state with full descriptors for chip display.
		updateValueState();
	}

	private List<Object> resolveOptions(List<String> ids) {
		List<Object> resolved = new ArrayList<>(ids.size());
		for (String id : ids) {
			Object option = _optionIds.objectOf(id);
			if (option != null) {
				resolved.add(option);
			}
		}
		return resolved;
	}

	/**
	 * Handles the {@link EnterTextArguments#COMMAND} command: chooses the value the text the user
	 * entered stands for, see {@link #setFreeEntry(Function)}.
	 *
	 * <p>
	 * Like every value arriving from a client, the text is dropped unless the field
	 * {@link #acceptsClientValue() accepts client values}. A field without free entry refuses it.
	 * </p>
	 *
	 * <p>
	 * The text names the value itself, so the command is recorded as it was dispatched and replays
	 * in a later session: there, the text chooses the value of that label, or creates it again.
	 * </p>
	 */
	@ReactCommandHandler(EnterTextArguments.COMMAND)
	void handleEnterText(EnterTextArguments arguments) {
		if (!acceptsClientValue()) {
			return;
		}
		if (!hasFreeEntry()) {
			throw new TopLogicException(I18NConstants.ERROR_NO_FREE_ENTRY);
		}
		String text = arguments.getText() == null ? "" : arguments.getText().trim();
		if (text.isEmpty()) {
			return;
		}

		Object value = findByLabel(text);
		if (value == null) {
			value = _textToValue.apply(text);
			if (value == null) {
				return;
			}
			_enteredValues.add(value);
		}

		List<Object> newSelection;
		if (_selectModel.isMultiple()) {
			newSelection = new ArrayList<>(selection());
			if (newSelection.contains(value)) {
				return;
			}
			newSelection.add(value);
		} else {
			newSelection = Collections.singletonList(value);
		}
		_updatingFromClient = true;
		try {
			_selectModel.setValue(newSelection);
		} finally {
			_updatingFromClient = false;
		}
		updateValueState();
		// A value created from the text is offered from now on, which the list held by the client
		// does not know yet.
		invalidateOptions();
	}

	/**
	 * The value whose label is the given text, ignoring case: an option, a value of the field, or
	 * a value created before from an entered text. {@code null}, if there is none.
	 *
	 * <p>
	 * A {@link #isMasked(Object) masked} value is no match: its label is not disclosed, neither by
	 * displaying nor by matching it.
	 * </p>
	 */
	private Object findByLabel(String text) {
		for (Object candidate : optionsAndValue()) {
			if (candidate == null || isMasked(candidate)) {
				continue;
			}
			String label = _labelProvider.getLabel(candidate);
			if (label != null && label.trim().equalsIgnoreCase(text)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * The options and the elements of the value, by the ids the control displays them under.
	 *
	 * <p>
	 * The keys of the options are built relative to the {@link ReactOptionScope option scope} of
	 * the control: its {@link #offeredOptions() options} and the elements of its value, named by
	 * the labels the control shows. A key resolves among all of them - not only among the options
	 * the client has loaded -, so a recorded value change replays without the option list being
	 * opened first. A {@link #isMasked(Object) masked} element of the value belongs to the scope as
	 * well, so a replayed value keeps it.
	 * </p>
	 */
	@Override
	public ObjectIdentities objectIdentities() {
		return _identities;
	}

	/**
	 * {@link ObjectIdentities} of the options over {@link ReactDropdownSelectControl#_optionIds}.
	 */
	private final class OptionIdentities implements ObjectIdentities {

		@Override
		public Object objectOf(String id) {
			return _optionIds.objectOf(id);
		}

		/**
		 * The id of the given option, allocated when the option is first addressed.
		 */
		@Override
		public String idOf(Object object) {
			String id = _optionIds.idOf(object);
			if (id == null && object != null && optionsAndValue().contains(object)) {
				id = _optionIds.add(object);
			}
			return id;
		}

		@Override
		public ReactOptionScope keyContext() {
			return new ReactOptionScope(optionsAndValue(), _labelProvider);
		}

		/**
		 * Resolves a {@link ContextDependent context-relative} key within the option scope and any
		 * other key globally, and accepts only an object of the option scope.
		 */
		@Override
		public String idOfKey(ModelName key) {
			ReactOptionScope scope = keyContext();
			Object option = ScriptingModelKey.locate(key instanceof ContextDependent ? scope : null, key);
			if (option == null || !scope.options().contains(option)) {
				return null;
			}
			return _optionIds.add(option);
		}
	}

	/**
	 * The {@link #offeredOptions() offered options} followed by the elements of the value of the
	 * field that are none of them (e.g. {@link #isMasked(Object) masked} values, which no option
	 * list contains).
	 */
	private List<Object> optionsAndValue() {
		List<Object> result = offeredOptions();
		Set<Object> contained = new HashSet<>(result);
		for (Object element : getSelectionSorted()) {
			if (contained.add(element)) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * The options of the field followed by the {@link #_enteredValues values created from entered
	 * texts} that are no options and are still selected.
	 */
	private List<Object> offeredOptions() {
		List<Object> result = new ArrayList<>(_selectModel.getOptions());
		if (!_enteredValues.isEmpty()) {
			_enteredValues.retainAll(selection());
			Set<Object> contained = new HashSet<>(result);
			for (Object entered : _enteredValues) {
				if (contained.add(entered)) {
					result.add(entered);
				}
			}
		}
		return result;
	}

	/**
	 * Builds the descriptors of the given options, each under its id in {@link #_optionIds}.
	 */
	private List<Map<String, Object>> buildOptionDescriptors(List<?> options) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		ResourceProvider resourceProvider = toResourceProvider(_labelProvider);

		for (Object option : options) {
			descriptors.add(describe(_optionIds.add(option), option, resourceProvider));
		}
		return descriptors;
	}

	/**
	 * Converts a list of selected options to descriptors, each under its id in
	 * {@link #_optionIds}, indexing an option that has none yet.
	 *
	 * <p>
	 * These are the options the field displays as its value, so they are the ones that carry
	 * {@link com.top_logic.layout.react.state.DropdownSelectState.Option#LINK__PROP}.
	 * </p>
	 */
	private List<Map<String, Object>> toOptionDescriptors(List<?> options) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		ResourceProvider resourceProvider = toResourceProvider(_labelProvider);
		ObjectNavigator navigator = _selectModel.isEditable() ? null : navigator();

		for (Object option : options) {
			Map<String, Object> descriptor = describe(_optionIds.add(option), option, resourceProvider);
			if (navigator != null && !isMasked(option) && navigator.canShow(option)) {
				descriptor.put(DropdownSelectState.Option.LINK__PROP, Boolean.TRUE);
			}
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * The descriptor telling the client what one option looks like: its id, its label, the image it
	 * is presented with, and the color it is displayed in.
	 *
	 * <p>
	 * Every option and every selected value reaches the client through here - the option list, the
	 * chips of the selection while editing, and the read-only display - so the presentation of an
	 * option is decided in one place.
	 * </p>
	 *
	 * <p>
	 * A {@link #isMasked(Object) masked} option is described by its id and the placeholder label
	 * only.
	 * </p>
	 *
	 * @param id
	 *        The id the option is addressed by, from {@link #_optionIds}.
	 * @param option
	 *        The option to describe.
	 * @param resourceProvider
	 *        The provider answering the option's image, or <code>null</code> if the label provider
	 *        answers no images.
	 * @return The descriptor sent to the client.
	 */
	private Map<String, Object> describe(String id, Object option, ResourceProvider resourceProvider) {
		Map<String, Object> descriptor = new HashMap<>();
		descriptor.put(DropdownSelectState.Option.VALUE__PROP, id);
		if (isMasked(option)) {
			// Only the opaque id, so the masked value is written back unchanged.
			descriptor.put(DropdownSelectState.Option.LABEL__PROP,
				Resources.getInstance().getString(I18NConstants.MASKED_VALUE));
			return descriptor;
		}
		descriptor.put(DropdownSelectState.Option.LABEL__PROP, _labelProvider.getLabel(option));

		if (resourceProvider != null) {
			putImage(descriptor, resourceProvider.getImage(option, Flavor.DEFAULT));
		}
		ReactValueColor.putRole(descriptor, option);

		return descriptor;
	}

	private ObjectNavigator navigator() {
		ReactContext context = getReactContext();
		return context == null ? null : context.getObjectNavigator();
	}

	/**
	 * Following the link of a displayed value is offered also while the field takes no input: a
	 * read-only field displays its value with links, see {@link #toOptionDescriptors(List)}.
	 */
	@Override
	protected Set<String> scriptingDisplayCommands() {
		return Set.of(CMD_GOTO);
	}

	/**
	 * The {@link #CMD_GOTO} is executable while a displayed value carries a link.
	 */
	@Override
	public ExecutableState scriptingCommandState(String command) {
		if (CMD_GOTO.equals(command) && !displaysLink()) {
			return ExecutableState.NOT_EXEC_DISABLED;
		}
		return super.scriptingCommandState(command);
	}

	/**
	 * Whether a displayed value carries a link, see {@link #toOptionDescriptors(List)}.
	 */
	private boolean displaysLink() {
		if (_selectModel.isEditable()) {
			return false;
		}
		ObjectNavigator navigator = navigator();
		if (navigator == null) {
			return false;
		}
		for (Object option : selection()) {
			if (!isMasked(option) && navigator.canShow(option)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Displays the option the user followed the link of, where the application displays objects of
	 * its type.
	 */
	@ReactCommandHandler(value = CMD_GOTO, params = @ReactParam(name = ARG_OPTION, required = true,
		description = "Id of the displayed option to lead to (from the value descriptors)."))
	void handleGoto(ReactContext context, Map<String, Object> arguments) {
		Object option = arguments.get(ARG_OPTION) instanceof String id ? _optionIds.objectOf(id) : null;
		ObjectNavigator navigator = navigator();
		if (option != null && navigator != null && !isMasked(option) && navigator.canShow(option)) {
			navigator.show(context, option);
		}
	}

	/**
	 * Returns the current selection as a sorted list. If {@link #_customOrder} is enabled, the
	 * selection order is preserved. Otherwise it is sorted by {@link #_optionComparator}, with the
	 * {@link #isMasked(Object) masked} values appended in their stored order: sorting them by their
	 * labels would disclose these labels by their position.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> getSelectionSorted() {
		List<?> selection = selection();
		if (_customOrder || selection.size() <= 1 || _optionComparator == null) {
			return selection;
		}
		List<Object> visible = new ArrayList<>(selection.size());
		List<Object> masked = new ArrayList<>();
		for (Object element : selection) {
			(isMasked(element) ? masked : visible).add(element);
		}
		visible.sort((Comparator) _optionComparator);
		visible.addAll(masked);
		return visible;
	}

	/**
	 * The current selection in the order the model holds it.
	 */
	private List<?> selection() {
		Object value = _selectModel.getValue();
		if (!(value instanceof List)) {
			return value != null ? Collections.singletonList(value) : Collections.emptyList();
		}
		return (List<?>) value;
	}

	/**
	 * Returns the {@link #offeredOptions() offered options} sorted by the comparator. Custom order
	 * only affects selection order, not option display order.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> sortedOptions() {
		List<?> options = offeredOptions();
		if (options.size() <= 1 || _optionComparator == null) {
			return options;
		}
		Object[] array = options.toArray();
		Arrays.sort(array, (Comparator) _optionComparator);
		return Arrays.asList(array);
	}

	/**
	 * Names the image of an option in its descriptor, unless the option has none.
	 *
	 * <p>
	 * The image is resolved first, so a reference - the icon a theme configures for a type, say -
	 * reaches the client as the image it stands for rather than as the reference, which the client
	 * has no theme to look up, and an image file as the URL it is served at, see
	 * {@link ReactImages}. An invisible image is left out altogether: an option with no icon is
	 * one whose descriptor names none, not one naming an icon that draws nothing but still takes
	 * the width of one.
	 * </p>
	 */
	private void putImage(Map<String, Object> descriptor, ThemeImage image) {
		if (image == null || image == ThemeImage.none()) {
			return;
		}
		descriptor.put(DropdownSelectState.Option.IMAGE__PROP, ReactImages.encode(getReactContext(), image));
	}

	private ResourceProvider toResourceProvider(LabelProvider labelProvider) {
		if (labelProvider instanceof ResourceProvider) {
			return (ResourceProvider) labelProvider;
		}
		return null;
	}
}
