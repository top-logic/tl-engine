/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.AbstractFunction;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.data.Function;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.MOFunctionImpl;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.template.model.ExpansionModel;
import com.top_logic.template.model.function.EqualsFunction;
import com.top_logic.template.model.function.ExistsFunction;
import com.top_logic.template.model.function.IsEmptyFunction;

/**
 * A demo implementation of an ExpansionModel for the template engine. It also creates the necessary
 * {@link TypeSystem} and some demo {@link MetaObject}s, that can be used in the template.
 * </p>
 * Demo structures with their attributes:</br>
 * <strong>Person</strong>:
 * <ul>
 * <li> vName[String] </li>
 * <li> nName[String] </li>
 * <li> isManager[boolean] </li>
 * <li> accounts[AccountList] </li>
 * </ul>
 * 
 * <strong>Account</strong>:
 * <ul>
 * <li> name[String] </li>
 * <li> login[String] </li>
 * <li> passwd[String] </li>
 * </ul>
 * 
 * <strong>AccountList</strong>:
 * <ul>
 * <li> list of [Account] </li>
 * </ul>
 * 
 * <strong>Group</strong>:
 * <ul>
 * <li> list of [Person] </li>
 * </ul>
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@DeactivatedTest("No test case but ExpansionModel for tests.")
public class TestExpansionModel implements ExpansionModel {
	
	public static final String GROUP_MO             = "Group";
	
	public static final String PERSON_MO            = "Person";
	public static final String PERSON_ATTR_N_NAME   = "nName";
	public static final String PERSON_ATTR_V_NAME   = "vName";
	public static final String PERSON_ATTR_POSITION = "isManager";
	public static final String PERSON_ATTR_CAR      = "hasCar";
	public static final String PERSON_ATTR_ACCOUNT  = "accounts";
	
	public static final String ACCOUNT_MO           = "Account";
	public static final String ACCOUNT_ATTR_NAME    = "name";
	public static final String ACCOUNT_ATTR_PW      = "passwd";
	public static final String ACCOUNT_ATTR_LOGIN   = "login";
	public static final String ACCOUNT_ATTR_ID      = "id";
	public static final String ACCOUNT_LIST         = "Accounts";


	private Map<String, MetaObject>       typesByNamespace;
	private Map<String, DataObject>       persons;
	private Map<String, List<DataObject>> groups;
	private Map<String, Function>         functions;
	
	
	private TypeSystem typeSystem;

	public TestExpansionModel() {
		try {
			this.typesByNamespace = new HashMap<>();
			this.persons          = new HashMap<>();
			this.groups           = new HashMap<>();
			this.functions        = new HashMap<>();
			
			/*
			 * Add some sample functions
			 */
			this.functions.put(EqualsFunction.FUNCTION_NAME, new EqualsFunction());
			this.functions.put(ExistsFunction.FUNCTION_NAME, new ExistsFunction());
			this.functions.put(IsEmptyFunction.FUNCTION_NAME, new IsEmptyFunction(this));
			this.functions.put(StringListFunction.FUNCTION_NAME, new StringListFunction());

			MetaObject theRoot = new MOKnowledgeItemImpl("root");
			
			/*
			 * Create the Account structure. An account has a name, a login and a password, all of
			 * type String.
			 */
			MOStructure  theAccountStruct = new MOStructureImpl(ACCOUNT_MO);
			theAccountStruct.addAttribute(new MOAttributeImpl(ACCOUNT_ATTR_NAME, MOPrimitive.STRING));
			theAccountStruct.addAttribute(new MOAttributeImpl(ACCOUNT_ATTR_LOGIN, MOPrimitive.STRING));
			theAccountStruct.addAttribute(new MOAttributeImpl(ACCOUNT_ATTR_ID, MOPrimitive.STRING));
			theAccountStruct.addAttribute(new MOAttributeImpl(ACCOUNT_ATTR_PW, MOPrimitive.STRING));
			theAccountStruct.freeze();

			/*
			 * Create the account list, a MOCollection of account structures.
			 */
			MOCollection theAccountList   = MOCollectionImpl.createListType(theAccountStruct);

			/*
			 * Create the person structure. A Person has a name (String), a family name (String), a
			 * boolean flag indicating an executive position and a list of accounts.
			 */
			MOStructure thePersonStruct = new MOStructureImpl(PERSON_MO);
			thePersonStruct.addAttribute(new MOAttributeImpl(PERSON_ATTR_V_NAME, MOPrimitive.STRING));
			thePersonStruct.addAttribute(new MOAttributeImpl(PERSON_ATTR_N_NAME, MOPrimitive.STRING));
			thePersonStruct.addAttribute(new MOAttributeImpl(PERSON_ATTR_POSITION, MOPrimitive.BOOLEAN));
			thePersonStruct.addAttribute(new MOAttributeImpl(PERSON_ATTR_CAR, MOPrimitive.BOOLEAN));
			thePersonStruct.addAttribute(new MOAttributeImpl(PERSON_ATTR_ACCOUNT, theAccountList));
			thePersonStruct.freeze();

			this.typesByNamespace.put(PERSON_MO, thePersonStruct);
			this.typesByNamespace.put(ACCOUNT_MO, theAccountStruct);

			this.typesByNamespace.put(ACCOUNT_LIST, theAccountList);
			this.typesByNamespace.put(GROUP_MO, MOCollectionImpl.createListType(thePersonStruct));

			// create a simple TypeSystem with the used structures and collections
			List<MetaObject> theTypes = new ArrayList<>();
			theTypes.add(thePersonStruct);
			theTypes.add(theAccountStruct);
			theTypes.add(theAccountList);
			theTypes.add(theRoot);
			
			this.typeSystem = new DefaultTypeSystem(theTypes);
		
			DataObject p1 = createPerson("Till", "Bentz", false, false);
			DataObject p2 = createPerson("Friedemann", "Schneider", false, false);
			DataObject p3 = createPerson("Theo", "Sattler", false, true);
			DataObject p4 = createPerson("Michael", "Gänsler", true, true);
			DataObject p5 = createPerson("Frank", "Mausz", false, true);

			ArrayList<DataObject> group = new ArrayList<>();
			group.add(p1);
			group.add(p2);
			group.add(p3);
			group.add(p4);
			group.add(p5);
			
			this.groups.put("Empl", group);
			
			ArrayList<DataObject> emptyGroup = new ArrayList<>();
			this.groups.put("Empty", emptyGroup);
			
			this.persons.put("TBE",p1);
			this.persons.put("FSC",p2);
			this.persons.put("TSA",p3);
			this.persons.put("MGA",p4);
		}
		catch (DataObjectException e) {
			Logger.error("Error setting the Demo Person objects", e, TestExpansionModel.class);
		}
	}

	/**
	 * Creates a {@link DataObject} representing a Person. The account list will automatically be
	 * created using the given information
	 * 
	 * @param aVName the name of the person
	 * @param aNName the family name of the person
	 * @param isManager a flag indicating whether this person is an executive or not
	 * @param hasCar a flag indicating the possession of a company car
	 * @return a {@link DataObject}.
	 */
	private DataObject createPerson(String aVName, String aNName, boolean isManager, boolean hasCar) throws DataObjectException {
		DataObject theDOI = new DefaultDataObject(typesByNamespace.get(PERSON_MO));
		
		theDOI.setAttributeValue(PERSON_ATTR_V_NAME, aVName);
		theDOI.setAttributeValue(PERSON_ATTR_N_NAME, aNName);
		theDOI.setAttributeValue(PERSON_ATTR_POSITION, isManager);
		theDOI.setAttributeValue(PERSON_ATTR_CAR, hasCar);
		theDOI.setAttributeValue(PERSON_ATTR_ACCOUNT, createAccountList(aNName, aVName));
		return theDOI;
	}

	private Object createAccountList(String aNName, String aVName) throws DataObjectException {
		List<DataObject> theResult = new ArrayList<>();
		
		String theLogin = (aVName.substring(0, 1) + aNName.substring(0, 2)).toLowerCase();
		String thePW    = theLogin + "123";

		for(int i = 0; i < 2; i++) {
			DataObject theDOI = new DefaultDataObject(typesByNamespace.get(ACCOUNT_MO));
			theDOI.setAttributeValue(ACCOUNT_ATTR_NAME, "acc" + i);
			theDOI.setAttributeValue(ACCOUNT_ATTR_LOGIN, theLogin);
			theDOI.setAttributeValue(ACCOUNT_ATTR_ID, "acc_" + theLogin);
			theDOI.setAttributeValue(ACCOUNT_ATTR_PW, i + thePW);
			
			theResult.add(theDOI);
		}
		return theResult;
	}

	@Override
	public MetaObject getTypeForVariable(String nameSpace, String name) {
		if (PERSON_MO.equals(nameSpace)) {
			return this.typesByNamespace.get(PERSON_MO);
		}
		else if (GROUP_MO.equals(nameSpace)) {
			return this.typesByNamespace.get(GROUP_MO);
		}
		return null;
	}

	@Override
	public Object getValueForVariable(String nameSpace, String name) {
		if (PERSON_MO.equals(nameSpace)) {
			return this.persons.get(name);
		}
		else if (GROUP_MO.equals(nameSpace)) {
			return this.groups.get(name);
		}
		return null;
	}

	@Override
	public TypeSystem getTypeSystem() {
		return this.typeSystem;
	}

	@Override
	public Function getImplementationForFunction(String name) {
		return this.functions.get(name);
	}

	@Override
	public MOFunction getTypeForFunction(String name) {
		Function theFunction = this.functions.get(name);
		if (theFunction != null) {
			return theFunction.getType();
		}
		else {
			return null;
		}
	}

	@Override
	public Iterator<?> getIteratorForObject(Object object) {
		if (object instanceof Collection<?>) {
			return ((Collection<?>)object).iterator();
		}
		return null;
	}
	
	private Object getDerivedDO(Object aValue, String anAttr) throws NoSuchAttributeException {
		if (!(aValue instanceof DataObject)) {
			throw new NoSuchAttributeException("wrong meta object type");
		}
		DataObject theDO = (DataObject) aValue;
		Object attribute = theDO.getAttributeValue(anAttr);
		return attribute;
	}

	@Override
	public Object getValueForAttribute(Object object, String attribute) throws NoSuchAttributeException {
		return getDerivedDO(object, attribute);
	}
	
	/**
	 * Function that transforms the comma separated list of arguments into a collection.
	 */
	public static class StringListFunction extends AbstractFunction {

		public static final String FUNCTION_NAME = "stringList";
		
		public StringListFunction() {
			this.moFunction = new MOFunctionImpl(FUNCTION_NAME, getReturnType(), getArgumentTypes(), true);
		}
		
		@Override
		public Object apply(List<?> arguments) {
			return arguments;
		}

		@Override
		protected List<MetaObject> getArgumentTypes() {
			return CollectionUtil.createList(MetaObject.ANY_TYPE);
		}

		@Override
		protected MetaObject getReturnType() {
			return MOCollectionImpl.createCollectionType(MetaObject.ANY_TYPE);
		}
	}

	@Override
	public Maybe<String> format(Object value) {
		return Maybe.none();
	}

}
