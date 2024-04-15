/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Locale;
import java.util.Map;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.PropertyType;
import com.top_logic.base.cluster.ClusterManagerListener;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependency;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;

/**
 * Default {@link ResourcesModule} implementation creating {@link Resources}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultResourcesModule extends ResourcesModule
		implements Reloadable, ClusterManagerListener, ServiceDependency<ClusterManager> {

	/**
	 * Key under which this instance is registered in the {@link ReloadableManager}.
	 * 
	 * @see #getName()
	 */
	public static final String RELOADABLE_KEY = "Resources";

	/** {@link ClusterManager} value to notify other clusters to reload resources. */
	private static final String CM_PROPERTY_RELOAD = "res_reload";

	private ClusterManager _clusterManager;

	/**
	 * Creates a {@link DefaultResourcesModule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultResourcesModule(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected I18NBundleSPI createBundle(Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
		return new Resources(this, locale, bundle, fallback);
	}

	@Override
	public void handleUnknownKey(I18NBundleSPI bundle, ResKey key) {
		if (ScriptingRecorder.isEnabled() && ScriptingRecorder.isResourceInspecting()) {
			return;
		}

		super.handleUnknownKey(bundle, key);
	}

	@Override
	protected void startUp() {
		super.startUp();
		
		ClusterManager.Module.INSTANCE.addServiceDependency(this);
		ReloadableManager.getInstance().addReloadable(this);
	}

	@Override
	public void onConnect(ClusterManager impl) {
		_clusterManager = impl;

		if (runInCluster()) {
			_clusterManager.addClusterMessageListener(this);
			_clusterManager.declareValue(CM_PROPERTY_RELOAD, PropertyType.NULL);
		}
	}

	@Override
	public void onDisconnect(ClusterManager oldInstance) {
		if (runInCluster()) {
			_clusterManager.undeclareValue(CM_PROPERTY_RELOAD);
			_clusterManager.removeClusterMessageListener(this);
		}

		_clusterManager = null;
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		ClusterManager.Module.INSTANCE.removeServiceDependency(this);

		super.shutDown();
	}

	@Override
	public void clusterPropertyChangeConfirmed(String propertyName, Object currentValue) {
		// ignore property.
	}

	@Override
	public void clusterPropertyChanged(String propertyName, Object oldValue, Object newValue,
			boolean changedByThisNode) {
		if (CM_PROPERTY_RELOAD.equals(propertyName)) {
			internalReload();
		}
	}

	/**
	 * Forces the instance to reload the configuration.
	 *
	 * This implementation initialize the properties again and returns true afterwards.
	 *
	 * @return true, if reloading succeeds.
	 */
	@Override
	public boolean reload() {
		doReload();
		return true;
	}

	@Override
	protected void updateDynamicResources(Map<Locale, Map<ResKey, String>> changes) {
		super.updateDynamicResources(changes);

		doReload();
	}

	private void doReload() {
		if (runInCluster()) {
			/* It is no problem always to set null, because a property change is also fired if the
			 * value was null before. */
			_clusterManager.setValue(CM_PROPERTY_RELOAD, null);
		} else {
			internalReload();
		}
	}

	private boolean runInCluster() {
		return _clusterManager != null && _clusterManager.isClusterMode();
	}

	/**
	 * Returns a user understandable name of the implementing class.
	 *
	 * If the returned value is empty, the value will not be displayed in the user interface.
	 *
	 * @return The name of the class.
	 */
	@Override
	public String getName() {
		return RELOADABLE_KEY;
	}

	/**
	 * Returns the description of the functionality of the implementing class.
	 *
	 * @return The description of the function of this class.
	 */
	@Override
	public String getDescription() {
		return ("The I18N information for the whole application.");
	}

	/**
	 * Returns true, if the instance uses the XMLproperties.
	 *
	 * This is important for the Reloadable function to clarify, if the XMLProperties have to be
	 * reloaded also.
	 *
	 * @return true, if the XMLProperties are in use.
	 */
	@Override
	public boolean usesXMLProperties() {
		return (true);
	}

	@Override
	public void dropCachedTranslations(SubSessionContext subSession) {
		Resources.dropCachedTranslations(subSession);
	}

}
