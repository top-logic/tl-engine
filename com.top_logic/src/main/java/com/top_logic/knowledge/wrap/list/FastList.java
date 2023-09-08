/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.annotation.EnumScope;
import com.top_logic.model.config.annotation.MultiSelect;
import com.top_logic.model.config.annotation.SystemEnum;
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.util.model.ModelService;

/**
 * A List of FastListElements designed for speed.
 * 
 * Although this is a FlexWrapper this implementation should not 
 * use any FlexAttribute by itself. This shall become the official successor
 * of the (bloated, old) POSList.
 * 
 * <ul>
 *  <li>FastList have no name, or description (its assumed they belong to some other object that provides one)</li>
 *  <li>This List is ordered by name (as of now) may become order by anything else by Subclassing.</li>
 *  <li>Changes in the Elements that affect sorting can be resolved by calling {@link #refetchWrapper()}</li>
 *  <li>In case of a cluster you must {@link #touch()} the List on change of any element</li>
 *  <li>The Multiselect feature is not enforced</li>
 *  <li>Attaching Fast List to Objects in some other Knowledgebase will
 *      not work due to the nature of Bridge KAs</li>
 * </ul>
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class FastList extends PersistentType implements TLEnumeration, Cloneable {

	/** Convenient Constant when specifying {@link #isSystem()} */
	public static final boolean SYSTEM = true;

    /** Convenient Constant when specifying Multiselect */
    public static final boolean MULTI_SELECT = true;

    /** Type Name of the underlying KnowledgeObject */
	public static final String OBJECT_NAME = TlModelFactory.KO_NAME_TL_ENUMERATION;

    /** Attribute for Description */
    public static final String DESC_ATTRIBUTE       = "descr";
    
	/** Attribute name for the default {@link FastListElement}. */
	public static final String DEFAULT_ATTRIBUTE = "default";

	private static final OrderedLinkQuery<FastListElement> ELEMENTS_QUERY = AssociationQuery.createOrderedLinkQuery(
		"elements", FastListElement.class,
		FastListElement.OBJECT_NAME, FastListElement.OWNER_ATTRIBUTE, FastListElement.ORDER_ATTRIBUTE,
		null, false);

	private static final OrderedLinkQuery<TLClassifier> CLASSIFIERS_QUERY = AssociationQuery.createOrderedLinkQuery(
		"elementsLive", TLClassifier.class,
		FastListElement.OBJECT_NAME, FastListElement.OWNER_ATTRIBUTE, FastListElement.ORDER_ATTRIBUTE,
		null, true, true);

    /**
     * Value of {@link #getClassificationType()} that marks the classification to be a checklist.
     */
	public static final String CHECKLIST_CLASSIFICATION_TYPE = "Checklist";

	/**
	 * Returns {@link TlModelFactory#getTLEnumerationType()}.
	 */
	@CalledByReflection
	public static TLStructuredType getFastListType() throws ConfigurationError {
		return TlModelFactory.getTLEnumerationType();
	}

	/**
	 * Returns the {@link AssociationQuery queries} that are used by instances of
	 * {@link TLEnumeration} to cache relations to other model elements.
	 */
	@FrameworkInternal
	public static List<AbstractAssociationQuery<? extends TLObject, ?>> getAssociationQueries() {
		List<AbstractAssociationQuery<? extends TLObject, ?>> queries =
			new ArrayList<>();
		queries.add(ELEMENTS_QUERY);
		queries.add(CLASSIFIERS_QUERY);
		return queries;
	}

    /**
     * Create a FastList as by Contract with the WrapperFactory.
     */
	public FastList(KnowledgeObject ko) {
        super(ko);
    }

    /** 
     * This methods is a hook for subclasses.
     */
    protected String getObjectName() {
        return FastListElement.OBJECT_NAME;
    }

	/**
	 * Updates the value of {@link #isOrdered()}.
	 * 
	 * @param ordered
	 *        New value of {@link #isOrdered()}.
	 */
	public void setOrdered(boolean ordered) {
		if (ordered == isOrdered()) {
			return;
		}
		if (ordered) {
			removeAnnotation(UnorderedEnum.class);
		} else {
			setAnnotation(TypedConfiguration.newConfigItem(UnorderedEnum.class));
		}

	}

    /** Return the type of this List.
     */
    public String getDescription() {
		return (String) tGetData(DESC_ATTRIBUTE);
    }

    /** 
     * Allow Changing of the Description
     */
    public void setDescription(String newDesc) {
		tSetData(DESC_ATTRIBUTE, newDesc);
    }

    /** Return the type of this List.
     */
	public String getClassificationType() {
		EnumScope scope = getAnnotation(EnumScope.class);
		if (scope != null) {
			return scope.getValue();
		}
		return EnumScope.DEFAULT_ENUM_SCOPE;
    }
    
    /**
     * Method isMultiSelect.
     * 
     * @return true if this list is a multi-selectable list
     */
    public boolean isMultiSelect() {
		return TLAnnotations.isMultiSelectDefault(this);
    }

	/**
	 * Optional Boolean Attribute indicating that the List is a system list,
	 * i.e. is not user modifiable.
	 * 
	 * @return true if this list is a system list
	 */
	public boolean isSystem() {
		SystemEnum systemEnum = getAnnotation(SystemEnum.class);
		if (systemEnum != null) {
			return systemEnum.getValue();
		}
		return false;
	}

    /** Return the number of List elements in this List
     */
    public int size() {
		return elementsDirect().size();
    }
    
    /** Return the default element of the list.
     * 
     * @return null when the List has no DefaultElement.
     */
    public FastListElement getDefaultElement() {
		KnowledgeObject defaultKO = (KnowledgeObject) tGetData(DEFAULT_ATTRIBUTE);
		if (defaultKO != null) {
			FastListElement element = (FastListElement) WrapperFactory.getWrapper(defaultKO);
			if (element.getList() != this) {
				Logger.error("Found default element '" + defaultKO + "' in list '" + getName()
					+ "' that does not match its list. " + "Expected: " + this + ", found " + element.getList(),
					FastList.class);
				return null;
            }
			return element;
		} else {
			return null;
        }
    }
    
    /** Set the default element of this list.
     */
    public void setDefaultElement(FastListElement anElement) {
		if (anElement != null && !elementsDirect().contains(anElement))
            throw new IllegalArgumentException("Element must be member of List");
        if (anElement == null) {
			tSetData(DEFAULT_ATTRIBUTE, null);
        } else {
			tSetData(DEFAULT_ATTRIBUTE, anElement.tHandle());
        }
    }

    /**
	 * Return a read only view of the FastListElements
	 * 
	 * @deprecated Use {@link #getClassifiers()}
	 */
	@Deprecated
    public List<FastListElement> elements() {
		return elementsDirect();
    }

	private List<FastListElement> elementsDirect() {
		return AbstractWrapper.resolveLinks(this, ELEMENTS_QUERY);
	}

    /** 
     * Return a FastListElement identified by its name.
     * 
     * @param    aName    The name to be looked up, <code>null</code> or "" will result in  <code>null</code>
     * @return   The requested element or <code>null</code>, if not found.
     */
	public FastListElement getElementByName(String aName) {
        if (!StringServices.isEmpty(aName)) {
			for (FastListElement element : elementsDirect()) {
				if (element.tValid() && aName.equals(element.getName())) {
					return element;
                }
            }
        }

        return null;
    }

    /**
     * Return a logic OR over all bit flags of the elements.
     * 
     * @return 0 in case no elements where found.
     */
	public int getFlagsORed() {
		int result = 0;
		for (FastListElement fle : elementsDirect()) {
            result |= fle.getFlags();
        }
        return result;
    }

    /**
     * Return a logic AND over all bit flags of the elements.
     *  
     * @return  0xFFFFFFFF in case no elements where found.
     */
	public int getFlagsANDed() {
		int result = 0xFFFFFFFF;
		for (FastListElement fle : elementsDirect()) {
            result &= fle.getFlags();
        }
        return result;
    }

    /**
     * Return true when the CHECKED bit is set in all Elements.
     */
	public boolean isAllChecked() {
		for (FastListElement fle : elementsDirect()) {
            if (!fle.isChecked())
                return false;
        }
        return true;
    }
    
    /** 
     * Return a modifiable copy of the FastListElements 
     */
    public List<FastListElement> copyElements() {
		return new ArrayList<>(elements());
    }

    /**
     * Convenience method to return first Element in List.
     * 
     * @return null when list is empty.
     */
    public FastListElement first() { 
		List<FastListElement> elements = elements();
		if (!elements.isEmpty())
            return elements.get(0);
        return null;
    }

    /**
     * Convenience method to return last Element in List.
     * 
     * @return null when list is empty.
     */
    public FastListElement last() { 
		List<FastListElement> elements = elements();
		if (!elements.isEmpty())
            return elements.get(elements.size() - 1);
        return null;
    }
    
	/**
	 * Helper function to create a new FastListElement
	 * 
	 * This Object is invalid until correctly added to the list.
	 */
	private FastListElement createElement(String name, String desc, int flags) {
		{
			KnowledgeObject ko = tKnowledgeBase().createKnowledgeObject(FastListElement.OBJECT_NAME);
			ko.setAttributeValue(FastListElement.NAME_ATTRIBUTE, name);
			if (desc != null) {
				ko.setAttributeValue(FastListElement.DESC_ATTRIBUTE, desc);
			}
			if (flags != 0) {
				ko.setAttributeValue(FastListElement.FLAGS_ATTRIBUTE, Integer.valueOf(flags));
			}

			TLClassifier newElement = (TLClassifier) WrapperFactory.getWrapper(ko);
			getClassifiers().add(newElement);

			return (FastListElement) newElement;
		}
	}

    /**
	 * create a new list element and add it at the end of the list.
	 * 
	 * @param id
	 *        the unique id of the new list element
	 * @param name
	 *        the name of the new element
	 * @param desc
	 *        the description of the new element (may be <code>null</code>
	 * @param flags
	 *        some flags for the list element
	 * @return the newly created list element
	 */
	public FastListElement addElement(TLID id, String name, String desc,
			int flags) {
		FastListElement existingElement = getElementByName(name);
		if (existingElement != null) {
			throw new IllegalArgumentException("Duplicate entry for name '" + name + "'");
		}
		return createElement(name, desc, flags);
	}

    /**
     * This method adds the given fastlist element to this list.
     * 
     * @param anElement
     *        The fastlist element to add. Must not be <code>null</code>.
     */
    public void addElement(FastListElement anElement) {
		checkNotContained(anElement);
		elementsDirect().add(anElement);
    }
    
	private void checkNotContained(FastListElement anElement) throws IllegalArgumentException {
		if (elementsDirect().contains(anElement)) {
			throw new IllegalArgumentException("'" + this + "' already contains '" + anElement + "'");
		}
	}

    /**
     * Remove anElem from this list and actually destroy it. 
     * 
     * @return false when element is not member of the list.
     */
    public boolean removeElement(FastListElement anElem) {
		if (anElem.tValid() && elementsDirect().contains(anElem)) {
			anElem.tDelete();
			if (anElem == getDefaultElement()) {
				// removing the default resets it to null.
				tSetData(DEFAULT_ATTRIBUTE, null);
            }
            return true;
        }
        // else
        Logger.info("Tried to removeElement not contained, or invalid", this);
        return false;
    }

    /**
	 * This method returns a list containing of all {@link TLObject}s with {@link KnowledgeObject}
	 * {@link #OBJECT_NAME}.
	 */
	public static List<FastList> getAllLists() {
		KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
		
		return getAllLists(theKB);
	}

	private static List<FastList> getAllLists(KnowledgeBase kb) {
		ArrayList<KnowledgeObject> allListKOs = new ArrayList<>();
		allListKOs.addAll(kb.getAllKnowledgeObjects(OBJECT_NAME));
		return WrapperFactory.getWrappersForKOs(FastList.class, allListKOs);
	}

	/** Standard getInstance() method for a Wrapper */
    public static FastList getFastList(KnowledgeObject aKO) {
		return (FastList) WrapperFactory.getWrapper(aKO);
    }

    /**
     * Standard getInstance() method for a Wrapper. 
     * 
     * @param name Identifier of a FastList.
     * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}. There may be more than one
	 *             list with a certain local name in different modules.
	 */
	@Deprecated
    public static FastList getFastList(String name, KnowledgeBase aKB) {
		return (FastList) WrapperFactory.getWrapper((KnowledgeObject) aKB.getObjectByAttribute(OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, name));
    }

    /**
     * Standard getInstance() method for a Wrapper. 
     * 
     * @param    name    Identifier of a FastList.
     * @return   The requested list.
     * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}. There may be more than one
	 *             list with a certain local name in different modules.
	 */
	@Deprecated
    public static FastList getFastList(String name) {
		return getFastList(name, PersistencyLayer.getKnowledgeBase());
    }

	/**
	 * Creates a new list non system, ordered list with the given parameters.
	 */
	public static FastList createEnum(KnowledgeBase aBase, String aName, String aDesc, String aType, boolean isMulti) {
		return createFastList(aBase, aName, aDesc, aType, isMulti, false, null);
	}
    
	/**
	 * Creates a new list with the given parameters.
	 * 
	 * @param aBase
	 *        The knowledge base to store the list in, must nor be <code>null</code>.
	 * @param aName
	 *        The name of the list, must not be <code>null</code>.
	 * @param aDesc
	 *        The description of the list (optional).
	 * @param aType
	 *        The type of the list, must not be <code>null</code>.
	 * @param isMulti
	 *        Flag, if list is a multi selection list.
	 * @param isSystem
	 *        see {@link #isSystem()}
	 * @param unordered
	 *        Flag whether the {@link TLEnumeration} shall be unordered. May be <code>null</code>,
	 *        which means ordered.
	 * @return The requested list, never <code>null</code>.
	 */
	public static FastList createFastList(KnowledgeBase aBase, String aName, String aDesc, String aType,
			boolean isMulti,
			boolean isSystem, TLAnnotation unordered) {

		if (listExists(aName, aBase)) {
			throw new IllegalArgumentException("There already exists a list with name " + aName);
		}

		TLModel applicationModel = ModelService.getApplicationModel();
		TLModule module = applicationModel.getModule(ModelLayout.TL5_ENUM_MODULE);

		FastList newEnum = createEnum(aBase, module, module, aName, aDesc);

		{
			if (aType != null) {
				EnumScope enumScope = TypedConfiguration.newConfigItem(EnumScope.class);
				enumScope.setValue(aType);
				newEnum.setAnnotation(enumScope);
			}

			if (isMulti) {
				MultiSelect multipleEnum = TypedConfiguration.newConfigItem(MultiSelect.class);
				multipleEnum.update(multipleEnum.descriptor().getProperty(MultiSelect.VALUE), true);
				newEnum.setAnnotation(multipleEnum);
			}
			if (unordered != null) {
				newEnum.setAnnotation(unordered);
			}
			if (isSystem) {
				SystemEnum systemEnum = TypedConfiguration.newConfigItem(SystemEnum.class);
				systemEnum.update(systemEnum.descriptor().getProperty(SystemEnum.VALUE), true);
				newEnum.setAnnotation(systemEnum);
			}
			return newEnum;
        }
    }

	/**
	 * Creates a new {@link TLEnumeration} in the given scope and module.
	 */
	public static FastList createEnum(KnowledgeBase kb, TLModule module, TLScope scope, String name, String description) {
		if (name == null) {
            throw new NullPointerException("Given name for list is null");
        }

		KnowledgeObject theKO = kb.createKnowledgeObject(OBJECT_NAME);
		theKO.setAttributeValue(NAME_ATTRIBUTE, name);
		if (scope != null) {
			theKO.setAttributeValue(SCOPE_ATTR, scope.tHandle());
		}
		if (module != null) {
			theKO.setAttributeValue(MODULE_ATTR, module.tHandle());
		}
		if (description != null) {
			theKO.setAttributeValue(DESC_ATTRIBUTE, description);
		}
		FastList newEnum = (FastList) WrapperFactory.getWrapper(theKO);
		return newEnum;
	}

	/**
	 * Checks whether there is list with the given name in the given {@link KnowledgeBase}.
	 * 
	 * @param aName
	 *        The name of the fast list to check.
	 * 
	 * @return <code>true</code> iff there is a {@link FastList} with the given name.
	 * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}.
	 */
	@Deprecated
	public static boolean listExists(String aName, KnowledgeBase aBase) {
		return getFastList(aName, aBase) != null;
	}

	/**
	 * Checks whether there is list with the given name in the default {@link KnowledgeBase}.
	 * 
	 * @param aName
	 *        The name of the fast list to check.
	 * 
	 * @return <code>true</code> iff there is a {@link FastList} with the given name.
	 */
	public static boolean listExists(String aName) {
		return listExists(aName, PersistencyLayer.getKnowledgeBase());
	}

    /**
     * Return a List of all FastList with given Type.
     */
    public static List<FastList> getListsForType(KnowledgeBase aKBase, String listType) {
		List<FastList> allLists = getAllLists(aKBase);
		FilterUtil.filterInline(new ClassificationTypeFilter(listType), allLists);
		return allLists;
    }

    /**
     * Return a collection of known list types for FastLists.
     * 
     * @param     kb          The knowledge base to look up the list types.
     * @return    The collection of known list types.
     */
    public static Collection<String> getAllListTypes(KnowledgeBase kb) {
		List<FastList> allLists = getAllLists(kb);
		if (allLists.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> result = new HashSet<>();
		for (FastList list : allLists) {
			result.add(list.getClassificationType());
		}
		return result;
    }

    /**
     * Return a collection of known list types for FastLists in the default knowledge base.
     * 
     * @return   The collection of known list types.
     */
    public static Collection<String> getAllListTypes() {
		return (FastList.getAllListTypes(PersistencyLayer.getKnowledgeBase()));
    }

    /**
	 * Return a List of all {@link FastList} with given Type.
	 */
    public static List<FastList> getListsForType(String listType) {
		return getListsForType(PersistencyLayer.getKnowledgeBase(), listType);
    }

	@Override
	public List<TLClassifier> getClassifiers() {
		return AbstractWrapper.resolveLinks(this, CLASSIFIERS_QUERY);
	}

	private static class ClassificationTypeFilter implements Filter<FastList> {

		private final String _expectedClassificationType;

		public ClassificationTypeFilter(String classificationType) {
			_expectedClassificationType = classificationType;
		}

		@Override
		public boolean accept(FastList anObject) {
			return anObject.getClassificationType().equals(_expectedClassificationType);
		}

	}

}
