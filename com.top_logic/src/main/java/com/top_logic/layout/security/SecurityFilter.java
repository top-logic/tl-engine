/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Collection;
import java.util.List;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.AllowAllChecker;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;

/**
 * Filter that checks the security of a given object by either checking some roles or delegation to
 * a certain component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityFilter implements Filter<Object> {

	/**
	 * Configuration of a {@link SecurityFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<T extends SecurityFilter> extends PolymorphicConfiguration<T>, ModelMappingConfig {

		/** Name of the {@link #getDelegationDestination} property. */
		String DELEGATION_DESTINATION = "delegationDestination";

		/**
		 * The configuration parameter for {@link #getSelectionRoles()}.
		 */
		String SELECTION_ROLES_ATTRIBUTE = "selectionRole";

		/**
		 * Name of the component to delegate security to.
		 * 
		 * <p>
		 * The delegation destination is used to check whether a node can be selected. Typically it
		 * is the name of a slave component of a tree which decides whether the node can be set as
		 * model, because if a tree selects that node, the slave gets it as model.
		 * </p>
		 */
		@Name(DELEGATION_DESTINATION)
		ComponentName getDelegationDestination();

		/**
		 * This method returns a list of role names. These roles specify which objects are acccepted
		 * by this filter. E.g. in a tree a node is selectable by the user (for security reasons) if
		 * the user has one of these roles on the node.
		 */
		@Name(SELECTION_ROLES_ATTRIBUTE)
		@Format(CommaSeparatedStrings.class)
		List<String> getSelectionRoles();

	}

	/** {@link BoundChecker} to delegate security checks to. */
	private BoundChecker _delegationDestination;

	/**
	 * Configured name of the component to serve as {@link #_delegationDestination}. May be empty.
	 */
	private final ComponentName _delegationName;

	/** @see Config#getSelectionRoles() */
	private final Collection<BoundedRole> _selectionRoles;

	private Mapping<Object, ? extends TLObject> _modelMapping;

	/**
	 * Creates a new {@link SecurityFilter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SecurityFilter}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public SecurityFilter(InstantiationContext context, Config<? extends SecurityFilter> config)
			throws ConfigurationException {
		_delegationName = config.getDelegationDestination();
		_selectionRoles = BoundedRole.getRolesByName(context, config.getSelectionRoles());
		_modelMapping = context.getInstance(config.getModelMapping());
	}

	/**
	 * Installs the delegation destination for this {@link SecurityFilter}.
	 */
	public void setDelegationDestination(BoundChecker checker) {
		_delegationDestination = checker;
	}

	/**
	 * Checks whether the user has the right to select the given business object.
	 * 
	 * <p>
	 * This method checks the security. It determines whether the user is allowed to select the
	 * given business object.
	 * </p>
	 * 
	 * @param businessObject
	 *        A potential business node in the displayed tree.
	 */
	@Override
	public boolean accept(Object businessObject) {
		if (_modelMapping != null) {
			businessObject = _modelMapping.map(businessObject);
		}
		if (!_selectionRoles.isEmpty()) {
			return selectableByRole((BoundObject) businessObject, _selectionRoles);
		}
		if (_delegationDestination != null) {
			return selectableByDelegate(_delegationDestination, (BoundObject) businessObject);
		}
		return true;
	}

	private boolean selectableByDelegate(BoundChecker securityDelegate, BoundObject bo) {
		return securityDelegate.allow(bo);
	}

	private boolean selectableByRole(BoundObject bo, Collection<BoundedRole> roles) {
		TLSubSessionContext session = TLContextManager.getSubSession();
		if (session == null) {
			return false;
		}
		Person person = session.getPerson();
		if (person == null) {
			return false;
		}

		return AccessManager.getInstance().hasRole(person, bo, roles);
	}

	/**
	 * Resolves and installs the component to delegate security computation to.
	 * 
	 * <p>
	 * Calling this method twice is a no-op.
	 * </p>
	 * 
	 * @see Config#getDelegationDestination()
	 */
	public void ensureDelegationDestination(Log log, MainLayout mainLayout) {
		ComponentName delegationName = _delegationName;
		if (delegationName == null) {
			return;
		}
		if (_delegationDestination != null) {
			return;
		}
		LayoutComponent delegationComponent = mainLayout.getComponentByName(delegationName);
		BoundChecker delgationChecker;
		if (delegationComponent == null) {
			StringBuilder noComponent = new StringBuilder();
			noComponent.append("No component found for configured name '");
			noComponent.append(delegationName);
			noComponent.append("'.");
			log.error(noComponent.toString());
			delgationChecker = new AllowAllChecker(delegationName);
		} else if (!(delegationComponent instanceof BoundChecker)) {
			StringBuilder noBoundChecker = new StringBuilder();
			noBoundChecker.append("Configured component '");
			noBoundChecker.append(delegationComponent);
			noBoundChecker.append("' is not a ");
			noBoundChecker.append(BoundChecker.class.getName());
			noBoundChecker.append(".");
			log.error(noBoundChecker.toString());
			delgationChecker = new AllowAllChecker(delegationName);
		} else {
			delgationChecker = (BoundChecker) delegationComponent;
		}
		setDelegationDestination(delgationChecker);
	}
}

