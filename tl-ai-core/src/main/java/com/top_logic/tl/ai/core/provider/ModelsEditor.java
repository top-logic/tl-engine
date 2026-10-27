/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.text.Format;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.MultiSelectDialog;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.AbstractEditor;
import com.top_logic.layout.form.values.edit.editor.ConfigurationFormatAdapter;
import com.top_logic.layout.form.values.edit.editor.Converters;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Custom property editor for the model allowlist of an {@link LLMProviderConfig.Config}.
 *
 * <p>
 * Renders the {@link LLMProviderConfig.Config#getModels() models} property as a free-text,
 * comma-separated input field accompanied by a "Load models" command. The command queries the
 * provider's discovery endpoint and opens the standard multi-select chooser dialog (the same dialog
 * an option-bound field uses) prefilled with the currently configured models. The chosen selection
 * replaces the field value. This uniform UX works for every provider, including those whose
 * endpoint exposes no model listing: in that case the field simply remains editable by hand.
 * </p>
 */
public class ModelsEditor extends AbstractEditor {

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> provider =
			(ConfigurationValueProvider<Object>) (ConfigurationValueProvider<?>) CommaSeparatedStrings.INSTANCE;
		Format format = new ConfigurationFormatAdapter(provider);

		ComplexField field = Fields.complex(container, fieldName, format);

		init(editorFactory, model, field, Identity.INSTANCE,
			Converters.valueConverter(model.getProperty()));

		// Render the value box as a free-text popup-select look-alike: the control attaches an
		// expand icon (opens the discover-and-choose dialog) and a clear icon inline on the right,
		// while keeping the box editable so model names can be typed directly.
		field.setControlProvider(new ModelsControl.Provider(model.getModel()));

		return field;
	}

	/**
	 * Discovers the available models and opens a multi-select chooser dialog so the user can pick
	 * which ones to keep. The chosen selection replaces the field value.
	 *
	 * <p>
	 * The dialog options are the union of the currently configured models and the discovered ones
	 * (current first), so manually entered entries that the endpoint does not report are not lost
	 * and remain deselectable. The current models are pre-selected. If discovery yields nothing, no
	 * dialog opens and the user keeps editing the field by hand.
	 * </p>
	 *
	 * @param context
	 *        The command execution context.
	 * @param field
	 *        The field holding the comma-separated model list.
	 * @param config
	 *        The provider configuration being edited.
	 * @return Always {@link HandlerResult#DEFAULT_RESULT}.
	 */
	static HandlerResult openModelChooser(DisplayContext context, ComplexField field, ConfigurationItem config) {
		List<String> discovered = discover(config);
		if (discovered.isEmpty()) {
			InfoService.showInfo(I18NConstants.NO_MODELS_FROM_ENDPOINT);
			return HandlerResult.DEFAULT_RESULT;
		}

		@SuppressWarnings("unchecked")
		List<String> current = (List<String>) field.getValue();
		List<String> options = merge(current, discovered);

		FormContext chooserContext = new FormContext("loadModels", ResPrefix.GLOBAL);
		ScriptingRecorder.annotateAsDontRecord(chooserContext);

		SelectField selection = Fields.multiSelectField(chooserContext, "models", options);
		selection.setLabel(Resources.getInstance().getString(I18NConstants.LOAD_MODELS));
		// Pre-select the current value BEFORE registering the listener, so the preselection itself
		// does not trigger a write-back.
		Fields.selections(selection, current == null ? List.of() : new ArrayList<>(current));
		selection.addValueListener((sender, oldValue, newValue) -> {
			@SuppressWarnings("unchecked")
			List<String> chosen = (List<String>) newValue;
			field.setValue(new ArrayList<>(chosen));
			InfoService.showInfo(I18NConstants.LOADED_MODELS__COUNT.fill(Integer.valueOf(chosen.size())));
		});

		SelectDialogConfig dialogConfig = TypedConfiguration.newConfigItem(SelectDialogConfig.class);
		new MultiSelectDialog(selection, dialogConfig).open(context.getWindowScope());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Discovers the available model IDs from the provider configured by the given
	 * {@link ConfigurationItem}.
	 *
	 * <p>
	 * Delegates to the provider's own {@link LLMProviderConfig#discoverModels()}, which uses the
	 * correct authentication headers for each provider. Any failure is reported to the user and
	 * yields an empty result, so does a provider whose credentials are not available, which is not
	 * asked at all.
	 * </p>
	 *
	 * @param config
	 *        The provider configuration being edited.
	 * @return The discovered model IDs, never {@code null}; empty if discovery is unsupported or
	 *         fails.
	 */
	public static List<String> discover(ConfigurationItem config) {
		if (!(config instanceof LLMProviderConfig.Config)) {
			return List.of();
		}
		LLMProviderConfig.Config providerConfig = (LLMProviderConfig.Config) config;
		String baseUrl = providerConfig.getBaseUrl();
		if (StringServices.isEmpty(baseUrl)) {
			return List.of();
		}
		try {
			LLMProviderConfig provider = TypedConfigUtil.createInstance(providerConfig);
			if (!provider.isAvailable()) {
				// No request without credentials: its failure would not tell what is missing.
				ResKey secret = provider.credentialsReference();
				InfoService.showError(secret == null
					? I18NConstants.ERROR_DISCOVERY_UNAVAILABLE__PROVIDER.fill(providerConfig.getName())
					: I18NConstants.ERROR_DISCOVERY_UNAVAILABLE__PROVIDER_SECRET.fill(providerConfig.getName(), secret));
				return List.of();
			}
			return provider.discoverModels();
		} catch (Exception ex) {
			InfoService.showError(I18NConstants.MODEL_DISCOVERY_FAILED);
			return List.of();
		}
	}

	/**
	 * Merges two model lists: keeps every existing entry in order, then appends only the entries
	 * from the second list that are not already present.
	 *
	 * @param existing
	 *        The currently configured model IDs, may be {@code null}.
	 * @param discovered
	 *        The discovered model IDs, may be {@code null}.
	 * @return A new list containing the existing entries followed by the new discovered entries.
	 */
	public static List<String> merge(List<String> existing, List<String> discovered) {
		List<String> result = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		if (existing != null) {
			for (String entry : existing) {
				if (seen.add(entry)) {
					result.add(entry);
				}
			}
		}
		if (discovered != null) {
			for (String entry : discovered) {
				if (entry != null && !entry.isBlank() && seen.add(entry)) {
					result.add(entry);
				}
			}
		}
		return result;
	}

}
