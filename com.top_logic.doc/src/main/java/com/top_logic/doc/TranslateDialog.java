/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc;

import static com.top_logic.basic.shared.string.StringServicesShared.isEmpty;
import static com.top_logic.basic.util.Utils.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.doc.model.Page;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.component.WarningsDialog;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleTemplateDialog;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLResKeyUtil;

/**
 * A {@link SimpleTemplateDialog} to translate {@link Page}s from {@link #getSourceLocale} to
 * {@link #getTargetLocale}.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class TranslateDialog extends SimpleTemplateDialog {

	/** A constant value for {@link #isOverwrite}. */
	public static final boolean ONLY_FOR_EMPTY_TARGET = true;

	/**
	 * Names of {@link ResKey}-valued attributes of a {@link Page}, which are relevant for translation.
	 */
	public static final String[] RELEVANT_PAGE_ATTRIBUTES = new String[] { Page.CONTENT_ATTR, Page.TITLE_ATTR };

	/** @see #getPages */
	private List<Page> _pages;

	/** @see #isOverwrite */
	private boolean _overwrite;

	private Locale _targetLocale;

	/**
	 * Creates a new {@link TranslateDialog}.
	 * 
	 * @param targetLocale
	 *        Locale to which the pages are translated.
	 */
	public TranslateDialog(List<Page> pages, Locale targetLocale) {
		this(pages, targetLocale, !ONLY_FOR_EMPTY_TARGET);
	}

	/**
	 * Creates a new {@link TranslateDialog}.
	 * 
	 * @param targetLocale
	 *        Locale to which the pages are translated.
	 * @param overwrite
	 *        See {@link #isOverwrite()}.
	 */
	public TranslateDialog(List<Page> pages, Locale targetLocale, boolean overwrite) {
		super(
			I18NConstants.SOURCE_LANGUAGE_DIALOG.key("title"),
			null,
			I18NConstants.SOURCE_LANGUAGE_DIALOG.key("message"),
			DisplayDimension.dim(350, DisplayUnit.PIXEL), DisplayDimension.dim(175, DisplayUnit.PIXEL));
		_pages = pages;
		_targetLocale = targetLocale;
		_overwrite = overwrite;
	}

	@Override
	protected void fillFormContext(FormContext formContext) {
		List<Locale> options = getSupportedLocales();
		SelectField selectField = FormFactory.newSelectField(INPUT_FIELD, options);
		selectField.setMandatory(true);
		selectField.setAsSingleSelection(getAnyButTargetLocale(options));
		formContext.addMember(selectField);
	}

	private List<Locale> getSupportedLocales() {
		List<Locale> options = new ArrayList<>();
		for (Locale language : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			if (TranslationService.getInstance().isSupported(language)) {
				options.add(language);
			}
		}
		return options;
	}

	private Locale getAnyButTargetLocale(List<Locale> locales) {
		Locale targetLocale = getTargetLocale();
		for (Locale locale : locales) {
			if (!Objects.equals(locale.getLanguage(), targetLocale.getLanguage())) {
				return locale;
			}
		}
		return null;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK, getOKCommand()));
		buttons.add(MessageBox.button(ButtonType.CANCEL, getCancelCommand()));
	}

	private Command getCancelCommand() {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return getDialogModel().getCloseAction().executeCommand(context);
			}
		};
	}

	private Command getOKCommand() {
		return new FormContextCheckingCommand(this) {
			@Override
			protected boolean doExecuteCommand(FormContext formContext) {
				Locale sourceLocale = getSourceLocale(formContext);
				List<ResKey> errors = new ArrayList<>();
				List<Page> pages = getPages();
				for (Page page : pages) {
					try {
						Logger.info("Translating page '" + page.getName() + "'.", TranslateDialog.class);
						KBUtils.inTransaction(() -> translate(page, sourceLocale));
					} catch (Throwable ex) {
						Logger.info("Failed translating page '" + page.getName() + "'.", ex, TranslateDialog.class);
						errors.add(I18NConstants.ERROR_TRANSLATION_FAILED__PAGE_DETAIL.fill(page, toMessage(ex)));
					}
				}

				if (!errors.isEmpty()) {
					InfoService.showErrorList(errors);
				} else {
					InfoService.showInfo(I18NConstants.TRANSLATED_SUCCESSFUL__COUNT.fill(pages.size()));
				}

				return true;
			}
		};
	}

	static ResKey toMessage(Throwable ex) {
		if (ex instanceof I18NFailure) {
			return ((I18NFailure) ex).getErrorKey();
		} else {
			return ResKey.text(ex.getClass().getName() + (ex.getMessage() != null ? ": " + ex.getMessage() : ""));
		}
	}

	/**
	 * Translate the relevant attributes of the given {@link Page} to {@link #getTargetLocale},
	 * using documentation prepared in the given {@link Locale}.
	 */
	protected void translate(Page page, Locale sourceLocale) {
		Locale targetLocale = getTargetLocale();
		for (String attributeName : RELEVANT_PAGE_ATTRIBUTES) {
			Object value = page.tValueByName(attributeName);
			if (value == null) {
				return;
			}
			if (value instanceof ResKey) {
				translate(page, (ResKey) value, attributeName, sourceLocale, targetLocale);
			}
			else if (value instanceof I18NStructuredText) {
				translate(page, (I18NStructuredText) value, attributeName, sourceLocale, targetLocale);
			}
			else {
				throw new UnsupportedOperationException("Unexpected value type: " + debug(value));
			}
		}
	}

	/**
	 * Update the attribute value in the target {@link Locale} with a value translated from the
	 * source {@link Locale}.
	 * <p>
	 * The attribute value has to be a {@link ResKey}, or null.
	 * </p>
	 */
	protected void translate(TLObject tlObject, ResKey value, String attributeName,
			Locale sourceLocale, Locale targetLocale) {
		if (!_overwrite && !hasEmptyTranslation(targetLocale, value)) {
			return;
		}
		String originalText = localize(sourceLocale, value);
		String translatedText = TranslationService.getInstance().translate(originalText, sourceLocale, targetLocale);
		TLResKeyUtil.updateTranslation(tlObject, attributeName, targetLocale, translatedText);
	}

	/**
	 * Update the attribute value in the target {@link Locale} with a value translated from the
	 * source {@link Locale}.
	 * <p>
	 * The attribute value has to be an {@link I18NStructuredText}, or null.
	 * </p>
	 */
	protected void translate(TLObject tlObject, I18NStructuredText value, String attributeName,
			Locale sourceLocale, Locale targetLocale) {
		if (!_overwrite && !hasEmptyTranslation(targetLocale, value)) {
			return;
		}
		String originalText = localize(sourceLocale, value);
		String translatedText;
		if (StringServices.isEmpty(originalText)) {
			translatedText = "";
		} else {
			translatedText = TranslationService.getInstance().translate(originalText, sourceLocale, targetLocale);
		}

		Map<Locale, StructuredText> newContent = I18NStructuredTextUtil.copyEntries(value);
		// use same images as in the source language.
		Map<String, BinaryData> images = value.localizeImages(sourceLocale);
		newContent.put(targetLocale, new StructuredText(translatedText, images));
		tlObject.tUpdateByName(attributeName, new I18NStructuredText(newContent));
	}

	private static String localize(Locale sourceLocale, ResKey value) {
		return Resources.getInstance(sourceLocale).getString(value);
	}

	private static String localize(Locale sourceLocale, I18NStructuredText value) {
		return value.localizeSourceCode(sourceLocale);
	}

	/**
	 * Whether the given value is empty in the given {@link Locale}.
	 * 
	 * @param value
	 *        Has to be either null, a {@link ResKey} or a {@link StructuredText}.
	 */
	public static boolean isEmptyAttributeValue(Object value, Locale locale) {
		if (value == null) {
			return true;
		}
		if (value instanceof ResKey) {
			return hasEmptyTranslation(locale, (ResKey) value);
		}
		if (value instanceof I18NStructuredText) {
			return hasEmptyTranslation(locale, (I18NStructuredText) value);
		}
		throw new UnsupportedOperationException("Unexpected value type: " + debug(value));
	}

	private static boolean hasEmptyTranslation(Locale locale, ResKey resKey) {
		return isEmpty(localize(locale, resKey));
	}

	private static boolean hasEmptyTranslation(Locale locale, I18NStructuredText value) {
		return isEmpty(localize(locale, value));
	}

	/**
	 * Returns the source {@link Locale} selected in the {@link TranslateDialog}.
	 */
	protected Locale getSourceLocale(FormContext formContext) {
		SelectField field = (SelectField) formContext.getField(INPUT_FIELD);
		return (Locale) field.getSingleSelection();
	}

	/**
	 * Returns the target {@link Locale} to which the {@link #getPages()} is translated.
	 */
	protected Locale getTargetLocale() {
		return _targetLocale;
	}

	/**
	 * The {@link Page}s to translate.
	 */
	public List<Page> getPages() {
		return _pages;
	}

	/**
	 * Whether to replace old text with newly translated text.
	 * 
	 * <p>
	 * If <code>false</code>, only empty pages are filled with translated text.
	 * </p>
	 */
	public boolean isOverwrite() {
		return _overwrite;
	}

	/**
	 * {@link Command} checking {@link FormContext} first.
	 */
	public static abstract class FormContextCheckingCommand implements Command {

		private final AbstractFormDialogBase _dialog;

		/**
		 * Creates a new {@link FormContextCheckingCommand}.
		 */
		public FormContextCheckingCommand(AbstractFormDialogBase dialog) {
			_dialog = dialog;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext displayContext) {
			FormContext formContext = _dialog.getFormContext();

			// display errors
			if (!formContext.checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(formContext);
			}

			// display warnings
			if (!AbstractFormCommandHandler.warningsDisabledTemporarily() && formContext.hasWarnings()) {
				HandlerResult suspended = HandlerResult.suspended();
				WarningsDialog.openWarningsDialog(displayContext.getWindowScope(),
					I18NConstants.SOURCE_LANGUAGE_DIALOG_WARNINGS,
					formContext, AbstractFormCommandHandler.resumeContinuation(suspended));
				return suspended;
			}

			boolean closeDialog = doExecuteCommand(formContext);
			return closeDialog ? _dialog.getDialogModel().getCloseAction().executeCommand(displayContext)
				: HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Executes the command after form context was checked for errors.
		 *
		 * @return <code>true</code>, if the dialog shall be closed, <code>false</code> otherwise.
		 */
		protected abstract boolean doExecuteCommand(FormContext formContext);

	}
}
