/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KAIterator;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Static utilities for {@link ElementAccessManager}
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementAccessHelper {

    private ElementAccessHelper() {
        super();
    }

    /**
     * Get a map of access rights declared for the given classifier.
     */
	public static Map<String, Set<BoundedRole>> getRolesMap(FastListElement aClassifier) {
		Map<String, Set<BoundedRole>> theRolesMap = new HashMap<>();
		for (Iterator<KnowledgeAssociation> theAIt =
			((KnowledgeObject) aClassifier.tHandle()).getOutgoingAssociations(PersBoundComp.NEEDS_ROLE); theAIt
				.hasNext();) {
			KnowledgeAssociation theAss = theAIt.next();
            String theAccessRight = (String) theAss.getAttributeValue(PersBoundComp.ATTRIBUTE_CMDGRP);
			TLObject theRole = WrapperFactory.getWrapper(theAss.getDestinationObject());
			Set<BoundedRole> theRoles = theRolesMap.get(theAccessRight);
            if (theRoles == null) {
				theRoles = new HashSet<>();
                theRolesMap.put(theAccessRight, theRoles);
            }
			theRoles.add((BoundedRole) theRole);
        }
        return theRolesMap;
    }

    /**
     * Add access to the PersBoundComp using a CommandGroup for a given BoundRole.
     *
     *
     *
     * @param   aGroup      The CommandGroups that will require the role.
     * @param   aRole       the BoundRole to add access for.
     *
     * @return false when an KnowledgeAssociation aRole was already attached,
     *         true indicates that you should call commit()
     */
	public static boolean addAccessRight(TLObject aClassification, String aGroup, BoundedRole aRole) {
		{
			KnowledgeObject theClassKO = (KnowledgeObject) aClassification.tHandle();
            KnowledgeObject theRoleKO  = aRole.tHandle();
			Iterator<KnowledgeAssociation> theIt =
				theClassKO.getOutgoingAssociations(PersBoundComp.NEEDS_ROLE, theRoleKO);
            if (theIt != null) {
                while (theIt.hasNext()) {
					KnowledgeAssociation theAssociation = theIt.next();
                    if (theAssociation.getAttributeValue(PersBoundComp.ATTRIBUTE_CMDGRP).equals(aGroup)) {
                       return false;
                    }
                }
            }
			KnowledgeAssociation needsRoleKA = aClassification.tHandle().getKnowledgeBase()
				.createAssociation(theClassKO, theRoleKO, PersBoundComp.NEEDS_ROLE);
            needsRoleKA.setAttributeValue(PersBoundComp.ATTRIBUTE_CMDGRP, aGroup);
            return true;
		}
    }

    /**
     * This method removes all {@link PersBoundComp#NEEDS_ROLE} associations from a given wrapper.
     * This can be used both for removing the calculated classifier access from {@link TLStructuredTypePart}s,
     * and to remove the declared access on classifiers.
     */
	public static void clearAccessRights(TLObject theElement) {
		KnowledgeObject ko = (KnowledgeObject) theElement.tHandle();
		KBUtils.deleteAllKI(ko.getOutgoingAssociations(PersBoundComp.NEEDS_ROLE));
    }

	public static Collection<String> getAccessRights(TLClassifier aClassifier, BoundRole aBO) {
		if (aClassifier == null || aBO == null || !(aBO instanceof TLObject)) {
            return Collections.emptySet();
        } else {
            Collection<String> theResult = new HashSet<>();
			TLObject theBO = (TLObject) aBO;
            try {
				for (Iterator<KnowledgeAssociation> theIt =
					((KnowledgeObject) aClassifier.tHandle()).getOutgoingAssociations(PersBoundComp.NEEDS_ROLE,
						(KnowledgeObject) theBO.tHandle()); theIt.hasNext();) {
					KnowledgeAssociation theAss = theIt.next();
                    String theAccessRight = (String) theAss.getAttributeValue(PersBoundComp.ATTRIBUTE_CMDGRP);
                    theResult.add(theAccessRight);
                }
            }
            catch (Exception e) {
                Logger.error("Unable to determin maximum access right. Return null.", e, ElementAccessHelper.class);
                return null;
            }
            return theResult;
        }

    }

    /**
     * Get the access rights per role
     *
     * @return Map < BoundedRole , Set < BoundCommandGroup > >
     *         Map < role , Set < access right > >
     */
	public static Map<BoundedRole, Set<BoundCommandGroup>> getAccessRights(TLObject aClassifier) {
        Map<BoundedRole,Set<BoundCommandGroup>> theResult = new HashMap<>();
		{
			KnowledgeObject theKO = (KnowledgeObject) aClassifier.tHandle();
            Iterator<KnowledgeAssociation> theIt = theKO.getOutgoingAssociations(PersBoundComp.NEEDS_ROLE);
            
            while (theIt.hasNext()) {
                KnowledgeAssociation   theKA            = theIt.next();
                String                 theAccessRightId = (String) theKA.getAttributeValue(PersBoundComp.ATTRIBUTE_CMDGRP);
                BoundCommandGroup      theAccess        = CommandGroupRegistry.resolve(theAccessRightId);
				BoundedRole theRole = (BoundedRole) WrapperFactory.getWrapper(theKA.getDestinationObject());
                Set<BoundCommandGroup> theAccessList    = theResult.get(theRole);
            
                if (theAccessList == null) {
                    theAccessList = new HashSet<>();
                    theResult.put(theRole, theAccessList);
                }
                
                theAccessList.add(theAccess);
            }
        }
        return theResult;
    }

	public static Map<String, TLClass> getUniqueMetaElements() {
        MetaElementFactory theMEFactory = MetaElementFactory.getInstance();
        Map<String, TLClass> theUniqueMEs = new HashMap<>();
		for (TLClass theME : theMEFactory.getGlobalMetaElements()) {
			put(theUniqueMEs, theME, theME.getName());
			put(theUniqueMEs, theME, TLModelUtil.qualifiedName(theME));
        }
        return theUniqueMEs;
    }

	private static void put(Map<String, TLClass> buffer, TLClass me, String name) {
		if (buffer.containsKey(name)) {
			// mark as duplicate
			buffer.put(name, null);
		} else {
			buffer.put(name, me);
		}
	}

    public static List<BoundRole> getAvailableRoles(TLClass aME, ElementAccessManager accessManager) {
        List<BoundRole> theList = new ArrayList<>();
        if (aME != null) {
            theList.addAll(accessManager.getRolesForMetaElement(aME));
        } else {
            HashSet<BoundRole> theRoles = new HashSet<>();
            for (Iterator<TLClass> theIt = accessManager.getSupportedMetaElements().iterator(); theIt.hasNext(); ) {
                TLClass theME = theIt.next();
                theRoles.addAll(getAvailableRoles(theME, accessManager));
            }
			Collection<BoundRole> theElementRoles = accessManager.getSecurityRoot().getRoles();
            if (theElementRoles != null) {
            	theRoles.addAll(theElementRoles);
            }
            theList.addAll(theRoles);
        }
        return theList;
    }

	public static TLClassifier getClassifier(String aClassifierName) {
        return FastListElement.getElementByName(aClassifierName);
    }

    /**
     * Set the attribute classifiers for the given meta element
     *
     * @param aMetaElementName   the name of the meta element.
     * @param someAttributeClassifications
     *            the classifications for the meta attributes
     *            Map < String, < Collecion < Wrapper > >
     *            Key:   meta attribute name
     *            Value: classifiers for the meta attribute
     *
     */
    public static void setClassifiers(String aMetaElementName, Map<String, Collection<String>> someAttributeClassifications) throws NoSuchAttributeException {
		TLClass theME = getUniqueMetaElements().get(aMetaElementName);
        if (theME == null) {
            throw new IllegalArgumentException("Unknown meta element: "+aMetaElementName);
        }
		Map<TLStructuredTypePart, Collection<TLClassifier>> theMap = new HashMap<>();
        for (Iterator<Map.Entry<String, Collection<String>>> theIt = someAttributeClassifications.entrySet().iterator(); theIt.hasNext();) {
            Map.Entry<String, Collection<String>> theEntry = theIt.next();
            String                      theAttributneName  = theEntry.getKey();
            Collection<String> theClassifierNames = theEntry.getValue();
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, theAttributneName);
			Collection<TLClassifier> theClassifiers = new HashSet<>();
            for (Iterator<String> theCIt = theClassifierNames.iterator(); theCIt.hasNext();) {
                String theClassifierName = theCIt.next();
				TLClassifier theClassifier = getClassifier(theClassifierName);
                if (theClassifier == null) {
                    throw new IllegalArgumentException("Unknown ckassifier: "+theClassifierName);
                }
                theClassifiers.add(theClassifier);
            }
            theMap.put(theMA, theClassifiers);
        }
		for (Iterator<Map.Entry<TLStructuredTypePart, Collection<TLClassifier>>> theIt = theMap.entrySet().iterator(); theIt
			.hasNext();) {
			Map.Entry<TLStructuredTypePart, Collection<TLClassifier>> theEntry = theIt.next();
			TLStructuredTypePart theMA = theEntry.getKey();
			Collection<TLClassifier> theClassifiers = theEntry.getValue();
            AttributeOperations.setClassifiers(theMA, theClassifiers);
        }
    }

    /**
     * @param someClassifierRoles the classifier roles
     *              Map < String, Map < String, Collection < String > > >
     *              classifier name -> access right -> role name
     */
	public static void setClassifierRoles(
			Map<String, ? extends Map<String, ? extends Map<String, ?>>> someClassifierRoles) {
        KnowledgeBase  theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		for (Map.Entry<String, ? extends Map<String, ? extends Map<String, ?>>> theCEntry : someClassifierRoles
			.entrySet()) {
			String theCName = theCEntry.getKey();
			Map<String, ? extends Map<String, ?>> theCValue = theCEntry.getValue();
			TLClassifier theClassifier = getClassifier(theCName);
            if (theClassifier == null) {
                throw new IllegalArgumentException("Unknown classifier '"+theCEntry+"' passed in.");
            }
            try {
                // first remove old classifiers
                ElementAccessHelper.clearAccessRights(theClassifier);
            }
            catch (Exception e) {
                throw new WrapperRuntimeException(e);
            }


			for (Map.Entry<String, ? extends Map<String, ?>> theREntry : theCValue.entrySet()) {
				String theAccessName = theREntry.getKey();
				Map<String, ?> theRValue = theREntry.getValue();
				List<TLObject> theRoles;
				for (String theRName : theRValue.keySet()) {

                    try {
						theRoles = WrapperFactory.getWrappersByAttribute(BoundedRole.OBJECT_NAME, theKB,
							BoundedRole.NAME_ATTRIBUTE, theRName);
                    }
                    catch (UnknownTypeException e) {
                        throw new WrapperRuntimeException(e);
                    }
                    if (theRoles == null || theRoles.isEmpty()) {
                        throw new IllegalArgumentException("No Role named '"+theRName+"' found.");
                    }
                    if (theRoles.size() > 1) {
                        throw new IllegalArgumentException("Role named '"+theRName+"' not unique.");
                    }
                    BoundedRole theRole = (BoundedRole) theRoles.get(0);
                    addAccessRight(theClassifier, theAccessName, theRole);
                }

            }
        }
    }


    /**
     * Get all Objects affected by the given rule and using the given meta attribute association
     *
     * @param aRule   the rule to check
     * @param aMA     the meta attribute the association is mapped to
     * @param aKA     the association (which is assumably part of the path)
     * @return the wrappers effected
     */
    public static Set<BoundObject> traversRoleRuleBackwards(RoleRule aRule, TLStructuredTypePart aMA, KnowledgeAssociation aKA) {

        List<PathElement> thePath = aRule.getPath();

        int           thePathLength = thePath.size();
        List<Integer> theMALocations = new ArrayList<>();

        // locate the meta attribute, may appear more than once
        for (int i=0; i < thePathLength; i++) {
            PathElement thePE = thePath.get(i);
            TLStructuredTypePart theMetaAttribute = thePE.getMetaAttribute();
            if (theMetaAttribute != null && theMetaAttribute.equals(aMA)) {
				theMALocations.add(Integer.valueOf(i));
            }
        }

        return taversRoleRuleBackwards(aRule, aKA, thePath, theMALocations);
    }

    /**
     * Get all Objects affected by the given rule and using the given association
     *
     * @param aRule   the rule to check
     * @param aKA     the association (which is assumably part of the path)
     * @return the Wrppers effected
     */
    public static Set<BoundObject> traversRoleRuleBackwards(RoleRule aRule, KnowledgeAssociation aKA) {

        List<PathElement> thePath = aRule.getPath();

        int thePathLength = thePath.size();
        List<Integer> theMALocations = new ArrayList<>();
        String theAssociationType = aKA.tTable().getName();

        // locate the meta attribute, may appeare more than once
        for (int i=0; i < thePathLength; i++) {
            PathElement thePE = thePath.get(i);
            String theAssociation = thePE.getAssociation();
            if (theAssociation != null && theAssociation.equals(theAssociationType)) {
				theMALocations.add(Integer.valueOf(i));
            }
        }

        return taversRoleRuleBackwards(aRule, aKA, thePath, theMALocations);
    }

    /**
     * Get all Objects affected by the given rule and using the given association
     */
    private static Set<BoundObject> taversRoleRuleBackwards(RoleRule aRule, KnowledgeAssociation aKA, List<PathElement> thePath, List<Integer> theMALocations) {
        Set<BoundObject> theResult = new HashSet<>();
        for (Iterator<Integer> theIt = theMALocations.iterator(); theIt.hasNext();) {
            int             thePos = theIt.next().intValue();
            PathElement     thePE  = thePath.get(thePos);
            KnowledgeObject theKO;
			TLObject theWrapper;
            try {
                if (thePE.isInverse()) {
                    theKO = aKA.getDestinationObject();
                } else {
                    theKO = aKA.getSourceObject();
                }
                theWrapper = WrapperFactory.getWrapper(theKO);

                addBaseObjects(thePath, thePos, Collections.singleton(theWrapper), theResult);
            }
            catch (InvalidLinkException ex) {
// this maight be ok because it happens while traversing a rule path backwards.
// if it happens in this case, the effect will alredy be estblished by the change to an other association...
// TODO TSA:  ??? !!! ??? think about this
//                Logger.error("Unable to get wrapper while determining security. Security may be compromised. Global refresh of security is advisable.", ex, ElementAccessHelper.class);
            }
        }
        // handle base object
		TLObject theBaseObject = aRule.getBase();
        if (theBaseObject == null) {
            // no base object, therefore the calculated objects are already the result
            // Filter the objects that do not fit the meta object / meta element of the rule
            for (Iterator<BoundObject> theIt = theResult.iterator(); theIt.hasNext();) {
            	BoundObject theObject = theIt.next();
                if ( ! (aRule.matches(theObject)) ) {
                    theIt.remove();
                }
            }
            
            return theResult;
        } else {
            if (theResult.contains(theBaseObject)) {
                return getTargetObjects(aRule);
            } else {
                return Collections.emptySet();
            }
        }
    }

    /**
     * Get all objects potentially affected by this rule
     *
     * @return never NULL
     */
    public static Set<BoundObject> getTargetObjects(RoleRule aRule) {
        MetaObject theMO = aRule.getMetaObject();
        if (theMO != null) {
            return new HashSet(WrapperFactory.getWrappersByType(theMO.getName(), KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase()));
        } else {
            Set<BoundObject> theResult = new HashSet<>();
            TLClass      theME     = aRule.getMetaElement();
			Iterable<TLClass> theMEs =
				aRule.isInherit() ?
					TLModelUtil.getConcreteSpecializations(theME) :
					Collections.singleton(theME);
            for (Iterator theIt = theMEs.iterator(); theIt.hasNext();) {
				TLClass theFoundME = (TLClass) theIt.next();
				try (CloseableIterator<BoundObject> theObjectIterator =
					MetaElementUtil.iterateDirectInstances(theFoundME, BoundObject.class)) {
                    while (theObjectIterator.hasNext()) {
						theResult.add(theObjectIterator.next());
                    }
                }

            }
            return theResult;
        }
    }

	private static void addBaseObjects(List<PathElement> aPath, int aPos, Set<TLObject> someSources,
			Set<BoundObject> someResult) {
        if (aPos == 0) {
            uncheckedAddAll(someSources, someResult);
        } else {
			Set<TLObject> theNewSources = new HashSet<>();
            int           theNewPos     = aPos - 1;
            PathElement   thePE         = aPath.get(theNewPos);
            TLStructuredTypePart theMA         = thePE.getMetaAttribute();
            String        theAssType    = thePE.getAssociation();
            if (theMA != null) {
                if (thePE.isInverse()) {
					for (Iterator<TLObject> theIt = someSources.iterator(); theIt.hasNext();) {
						TLObject theSource = theIt.next();
						Object theDestination = theSource.tValue(theMA);
                        if (theDestination != null) {
                            theNewSources.addAll((theDestination instanceof Collection)
								? (Collection<TLObject>) theDestination
								: Collections.singleton((TLObject) theDestination));
                        }
                    }
                } else {
					for (Iterator<TLObject> theIt = someSources.iterator(); theIt.hasNext();) {
						TLObject theSource = theIt.next();
						theNewSources.addAll(AttributeOperations.getReferers(theSource, theMA));
                    }
                }
            } else {
				for (Iterator<TLObject> theIt = someSources.iterator(); theIt.hasNext();) {
					{
						TLObject theSource = theIt.next();
                        KAIterator theKAIt;
                        if (thePE.isInverse()) {
							theKAIt = new DestinationIterator((KnowledgeObject) theSource.tHandle(), theAssType);
                        } else {
							theKAIt = new SourceIterator((KnowledgeObject) theSource.tHandle(), theAssType);
                        }
						theNewSources.addAll(AbstractWrapper.getWrappersFromAssociations(theKAIt, null));
                    }
                }
            }
            addBaseObjects(aPath, theNewPos, theNewSources, someResult);
        }
    }



    /**
	 * Gets a set with the given type and all specializations.
	 *
	 * @param aMetaElement
	 *        the type to get all specializations from
	 * @return The given type and all its specializations.
	 */
    public static Set<TLClass> getAllSubMetaElements(TLClass aMetaElement) {
        if (aMetaElement == null) return Collections.emptySet();
        Set<TLClass> theResult = new HashSet<>();
        theResult.add(aMetaElement);
		Iterator<TLClass> it = aMetaElement.getSpecializations().iterator();
        while (it.hasNext()) {
			Set<TLClass> theSet = getAllSubMetaElements(it.next());
            theResult.addAll(theSet);
        }
        return theResult;
    }
    
    @SuppressWarnings("unchecked")
	private static <U, V> void uncheckedAddAll(Collection<V> aFrom, Collection<U> aTo) {
    	for (V object : aFrom) {
    		aTo.add((U) object);
    	}
    }

	/**
	 * Converts a given collection in a more concise representation.
	 * 
	 * @param input
	 *        the collection to shrink
	 * @return a collection containing the same elements but in a more concise container.
	 * 
	 * @since 5.7.4
	 */
	public static <T> Collection<T> shrink(Collection<T> input) {
		int size = input.size();

		if (size == 0) {
			return Collections.emptySet();
		} else if (size < 6) {
			return new ArrayList<>(input);
		} else {
			return input;
		}
	}

}
