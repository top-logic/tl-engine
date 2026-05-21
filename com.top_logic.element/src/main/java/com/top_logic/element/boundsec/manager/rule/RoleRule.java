/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
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

    /** The meta element type of objects the role applies to */
    private TLClass metaElement;

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

    /**
     * The resource key (prefix) used to get a name and a description for the role
     */
    private ResKey resourceKey;

    private String id;

    /**
     * Constructor with all attributes
     */
    private RoleRule(TLClass aME, TLClass aSourceME, boolean isInterit, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, Type aType, ResKey aResourceKey) {
        super();
		this.metaElement = Objects.requireNonNull(aME);
        this.sourceMetaElement = aSourceME;
        this.inherit           = isInterit;
        this.role              = aRole;
        this.sourceRole        = aSourceRole;
        this.path              = aPath;
        this.type              = aType;
        this.resourceKey       = aResourceKey;
        this.id                = this.computeId(aME, aSourceME, isInterit, aRole, aSourceRole, aPath, aType);
    }

    /**
     * Constructor for reference rules on meta elements
     */
    public RoleRule(TLClass aME, boolean isInterit, BoundRole aRole, List<PathElement> aPath, ResKey aResourceKey) {
        this(aME, null, isInterit, aRole, null, aPath, Type.reference, aResourceKey);
    }

    /**
     * Constructor for inheritance rules on meta elements
     */
    public RoleRule(TLClass aME, TLClass aSourceME, boolean isInterit, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, ResKey aResourceKey) {
        this(aME, aSourceME, isInterit, aRole, aSourceRole, aPath, Type.inheritance, aResourceKey);
    }

    public String computeId(TLClass aME, TLClass aSourceME, boolean isInherit, BoundRole aRole, BoundRole aSourceRole, List<PathElement> aPath, Type aType) {
        StringBuffer theSB = new StringBuffer();
		theSB.append("me:");
		theSB.append(aME.getName());
        if (aSourceME != null) {
            theSB.append("sme:");
            theSB.append(aSourceME.getName());
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
        for (Iterator<PathElement> theIt = aPath.iterator(); theIt.hasNext();) {
			try {
				theIt.next().appendId(theSB);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
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

    /**
     * true if the rule is applicable to the given wrapper
     */
    public boolean matches(Wrapper aWrapper) {
    	if (aWrapper == null || !aWrapper.tValid()) return false;

		if (!(aWrapper instanceof Wrapper)) {
			return false;
		}
		return this.matches((TLClass) ((Wrapper) aWrapper).tType());
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
	public Collection<Group> getGroupsFor(Wrapper base) {
		if (base == null || !base.tValid()) {
    		return Collections.emptySet();
    	}
		if (!matches(base)) {
    	    return Collections.emptySet();
    	}

		Collection<Group> theResult;
        ElementAccessManager theAM = (ElementAccessManager)AccessManager.getInstance();
		Tuple resultCacheKey;
        if (theAM.isInCacheMode()) {
			resultCacheKey = new TupleFactory.Pair<>(getId(), KBUtils.getWrappedObjectName(base));
			theResult = (Collection<Group>) theAM.getResult(resultCacheKey);

			if (theResult != null) {
				return theResult;
			}
		} else {
			resultCacheKey = null;
        }

		Set theCollector = getContents(base);
		theResult = new HashSet<>();
        if (this.getType().equals(Type.reference)) {
            CollectionUtil.mapIgnoreNull(theCollector.iterator(), theResult, theAM.getGroupMapper());
        } else {
            BoundRole theRole = this.getSourceRole();
            if (theRole == null) {
                theRole = this.getRole();
            }
            CollectionUtil.mapIgnoreNull(theCollector.iterator(), theResult, new HasRoleGroupMapper(theRole, this.sourceMetaElement));
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
	public BaseObjects<Set<BoundObject>> getBaseObjects(Object aDestination) {
		if (!(aDestination instanceof TLObject)) {
			return BaseObjects.of(Collections.emptySet());
    	}
		TLObject theDestination = (TLObject) aDestination;
		Set<TLObject> theCollector = new HashSet<>();
        boolean couldBeComputed = this.getContentBackwards(theDestination, this.path, this.path.size() - 1, theCollector);
		if (!couldBeComputed) {
			return BaseObjects.all();
		}

		for (Iterator<TLObject> it = theCollector.iterator(); it.hasNext();) {
			if (!matches((Wrapper) it.next())) {
				it.remove();
			}
		}
		return BaseObjects.of((Set) theCollector);
    }

	private Set<TLObject> getContents(TLObject aNode) {
		Set<TLObject> result = new HashSet<>();
		getContent(aNode, getPath(), 0, result);
		return result;
	}

	private void getContent(TLObject aNode, List<PathElement> aPath, int aPosition, Set<TLObject> aResult) {
    	if (aNode == null || !aNode.tValid()) return;

		PathElement thePE = path.get(aPosition);
		Collection<? extends TLObject> theNodeElements = thePE.getValues(aNode);
        if (path.size() == aPosition + 1) {
        	aResult.addAll(theNodeElements);
        } else {
            int theChildPosition = aPosition + 1;
            for (Iterator<? extends TLObject> theIt = theNodeElements.iterator(); theIt.hasNext();) {
				TLObject theElement = theIt.next();
                this.getContent(theElement, aPath, theChildPosition, aResult);
            }
        }
    }

	private boolean getContentBackwards(TLObject aNode, List<PathElement> aPath, int aPosition, Set<TLObject> aResult) {
		PathElement thePE = aPath.get(aPosition);
		BaseObjects<? extends Collection<? extends TLObject>> nodeElements = thePE.getSources(aNode);
		if (nodeElements.isAll()) {
			return false;
		}
		Collection<? extends TLObject> theNodeElements = nodeElements.get();
        if (aPosition == 0) {
            aResult.addAll(theNodeElements);
        } else {
            int theChildPosition = aPosition - 1;
			for (Iterator<? extends TLObject> theIt = theNodeElements.iterator(); theIt.hasNext();) {
				TLObject theElement = theIt.next();
				boolean couldBeComputed = this.getContentBackwards(theElement, aPath, theChildPosition, aResult);
				if (!couldBeComputed) {
					return false;
				}
            }
        }
		return true;
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

	@Override
    public String toString() {
        return this.id;
    }

}
