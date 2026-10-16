/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigAnnotationControlProvider;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigSelectFieldModel;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.I18NEditorDialog;
import com.top_logic.layout.react.control.form.I18NLocalizedFieldModel;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.control.form.ReactRenderedTextControl;

/**
 * {@link ConfigAnnotationControlProvider} editing a property whose text is {@link Html} in a
 * {@link ReactWysiwygControl}.
 *
 * <p>
 * While the property can be edited, the formatted text is edited in the WYSIWYG editor. For a
 * feature set listed in {@link Config#getSourceFeatureSets()}, the editor additionally offers
 * editing the HTML source. While the property cannot be edited, the formatted text is displayed.
 * </p>
 *
 * <p>
 * An edited text is stored only if it passes the application's HTML safety check. A text the check
 * rejects is reported at the field, which keeps its last accepted value.
 * </p>
 *
 * <p>
 * A value {@link ConfigControlService#holdsText(ConfigFieldModel) held as text} - a plain text,
 * or the text of a value with a format, such as a TL-Script expression written as HTML with
 * embedded expressions - is edited as one text. An internationalized text is edited per language:
 * the text in the user's language inline, the other languages in a dialog. Any other property is
 * left to the control the {@link ConfigControlService} chooses without the annotation.
 * </p>
 */
public class HtmlConfigProvider extends AbstractConfiguredInstance<HtmlConfigProvider.Config>
		implements ConfigAnnotationControlProvider<Html> {

	/**
	 * Configuration options for {@link HtmlConfigProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<HtmlConfigProvider> {

		/** Property name of {@link #getSourceFeatureSets()}. */
		String SOURCE_FEATURE_SETS = "source-feature-sets";

		/**
		 * The feature sets for which the editor offers editing the HTML source.
		 *
		 * <p>
		 * A feature set is named by the {@link Html} annotation of the property.
		 * </p>
		 */
		@Name(SOURCE_FEATURE_SETS)
		@Format(CommaSeparatedStrings.class)
		List<String> getSourceFeatureSets();

	}

	/**
	 * Creates a {@link HtmlConfigProvider} from configuration.
	 */
	@CalledByReflection
	public HtmlConfigProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, Html annotation) {
		boolean sourceEditing = getConfig().getSourceFeatureSets().contains(annotation.features());
		if (ConfigControlService.holdsText(model)) {
			return new ReactRenderedTextControl(context, model,
				() -> ReactWysiwygControl.forText(context, model, sourceEditing),
				Function.identity());
		}
		if (holdsInternationalizedText(model)) {
			String label = Labels.propertyLabel(model.getProperty(), false);
			I18NHtmlTextValueEditor valueEditor = new I18NHtmlTextValueEditor(sourceEditing);
			return new ReactRenderedTextControl(context, model,
				() -> {
					I18NWysiwygControl inline =
						new I18NWysiwygControl(context, new I18NLocalizedFieldModel(model, valueEditor));
					inline.setTextOnly(true);
					inline.setSourceEditing(sourceEditing);
					inline.setCheckHtml(true);
					return I18NEditorDialog.createEditor(context, model, inline, valueEditor, label);
				},
				ReactI18NStringInputControl::displayText, Function.identity());
		}
		return null;
	}

	/**
	 * Whether the given model holds an internationalized text as {@link ResKey}.
	 */
	private static boolean holdsInternationalizedText(ConfigFieldModel model) {
		return !ConfigControlService.holdsText(model)
			&& !(model instanceof ConfigSelectFieldModel)
			&& ResKey.class.isAssignableFrom(model.getProperty().getType());
	}

}
