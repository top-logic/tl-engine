/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Utilities that help to attach translating {@link ButtonControl} to {@link FormField}.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
public class I18NTranslationUtil {

	private static final String TRANSLATE_COMMAND_CSS_CLASS = "i18nTranslationCommand";

	private static final class Listener implements AttachedPropertyListener, ImmutablePropertyListener,
			DisabledPropertyListener, BlockedStateChangedListener, VisibilityListener {

		private FormField _field;

		private ButtonControl _control;

		Listener(FormField field, ButtonControl control) {
			_field = field;
			_control = control;
		}

		@Override
		public Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue) {
			if (sender == _field) {
				updateControl();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			if (sender == _field) {
				updateControl();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (sender == _field) {
				updateControl();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (sender == _field) {
				updateControl();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
			if (newValue.booleanValue()) {
				_field.addListener(FormMember.IMMUTABLE_PROPERTY, this);
				_field.addListener(FormMember.DISABLED_PROPERTY, this);
				_field.addListener(FormMember.VISIBLE_PROPERTY, this);
				_field.addListener(FormField.BLOCKED_PROPERTY, this);
				updateControl();
			} else {
				_field.removeListener(FormMember.IMMUTABLE_PROPERTY, this);
				_field.removeListener(FormMember.DISABLED_PROPERTY, this);
				_field.removeListener(FormMember.VISIBLE_PROPERTY, this);
				_field.removeListener(FormField.BLOCKED_PROPERTY, this);
			}
		}

		private void updateControl() {
			switch (_field.getMode()) {
				case ACTIVE:
					_control.setVisible(true);
					_control.getModel().setExecutable();
					break;
				case BLOCKED:
				case DISABLED:
					_control.setVisible(true);
					_control.getModel()
						.setNotExecutable(com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED);
					break;
				case LOCALLY_IMMUTABLE:
				case IMMUTABLE:
				case INVISIBLE:
					_control.setVisible(false);
					break;
				default:
					break;
			}
		}

	}

	/**
	 * Creates a {@link ButtonControl} for a {@link #getTranslateCommand translation command}.
	 */
	public static ButtonControl getTranslateControl(FormField field, Iterable<? extends FormField> languageFields,
			FieldTranslator translator) {
		CommandModel command = getTranslateCommand(field, languageFields, translator);
		ButtonControl button = new ButtonControl(command);
		button.addListener(AbstractControlBase.ATTACHED_PROPERTY, new Listener(field, button));
		return button;
	}

	/**
	 * Creates a command that fills the given I18N-{@link FormField} with a translation based on the
	 * value of a specified {@link #getSourceField source I18N-field} from the given language
	 * fields.
	 */
	public static CommandModel getTranslateCommand(FormField targetField, Iterable<? extends FormField> languageFields,
			FieldTranslator translator) {
		CommandModel command = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				FormField sourceField = getSourceField(languageFields);
				translator.translate(sourceField, targetField);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		command.setImage(Icons.TRANSLATE);
		command.setNotExecutableImage(Icons.TRANSLATE);
		command.setLabel(Resources.getInstance().getString(I18NConstants.TRANSLATE));
		command.setCssClasses(TRANSLATE_COMMAND_CSS_CLASS);

		// Translation is recorded explicitly.
		ScriptingRecorder.annotateAsDontRecord(command);
		return command;
	}

	/**
	 * Whether the given {@link FormField} belongs to the {@link #getSourceLanguage() source language}.
	 */
	public static boolean isSourceField(FormField field) {
		Locale fieldLanguage = I18NTranslationUtil.getLocaleFromField(field);
		Locale sourceLanguage = I18NTranslationUtil.getSourceLanguage();
		return I18NTranslationUtil.equalLanguage(fieldLanguage, sourceLanguage);
	}

	/**
	 * Looks up the {@link FormField} belonging to the {@link #getSourceLanguage() source language}
	 * from the given language fields.
	 *
	 * <p>
	 * Each language field must have an attached {@link Locale}.
	 * </p>
	 * 
	 * @see #getLocaleFromField(FormField)
	 */
	public static FormField getSourceField(Iterable<? extends FormField> languageFields) {
		Locale sourceLanguage = getSourceLanguage();
		for (FormField field : languageFields) {
			Locale fieldLanguage = I18NTranslationUtil.getLocaleFromField(field);
			if (I18NTranslationUtil.equalLanguage(fieldLanguage, sourceLanguage)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Determines the {@link Locale} whose I18N-{@link FormField} will be used as source for
	 * translation.
	 */
	public static Locale getSourceLanguage() {
		return TLContext.getLocale();
	}

	/**
	 * Determines the {@link Locale} from the given internationalization field.
	 */
	public static Locale getLocaleFromField(FormField field) {
		return field.get(InternationalizationEditor.LOCALE);
	}

	/**
	 * Whether the given {@link Locale}s represent equal languages.
	 */
	public static boolean equalLanguage(Locale locale1, Locale locale2) {
		return locale1.getLanguage().equals(locale2.getLanguage());
	}

	/**
	 * A helper class encapsulating the call to {@link TranslationService} and the actual transfer
	 * of the translation between {@link FormField}s.
	 */
	public static interface FieldTranslator {

		/**
		 * Fills the target {@link FormField} by translating the string from the source
		 * {@link FormField}.
		 */
		String translate(FormField source, FormField target);

	}

	/**
	 * Abstract implementation of {@link FieldTranslator}.
	 */
	public static abstract class AbstractFieldTranslator implements FieldTranslator {

		@Override
		public String translate(FormField source, FormField target) {
			String text = getValueAsString(source);
			String translatedText;
			if (text.trim().isEmpty()) {
				translatedText = text;
			} else {
				Locale sourceLocale = getLocaleFromField(source);
				Locale targetLocale = getLocaleFromField(target);
				translatedText = TranslationService.getInstance().translate(text, sourceLocale, targetLocale);
			}
			setValueFromString(translatedText, source, target);
			return translatedText;
		}

		/**
		 * Extracts the {@link String} part of the value from the given {@link FormField}.
		 */
		public abstract String getValueAsString(FormField field);

		/**
		 * Sets the {@link String} part of the value of the target {@link FormField}. The source
		 * {@link FormField} can be helpful, when the value indeed has a non-{@link String} part.
		 */
		public abstract void setValueFromString(String string, FormField source, FormField target);
	}

	/**
	 * A {@link AbstractFieldTranslator} for {@link FormField}s that are meant to be {@link String}-valued.
	 */
	public static class StringValuedFieldTranslator extends AbstractFieldTranslator {

		/**
		 * Singleton {@link I18NTranslationUtil.StringValuedFieldTranslator} instance.
		 */
		public static final StringValuedFieldTranslator INSTANCE = new StringValuedFieldTranslator();

		private StringValuedFieldTranslator() {
			// Singleton constructor.
		}

		@Override
		public String getValueAsString(FormField field) {
			Object value = field.getValue();
			if (value == null) {
				return StringServices.EMPTY_STRING;
			}
			return value.toString();
		}

		@Override
		public void setValueFromString(String string, FormField source, FormField target) {
			target.setValue(string);

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(target, string);
			}
		}
	}
}
