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
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.AgentModelKey;
import com.top_logic.layout.react.scripting.ReactActionContext;
import com.top_logic.layout.react.scripting.ReactOptionScope;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ReactFormFieldControl} that renders a dropdown select with search-as-you-type filtering
 * via the {@code TLDropdownSelect} React component.
 *
 * <p>
 * Supports single and multi-selection, chip/tag display for selected values, and image-rich option
 * rendering. Options are loaded lazily on first dropdown open via the {@code loadOptions} command.
 * </p>
 *
 * <p>
 * Extends {@link ReactFormFieldControl} to automatically observe model changes (value, editability,
 * mandatory, visibility, errors) and push incremental patches to the client via SSE.
 * </p>
 */
public class ReactDropdownSelectControl extends ReactFormFieldControl {

	private static final String OPTIONS = "options";

	private static final String OPTIONS_LOADED = "optionsLoaded";

	private static final String CUSTOM_ORDER = "customOrder";

	private static final String MULTI_SELECT = "multiSelect";

	private static final String EMPTY_OPTION_LABEL = "emptyOptionLabel";

	private static final String OPT_VALUE = "value";

	private static final String OPT_LABEL = "label";

	private static final String OPT_IMAGE = "image";

	// Command names.
	private static final String CMD_LOAD_OPTIONS = "loadOptions";

	private static final String CMD_SELECT_BY_KEY = "selectByKey";

	/** Command argument carrying the list of option business keys for {@link #CMD_SELECT_BY_KEY}. */
	private static final String ARG_KEYS = "keys";

	private final SelectFieldModel _selectModel;

	private final LabelProvider _labelProvider;

	private final Comparator<?> _optionComparator;

	private final boolean _customOrder;

	/**
	 * Maps option ID strings to the original option objects. Used by
	 * {@link #handleValueChanged(Map)} to resolve client-sent IDs back to model objects. Populated
	 * by {@link #handleLoadOptions()} and incrementally by {@link #toOptionDescriptors(List)} when
	 * fresh IDs are allocated before the full option list has been loaded.
	 */
	private Map<String, Object> _optionIndex = new HashMap<>();

	/**
	 * Maps option objects to their allocated IDs. Built alongside {@link #_optionIndex} so that
	 * {@link #toOptionDescriptors(List)} produces IDs consistent with the option index.
	 */
	private Map<Object, String> _optionIdByObject = new IdentityHashMap<>();

	/**
	 * Guard flag to suppress the value changed listener when the change originates from the client
	 * via {@link #handleValueChanged(Map)}.
	 */
	private boolean _updatingFromClient;

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
	 */
	public ReactDropdownSelectControl(ReactContext context, SelectFieldModel model,
			LabelProvider labelProvider, Comparator<?> optionComparator, boolean customOrder) {
		super(context, model, "TLDropdownSelect");
		_selectModel = model;
		_labelProvider = labelProvider;
		_optionComparator = optionComparator;
		_customOrder = customOrder;
		initSelectState();
	}

	/**
	 * Initializes select-specific state that is not covered by
	 * {@link ReactFormFieldControl#ReactFormFieldControl(ReactContext, FieldModel, String)}.
	 */
	private void initSelectState() {
		Resources resources = Resources.getInstance();

		updateValueState();
		putState(MULTI_SELECT, _selectModel.isMultiple());
		putState(CUSTOM_ORDER, _customOrder);
		putState(EMPTY_OPTION_LABEL, resources.getString(I18NConstants.JS_DROPDOWN_SELECT_EMPTY));
		setOptionsLoaded(false);
	}

	private void updateValueState() {
		putState(VALUE, toOptionDescriptors(getSelectionSorted()));
	}

