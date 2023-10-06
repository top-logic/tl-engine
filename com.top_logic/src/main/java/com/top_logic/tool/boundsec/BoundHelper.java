/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundChecker;
import com.top_logic.util.TLContext;

/**
 * Common, configurable functions used by the Bound hierarchy.
 * <p>
 * This class can easily be overridden to modify aspects of the BoundSecurity
 * as needed by applications.
 * </p>
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class BoundHelper extends ManagedClass {

    /** When asking for groups this will merge in the groups of the SuperObject */
    public static final int INHERIT_ROLES = 0x0001;

    /** Tells BoundCheckers to ignore the included Object. */
    public static final int IGNORE_OBJECT = 0x0002;

	/**
	 * Separator for type name and {@link BoundCommandGroup} name in defaultFor XML attribute.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent.Config#getDefaultFor()
	 */
	public static final String BCG_SEPARATOR = ":";

	/**
	 * Wildcard for {@link BoundCommandGroup} name in defaultFor XML attribute.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent.Config#getDefaultFor()
	 */
	public static final String BCG_DEFAULT = "*";

	/**
	 * Configuration of the {@link BoundHelper}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ServiceConfiguration<BoundHelper> {

		/**
		 * Whether to use the {@link BoundHelper#getDefaultObject() global default object} as
		 * {@link AbstractBoundWrapper#getSecurityParent() security parent} by default.
		 * 
		 * @see BoundHelper#getDefaultObject()
		 * @see AbstractBoundWrapper#getSecurityParent()
		 * @see BoundHelper#useDefaultObject()
		 */
		@Name("use-default-security-parent")
		boolean getUseDefaultSecurityParent();

	}

    /** The default {@link com.top_logic.tool.boundsec.BoundObject} */
	private BoundObject defaultObject;

	private Map<Location, Map<String, Collection<BoundChecker>>> defaultPersBoundCheckers;

	private Map<Location, Map<String, Collection<ComponentName>>> boundCheckerCache;
    

    /**
     * Flag indicating that the delete protection flags in different components
     * can be changed by the user.
     */
    boolean allowChangeDeleteProtection = false;

    /**
     * Flag indicating whether disabled buttons can be pressed anyway.
     */
    boolean allowExecuteDisabledButtons = false;

	/** @see Config#getUseDefaultSecurityParent() */
	private final boolean _useDefaultSecurityParent;

	/**
	 * Creates a new {@link BoundHelper} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link BoundHelper}.
	 */
	public BoundHelper(InstantiationContext context, Config config) {
		super(context, config);
        // for getInstance
		defaultPersBoundCheckers =
			Collections.synchronizedMap(new HashMap<>());
		boundCheckerCache =
			Collections.synchronizedMap(new HashMap<>());

		_useDefaultSecurityParent = config.getUseDefaultSecurityParent();
    }

	public final boolean registerPersBoundCheckerFor(Location rootLocation, ComponentName aPersBoundID, String aType) {
		Map<String, Collection<BoundChecker>> checkerForType = defaultPersBoundCheckers.get(rootLocation);
		if (checkerForType == null) {
			checkerForType = Collections.synchronizedMap(new HashMap<>());
			defaultPersBoundCheckers.put(rootLocation, checkerForType);
		}
		Collection<BoundChecker> theCheckers = checkerForType.get(aType);
        if (theCheckers == null) {
			theCheckers = CollectionUtil.newSet(1);
        }

        theCheckers.add(new PersBoundChecker(aPersBoundID));
		checkerForType.put(aType, theCheckers);

        if (theCheckers.size() > 1) {
            String theIDs = StringServices.toString(asIDs(theCheckers), ",");
			Logger.warn("Multiple PersBoundComp are default for type '" + aType + "': " + theIDs, BoundHelper.class);
        }
        return true;
    }

	private Collection<ComponentName> asIDs(Collection<? extends BoundChecker> someCheckers) {
		ArrayList<ComponentName> theIds = new ArrayList<>(someCheckers.size());
		for (BoundChecker theChecker : someCheckers) {
			theIds.add(theChecker.getSecurityId());
        }
        return theIds;
    }

    /**
     * Get the default {@link com.top_logic.tool.boundsec.BoundObject}.
     *
     * @return the default {@link com.top_logic.tool.boundsec.BoundObject}
     */
    public BoundObject getDefaultObject() {
        if (defaultObject == null) {
			defaultObject = newDefaultObject();
        }
        return defaultObject;
    }

	/**
	 * Creates the object that is cached and later returned by {@link #getDefaultObject()}.
	 */
	protected BoundObject newDefaultObject() {
		return new SimpleBoundObject("defaultObject");
	}

    /**
     * Get the default {@link com.top_logic.tool.boundsec.BoundChecker}.
     * @param  anObject	the object to be checked
     *
     * @return the default {@link com.top_logic.tool.boundsec.BoundChecker}
     */
	public final BoundChecker getDefaultChecker(MainLayout rootLayout, BoundObject anObject) {
    	return this.getDefaultChecker(rootLayout, anObject, null);
    }
    
	/**
	 * Determines the default {@link BoundChecker} for the given Object and the given
	 * {@link BoundCommandGroup}.
	 * @param o
	 *        The object to get default checker for. May be <code>null</code>.
	 * 
	 * @return The default checker for the given object or <code>null</code> if none can be found.
	 */
	public final BoundChecker getDefaultBoundChecker(MainLayout rootLayout, Object o, BoundCommandGroup commandGroup) {
		if (o == null || o instanceof BoundObject) {
			return getDefaultChecker(rootLayout, (BoundObject) o, commandGroup);
		} else {
			Collection<BoundChecker> checkersForType = getDefaultCheckers(o, commandGroup);
			if (checkersForType.isEmpty()) {
				return null;
			} else {
				return checkersForType.iterator().next();
			}
		}
	}

	/**
	 * Get the default {@link com.top_logic.tool.boundsec.BoundChecker}s.
	 *
	 * @param o
	 *        the object to be checked
	 * @param commandGroup
	 *        the command group to be checked on the object. May be <code>null</code>.
	 * @return the default {@link com.top_logic.tool.boundsec.BoundChecker}s
	 */
	public Collection<BoundChecker> getDefaultCheckers(Object o, BoundCommandGroup commandGroup) {
		List<String> types = getBoundCheckerDefaultTypes(o);
		Collection<BoundChecker> checkersForType;
		switch(types.size()) {
			case 0:
				checkersForType = Collections.emptyList();
				break;
			case 1: 
				checkersForType = getDefaultCheckers(types.get(0), commandGroup);
				break;
			default:
				checkersForType = Collections.emptySet();
				for (String type : types) {
					Collection<BoundChecker> defaultCheckers = getDefaultCheckers(type, commandGroup);
					if (defaultCheckers.isEmpty()) {
						continue;
					}
					if (checkersForType.isEmpty()) {
						checkersForType = new HashSet<>();
					}
					checkersForType.addAll(defaultCheckers);
				}
		}
		return checkersForType;
	}

	/**
	 * Get the default {@link com.top_logic.tool.boundsec.BoundChecker}s for the given type and the
	 * read command group.
	 *
	 * @see #getDefaultCheckersForType(TLClass, BoundCommandGroup)
	 */
	public final Collection<BoundChecker> getDefaultCheckersForType(TLClass type) {
		return getDefaultCheckers(getCheckerTypeForType(type), SimpleBoundCommandGroup.READ);
	}

	/**
	 * Get the default {@link com.top_logic.tool.boundsec.BoundChecker}s for the given type.
	 *
	 * @param type
	 *        The {@link TLClass} of the objects to be checked.
	 * @param aBCG
	 *        The {@link BoundCommandGroup command group} to be checked on the object
	 * @return the default {@link com.top_logic.tool.boundsec.BoundChecker}s.
	 */
	public Collection<BoundChecker> getDefaultCheckersForType(TLClass type, BoundCommandGroup aBCG) {
		return getDefaultCheckers(getCheckerTypeForType(type), aBCG);
	}

	private Collection<BoundChecker> getDefaultCheckers(String aType, BoundCommandGroup aBCG) {
    	
    	BoundChecker theRoot    = getRootChecker();
    	
    	// context is available, get the "live" checkers
    	if (theRoot != null) {
    		return getBoundHandlers(aType, theRoot, aBCG);
    	}
    	// no context, get cached PersBoundCheckers
		else {
			switch (defaultPersBoundCheckers.size()) {
				case 0: {
					return Collections.emptyList();
				}
				case 1: {
					return getDefaultFromMap(this.defaultPersBoundCheckers.values().iterator().next(), aType);
				}
				default: {
					Collection<BoundChecker> allChecker = new ArrayList<>();
					defaultPersBoundCheckers.values().forEach(m -> {
						allChecker.addAll(getDefaultFromMap(m, aType));
					});
					return allChecker;
				}
			}
		}
    	
    }
    
    /**
	 * Get the default {@link com.top_logic.tool.boundsec.BoundChecker}.
	 * 
	 * @param anObject
	 *        the object to be checked
	 * @param aBCG
	 *        the command group to be checked on the object
	 *
	 * @return the default {@link com.top_logic.tool.boundsec.BoundChecker}. May be
	 *         <code>null</code>.
	 */
    public BoundChecker getDefaultChecker(MainLayout rootLayout, BoundObject anObject, BoundCommandGroup aBCG) {

        if (anObject == null) {
			return null;
        }

        BoundChecker theChecker = null;
		Collection<BoundChecker> theCheckers = getBoundCheckers(anObject, (BoundChecker) rootLayout, aBCG);

        if (theCheckers != null) {
            Iterator<BoundChecker> theIter = theCheckers.iterator();

            // if size>1 return the first checker that allows the object
            while (theIter.hasNext()) {
                theChecker = theIter.next();
                boolean allow = (aBCG == null) ? theChecker.allow(anObject) : theChecker.allow(aBCG, anObject);
                if (allow) {
                    return theChecker;
                }
            }
        }
        return theChecker; // return the last found checker or null
    }

    
    /**
     * Get the current root BoundChecker.
     * Per default this is the BoundMainLayout.
     * If no BoundMainLayout is defined, <code>null</code> will be returned.
     *
     * @return the root BoundChecker
     */
    public BoundChecker getRootChecker() {
        try {
			return TLContext.getContext().get(BoundMainLayout.ROOT_CHECKER);
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the BoundCheckers that are registered as default for the
     * given aType of a BoundObject. Use the BoundCheckers in the checker
     * tree of aChecker to search for checkers.
     *
     * @param aType     an objects type registered as default for in the layout xmls
     * @param aChecker  the BoundChecker
     * @return the BoundCheckers that are registered to check the
     *         most specific type of the given BoundObject
     */
//    public Collection getBoundCheckers(String aType, BoundChecker aChecker) {
//        List theColl = new ArrayList();
//        if (aChecker==null)
//        addBoundCheckers(aChecker, aType, theColl, false, true);
//        return theColl;
//    }

    /**
     * Get the BoundCheckers that are registered as default for the
     * given BoundObject. Use the BoundCheckers in the checker
     * tree of aChecker to search for checkers.
     *
     * @param anObject  the BoundObject
     * @param aChecker  the BoundChecker
     * @param aBCG 		the command group to be checked
     * @return the BoundCheckers that are registered to check the
     *         most specific type of the given BoundObject
     */
	public Collection<BoundChecker> getBoundCheckers(BoundObject anObject, BoundChecker aChecker, BoundCommandGroup aBCG) {
        if (aChecker == null) {
			return Collections.emptyList();
        }

		Iterator<String> theCheckerTypes = this.getBoundCheckerDefaultTypes(anObject).iterator();

        // Get the components from the MainLayout that are default for the type
		Collection<BoundChecker> theCheckers = new HashSet<>();

        // search for the most specific type for which bound checkers are registered.
        // Only the checkers registered for that type are returned
        while (theCheckers.isEmpty() && theCheckerTypes.hasNext()) {
			String theCheckerType = theCheckerTypes.next();
            theCheckers.addAll(getBoundHandlers(theCheckerType, aChecker, aBCG));
        }
		if (Logger.isDebugEnabled(BoundHelper.class)) {
			Object typeName = (anObject != null) ? TLModelUtil.qualifiedName(anObject.tType()) : anObject;
			List<ComponentName> componentNames = theCheckers.stream()
				.map(component -> ((LayoutComponent) component).getName())
				.collect(toList());
			Logger.debug("Default for '" + typeName + "': " + componentNames, BoundHelper.class);
		}
        return theCheckers;
    }

	protected Collection<BoundChecker> getBoundHandlers(String aType, BoundChecker aChecker,
			BoundCommandGroup aBCG) {
		Collection<ComponentName> checkerNames;
		if (aChecker instanceof MainLayout) {
			/* access cache only if given checker is the main layout. Otherwise the cache may be
			 * filled with result for MainLayout but accessed with concrete component which has less
			 * checkers. */
			MainLayout main = (MainLayout) aChecker;
			Map<String, Collection<ComponentName>> storedCache = boundCheckerCache.get(boundCheckerCacheKey(main));
			Collection<ComponentName> cachedCheckerNames;
			if (storedCache == null) {
				storedCache = Collections.synchronizedMap(new HashMap<>());
				boundCheckerCache.put(boundCheckerCacheKey(main), storedCache);
				cachedCheckerNames = null;
			} else {
				cachedCheckerNames = getDefaultFromMap(storedCache, aType);
			}
			if (cachedCheckerNames != null) {
				checkerNames = cachedCheckerNames;
			} else {
				checkerNames = new ArrayList<>();
				addBoundCheckers(aChecker, aBCG, aType, checkerNames, false);
				String key;
				if (aBCG != null) {
					key = aType + BCG_SEPARATOR + aBCG.getID();
				} else {
					key = aType;
				}
				if (Logger.isDebugEnabled(BoundHelper.class)) {
					Logger.debug("Default for '" + aType + "': " + checkerNames, BoundHelper.class);
				}
				storedCache.put(key, checkerNames);
			}
		} else {
			checkerNames = new ArrayList<>();
			addBoundCheckers(aChecker, aBCG, aType, checkerNames, false);
			if (checkerNames.isEmpty()) {
				// otherwise assume the user knows a proper checker to get the default from.
				// if this checker does not produce any result this may indicate a bug... better do
				// some logging.
				Logger.info("No checker found for " + aType + ". Used Checker: " + aChecker, this);
			}
		}

		if (checkerNames.isEmpty()) {
			return new ArrayList<>();
		}

		MainLayout mainLayout;
		if (aChecker instanceof LayoutComponent) {
			mainLayout = ((LayoutComponent) aChecker).getMainLayout();
		} else {
			BoundChecker rootChecker = getRootChecker();
			if (rootChecker instanceof MainLayout) {
				mainLayout = ((MainLayout) rootChecker);
			} else {
				Logger.error("No MainLayout found to get BoundCheckers", this);
				return new ArrayList<>();
			}
		}

		Collection<BoundChecker> theRawCheckers = new ArrayList<>(checkerNames.size());
		for (ComponentName theName : checkerNames) {
			BoundChecker componentByName = (BoundChecker) mainLayout.getComponentByName(theName);
			if (componentByName == null) {
				Logger.error("Component '" + theName + "' not found by name", this);
			} else {
				theRawCheckers.add(componentByName);
			}
		}

		return theRawCheckers;
	}

	private Location boundCheckerCacheKey(MainLayout main) {
		return main.getConfig().location();
	}

	/**
	 * Invalidates the {@link BoundChecker} cache for the given {@link MainLayout}.
	 */
	public void invalidateBoundCheckerCache(MainLayout main) {
		boundCheckerCache.remove(boundCheckerCacheKey(main));
	}

    /**
	 * Add checkers that are default for the given type to the given collection
	 *
	 * @param aChecker
	 *        the checker. Must not be <code>null</code>.
	 * @param aType
	 *        the type.
	 * @param aColl
	 *        the Collection. Must not be <code>null</code>.
	 */
	private void addBoundCheckers(BoundChecker aChecker, BoundCommandGroup aBCG, String aType,
			Collection<ComponentName> aColl, boolean useCache) {

        if (aChecker.isDefaultCheckerFor(aType, aBCG)) {
			aColl.add(aChecker.getSecurityId());
        } 

		Collection<? extends BoundChecker> theChildrenC = aChecker.getChildCheckers();

        if (theChildrenC != null) {
			for (BoundChecker childChecker : theChildrenC) {
				addBoundCheckers(childChecker, aBCG, aType, aColl, useCache);
            }
        }
    }

    /**
	 * Get the default type strings that bound checkers may be registered for.
	 * 
	 * @param anObject
	 *        The instance object to find checkers for.
	 * @return Type strings that bound checkers applicable for the given object
	 *         may be registered for. The result is ordered according to their
	 *         specificity. The types with lower index are more specific.
	 */
    public List<String> getBoundCheckerDefaultTypes(Object anObject) {
        List<String> checkerTypes = new ArrayList<>();

        String checkerType = getCheckerTypeForInstance(anObject);
        if (checkerType != null) {
        	checkerTypes.add(checkerType);
        }
        
		Object baseType = getObjectType(anObject);
		if (baseType == null) {
			return checkerTypes;
		}
		List<Object> allTypes = CollectionUtil.topsort(new Mapping<Object, Iterable<? extends Object>>() {

			@Override
			public Iterable<? extends Object> map(Object input) {
				return getSuperTypes(input);
			}
		}, Collections.singletonList(baseType), true);

		for (int i = allTypes.size() - 1; i >= 0; i--) {
			checkerType = this.getCheckerTypeForType(allTypes.get(i));
			if (checkerType != null) {
				int size = checkerTypes.size();
				if (size == 0 || (! checkerTypes.get(size - 1).equals(checkerType))) {
					checkerTypes.add(checkerType);
				}
			}
        }

        return checkerTypes;
    }
    
	/**
	 * The type of the most specific checkers that are check access to the given
	 * instance object.
	 * 
	 * @param anObject
	 *        The instance object to find checkers for.
	 * @return The most specific checker type for the given object.
	 * 
	 * @see #getBoundCheckerDefaultTypes(Object) Finding all checker types
	 *      that could check access to the given object.
	 */
    public final String getCheckerType(Object anObject) {
        String instanceCheckerType = getCheckerTypeForInstance(anObject);
        if (instanceCheckerType != null) {
        	return instanceCheckerType;
        } else {
        	return getCheckerTypeForType(getObjectType(anObject));
        }
	}

	/**
	 * Checker type for the concrete object instance.
	 * 
	 * <p>
	 * This object must only be implemented, if the checker cannot be determined
	 * by the given object's {@link #getObjectType(Object) type} alone.
	 * </p>
	 * 
	 * @param anObject
	 *        an object
	 * @return a checker type name
	 * 
	 * @see #getCheckerTypeForType(Object)
	 */
    protected String getCheckerTypeForInstance(Object anObject) {
        return null;
    }

	/**
	 * Checker type for the given object type.
	 * 
	 * <p>
	 * Note: This method is not intended to be overridden outside the framework.
	 * </p>
	 * 
	 * @param type
	 *        The object type representation. In <i>TopLogic</i> either {@link MetaObject} or
	 *        {@link Class}.
	 * @return a checker type name.
	 */
    protected String getCheckerTypeForType(Object type) {
        if (type instanceof MetaObject) {
            return ((MetaObject) type).getName();
        } else if (type instanceof Class<?>) {
            return ((Class<?>) type).getName();
        } else {
        	return null;
        }
	}

	/**
	 * The type abstraction for the given implementation object.
	 * 
	 * <p>
	 * Note: This method is not intended to be overridden outside the framework.
	 * </p>
	 * 
	 * @param anObject
	 *        The object to find its type for.
	 * @return In <i>TopLogic</i> either {@link MetaObject} or {@link Class}.
	 */
	protected Object getObjectType(Object anObject) {
        if (anObject instanceof Wrapper) {
            return ((Wrapper) anObject).tTable();
        }
        else if (anObject instanceof KnowledgeObject) {
            return ((KnowledgeObject) anObject).tTable();
        }
        else if (anObject != null) {
            return anObject.getClass();
        }
        else {
        	return null;
        }
	}

	/**
	 * The super type of the given type.
	 * 
	 * <p>
	 * Note: This method is not intended to be overridden outside the framework.
	 * </p>
	 * 
	 * @param type
	 *        the type
	 * @return the super type
	 * 
	 * @see #getObjectType(Object) Finding types for implementations.
	 */
	protected List<?> getSuperTypes(Object type) {
        if (type instanceof MOClass) {
			MOClass superclass = ((MOClass) type).getSuperclass();
			return CollectionUtilShared.singletonOrEmptyList(superclass);
        } else if (type instanceof Class<?>) {
			Class<?>[] interfaces = ((Class<?>) type).getInterfaces();
			Class<?> superclass = ((Class<?>) type).getSuperclass();
			if (superclass == null) {
				if (interfaces.length == 0) {
					return Collections.emptyList();
				} else {
					return Arrays.asList(interfaces);
				}
			} else {
				if (interfaces.length == 0) {
					return Collections.singletonList(superclass);
				} else {
					ArrayList<Object> result = new ArrayList<>(interfaces.length + 1);
					result.add(superclass);
					Collections.addAll(result, interfaces);
					return result;
				}
			}
        } else {
			return Collections.emptyList();
        }
    }


    /**
     * Check if the current user is allowed to navigate the
     * given BoundObject.
     * Defaults to true here and should be overridden in sub classes.
     *
     * @param anObject  the BoundObject
     * @return if the current user is allowed to navigate the
     * given BoundObject.
     */
    public boolean allowNavigate(BoundObject anObject) {
        return true;
    }


    /**
     * Check if the current user may view the given BoundObject.
     * Use the BoundCheckers in the checker
     * tree of aChecker to check it.
     *
     * @param anObject  the BoundObject
     * @param aChecker  the BoundChecker
     * @return true if the current user may view the given BoundObject,
     * false otherwise or if the object is <code>null</code>.
     */
    public boolean allowView(BoundObject anObject, BoundChecker aChecker) {
		Iterator<BoundChecker> theCheckers = this.getBoundCheckers(anObject, aChecker, null).iterator();

        boolean allow = false;
        while (!allow && theCheckers.hasNext()) {
			BoundChecker theChecker = theCheckers.next();
			allow = ((LayoutComponent) theChecker).supportsModel(anObject) && theChecker.allowPotentialModel(anObject);
        }

        return allow;
    }

    /**
     * This method returns a new collection that contains only the allowed bound
     * objects for the current user and the given bound checker. The given
     * collection isn't modified.
     *
     * NOTE!!! This method can only be used if and only if all objects of the
     * given collection have the same type.
     *
     * @param boundObjects
     *            A collection of bound objects with the same type.
     * @param aChecker
     *            A bound checker. Must NOT be <code>null</code>.
     */
    public List allow(Collection boundObjects, BoundChecker aChecker) {
        if (boundObjects == null || boundObjects.size() == 0) {
            return Collections.EMPTY_LIST;
        }

        // Get the bound checkers for the first bound object of the given
        // list. All bound objects in the list must be from the same
        // type!
        BoundObject firstBoundObject = (BoundObject) boundObjects.iterator().next();
        Collection  boundCheckers    = getBoundCheckers(firstBoundObject, aChecker, null);

        // Iterate over all given bound objects and ask the checkers
        // if the bound object is allowed.
        List allowedObjects = new ArrayList(boundObjects.size());
        for (Iterator boundObjectIter = boundObjects.iterator(); boundObjectIter.hasNext();) {
            BoundObject boundObject = (BoundObject) boundObjectIter.next();
            boolean     allowed     = false;

            for (Iterator checkerIter = boundCheckers.iterator(); checkerIter.hasNext();) {
                BoundChecker boundChecker = (BoundChecker) checkerIter.next();
                if (boundChecker.allow(boundObject)) {
                    allowed = true;
                    break;
                }
            }

            if (allowed) {
                allowedObjects.add(boundObject);
            }
        }

        return allowedObjects;
    }

    /**
     * Check if the current user may use the given BoundCommandGroup
     * on the given BoundObject. Use the BoundCheckers in the checker
     * tree of aChecker to check it.
     *
     * @param anObject  the BoundObject
     * @param aChecker  the BoundChecker
     * @param aCmdGrp   the BoundCommandGroup
     * @return true if the current user may use the given BoundCommandGroup
     * on the given BoundObject, false otherwise or if the object is <code>null</code>.
     */
    public boolean allowCommandGroup(BoundObject anObject, BoundChecker aChecker, BoundCommandGroup aCmdGrp) {
		Iterator<BoundChecker> theCheckers =
			getBoundCheckers(anObject, aChecker, aCmdGrp).iterator();

        boolean allow = false;
        while (!allow && theCheckers.hasNext()) {
			allow = theCheckers.next().allow(aCmdGrp, anObject);
        }

        return allow;
    }

    /**
     * Return true when the current Person has any role on the given BoundObject.
     *
     * This will check for superuser modes and care about resolving
     * the current Person.
     *
     * @param anObject the object to check against.
     */
    public boolean hasAnyRole(BoundObject anObject) {
        if (anObject == null) {
            return false;
        }

        TLContext context = TLContext.getContext();
        if (context == null)
            return true;   // no Context, no Security

        if (ThreadContext.isAdmin()) {
            return true;
        }
		{
			Person currPerson = TLContext.currentUser();
            if (currPerson == null)
                return false;   // no Person, no Security

			if (Person.isAdmin(currPerson)) {
				return true;
			}

            return hasAnyRole(anObject, currPerson);
        }
    }

    /**
     * Hook for subclasses to modify the hasAnyRole semantics.
     *
     * Per default we just ask the BoundObject.
     *
     * @param anObject the object to check against.
     * @param aPerson  the Person to check against.
     */
    public boolean hasAnyRole(BoundObject anObject, Person aPerson) {
		return !AccessManager.getInstance().getRoles(aPerson, anObject).isEmpty(); // anObject.hasAnyRole(aPerson);
    }

	/**
	 * All {@link BoundedRole}s defined for the given {@link TLModule}
	 */
	public Set<? extends BoundedRole> getPossibleRoles(TLModule securityModule) {
		if (securityModule != null) {
			return BoundedRole.getDefinedRoles(securityModule);
		} else {
			return Collections.emptySet();
        }
    }

    /**
     * Make aDest have all the same Roles as aSource.
     *
     * For TL this relies on BoundWrapper.
     */
    public void copyRoles (BoundObject aSource, BoundObject aDest) {

        if ( (aSource instanceof AbstractBoundWrapper)
          && (aDest   instanceof AbstractBoundWrapper)) {

			{
                AbstractBoundWrapper sBW = (AbstractBoundWrapper) aSource;
                AbstractBoundWrapper dBW = (AbstractBoundWrapper) aDest;

                BoundedRole.removeRoleAssignments(sBW);
                BoundedRole.copyRoleAssignments(dBW, sBW);
            }
        }
    }

	/**
	 * Check if the role is in use in the system, i.e. if a user has the role somewhere (used to
	 * check if it can be safely deleted).
	 * 
	 * @param aRole
	 *        the role. Must not be <code>null</code>.
	 * @return true if the role is in use
	 */
    public boolean isRoleInUse(BoundedRole aRole) {
        try {
            boolean inUse = false;
			Iterator<KnowledgeAssociation> theRoleKAs =
				aRole.tHandle().getIncomingAssociations(BoundedRole.HAS_ROLE_ASSOCIATION);
            while (theRoleKAs.hasNext() && !inUse) {
				KnowledgeAssociation theKA = theRoleKAs.next();
                inUse = theKA.getAttributeValue(BoundedRole.ATTRIBUTE_OWNER) != null;
            }

            return inUse;
        }
        catch (Exception wex) {
            return false;
        }
    }

    /**
	 * Returns the only instance of this class.
	 * 
	 * The instance to be returned can be configured in the typed configuration in the
	 * {@link BoundHelper} section.
	 * 
	 * @return The only instance of this class.
	 */
	public static BoundHelper getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }


    /** Merge to collections (assuming they are sets).
     *
     * @param c1 may be null
     * @param c2 may be null
     */
	public static final <T> Collection<T> merge(Collection<T> c1, Collection<T> c2) {
        if (c1 == null || c1.isEmpty())
            return c2;
        if (c2 == null || c2.isEmpty())
            return c1;
		HashSet<T> result = new HashSet<>(c1.size() + c2.size());
        result.addAll(c1);
        result.addAll(c2);
        return result;
    }

    /** Remove all Roles from aDest and Copy the Roles from aSource */
    public static void inheritSecurity (AbstractBoundWrapper aSource, AbstractBoundWrapper aDest) {
		BoundedRole.removeRoleAssignments(aDest);
		BoundedRole.copyRoleAssignments(aDest, aSource);
    }
    
	private static <T> Collection<T> getDefaultFromMap(Map<String, Collection<T>> defaultsByType, String type) {
		return defaultsByType.get(type);
    }

    /**
     * Flag if using default object as security parent
     * for all AbstractBoundWrappers that don't define
     * one themselves.
     *
     * @return the flag
     */
    public boolean useDefaultObject() {
		return _useDefaultSecurityParent;
    }

    /**
     * This method sets the {@link #allowChangeDeleteProtection} flag.
     */
    public void setAllowChangeDeleteProtection(boolean aAllowChangeDeleteProtection) {
        this.allowChangeDeleteProtection = aAllowChangeDeleteProtection;
    }

    /**
     * This method gets the {@link #allowChangeDeleteProtection} flag.
     */
    public boolean isAllowChangeDeleteProtection() {
        return this.allowChangeDeleteProtection;
    }

    /**
     * This method sets the {@link #allowExecuteDisabledButtons} flag.
     */
    public void setAllowExecuteDisabledButtons(boolean aAllowExecuteDisabledButtons) {
        this.allowExecuteDisabledButtons = aAllowExecuteDisabledButtons;
    }

    /**
     * This method gets the {@link #allowExecuteDisabledButtons} flag.
     */
    public boolean isAllowExecuteDisabledButtons() {
        return this.allowExecuteDisabledButtons;
    }

	public static final class Module extends TypedRuntimeModule<BoundHelper> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<BoundHelper> getImplementation() {
			return BoundHelper.class;
		}
	}

}
