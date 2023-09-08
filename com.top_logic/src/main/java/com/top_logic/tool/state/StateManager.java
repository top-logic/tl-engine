/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.base.merge.Validator;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * A Singelton holding States that form a Statemaschine.
 *
 * (This is Workflow light ;-)
 *
 * Is intialized via the ProjectStateConfiguration.xml file.
 *
 * @author    <a href="mailto:tgi@top-logic.com></a>
 */
public class StateManager /* implements EventListener */ {

	public interface Config extends ConfigurationItem {

		String STATE_DESCRIPTORS_PROPERTY = "StateManagerConfiguration";

		@Name(STATE_DESCRIPTORS_PROPERTY)
		@EntryTag("statedescription")
		@Key(StateDescription.ATTRIBUTE_KEY)
		Map<String, StateDescription> getStateDescriptions();

		@Name("StateManagerInitial")
		@EntryTag("fixedState")
		List<InitialConfig> getInitialConfigs();
	}

	public interface KeyConfiguration extends ConfigurationItem {

		String ATTRIBUTE_KEY = "key";

		@Name(ATTRIBUTE_KEY)
		@Mandatory
		String getKey();

	}

	public interface StateDescription extends KeyConfiguration {

		String ATTRIBUTE_STEP = "step";

		String ATTRIBUTE_STATECLASS = "stateclass";

		String ATTRIBUTE_OWNER = "owner";

		@Mandatory
		@Name(ATTRIBUTE_STATECLASS)
		String getStateclass();

		@Nullable
		@Name(ATTRIBUTE_OWNER)
		String getOwner();

		@Mandatory
		@Name(ATTRIBUTE_STEP)
		Integer getStep();

		List<KeyConfiguration> getSuccessors();

	}

	public interface InitialConfig extends NamedConfiguration, KeyConfiguration {

		// Sum interface

	}

    // Konstanz

    private static String CONFIG_FILE = "/WEB-INF/xml/state/ProjectStateConfiguration.xml";

    /** This Association refers from the Project to the ProjectStateChangedEvent, that resulted
     *  in a state transition ?
     */
    public static final String STATE_CHANGED = "stateChanged";

    private static final String INITIAL_STATE_CONFIG_NAME="initial";
    private static final String FINISH_STATE_CONFIG_NAME="finish";

    /** Signature of constructor class for a new StructuredElementState */
    private static final Class[] SIGNATURE = new Class[] {String.class, List.class, Integer.class, Set.class};





    private static final Object DEFAULT_STATE = null;

    protected static String INITIAL_STATE = null;
    protected static String FINISH_STATE  = null;

    // static members

    /** Looks like a singleton, well. */
    private static StateManager manager;

    /** Map of {@link ElementState}s index by their key. */
    private Map states;

    /**
     * Map of PersistentEvent (ProjectStateChangedEvent) index by StructuredElement.
     *
     * All the PersistentEvent attached to the StructuredElement by the STATE_CHANGED association.
     */
    private Map changeEvents;

    /** Default state, when no state can be found (used in {@link #getState(String)}). */
    private ElementState defaultState;

    /**
     * priavte CTor , this is a Singleton.
     */
    private StateManager() throws Exception {
        this.initStates();

        // EventCache.getInstance().addEventListener(this);
    }

    /**
     * Attach the STATE_CHANGED event so the States can be retrieved.
     */
    public void addEvent(EventObject anEvent) {
        if (ProjectStateChanged.isStateChange(anEvent)) {
            /*
            Wrapper theSource = (Wrapper) anEvent.getSource();
            List theList = this.getChangeHistory(theSource);

            if (!theList.contains(anEvent)) {
                theList.add(anEvent);

                POSUtil.createKnowledgeAssociation(STATE_CHANGED,
                                                   theSource.getWrappedObject(),
                                                   anEvent.getWrappedObject());
             }
             */
        }
    }

    /**
	 * This method returns the init state of the state manager.
	 *
	 * @return Returns the init state.
	 */
    public ElementState getInitState() {
    	return getState(INITIAL_STATE);
    }

    /**
	 * This method returns the finish state of the state manager.
	 *
	 * @return Returns the finish state.
	 */
    public ElementState getFinishState() {
    	return getState(FINISH_STATE);
    }

