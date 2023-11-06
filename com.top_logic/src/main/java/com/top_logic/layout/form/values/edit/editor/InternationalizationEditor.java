/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.edit.editor.ItemEditor.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.base.config.i18n.InternationalizedUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.FieldTranslator;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.StringValuedFieldTranslator;
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
public class InternationalizationEditor implements Editor {

	/**
	 * Annotation for the method or type which is annotated with an
	 * {@link InternationalizationEditor} to define whether derived resources can be edited.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Retention(RUNTIME)
	@Target({ TYPE, METHOD })
	@TagName("with-tooltip-configuration")
	public @interface WithTooltipConfiguration {

		/**
		 * If <code>true</code> that not just the actual {@link ResKey} can be edited in an
		 * {@link InternationalizationEditor}, but also the derived {@link ResKey}.
		 */
		boolean value();
	}


	private static final String ADDITIONAL_RESOURCE_CSS = "additional-resource";

	private static final String ADDITIONAL_RESOURCE_SPACING_CSS = "with-spacing";

	private static final String DISPLAY_DERIVED_FIELD = "displayDerived";

	private static TagTemplate templateDefinition(FormMember member, boolean initiallyCollapsed, List<String> languages,
			List<String> additionals) {

		TranslateButtonCP cp = new TranslateButtonCP(additionals);
		List<HTMLTemplateFragment> contentTemplates = new ArrayList<>();
		contentTemplates.add(css(ITEM_CONTENT_CSS_CLASS));
		int numberAdditionals = additionals.size();
		int numberLanguages = languages.size();
		for (int languageIndex = 0; languageIndex < numberLanguages; languageIndex++) {
			String lang = languages.get(languageIndex);
			contentTemplates.add(member(lang, 
				descriptionBox(
					fragment(labelWithColon(), direct(cp), error()),
					self())));
			boolean withSpacing = languageIndex < (numberLanguages - 1);
			for (int additionalIndex = 0; additionalIndex < numberAdditionals; additionalIndex++) {
				String additionalResourceCss = ADDITIONAL_RESOURCE_CSS;
				if (withSpacing && additionalIndex == numberAdditionals - 1) {
					additionalResourceCss += " " + ADDITIONAL_RESOURCE_SPACING_CSS;
				}
				contentTemplates.add(
					div(css(additionalResourceCss),
						fieldBox(suffixFieldName(lang, additionals.get(additionalIndex)))));
			}
		}

		return div(css(ITEM_CSS_CLASS),
			fieldsetBox(
				span(css(ITEM_TITLE_CSS_CLASS), label(), span(css(TOOLBAR_CSS_CLASS), member(DISPLAY_DERIVED_FIELD))),
				div(contentTemplates.toArray(new HTMLTemplateFragment[contentTemplates.size()])),
				ConfigKey.field(member)).setInitiallyCollapsed(initiallyCollapsed));
	}

	/**
	 * {@link Locale} {@link Property} on a {@link FormField} of a multi-language field group.
	 */
	public static final Property<Locale> LOCALE = TypedAnnotatable.property(Locale.class, "locale");

	static final Property<String> SUFFIX = TypedAnnotatable.property(String.class, "suffix");

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		PropertyDescriptor property = model.getProperty();
		FormGroup group = Fields.group(container, editorFactory, property);

		boolean minimized = Fields.displayMinimized(editorFactory, property);
		Resources resources = Resources.getInstance();
		boolean propertyMandatory = isMandatory(editorFactory, property);
		boolean currentlyMandatory = propertyMandatory && model.getValue() == null;
		
		Map<String, ResKey> derivedKeyDefinitions = getDerivedResourceDefinition(editorFactory, model);
		ArrayList<String> suffixes = new ArrayList<>(derivedKeyDefinitions.keySet());
		List<String> languages = new ArrayList<>();
		List<StringField> suffixMembers = new ArrayList<>();

		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			String language = locale.getLanguage();
			languages.add(language);
			
			FormField input = createInternationalizationField(editorFactory, model, language);
			input.setMandatory(currentlyMandatory);
			input.set(LOCALE, locale);
			input.setLabel(translateLanguageName(resources, locale));
			group.addMember(input);
			
			for (String keySuffix : suffixes) {
				StringField suffixField = FormFactory.newStringField(suffixFieldName(language, keySuffix));

				ResKey i18N = derivedKeyDefinitions.get(keySuffix);
				suffixField.setLabel(resources.getString(i18N));
				suffixField.setTooltip(resources.getString(i18N.tooltip(), null));
				suffixField.set(LOCALE, locale);
				suffixField.set(SUFFIX, keySuffix);

				group.addMember(suffixField);
				suffixMembers.add(suffixField);
			}
		}

		ValueBinding binding = new ValueBinding(propertyMandatory, model, group);
		binding.bind();

		displayDerivedCommand(group, suffixMembers);

		template(group, templateDefinition(group, minimized, languages, suffixes));

		return group;
	}

	private Map<String, ResKey> getDerivedResourceDefinition(EditorFactory editorFactory, ValueModel model) {
		WithTooltipConfiguration annotation =
			editorFactory.getAnnotation(model.getProperty(), WithTooltipConfiguration.class);
		if (annotation != null && !annotation.value()) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap(ResKey.TOOLTIP.substring(1), I18NConstants.DERIVED_RESOURCE_TOOLTIP);
	}

	static String suffixFieldName(String language, String keySuffix) {
		return normalizeFieldName(language + "_" + keySuffix);
	}

	private void displayDerivedCommand(FormGroup container, List<? extends StringField> members) {
		if (members.isEmpty()) {
			CommandField button = button(container, DISPLAY_DERIVED_FIELD, null, Command.DO_NOTHING);
			button.setVisible(false);
			return;
		}
		ConfigKey key = ConfigKey.derived(ConfigKey.field(container), "derivedResourcesDisplayed");
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
		displayDerivedCommand(container, members, key, initiallyVisible);
	}

	private void displayDerivedCommand(FormGroup container, List<? extends FormMember> members,
			ConfigKey configKey, boolean initiallyVisible) {
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
					button.setTooltip(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES.tooltip(), null));
				} else {
					button.setImage(Icons.DISPLAY_DERIVED_RESOURCES);
					button.setLabel(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES));
					button.setTooltip(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES.tooltip(), null));
				}
				PersonalConfiguration.getPersonalConfiguration().setBooleanValue(configKey.get(), _membersVisible);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		for (FormMember derivedMember : members) {
			derivedMember.setVisible(initiallyVisible);
		}
		ThemeImage icon = initiallyVisible ? Icons.HIDE_DERIVED_RESOURCES : Icons.DISPLAY_DERIVED_RESOURCES;
		CommandField button = button(container, DISPLAY_DERIVED_FIELD, icon, command);
		/* Let the user display the derived resources, also when they can not be changed. */
		button.setInheritDeactivation(false);
		Resources resources = Resources.getInstance();
		if (initiallyVisible) {
			button.setLabel(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES));
			button.setTooltip(resources.getString(I18NConstants.HIDE_DERIVED_RESOURCES.tooltip(), null));
		} else {
			button.setLabel(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES));
			button.setTooltip(resources.getString(I18NConstants.DISPLAY_DERIVED_RESOURCES.tooltip(), null));
		}

	}


	static boolean isMandatory(AnnotationCustomizations customizations, PropertyDescriptor property) {
		return property.isMandatory() || !property.isNullable()
			|| customizations.getAnnotation(property, Mandatory.class) != null
			|| customizations.getAnnotation(property, NonNullable.class) != null;
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
	 * {@link ControlProvider} that renders a translate button for the given {@link FormField}.
	 */
	private static class TranslateButtonCP extends AbstractFormFieldControlProvider {

		private List<String> _additionals;

		TranslateButtonCP(List<String> additionals) {
			_additionals = additionals;
		}

		@Override
		protected Control createInput(FormMember member) {
			FormField field = (FormField) member;

			Locale fieldLanguage = I18NTranslationUtil.getLocaleFromField(field);
			if (I18NTranslationUtil.equalLanguage(fieldLanguage, I18NTranslationUtil.getSourceLanguage())) {
				return null;
			}
			if (!TranslationService.getInstance().isSupported(fieldLanguage)) {
				return null;
			}

			return I18NTranslationUtil.getTranslateControl(field, member.getParent(),
				new MultiFieldTranslator(_additionals));
		}

	}

	private static class MultiFieldTranslator implements FieldTranslator {

		private List<String> _additionals;

		MultiFieldTranslator(List<String> additionals) {
			_additionals = additionals;
		}

		@Override
		public String translate(FormField source, FormField target) {
			StringValuedFieldTranslator dispatch = StringValuedFieldTranslator.INSTANCE;
			String result = dispatch.translate(source, target);
			if (!_additionals.isEmpty()) {
				String sourceLang = source.get(LOCALE).getLanguage();
				String targetLang = target.get(LOCALE).getLanguage();
				FormContainer container = source.getParent();
				for (String additional : _additionals) {
					FormMember derivedSource = container.getMember(suffixFieldName(sourceLang, additional));
					FormMember derivedTarget = container.getMember(suffixFieldName(targetLang, additional));
					if (derivedSource.isVisible() && derivedTarget.isVisible()) {
						dispatch.translate((FormField) derivedSource, (FormField) derivedTarget);
					}
				}
			}
			return result;
		}

	}

	/**
	 * Creates a field displaying a single internationalization.
	 * 
	 * @param editorFactory
	 *        The {@link EditorFactory} creating the form.
	 * @param model
	 *        The {@link ResKey} property to display.
	 * @param name
	 *        The field name to use.
	 * @return The new {@link FormField}.
	 */
	protected StringField createInternationalizationField(EditorFactory editorFactory, ValueModel model, String name) {
		StringField result = FormFactory.newStringField(normalizeFieldName(name));
		editorFactory.processControlProviderAnnotation(model.getProperty(), result);

		return result;
	}

	static class ValueBinding implements ValueListener, ConfigurationListener {

		private final boolean _mandatory;

		private final ValueModel _model;

		private final FormGroup _group;

		public ValueBinding(boolean mandatory, ValueModel model, FormGroup group) {
			_mandatory = mandatory;
			_model = model;
			_group = group;
		}

		public void bind() {
			initValues();
			bindModelListener();
			bindValueListeners();
		}

		private void initValues() {
			updateUI((ResKey) _model.getValue());
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			unbindModelListener();
			updateModel();
			updateMandatory();
			bindModelListener();
		}

		private void updateMandatory() {
			if (_mandatory) {
				boolean fieldMandatory = _model.getValue() == null;
				for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
					FormField input = it.next();
					String suffix = input.get(SUFFIX);
					if (suffix != null) {
						// Just a derived resource
						continue;
					}
					input.setMandatory(fieldMandatory);
				}
			}
		}

		@Override
		public void onChange(ConfigurationChange change) {
			unbindValueListeners();
			updateUI((ResKey) change.getNewValue());
			updateMandatory();
			bindValueListeners();
		}

		private void updateModel() {
			ResKey oldValue = (ResKey) _model.getValue();
			String oldKey = oldValue != null && oldValue.hasKey() ? oldValue.getKey() : null;

			// Note: If the original value was a code-defined resource key, this key must not be
			// assigned a new value to. Instead, a new dynamic key must be allocated.
			boolean hasDynamicKey = oldKey != null && oldKey.startsWith(
				com.top_logic.base.config.i18n.I18NConstants.DYNAMIC.toPrefix());
			String key = hasDynamicKey ? oldKey : InternationalizedUtil.newKey().getKey();

			Builder literal = ResKey.builder(key);
			for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
				FormField input = it.next();
				String value = (String) input.getValue();
				if (!StringServices.isEmpty(value)) {
					String suffix = input.get(SUFFIX);
					Builder fieldLiteral;
					if (suffix != null) {
						fieldLiteral = literal.suffix(suffix);
					} else {
						fieldLiteral = literal;
					}
					fieldLiteral.add(input.get(LOCALE), value);
				}
			}
			_model.setValue(literal.build());
		}

		private void updateUI(ResKey newValue) {
			if (newValue == null) {
				clearUI();
			} else {
				setUI(newValue);
			}
		}

		private void setUI(ResKey newValue) {
			for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
				FormField field = it.next();
				Locale locale = field.get(LOCALE);
				if (locale == null) {
					continue;
				}
				String suffix = field.get(SUFFIX);
				String text;
				if (suffix == null) {
					text = ResKeyUtil.getTranslation(newValue, locale);
				} else {
					text = ResKeyUtil.getTranslation(newValue.suffix(suffix), locale);
				}
				if (text != null) {
					field.setValue(text);
				}
			}
		}

		private void clearUI() {
			for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
				it.next().setValue(null);
			}
		}

		private void bindValueListeners() {
			for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
				it.next().addValueListener(this);
			}
		}

		private void unbindValueListeners() {
			for (Iterator<FormField> it = _group.getFields(); it.hasNext();) {
				it.next().removeValueListener(this);
			}
		}

		private void bindModelListener() {
			_model.getModel().addConfigurationListener(_model.getProperty(), this);
		}

		private void unbindModelListener() {
			_model.getModel().removeConfigurationListener(_model.getProperty(), this);
		}
	}

}
