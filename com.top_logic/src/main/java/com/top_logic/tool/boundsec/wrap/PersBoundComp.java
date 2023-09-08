/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Wrapper for the knowledge object "PersBoundComp".
 *
 * (The name PersistantBoundComponenent would eventually be to
 *  long for the name of a KO, so forgive me this abbreviation.)
 *
 * A PersBoundComp is a Helper Object for a BoundChecker (especially
 * for the BoundComponent). PersBoundComps are created dynamically.
 * They are used for Security reasons
 * only and carry <em>no</em> Layout/HTML/User specific information
 *
 * @see com.top_logic.tool.boundsec.BoundMainLayout#initBoundComponents(KnowledgeBase)
 * @see SecurityComponentCache
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class PersBoundComp extends AbstractWrapper {

    /** Name of the knowledge object represented by this wrapper. */
    public static final String OBJECT_NAME = "PersBoundComp";

    /** Name of the Attribute used as Identifier (as built-in IDENTIFER got to small) */
    public static final String IDENTIFIER = "ID";

    /** The name of the Association we have to (Bound)Roles. */
    public static final String NEEDS_ROLE = "needsRole";

    /** The name of attribute for CommandGroups in the needsRole KA */
    public static final String ATTRIBUTE_CMDGRP = "cmdGrp";

	private static final Map<String, AssociationSetQuery<KnowledgeAssociation>> NEEDS_ROLE_QUERY_BY_GROUP_ID =
		new HashMap<>();

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *
     * @param    ko    The KnowledgeObject, must never be <code>null</code>.
     */
    public PersBoundComp(KnowledgeObject ko) {
        super(ko);
    }

	/**
	 * Sets access to this {@link PersBoundComp} for a given {@link BoundedRole role} for exactly
	 * the given {@link BoundCommandGroup}s.
	 * 
	 * @return Whether any changes occurred.
	 */
	public boolean setAccess(
			Map<? extends BoundedRole, ? extends Collection<? extends BoundCommandGroup>> neededRights) {
		BooleanFlag result = new BooleanFlag(false);
		Map<KnowledgeObject, Set<String>> tmp = new HashMap<>();
		neededRights.entrySet().forEach(entry -> {
			Set<String> groupIds = entry.getValue()
				.stream()
				.map(BoundCommandGroup::getID)
				.collect(Collectors.toSet());
			tmp.put(entry.getKey().tHandle(), groupIds);
		});
		Iterator<KnowledgeAssociation> neededRoles = tHandle().getOutgoingAssociations(NEEDS_ROLE);
		while (neededRoles.hasNext()) {
			KnowledgeAssociation neededRole = neededRoles.next();
			Set<String> rights = tmp.get(neededRole.getDestinationObject());
			if (rights == null || !rights.remove(neededRole.getAttributeValue(ATTRIBUTE_CMDGRP))) {
				// right not longer needed
				neededRole.delete();
				result.set(true);
			}
		}
		// Add missing associations.
		tmp.entrySet().forEach(entry -> {
			entry.getValue().forEach(grpId -> {
				addNeedsRoleAssociation(entry.getKey(), grpId);
				result.set(true);
			});
		});
		return result.get();
	}

	/**
	 * Sets access to this {@link PersBoundComp} for a given {@link BoundCommandGroup} for exactly
	 * the given {@link BoundedRole roles}.
	 * 
	 * @return Whether any changes occurred.
	 */
	public boolean setAccess(Collection<? extends BoundedRole> roles, BoundCommandGroup group) {
		boolean result = false;
		String grpId = group.getID();
		Set<KnowledgeObject> tmp = roles.stream().map(BoundedRole::tHandle).collect(Collectors.toSet());
		Iterator<KnowledgeAssociation> neededRoles = tHandle().getOutgoingAssociations(NEEDS_ROLE);
		while (neededRoles.hasNext()) {
			KnowledgeAssociation neededRole = neededRoles.next();
			if (!grpId.equals(neededRole.getAttributeValue(ATTRIBUTE_CMDGRP))) {
				// Different command group; ignore
				continue;
			}
			if (!tmp.remove(neededRole.getSourceObject())) {
				// right not longer needed
				neededRole.delete();
				result = true;
			}
		}
		if (!tmp.isEmpty()) {
			// Add missing associations.
			tmp.forEach(entry -> addNeedsRoleAssociation(entry, grpId));
			result = true;
		}
		return result;
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
    public boolean addAccess (BoundCommandGroup aGroup, BoundedRole aRole) {
		{
            KnowledgeObject thepersBoundKO = this.tHandle();
            KnowledgeObject theRoleKO      = aRole.tHandle();
            Iterator theIt = thepersBoundKO.getOutgoingAssociations(NEEDS_ROLE, theRoleKO);
            if (theIt != null) {
                while (theIt.hasNext()) {
                    KnowledgeAssociation theAssociation = (KnowledgeAssociation) theIt.next();
                    if (theAssociation.getAttributeValue(ATTRIBUTE_CMDGRP).equals(aGroup.getID())) {
                       return false;
                    }
                }
            }
			addNeedsRoleAssociation(theRoleKO, aGroup.getID());
            return true;
        }
    }

	private void addNeedsRoleAssociation(KnowledgeObject role, String group) {
		KnowledgeAssociation needsRoleKA = getKnowledgeBase().createAssociation(tHandle(), role, NEEDS_ROLE);
		needsRoleKA.setAttributeValue(ATTRIBUTE_CMDGRP, group);
	}

    /**
     * Remove access to the PersBoundComp for a given BoundedRole.
     *
     * @param   aGroup      The CommandGroups that will require the role. If <code>null</code> remove any access for the role
     * @param   aRole       the BoundRole to add access for.
     *
     * @return true when something was actually removed (you must call commit() to finlaize this).
     */
    public boolean removeAccess (BoundCommandGroup aGroup, BoundedRole aRole) {
		{
            KnowledgeObject theBoundCompKO  = this.tHandle();
            KnowledgeObject theRoleKO       = aRole.tHandle();
			Iterator<KnowledgeAssociation> iter = theBoundCompKO.getOutgoingAssociations(NEEDS_ROLE, theRoleKO);
			if (!iter.hasNext()) {
				return false;
			}

            if (aGroup == null) {
				getKnowledgeBase().deleteAll(CollectionUtil.toList(iter));
                return true;
            } else {
                String grpId = aGroup.getID();
				List<KnowledgeItem> toDelete = new ArrayList<>();
                while (iter.hasNext()) {
					KnowledgeAssociation tmpKa = iter.next();
                    if (grpId.equals(tmpKa.getAttributeValue(ATTRIBUTE_CMDGRP))) {
						toDelete.add(tmpKa);
                    }
                }
				if (toDelete.isEmpty()) {
					return false;
				}
				getKnowledgeBase().deleteAll(toDelete);
				return true;
            }
        }
    }

    /**
	 * Remove access to the {@link PersBoundComp} for a all {@link BoundedRole} for the given
	 * {@link BoundCommandGroup}.
	 *
	 * @param group
	 *        The group that will require the role.
	 *
	 * @return <code>true</code> when something was actually removed (you must call commit() to
	 *         finlaize this).
	 */
	public boolean removeAllAccess(BoundCommandGroup group) {
		return setAccess(Collections.emptySet(), group);
	}

	/**
	 * Remove all access to the PersBoundComp for a any BoundedRole.
	 *
	 * @return true when something was actually removed (you should call commit() then !)
	 */
    public boolean removeAllAccess () {
		return setAccess(Collections.emptyMap());
    }

    /**
     * Return the Set of Roles that need the given CommandGroup
     *
     * @param   aGroup      The CommandGroups that will require the roles.
     */
    public Set rolesForCommandGroup(BoundCommandGroup aGroup) {
    	String grpId = aGroup.getID();
		AssociationSetQuery<KnowledgeAssociation> theQuery;
    	synchronized (NEEDS_ROLE_QUERY_BY_GROUP_ID) {
    		theQuery = NEEDS_ROLE_QUERY_BY_GROUP_ID.get(grpId);

    		if (theQuery == null) {
    			Map<String, String> attributes = Collections.singletonMap(ATTRIBUTE_CMDGRP, grpId);
				theQuery = AssociationQuery.createOutgoingQuery(NEEDS_ROLE + "(" + grpId + ")", NEEDS_ROLE, attributes);

    			NEEDS_ROLE_QUERY_BY_GROUP_ID.put(grpId, theQuery);
    		}
    	}

    	return resolveWrappers(theQuery);
    }

    /**
	 * Create a {@link PersBoundComp} for the given id.
	 *
	 * @param kb
	 *        The knowledge base to be used.
	 * @param name
	 *        The name to identify the {@link PersBoundComp}.
	 * 
	 * @return The newly created {@link PersBoundComp}.
	 */
	public static PersBoundComp createInstance(KnowledgeBase kb, ComponentName name) {
		Objects.requireNonNull(name, "Name must not be null");
		String qualifiedName = name.qualifiedName();
		if (qualifiedName.length() > 254) {
			throw new IllegalArgumentException("Name '" + name + "' to long.");
		}
		PersBoundComp persBoundComp = kb.createKnowledgeObject(OBJECT_NAME).getWrapper();
		persBoundComp.setName(PersBoundComp.asPersistentValue(name));
		return persBoundComp;
    }

	/**
	 * Returns the {@link PersBoundComp} with the given id.
	 *
	 * @param kb
	 *        The knowledge base to be used.
	 * @param name
	 *        The name to identify the {@link PersBoundComp}.
	 * 
	 * @return The {@link PersBoundComp} with the given name, or <code>null</code> when there is no
	 *         such {@link PersBoundComp}.
	 */
	public static PersBoundComp getInstance(KnowledgeBase kb, ComponentName name) {
		DataObject ko = kb.getObjectByAttribute(OBJECT_NAME, IDENTIFIER, asPersistentValue(name));
		if (ko != null) {
			return ((KnowledgeObject) ko).getWrapper();
		}
		return null;
	}

	private static String asPersistentValue(ComponentName name) {
		return ComponentNameFormat.INSTANCE.getSpecification(name);
	}

	/**
	 * This method returns the identifier.
	 * 
	 * @return Returns the identifier.
	 */
	public ComponentName getIdentifier() {
		String persistentName = getName();
		try {
			return ComponentName.newConfiguredName("persistentValue", persistentName);
		} catch (ConfigurationException ex) {
			Logger.error("Illegal persistent value: " + persistentName, PersBoundComp.class);
			return ComponentName.newName(persistentName);
		}
	}


	@Override
	public String getName() {
		return getString(IDENTIFIER);
	}

	@Override
	public void setName(String value) {
		setString(IDENTIFIER, value);
	}
}
