/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAction;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Dialog editing an internationalized value in two panes: the current user's language on top and
 * one selectable other language below.
 *
 * <p>
 * Restricting the display to two languages keeps the dialog usable for applications with many
 * supported locales and for tall entry editors (multi-line texts, wysiwyg editors), and matches
 * the pairwise workflow of translating: write in one language, then translate into or manually
 * adjust one other language at a time. The language selector marks locales without content, so
 * completeness stays visible without rendering every language. Edits of previously visited
 * languages are buffered and applied together on OK.
 * </p>
 *
 * <p>
 * When a {@link TranslationService} is active, each pane has a button filling it with a
 * translation of the other pane, overwriting the pane's own text. The button sits at the pane it
 * modifies — with exactly two panes, the translation source is always the other one. Translation
 * only ever targets a visible pane, so no language is overwritten sight unseen.
 * </p>
 *
 * <p>
 * The dialog is composed entirely from existing controls — a {@code TLWindow} chrome holding a
 * {@link ReactFormBuilder responsive form} of the two entry editors and a {@code TLSelect}
 * language picker, plus standard {@code TLButton} OK/Cancel actions. The value kind (plain
 * strings, structured HTML) is abstracted by an {@link I18NValueEditor}.
 * </p>
 */
public class I18NEditorDialog {

	private final ReactContext _context;

	private final DialogManager _dialogManager;

	private final FieldModel _mainModel;

	private final I18NValueEditor _valueEditor;

	private final Resources _resources;

	/** The label of the edited field, or {@code null} for a generic dialog title. */
	private final String _fieldLabel;

	/** The locale used to render language names. */
	private final Locale _displayLocale;

	/** The locale of the fixed top pane: the supported locale matching the user's language. */
	private final Locale _topLocale;

	/** The supported locales offered in the bottom pane's language selector, keyed by tag. */
	private final Map<String, Locale> _otherLocales;

	/**
	 * The buffered entry per supported locale. The two panes' current values live in their field
	 * models and are flushed here on a locale switch and on OK.
	 */
	private final Map<Locale, Object> _entries = new HashMap<>();

	private final AbstractFieldModel _topModel;

	private final AbstractFieldModel _bottomModel;

	/** The active translator, or {@code null} when no {@link TranslationService} is active. */
	private final Translator _translator;

	/** The locale currently edited in the bottom pane. */
	private Locale _bottomLocale;

	private SimpleSelectFieldModel _languagePicker;

	private ReactFormFieldChromeControl _bottomChrome;

	/** The top pane's translate button: fills the top pane from the bottom pane. */
	private ReactButtonControl _translateTop;

	/** The bottom pane's translate button: fills the bottom pane from the top pane. */
	private ReactButtonControl _translateBottom;

	/** Whether the bottom pane's entry was empty when the selector options were last built. */
	private boolean _bottomShownEmpty;

	/**
	 * Composes the ready-to-use editor for an internationalized field: the given inline
	 * current-locale control together with the languages button opening the two-pane dialog.
	 *
	 * <p>
	 * Editing other languages is an intrinsic part of an internationalized value, so the dialog
	 * wiring (and hiding the button outside edit mode) is encapsulated here — callers need no
	 * further assembly. The languages button is laid out as an inline adornment so the input keeps
	 * the full field width. When the application supports fewer than two locales, there is nothing
	 * to switch to and the inline control is returned unadorned.
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param mainModel
	 *        The field model whose value is the internationalized value.
	 * @param inline
	 *        The control editing the current locale's entry inline.
	 * @param valueEditor
	 *        The strategy for the edited value kind.
	 * @param fieldLabel
	 *        The label of the edited field, shown in the dialog title, or {@code null} for a
	 *        generic title.
	 * @return The composed editor control.
	 */
	public static ReactControl createEditor(ReactContext context, FieldModel mainModel, ReactFormFieldControl inline,
			I18NValueEditor valueEditor, String fieldLabel) {
		if (Resources.getInstance().getSupportedLocalesInDisplayOrder().size() < 2) {
			return inline;
		}
		ReactButtonControl editLanguages = new ReactButtonControl(context,
			Resources.getInstance().getString(I18NConstants.I18N_EDITOR_OPEN_BUTTON),
			ctx -> {
				openEditor(ctx, mainModel, valueEditor, fieldLabel);
				return HandlerResult.DEFAULT_RESULT;
			});
		editLanguages.setImage(ThemeImage.icon("css:fa-solid fa-globe"));
		editLanguages.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		inline.setEditModeAdornment(editLanguages);

		// Top-align the languages button next to a tall editor rather than centering it.
		return ReactFormBuilder.inputWithAdornment(context, inline, editLanguages,
			valueEditor.isTall() ? StackAlign.START : StackAlign.CENTER);
	}

