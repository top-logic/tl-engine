/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.GlobalVariableAction;
import com.top_logic.layout.scripting.action.SetGlobalVariableAction;
import com.top_logic.layout.scripting.action.assertion.CheckAction;
import com.top_logic.layout.scripting.action.assertion.GlobalVariableExistenceAssertion;
import com.top_logic.layout.scripting.check.EqualsCheck;
import com.top_logic.layout.scripting.check.NullCheck;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.runtime.action.DelGlobalVariableOp;
import com.top_logic.layout.scripting.runtime.action.GlobalVariableActionOp;
import com.top_logic.layout.scripting.runtime.action.SetGlobalVariableOp;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * Store for global variables of the scripting framework.
 * <p>
 * The variables stored here cannot have <code>null</code> as their name. If <code>null</code> is
 * given to any method as variable name, an {@link NullPointerException} will be thrown. The empty
 * string is okay, but a bit pointless as a 'name'.<br/>
 * Global variables of the scripting framework can be accessed in scripts with
 * {@link GlobalVariableRef} and {@link SetGlobalVariableOp}.<br/>
 * In addition to just referencing them in scripts, the following things can be done with the global
 * variables:
 * </p>
 * 
 * <ul>
 * <li>A global variable can be deleted with {@link DelGlobalVariableOp}.</li>
 * <li>A global variable can be set (and changed) with {@link SetGlobalVariableAction}.</li>
 * <li>It can be checked whether a global variable is set with
 * {@link GlobalVariableExistenceAssertion}.</li>
 * <li>With an {@link CheckAction}, the {@link GlobalVariableRef} and the {@link EqualsCheck},
 * equality can be checked.</li>
 * <li>With an {@link CheckAction}, the {@link GlobalVariableRef} and the {@link NullCheck}, it can
 * be checked whether a value is <code>null</code>.</li>
 * </ul>
 * 
 * @see ReferenceInstantiator#globalVariableRef(String)
 * @see GlobalVariableAction
 * @see GlobalVariableActionOp
 * @see ActionFactory#setGlobalVariableAction(String,
 *      com.top_logic.layout.scripting.recorder.ref.ModelName)
 * @see ActionFactory#delGlobalVariableAction(String)
 * @see ActionFactory#clearGlobalVariablesAction()
 * @see ActionFactory#assertGlobalVariableExistence(String, boolean)
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class GlobalVariableStore {

	private static final Property<GlobalVariableStore> TL_CONTEXT_KEY =
		TypedAnnotatable.property(GlobalVariableStore.class, "SCRIPTING_FRAMEWORK_VARIABLE_STORE");

	private final Map<String, Object> _variables;

	/** Creates a new, empty {@link GlobalVariableStore}. */
	public GlobalVariableStore() {
		_variables = new HashMap<>();
	}

	/**
	 * Setter for global variables of the scripting framework.
	 * 
	 * @param name
	 *        Must not be <code>null</code>.
	 * @param value
	 *        Is allowed to be <code>null</code>.
	 * @throws NullPointerException
	 *         When the given name is <code>null</code>.
	 */
	public void set(String name, Object value) {
		checkName(name);
		checkNoOverride(name, value);
		_variables.put(name, value);
	}

	private void checkNoOverride(String name, Object value) {
		if (_variables.containsKey(name) && !Utils.equals(_variables.get(name), value)) {
			throw new ApplicationAssertion("Overriding a global variable is not allowed. Tried to change '"
				+ name + "' from " + StringServices.getObjectDescription(_variables.get(name)) + " to "
				+ StringServices.getObjectDescription(value));
		}
	}

	/**
	 * Getter for global variables of the scripting framework.
	 * 
	 * @param name
	 *        Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         When the given name is <code>null</code>.
	 * @return <code>null</code>, when there is no such variable or the variable has the value
	 *         <code>null</code>. Use {@link #has(String)} to differentiate between
	 *         these two cases.
	 */
	public Object get(String name) {
		checkName(name);
		if (!_variables.containsKey(name)) {
			throw new ApplicationAssertion("There is no global variable named '" + name + "'. Existing variables: "
				+ _variables.keySet());
		}
		return _variables.get(name);
	}

	/**
	 * Is there a global variables of the scripting framework under the given name?
	 * 
	 * @param name
	 *        Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         When the given name is <code>null</code>.
	 */
	public boolean has(String name) {
		checkName(name);
		return _variables.containsKey(name);
	}

	/**
	 * Delete the global variables of the scripting framework with the given name.
	 * <p>
	 * If there is no such variable, nothing happens. <br/>
	 * </p>
	 * 
	 * @param name
	 *        Must not be <code>null</code>.
	 * @throws NullPointerException
	 *         When the given name is <code>null</code>.
	 */
	public void del(String name) {
		checkName(name);
		_variables.remove(name);
	}

	/**
	 * Returns a new, mutable {@link Map} with all the variableName -> variableValue mappings.
	 */
	public Map<String, Object> getMappings() {
		return new HashMap<>(_variables);
	}

	/** Deletes all global variables. */
	public void clear() {
		_variables.clear();
	}

	private void checkName(String name) {
		if (name == null) {
			throw new NullPointerException("A global variable of the scripting framework cannot have null as name.");
		}
	}

	/**
	 * Returns the {@link GlobalVariableStore} stored in the session, if there is no:
	 * <code>null</code>.
	 */
	public static GlobalVariableStore getFromSession() {
		return context().get(TL_CONTEXT_KEY);
	}

	/**
	 * Returns the {@link GlobalVariableStore} stored in the session. If there is no, a new one is
	 * created, stored in the session and returned.
	 * 
	 * @return Never <code>null</code>.
	 */
	public static GlobalVariableStore getElseCreateFromSession() {
		GlobalVariableStore storeFromSession = getFromSession();
		if (storeFromSession != null) {
			return storeFromSession;
		}
		GlobalVariableStore newStore = new GlobalVariableStore();
		context().set(TL_CONTEXT_KEY, newStore);
		return newStore;
	}

	private static TLSessionContext context() {
		return TLContext.getContext().getSessionContext();
	}

}