/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.layout.admin.component.SecurityRow.ComplexSecurityRow;
import com.top_logic.element.layout.admin.component.SecurityRow.SimpleSecurityRow;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Convert the result from the security storage to a human readable object.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ShowSecurityResultConverter {

	private ElementAccessManager accessManager;

	private Map<TLID, Group> groupMap;

	/** 
	 * Creates a {@link ShowSecurityResultConverter}.
	 */
	public ShowSecurityResultConverter() {
		this.accessManager = (ElementAccessManager) AccessManager.getInstance();
		this.groupMap = new HashMap<>();
	}

	/**
	 * Convert the given search result to a list of security rows to be displayed in the
	 * application.
	 * 
	 * @param    someRows    The rows to be converted, must not be <code>null</code>.
	 * @param    simpleMode    <code>true</code> when no resolving is needed (will only show IDs).
	 * @return   The list of converted security rows, never <code>null</code>.
	 */
	public List<SecurityRow> convert(List<Object[]> someRows, boolean simpleMode) {
		return simpleMode ? this.convertSimpleResult(someRows) : this.convertComplexResult(someRows);
	}

	/**
	 * Just wrap the given rows by some {@link SimpleSecurityRow}s.
	 * 
	 * @param    someRows    The rows to be converted, must not be <code>null</code>.
	 * @return   The list of converted security rows, never <code>null</code>.
	 */
	protected List<SecurityRow> convertSimpleResult(List<Object[]> someRows) {
		List<SecurityRow> theResult = new ArrayList<>(someRows.size());

		for (Object[] theEntry : someRows) {
			theResult.add(new SimpleSecurityRow(theEntry));
		}

		return theResult;
	}

	/**
	 * Just wrap the given rows by some {@link SimpleSecurityRow}s.
	 * 
	 * @param    someRows    The rows to be converted, must not be <code>null</code>.
	 * @return   The list of converted security rows, never <code>null</code>.
	 * @see      #prepareRows(List)
	 */
	protected List<SecurityRow> convertComplexResult(List<Object[]> someRows) {
		Map<RoleProvider, List<SecurityRow>> theMap    = this.prepareRows(someRows);
		List<SecurityRow>                    theResult = new ArrayList<>(someRows.size());

		for (List<SecurityRow> theValue : theMap.values()) {
			theResult.addAll(theValue);
		}

		return theResult;
	}

	/**
	 * Convert the given rows into a map of role provider and the security rows with resolved business objects.
	 * 
	 * @param    someRows    The rows to prepare, must not be <code>null</code>.
	 * @return   The rows split up into a map.
	 * @see      #preload(Map)
	 */
	protected Map<RoleProvider, List<SecurityRow>> prepareRows(List<Object[]> someRows) {
		Map<Short, RoleProvider> roleMap = new HashMap<>();
		Map<RoleProvider, List<SecurityRow>> theResult  = new HashMap<>();

		for (Object[] theEntry : someRows) {
			SimpleSecurityRow theRow = new SimpleSecurityRow(theEntry);

			MapUtil.addObject(theResult, this.getRoleProvider(theRow, roleMap), theRow);
		}

		return this.preload(theResult);
	}

	/**
	 * Convert the map of given (raw) security rows into complex rows.
	 * 
	 * The simple rows contains only strings whereas the complex rows provide 
	 * resolved business objects.
	 * 
	 * @param    aMap    The map of string based rows.
	 * @return   A map of (pre-) resolved business objects.
	 * @see      #preload(RoleProvider, List)
	 */
	protected Map<RoleProvider, List<SecurityRow>> preload(Map<RoleProvider, List<SecurityRow>> aMap) {
		Map<RoleProvider, List<SecurityRow>> theResult = new HashMap<>();

		for (Entry<RoleProvider, List<SecurityRow>> theEntry : aMap.entrySet()) {
			RoleProvider      theKey   = theEntry.getKey();
			List<SecurityRow> theValue = theEntry.getValue();

			if (theKey != null) {
				theResult.put(theKey, this.preload(theKey, theValue));
			}
			else {
				theResult.put(theKey, this.preloadDirectRoleRules(theValue));
			}
		}

		return theResult;
	}

	/** 
	 * Convert security rows which represents rows without a {@link RoleProvider} (e.g. reason is "hasRole").
	 * 
	 * @param    someValues    The list of string based rows.
	 * @return   A list of (pre-) resolved business objects.
	 * @see      #createDirectRoleRuleRow(SecurityRow, KnowledgeBase)
	 */
	protected List<SecurityRow> preloadDirectRoleRules(List<SecurityRow> someValues) {
		List<SecurityRow> theComplex = new ArrayList<>(someValues.size());
		KnowledgeBase     theKB      = PersistencyLayer.getKnowledgeBase();

		for (SecurityRow theRow : someValues) {
			theComplex.add(this.createDirectRoleRuleRow(theRow, theKB));
		}

		return theComplex;
	}

	/**
	 * Convert the list of (raw) security rows into complex rows.
	 * 
	 * The simple rows contains only strings whereas the complex rows provide 
	 * resolved business objects.
	 * 
	 * @param    aProvider     The role rule containing information about the business object types.
	 * @param    someValues    The list of string based rows.
	 * @return   A list of (pre-) resolved business objects.
	 * @see      #preload(TLClass, RoleProvider, List)
	 * @see      #preload(MetaObject, RoleProvider, List)
	 */
	protected List<SecurityRow> preload(RoleProvider aProvider, List<SecurityRow> someValues) {
		TLClass theME = this.getMetaElement(aProvider);

		if (theME != null) {
			return this.preload(theME, aProvider, someValues);
		}
		else {
			MetaObject theMO = this.getMetaObject(aProvider);

			if (theMO != null) {
				return this.preload(theMO, aProvider, someValues);
			}
		}

		return someValues;
	}

	/** 
	 * Convert the list of (raw) security rows into complex rows.
	 * 
	 * The simple rows contains only strings whereas the complex rows provide 
	 * resolved business objects.
	 * 
	 * @param    aMO           The meta object to get the query for.
	 * @param    someValues    The list of string based rows.
	 * @return   A list of (pre-) resolved business objects.
	 * @see      #getMetaObjectNames(MetaObject)
	 * @see      #preload(Collection, RoleProvider, List)
	 */
	protected List<SecurityRow> preload(MetaObject aMO, RoleProvider aProvider, List<SecurityRow> someValues) {
		return this.preload(this.getMetaObjectNames(aMO), aProvider, someValues);
	}

	/** 
	 * Convert the list of (raw) security rows into complex rows.
	 * 
	 * The simple rows contains only strings whereas the complex rows provide 
	 * resolved business objects.
	 * 
	 * @param    aME           The meta element to get the query for.
	 * @param    someValues    The list of string based rows.
	 * @return   A list of (pre-) resolved business objects.
	 * @see      #getMetaObjectNames(MetaObject)
	 * @see      #preload(Collection, RoleProvider, List)
	 */
	protected List<SecurityRow> preload(TLClass aME, RoleProvider aProvider, List<SecurityRow> someValues) {
		return this.preload(this.getMetaObjectNames(aME), aProvider, someValues);
	}

	/**
	 * Convert the list of (raw) security rows into complex rows.
	 * 
	 * The simple rows contains only strings whereas the complex rows provide 
	 * resolved business objects.
	 * 
	 * @param    someTypes     The possible knowledge object types to be used in lookup.
	 * @param    aProvider     The role rule containing information about the business object types.
	 * @param    someValues    The list of string based rows.
	 * @return   A list of (pre-) resolved business objects.
	 */
	protected List<SecurityRow> preload(Collection<String> someTypes, RoleProvider aProvider, List<SecurityRow> someValues) {
		if (!CollectionUtil.isEmptyOrNull(someTypes)) {
			List<SecurityRow> theResult   = new ArrayList<>(someValues.size());
			BoundRole         theRole     = aProvider.getRole();
			ShowSecurityBusinessObjectResolver        theResolver = new ShowSecurityBusinessObjectResolver(someTypes);

			for (SecurityRow theRow : someValues) {
				Group theGroup = this.getGroup((TLID) theRow.getTarget());

				theResult.add(new ComplexSecurityRow(theGroup, (TLID) theRow.getBO(), theRole, aProvider, theResolver));
			}

			return theResult;
		}
		else {
			return Collections.emptyList();
		}
	}

	/**
	 * Return the group identified by the given string ID.
	 * 
	 * @param    groupID    The ID to get the group for.
	 * @return   The requested group.
	 */
	protected Group getGroup(TLID groupID) {
		Group theGroup = this.groupMap.get(groupID);

		if (theGroup == null) {
			theGroup = (Group) WrapperFactory.getWrapper(groupID, Group.OBJECT_NAME);

			this.groupMap.put(groupID, theGroup);
		}

		return theGroup;
	}

	/**
	 * Return the role provider out of the given security row.
	 * 
	 * @param    aRow        The row to get the role provider for, must not be <code>null</code>.
	 * @param    aRoleMap    The cache map for the role providers, must not be <code>null</code>.
	 * @return   The requested role provider, may be <code>null</code>.
	 */
	protected RoleProvider getRoleProvider(SimpleSecurityRow aRow, Map<Short, RoleProvider> aRoleMap) {
		Short reasonID = (Short) aRow.getReason();
		RoleProvider theProvider = aRoleMap.get(reasonID);

		if (theProvider == null) {
			theProvider = this.accessManager.getRulefromPersitancyId(Integer.valueOf(reasonID.intValue()));
			aRoleMap.put(reasonID, theProvider);
		}

		return theProvider;
	}

	/** 
	 * Return the name of the given meta object as single collection.
	 * 
	 * @param    aMO    The meta object to get its name from, must not be <code>null</code>..
	 * @return   The requested name as single value collection, never <code>null</code>.
	 */
	protected Collection<String> getMetaObjectNames(MetaObject aMO) {
		return Collections.singleton(aMO.getName());
	}

	/**
	 * Return the names of the meta objects which implement the given meta element.
	 * 
	 * @param    aME    The meta element to get the meta object names from, must not be <code>null</code>..
	 * @return   The requested meta object names, may be <code>null</code>.
	 */
	protected Collection<String> getMetaObjectNames(TLClass aME) {
		Map<String, ? extends Collection<TLClass>> potentialTables = TLModelUtil.potentialTables(aME, false);
		return potentialTables.keySet();
	}

	/**
	 * Return the meta object out of the given role provider.
	 * 
	 * @param    aProvider    The role provider to get the meta object from, may be <code>null</code>.
	 * @return   The requested meta object, may be <code>null</code>.
	 */
	protected MetaObject getMetaObject(RoleProvider aProvider) {
		return (aProvider instanceof RoleRule) ? ((RoleRule) aProvider).getMetaObject() : null;
	}

	/**
	 * Return the meta element out of the given role provider.
	 * 
	 * @param    aProvider    The role provider to get the meta element from, may be <code>null</code>.
	 * @return   The requested meta element, may be <code>null</code>.
	 */
	protected TLClass getMetaElement(RoleProvider aProvider) {
		return (aProvider instanceof RoleRule) ? ((RoleRule) aProvider).getMetaElement() : null;
	}

	/** 
	 * Convert the given row into a complex security row.
	 * 
	 * @param    aRow        The string based row, must not be <code>null</code>.
	 * @param    aKB         The knowledge base to find the {@link KnowledgeAssociation}.
	 * @return   The converted security row, never <code>null</code>.
	 */
	protected SecurityRow createDirectRoleRuleRow(SecurityRow aRow, KnowledgeBase aKB) {
		Short reason = (Short) aRow.getReason();

		if (SecurityStorage.REASON_HAS_ROLE.equals(reason)) {
			TLID groupID = (TLID) aRow.getTarget();
			TLID roleID = (TLID) aRow.getRole();
			BoundedRole theRole = (BoundedRole) WrapperFactory.getWrapper(roleID, BoundedRole.OBJECT_NAME);

			return new ComplexSecurityRow(this.getGroup(groupID), (TLID) aRow.getBO(), roleID, theRole, reason);
		}
		else {
			return aRow;
		}
	}

	/**
	 * Resolve a collection of business objects on demand. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public class ShowSecurityBusinessObjectResolver {

		// Attributes

		private Collection<TLID> idColl;

		private Map<TLID, TLObject> map;

		private Collection<String> moNames;

		// Constructors
		
		/** 
		 * Creates a {@link ShowSecurityBusinessObjectResolver}.
		 * 
		 * @param    someMONames   The requested meta object names, must not be <code>null</code> or empty.
		 */
		public ShowSecurityBusinessObjectResolver(Collection<String> someMONames) {
			if (CollectionUtil.isEmptyOrNull(someMONames)) {
				throw new IllegalArgumentException("Possible meta object names shouldn't be null or empty.");
			}

			this.moNames = someMONames;
			this.idColl = new ArrayList<>();
		}

		// Public methods

		/**
		 * Add the given ID to the business object IDs to be resolved later.
		 * 
		 * @param id
		 *        The ID to be used on lookup.
		 */
		public void addID(TLID id) {
			this.idColl.add(id);
		}

		/**
		 * Return the business object identified by the given ID.
		 * 
		 * @param id
		 *        The requested ID.
		 * @return The found business object.
		 */
		public Object get(TLID id) {
			if (this.map == null) {
				this.map = new HashMap<>();

				this.initMap(this.map);
			}

			return this.map.get(id);
		}

		// Private methods

		/**
		 * Initialize the given map by query the knowledge base for the business objects.
		 * 
		 * @param aMap
		 *        Initialize the given map by query.
		 */
		private void initMap(Map<TLID, TLObject> aMap) {
			DBKnowledgeBase       theKB      = (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
			List<KnowledgeObject> theResult  = new ArrayList<>();

			for (Iterator<List<TLID>> theIt = CollectionUtil.chunk(theKB.getChunkSize(), this.idColl.iterator()); theIt
				.hasNext();) {
				theResult.addAll(theKB.search(ExpressionFactory.queryUnresolved(
					ExpressionFactory.filter(
						this.getBaseExpression(),
						ExpressionFactory.inLiteralSet(
							ExpressionFactory
								.attribute(BasicTypes.ITEM_TYPE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME),
							theIt.next())))));
			}

			for (TLObject theWrapper : WrapperFactory.getWrappersForKOsGeneric(theResult)) {
				aMap.put(KBUtils.getWrappedObjectName(theWrapper), theWrapper);
			}
		}

		/**
		 * Return the moNames of the possible meta objects.
		 * 
		 * @return    The requested moNames, never <code>null</code>.
		 */
		private SetExpression getBaseExpression() {
			SetExpression theResult = null;

			for (String theMOName : this.moNames) {
				SetExpression thePart = ExpressionFactory.allOf(theMOName);

				theResult = (theResult == null) ? thePart : ExpressionFactory.union(theResult, thePart);
			}

			return theResult;
		}
	}
}

