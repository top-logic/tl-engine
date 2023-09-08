/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Collection;
import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.gui.Theme;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeItem.State;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBCache;
import com.top_logic.knowledge.service.db2.SimpleKBCache;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutCache;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.PersistentLayoutWrapper;
import com.top_logic.mig.html.layout.PersistentTemplateLayoutWrapper;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.model.TLObject;

/**
 * A {@link KBCache} based cache for database layouts.
 * 
 * <p>
 * The {@link LayoutStorage} uses the {@link DatabaseLayoutCache database layout cache} beside the
 * filesystem layout cache to provide the layouts to the user.
 * </p>
 * 
 * @see LayoutCache
 * @see DatabaseLayoutCacheEntry
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DatabaseLayoutCache extends SimpleKBCache<DatabaseLayoutCacheEntry> implements LayoutCache {

	private final DBKnowledgeBase _kb;

	private final MetaObject _layoutType;

	/**
	 * Creates a {@link DatabaseLayoutCache}.
	 */
	public DatabaseLayoutCache(KnowledgeBase kb) {
		_kb = (DBKnowledgeBase) kb;
		_layoutType = kb.getMORepository().getMetaObject(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
	}

	@Override
	protected DatabaseLayoutCacheEntry handleEvent(DatabaseLayoutCacheEntry cacheValue, UpdateEvent event,
			boolean copyOnChange)
			throws SimpleKBCache.InvalidCacheException {
		boolean copied = false;
		ChangeSet changes = event.getChanges();
		for (KnowledgeItem created : event.getCreatedObjects().values()) {
			if (!isLayoutEvent(created)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleCreation(created.getWrapper());
		}
		for (KnowledgeItem update : event.getUpdatedObjects().values()) {
			if (!isLayoutEvent(update)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleUpdate(update.getWrapper());
		}
		for (ItemDeletion deletion : changes.getDeletions()) {
			if (!isLayoutEvent(deletion)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleDeletion(deletion);
		}
		if (copied) {
			return cacheValue;
		}
		return null;
	}

	private boolean isLayoutEvent(ItemEvent event) {
		return isLayoutEvent(event.getObjectType());
	}

	private boolean isLayoutEvent(MetaObject objectType) {
		return _layoutType.equals(objectType);
	}

	private boolean isLayoutEvent(KnowledgeItem item) {
		return isLayoutEvent(item.tTable());
	}

	@Override
	protected void handleChanges(DatabaseLayoutCacheEntry cacheValue,
			Collection<? extends KnowledgeItem> changedObjects)
			throws SimpleKBCache.InvalidCacheException {
		for (KnowledgeItem item : changedObjects) {
			if (!isLayoutEvent(item)) {
				continue;
			}
			cacheValue.handleUpdate(asLayoutWrapper(item));
		}
	}

	@Override
	protected void handleChange(DatabaseLayoutCacheEntry cacheValue, KnowledgeItem item, String attributeName,
			Object oldValue, Object newValue) throws InvalidCacheException {
		if (isLayoutEvent(item)) {
			PersistentTemplateLayoutWrapper template = asLayoutWrapper(item);

			String newKey;
			ObjectKey newPerson;
			String oldKey;
			ObjectKey oldPerson;

			if (attributeName.equals(PersistentLayoutWrapper.PERSON_ATTR)) {
				oldPerson = asId((KnowledgeItem) oldValue);
				newPerson = asId((KnowledgeItem) newValue);
			} else {
				oldPerson = newPerson = asId(template.getPerson());
			}

			if (attributeName.equals(PersistentLayoutWrapper.LAYOUT_KEY_ATTR)) {
				oldKey = asKey(oldValue);
				newKey = asKey(newValue);
			} else {
				oldKey = newKey = template.getLayoutKey();
			}

			cacheValue.handleUpdate(oldPerson, oldKey, newPerson, newKey, template);
		}
	}

	private String asKey(Object value) {
		return (String) value;
	}

	private static ObjectKey asId(TLObject obj) {
		return obj == null ? null : asId(obj.tHandle());
	}

	private static ObjectKey asId(KnowledgeItem value) {
		return value == null ? null : value.tId();
	}

	private static PersistentTemplateLayoutWrapper asLayoutWrapper(KnowledgeItem item) {
		return item.getWrapper();
	}

	@Override
	protected void handleCreation(DatabaseLayoutCacheEntry localCacheValue, KnowledgeItem item)
			throws SimpleKBCache.InvalidCacheException {
		if (!isLayoutEvent(item)) {
			return;
		}
		localCacheValue.handleCreation(asLayoutWrapper(item));
	}

	@Override
	protected void handleDeletion(DatabaseLayoutCacheEntry localCacheValue, KnowledgeItem item)
			throws SimpleKBCache.InvalidCacheException {
		if (!isLayoutEvent(item)) {
			return;
		}
		if (item.getState() == State.NEW) {
			localCacheValue.handleDeletion(asLayoutWrapper(item));
		} else {
			kb().withoutModifications(() -> {
				localCacheValue.handleDeletion(asLayoutWrapper(item));
				return null;
			});
		}
	}

	@Override
	protected DatabaseLayoutCacheEntry copy(DatabaseLayoutCacheEntry cacheValue) {
		return cacheValue.copy();
	}

	@Override
	protected DBKnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected DatabaseLayoutCacheEntry newLocalCacheValue() {
		return new DatabaseLayoutCacheEntry(_kb);
	}

	@Override
	public TLLayout getLayout(Theme theme, Person person, String layoutKey) {
		return getValue().getLayout(theme, person, layoutKey);
	}

	@Override
	public void putLayout(Theme theme, Person person, String layoutKey, TLLayout layout) {
		throw new UnsupportedOperationException(
			"Storage layout cache is a KB cache and is kept synchronous with the database content. Adding a layout to the database also adds it to the cache.");
	}

	@Override
	public void removeLayout(Theme theme, Person person, String layoutKey) {
		throw new UnsupportedOperationException(
			"Storage layout cache is a KB cache and is kept synchronous with the database content. Removing a layout from the database also removes it from the cache.");
	}

	@Override
	public Map<String, TLLayout> getLayouts(Theme theme, Person person) {
		return getValue().getLayoutsByKey(theme, person);
	}

}
