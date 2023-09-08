/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.EnabledConfiguration;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.layout.ConfigureLogEntriesComponent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ResPrefix;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.util.Resources;

/**
 * Service for configuring {@link MonitorEvent}s.
 * 
 * <p>
 * The {@link LogEntryConfiguration} is used to configure
 * </p>
 * 
 * <ul>
 * <li>what kind of {@link MonitorEvent}s sent by the {@link ModelTrackingService} are made
 * persistent within the {@link LogEntryReceiver}.</li>
 * <li>the string message key a {@link LogEntry} holds.</li>
 * <li>the grouping of different types of {@link LogEntry}s to a {@link LogEntryDisplayGroup}. In
 * {@link ConfigureLogEntriesComponent}, the user can choose which of these groups should be
 * displayed in his context.</li>
 * </ul>
 * 
 * <pre>
 * &lt;object-types&gt;
 *  &lt;object-type
 *   name="projectElement.Project"
 *   display-group="Project"
 *   event-types="created,deleted,modified"
 *  /&gt;
 * &lt;/object-types&gt;
 * </pre>
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@ServiceDependencies({
	ResourcesModule.Module.class,
	BoundHelper.Module.class,
})
public final class LogEntryConfiguration extends ManagedClass {

    public static final String  SEPARATOR = "_";
    
    /**
     * prefix for resource keys used in {@link ConfigureLogEntriesComponent}
     */
	public static final ResPrefix I18N_CONFIG_PREFIX = I18NConstants.LOGENTRY_CONF;
    
    /**
     * prefix for resource keys used in {@link LogEntry}
     */
	public static final ResPrefix I18N_MESSAGE_PREFIX = I18NConstants.LOGENTRY_MESSAGE;
    
	/**
	 * Configuration of the {@link LogEntryConfiguration}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<LogEntryConfiguration> {

		/**
		 * All known {@link ObjectType} indexed by their {@link ObjectType#getName() name}.
		 */
		@Key(ObjectType.NAME_ATTRIBUTE)
		Map<String, ObjectType> getObjectTypes();
	}

	/**
	 * Configuration of the known object type.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ObjectType extends EnabledConfiguration {
		
		/**
		 * Name of the display group to include this objectType.
		 */
		@Mandatory
		String getDisplayGroup();
		
		/**
		 * Event types for which events may be displayed.
		 */
		@Mandatory
		@Format(CommaSeparatedStringSet.class)
		Set<String> getEventTypes();

		/** Name of the object type to display {@link LogEntry} for. */
		@Override
		String getName();

	}

	private final BoundHelper boundHelper;
    
    private Map<String,LogEntryDisplayGroup> allDisplayGroups;
    
    /** Map <String anObjectType, LogEntryDisplayGroup> */
    private Map<String,LogEntryDisplayGroup> mapObjToDisplayGroup;
    
    public static synchronized LogEntryConfiguration getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * Creates a new {@link LogEntryConfiguration} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link LogEntryConfiguration}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public LogEntryConfiguration(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		boundHelper = BoundHelper.getInstance();
        this.mapObjToDisplayGroup = new HashMap<>();
        this.allDisplayGroups     = new HashMap<>();
        
		for (ObjectType objectType : config.getObjectTypes().values()) {
			if (!objectType.isEnabled()) {
				continue;
			}
			String displayGroupName = objectType.getDisplayGroup();
			LogEntryDisplayGroup displayGroup = allDisplayGroups.get(displayGroupName);
			if (displayGroup == null) {
				displayGroup = new LogEntryDisplayGroup(displayGroupName);
				this.allDisplayGroups.put(displayGroupName, displayGroup);
            }

			displayGroup.addObjectType(objectType.getName(), objectType.getEventTypes());
			this.mapObjToDisplayGroup.put(objectType.getName(), displayGroup);
        }
        
        checkI18N();
    }
    
    /**
     * This is called on startup to check if a tranlastions are in place.
     * 
     * @return true when everything is OK. 
     */
    public final boolean checkI18N() {
		for (String language : ResourcesModule.getInstance().getSupportedLocaleNames()) {
			Resources theResources = Resources.getInstance(language);
			for (LogEntryDisplayGroup displayGroup : getDisplayGroups()) {
				checkKeyExistence(theResources, displayGroup.getResourceKey(), language);
				for (String eventType : displayGroup.getEventTypes()) {
					checkKeyExistence(theResources, displayGroup.getResourceKey(eventType), language);
                }
				for (String logEntryType : displayGroup.getLogEntryTypes()) {
					checkKeyExistence(theResources, I18N_MESSAGE_PREFIX.key(logEntryType), language);
                }
            }
        }
        return false;
    }

	private void checkKeyExistence(Resources resources, ResKey key, String language) {
		if (resources.getString(key, null) == null) {
			Logger.warn("No resource key " + key + " found for language " + language, LogEntryConfiguration.class);
		}
	}
    
    public final String getTypeFor(Wrapper aWrapper) {
        String theType = boundHelper.getCheckerType(aWrapper);
        return theType;
    }
    
	public final Collection<? extends LogEntryDisplayGroup> getDisplayGroups() {
        return allDisplayGroups.values();
    }
    
    public final LogEntryDisplayGroup getDisplayGroup(Wrapper aTrigger) {
        return this.mapObjToDisplayGroup.get(this.getTypeFor(aTrigger));
    }
    
    public final LogEntryDisplayGroup getDisplayGroup(String aGroupName) {
        return this.allDisplayGroups.get(aGroupName);
    }
    
    public final boolean check(String anObjectType, String anEventType) {
        LogEntryDisplayGroup theDisplayGroup = this.mapObjToDisplayGroup.get(anObjectType);

        if (theDisplayGroup != null) {
            return theDisplayGroup.check(anObjectType, anEventType);
        }
        else {
            Logger.debug("No configuration found for " + anObjectType, this);

            return false;
        }
    }
    
    /**
     * This method checks if for the type of the given wrapper th given event type is registerd
     * 
     * @return <code>true</code> if the type of aTrigger is 
     */
    public final boolean check(Wrapper aTrigger, String anEventType) {
        return this.check(this.getTypeFor(aTrigger), anEventType);
    }

	public static final class Module extends TypedRuntimeModule<LogEntryConfiguration> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<LogEntryConfiguration> getImplementation() {
			return LogEntryConfiguration.class;
		}

	}
}

