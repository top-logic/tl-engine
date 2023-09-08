/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleKBCache;

/**
 * {@link SimpleKBCache} used by currency to store all exchange rates.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ExchangeRateCache extends SimpleKBCache<SortedMap<Object, KnowledgeAssociation>> {

	private final KnowledgeObject _currency;

	ExchangeRateCache(KnowledgeObject currency) {
		_currency = currency;
	}

	@Override
	protected DBKnowledgeBase kb() {
		return (DBKnowledgeBase) _currency.getKnowledgeBase();
	}

	@Override
	protected SortedMap<Object, KnowledgeAssociation> copy(SortedMap<Object, KnowledgeAssociation> cacheValue) {
		return new TreeMap<>(cacheValue);
	}

	@Override
	protected SortedMap<Object, KnowledgeAssociation> handleEvent(SortedMap<Object, KnowledgeAssociation> value,
			UpdateEvent event, boolean copyOnChange) throws InvalidCacheException {
		boolean changed = false;
		ChangeSet changes = event.getChanges();
		KnowledgeBase sender = event.getKnowledgeBase();
		for (ObjectCreation creation : changes.getCreations()) {
			if (!isExchange(creation)) {
				continue;
			}
			if (!correctReference(creation)) {
				continue;
			}
			KnowledgeItem item = sender.resolveObjectKey(creation.getObjectId().toCurrentObjectKey());
			Object dateValue = dateValue(item);
			if (dateValue != null) {
				if (copyOnChange && !changed) {
					value = copy(value);
				}
				changed = true;
				value.put(dateValue, (KnowledgeAssociation) item);
			}
		}
		for (ItemUpdate update : changes.getUpdates()) {
			if (!isExchange(update)) {
				continue;
			}
			if (update.getOldValues().containsKey(Currency.DATE)) {
				// Check exchange for represented currency
				KnowledgeItem changedItem = sender.resolveObjectKey(update.getObjectId().toCurrentObjectKey());
				if (!correctReference(changedItem)) {
					continue;
				}
				if (copyOnChange && !changed) {
					value = copy(value);
				}
				changed = true;
				value.remove(update.getOldValues().get(Currency.DATE));
				value.put(update.getValues().get(Currency.DATE), (KnowledgeAssociation) changedItem);
			}
		}
		for (ItemDeletion deletion : changes.getDeletions()) {
			if (deletion.getObjectId().equals(ObjectBranchId.toObjectBranchId(_currency.tId()))) {
				throw new InvalidCacheException();
			}
			if (!isExchange(deletion)) {
				continue;
			}
			if (!correctReference(deletion)) {
				continue;
			}
			if (deletion.getValues().containsKey(Currency.DATE)) {
				if (copyOnChange && !changed) {
					value = copy(value);
				}
				changed = true;
				value.remove(deletion.getValues().get(Currency.DATE));
			}
		}
		if (changed) {
			return value;
		}
		return null;
	}

	@Override
	protected void handleChanges(SortedMap<Object, KnowledgeAssociation> cacheValue,
			Collection<? extends KnowledgeItem> changedObjects) {
		for (KnowledgeItem item : changedObjects) {
			if (!isExchange(item)) {
				continue;
			}
			assert noSourceChange(item) : "Associations does not change sources.";
			if (!correctReference(item)) {
				// Exchange rate for a different currency
				return;
			}
			Object oldValue;
			Object newValue;
			try {
				oldValue = item.getGlobalAttributeValue(Currency.DATE);
				newValue = item.getAttributeValue(Currency.DATE);
			} catch (NoSuchAttributeException ex) {
				throw new RuntimeException(ex);
			}
			if (!Utils.equals(oldValue, newValue)) {
				cacheValue.remove(oldValue);
				cacheValue.put(newValue, (KnowledgeAssociation) item);
			}
		}
	}

	private boolean noSourceChange(KnowledgeItem item) {
		try {
			Object localSource = item.getAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
			Object globalSource = item.getGlobalAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
			return localSource == globalSource;
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	@Override
	protected void handleChange(SortedMap<Object, KnowledgeAssociation> cacheValue, KnowledgeItem item,
			String attributeName, Object oldValue, Object newValue) {
		if (!isExchange(item)) {
			return;
		}
		assert !attributeName.equals(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME) : "Associations does not change sources.";
		if (!attributeName.equals(Currency.DATE)) {
			return;
		}
		if (!correctReference(item)) {
			// Exchange rate for a different currency
			return;
		}
		if (oldValue != null) {
			cacheValue.remove(oldValue);
		}
		if (newValue != null) {
			cacheValue.put(newValue, (KnowledgeAssociation) item);
		}
	}

	@Override
	protected void handleDeletion(SortedMap<Object, KnowledgeAssociation> localCacheValue, KnowledgeItem item) throws InvalidCacheException{
		if (item == _currency) {
			throw new InvalidCacheException();
		}
		if (!isExchange(item)) {
			return;
		}
		if (!correctReference(item)) {
			// Exchange rate for a different currency
			return;
		}
		Object date = dateValue(item);
		if (date != null) {
			localCacheValue.remove(date);
		}
	}

	@Override
	protected void handleCreation(SortedMap<Object, KnowledgeAssociation> localCacheValue, KnowledgeItem item) {
		if (!isExchange(item)) {
			return;
		}
		if (!correctReference(item)) {
			// Exchange rate for a different currency
			return;
		}
		Object date = dateValue(item);
		if (date != null) {
			localCacheValue.put(date, (KnowledgeAssociation) item);
		}
	}

	@Override
	protected SortedMap<Object, KnowledgeAssociation> newLocalCacheValue() {
		return toTreeMap(_currency.getOutgoingAssociations(Currency.HAS_EXCHANGE));
	}

	private TreeMap<Object, KnowledgeAssociation> toTreeMap(Iterator<? extends KnowledgeAssociation> exchanges) {
		TreeMap<Object, KnowledgeAssociation> exchangeRates = new TreeMap<>();
		while (exchanges.hasNext()) {
			KnowledgeAssociation ass = exchanges.next();
			exchangeRates.put(dateValue(ass), ass);
		}
		return exchangeRates;
	}

	private boolean isExchange(KnowledgeItem item) {
		return isExchangeType(item.tTable());
	}

	private boolean correctReference(KnowledgeItem item) throws UnreachableAssertion {
		MOReference sourceKey;
		try {
			sourceKey =
				MetaObjectUtils.getReference(item.tTable(), DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
		return _currency.tId().equals(item.getReferencedKey(sourceKey));
	}

	private boolean isExchangeType(MetaObject metaObject) {
		return metaObject.getName().equals(Currency.HAS_EXCHANGE);
	}

	private boolean isExchange(ItemEvent itemEvent) {
		return isExchangeType(itemEvent.getObjectType());
	}

	private boolean correctReference(ItemChange change) {
		Object reference = change.getValues().get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		return _currency.tId().equals(reference);
	}

	private Object dateValue(KnowledgeItem item) {
		try {
			return item.getAttributeValue(Currency.DATE);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

}
