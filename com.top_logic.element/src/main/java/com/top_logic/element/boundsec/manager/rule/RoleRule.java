/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.singleton.ElementSingletonManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Utils;

/**
 * A rule determining the roles on an object.
 *
 * TODO TSA add handling for name and description attribute
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class RoleRule implements RoleProvider {

    /** The meta object type of objects the role applies to */
    private MetaObject metaObject;
    /** The meta element type of objects the role applies to */
    private TLClass metaElement;

    /** The meta object type of objects an inheritance rule may inherit from */
    private MetaObject sourceMetaObject;
    /** The meta element type of objects an inheritance rule may inherit from */
    private TLClass sourceMetaElement;

    /** Indicates that the role is to be applied to sub types too */
    private boolean     inherit;
    /** the role to apply by this rule */
    private BoundRole   role;
    /** the role to request in case of an inheritance rule that maps one role to another one */
    private BoundRole   sourceRole;
    /**
     * a list {@link com.top_logic.element.boundsec.manager.rule.PathElement}s
     * determining the groups the role is given to
     */
    private List<PathElement>        path;

    /** the type of the rule */
    private Type        type;

    /** the base the rule is evaluated on. Must be a singleton reference */
    private String      base;

    /**
     * The resource key (prefix) used to get a name and a description for the role
     */
    private ResKey resourceKey;

    private String id;

    /**
     * Constructor with all attributes
     */
    private RoleRule(TLClass aME, MetaObject aMO, TLClass aSourceME, MetaObject aSourceMO, boolean isInterit, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, Type aType, String aBase, ResKey aResourceKey) {
        super();
        this.metaElement       = aME;
        this.metaObject        = aMO;
        this.sourceMetaElement = aSourceME;
        this.sourceMetaObject  = aSourceMO;
        this.inherit           = isInterit;
        this.role              = aRole;
        this.sourceRole        = aSourceRole;
        this.path              = aPath;
        this.type              = aType;
        this.base              = aBase;
        this.resourceKey       = aResourceKey;
        this.id                = this.computeId(aME, aMO, aSourceME, aSourceMO, isInterit, aRole, aSourceRole, aPath, aType, aBase);
    }

    /**
     * Constructor for reference rules on meta elements
     */
    public RoleRule(TLClass aME, boolean isInterit, BoundRole aRole, List<PathElement> aPath, String aBase, ResKey aResourceKey) {
        this(aME, null, null, null, isInterit, aRole, null, aPath, Type.reference, aBase, aResourceKey);
    }

    /**
     * Constructor for reference rules on meta objects
     */
    public RoleRule(MetaObject aMO, BoundRole aRole, List<PathElement> aPath, String aBase, ResKey aResourceKey) {
        this(null, aMO, null, null, false, aRole, null, aPath, Type.reference, aBase, aResourceKey);
    }

    /**
     * Constructor for inheritance rules on meta elements
     */
    public RoleRule(TLClass aME, TLClass aSourceME, MetaObject aSourceMO, boolean isInterit, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, String aBase, ResKey aResourceKey) {
        this(aME, null, aSourceME, aSourceMO, isInterit, aRole, aSourceRole, aPath, Type.inheritance, aBase, aResourceKey);
    }

    /**
     * Constructor for inheritance rules on meta objects
     */
    public RoleRule(MetaObject aMO, TLClass aSourceME, MetaObject aSourceMO, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, String aBase, ResKey aResourceKey) {
        this(null, aMO, aSourceME, aSourceMO, false, aRole, aSourceRole, aPath, Type.inheritance, aBase, aResourceKey);
    }




    public String computeId(TLClass aME, MetaObject aMO, TLClass aSourceME, MetaObject aSourceMO, boolean isInherit, BoundRole aRole, BoundRole aSourceRole, List aPath, Type aType, String aBase) {
        StringBuffer theSB = new StringBuffer();
        if (aME != null) {
            theSB.append("me:");
            theSB.append(aME.getName());
        } else {
            theSB.append("mo:");
            theSB.append(aMO.getName());
        }
        if (aSourceME != null) {
            theSB.append("sme:");
            theSB.append(aSourceME.getName());
        }
        if (aSourceMO != null) {
            theSB.append("smo:");
            theSB.append(aSourceMO.getName());
        }
        theSB.append('_');
        theSB.append(isInherit);
        theSB.append('_');
        theSB.append(aRole.getName());
        if (aSourceRole != null) {
            theSB.append('=');
            theSB.append(aSourceRole.getName());
        }
        theSB.append('_');
        for (Iterator theIt = aPath.iterator(); theIt.hasNext();) {
            PathElement   thePE  = (PathElement) theIt.next();
            TLStructuredTypePart theMA  = thePE.getMetaAttribute();
            String        theAss = thePE.getAssociation();
            if (theMA != null) {
                TLClass theME = AttributeOperations.getMetaElement(theMA);
                if (theME != null) {
                    theSB.append("pme:");
                    theSB.append(theME.getName());
                }
                theSB.append("ma:");
                theSB.append(theMA.getName());
            } else {
                theSB.append("a:");
                theSB.append(theAss);
            }
            theSB.append('_');
            theSB.append(thePE.isInverse() ? "back" : "succ");
        }
        theSB.append("_base:");
        if (aBase != null) {
            theSB.append(aBase);
        }
        theSB.append('_');
        theSB.append(aType.ordinal());
        return theSB.toString();
    }

    /**
	 * @see com.top_logic.element.boundsec.manager.rule.RoleProvider#getId()
	 */
    @Override
	public String getId() {
        return this.id;
    }

    /**
     * <code>true</code> if the rule applies to the given meta element
     */
    public boolean matches(TLClass aME) {
        if (aME == null) {
            return false;
        }
        if (this.inherit) {
			return MetaElementUtil.hasGeneralization(aME, metaElement);
		} else {
			return this.metaElement.equals(aME);
        }
    }

    public boolean matches(MetaObject aMO) {
        if (aMO == null) {
            return false;
        }
        return aMO.isSubtypeOf(this.metaObject);
    }

    /**
     * true if the rule is applicable to the given wrapper
     */
    public boolean matches(Wrapper aWrapper) {
    	if (aWrapper == null || !aWrapper.tValid()) return false;

        if (this.metaElement != null) {
            if ( ! (aWrapper instanceof Wrapper)) {
                return false;
            }
			return this.matches((TLClass) ((Wrapper) aWrapper).tType());
        }
        return this.matches(aWrapper.tTable());
    }
    
    @Override
	public boolean matches(BoundObject anObject) {
    	return (anObject instanceof Wrapper)
    		? this.matches((Wrapper) anObject)
    	    : false;
    }



    /**
     * Getter
     */
    public TLClass getMetaElement() {
        return (metaElement);
    }

    /**
     * Getter
     */
    public TLClass getSourceMetaElement() {
        return (sourceMetaElement);
    }

    /**
	 * @see com.top_logic.element.boundsec.manager.rule.RoleProvider#getRole()
	 */
    @Override
	public BoundRole getRole() {
        return (role);
    }

    /**
	 * Indicates, that the rule also applies to the sub types (in case of a set {@link TLClass}) 
	 */
    public boolean isInherit() {
        return (inherit);
    }

    /**
     * This method returns the resourceKey.
     *
     * @return    Returns the resourceKey.
     */
    public ResKey getResourceKey() {
        return (this.resourceKey);
    }

    /**
     * Getter
     */
    public List<PathElement> getPath() {
        return (path);
    }


    @Override
	public Collection<Group> getGroups(BoundObject anObject) {
    	return (anObject instanceof Wrapper)
			? this.getGroupsFor((Wrapper) anObject)
		    : Collections.<Group>emptySet();
    }
    
	/**
	 * Value of {@link #getGroups(BoundObject)} for {@link Wrapper}.
	 * 
	 * @see #getGroups(BoundObject)
	 * 
	 * @since 5.7.4
	 */
	public Collection<Group> getGroupsFor(Wrapper aBase) {
		if (aBase == null || !aBase.tValid()) {
    		return Collections.emptySet();
    	}
    	if (!matches(aBase)) {
    	    return Collections.emptySet();
    	}

        Wrapper theBase = this.getBase();
        if (theBase == null) {
            theBase = aBase;
        }

		Collection<Group> theResult;
        ElementAccessManager theAM = (ElementAccessManager)AccessManager.getInstance();
		Tuple resultCacheKey;
        if (theAM.isInCacheMode()) {
			resultCacheKey = new TupleFactory.Pair<>(getId(), KBUtils.getWrappedObjectName(theBase));
			theResult = (Collection<Group>) theAM.getResult(resultCacheKey);

			if (theResult != null) {
				return theResult;
			}
		} else {
			resultCacheKey = null;
        }

        if (this.getType().equals(Type.reference)) {
            Set theCollector = new HashSet();
            this.getContent(theBase, this.path, 0, theCollector);
            theResult = new HashSet<>();
            CollectionUtil.mapIgnoreNull(theCollector.iterator(), theResult, theAM.getGroupMapper());
        } else {
            Set theCollector = new HashSet();
            this.getContent(theBase, this.path, 0, theCollector);
            theResult = new HashSet<>();
            BoundRole theRole = this.getSourceRole();
            if (theRole == null) {
                theRole = this.getRole();
            }
            CollectionUtil.mapIgnoreNull(theCollector.iterator(), theResult, new HasRoleGroupMapper(theRole, this.sourceMetaElement, this.sourceMetaObject));
            Set<Group> theFinalResult = new HashSet<>();
            Utils.fold(theFinalResult, theResult.iterator(), new Utils.FolderFunction() {
                @Override
				public Object fold(Object aCurrentResult, Object aNewObject) {
                    ((Collection) aCurrentResult).addAll((Collection) aNewObject);
                    return aCurrentResult;
                }
            });
            theResult = theFinalResult;
        }

        if (theAM.isInCacheMode()) {
			theAM.putResult(resultCacheKey, ElementAccessHelper.shrink(theResult));
        }
        return theResult;
    }

    @Override
	public Set<BoundObject> getBaseObjects(Object aDestination) {
    	if ( ! (aDestination instanceof Wrapper)) {
    		return Collections.emptySet();
    	}
    	Wrapper theDestination = (Wrapper) aDestination;
        Set<BoundObject> theCollector = new HashSet<>();
        this.getContentBackwards(theDestination, this.path, this.path.size() - 1, theCollector);

        Wrapper theBase = this.getBase();
        if (theBase != null) {
            if (theCollector.contains(theBase)) {
                return ElementAccessHelper.getTargetObjects(this);
            } else {
                return Collections.emptySet();
            }
        } else {
            for (Iterator it = theCollector.iterator(); it.hasNext(); ) {
                if (!matches((Wrapper)it.next())) {
                    it.remove();
                }
            }
            return theCollector;
        }
    }

	private void getContent(Wrapper aNode, List aPath, int aPosition, Set aResult) {
    	if (aNode == null || !aNode.tValid()) return;

        PathElement thePE = (PathElement) aPath.get(aPosition);
        Collection theNodeElements = thePE.getValues(aNode);
        if (aPath.size() == aPosition + 1) {
        	aResult.addAll(theNodeElements);
        } else {
            int theChildPosition = aPosition + 1;
            for (Iterator theIt = theNodeElements.iterator(); theIt.hasNext();) {
                Wrapper theElement = (Wrapper) theIt.next();
                this.getContent(theElement, aPath, theChildPosition, aResult);
            }
        }
    }

    private void getContentBackwards(Wrapper aNode, List aPath, int aPosition, Set aResult) {
        PathElement thePE = (PathElement) aPath.get(aPosition);
        Collection theNodeElements = thePE.getSources(aNode);
        if (aPosition == 0) {
            aResult.addAll(theNodeElements);
        } else {
            int theChildPosition = aPosition - 1;
            for (Iterator theIt = theNodeElements.iterator(); theIt.hasNext();) {
                Wrapper theElement = (Wrapper) theIt.next();
                this.getContentBackwards(theElement, aPath, theChildPosition, aResult);
            }
        }
    }

    public MetaObject getMetaObject() {
        return (this.metaObject);
    }

    public MetaObject getSourceMetaObject() {
        return (this.sourceMetaObject);
    }

    /**
	 * @see com.top_logic.element.boundsec.manager.rule.RoleProvider#getSourceRole()
	 */
    @Override
	public BoundRole getSourceRole() {
        return this.sourceRole == null ? this.role : this.sourceRole;
    }

    /**
	 * @see com.top_logic.element.boundsec.manager.rule.RoleProvider#getType()
	 */
    @Override
	public Type getType() {
        return (this.type);
    }

    public Wrapper getBase() {
        if (StringServices.isEmpty(this.base)) {
            return null;
        } else {
			return (Wrapper) ElementSingletonManager.getSingleton(this.base);
        }
    }

    public String getBaseString() {
        return this.base;
    }

	@Override
    public String toString() {
        return this.id;
    }

}
