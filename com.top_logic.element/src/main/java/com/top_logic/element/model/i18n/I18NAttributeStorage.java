/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.kbbased.storage.AssociationQueryBasedStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.StaticItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AssociationQueryBasedStorage} to store {@link ResKey}s.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NAttributeStorage<C extends I18NAttributeStorage.Config<?>> extends AssociationQueryBasedStorage<C> {

	/** KO type of I18N storage. */
	public static final String I18N_STORAGE_KO_TYPE = "I18NAttributeStorage";

	/** Name of the attribute storing the locale to which the translation belongs. */
	public static final String LANGUAGE_ATTRIBUTE_NAME = "lang";

	/** Name of the value attribute. */
	public static final String VALUE_ATTRIBUTE_NAME = "value";

	private AssociationSetQuery<? extends KnowledgeItem> _query;

	/**
	 * Creates a new {@link I18NAttributeStorage}.
	 */
	public I18NAttributeStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_query = createQuery(I18N_STORAGE_KO_TYPE, attribute, StaticItem.class);
	}

	@Override
	public ResKey getAttributeValue(TLObject owner, TLStructuredTypePart attribute) {
		Builder builder = ResKey.builder();
		boolean hasValue = false;
		for (KnowledgeItem i18nItem : fetchTranslations(owner)) {
			String value = getI18N(i18nItem);
			if (!StringServices.isEmpty(value)) {
				String locale = (String) i18nItem.getAttributeValue(LANGUAGE_ATTRIBUTE_NAME);
				builder.add(ResourcesModule.localeFromString(locale), value);
				hasValue = true;
			}
		}
		return hasValue ? builder.build() : null;
	}

	@Override
	protected void internalSetAttributeValue(TLObject owner, TLStructuredTypePart attribute, Object value) {
		boolean anyChanges = false;
		ResKey i18nValue = (ResKey) value;
		ResourcesModule resMod = ResourcesModule.getInstance();
		Set<String> languages = CollectionUtil.toSet(resMod.getSupportedLocaleNames());
		// The collection is copied to avoid ConcurrentModificationExceptions.
		List<KnowledgeItem> entries = new ArrayList<>(fetchTranslations(owner));
		for (KnowledgeItem item : entries) {
			String lang = (String) item.getAttributeValue(LANGUAGE_ATTRIBUTE_NAME);
			String string = ResKeyUtil.getTranslation(i18nValue, new Locale(lang));
			if (string != null) {
				if (anyChanges) {
					/* It is not necessary to check for change of i18N, because only the accumulated
					 * change state is required. */
					setI18N(item, string);
				} else {
					anyChanges |= updateI18N(item, string);
				}
				languages.remove(lang);
			}
			else {
				item.delete();
				anyChanges = true;
			}
		}

		for (String lang : languages) {
			String string = ResKeyUtil.getTranslation(i18nValue, new Locale(lang));
			if (string != null) {
				KnowledgeItem i18nItem = createI18nItem();
				i18nItem.setAttributeValue(OBJECT_ATTRIBUTE_NAME, owner.tHandle());
				i18nItem.setAttributeValue(META_ATTRIBUTE_ATTRIBUTE_NAME, attribute.tHandle());
				i18nItem.setAttributeValue(LANGUAGE_ATTRIBUTE_NAME, lang);
				setI18N(i18nItem, string);
				anyChanges = true;
			}
		}

		if (anyChanges) {
			/* As an updated attribute does not affect the TLObject itself, Lucene will not create a
			 * new index. Thats why the owner has to be touched. */
			owner.tTouch();
		}
	}

	private String getI18N(KnowledgeItem i18nItem) {
		return (String) i18nItem.getAttributeValue(VALUE_ATTRIBUTE_NAME);
	}

	private void setI18N(KnowledgeItem i18nItem, String newI18N) {
		i18nItem.setAttributeValue(VALUE_ATTRIBUTE_NAME, newI18N);
	}

	/**
	 * Updates the I18N of the given object.
	 * 
	 * @return Whether source code changed.
	 */
	private boolean updateI18N(KnowledgeItem i18N, String newI18N) {
		String oldI18N = getI18N(i18N);
		if (!Utils.equals(oldI18N, newI18N)) {
			setI18N(i18N, newI18N);
			return true;
		} else {
			return false;
		}
	}


	private KnowledgeItem createI18nItem() {
		return createItem(I18N_STORAGE_KO_TYPE);
	}

	private Collection<? extends KnowledgeItem> fetchTranslations(TLObject owner) {
		return resolveQuery(owner, _query);
	}

	@Override
	protected Class<?> getApplicationValueType() {
		return ResKey.class;
	}

}
