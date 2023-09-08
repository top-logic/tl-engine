/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.util.Utils;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.SimpleKBCache;
import com.top_logic.knowledge.service.db2.SimpleKBCache.InvalidCacheException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * Cache value of {@link PersistentLayoutsCache}.
 * 
 * <p>
 * This cache holds for each {@link Person#tId() person} a mapping from the name of a layout
 * configuration to the configuration itself. The <code>null</code> person means the layout
 * configuration mapping for the users which have no personal settings.
 * </p>
 * 
 * <p>
 * This represents actually a transient copy of the table
 * {@link PersistentLayoutWrapper#KO_NAME_LAYOUT_CONFIGURATIONS}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PersistentLayouts extends HashMap<ObjectKey, Map<String, LayoutComponent.Config>> {

	private static final Person GLOBAL_CONFIGS = null;

	private Map<String, Config> getGlobalConfigs() {
		return getConfigs(GLOBAL_CONFIGS);
	}

	private Map<String, Config> getConfigs(Person person) {
		return get(getKey(person));
	}

	private ObjectKey getKey(Person person) {
		if (person == GLOBAL_CONFIGS) {
			return null;
		}
		return person.tId();
	}

	Config getConfig(Person person, String layoutKey) {
		Map<String, Config> storedConfigs = getConfigs(person);
		if (storedConfigs != null && !storedConfigs.isEmpty()) {
			return storedConfigs.get(layoutKey);
		}
		Map<String, Config> globalConfigs = getGlobalConfigs();
		if (globalConfigs != null && !globalConfigs.isEmpty()) {
			return globalConfigs.get(layoutKey);
		}
		return null;
	}

	void handleCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		handleCreation(person(values), key(values), config(values));
	}

	void handleCreation(PersistentLayoutWrapper layout) {
		handleCreation(person(layout), key(layout), config(layout));
	}

	void handleCreation(ObjectKey person, String layoutKey, Config config) {
		Map<String, Config> configs = getOrCreate(person);
		configs.put(layoutKey, config);
	}

	void handleUpdate(ItemUpdate update) throws InvalidCacheException {
		Map<String, Object> newValues = update.getValues();
		Map<String, Object> oldValues = update.getOldValues();
		if (oldValues == null) {
			throw new SimpleKBCache.InvalidCacheException();
		}
		handleUpdate(person(oldValues), key(oldValues), person(newValues), key(newValues), config(newValues));
	}

	void handleUpdate(PersistentLayoutWrapper layout) {
		ObjectKey oldPerson =
			personKey((KnowledgeItem) layout.tHandle().getGlobalAttributeValue(PersistentLayoutWrapper.PERSON_ATTR));
		String oldKey = (String) layout.tHandle().getGlobalAttributeValue(PersistentLayoutWrapper.LAYOUT_KEY_ATTR);
		handleUpdate(oldPerson, oldKey, person(layout), key(layout), config(layout));
	}

	void handleUpdate(ObjectKey oldPerson, String oldKey, ObjectKey newPerson, String newKey, Config newConfig) {
		if (Utils.equals(oldPerson, newPerson)) {
			Map<String, Config> layouts = getOrCreate(newPerson);
			if (Utils.equals(oldKey, newKey)) {
				if (newConfig != null) {
					layouts.put(newKey, newConfig);
				} else {
					// no change at all.
				}
			} else {
				if (newConfig != null) {
					layouts.put(newKey, newConfig);
				} else {
					Config oldLayout = layouts.remove(oldKey);
					layouts.put(newKey, oldLayout);
				}
			}
		} else {
			Map<String, Config> oldLayouts = getOrCreate(oldPerson);
			Map<String, Config> newLayouts = getOrCreate(newPerson);
			Config oldLayout = oldLayouts.remove(oldKey);
			if (oldLayouts.isEmpty()) {
				// all personal layouts removed.
				remove(oldPerson);
			}
			if (newConfig != null) {
				newLayouts.put(newKey, newConfig);
			} else {
				newLayouts.put(newKey, oldLayout);
			}

		}
	}

	void handleDeletion(ItemDeletion deletion) {
		Map<String, Object> values = deletion.getValues();
		handleDeletion(person(values), key(values));
	}

	void handleDeletion(PersistentLayoutWrapper layout) {
		handleDeletion(person(layout), key(layout));
	}

	void handleDeletion(ObjectKey person, String layoutKey) {
		Map<String, Config> oldLayouts = getOrCreate(person);
		oldLayouts.remove(layoutKey);
		if (oldLayouts.isEmpty()) {
			// all personal layouts removed.
			remove(person);
		}
	}

	private Config config(Map<String, Object> values) {
		return (LayoutComponent.Config) values.get(PersistentLayoutWrapper.CONFIGURATION_ATTR);
	}

	private String key(Map<String, Object> values) {
		return (String) values.get(PersistentLayoutWrapper.LAYOUT_KEY_ATTR);
	}

	private ObjectKey person(Map<String, Object> values) {
		return (ObjectKey) values.get(PersistentLayoutWrapper.PERSON_ATTR);
	}

	private Config config(PersistentLayoutWrapper layout) {
		return layout.getConfiguration();
	}

	private String key(PersistentLayoutWrapper layout) {
		return layout.getLayoutKey();
	}

	private ObjectKey person(PersistentLayoutWrapper layout) {
		Person person = layout.getPerson();
		KnowledgeItem personKI;
		if (person == null) {
			personKI = null;
		} else {
			personKI = person.tHandle();
		}
		return personKey(personKI);
	}

	private ObjectKey personKey(KnowledgeItem person) {
		if (person != null) {
			return person.tId();
		} else {
			return null;
		}
	}

	private Map<String, Config> getOrCreate(ObjectKey person) {
		Map<String, Config> configs = get(person);
		if (configs == null) {
			configs = new HashMap<>();
			put(person, configs);
		}
		return configs;
	}

	void add(ObjectKey person, String layoutKey, Config layoutConfig) {
		getOrCreate(person).put(layoutKey, layoutConfig);
	}

	@Override
	public PersistentLayouts clone() {
		return (PersistentLayouts) super.clone();
	}

}