    public ElementState getState(String aKey) {
        if (StringServices.isEmpty(aKey)) {
            return (this.getDefaultState());
        }
        else {
            return (ElementState) getStateMap().get(aKey);
        }
    }

    /**
     * Return the default state for an element.
     *
     * @return    The default state.
     */
    public ElementState getDefaultState() {
        if (this.defaultState == null) {
            this.defaultState = (ElementState) this.getStateMap().get(StateManager.DEFAULT_STATE);

        }

        return this.defaultState;
    }

    /**
     * returns a list with all states known by the manager
     */
    public List getStates() {
        List theRes = new ArrayList(getStateMap().values());
        Collections.sort(theRes);

        return theRes;
    }

    /**
     * @param    anElement    The element to get the history for.
     * @return   The list of PersistentEvents.
     */
    public List getChangeHistory(StatefullElement anElement) {
        List theList = null;
        Map  theMap = this.getChangeMap();

        synchronized (theMap) {
            if (theMap.containsKey(anElement)) {
                theList = ((List) theMap.get(anElement));
            }
            else {
                // theList = this.createChangeHistory(anElement);
                theMap.put(anElement, theList);
            }
        }

        return (theList);
    }

    private Map getStateMap() {
        return states;
    }

    /**
     * TODO KHA
     */
	private synchronized Map getChangeMap() {
        if (this.changeEvents == null) {
			{
				{
                    this.changeEvents = new HashMap();
                }
            }
        }

        return (this.changeEvents);
    }

    /**
     * Setup the states from the ProjectStateConfiguration.xml
     */
    private synchronized void initStates() throws Exception {

		BinaryData configFile = null;
        try {
            Logger.info("Initializing StateManager with config file "+CONFIG_FILE,this);
            configFile = FileManager.getInstance().getDataOrNull(CONFIG_FILE);
        } catch (Exception e) {
            Logger.info("Failed to get config file "+CONFIG_FILE, e, this);
            throw new TopLogicException(StateManager.class,"Failed to get config file "+CONFIG_FILE, e);
        }
		if (configFile == null) {
            Logger.info("Failed to get config file "+CONFIG_FILE, this);
            throw new TopLogicException(StateManager.class,"Failed to get config file "+CONFIG_FILE);
        }

		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.emptyMap();
		Protocol protocol = new LogProtocol(StateManager.class);
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

		Map<PreState, Set<Object>> temp = new HashMap<>();

		for (StateDescription description : stateManagerConf.getStateDescriptions().values()) {
			register(description, temp);
		}
		states = new HashMap();
		processRegister(temp, states, new HashSet(), true);

		for (InitialConfig initialConfig : stateManagerConf.getInitialConfigs()) {
			registerInitialState(initialConfig);
		}

    }

    public boolean isInitialState(State aState){
        if(!StringServices.isEmpty(INITIAL_STATE) && !StringServices.isEmpty(aState.getKey())){
            return INITIAL_STATE.equals(aState.getKey());
        }else{
            return false;
        }
    }

    public boolean isFinalState(State aState){
        if(!StringServices.isEmpty(FINISH_STATE) && !StringServices.isEmpty(aState.getKey())){
            return FINISH_STATE.equals(aState.getKey());
        }else{
            return false;
        }
    }

    private void registerInitialState(InitialConfig initialConf) {
		String name = initialConf.getName();
		String key = initialConf.getKey();
        if(INITIAL_STATE_CONFIG_NAME.equals(name)){
            INITIAL_STATE=key;
        }
        else if (FINISH_STATE_CONFIG_NAME.equals(name)){
            FINISH_STATE=key;
        }
        else{
            throw new TopLogicException(StateManager.class, "No initial state for '"+name+ "' known. check your config file.");
        }
        State theState = getState(key);

        if (theState == null) {
            throw new TopLogicException(StateManager.class, "No state for '"+key+ "' known. Check your config file");
        }
    }

