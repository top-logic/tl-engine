/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.util;

import java.util.Locale;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLMetaModelUtil;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * Utility class for handling internationalization (I18N) in AI tools.
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

		ResKey partKey = getResKey(part);
		Internationalized i18N = TypedConfiguration.newConfigItem(Internationalized.class);

		// Label: use new values if provided, otherwise preserve existing
		i18N.setLabel(texts.hasLabel()
			? buildResKey(texts.labelEn(), texts.labelDe())
			: readExisting(partKey));

		// Description: use new values if provided, otherwise preserve existing. Descriptions are
		// authored as rich text, so the engine expects them wrapped in a DefaultHtmlResKey.
		i18N.setDescription(new DefaultHtmlResKey(texts.hasDescription()
			? buildResKey(texts.descEn(), texts.descDe())
			: readExisting(partKey.tooltip())));

		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(part, i18N, tx);
			tx.commit();
		}
	}

	private static ResKey buildResKey(String en, String de) {
		ResKey.Builder builder = ResKey.builder();
		if (en != null) {
			builder.add(Locale.ENGLISH, en);
		}
		if (de != null) {
			builder.add(Locale.GERMAN, de);
		}
		return builder.build();
	}

	private static ResKey readExisting(ResKey key) {
		ResKey.Builder builder = ResKey.builder();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			String value = Resources.getInstance(locale).getString(key, null);
			if (value != null) {
				builder.add(locale, value);
			}
		}
		return builder.build();
	}

	/**
	 * Returns the current label of the given model element in the session locale.
	 *
	 * @param part
	 *        The model element.
	 * @return The label, or {@code null} if none is set.
	 */
	public static String getLabel(TLNamedPart part) {
		return Resources.getInstance().getString(getResKey(part), null);
	}

	/**
	 * Returns the current description of the given model element in the session locale.
	 *
	 * @param part
	 *        The model element.
	 * @return The description, or {@code null} if none is set.
	 */
	public static String getDescription(TLNamedPart part) {
		return Resources.getInstance().getString(getResKey(part).tooltip(), null);
	}

	private static ResKey getResKey(TLNamedPart part) {
		if (part instanceof TLType) {
			return TLModelNamingConvention.getTypeLabelKey((TLType) part);
		} else if (part instanceof TLModule) {
			return TLModelNamingConvention.getModuleLabelKey((TLModule) part);
		} else if (part instanceof TLTypePart) {
			return TLModelNamingConvention.resourceKey((TLTypePart) part);
		}
		return ResKey.NONE;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
