/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.util;

import java.util.Locale;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLMetaModelUtil;
import com.top_logic.model.TLNamedPart;

/**
 * Utility class for handling internationalization (I18N) in <code>LangChain4j</code> tools.
 *
 * <p>
 * This class provides helper methods to apply localized label and description values to TopLogic
 * model elements from flat string parameters (as used by {@code @Tool} methods).
 * </p>
 */
public class ToolI18NUtil {

	private ToolI18NUtil() {
		// utility class
	}

	/**
	 * Simple value holder for localized label/description.
	 *
	 * @param labelEn
	 *        English label.
	 * @param labelDe
	 *        German label.
	 * @param descEn
	 *        English description.
	 * @param descDe
	 *        German description.
	 */
	public record LocalizedTexts(
			String labelEn,
			String labelDe,
			String descEn,
			String descDe) {

		/**
		 * Checks if any label is present.
		 *
		 * @return {@code true} if English or German label is non-null.
		 */
		public boolean hasLabel() {
			return labelEn != null || labelDe != null;
		}

		/**
		 * Checks if any description is present.
		 *
		 * @return {@code true} if English or German description is non-null.
		 */
		public boolean hasDescription() {
			return descEn != null || descDe != null;
		}

		/**
		 * Checks if any localized content is present.
		 *
		 * @return {@code true} if any label or description is non-null.
		 */
		public boolean hasAny() {
			return hasLabel() || hasDescription();
		}
	}

	/**
	 * Creates a {@link LocalizedTexts} from flat string parameters.
	 *
	 * @param labelEn
	 *        English label (may be null).
	 * @param labelDe
	 *        German label (may be null).
	 * @param descEn
	 *        English description (may be null).
	 * @param descDe
	 *        German description (may be null).
	 * @return A {@link LocalizedTexts} holding the values.
	 */
	public static LocalizedTexts create(String labelEn, String labelDe, String descEn, String descDe) {
		return new LocalizedTexts(
			trimToNull(labelEn),
			trimToNull(labelDe),
			trimToNull(descEn),
			trimToNull(descDe));
	}

	/**
	 * Applies I18N to the given model element if anything is present.
	 *
	 * <p>
	 * The {@code part} can be a module, class, property, classifier, etc. - any type that
	 * {@link TLMetaModelUtil#saveI18NForPart} accepts.
	 * </p>
	 *
	 * @param part
	 *        The model element to apply I18N to.
	 * @param texts
	 *        The localized texts to apply.
	 */
	public static void applyIfPresent(TLNamedPart part, LocalizedTexts texts) {
		if (texts == null || !texts.hasAny()) {
			return;
		}

		Internationalized i18N = TypedConfiguration.newConfigItem(Internationalized.class);

		// Label
		if (texts.hasLabel()) {
			ResKey.Builder labelBuilder = ResKey.builder();
			if (texts.labelEn() != null) {
				labelBuilder.add(Locale.ENGLISH, texts.labelEn());
			}
			if (texts.labelDe() != null) {
				labelBuilder.add(Locale.GERMAN, texts.labelDe());
			}
			i18N.setLabel(labelBuilder.build());
		}

		// Description
		if (texts.hasDescription()) {
			ResKey.Builder descBuilder = ResKey.builder();
			if (texts.descEn() != null) {
				descBuilder.add(Locale.ENGLISH, texts.descEn());
			}
			if (texts.descDe() != null) {
				descBuilder.add(Locale.GERMAN, texts.descDe());
			}
			i18N.setDescription(descBuilder.build());
		}

		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(part, i18N, tx);
			tx.commit();
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