    private void processRegister(Map register, Map someStates, Set postprocess, boolean resolveids)  throws Exception {

        Iterator it = register.keySet().iterator();
        Set isDone = new HashSet();
        Set succs = null;

        while (it.hasNext()) {
            PreState aPreState = (PreState) it.next();
            if (resolveids) {
                resolveSuccessorIds(aPreState, register);
            }
            succs = (Set) register.get(aPreState);
            if (succs != null && (succs.size() == 0 || includes(someStates, succs))) {
                isDone.add(aPreState);
                postprocess.remove(aPreState);
            } else {
                postprocess.add(aPreState);
            }
        }
        if (isDone.isEmpty()) {
            postprocess(postprocess, register, someStates);
            processRegister(register, someStates, postprocess, false);
        }
        Iterator hns = isDone.iterator();
        while (hns.hasNext()) {
            PreState aPreState = (PreState) hns.next();
            createState(someStates, aPreState, register);
            register.remove(aPreState);
        }
        if (register.keySet().size() > 0) {
            processRegister(register, someStates, postprocess, false);
        }
    }

    private void postprocess(Set postprocess, Map register, Map someStates) throws Exception {
        Iterator it = postprocess.iterator();
        Map tmp = new HashMap();
        while (it.hasNext()) {
            PreState thePreState = (PreState) it.next();
            ElementState state = createState(thePreState, null);
            tmp.put(thePreState, state);
            someStates.put(thePreState.getKey(), state);

        }
        Iterator succs = null;
        ElementState stateForSucc = null;
        Set setoffSuccs = null;
        PreState aSucc = null;
        it = tmp.keySet().iterator();
        while (it.hasNext()) {
            PreState prestate = (PreState) it.next();
            setoffSuccs = ((Set) register.get(prestate));
            succs = setoffSuccs.iterator();
            while (succs.hasNext()) {
                aSucc = (PreState) succs.next();
                stateForSucc = (ElementState) someStates.get(aSucc.getKey());
                if (stateForSucc == null) {
                    stateForSucc = (ElementState) tmp.get(aSucc);
                }
                if (stateForSucc != null) {
                    AbstractState stateForPrestate = (AbstractState) tmp.get(prestate);
                    stateForPrestate.addSuccessor(stateForSucc);
                    setoffSuccs.remove(prestate);
                }
            }

        }
    }

    private boolean includes(Map someStates, Set succs) {
        Iterator it = succs.iterator();
        final Set tmp = someStates.keySet();
        while (it.hasNext()) {
            PreState apres = (PreState) it.next();
            if (!tmp.contains(apres.getKey())) {
                return false;
            }
        }
        return true;
    }

    private void resolveSuccessorIds(PreState aPreState, Map register) {
        final Set succs = (Set) register.get(aPreState);
        if (succs == null) {
            return;
        }
        Iterator it = succs.iterator();
        Set res = new HashSet();
        PreState preStateForSucc = null;
        while (it.hasNext()) {
            String succKey = (String) it.next();
            preStateForSucc = fetchPreState(register, succKey);
            if (aPreState != null) {
                res.add(preStateForSucc);
            } else {
                throw new IllegalStateException("StateManager.replace: illegal successor key: " + succKey);
            }

        }
        register.put(aPreState, res);
    }

    private PreState fetchPreState(Map register, String succKey) {
        if (succKey == null) {
            return null;
        }
        Iterator it = register.keySet().iterator();
        while (it.hasNext()) {
            PreState aPreState = (PreState) it.next();
            if (succKey.equals(aPreState.getKey())) {
                return aPreState;
            }
        }
        return null;
    }

    private void createState(Map someStates, PreState aPreState, Map register)  throws Exception {
        ElementState state = (ElementState) someStates.get(aPreState.getKey());
        Set tmp = (Set) register.get(aPreState);
        List succs = null;
        if (!(tmp == null || tmp.isEmpty())) {
            succs = new ArrayList();
            Iterator it = tmp.iterator();
            while (it.hasNext()) {
                PreState aprs = (PreState) it.next();
                succs.add(someStates.get(aprs.getKey()));
            }
        }
        if (state == null) {
            state = createState(aPreState, succs);
            someStates.put(aPreState.getKey(), state);
        }
    }

	private void register(StateDescription stateDescription, Map succsMap) throws Exception {

		final Integer step = stateDescription.getStep();
		final String owner = stateDescription.getOwner();
		final String key = stateDescription.getKey();
		final String stateclass = stateDescription.getStateclass();
        PreState preState = new PreState(key, stateclass, step, owner);
        //        NodeList successors = statedescription.getChildNodes();
        registerSuccessors(stateDescription, preState, succsMap);

    }

