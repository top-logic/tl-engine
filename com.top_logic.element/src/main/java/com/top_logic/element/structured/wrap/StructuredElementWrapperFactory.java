/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Collection;
import java.util.Comparator;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.core.TLElementComparator;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.MetaElementPreload;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.util.StructuredElementUtil;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.util.TLModelUtil;

/**
 * Factory to create StructuredElement based on Wrappers.
 * 
 * This is <em>not</em> a {@link com.top_logic.knowledge.wrap.WrapperFactory}
 * 
 * TODO better cache the root element ?
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementWrapperFactory extends StructuredElementFactory {

    /** Default comparator to use when sorting children */
    public static final Comparator DEFAULT_COMPARATOR = new StructuredElementWrapperComparator();

	private static final PreloadOperation DEFAULT_COMPARE_PRELOAD = MetaElementPreload.INSTANCE;
    
    @Override
	public Comparator getComparator() {
        return DEFAULT_COMPARATOR;
    }

	@Override
	public PreloadOperation getComparePreload() {
		return DEFAULT_COMPARE_PRELOAD;
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createChild(String, StructuredElementWrapper, String, TLClass)}
	 *             instead.
	 */
	@Deprecated
	protected StructuredElement createChild(String aStructureName, StructuredElementWrapper aWrapper, String aName,
			String typeName) throws CreateElementException {
		TLClass type = StructuredElementUtil.resolveChildType(aWrapper, typeName);
		return createChild(aStructureName, aWrapper, aName, type);
    }

    /**
     * Create a new child for the given element.
     * 
     * @param	  aStructureName the structure name
     * @param     aWrapper    The parent of the element to be created, may be <code>null</code> for the root element.
     * @param     aName       The name of the element to be created, must not be <code>null</code> or empty.
     * @param     aType       The type of the element to be created, must not be <code>null</code>.
     * @return    The new created element, never <code>null</code>.
     * @throws    CreateElementException    If creation process fails for a reason.
     */
    protected StructuredElement createChild(String aStructureName, StructuredElementWrapper aWrapper, String aName, TLClass aType) throws CreateElementException {
		return (this.createChild(aStructureName, aWrapper, null, aName, aType));
    }

    /**
     * Return the default knowledgebase to be used for elements.
     * 
     * @return    The requested knowledgebase.
     */
    protected KnowledgeBase getKnowledgeBase() {
        return PersistencyLayer.getKnowledgeBase();
    }

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createChild(String, StructuredElement, TLID, String, TLClass)}
	 *             instead.
	 */
	@Deprecated
	protected StructuredElement createChild(String structureName, StructuredElementWrapper parentElement,
			TLID id, String name, String typeName)
    			throws CreateElementException {
		TLClass type = StructuredElementUtil.resolveChildType(parentElement, typeName);
		return createChild(structureName, parentElement, id, name, type);
    }

	/**
	 * Create a new child for the given element.
	 * 
	 * @param structureName
	 *        The structure the new element lives in
	 * @param parentElement
	 *        The parent of the element to be created, may be <code>null</code> for the root
	 *        element.
	 * @param id
	 *        The ID of the new created element, may be <code>null</code>, which will create a
	 *        unique ID.
	 * @param name
	 *        The name of the element to be created, must not be <code>null</code> or empty.
	 * @param type
	 *        The type of the element to be created, must not be <code>null</code> or empty.
	 * @return The new created element, never <code>null</code>.
	 * @throws CreateElementException
	 *         If creation process fails for a reason.
	 */
	public StructuredElement createChild(String structureName, StructuredElement parentElement,
			TLID id, String name, TLClass type)
			throws CreateElementException {
		if (StringServices.isEmpty(name)) {
			throw new IllegalArgumentException("Given name is null or empty (in createChild)");
		}
		if (type == null) {
			throw new IllegalArgumentException("Given type is null or empty (in createChild)");
		}
		AttributedStructuredElementWrapper parent = (AttributedStructuredElementWrapper) parentElement;
		checkStructure(structureName);

		try {
			KnowledgeBase kb;
			Branch branch;
			if (parent != null) {
				kb = parent.getKnowledgeBase();
				branch = WrapperHistoryUtils.getBranch(parent);
			} else {
				kb = getKnowledgeBase();
				branch = kb.getHistoryManager().getContextBranch();
			}
			KnowledgeObject knowledgeObject =
				kb.createKnowledgeItem(branch, id, TLAnnotations.getTable(type),
					new NameValueBuffer()
						.put(PersistentObject.TYPE_REF, type.tHandle()),
					KnowledgeObject.class);

			AttributedStructuredElementWrapper child =
				(AttributedStructuredElementWrapper) WrapperFactory.getWrapper(knowledgeObject);
			if (name != null) {
				child.setElementName(name);
			}

			setupLocalScope(child);
			if (parent != null) {
				AttributeOperations.addValue(parent, StructuredElement.CHILDREN_ATTR, child);
			}

			setupDefaultValues(parentElement, child, type);

			return (child);
		} catch (Exception ex) {
			throw new CreateElementException("Unable to create element ('" + name + "','" + qualifiedName(type)
				+ "') in module '" + structureName + "'.", ex);
		}
	}

	@Override
	public TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues) {
		TLObject result = super.createObject(type, context, initialValues);
		if (result instanceof TLScope) {
			setupLocalScope((TLScope) result);
		}
		return result;
	}

	private void setupLocalScope(TLScope child) {
		MetaElementFactory factory = MetaElementFactory.getInstance();
		factory.setupLocalScope(getModule(), child, child.tType().getName());
	}

	private void checkStructure(String moduleName) {
		if (!moduleName.equals(getModuleName())) {
			throw new IllegalArgumentException("Request for non-existent structure: " + moduleName);
		}
	}

	/**
	 * The scope reference to search the concrete type of new child of the given parent element with
	 * the given type.
	 */
	public static ScopeRef getScopeRef(StructuredElement parent, String targetType) {
		if (parent == null) {
			return AttributeOperations.globalTypeRef(targetType);
		}
		return getScopeRef(parent.getStructureName(), parent.getElementType(), targetType);
	}

	/**
	 * The scope to search the concrete target type for a new object of the given target type in the
	 * context of an instance of the given class type.
	 * 
	 * @param moduleName
	 *        The module of the given class type.
	 * @param className
	 *        The (local) class name of the parent object.
	 * @param targetType
	 *        The type of the new object to create.
	 * 
	 * @return The specification of the target type.
	 */
	public static ScopeRef getScopeRef(String moduleName, String className, String targetType) {
		TLClass type = MetaElementUtil.getGlobalType(moduleName, className);
		
		for (TLStructuredTypePart containment : MetaElementUtil.getContainments(type)) {
			TLClass containmentType = (TLClass) containment.getType();
			if (!containmentType.getModule().getName().equals(moduleName)) {
				continue;
			}
			/* When there is no "FooChild" type, the ScopeRef is directly on the type of the
			 * attribute. */
			if (containmentType.getName().equals(targetType)) {
				return AttributeOperations.getScopeRef(containmentType);
			}
			/* When there is a "FooChild" type, the ScopeRef is on the subtypes of the attribute's
			 * type. */
			for (TLClass subtype : containmentType.getSpecializations()) {
				if (subtype.getName().equals(targetType)) {
					return AttributeOperations.getScopeRef(subtype);
				}
			}
		}
		throw new IllegalArgumentException("In '" + moduleName + TLModelUtil.QUALIFIED_NAME_SEPARATOR + className
			+ "' there is no containment of type '" + targetType
			+ "', allowed types are '" + MetaElementUtil.getContainmentNodeNames(type) + "'.");
	}

    /**
     * Comparator for structured elements.
     * 
     * The comparison will be done via the "sortOrder" and the "name" of the
     * elements. If both elements are StructureContexts, the comparison will
     * only look at the "name".
     * 
     * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */
    private static class StructuredElementWrapperComparator extends TLElementComparator {
        
		/**
		 * Prohibit creation of this class (use
		 * {@link StructuredElementWrapperFactory#DEFAULT_COMPARATOR} instead).
		 */
        protected StructuredElementWrapperComparator() {
            super();
        }

        @Override
		public int compare(Object anObject1, Object anObject2) {
			if (Utils.equals(anObject1, anObject2)) {
				return 0;
			}
            StructuredElementWrapper theElement1 = (StructuredElementWrapper) anObject1;
            StructuredElementWrapper theElement2 = (StructuredElementWrapper) anObject2;

            if (!theElement1.tValid()) {
                return (theElement2.tValid() ? -1 : 0); 
            }

            int theResult = this.compareSortOrder(theElement1, theElement2);

            if (theResult == 0) {
                theResult = this.compareName(theElement1, theElement2);
            }

            return (theResult);
        }

		protected int compareSortOrder(StructuredElement left, StructuredElement right) {
			int leftIndex = getSortOrderId(left);
			int rightIndex = getSortOrderId(right);
			return Integer.compare(leftIndex, rightIndex);
        }

		private int getSortOrderId(StructuredElement left) {
			try {
				AssociationStorage storage = parentStorage(left);
				String orderAttribute;
				if (storage instanceof LinkStorage) {
					orderAttribute =
						((LinkStorage.Config<?>) ((LinkStorage<?>) storage).getConfig()).getOrderAttribute();
				} else {
					orderAttribute = LinkStorage.SORT_ORDER;
				}
				Object orderId = getParentAssociation(left, storage).getAttributeValue(orderAttribute);
				if (orderId == null) {
					/* The element is probably new and has no sort-order yet. As new elements are
					 * added to the end of the list, Integer.MAX_VALUE is correct here. */
					return Integer.MAX_VALUE;
				}
				return (Integer) orderId;
			} catch (NoSuchAttributeException ex) {
				throw new RuntimeException("Failed to resolve sort order for: " + debug(left)
					+ ". Cause: " + ex.getMessage(), ex);
			}
		}

		private KnowledgeAssociation getParentAssociation(StructuredElement element, AssociationStorage parentStorage) {
			Collection<KnowledgeAssociation> parentLinks = getParentAssociations(element, parentStorage);
			if (parentLinks.isEmpty()) {
				return null;
			}
			return parentLinks.iterator().next();
		}

		private Collection<KnowledgeAssociation> getParentAssociations(StructuredElement element,
				AssociationStorage parentStorage) {
			AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> query =
				parentStorage.getOutgoingQuery();
			return AbstractWrapper.resolveLinks(element, query);
		}

		private AssociationStorage parentStorage(StructuredElement element) {
			TLClass type = (TLClass) element.tType();
			TLStructuredTypePart parentAttr = type.getPart(StructuredElementWrapper.PARENT_ATTR);
			return (AssociationStorage) AttributeOperations.getStorageImplementation(element, parentAttr);
		}

    }

}
