/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service class to register MBeans to a {@link MBeanServer}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TLDataBeanService extends ConfiguredManagedClass<TLDataBeanService.Config> {

	private final List<MBeanConfiguration<?>> _mBeanConfigurations;
	
	/**
	 * Configuration of a {@link TLDataBeanService}.
	 * 
	 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<TLDataBeanService> {
		/**
		 * @see #getMbeans()
		 */
		String MBEANS = "mbeans";

		/**
		 * Configured MBeans with metrics.
		 */
		@Name(MBEANS)
		@Key(MBeanConfiguration.NAME_ATTRIBUTE)
		List<MBeanConfiguration<?>> getMbeans();
	}

	/**
	 * Creates a new {@link TLDataBeanService}.
	 */
	public TLDataBeanService(InstantiationContext context, Config configuration) {
		super(context, configuration);

		_mBeanConfigurations = configuration.getMbeans();
	}

	private List<MBeanConfiguration<?>> getMBeans() {
		return _mBeanConfigurations;
	}

	@Override
	protected void startUp() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		for (MBeanConfiguration<?> mBean : getMBeans()) {
			registerMBean(mbs, mBean);
		}
	}

	private void registerMBean(MBeanServer mbs, MBeanConfiguration<?> mBeanConfiguration) {
		NamedMonitor mBean = TypedConfigUtil.createInstance(mBeanConfiguration);
		ObjectName objectName = createObjectName(mBeanConfiguration.getName());
		try {
			mbs.registerMBean(mBean, objectName);
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
			Logger.error("Failed to register MBean (" + mBeanConfiguration.getName() + ").", ex, TLDataBeanService.class);
		}
	}

	private ObjectName createObjectName(String name) {
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(name);
		} catch (MalformedObjectNameException ex) {
			Logger.error("Failed to create an ObjectName for MBean (" + name + ").", ex, TLDataBeanService.class);
		}
		return objectName;
	}

	@Override
	protected void shutDown() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		for (MBeanConfiguration<?> mBean : getMBeans()) {
			try {
				mbs.unregisterMBean(createObjectName(mBean.getName()));
			} catch (MBeanRegistrationException | InstanceNotFoundException ex) {
				Logger.error("Failed to unregister MBean (" + mBean.getName() + ").", ex, TLDataBeanService.class);
			}
		}
	}

	/**
	 * Access to the single {@link TLDataBeanService} instance.
	 */
	public static TLDataBeanService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link TLDataBeanService}.
	 */
	public static final class Module extends TypedRuntimeModule<TLDataBeanService> {

		/**
		 * Singleton {@link TLDataBeanService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<TLDataBeanService> getImplementation() {
			return TLDataBeanService.class;
		}
	}

}
