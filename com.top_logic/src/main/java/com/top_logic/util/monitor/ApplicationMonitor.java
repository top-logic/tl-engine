/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * {@link ManagedClass} providing {@link MonitorComponent}s.
 * 
 * <p>
 * A {@link MonitorComponent} checks a certain function of the application. This
 * {@link ManagedClass} collects the results from all {@link MonitorComponent}s.
 * </p>
 * 
 * @see MonitorComponent#checkState(MonitorResult)
 * @see #checkApplication()
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ApplicationMonitor extends ManagedClass {

	/**
	 * Configuration of the {@link ApplicationMonitor}
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<ApplicationMonitor> {

		/**
		 * Registered {@link MonitorComponent}s.
		 */
		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		Map<String, NamedPolymorphicConfiguration<MonitorComponent>> getComponents();

	}

    /** The result of the last application check. */
    private MonitorResult monitorResult;

    /** The map of known {@link com.top_logic.util.monitor.MonitorComponent monitors}. */
	private Map<String, MonitorComponent> monitors;

	/**
	 * Creates a new {@link ApplicationMonitor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ApplicationMonitor}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ApplicationMonitor(InstantiationContext context, Config config) throws ConfigurationException {
		monitors = TypedConfiguration.getInstanceMap(context, config.getComponents());
	}

    /**
     * Return a string repesentation of this instance.
     * 
     * @return    The string representation for debugging.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                + "monitors: " + this.monitors.size()
                + ", monitorResult: " + this.monitorResult 
                + "]");
    }

    /**
     * Append the given monitor to the list of known monitors in the application.
     * 
     * @param    aKey          The key to find the component later on.
     * @param    aComponent    The component to be added.
     * @return   <code>true</code>, if adding changed the list of monitors.
     */
    public boolean registerMonitor(String aKey, MonitorComponent aComponent) {
        return (this.monitors.put(aKey, aComponent) == null);
    }

    /**
     * Remove the given monitor from the list of known monitors in the application.
     * 
     * @param    aKey    The key of the component to be removed.
     * @return   <code>true</code>, if removing changed the list of monitors.
     */
    public boolean unregisterMonitor(String aKey) {
        return (this.monitors.remove(aKey) != null);
    }

    /**
     * Check the state of the application.
     * 
     * This method will go through the registered monitors and ask each of
     * them for the current state of its monitored component. The 
     * {@link MonitorMessage message} returned by him will be appended to 
     * the result. This result is not the one returned by {@link #getMonitorResult()}
     * which will reflect the ongoing state of the application.
     * 
     * @return    The result of the checks of all known monitors.
     */
    public MonitorResult checkApplication() {
		Iterator<MonitorComponent> theIt = this.monitors.values().iterator();
        MonitorResult    theResult = this.getMonitorResult();

        while (theIt.hasNext()) {
			MonitorComponent monitorComponent = theIt.next();
			try {
                long start = System.currentTimeMillis();
				monitorComponent.checkState(theResult);
                long delta = System.currentTimeMillis() - start;
                if (delta > 1000) {
					StringBuilder longRunningCheck = new StringBuilder();
					longRunningCheck.append("Check of  ");
					longRunningCheck.append(monitorComponent.getDescription());
					longRunningCheck.append(" needed ");
					longRunningCheck.append(DebugHelper.getTime(delta));
					Logger.warn(longRunningCheck.toString(), ApplicationMonitor.class);
                }
			} catch (Throwable aThrow) {
				Logger.warn("Unable to check state in " + monitorComponent, aThrow, this);
            }

        }

        this.clearMonitorResult();

        return (theResult);
    }

    /**
     * Return the component for the given key.
     * 
     * @param    aKey    The key of the component as defined in the configuration file.
     * @return   The requested component, may be <code>null</code>.
     */
    public MonitorComponent getMonitor(String aKey) {
		return this.monitors.get(aKey);
    }

    /**
     * Return the current monitor result.
     * 
     * @return    The requested result.
     */
    public MonitorResult getMonitorResult() {
        if (this.monitorResult == null) {
            this.monitorResult = new MonitorResult();
        }

        return (this.monitorResult);
    }

    /**
     * Append a monitor message to the list of current messages.
     * 
     * @param    aMessage    The message to be appended. 
     */
    public void addMessage(MonitorMessage aMessage) {
        this.getMonitorResult().addMessage(aMessage);
    }

    /**
     * Reset the current monitor result.
     */
    public void clearMonitorResult() {
        this.monitorResult = null;
    }

    /**
     * Return the only instance of this class.
     * 
     * @return    The requested singleton.
     */
	public static ApplicationMonitor getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Module for {@link ApplicationMonitor}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ApplicationMonitor> {

		/** Singleton {@link ApplicationMonitor.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<ApplicationMonitor> getImplementation() {
			return ApplicationMonitor.class;
		}

	}
}
