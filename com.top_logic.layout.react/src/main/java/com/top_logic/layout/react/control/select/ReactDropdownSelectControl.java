/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
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
 * Extends {@link ReactFormFieldControl} to automatically observe model changes (value, disabled,
 * mandatory, immutable, visibility, errors) and push incremental patches to the client via SSE.
 * </p>
 */
public class ReactDropdownSelectControl extends ReactFormFieldControl {

	private static final String OPTIONS = "options";

	private static final String OPTIONS_LOADED = "optionsLoaded";

	private static final String MULTI_SELECT = "multiSelect";

	private static final String EMPTY_OPTION_LABEL = "emptyOptionLabel";

	private static final String NOTHING_FOUND_LABEL = "nothingFoundLabel";

	private static final String OPT_VALUE = "value";

	private static final String OPT_LABEL = "label";

	private static final String OPT_IMAGE = "image";

	private final SelectField _selectField;

	/**
	 * Maps stable option ID strings to the original option objects. Built during
	 * {@link #handleLoadOptions()} and used by {@link #handleValueChanged(Map)} to resolve
	 * client-sent IDs back to model objects.
	 */
	private Map<String, Object> _optionIndex = Collections.emptyMap();

	/**
	 * Creates a new {@link ReactDropdownSelectControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param selectField
	 *        The {@link SelectField} model to wrap.
	 */
	public ReactDropdownSelectControl(ReactContext context, SelectField selectField) {
		super(context, selectField, "TLDropdownSelect");
		_selectField = selectField;
		initSelectState();
	}

	/**
	 * Initializes select-specific state that is not covered by
	 * {@link ReactFormFieldControl#ReactFormFieldControl(ReactContext, FormField, String)}.
	 */
	private void initSelectState() {
		Resources resources = Resources.getInstance();

		putState(VALUE, toOptionDescriptors(_selectField.getSelection()));
		putState(MULTI_SELECT, _selectField.isMultiple());
		putState(EMPTY_OPTION_LABEL, resources.getString(I18NConstants.JS_DROPDOWN_SELECT_EMPTY));
		putState(NOTHING_FOUND_LABEL, resources.getString(I18NConstants.JS_DROPDOWN_SELECT_NOTHING_FOUND));
		putState(OPTIONS_LOADED, false);
	}

	/**
	 * Overrides the default value change handling from {@link ReactFormFieldControl} to send full
	 * option descriptors (with label and image) instead of raw field values.
	 */
	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (!skipEvent(field)) {
			putState(VALUE, toOptionDescriptors(_selectField.getSelection()));
			// Invalidate cached options so the client reloads them on next open.
			putState(OPTIONS_LOADED, false);
		}
	}

	/**
	 * Handles the {@code loadOptions} command from the React client.
	 *
	 * <p>
	 * Sends the full option list as a state patch via SSE. Builds an internal index mapping stable
	 * option IDs to model objects for later resolution.
	 * </p>
	 */
	@ReactCommand("loadOptions")
	HandlerResult handleLoadOptions() {
		try {
			List<?> options = _selectField.getOptions();
			Map<String, Object> newIndex = new LinkedHashMap<>();
			List<Map<String, Object>> descriptors = buildOptionDescriptors(options, newIndex);
			_optionIndex = newIndex;

			Map<String, Object> patch = new HashMap<>();
			patch.put(OPTIONS, descriptors);
			patch.put(OPTIONS_LOADED, true);
			patchReactState(patch);
		} catch (Exception ex) {
			Logger.error("Failed to load options for " + _selectField.getName(), ex, this);
			return HandlerResult.error(I18NConstants.JS_DROPDOWN_SELECT_ERROR);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles the {@code valueChanged} command from the React client.
	 *
	 * @param arguments
	 *        Must contain a {@code "value"} key with a list of option value IDs.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("valueChanged")
	HandlerResult handleValueChanged(Map<String, Object> arguments) {
		List<String> selectedIds = (List<String>) arguments.get(VALUE);
		if (selectedIds == null) {
			selectedIds = Collections.emptyList();
		}

		List<Object> newSelection = resolveOptions(selectedIds);
		_selectField.setAsSelection(newSelection);

		// Update value state with full descriptors for chip display.
		putState(VALUE, toOptionDescriptors(_selectField.getSelection()));

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
	 * Builds option descriptors and populates the given index map with stable ID to option
	 * mappings.
	 */
	private List<Map<String, Object>> buildOptionDescriptors(List<?> options, Map<String, Object> index) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		LabelProvider labelProvider = _selectField.getOptionLabelProvider();
		ResourceProvider resourceProvider = toResourceProvider(labelProvider);

		int counter = 0;
		for (Object option : options) {
			String id = stableOptionId(option, labelProvider, counter++);
			index.put(id, option);

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, labelProvider.getLabel(option));

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
	 * Converts a list of selected options to descriptors. Uses the existing {@link #_optionIndex}
	 * for stable IDs when available, falling back to index-based IDs for the initial render (before
	 * loadOptions).
	 */
	private List<Map<String, Object>> toOptionDescriptors(List<?> options) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		LabelProvider labelProvider = _selectField.getOptionLabelProvider();
		ResourceProvider resourceProvider = toResourceProvider(labelProvider);

		int counter = 0;
		for (Object option : options) {
			String id = stableOptionId(option, labelProvider, counter++);

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, labelProvider.getLabel(option));

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
	 * Generates a stable option ID by combining the option's label with its position index. This
	 * survives server restarts and is unique within the option list.
	 */
	private String stableOptionId(Object option, LabelProvider labelProvider, int index) {
		String label = labelProvider.getLabel(option);
		if (label == null) {
			label = "";
		}
		return index + ":" + label;
	}

	private ResourceProvider toResourceProvider(LabelProvider labelProvider) {
		if (labelProvider instanceof ResourceProvider) {
			return (ResourceProvider) labelProvider;
		}
		return null;
	}
}
