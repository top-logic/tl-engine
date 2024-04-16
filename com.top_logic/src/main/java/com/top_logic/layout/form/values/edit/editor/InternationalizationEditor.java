/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.values.Fields.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.base.config.i18n.InternationalizedUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.i18n.I18NActiveLanguageControlProvider;
import com.top_logic.layout.form.i18n.I18NStringField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Editor} displaying a {@link ResKey} as its internationalizations in all supported
 * {@link Locale}s.
 * 
 * <p>
 * Editing an internationalization in a certain {@link Locale} results in creating a new literal
 * {@link ResKey} in the model property containing the updated values.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InternationalizationEditor extends AbstractEditor {

	/**
	 * Annotation for the method or type which is annotated with an
	 * {@link InternationalizationEditor} to define whether derived resources can be edited.
	 * 
	 * <p>
	 * The absence of this annotation means "edit also derived resources".
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Retention(RUNTIME)
	@Target({ TYPE, METHOD })
	@TagName("with-tooltip-configuration")
	public @interface WithTooltipConfiguration {

		/**
		 * If <code>true</code>, then not just the actual {@link ResKey} can be edited in an
		 * {@link InternationalizationEditor}, but also the derived {@link ResKey}.
		 */
		boolean value();
	}

	/**
	 * {@link Locale} {@link Property} on a {@link FormField} of a multi-language field group.
	 */
	public static final Property<Locale> LOCALE = TypedAnnotatable.property(Locale.class, "locale");

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		I18NStringField field = I18NStringField.newI18NStringField(fieldName, false, false);
		field.enableDerivedResources(getDerivedResourceDefinition(editorFactory, model));
		field.setControlProvider(new I18NActiveLanguageControlProvider());
		container.addMember(field);

		Mapping<Object, Object> uiConversion = Identity.INSTANCE;
		Mapping<Object, Object> storageConversion = Identity.INSTANCE;
		init(editorFactory, model, field,
			uiConversion,
			storageConversion);

		return field;
	}

	private Map<String, ResKey> getDerivedResourceDefinition(EditorFactory editorFactory, ValueModel model) {
		if (!withTooltipConfiguration(editorFactory, model)) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap(ResKey.TOOLTIP.substring(1), I18NConstants.DERIVED_RESOURCE_TOOLTIP);
	}

	private boolean withTooltipConfiguration(EditorFactory editorFactory, ValueModel model) {
		WithTooltipConfiguration annotation =
			editorFactory.getAnnotation(model.getProperty(), WithTooltipConfiguration.class);
		return annotation == null || annotation.value();
	}

	static String suffixFieldName(String language, String keySuffix) {
		return normalizeFieldName(language + "_" + keySuffix);
	}

	/**
	 * Creates a {@link CommandModel} that displays the derived resource members.
	 *
	 * @param <C>
	 *        Type of the {@link CommandModel} to create.
	 * @param members
	 *        The {@link FormField} displaying the derived resources. These members are displayed
	 *        resp. hidden.
	 * @param modelFactory
	 *        Factory to create the actual returned and by this method enhanced
	 *        {@link CommandModel}.
	 * @param baseKey
	 *        Key that is used to derive a {@link ConfigKey} to store personal settings.
	 */
	public static <C extends CommandModel> C displayDerivedCommand(List<? extends StringField> members,
			Function<Command, C> modelFactory, ConfigKey baseKey) {
		ConfigKey key = ConfigKey.derived(baseKey, "derivedResourcesDisplayed");
		boolean initiallyVisible;
		Boolean personalizedVal = PersonalConfiguration.getPersonalConfiguration().getBoolean(key.get());
		if (personalizedVal != null) {
			initiallyVisible = personalizedVal.booleanValue();
		} else {
			initiallyVisible = members.stream()
				.filter(field -> !StringServices.isEmpty(field.getAsString()))
				.findAny()
				.isPresent();
		}
		Command command = new Command() {

			boolean _membersVisible = initiallyVisible;

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				_membersVisible = !_membersVisible;
				for (FormMember derivedMember : members) {
					derivedMember.setVisible(_membersVisible);
				}
				CommandModel button = context.get(Command.EXECUTING_CONTROL).getModel();
				Resources resources = context.getResources();
				if (_membersVisible) {
					button.setImage(Icons.HIDE_DERIVED_RESOURCES);
					button.setLabel(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES));
					button.setTooltip(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES.tooltipOptional()));
				} else {
					button.setImage(Icons.DISPLAY_DERIVED_RESOURCES);
					button.setLabel(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES));
					button.setTooltip(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES.tooltipOptional()));
				}
				PersonalConfiguration.getPersonalConfiguration().setBooleanValue(key.get(), _membersVisible);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		for (FormMember derivedMember : members) {
			derivedMember.setVisible(initiallyVisible);
		}
		ThemeImage icon = initiallyVisible ? Icons.HIDE_DERIVED_RESOURCES : Icons.DISPLAY_DERIVED_RESOURCES;
		C button = modelFactory.apply(command);
		button.setImage(icon);
		Resources resources = Resources.getInstance();
		if (initiallyVisible) {
			button.setLabel(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES));
			button.setTooltip(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES.tooltipOptional()));
		} else {
			button.setLabel(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES));
			button.setTooltip(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES.tooltipOptional()));
		}
		return button;
	}

	/**
	 * Translates the given language (e.g. "de"/"en") into "Deutsch"/"Englisch" (resp.
	 * "German"/"English") according to the language of the given {@link I18NBundle}.
	 */
	public static String translateLanguageName(I18NBundle res, Locale language) {
		return res.getString(I18NConstants.LANGUAGE.key(language.getLanguage()),
			language.getDisplayLanguage(res.getLocale()));
	}

	/**
	 * Creates an empty {@link Builder} which has the same "dynamic key" as the old value, or a new
	 * "dynamic key" if the old value is <code>null</code> or does not have a dynamic key.
	 * 
	 * @see com.top_logic.base.config.i18n.I18NConstants#DYNAMIC
	 * 
	 * @param oldValue
	 *        Base value. May be <code>null</code>.
	 * @return Builder for a new {@link ResKey} with dynamic key.
	 */
	public static Builder builder(ResKey oldValue) {
		String oldKey = oldValue != null && oldValue.hasKey() ? oldValue.getKey() : null;

		// Note: If the original value was a code-defined resource key, this key must not be
		// assigned a new value to. Instead, a new dynamic key must be allocated.
		boolean hasDynamicKey = oldKey != null && oldKey.startsWith(
			com.top_logic.base.config.i18n.I18NConstants.DYNAMIC.toPrefix());
		String key = hasDynamicKey ? oldKey : InternationalizedUtil.newKey().getKey();

		return ResKey.builder(key);
	}

}