	/**
	 * Augments the agent projection of the loaded options with a stable {@code key} (the option
	 * object's {@link AgentModelKey ModelName}), so an agent can select a member by business object
	 * rather than by the session-allocated option id.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> agentScalarState() {
		Map<String, Object> result = super.agentScalarState();
		Object options = result.get(OPTIONS);
		if (options instanceof List<?> list && !list.isEmpty()) {
			ReactOptionScope scope = new ReactOptionScope(new ArrayList<>(_optionIndex.values()), _labelProvider);
			List<Object> withKeys = new ArrayList<>(list.size());
			for (Object entry : list) {
				if (entry instanceof Map<?, ?> descriptor) {
					Map<String, Object> augmented = new LinkedHashMap<>((Map<String, Object>) descriptor);
					String key = AgentModelKey.toJson(scope, _optionIndex.get(descriptor.get(OPT_VALUE)));
					if (key != null) {
						augmented.put("key", key);
					}
					withKeys.add(augmented);
				} else {
					withKeys.add(entry);
				}
			}
			result.put(OPTIONS, withKeys);
		}
		return result;
	}

	private void setOptionsLoaded(boolean loaded) {
		putState(OPTIONS_LOADED, loaded);
	}

	/**
	 * Overrides the default value change handling from {@link ReactFormFieldControl} to send full
	 * option descriptors (with label and image) instead of raw field values.
	 */
	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		if (!_updatingFromClient) {
			updateValueState();
			// Invalidate cached options so the client reloads them on next open.
			setOptionsLoaded(false);
		}
	}

	/**
	 * Handles the {@code loadOptions} command from the React client.
	 *
	 * <p>
	 * Sends the full option list as a state patch via SSE. Builds an internal index mapping
	 * allocated IDs to model objects for later resolution.
	 * </p>
	 */
	@ReactCommand(CMD_LOAD_OPTIONS)
	HandlerResult handleLoadOptions() {
		try {
			List<?> options = sortedOptions();
			Map<String, Object> newIndex = new HashMap<>();
			Map<Object, String> newReverse = new IdentityHashMap<>();
			List<Map<String, Object>> descriptors = buildOptionDescriptors(options, newIndex, newReverse);
			_optionIndex = newIndex;
			_optionIdByObject = newReverse;

			Object tx = beginUpdate();
			putState(OPTIONS, descriptors);
			setOptionsLoaded(true);
			// Re-send value with IDs consistent with the new option index.
			updateValueState();
			commitUpdate(tx);
		} catch (Exception ex) {
			Logger.error("Failed to load options for dropdown select control.", ex, this);
			return HandlerResult.error(I18NConstants.JS_DROPDOWN_SELECT_ERROR);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles the {@link #CMD_VALUE_CHANGED} command from the React client.
	 *
	 * @param arguments
	 *        Must contain a {@link #VALUE} entry with a list of option value IDs.
	 */
	// Argument is a string array (selected option value ids). The config-JSON binding does not
	// support a List of primitives (the reader expects list elements to be objects), so this command
	// keeps a raw Map with a lightweight @ReactParam schema rather than a typed ConfigurationItem.
	@SuppressWarnings("unchecked")
	@ReactCommand(value = CMD_VALUE_CHANGED, params = @ReactParam(name = VALUE, type = "string[]",
		required = true, description = "List of selected option value ids (from the options descriptors)."))
	HandlerResult handleValueChanged(Map<String, Object> arguments) {
		List<String> selectedIds = (List<String>) arguments.get(VALUE);
		if (selectedIds == null) {
			selectedIds = Collections.emptyList();
		}

		List<Object> newSelection = resolveOptions(selectedIds);
		_updatingFromClient = true;
		try {
			_selectModel.setValue(newSelection);
		} finally {
			_updatingFromClient = false;
		}

		// Update value state with full descriptors for chip display.
		updateValueState();

		return HandlerResult.DEFAULT_RESULT;
	}

	private List<Object> resolveOptions(List<String> ids) {
		List<Object> resolved = new ArrayList<>(ids.size());
		for (String id : ids) {
			Object option = _optionIndex.get(id);
			if (option != null) {
				resolved.add(option);
			}
		}
		return resolved;
	}

	/**
	 * Handles the {@link #CMD_SELECT_BY_KEY} command: sets the selection to the options designated by
	 * their stable business {@link AgentModelKey key}s — the round-trip inverse of the {@code key} that
	 * {@link #agentScalarState()} projects onto each option.
	 *
	 * <p>
	 * This lets a headless agent select members by business object identity (e.g. the group labeled
	 * {@code securityOwner} within this control) instead of session-allocated option ids that do not
	 * survive a reload or replay. Each key is parsed back to a {@link ModelName} and resolved against
	 * this control's {@link ReactOptionScope option scope} (for context-relative names) or globally.
	 * Keys that do not resolve are skipped.
	 * </p>
	 *
	 * @param arguments
	 *        Must contain a {@link #ARG_KEYS} entry with a list of key JSON strings.
	 */
	// Argument is a string array (business keys); see the note on handleValueChanged — a List of
	// primitives is not supported by the config-JSON binding, so this stays a raw Map.
	@SuppressWarnings("unchecked")
	@ReactCommand(value = CMD_SELECT_BY_KEY, params = @ReactParam(name = ARG_KEYS, type = "string[]",
		required = true,
		description = "List of option business keys (the 'key' projected onto each option)."))
	HandlerResult handleSelectByKey(Map<String, Object> arguments) {
		List<String> keys = (List<String>) arguments.get(ARG_KEYS);
		if (keys == null) {
			keys = Collections.emptyList();
		}

		List<String> unresolved = new ArrayList<>();
		List<Object> newSelection = resolveByKeys(keys, unresolved);
		if (!unresolved.isEmpty()) {
			// Drift contract: a recorded key that no longer designates an option in this session is an
			// explicit failure, never a silent partial/empty selection. Replay then reports
			// success:false (a real regression); an agent, which re-observes each step, re-plans.
			return HandlerResult.error(I18NConstants.ERROR_OPTION_KEYS_UNRESOLVED__KEYS.fill(unresolved));
		}
		_updatingFromClient = true;
		try {
			_selectModel.setValue(newSelection);
		} finally {
			_updatingFromClient = false;
		}
		updateValueState();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Records a value change in replay-stable form: the live {@link #CMD_VALUE_CHANGED} carries
	 * session-allocated option ids, so it is recorded as a {@link #CMD_SELECT_BY_KEY} of the selected
	 * options' business keys, which resolve again in a later session.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_VALUE_CHANGED.equals(command) && arguments != null) {
			List<String> ids = (List<String>) arguments.get(VALUE);
			if (ids != null) {
				ReactOptionScope scope =
					new ReactOptionScope(new ArrayList<>(_selectModel.getOptions()), _labelProvider);
				List<String> keys = new ArrayList<>(ids.size());
				for (String id : ids) {
					Object option = _optionIndex.get(id);
					String key = option == null ? null : AgentModelKey.toJson(scope, option);
					if (key != null) {
						keys.add(key);
					}
				}
				return new RecordedCommand(CMD_SELECT_BY_KEY, Map.of(ARG_KEYS, keys));
			}
		}
		return super.recordCommand(command, arguments);
	}

	/**
	 * Resolves each key to its option, collecting the keys that no longer resolve in
	 * {@code unresolvedOut}.
	 *
	 * <p>
	 * Resolution is against the model's authoritative option list, not only the options already
	 * streamed to the client ({@code _optionIndex}). This lets a recorded {@link #CMD_SELECT_BY_KEY}
	 * replay without first opening the dropdown ({@link #CMD_LOAD_OPTIONS}), and matches the full set
	 * the keys were built against. A key that fails to parse, throws, or resolves to nothing is
	 * reported as unresolved rather than skipped.
	 * </p>
	 */
	private List<Object> resolveByKeys(List<String> keys, List<String> unresolvedOut) {
		ReactOptionScope scope = new ReactOptionScope(new ArrayList<>(_selectModel.getOptions()), _labelProvider);
		ActionContext actionContext = newActionContext();
		List<Object> resolved = new ArrayList<>(keys.size());
		for (String key : keys) {
			ModelName name = AgentModelKey.fromJson(key);
			Object option = null;
			if (name != null) {
				// Context-relative names (ContextDependent) resolve within this control's option scope;
				// globally-named options (e.g. a person) resolve without a value context.
				Object valueContext = name instanceof ContextDependent ? scope : null;
				try {
					option = ModelResolver.locateModel(actionContext, valueContext, name);
				} catch (RuntimeException ex) {
					Logger.warn("Cannot resolve option for key: " + key, ex, this);
				}
			}
			if (option != null) {
				resolved.add(option);
			} else {
				unresolvedOut.add(key);
			}
		}
		return resolved;
	}

	private static ActionContext newActionContext() {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		return new ReactActionContext(displayContext, displayContext.asRequest().getSession());
	}

	/**
	 * Builds option descriptors and populates the given index and reverse maps. Each option
	 * receives a unique ID from the {@link ReactContext}.
	 */
	private List<Map<String, Object>> buildOptionDescriptors(List<?> options,
			Map<String, Object> index, Map<Object, String> reverse) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		ResourceProvider resourceProvider = toResourceProvider(_labelProvider);

		for (Object option : options) {
			String id = getReactContext().allocateId();
			index.put(id, option);
			reverse.put(option, id);

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, _labelProvider.getLabel(option));

			if (resourceProvider != null) {
				ThemeImage image = resourceProvider.getImage(option, Flavor.DEFAULT);
				if (image != null) {
					descriptor.put(OPT_IMAGE, image.toEncodedForm());
				}
			}

			descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * Converts a list of selected options to descriptors. Uses the existing
	 * {@link #_optionIdByObject} map for IDs when available (after {@code loadOptions}), falling
	 * back to freshly allocated IDs for the initial render.
	 */
	private List<Map<String, Object>> toOptionDescriptors(List<?> options) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		ResourceProvider resourceProvider = toResourceProvider(_labelProvider);

		for (Object option : options) {
			String id = _optionIdByObject.get(option);
			if (id == null) {
				id = getReactContext().allocateId();
				_optionIndex.put(id, option);
				_optionIdByObject.put(option, id);
			}

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, _labelProvider.getLabel(option));

			if (resourceProvider != null) {
				ThemeImage image = resourceProvider.getImage(option, Flavor.DEFAULT);
				if (image != null) {
					descriptor.put(OPT_IMAGE, image.toEncodedForm());
				}
			}

			descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * Returns the current selection as a sorted list. If {@link #_customOrder} is enabled, the
	 * selection order is preserved. Otherwise it is sorted by {@link #_optionComparator}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> getSelectionSorted() {
		Object value = _selectModel.getValue();
		if (!(value instanceof List)) {
			return value != null ? Collections.singletonList(value) : Collections.emptyList();
		}
		List<?> selection = (List<?>) value;
		if (_customOrder || selection.size() <= 1 || _optionComparator == null) {
			return selection;
		}
		Object[] array = selection.toArray();
		Arrays.sort(array, (Comparator) _optionComparator);
		return Arrays.asList(array);
	}

	/**
	 * Returns the option list sorted by the comparator. Custom order only affects selection order,
	 * not option display order.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> sortedOptions() {
		List<?> options = _selectModel.getOptions();
		if (options.size() <= 1 || _optionComparator == null) {
			return options;
		}
		Object[] array = options.toArray();
		Arrays.sort(array, (Comparator) _optionComparator);
		return Arrays.asList(array);
	}

	private ResourceProvider toResourceProvider(LabelProvider labelProvider) {
		if (labelProvider instanceof ResourceProvider) {
			return (ResourceProvider) labelProvider;
		}
		return null;
	}
}
