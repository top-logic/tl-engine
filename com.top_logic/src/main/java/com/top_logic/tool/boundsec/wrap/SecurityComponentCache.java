/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.BoundChecker;

/**
 * Cache for {@link PersBoundComp} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityComponentCache extends KBBasedManagedClass<SecurityComponentCache.Config> {

	/**
	 * Configuration of the {@link SecurityComponentCache}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends KBBasedManagedClass.Config<SecurityComponentCache> {
		// No special properties here
	}

	/**
	 * Cache of {@link PersBoundComp} by their security IDs.
	 */
	private Map<ComponentName, PersBoundComp> _cache;

	/**
	 * Creates a new {@link SecurityComponentCache} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SecurityComponentCache}.
	 */
	public SecurityComponentCache(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		initCache();
	}

	/**
	 * Initializes or updates the cache.
	 */
	public static void setupCache() {
		instance().initCache();
	}

	private void initCache() {
		_cache = createCache();
	}

	private void removeCache() {
		_cache = null;
	}

	/**
	 * Drops and disables the cache.
	 */
	public static void disableCache() {
		instance().removeCache();
	}

	private Map<ComponentName, PersBoundComp> createCache() {
		Collection<KnowledgeObject> securityComponentKOs = kb().getAllKnowledgeObjects(PersBoundComp.OBJECT_NAME);
		
		Map<ComponentName, PersBoundComp> newCache = MapUtil.newMap(securityComponentKOs.size());
		for (KnowledgeObject securityComponentKO : securityComponentKOs) {
			try {
				PersBoundComp securityComponent = securityComponentKO.getWrapper();
				ComponentName securityId = securityComponent.getIdentifier();
				
				newCache.put(securityId, securityComponent);
			} catch (Exception ex) {
				Logger.error("Failed to get PersBoundComp for '" + securityComponentKO + "'", ex,
					SecurityComponentCache.class);
			}
		}
		return newCache;
	}

	/**
	 * Searches for the {@link PersBoundComp} for the given {@link BoundChecker}.
	 * 
	 * @param boundChecker
	 *        The {@link BoundChecker} to {@link PersBoundComp} for.
	 * 
	 * @return May be <code>null</code>, when there is no {@link PersBoundComp} for the given
	 *         {@link BoundChecker}.
	 */
	public static PersBoundComp lookupPersBoundComp(BoundChecker boundChecker) {
		ComponentName theSecID = boundChecker.getSecurityId();
		if (theSecID != null && !LayoutConstants.isSyntheticName(theSecID)) {
			try {
				return getSecurityComponent(theSecID);
			} catch (Exception e) {
				Logger.error("failed to setupPersBoundComp '" + theSecID + "'", e, SecurityComponentCache.class);
			}
		}
		return null;
	}

	/**
	 * The {@link PersBoundComp} for the given layout configuration.
	 */
	public static PersBoundComp getSecurityComponent(LayoutComponent.Config config) {
		return getSecurityComponent(config.getName());
	}

	/**
	 * The {@link PersBoundComp} for the given {@link ComponentName}.
	 * @return The security component with the given identifier, or <code>null</code>, if there is
	 *         no such component.
	 */
	public static PersBoundComp getSecurityComponent(ComponentName name) {
		Map<ComponentName, PersBoundComp> cache = instance()._cache;
		if (cache == null) {
			return PersBoundComp.getInstance(instance().kb(), name);
		} else {
			return cache.get(name);
		}
	}

	/**
	 * All {@link PersBoundComp}s in the system.
	 */
	public static Collection<PersBoundComp> getAllSecurityComponents() {
		Map<ComponentName, PersBoundComp> cache = instance()._cache;
		if (cache == null) {
			KnowledgeBase kb = instance().kb();
			Collection<KnowledgeObject> allPersBoundComps = kb.getAllKnowledgeObjects(PersBoundComp.OBJECT_NAME);
			return WrapperFactory.getWrappersForKOs(PersBoundComp.class, allPersBoundComps);
		} else {
			return Collections.unmodifiableCollection(cache.values());
		}
	}

	private static final SecurityComponentCache instance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference for {@link SecurityComponentCache} service.
	 */
	public static final class Module extends TypedRuntimeModule<SecurityComponentCache> {

		/**
		 * Singleton {@link SecurityComponentCache.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SecurityComponentCache> getImplementation() {
			return SecurityComponentCache.class;
		}

	}

}
