/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.Resources;

/**
 * Utilities for the meta model.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLMetaModelUtil {

	/**
	 * Stores the I18N for an {@link TLNamedPart} into the dynamic application resource files.
	 * 
	 * @param part
	 *        The {@link TLNamedPart}.
	 * @param i18n
	 *        The localization entries.
	 */
	public static void saveI18NForPart(TLNamedPart part, Internationalized i18n, ResourceTransaction tx) {
		boolean autoTranslate = PersonalConfiguration.getPersonalConfiguration().getAutoTranslate();

		if (part instanceof TLModule) {
			storeInternationalized(i18n, getResKey(part), part.getName(), tx, name -> {
				return CodeUtil.englishLabel(TLModelUtil.getLocalName(name));
			}, autoTranslate);
		} else {
			storeInternationalized(i18n, getResKey(part), part.getName(), tx, CodeUtil::englishLabel, autoTranslate);
		}
	}

	/**
	 * Stores the I18N for the given {@link ResKey}.
	 */
	public static void saveI18N(Internationalized i18n, ResKey key, String name, ResourceTransaction tx) {
		boolean autoTranslate = PersonalConfiguration.getPersonalConfiguration().getAutoTranslate();

		storeInternationalized(i18n, key, name, tx, CodeUtil::englishLabel, autoTranslate);
	}

	/**
	 * Like {@link #saveI18NForPart(TLNamedPart, Internationalized, ResourceTransaction)} but uses
	 * the technical name directly as label, if no translation is given.
	 */
	public static void saveI18NForPartNoLabelHeuristic(TLNamedPart part, Internationalized i18n,
			ResourceTransaction tx) {
		storeInternationalized(i18n, getResKey(part), part.getName(), tx, name -> name, false);
	}

	private static void storeInternationalized(Internationalized i18n, ResKey key, String technicalName,
			ResourceTransaction tx, Function<String, String> labelHeuristic, boolean autoTranslate) {

		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			Resources bundle = Resources.getInstance(locale);

			ResKey labelKey = i18n.getLabel();
			String label = labelKey == null ? null
				: labelKey.isLiteral() ? ((LiteralKey) labelKey).getTranslationWithoutFallbacks(locale)
					: StringServices.nonEmpty(bundle.getString(labelKey, null));

			ResKey descriptionKey = i18n.getDescription();
			String description = descriptionKey == null ? null
				: descriptionKey.isLiteral() ? ((LiteralKey) descriptionKey).getTranslationWithoutFallbacks(locale)
					: StringServices.nonEmpty(bundle.getString(descriptionKey, null));

			if (label == null) {
				label = defaultTranslation(locale, labelKey, technicalName, labelHeuristic, autoTranslate);
			}

			if (description == null) {
				description = defaultTranslation(locale, descriptionKey, null, labelHeuristic, autoTranslate);
			}

			tx.saveI18N(locale, key, label);
			tx.saveI18N(locale, key.tooltip(), description);
		}
	}

	private static String defaultTranslation(Locale locale, ResKey labelKey, String technicalName,
			Function<String, String> labelHeuristic, boolean autoTranslate) {
		if (autoTranslate) {
			Translator service = TranslationService.getInstance();
			if (service.isSupported(locale)) {
				for (Locale other : ResourcesModule.getInstance().getSupportedLocales()) {
					if (service.isSupported(other)) {
						String otherLabel =
							StringServices.nonEmpty(Resources.getInstance(other).getString(labelKey, null));
						if (otherLabel != null) {
							return service.translate(otherLabel, other, locale);
						}
					}
				}

				if (technicalName != null) {
					return service.translate(labelHeuristic.apply(technicalName), Locale.ENGLISH, locale);
				}
			}
		}

		if (technicalName != null) {
			return labelHeuristic.apply(technicalName);
		}

		return null;
	}

	private static ResKey getResKey(TLNamedPart part) {
		if (part instanceof TLType) {
			return TLModelNamingConvention.getTypeLabelKey((TLType) part);
		} else if (part instanceof TLModule) {
			return TLModelNamingConvention.getModuleLabelKey((TLModule) part);
		} else if (part instanceof TLTypePart) {
			return TLModelNamingConvention.resourceKey((TLTypePart) part);
		} else {
			return ResKey.NONE;
		}
	}

	/**
	 * Computes all {@link TLModelPart}s destroying the model consistency when deleting the given
	 * model part.
	 * 
	 * @see TLModelPartDeletionChecker
	 */
	public static Set<Pair<TLModelPart, ResKey>> getDeleteConflictingModelParts(TLModelPart modelPart) {
		TLModelPartDeletionChecker deletionChecker = new TLModelPartDeletionChecker(Collections.singleton(modelPart));

		modelPart.visit(deletionChecker, null);

		return deletionChecker.getDeleteConflictingModelParts();
	}

	/**
	 * {@link ReferenceKind#FORWARDS} if the {@link TLReference} is forward, otherwise
	 * {@link ReferenceKind#BACKWARDS}.
	 * 
	 * @see TLModelUtil#isForwardReference(TLReference)
	 */
	public static ReferenceKind getReferenceKind(TLReference reference) {
		return TLModelUtil.isForwardReference(reference) ? ReferenceKind.FORWARDS : ReferenceKind.BACKWARDS;
	}

}
