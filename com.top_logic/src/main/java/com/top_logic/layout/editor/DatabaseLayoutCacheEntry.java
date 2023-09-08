/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutTemplateCall;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.LazyParsingTemplateCall;
import com.top_logic.mig.html.layout.PersistentLayoutWrapper;
import com.top_logic.mig.html.layout.PersistentTemplateLayoutWrapper;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * An entry of the {@link DatabaseLayoutCache}.
 * 
 * <p>
 * This cache contains the database layouts, the layouts configured in the application that have not
 * been exported to the filesystem, by {@link Person person} and {@link Theme theme}.
 * </p>
 * <p>
 * The person, owner of the layouts, could be <code>null</code>. This is used for global layouts,
 * users which have no personal settings.
 * </p>
 * <p>
 * All database layouts are cached for every choosable theme.
 * </p>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DatabaseLayoutCacheEntry {

	private static final Person GLOBAL = null;

	private final Map<MultiKey<?>, Map<String, TLLayout>> _layoutsByThemeAndPerson = Collections.synchronizedMap(new HashMap<>());

	private List<Theme> _uncachedThemes = new ArrayList<>();

	private ScheduledFuture<?> _loadLayoutsLazyAction;

	DatabaseLayoutCacheEntry(KnowledgeBase kb) {
		Collection<PersistentTemplateLayoutWrapper> layouts = getLayouts(kb);

		if (SchedulerService.Module.INSTANCE.isActive()) {
			initCacheLazy(layouts);
		} else {
			initCache(layouts);
		}
	}

	private DatabaseLayoutCacheEntry(DatabaseLayoutCacheEntry otherEntry) {
		_uncachedThemes = otherEntry._uncachedThemes;
		_loadLayoutsLazyAction = otherEntry._loadLayoutsLazyAction;

		for (Entry<MultiKey<?>, Map<String, TLLayout>> entry : otherEntry._layoutsByThemeAndPerson.entrySet()) {
			_layoutsByThemeAndPerson.put(entry.getKey(), Collections.synchronizedMap(new HashMap<>(entry.getValue())));
		}
	}

	private Collection<PersistentTemplateLayoutWrapper> getLayouts(KnowledgeBase kb) {
		return WrapperFactory.getWrappersForKOsGeneric(getLayoutObjects(kb));
	}

	private Collection<KnowledgeObject> getLayoutObjects(KnowledgeBase kb) {
		return kb.getAllKnowledgeObjects(PersistentTemplateLayoutWrapper.KO_NAME_TEMPLATE_LAYOUTS);
	}

	private List<Theme> getOrderedChoosableThemes() {
		return CollectionUtil.topsort(theme -> theme.getParentThemes(), getChoosableThemes(), false);
	}

	private Collection<Theme> getChoosableThemes() {
		return ThemeFactory.getInstance().getChoosableThemes();
	}

	private void initCache(Collection<PersistentTemplateLayoutWrapper> layouts) {
		putAll(Collections.emptySet(), getOrderedChoosableThemes(), layouts);
	}

	private void initCacheLazy(Collection<PersistentTemplateLayoutWrapper> layouts) {
		List<Theme> choosableThemes = getOrderedChoosableThemes();
		List<Theme> parentThemes = ThemeUtil.getAllParentThemes(ThemeFactory.getInstance().getDefaultTheme());

		parentThemes.retainAll(choosableThemes);
		choosableThemes.removeAll(parentThemes);

		putAll(Collections.emptySet(), parentThemes, layouts);
		putAllLazy(parentThemes, choosableThemes, layouts);
	}

	private void putAllLazy(List<Theme> cachedThemes, List<Theme> themesToCache,
			Collection<PersistentTemplateLayoutWrapper> layouts) {
		Runnable loadLayoutsLazy = () -> ThreadContextManager.inSystemInteraction(LayoutStorage.class, () -> {
			_uncachedThemes = Collections.emptyList();
			putAll(cachedThemes, themesToCache, layouts);
		});

		_uncachedThemes = themesToCache;
		_loadLayoutsLazyAction = SchedulerService.getInstance().schedule(loadLayoutsLazy, 10, TimeUnit.MILLISECONDS);
	}

	private void putAll(Collection<Theme> cachedThemes, List<Theme> themesToCache,
			Collection<PersistentTemplateLayoutWrapper> layouts) {
		for (PersistentTemplateLayoutWrapper layout : layouts) {
			put(cachedThemes, themesToCache, layout);
		}
	}

	private void put(Collection<Theme> cachedThemes, Collection<Theme> themesToCache, PersistentTemplateLayoutWrapper layout) {
		ObjectKey person = person(layout);
		String layoutKey = layout.getLayoutKey();
		String templateName = layout.getTemplate();
		String arguments = layout.getArguments();

		if (templateName != null && arguments != null) {
			Collection<TLLayout> cachedLayouts = getCachedLayouts(cachedThemes, person, layoutKey);

			themes:
			for (Theme theme : themesToCache) {
				TLLayout newLayout = createLayout(theme, layoutKey, templateName, arguments);

				for (TLLayout cachedLayout : cachedLayouts) {
					if (LayoutUtils.hasSameLayoutConfig(layoutKey, cachedLayout, newLayout)) {
						put(theme, person, layoutKey, cachedLayout);
						continue themes;
					}
				}

				put(theme, person, layoutKey, newLayout);
				cachedLayouts.add(newLayout);
			}
		}
	}

	private HashSet<TLLayout> getCachedLayouts(Collection<Theme> themes, ObjectKey personID, String layoutKey) {
		HashSet<TLLayout> cachedLayouts = new HashSet<>();

		for (Theme theme : themes) {
			cachedLayouts.add(get(theme, personID).get(layoutKey));
		}

		return cachedLayouts;
	}

	private static TLLayout createLayout(Theme theme, String layoutKey, String templateName, String arguments) {
		return ThemeFactory.getInstance().withTheme(theme, new Computation<TLLayout>() {
			@Override
			public TLLayout run() {
				return newLayout(layoutKey, templateName, arguments);
			}
		});
	}

	private Map<String, TLLayout> get(Theme theme, ObjectKey personID) {
		if (_uncachedThemes.contains(theme)) {
			try {
				_loadLayoutsLazyAction.get();
			} catch (InterruptedException | ExecutionException exception) {
				Logger.error("Layouts for " + _uncachedThemes + " could not be added to cache.", exception, this);
			}
		}

		return _layoutsByThemeAndPerson.get(new MultiKey<>(theme.getThemeID(), personID));
	}

	private ObjectKey getKey(Person person) {
		if (person == GLOBAL) {
			return null;
		}
		return person.tId();
	}

	/**
	 * Computes the {@link TLLayout} for the given {@link Person} with the given layout key.
	 * 
	 * @param theme
	 *        {@link Theme} containing the layout.
	 * @param person
	 *        The user owning the layout.
	 * @param layoutKey
	 *        The name of the layout to find.
	 * 
	 * @return Persistent template definition.
	 */
	public TLLayout getLayout(Theme theme, Person person, String layoutKey) {
		Map<String, TLLayout> storedConfigs = getLayoutsByKey(theme, person);
		if (storedConfigs != null) {
			return storedConfigs.get(layoutKey);
		}
		return null;
	}

	/**
	 * Computes all layout templates by a layout key for a specific {@link Person}.
	 * 
	 * Global layout templates are not contained.
	 */
	public Map<String, TLLayout> getLayoutsByKey(Theme theme, Person person) {
		return get(theme, getKey(person));
	}

	void handleCreation(PersistentTemplateLayoutWrapper layout) {
		put(Collections.emptySet(), getChoosableThemes(), layout);
	}

	void handleUpdate(PersistentTemplateLayoutWrapper layout) {
		KnowledgeObject handle = layout.tHandle();
		ObjectKey oldPerson =
			personKey((KnowledgeItem) handle.getGlobalAttributeValue(PersistentLayoutWrapper.PERSON_ATTR));
		String oldKey = (String) handle.getGlobalAttributeValue(PersistentLayoutWrapper.LAYOUT_KEY_ATTR);
		handleUpdate(oldPerson, oldKey, person(layout), key(layout), layout);
	}

	void handleUpdate(ObjectKey oldPerson, String oldKey, ObjectKey newPerson, String newKey,
			PersistentTemplateLayoutWrapper template) {
		Collection<Theme> choosableThemes = getChoosableThemes();

		if (Utils.equals(oldPerson, newPerson)) {
			if (!Utils.equals(oldKey, newKey)) {
				for (Theme theme : choosableThemes) {
					Map<String, TLLayout> layouts = getOrCreate(theme, newPerson);
					layouts.remove(oldKey);
				}

				put(Collections.emptySet(), choosableThemes, template);
			} else {
				put(Collections.emptySet(), choosableThemes, template);
			}
		} else {
			// At least, person has changed.
			for (Theme theme : choosableThemes) {
				Map<String, TLLayout> oldLayouts = getOrCreate(theme, oldPerson);
				oldLayouts.remove(oldKey);
			}

			put(Collections.emptySet(), choosableThemes, template);
		}
	}

	void handleDeletion(ItemDeletion deletion) {
		Map<String, Object> values = deletion.getValues();
		handleDeletion(person(values), key(values));
	}

	void handleDeletion(PersistentTemplateLayoutWrapper deletion) {
		handleDeletion(person(deletion), key(deletion));
	}

	void handleDeletion(ObjectKey person, String layoutKey) {
		for (Theme theme : getChoosableThemes()) {
			Map<String, TLLayout> oldLayouts = getOrCreate(theme, person);
			oldLayouts.remove(layoutKey);
		}
	}

	static TLLayout newLayout(String layoutKey, String templateName, String arguments) {
		try {
			ConfigurationItem args =
				LayoutTemplateUtils.getDeserializedTemplateArguments(layoutKey, templateName, arguments);
			return new LayoutTemplateCall(templateName, args, layoutKey);
		} catch (ConfigurationException ex) {
			return new LazyParsingTemplateCall(templateName,
				new ComputationEx<ConfigurationItem, ConfigurationException>() {
					
					@Override
					public ConfigurationItem run() throws ConfigurationException {
							return LayoutTemplateUtils.getDeserializedTemplateArguments(layoutKey, templateName, arguments);
					}
					},
				layoutKey);
		}
	}

	private String key(Map<String, Object> values) {
		return (String) values.get(PersistentTemplateLayoutWrapper.LAYOUT_KEY_ATTR);
	}

	private ObjectKey person(Map<String, Object> values) {
		return (ObjectKey) values.get(PersistentTemplateLayoutWrapper.PERSON_ATTR);
	}

	private String key(PersistentTemplateLayoutWrapper layout) {
		return layout.getLayoutKey();
	}

	private ObjectKey person(PersistentTemplateLayoutWrapper layout) {
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

	private Map<String, TLLayout> getOrCreate(Theme theme, ObjectKey personID) {
		Map<String, TLLayout> layouts = get(theme, personID);

		if (layouts == null) {
			layouts = Collections.synchronizedMap(new HashMap<>());
			putInternal(theme.getThemeID(), personID, layouts);
		}
		return layouts;
	}

	private void putInternal(String themeID, ObjectKey personID, Map<String, TLLayout> layouts) {
		_layoutsByThemeAndPerson.put(new MultiKey<>(themeID, personID), layouts);
	}

	void put(Theme theme, ObjectKey personID, String layoutKey, TLLayout layout) {
		getOrCreate(theme, personID).put(layoutKey, layout);
	}

	/**
	 * Creates a copy of the current cache entry.
	 */
	public DatabaseLayoutCacheEntry copy() {
		return new DatabaseLayoutCacheEntry(this);
	}
}
