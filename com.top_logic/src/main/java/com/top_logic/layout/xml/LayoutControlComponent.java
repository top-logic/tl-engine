/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.xml;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.component.configuration.ViewConfiguration;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutViewProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;
import com.top_logic.tool.boundsec.BoundCheckerLayoutConfig;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.AllowNoneChecker;

/**
 * Workaround {@link LayoutComponent} to configure to render another {@link LayoutComponent} using a
 * separate {@link LayoutControlProvider}.
 * 
 * <p>
 * Can also be used as dummy component displaying a custom control created by a
 * {@link LayoutControlProvider}. In case, a simple {@link HTMLFragment} should be rendered, a
 * {@link LayoutViewProvider} can be used with an inner {@link ViewConfiguration}.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class LayoutControlComponent extends LayoutComponent implements BoundCheckerDelegate {

	/**
	 * Configuration options for {@link LayoutControlComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends LayoutComponent.Config, BoundCheckerLayoutConfig {

		/** Standard tag name to configure {@link LayoutControlComponent}. */
		String TAG_NAME = "layoutControl";

		/** Name of the config property {@link #getComponentName()}. */
		String COMPONENT_NAME = "componentName";

		/**
		 * Name of the component to render with the configured {@link LayoutControlProvider}
		 */
		@Name(Config.COMPONENT_NAME)
		ComponentName getComponentName();

		/**
		 * Setter for {@link #getComponentName()}.
		 */
		void setComponentName(ComponentName name);

		@Override
		@ClassDefault(LayoutControlComponent.class)
		Class<LayoutControlComponent> getImplementationClass();

		@Override
		@Derived(fun = DispatchingLayoutControlProviderConst.class, args = {})
		LayoutControlProvider getActiveComponentControlProvider();

		/**
		 * {@link Function0} returning the constant
		 * {@link DispatchingLayoutControlProvider#INSTANCE}.
		 */
		class DispatchingLayoutControlProviderConst extends Function0<LayoutControlProvider> {
			@Override
			public LayoutControlProvider apply() {
				return DispatchingLayoutControlProvider.INSTANCE;
			}
		}

	}

	private final ComponentName _componentName;

	private LayoutComponent _referencedComponent;

	private BoundChecker _allowDelegate;

	/**
	 * Create a {@link LayoutControlComponent}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public LayoutControlComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_componentName = config.getComponentName();
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		MainLayout mainLayout = getMainLayout();
		if (_componentName == null) {
			_referencedComponent = this;
			_allowDelegate = new AllowNoneChecker(getName());
		} else {
			_referencedComponent = mainLayout.getComponentByName(_componentName);
			if (_referencedComponent == null) {
				Logger.error("Component cannot be resolved (" + getLocation() + "): " + _componentName,
					LayoutControlComponent.class);
			}
			initSecurityDelegate(context);
		}
	}

	private void initSecurityDelegate(Log log) {
		if (_referencedComponent instanceof BoundChecker) {
			_allowDelegate = checkReferenceNotAncestor(log, _referencedComponent);
		} else {
			_allowDelegate = new AllowNoneChecker(_componentName);
		}
	}

	/**
	 * The referenced {@link LayoutComponent}.
	 */
	public LayoutComponent getReferencedComponent() {
		return _referencedComponent;
	}

	/**
	 * Replaces the currently referenced component by a new instance of the logically same
	 * component.
	 * 
	 * @param newReferencedComponent
	 *        New version of the currently referenced component.
	 * @return Former reference.
	 */
	@FrameworkInternal
	public LayoutComponent replaceReferencedComponent(LayoutComponent newReferencedComponent) {
		if (_referencedComponent == null) {
			throw new IllegalStateException(
				"Component '" + this + "' not yet resolved? No current reference component.");
		}
		if (_referencedComponent == newReferencedComponent) {
			return _referencedComponent;
		}
		if (_referencedComponent == this) {
			// Special case, when no component name is configured. In this case replacement is not
			// allowed.
			throw new IllegalArgumentException(
				"Must not replace referenced component, when component references itself.");
		}
		if (!_referencedComponent.getName().equals(newReferencedComponent.getName())) {
			throw new IllegalArgumentException(
				"New referenced component '" + newReferencedComponent + "' has unexpected name: Expected: "
					+ _referencedComponent.getName() + ", Actual: " + newReferencedComponent.getName());
		}
		LayoutComponent oldReferenced = _referencedComponent;
		_referencedComponent = newReferencedComponent;
		initSecurityDelegate(NoProtocol.INSTANCE);
		return oldReferenced;
	}


	private BoundChecker checkReferenceNotAncestor(Log log, LayoutComponent referencedComponent) {
		LayoutComponent parentOrSelf = this;
		while (parentOrSelf != null) {
			if (parentOrSelf == referencedComponent) {
				StringBuilder referencesAncestor = new StringBuilder();
				referencesAncestor.append("Referenced component '");
				referencesAncestor.append(referencedComponent);
				referencesAncestor.append("' in component '");
				referencesAncestor.append(this);
				referencesAncestor.append("' is an ancestor. Can not delegate security.");
				log.info(referencesAncestor.toString(), Protocol.WARN);
				return new AllowNoneChecker(referencedComponent.getName());
			}
			parentOrSelf = parentOrSelf.getParent();
		}
		if (referencedComponent instanceof BoundChecker) {
			return (BoundChecker) referencedComponent;
		}
		return new AllowNoneChecker(referencedComponent.getName());
	}

	static class DispatchingLayoutControlProvider implements LayoutControlProvider {

		/**
		 * Singleton {@link LayoutControlComponent.DispatchingLayoutControlProvider} instance.
		 */
		public static final LayoutControlComponent.DispatchingLayoutControlProvider INSTANCE =
			new LayoutControlComponent.DispatchingLayoutControlProvider();

		private DispatchingLayoutControlProvider() {
			// Singleton constructor.
		}
		
		@Override
		public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
			LayoutControlComponent controlComponent = (LayoutControlComponent) component;
			LayoutComponent referencedComponent = controlComponent.getReferencedComponent();
			if (referencedComponent == null) {
				Logger.error("Component cannot be resolved: " + component.getName(),
					DispatchingLayoutControlProvider.class);
				return null;
			}

			PolymorphicConfiguration<LayoutControlProvider> customProvider =
				controlComponent.getConfig().getComponentControlProvider();
			if (customProvider == null) {
				return strategy.createLayout(referencedComponent);
			} else {
				LayoutControlProvider customProviderImpl = TypedConfigUtil.createInstance(customProvider);
				return customProviderImpl.createLayoutControl(strategy, referencedComponent);
			}
		}
		
	}

	@Override
	public BoundChecker getDelegate() {
		return _allowDelegate;
	}

	/**
	 * This is not a default checker.
	 */
	@Override
	public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
		return false;
	}

	/**
	 * Use own security id.
	 */
	@Override
	public ComponentName getSecurityId() {
		return getName();
	}

	@Override
	public Collection<BoundChecker> getChildCheckers() {
		return Collections.emptyList();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}
