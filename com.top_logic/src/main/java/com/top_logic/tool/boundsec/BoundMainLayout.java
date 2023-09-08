/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

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
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.TLContext;

/**
 * A Layout that that will care for Bound security.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class BoundMainLayout extends MainLayout implements BoundCheckerDelegate {

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

	private BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

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
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
    }

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

    /**
     * Call this method to setup the Persitency for all subcomponenets.
     * 
     * @return the number of componenets initializes, anything &gt; 0 implies that
     *         you must commit the given Knlwoedgebase.
     */
    public int initBoundComponents(KnowledgeBase kBase) {
		return initPersBoundComp(kBase, getConfig());
    }

	/**
	 * Call this method to setup the persistence for all subcomponents.
	 * 
	 * @return The number of newly initialised components; anything &gt; 0 implies that you must
	 *         commit the given {@link KnowledgeBase}.
	 */
	public static int initPersBoundComp(KnowledgeBase kb, LayoutComponent.Config config) {
		Protocol protocol = new LogProtocol(BoundMainLayout.class);
		InitPersBoundCompVisitor visitor = new InitPersBoundCompVisitor(kb, config);
		visitor.visit(protocol);
		protocol.checkErrors();
		return visitor.getCount();
	}

	private static class InitPersBoundCompVisitor {

		private final KnowledgeBase _kb;

		private final Map<String, LayoutComponent.Config> _configById = new HashMap<>();

		private final Iterator<LayoutComponent.Config> _configs;

		private int _count;

		private final Location _rootLocation;

		public InitPersBoundCompVisitor(KnowledgeBase kBase, LayoutComponent.Config root) {
			_kb = kBase;
			_rootLocation = root.location();
			_configs = new DescendantDFSIterator<>(LayoutConfigTreeView.INSTANCE, root, true);
		}

		public void visit(Log log) {
			while (_configs.hasNext()) {
				doWork(log, _configs.next());
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
			if (LayoutConstants.isSyntheticName(componentName)) {
				// No real security here
				return;
			}

			String lowercaseId = componentName.qualifiedName().toLowerCase();
			LayoutComponent.Config prevComp = _configById.get(lowercaseId);
			if (_configById.containsKey(lowercaseId)) {
				logDuplicateComponent(log, prevComp, config);
				return;
			}
			_configById.put(lowercaseId, config);
			
			if (SecurityComponentCache.getSecurityComponent(componentName) == null) {
				PersBoundComp.createInstance(_kb, componentName);
				_count++;
			}
			registerDefaultTypes(config, componentName);

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
		 * check the defaultFor config and register the component as defaultFor for the found ko/me
		 * types in the BoundHelper
		 * 
		 * @see BoundHelper#getDefaultChecker(MainLayout rootLayout, BoundObject anObject)
		 */
		private void registerDefaultTypes(LayoutComponent.Config config, ComponentName id) {
			List<String> theDefaultForTypes = config.getDefaultFor();
			for (int i = 0; i < theDefaultForTypes.size(); i++) {
				String theType = theDefaultForTypes.get(i);
				BoundHelper.getInstance().registerPersBoundCheckerFor(_rootLocation, id, theType);
			}
		}

	}
 
}
