/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;
import com.top_logic.tool.state.State;
import com.top_logic.tool.state.StateManager;
import com.top_logic.tool.state.StateManager.KeyConfiguration;
import com.top_logic.util.error.TopLogicException;

/**
 * This class kicks into the Layout security management via
 * <code>com.top_logic.tool.boundsec.BoundHelper#allowInState(com.top_logic.util.TLContext, com.top_logic.tool.boundsec.BoundChecker, com.top_logic.tool.boundsec.BoundCommand, com.top_logic.tool.boundsec.BoundObject)</code>.
 *
 * So components/command groups/commands are only enabled as specified by the ViewStateManager.
 *
 * TODO #25956: Remove class
 * 
 * @author <a href="mailto:tgi@top-logic.com> </a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class ViewStateManager {

	public interface Config extends ConfigurationItem {

		String TAG_SPECIAL_VALUES = "SpecialValues";

		String TAG_DEFAULT_VALUES = "DefaultValues";

		@Key(StateDescription.ATTRIBUTE_KEY)
		@Name(TAG_DEFAULT_VALUES)
		@EntryTag("defaultState")
		Map<String, StateDescription> getDefaultValues();

		@Name(TAG_SPECIAL_VALUES)
		@EntryTag("specialState")
		List<SpecialStateDescription> getSpecialValues();
	}

	public interface StateDescription extends KeyConfiguration {

		String ATTRIBUTE_STATECLASS = "stateclass";

		String TAG_COMMAND_GROUP = "commandGroups";

		String TAG_COMMAND = "commands";

		@Name(ATTRIBUTE_STATECLASS)
		Class<?> getStateclass();

		@Name(TAG_COMMAND)
		List<KeyConfiguration> getCommands();

		@Name(TAG_COMMAND_GROUP)
		List<KeyConfiguration> getCommandGroups();

	}

	public interface SpecialStateDescription extends StateDescription {

		String ATTRIBUTE_VIEW_NAME = "viewName";

		@Name(ATTRIBUTE_VIEW_NAME)
		@Constraint(QualifiedComponentNameConstraint.class)
		ComponentName getViewName();

	}

    /** Signature for a CTor using List as parameter, for introspection */
	static final Class<?>[] LIST_SIGNATURE = new Class[] { List.class, List.class };

    private static String CONFIG_FILE = "/WEB-INF/xml/state/ViewStateConfiguration.xml";

    /** Yet another Singleton. */
    private static ViewStateManager instance = null;

    /** Map indexed by state-keys, containing view-states */
	private Map<String, ViewState> defaultStates;

    /** Map of ViewState indexed by their class name. */
	private Map<Class<?>, ViewState> staticStates;

    /** Map indexed by state-keys, containing Maps index by view names containing view-states */
	private Map<ComponentName, Map<String, ViewState>> specialStates;

    /**
     * Setup the ViewStateManager by parsing the ViewStateConfiguration.xml.
     *
     * @throws Exception
     *             if initializing of the ViewStateManager fails
     */
    private ViewStateManager() throws Exception {
       initStates();
    }


    /**
     * Setup the states from the ViewStateConfiguration.xml.
     *
     * @throws Exception
     *             if initializing of the ViewStateManager fails
     */
    private synchronized void initStates() throws Exception {
		defaultStates = new HashMap<>();
		staticStates = new HashMap<>();
		specialStates = new HashMap<>();
		BinaryData configFile = null;
        try {
            Logger.info("Initializing ViewStateManager with config file "+CONFIG_FILE,this);
               configFile = FileManager.getInstance().getDataOrNull(CONFIG_FILE);
        } catch (Exception e) {
            Logger.error("Failed to get config file "+CONFIG_FILE, e, this);
            throw new TopLogicException(ViewStateManager.class, "config.load", new Object[] {CONFIG_FILE});
        }
		if (configFile == null) {
            Logger.error("Failed to get config file "+CONFIG_FILE, this);
            throw new TopLogicException(ViewStateManager.class, "config.load.null", new Object[] {CONFIG_FILE});
        }

		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.emptyMap();
		Protocol protocol = new LogProtocol(ViewStateManager.class);
		ConfigurationItem config = ConfigurationReader.readContent(protocol, globalDescriptors, configFile);
		protocol.checkErrors();
		if (!(config instanceof Config)) {
			StringBuilder wrongConfigurationInterface = new StringBuilder();
			wrongConfigurationInterface.append("Configuration in file '");
			wrongConfigurationInterface.append(configFile.getName());
			wrongConfigurationInterface.append("' does not match structure as defined in '");
			wrongConfigurationInterface.append(Config.class.getName());
			wrongConfigurationInterface.append("'.");
			throw new ConfigurationException(wrongConfigurationInterface.toString());
		}
		Config stateManagerConf = (Config) config;

		// init default values
		for (StateDescription stateDescription : stateManagerConf.getDefaultValues().values()) {
			registerDefault(Config.TAG_DEFAULT_VALUES, stateDescription);
		}

        // check if all needed default values were read
        StateManager theManager = StateManager.getManager();

        for (Iterator theIt = theManager.getStates().iterator(); theIt.hasNext(); ) {
            State theState = (State) theIt.next();

            if (!defaultStates.containsKey(theState.getKey())){
                throw new TopLogicException(ViewStateManager.class, "config.get.state", new Object[] {theState.getKey(), CONFIG_FILE});
            }
        }

        // read special values
		for (SpecialStateDescription stateDescription : stateManagerConf.getSpecialValues()) {
			registerSpecial(Config.TAG_SPECIAL_VALUES, stateDescription);
		}
    }


    private ViewState createViewStateFromConfig(String propertyName, StateDescription stateDescription) throws Exception {
		Class<?> stateclass = stateDescription.getStateclass();
		List<KeyConfiguration> commands = stateDescription.getCommands();
		List<KeyConfiguration> commandGroups = stateDescription.getCommandGroups();

		if (!commands.isEmpty() || !commandGroups.isEmpty()) {
            // if commands exist: do special
			List<String> allCommandNames = new ArrayList<>();
			for (KeyConfiguration command : commands) {
				allCommandNames.add(command.getKey());
            }
			List<String> allCommandGroupNames = new ArrayList<>();
			for (KeyConfiguration commandGroup : commandGroups) {
				allCommandGroupNames.add(commandGroup.getKey());
            }
            return createNewViewState(propertyName, stateclass, allCommandNames, allCommandGroupNames);
        }
        else{
            // else register as a static ViewState
            return getStaticState(stateclass);
        }
    }

    /**
	 * Registers the state description given by the element as a default description.
     * @param stateDescription
	 *        Configuration of the state
     * @throws Exception
	 *         if the configured ViewState could not be created
	 */
	private void registerDefault(String propertyName, StateDescription stateDescription) throws Exception {
		String key = stateDescription.getKey();
        StateManager theManager = StateManager.getManager();
        State theState = theManager.getState(key);
        if (theState == null) {
            throw new TopLogicException(ViewStateManager.class, "wrong config file: key "+key+" is not a valid state. Valid states are: "+theManager.getStates());
        }
		ViewState theViewState = createViewStateFromConfig(propertyName, stateDescription);
        defaultStates.put(key, theViewState);
    }

	private void registerSpecial(String propertyName, SpecialStateDescription stateDescription) throws Exception {
		ComponentName view = stateDescription.getViewName();
		String state = stateDescription.getKey();
        StateManager theManager = StateManager.getManager();
        State theState = theManager.getState(state);
        if (theState == null) {
            throw new TopLogicException(ViewStateManager.class, "wrong config file: key "+view+" is not a valid state");
        }

		ViewState theViewState = createViewStateFromConfig(propertyName, stateDescription);
		Map<String, ViewState> theMap = specialStates.get(view);
        if (theMap == null) {
			theMap = new HashMap<>();
            specialStates.put(view, theMap);
        }
        theMap.put(state, theViewState);
    }


    /**
     * Returns the static state belonging to the given class.
     */
    private ViewState getStaticState(Class<?> aStateClass) throws Exception {
		ViewState theState = staticStates.get(aStateClass);
        if (theState == null) {
			theState = (ViewState) ConfigUtil.newInstance(aStateClass);
            staticStates.put(aStateClass,theState);
        }
        return theState;
    }


    /**
	 * Creates a new instance of aStateClass with the given list of allowed commands and allowed
	 * command groups.
	 * 
	 * @param propertyName
	 *        Name of the property wher the class is read from.
	 * @param aStateClass
	 *        Name of a class resolved via introspection.
	 * @param allowedCommands
	 *        a list of allowed commands
	 * @param allowedCommandGroups
	 *        a list of allowed command groups
	 */
	private ViewState createNewViewState(String propertyName, Class<?> aStateClass,
			List<String> allowedCommands, List<String> allowedCommandGroups) throws ConfigurationException {
		return (ViewState) ConfigUtil.getNewInstance(propertyName, aStateClass, LIST_SIGNATURE, allowedCommands,
			allowedCommandGroups);
    }


    /**
     * Returns the ViewState for the given state and the given view
     *
     * @param aState Must not be <code>null</code>
     * @param aViewName today this is the name of a LayoutComponent
     */
    public ViewState getViewState(State aState, ComponentName aViewName) {
        String theKey = aState.getKey();
		Map<String, ViewState> theMap = specialStates.get(aViewName);
        if(theMap != null){
			ViewState theState = theMap.get(theKey);
            if(theState != null){
                return theState;
            }
        }
        // if no special value registered: return the default value
		return defaultStates.get(theKey);
    }

    /**
     * Yet another Singleton.
     */
	public static synchronized ViewStateManager getManager() {
        if (instance == null) {
			{
                {
                    try {
                        instance = new ViewStateManager();
                    } catch (Exception e) {
                        Logger.error(
                            "getManager(): Error in initializing State, check your config file",
                            e, ViewStateManager.class);
                        instance = null;
                        if(e instanceof TopLogicException){
                            throw (TopLogicException)e;
                        }
                        else{
                            throw new TopLogicException(ViewStateManager.class,"getManager(): Error in initializing ViewStatemanager, check your config file" ,e);
                        }
                    }
                }
            }
        }
        return instance;
    }


    /**
     * resets the manager. ONLY TO BE USED FOR TESTING!!
     *
     */
    public static void resetForTest(){
        instance=null;
    }

    /**
     * Only to be used for tests.
     *
     * Sets the name (and relative path) of the config file to be able to
     * use a test configuration.
     * Must be called before instantiating the StateManager
     */
    public static void setCONFIG_FILEForTest(String aConfigFile) {
        CONFIG_FILE = aConfigFile;
    }

}