	private Map registerSuccessors(StateDescription stateDescription, PreState aPrestate, Map register) {
		Set successors = (Set) register.get(aPrestate);
		if (successors == null) {
			successors = new HashSet();
			register.put(aPrestate, successors);
		}
		for (KeyConfiguration successorKey : stateDescription.getSuccessors()) {
			successors.add(successorKey.getKey());
		}
        return register;
    }

    /**
     * Create a new StructuredElementState from the configuration found in the PreState.
     */
    private ElementState createState(PreState aPreState, List allSuccs) throws Exception {
        try {
            Class       theClass = Class.forName(aPreState.getClassName());
            Constructor constr = theClass.getConstructor(SIGNATURE);
            Object[] args = new Object[] {
                 aPreState.getKey(), allSuccs, aPreState.getStep(),getOwners(aPreState) };
            ElementState theState = (ElementState) constr.newInstance(args);

            if (aPreState.getStep().intValue() == 0) {
                this.defaultState = theState;
            }

            return theState;
        } catch (Exception e) {
            Logger.error("createState:", e, this);
            throw new Exception(e);
        }
    }

    private Set getOwners(PreState aPreState) {
        Set res = new HashSet();
        StringTokenizer tk = new StringTokenizer(aPreState.getOwner(), ",");
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            res.add(token);
        }
        return res;
    }


    /**
     * Check, if the given project is finished.
     *
     * If the given element is not a project, the method will find the project,
     * this element belongs to and return its state.
     *
     * NOTE: This is not a good way to do this! In this case the elements state determines
     * himself wether it is the final state or not. This is done by checking if there are any
     * successors or not.
     * However, the state can NOT determine wether it is the initial state this way.
     * It cant do that at all actually. So this is done by the statemanager::isInitalState() method.
     * Likewise the statemanager offers a isFinalState() method, of course. So it can check for both criterias,
     * something the state can not do. Further there is a risk that the results of State::isFinal() and
     * StateManager::isFinalState(State) are different due to different implementations.
     *
     * So my suggestion is to deprecate / remove the method State::isFinal() and to use the statemanagers isFinalState() method instead.
     * Alternatively the implementation of State::isFinal() could be changed to actually use the statemanager::isFinalState() method.
     * In this case however the State should also provide the corresponding isInitial() method, that one would naturally expect, if there
     * is an isFinal() method provided.
     * However- smoothes and clearest thing would be to remove said function from the state and leave that to the state manager!
     *
     * @param    anElement    The project to be inspected, may be <code>null</code>.
     * @return   <code>true</code> if project is finished.
     */
    public boolean isFinished(StatefullElement anElement) {
        ElementState theState = anElement.getState();
        return theState.isFinal();
    }

    /**
     * Check, if the given project is running.
     *
     * If the given element is not a project, the method will find the project,
     * this element belongs to and return its state.
     *
     * @param    anElement    The project to be inspected, may be <code>null</code>.
     * @return   <code>true</code> if project is running.
     */
    public boolean isRunning(StatefullElement anElement) {
        ElementState theState =  anElement.getState();
        return !theState.isFinal();
    }

    /**
     * Change the state of the given element to the given value.
     *
     * If one of both parameters is <code>null</code>, the state will not
     * be changed.
     *
     * @param    anElement    The element to change the state for, may be <code>null</code>.
     * @param    aKey         The key of the new state of the element, may be <code>null</code>.
     */
    public void changeState(StatefullElement anElement, String aKey) {
        if (anElement != null) {
            ElementState oldState = anElement.getState();
            ElementState newState = getState(aKey);
            if(oldState.isValidSuccessor(newState)){
                anElement.setState(newState);
            }
        }
    }

    /**
     * Sets the given element to inital state.
     */
    public void setInitialState(StatefullElement anElement) {
        anElement.setState(this.getState(INITIAL_STATE));
    }

    /**
     * Validates whether switching the state is allowed due to some conditions defined in a Validator
     *
     * @param anElement         The Element to be switched into the new State.
     * @param aFutureStateID    ID of the new desired status.
     *
     * @return A Tree of Nodes containing Messages concerning unfulfilled conditions about state switching
     */
    public MergeTreeNode validate(StatefullElement anElement, String aFutureStateID) {
        return validate(anElement, getState(aFutureStateID));
    }

    /** See {@link #validate(StatefullElement, String)}. */
    public MergeTreeNode validate(StatefullElement anElement, ElementState newState) {
        MergeTreeNode theResult = new RootMergeNode(anElement);

        if (newState == null || anElement == null) {
            return theResult;
        }

        Validator theValidator = newState.validate(ElementState.VALIDATE_FOR_STATE_CHANGE, -1, anElement, null);

        if (theValidator != null) {
            theResult = new MergeTreeNode(theResult, anElement, null);
            theValidator.validate(theResult); // Result is actually ignored, mmh.
        }
        return theResult;
    }

    public boolean isOwner(StatefullElement anElement, ElementState aState, Person aPerson) {
        try {
            /*
            final Set owners = aState.getOwners();
            if (owners == null || owners.size() == 0) {
                return true;
            }
            if (anElement == null) {
                return false;
            }
            */
            Logger.warn("StateManager.isOwner HAS TO BE IMPLEMENTED",this);
            if (ThreadContext.isSuperUser()) {
                return true;
            }
        } catch (Exception e) {
            Logger.error("isOwner", e, this);
        }
        return false;
    }

    public List getSuccessorsFor(StatefullElement anElement, ElementState aState, Person aPerson) {
        if (aState == null) {
            return Collections.EMPTY_LIST;
        }
        List tmp = new ArrayList();
        Iterator it = aState.getSuccessors().iterator();
        while (it.hasNext()) {
            ElementState xstate = (ElementState) it.next();
            if (isOwner(anElement, xstate, aPerson)) {
                tmp.add(xstate);
            }
        }
        return tmp;
    }

    public boolean allowsStateChange(StatefullElement element) {
        if (element == null) {
            return false;
        }
        ElementState state = element.getState();
        Person person = TLContext.getContext().getCurrentPersonWrapper();
        return !getSuccessorsFor(element, state, person).isEmpty();
    }

    /**
     * A value holder for data fetched from the XML-Configuration.
     *
     * Will finally result in a State object.
     *
     * @author    <a href="mailto:tgi@top-logic.com></a>
     */
    private static class PreState {

        private String key = null;
        private String owner = null;
        private String className = null;
        private Integer step;

        PreState(String aKey, String aClassName, Integer aStep, String anOwner) {
            if (aKey == null || aClassName == null || aStep == null) {
                throw new IllegalStateException("StateManager.Prestate Constructor: arguments must not be null");
            }
            this.key       = aKey;
            this.className = aClassName;
            this.step      = aStep;
            this.owner     = anOwner;
        }

        // overriden methods from Objects

        /**
         * Equals is based on key, classname and step.
         */
        @Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
            if (!(obj instanceof PreState)) {
                return false;
            }
            PreState ps = (PreState) obj;
            return getKey()      .equals(ps.getKey())
                && getClassName().equals(ps.getClassName())
                && step          .equals(ps.getStep());
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
		public int hashCode() {
            return (getKey()     .hashCode()
                 ^ getClassName().hashCode())
                 * step.intValue();
        }


        /**
         * Acessor for className
         */
        String getClassName() {
            return className;
        }

        /**
         * Acessor for key
         */
        String getKey() {
            return key;
        }

        /**
         * Acessor for step
         */
        Integer getStep() {
            return step;
        }

        /**
         * Acessor for owner
         */
        public String getOwner() {
            return owner;
        }
    }

    /**
     * Yet another Singleton.
     */
	public static synchronized StateManager getManager() {
        if (manager == null) {
			{
				{
                    try {
                        manager = new StateManager();
                    } catch (TopLogicException tlx) {
                         manager = null;
                         throw tlx;
                    } catch (Exception e) {
                        throw new TopLogicException(StateManager.class,
                                "getManager(): Error in initializing State, check your step numbers",e);
                    }
                }
            }
        }
        return manager;
    }

    /**
     * Resets the manager. ONLY TO BE USED FOR TESTING!!
     */
    public static void resetForTest(){
        manager=null;
    }

    /**
     * Only to be used for tests.
     *
     * sets the name (and relative path) of the config file to be able to
     * use a test configuration
     * Must be called before instantiating the StateManager
     *
     */
    public static void setCONFIG_FILEForTest(String aConfigFile) {
        CONFIG_FILE = aConfigFile;
    }

}