	/**
	 * Opens the two-pane language editor for the given field.
	 *
	 * @param context
	 *        The current React context (must provide a {@link DialogManager}).
	 * @param mainModel
	 *        The field model whose value is the edited internationalized value.
	 * @param valueEditor
	 *        The strategy for the edited value kind.
	 * @param fieldLabel
	 *        The label of the edited field, shown in the dialog title, or {@code null} for a
	 *        generic title.
	 */
	public static void openEditor(ReactContext context, FieldModel mainModel, I18NValueEditor valueEditor,
			String fieldLabel) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			return;
		}
		List<Locale> supportedLocales = Resources.getInstance().getSupportedLocalesInDisplayOrder();
		if (supportedLocales.size() < 2) {
			// Nothing to translate to.
			return;
		}
		new I18NEditorDialog(context, dialogManager, mainModel, valueEditor, supportedLocales, fieldLabel).open();
	}

	private I18NEditorDialog(ReactContext context, DialogManager dialogManager, FieldModel mainModel,
			I18NValueEditor valueEditor, List<Locale> supportedLocales, String fieldLabel) {
		_context = context;
		_dialogManager = dialogManager;
		_mainModel = mainModel;
		_valueEditor = valueEditor;
		_fieldLabel = fieldLabel;
		_resources = Resources.getInstance();
		_displayLocale = TLContext.getLocale();
		_translator = TranslationService.isActive() ? TranslationService.getInstance() : null;

		_topLocale = topLocale(supportedLocales, _displayLocale);
		_otherLocales = new LinkedHashMap<>();
		for (Locale locale : supportedLocales) {
			if (!locale.equals(_topLocale)) {
				_otherLocales.put(locale.toLanguageTag(), locale);
			}
		}
		_bottomLocale = _otherLocales.values().iterator().next();

		Object i18nValue = _mainModel.getValue();
		for (Locale locale : supportedLocales) {
			_entries.put(locale, _valueEditor.localize(i18nValue, locale));
		}
		_topModel = new AbstractFieldModel(_entries.get(_topLocale));
		_bottomModel = new AbstractFieldModel(_entries.get(_bottomLocale));
		_bottomShownEmpty = _valueEditor.isEmpty(_bottomModel.getValue());
	}

	/** The supported locale matching the user's language, or the first supported locale. */
	private static Locale topLocale(List<Locale> supportedLocales, Locale userLocale) {
		for (Locale locale : supportedLocales) {
			if (locale.getLanguage().equals(userLocale.getLanguage())) {
				return locale;
			}
		}
		return supportedLocales.get(0);
	}

	private void open() {
		ReactFormBuilder form = new ReactFormBuilder(_context);

		ReactControl topEditor = _valueEditor.createEntryEditor(_context, _topModel);
		if (_translator != null) {
			_translateTop = createTranslateButton(
				ctx -> translateEntry(_bottomModel, _bottomLocale, _topModel, _topLocale));
			topEditor = withTranslateButton(topEditor, _translateTop);
		}
		form.addField(languageName(_topLocale), topEditor);

		_languagePicker = new SimpleSelectFieldModel(_bottomLocale.toLanguageTag(),
			new ArrayList<>(_otherLocales.keySet()), false);
		_languagePicker.setNullable(false);
		_languagePicker.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				switchBottomLocale(_otherLocales.get(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});
		form.addField(_resources.getString(I18NConstants.I18N_EDITOR_OTHER_LANGUAGE),
			new ReactSelectFormFieldControl(_context, _languagePicker, this::languageOptionLabel));

		ReactControl bottomEditor = _valueEditor.createEntryEditor(_context, _bottomModel);
		if (_translator != null) {
			_translateBottom = createTranslateButton(
				ctx -> translateEntry(_topModel, _topLocale, _bottomModel, _bottomLocale));
			bottomEditor = withTranslateButton(bottomEditor, _translateBottom);
		}
		_bottomChrome = form.addField(languageName(_bottomLocale), bottomEditor);

		// Refresh the selector options when editing flips the bottom entry between empty and
		// non-empty, so the emptiness markers stay accurate.
		_bottomModel.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				boolean empty = _valueEditor.isEmpty(newValue);
				if (empty != _bottomShownEmpty) {
					refreshLanguageOptions();
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		updateTranslateButtons();

		ReactWindowControl window = new ReactWindowControl(_context,
			dialogTitle(),
			_valueEditor.dialogWidth(),
			() -> _dialogManager.closeTopDialog(DialogResult.cancelled()));
		window.setChild(form.build());

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.cancel(_context, ctx -> {
			_dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		ReactButtonControl okButton = MessageButtons.ok(_context, ctx -> {
			applyValue();
			_dialogManager.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		if (!_valueEditor.isTall()) {
			// Make OK the Enter-default. Only for single-line entry editors: in a multi-line editor
			// Enter inserts a newline, so it must not also submit the dialog.
			okButton.markAsDefault();
		}
		actions.add(okButton);
		window.setActions(actions);

		_dialogManager.openDialog(false, window, result -> {
			// Nothing to do on close: OK already applied the value, Cancel discards.
		});
	}

	/** The dialog title, naming the edited field when its label is known. */
	private String dialogTitle() {
		if (_fieldLabel == null || _fieldLabel.isEmpty()) {
			return _resources.getString(I18NConstants.I18N_EDITOR_TITLE);
		}
		return _resources.getString(I18NConstants.I18N_EDITOR_TITLE__FIELD.fill(_fieldLabel));
	}

	private ReactButtonControl createTranslateButton(ButtonAction action) {
		ReactButtonControl button = new ReactButtonControl(_context, "", action);
		button.setImage(ThemeImage.icon("css:fa-solid fa-language"));
		button.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		return button;
	}

	private ReactControl withTranslateButton(ReactControl editor, ReactButtonControl button) {
		// Top-align the translate button next to a tall editor rather than centering it.
		return ReactFormBuilder.inputWithAdornment(_context, editor, button,
			_valueEditor.isTall() ? StackAlign.START : StackAlign.CENTER);
	}

	/**
	 * Switches the bottom pane to another locale: buffers the current entry and rebinds the pane.
	 */
	private void switchBottomLocale(Locale locale) {
		if (locale == null || locale.equals(_bottomLocale)) {
			return;
		}
		_entries.put(_bottomLocale, _bottomModel.getValue());
		_bottomLocale = locale;
		_bottomModel.setValue(_entries.get(locale));
		_bottomChrome.setLabel(languageName(locale));
		updateTranslateButtons();
		refreshLanguageOptions();
	}

	/**
	 * Translates the source pane's entry into the target pane, overwriting it. A service failure
	 * leaves the target unchanged.
	 */
	private HandlerResult translateEntry(FieldModel sourceModel, Locale source, FieldModel targetModel, Locale target) {
		Object entry = sourceModel.getValue();
		if (_translator == null || _valueEditor.isEmpty(entry)
				|| !_translator.isSupported(source) || !_translator.isSupported(target)) {
			return HandlerResult.DEFAULT_RESULT;
		}
		try {
			targetModel.setValue(_valueEditor.translate(_translator, entry, source, target));
		} catch (I18NRuntimeException ex) {
			// Leave the target unchanged on a service failure.
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/** Updates the translate buttons' labels and enablement for the current language pair. */
	private void updateTranslateButtons() {
		if (_translator == null) {
			return;
		}
		boolean pairSupported = _translator.isSupported(_topLocale) && _translator.isSupported(_bottomLocale);
		_translateTop.setLabel(translateLabel(_bottomLocale));
		_translateTop.setDisabled(!pairSupported);
		_translateBottom.setLabel(translateLabel(_topLocale));
		_translateBottom.setDisabled(!pairSupported);
	}

	private String translateLabel(Locale source) {
		return _resources.getString(I18NConstants.I18N_EDITOR_TRANSLATE_FROM__LANG.fill(languageName(source)));
	}

	/** Rebuilds the language selector options so the emptiness markers reflect the current state. */
	private void refreshLanguageOptions() {
		_bottomShownEmpty = _valueEditor.isEmpty(_bottomModel.getValue());
		_languagePicker.setOptions(new ArrayList<>(_otherLocales.keySet()));
	}

	/**
	 * The selector label for a language tag option: the language name, marked when the locale has
	 * no content yet.
	 */
	private String languageOptionLabel(Object option) {
		Locale locale = _otherLocales.get(option);
		String name = languageName(locale);
		if (_valueEditor.isEmpty(currentEntry(locale))) {
			return _resources.getString(I18NConstants.I18N_EDITOR_EMPTY_LANGUAGE__LANG.fill(name));
		}
		return name;
	}

	/** The locale's entry in its current edit state (pane content wins over the buffer). */
	private Object currentEntry(Locale locale) {
		if (locale.equals(_bottomLocale)) {
			return _bottomModel.getValue();
		}
		if (locale.equals(_topLocale)) {
			return _topModel.getValue();
		}
		return _entries.get(locale);
	}

	private String languageName(Locale locale) {
		return locale.getDisplayLanguage(_displayLocale);
	}

	/** Flushes both panes into the entry buffer and applies the merged value to the main model. */
	private void applyValue() {
		_entries.put(_topLocale, _topModel.getValue());
		_entries.put(_bottomLocale, _bottomModel.getValue());
		// Setting the main field's value re-renders the inline control via its model listener.
		_mainModel.setValue(_valueEditor.merge(_mainModel.getValue(), _entries));
	}

}
