/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTreeView;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.TLContext;

/**
 * A Layout that that will care for Bound security.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class BoundMainLayout extends MainLayout implements LayoutContainerBoundChecker {

	/**
	 * Configuration for the {@link BoundMainLayout}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends MainLayout.Config, BoundCheckerLayoutConfig {

		// Sum interface

	}
    
	/** Constant to use with <code>writeChangeJS()</code>. */
    public static final boolean RESET = true;
    
    /** Constant for putting the BoundMainLayout into TLContext. */
	public static final Property<BoundChecker> ROOT_CHECKER = TypedAnnotatable.property(BoundChecker.class, "_root_checker_");

    /** Construct a BoundMainLayout from (XML-)Attributes. */
    public BoundMainLayout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    /**
     * Store BoundMainLayout in TLContext to use it as root BoundChecker
     *  
     * @see com.top_logic.mig.html.layout.LayoutComponent#componentsResolved(InstantiationContext)
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
    	// KBU: layout is put in context before calling
    	// super method which calls setVisible
    	// which in turn might cause inits that need
    	// the root checker (bound main layout) to perform
    	TLContext ctx = TLContext.getContext(); 
    	if(ctx != null) {
    	    ctx.set(ROOT_CHECKER, this);
    	}

    	super.componentsResolved(context);
    }
    
	@Override
	public ResKey hideReason() {
		// The top-level component cannot be hidden.
		return null;
	}

    /**
     * Call this method to setup the Persitency for all subcomponenets.
     * 
     * @return the number of componenets initializes, anything &gt; 0 implies that
     *         you must commit the given Knlwoedgebase.
     */
    public int initBoundComponents(KnowledgeBase kBase) {
		return initPersBoundComps(kBase, Collections.singletonList(getConfig()));
    }

	/**
	 * Call this method to setup the persistence for all subcomponents.
	 * 
	 * @return The number of newly initialised components; anything &gt; 0 implies that you must
	 *         commit the given {@link KnowledgeBase}.
	 */
	public static int initPersBoundComps(KnowledgeBase kb, List<LayoutComponent.Config> configs) {
		Protocol protocol = new LogProtocol(BoundMainLayout.class);
		InitPersBoundCompVisitor visitor = new InitPersBoundCompVisitor(kb, configs);
		visitor.visit(protocol);
		protocol.checkErrors();
		return visitor.getCount();
	}

	private static class InitPersBoundCompVisitor {

		private final KnowledgeBase _kb;

		private final Map<String, LayoutComponent.Config> _configById = new HashMap<>();

		private final Map<ComponentName, CompoundSecurityLayout.Config> _securityLayoutByName = new HashMap<>();

		private int _count;

		private Location _rootLocation;

		private ComponentName _nextSecurityParent;

		private List<LayoutComponent.Config> _roots;

		public InitPersBoundCompVisitor(KnowledgeBase kBase, List<LayoutComponent.Config> roots) {
			_kb = kBase;
			_roots = roots;
		}

		public void visit(Log log) {
			_roots.forEach(this::collectSecurityLayouts);
			createPersBoundComps(log);
			for (LayoutComponent.Config root : _roots) {
				_rootLocation = root.location();
				_configById.clear();
				visit(log, root);
			}
		}

		private void createPersBoundComps(Log log) {
			/* Security is only configured for CompoundSecurityLayout's. Therefore PersBoundComp's
			 * must only be created for those components. */
			for (CompoundSecurityLayout.Config conf : _securityLayoutByName.values()) {
				ComponentName persBoundCompName = persBoundCompId(log, conf);
				if (!conf.getName().equals(persBoundCompName)) {
					/* Security is delegated to different component. Do not create PersBoundComp. */
					continue;
				}
				if (SecurityComponentCache.getSecurityComponent(persBoundCompName) == null) {
					PersBoundComp.createInstance(_kb, persBoundCompName);
					_count++;
				}

			}
		}

		private ComponentName persBoundCompId(Log log, CompoundSecurityLayout.Config secLayout) {
			ComponentName securityId = secLayout.getSecurityId();
			if (securityId == null) {
				return secLayout.getName();
			}
			CompoundSecurityLayout.Config delegateConf = _securityLayoutByName.get(securityId);
			if (delegateConf == null) {
				log.error("No security layout with id '" + securityId + "' found in '" + secLayout + "'.");
				return null;
			}
			return persBoundCompId(log, delegateConf);
		}

		private void collectSecurityLayouts(LayoutComponent.Config root) {
			DescendantDFSIterator<LayoutComponent.Config> it =
				new DescendantDFSIterator<>(LayoutConfigTreeView.INSTANCE, root, true);
			while(it.hasNext()) {
				LayoutComponent.Config next = it.next();
				if (isSecurityConfig(next) ) {
					_securityLayoutByName.put(next.getName(), (CompoundSecurityLayout.Config) next);
				}
			}

		}

		private void visit(Log log, LayoutComponent.Config config) {
			ComponentName before = _nextSecurityParent;
			if (isSecurityConfig(config)) {
				_nextSecurityParent = persBoundCompId(log, (CompoundSecurityLayout.Config) config);
			}
			try {
				doWork(log, config);
				for (Iterator<? extends LayoutComponent.Config> it =
					LayoutConfigTreeView.INSTANCE.getChildIterator(config); it.hasNext();) {
					visit(log, it.next());
				}
			} finally {
				_nextSecurityParent = before;
			}
		}

		int getCount() {
			return _count;
		}

		private void doWork(Log log, LayoutComponent.Config config) {
			if (!(config instanceof BoundCheckerLayoutConfig)) {
				return;
			}
			ComponentName componentName = config.getName();
			String lowercaseId = componentName.qualifiedName().toLowerCase();
			LayoutComponent.Config prevComp = _configById.get(lowercaseId);
			if (_configById.containsKey(lowercaseId)) {
				logDuplicateComponent(log, prevComp, config);
				return;
			}
			_configById.put(lowercaseId, config);
			
			registerDefaultTypes(log, config);
		}

		private boolean isSecurityConfig(LayoutComponent.Config config) {
			return config instanceof CompoundSecurityLayout.Config
					&& !LayoutConstants.isSyntheticName(config.getName());
		}

		private static void logDuplicateComponent(Log log, LayoutComponent.Config formerConfig,
				LayoutComponent.Config config) {
			StringBuilder duplicateComponentName = new StringBuilder();
			duplicateComponentName.append("Duplicate components '");
			duplicateComponentName.append(config.getName());
			duplicateComponentName.append("'");
			duplicateComponentName.append(" in ");
			duplicateComponentName.append(config.location());
			duplicateComponentName.append(" <-> ");
			duplicateComponentName.append("'");
			duplicateComponentName.append(formerConfig.getName());
			duplicateComponentName.append("'");
			duplicateComponentName.append(" in ");
			duplicateComponentName.append(formerConfig.location());
			duplicateComponentName.append(" for security. Check layout and case of names.");
			log.error(duplicateComponentName.toString());
		}

		/**
		 * Check the "defaultFor" config and register the surrounding security layout as
		 * "defaultFor" for the found ko/me types in the {@link BoundHelper}
		 * 
		 * @see BoundHelper#getDefaultChecker(MainLayout rootLayout, BoundObject anObject)
		 */
		private void registerDefaultTypes(Log log, LayoutComponent.Config config) {
			List<String> defaultForTypes = config.getDefaultFor();
			if (defaultForTypes.isEmpty()) {
				return;
			}
			if (_nextSecurityParent == null) {
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("No security layout found for '");
				errMsg.append(config.getName());
				errMsg.append("' in ");
				errMsg.append(config.location());
				errMsg.append(". Can not register defaultFor for types: ");
				errMsg.append(defaultForTypes);
				log.error(errMsg.toString());
				return;
			}
			defaultForTypes.forEach(type -> BoundHelper.getInstance()
				.registerPersBoundCheckerFor(_rootLocation, _nextSecurityParent, type));
		}

	}
 
}
