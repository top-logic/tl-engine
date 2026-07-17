/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tools.resources.translate.Translator;

/**
 * Strategy adapting one kind of internationalized value (a per-locale map of entries) for the
 * {@link I18NEditorDialog}.
 *
 * <p>
 * An implementation defines how a single locale's entry is extracted from and merged back into the
 * internationalized value, how an entry is machine-translated, and which control edits a single
 * entry. The dialog itself is agnostic of the value kind: the same dialog edits {@code I18NString}
 * values (entries are {@link String}s edited in text inputs) and {@code I18NHtml} values (entries
 * are structured texts edited in a wysiwyg editor).
 * </p>
 */
public interface I18NValueEditor {

	/**
	 * Extracts the given locale's entry from the internationalized value.
	 *
	 * <p>
	 * The extraction is strict (no fallback to other languages), so editing never silently copies
	 * another language's content.
	 * </p>
	 *
	 * @param i18nValue
	 *        The internationalized value, or {@code null}.
	 * @return The locale's entry, or {@code null} if the locale has none.
	 */
	Object localize(Object i18nValue, Locale locale);

	/**
	 * Whether the given entry counts as having no content.
	 *
	 * @param entry
	 *        An entry as returned by {@link #localize(Object, Locale)} or produced by the
	 *        {@link #createEntryEditor(ReactContext, FieldModel) entry editor}.
	 */
	boolean isEmpty(Object entry);

	/**
	 * Builds the internationalized value from the edited entries.
	 *
	 * @param i18nValue
	 *        The value before editing, or {@code null}. Implementations use it to preserve entries
	 *        of locales not covered by {@code entries} (e.g. locales the application no longer
	 *        supports).
	 * @param entries
	 *        One entry per supported locale; an {@link #isEmpty(Object) empty} entry removes the
	 *        locale from the value.
	 * @return The merged internationalized value, or {@code null} if no locale has content.
	 */
	Object merge(Object i18nValue, Map<Locale, Object> entries);

	/**
	 * Machine-translates the given entry.
	 *
	 * @param translator
	 *        The translator to use; both locales are {@link Translator#isSupported(Locale)
	 *        supported}.
	 * @param entry
	 *        The non-{@link #isEmpty(Object) empty} source entry.
	 * @return The translated entry.
	 * @throws I18NRuntimeException
	 *         If the translation service fails.
	 */
	Object translate(Translator translator, Object entry, Locale source, Locale target);

	/**
	 * Creates the control editing a single locale's entry.
	 *
	 * @param entryModel
	 *        The model holding the entry value.
	 */
	ReactControl createEntryEditor(ReactContext context, FieldModel entryModel);

	/**
	 * Whether the {@link #createEntryEditor(ReactContext, FieldModel) entry editor} is a tall
	 * multi-line control.
	 *
	 * <p>
	 * Next to a tall editor, adornment buttons are top-aligned instead of vertically centered, and
	 * the dialog's OK button is not the Enter-default (Enter inserts a line break instead of
	 * submitting).
	 * </p>
	 */
	boolean isTall();

	/**
	 * The width of the editor dialog.
	 */
	DisplayDimension dialogWidth();

}
