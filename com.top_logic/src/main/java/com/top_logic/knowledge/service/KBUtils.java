/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.DefaultEventWriter;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.KnowledgeEventConverter;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KAIterator;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DBObjectKey;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.util.Utils;
import com.top_logic.util.message.MessageStoreFormat;

/**
 * Utilities for abstracting features not declared at the {@link KnowledgeBase}
 * interface, but provided by different {@link KnowledgeBase} implementations
 * differently.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBUtils {

	/** Format for getting the wrapper name out of the given wrappers. */
    public static final Format WRAPPER_NAME_FORMAT = new Format() {

        @Override
		public StringBuffer format(Object aObj, StringBuffer aToAppendTo, FieldPosition aPos) {
			return aToAppendTo.append(KBUtils.getWrappedObjectName((TLObject) aObj));
        }

        @Override
		public Object parseObject(String aSource, ParsePosition aPos) {
            throw new UnsupportedOperationException("This formatter cannot parse strings!");
        }
    };

    /** Names of attributes that must not be cloned in associations. */
    public static final Set<String> KA_SYSTEM_ATTRIBUTES;

    static {
        Set<String> set = new HashSet<>();
        set.addAll(LifecycleAttributes.LSANAMES);
        set.add(KOAttributes.IDENTIFIER);
        set.add(KOAttributes.PHYSICAL_RESOURCE);
		set.add(BasicTypes.BRANCH_ATTRIBUTE_NAME);
		set.add(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		set.add(BasicTypes.REV_CREATE_ATTRIBUTE_NAME);
		set.add(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		set.add(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
        KA_SYSTEM_ATTRIBUTES = Collections.unmodifiableSet(set);
    }


    public static CommitContext createCommitContext(KnowledgeBase kb) {
    	return ((CommitHandler) kb).createCommitContext();
    }
    
    public static CommitContext getCurrentContext(KnowledgeBase kb) {
    	return ((CommitHandler) kb).getCurrentContext();
    }

	/** 
	 * @deprecated either use {@link #getCurrentContext(KnowledgeBase)} or {@link #createCommitContext(KnowledgeBase)}.
	 */
    @Deprecated
	public static final CommitContext getCommitContext(KnowledgeBase kb, boolean create) {
    	if (create) {
    		return KBUtils.createCommitContext(kb);
    	} else {
    		return KBUtils.getCurrentContext(kb);
    	}
	}

	public static ConnectionPool getConnectionPool(KnowledgeBase knowledgeBase) {
		DBKnowledgeBase dbKb = (DBKnowledgeBase) knowledgeBase;
		return dbKb.getConnectionPool();
	}
	
	/**
	 * The to string converted {@link KnowledgeItem#tId()} of the argument.
	 */
	public static String getObjectKeyString(KnowledgeItem item) {
		return item.tId().toString();
	}
	
	/**
	 * The to string converted {@link KnowledgeItem#getObjectName()} of the argument.
	 */
	public static TLID getObjectName(KnowledgeItem item) {
		return item.getObjectName();
	}

	/**
	 * The to string converted {@link KnowledgeItem#getObjectName()} of the
	 * {@link TLObject#tHandle()} of the argument.
	 */
	public static TLID getWrappedObjectName(TLObject wrapper) {
		return wrapper.tIdLocal();
	}

	/**
	 * The to string converted {@link KnowledgeItem#tId()} of the
	 * {@link TLObject#tHandle()} of the argument.
	 */
	public static String getWrappedObjectKeyString(TLObject wrapper) {
		return wrapper.tId().toString();
	}

	/**
	 * The {@link KnowledgeItem#tId()} of the {@link TLObject#tHandle()} of the
	 * argument.
	 */
	public static Object getWrappedObjectKey(TLObject wrapper) {
		return wrapper.tId();
	}

	
	public static String getKAType(KnowledgeAssociation aKA) {
	    return aKA.tTable().getName();
	}


    /**
     * Creates a new KnowledgeAssociation with the same attribute values and destination
     * object as the given one but with the given source object and deletes the given
     * KnowledgeAssociation.
     * 
     * @param aKA
     *        the KnowledgeAssociation to change source object / to delete
     * @param aNewSource
     *        the new source object for the KnowledgeAssociation
     * @param createDuplicateKAs
     *        if <code>true</code>, a new KA is always created; if <code>false</code>,
     *        a new KA is only created if there doesn't exist one with the same attribute
     *        values.
     * @return the new created KnowledgeAssociation with the new source object and the
     *         destination object and attribute values of the old KnowledgeAssociation
     * @throws DataObjectException
     *         if accessing / creating of KnowledgeAssociation fails
     */
    public static KnowledgeAssociation changeSource(KnowledgeAssociation aKA, KnowledgeObject aNewSource, boolean createDuplicateKAs) throws DataObjectException {
        if (!createDuplicateKAs) {
            // check whether the new KA already exists
			Iterator<KnowledgeAssociation> it =
				aNewSource.getOutgoingAssociations(getKAType(aKA), aKA.getDestinationObject());
            while (it.hasNext()) {
				KnowledgeAssociation theKA = it.next();
                if (compareKAAttributes(aKA, theKA)) {
                    aKA.delete();
                    return theKA;
                }
            }
        }
        
        // create new KA
		KnowledgeAssociation newKA =
			aKA.getKnowledgeBase().createAssociation(aNewSource, aKA.getDestinationObject(), getKAType(aKA));
        String[] attributes = aKA.getAttributeNames();
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
			if (!KA_SYSTEM_ATTRIBUTES.contains(attribute)
				&& !DBKnowledgeAssociation.REFERENCE_SOURCE_NAME.equals(attribute)) {
                newKA.setAttributeValue(attribute, aKA.getAttributeValue(attribute));
            }
        }
        
        aKA.setAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, null);
        aKA.setAttributeValue(DBKnowledgeAssociation.REFERENCE_DEST_NAME, null);
        aKA.delete();
        
        return newKA;
    }

    /**
     * Creates a new KnowledgeAssociation with the same attribute values and source object
     * as the given one but with the given destination object and deletes the given
     * KnowledgeAssociation.
     * 
     * @param aKA
     *        the KnowledgeAssociation to change destination object / to delete
     * @param aNewDestination
     *        the new destination object for the KnowledgeAssociation
     * @param createDuplicateKAs
     *        if <code>true</code>, a new KA is always created; if <code>false</code>,
     *        a new KA is only created if there doesn't exist one with the same attribute
     *        values.
     * @return the new created KnowledgeAssociation with the new destination object and the
     *         source object and attribute values of the old KnowledgeAssociation
     * @throws DataObjectException
     *         if accessing / creating of KnowledgeAssociation fails
     */
    public static KnowledgeAssociation changeDestination(KnowledgeAssociation aKA, KnowledgeObject aNewDestination, boolean createDuplicateKAs) throws DataObjectException {
        if (!createDuplicateKAs) {
            // check whether the new KA already exists
            // same as aNewDestination.getIncomingAssociations(getKAType(aKA), aKA.getSourceObject())
			Iterator<KnowledgeAssociation> it =
				aKA.getSourceObject().getOutgoingAssociations(getKAType(aKA), aNewDestination);
            while (it.hasNext()) {
				KnowledgeAssociation theKA = it.next();
                if (compareKAAttributes(aKA, theKA)) {
                    aKA.delete();
                    return theKA;
                }
            }
        }
        
        // create new KA
		KnowledgeAssociation newKA =
			aKA.getKnowledgeBase().createAssociation(aKA.getSourceObject(), aNewDestination, getKAType(aKA));
        String[] attributes = aKA.getAttributeNames();
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
			if (!KA_SYSTEM_ATTRIBUTES.contains(attribute)
				&& !DBKnowledgeAssociation.REFERENCE_DEST_NAME.equals(attribute)) {
                newKA.setAttributeValue(attribute, aKA.getAttributeValue(attribute));
            }
        }
        
        aKA.setAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, null);
        aKA.setAttributeValue(DBKnowledgeAssociation.REFERENCE_DEST_NAME, null);
        aKA.delete();
        
        return newKA;
    }

    /**
     * Creates a new KnowledgeAssociation with the same attribute values and destination
     * object as the given one but with the given source object and deletes the given
     * KnowledgeAssociation. The new KA is created only if none exists already.
     * 
     * @param aKA
     *        the KnowledgeAssociation to change source object / to delete
     * @param aNewSource
     *        the new source object for the KnowledgeAssociation
     * @return the new created KnowledgeAssociation with the new source object and the
     *         destination object and attribute values of the old KnowledgeAssociation
     * @throws DataObjectException
     *         if accessing / creating of KnowledgeAssociation fails
     */
	public static KnowledgeAssociation changeSource(KnowledgeAssociation aKA, TLObject aNewSource) {
		return changeSource(aKA, (KnowledgeObject) aNewSource.tHandle(), false);
    }

    /**
     * Creates a new KnowledgeAssociation with the same attribute values and source object
     * as the given one but with the given destination object and deletes the given
     * KnowledgeAssociation. The new KA is created only if none exists already.
     * 
     * @param aKA
     *        the KnowledgeAssociation to change destination object / to delete
     * @param aNewDestination
     *        the new destination object for the KnowledgeAssociation
     * @return the new created KnowledgeAssociation with the new destination object and the
     *         source object and attribute values of the old KnowledgeAssociation
     * @throws DataObjectException
     *         if accessing / creating of KnowledgeAssociation fails
     */
	public static KnowledgeAssociation changeDestination(KnowledgeAssociation aKA, TLObject aNewDestination) {
		return changeDestination(aKA, (KnowledgeObject) aNewDestination.tHandle(), false);
    }

    /**
     * Compares the attribute names and values of two KnowledgeAssociations.
     * 
     * @param aKA1
     *        the first KnowledgeAssociation; must not be <code>null</code>
     * @param aKA2
     *        the second KnowledgeAssociation; must not be <code>null</code>
     * @return <code>true</code>, if all attribute names and values of both KnowledgeAssociations
     *         are equal, <code>false</code> otherwise
     */
    public static boolean compareKAAttributes(KnowledgeAssociation aKA1, KnowledgeAssociation aKA2) {
        try {
            String[] attributes1 = aKA1.getAttributeNames();
            String[] attributes2 = aKA2.getAttributeNames();
            if (!ArrayUtil.equals(attributes1, attributes2)) {
                return false;
            }
            for (int i = 0; i < attributes1.length; i++) {
                String attribute = attributes1[i];
                if (!KA_SYSTEM_ATTRIBUTES.contains(attribute) &&
                    !Utils.equals(aKA1.getAttributeValue(attribute), aKA2.getAttributeValue(attribute))) {
                    return false;
                }
            }
        }
        catch (NoSuchAttributeException ex) {
            // should never happen because the KA is asked for its attributes
            return false;
        }
        return true;
    }


    /**
     * Assert that given Wrapper has not Associations except those exclude by given Filters.
     * 
     * @see #assertAssociations(KnowledgeObject, Filter, Filter)
     */
	public static boolean assertAssociations(TLObject aWrapper,
            Filter incomingFilter, Filter outgoingFilter) {
		return assertAssociations((KnowledgeObject) aWrapper.tHandle(), incomingFilter, outgoingFilter);
    }

    /**
     * Assert that given KO has not Associations except those exclude by given Filters.
     * 
     * @param incomingFilter must accept KAIterator (to check for KO and KA)
     * @param outgoingFilter must accept KAIterator (to check for KO and KA)
     * 
     * @return true when all Associations for aKO are matched by the corresponding filters.
     * @throws AssertionError instead of returning false.
     */
    public static boolean assertAssociations(KnowledgeObject aKO,
            Filter incomingFilter, Filter outgoingFilter) {
        return assertAssociations(new SourceIterator(aKO)     , incomingFilter)
            && assertAssociations(new DestinationIterator(aKO), outgoingFilter);
    }

    /**
     * Assert that given KO has not Associations in anIter those excluded by given Filters.
     * 
     * @throws AssertionError instead of returning false.
     */
    public static boolean assertAssociations(KAIterator anIter, Filter iterFilter) {
        while (anIter.hasNext()) {
            KnowledgeObject theKo = anIter.nextKO();
            if (!iterFilter.accept(anIter))
                throw new AssertionError("Unexpected '" + anIter.currentKA() + "' to '" + theKo + "'");
        }
        return true;
    }
    
	/**
	 * Reverts the events starting from startRevision on startBranch to stopRevision on stopBranch.
	 * The application must ensure that the events can be applied to the startBranch at
	 * {@link Revision#CURRENT_REV} (e.g. if startRevision is {@link Revision#CURRENT}).
	 * 
	 * @see KnowledgeBase#getDiffReader(Revision, Branch, Revision, Branch, boolean)
	 */
	public static void revert(KnowledgeBase kb, Revision startRevision, Branch startBranch, Revision stopRevision,
			Branch stopBranch) {

		long targetBranchId = startBranch.getBranchId();
		TargetBranchConverter converter = new TargetBranchConverter(targetBranchId);
		try (ChangeSetReader reader = kb.getDiffReader(startRevision, startBranch, stopRevision, stopBranch, false);
				EventWriter writer = new DefaultEventWriter(kb)) {
			while (true) {
				ChangeSet currentCS = reader.read();
				if (currentCS == null) {
					break;
				}
				converter.rewrite(currentCS, writer);
			}

			/* The EventWriter does not report commit events, therefore to ensure the the revert
			 * actions are committed write a synthetic commit event. */
			writeSyntheticCommit(writer, kb);
		}
	}

	private static void writeSyntheticCommit(EventWriter writer, KnowledgeBase kb) {
		long newRevision = kb.getHistoryManager().getLastRevision() + 1;
		ChangeSet cs = new ChangeSet(newRevision);
		CommitEvent commit = new CommitEvent(newRevision,
			ThreadContextManager.getSubSession().getContextId(),
			System.currentTimeMillis(),
			MessageStoreFormat.toString(Messages.NO_COMMIT_MESSAGE));
		cs.setCommit(commit);
		writer.write(cs);
	}

	/**
	 * Convenience short cut for {@link #revert(KnowledgeBase, Revision, Branch)} where the
	 * {@link Branch} is the {@link HistoryManager#getContextBranch() context branch}.
	 */
	public static void revert(KnowledgeBase knowledgeBase, Revision stopRevision) {
		Branch branch = knowledgeBase.getHistoryManager().getContextBranch();
		revert(knowledgeBase, Revision.CURRENT, branch, stopRevision, branch);
	}

	/**
	 * This method reverts anything on the given branch from up to
	 * <code>stopRevision</code>, i.e. the data on the given branch are the same
	 * as at revision <code>stopRevision</code>.
	 * 
	 * @see KnowledgeBase#getDiffReader(Revision, Branch, Revision, Branch,
	 *      boolean)
	 */
	public static void revert(KnowledgeBase kb, Revision stopRevision, Branch targetBranch) {
		revert(kb, Revision.CURRENT, targetBranch, stopRevision, targetBranch);
    }

	/**
	 * Factory method to create {@link ObjectKey} with the given values.
	 */
	public static ObjectKey createObjectKey(long branch, long revValue, MetaObject type, TLID objectName) {
		ObjectKey value;
		if (type instanceof MOKnowledgeItem) {
			value = new DBObjectKey(branch, revValue, (MOKnowledgeItem) type, objectName);
		} else {
			value = new DefaultObjectKey(branch, revValue, type, objectName);
		}
		return value;
	}

	/**
	 * Creates an {@link ObjectKey} with a changed {@link ObjectKey#getHistoryContext()}.
	 */
	public static ObjectKey createHistoricObjectKey(ObjectKey origKey, long newHistoryContext) {
		long originalBranch = origKey.getBranchContext();
		MetaObject originalType = origKey.getObjectType();
		TLID originalName = origKey.getObjectName();
		return createObjectKey(originalBranch, newHistoryContext, originalType, originalName);
	}

	/**
	 * Checks whether the given key has the given {@link ObjectKey#getHistoryContext()}. If not a
	 * new key with this context is created and returned. Otherwise the input is returned.
	 */
	public static ObjectKey ensureHistoryContext(ObjectKey origKey, long newHistoryContext) {
		if (origKey.getHistoryContext() == newHistoryContext) {
			return origKey;
		}
		return createHistoricObjectKey(origKey, newHistoryContext);
	}

	/**
	 * Translates an {@link ObjectKey} to an {@link ObjectKey} in the given {@link MORepository}. If
	 * it is already adequate for the given {@link MORepository} the source key may be returned.
	 */
	public static ObjectKey transformKey(MORepository moRepository, ObjectKey sourceKey) throws UnknownTypeException {
		return transformKey(moRepository, sourceKey, Mappings.<String>identity());
	}

	/**
	 * Translates an {@link ObjectKey} to an {@link ObjectKey} in the given {@link MORepository}. If
	 * it is already adequate for the given {@link MORepository} the source key may be returned.
	 * 
	 * @param nameConversion
	 *        Mapping of the type name in the source key to the name of the target type in the given
	 *        {@link MORepository}.
	 */
	public static ObjectKey transformKey(MORepository moRepository, ObjectKey sourceKey,
			Mapping<String, String> nameConversion) throws UnknownTypeException {
		final MetaObject sourceType = sourceKey.getObjectType();
		// resolve type in target KB
		final MetaObject targetType = moRepository.getMetaObject(nameConversion.map(sourceType.getName()));
		if (targetType == sourceType) {
			return sourceKey;
		} else {
			final long branchContext = sourceKey.getBranchContext();
			final long historyContext = sourceKey.getHistoryContext();
			final TLID objectName = sourceKey.getObjectName();
			return KBUtils.createObjectKey(branchContext, historyContext, targetType, objectName);
		}
	}

	/**
	 * Translates an {@link ObjectBranchId} to an {@link ObjectBranchId} in the given
	 * {@link MORepository}. If it is already adequate for the given {@link MORepository} the source
	 * key may be returned.
	 */
	public static ObjectBranchId transformId(MORepository moRepository, ObjectBranchId id) throws UnknownTypeException {
		return transformId(moRepository, id, Mappings.<String> identity());
	}

	/**
	 * Translates an {@link ObjectBranchId} to an {@link ObjectBranchId} in the given
	 * {@link MORepository}. If it is already adequate for the given {@link MORepository} the source
	 * key may be returned.
	 * 
	 * @param nameConversion
	 *        Mapping of the type name in the given id to the name of the target type in the given
	 *        {@link MORepository}.
	 */
	public static ObjectBranchId transformId(MORepository moRepository, ObjectBranchId id,
			Mapping<String, String> nameConversion) throws UnknownTypeException {
		final MetaObject sourceType = id.getObjectType();
		// resolve type in target KB
		final MetaObject targetType = moRepository.getMetaObject(nameConversion.map(sourceType.getName()));
		if (targetType == sourceType) {
			return id;
		} else {
			final long branchContext = id.getBranchId();
			final TLID objectName = id.getObjectName();
			return new ObjectBranchId(branchContext, targetType, objectName);
		}
	}

	/**
	 * Creates an {@link EventWriter} for the given {@link KnowledgeBase}. The events written to the
	 * resulted writer are translated to the correct repository, i.e. If the event comes from a
	 * different {@link KnowledgeBase} the IDs are changed such that the types are known by the
	 * given {@link KnowledgeBase}.
	 * 
	 * @param targetKB
	 *        the {@link KnowledgeBase} to replay to.
	 */
	public static EventWriter getReplayWriter(KnowledgeBase targetKB) {
		return KnowledgeEventConverter.createEventConverter(targetKB.getMORepository(), targetKB.getReplayWriter());
	}

	/**
	 * Converts an TLID identifier to string.
	 * 
	 * @see KnowledgeItem#getObjectName()
	 */
	public static String objectNameString(TLID id) {
		return id.toString();
	}

	/**
	 * Stable {@link Comparator#compare(Object, Object)} for two {@link KnowledgeItem}s based on
	 * their {@link ObjectKey}.
	 */
	public static int compareObjectByKey(KnowledgeObject ko1, KnowledgeObject ko2) {
		return compareKey(ko1.tId(), ko2.tId());
	}

	/**
	 * Stable {@link Comparator#compare(Object, Object)} for two {@link ObjectKey}s.
	 */
	public static int compareKey(ObjectKey ko1, ObjectKey ko2) {
		int branchCompare = CollectionUtil.compareLong(ko1.getBranchContext(), ko2.getBranchContext());
		if (branchCompare != 0) {
			return branchCompare;
		}

		int revisionCompare = CollectionUtil.compareLong(ko1.getHistoryContext(), ko2.getHistoryContext());
		if (revisionCompare != 0) {
			return revisionCompare;
		}

		return compareIds(ko1.getObjectName(), ko2.getObjectName());
	}

	/**
	 * Stable {@link Comparator#compare(Object, Object)} for two {@link ObjectKey#getObjectName()}
	 * values.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compareIds(TLID id1, TLID id2) {
		return ((Comparable) id1).compareTo(id2);
	}

	/**
	 * Loads all items with one of the given keys.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to resolve items in.
	 * @param ids
	 *        The identifiers of the objects to load.
	 * 
	 * @return {@link List} containing the {@link KnowledgeItem} with the given keys.
	 * 
	 * @see BulkIdLoad#load(KnowledgeBase, Collection)
	 */
	public static List<KnowledgeItem> bulkLoad(KnowledgeBase kb, Collection<? extends ObjectKey> ids) {
		return BulkIdLoad.load(kb, ids);
	}

	/**
	 * Fetches the {@link SchemaSetup} defined for the {@link KnowledgeBase} of the given
	 * {@link KnowledgeBaseConfiguration} in the {@link ApplicationTypes} configuration section.
	 * 
	 * @see ApplicationTypes#getTypeSystems()
	 */
	public static SchemaSetup getSchemaConfigResolved(KnowledgeBaseConfiguration kbConfig) {
		return getSchemaConfigResolved(kbConfig.getTypeSystem());
	}

	/**
	 * Fetches the {@link SchemaSetup} for the given type system name and resolves references.
	 * 
	 * @see ApplicationTypes#getTypeSystems()
	 */
	public static SchemaSetup getSchemaConfigResolved(String typeSystem) {
		SchemaSetup result = getSchemaConfig(typeSystem);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		try {
			return result.resolve(context);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Fetches the {@link SchemaSetup} defined for the {@link KnowledgeBase} of the given
	 * {@link KnowledgeBaseConfiguration} in the {@link ApplicationTypes} configuration section.
	 * 
	 * @see ApplicationTypes#getTypeSystems()
	 */
	public static SchemaSetup getSchemaConfig(KnowledgeBaseConfiguration kbConfig) {
		return getSchemaConfig(kbConfig.getTypeSystem());
	}

	private static SchemaSetup getSchemaConfig(String typeSystem) {
		ApplicationTypes applicationTypes = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
		SchemaSetup schema = applicationTypes.getTypeSystem(typeSystem);
		if (schema == null) {
			throw new ConfigurationError("Type system '" + typeSystem + "' is not defined.");
		}
		return schema;
	}

	/**
	 * Executes the given action within a {@link Transaction}.
	 * <p>
	 * Uses the {@link PersistencyLayer#getKnowledgeBase() default} {@link KnowledgeBase}.
	 * </p>
	 * <p>
	 * The {@link Transaction} is committed, if the action completes without throwing something. It
	 * is rolled back otherwise.
	 * </p>
	 * 
	 * @param action
	 *        Is not allowed to be null;
	 * @return The result of the given action. Is null when the action returns null.
	 */
	public static <T> T inTransaction(Supplier<T> action) {
		return inTransaction(PersistencyLayer.getKnowledgeBase(), action);
	}

	/**
	 * Executes the given action within a {@link Transaction}.
	 * <p>
	 * The {@link Transaction} is committed, if the action completes without throwing something. It
	 * is rolled back otherwise.
	 * </p>
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} to use.
	 * @param action
	 *        Is not allowed to be null;
	 * @return The result of the given action. Is null when the action returns null.
	 */
	public static <T> T inTransaction(KnowledgeBase knowledgeBase, Supplier<T> action) {
		try (Transaction transaction = knowledgeBase.beginTransaction()) {
			T result = action.get();
			transaction.commit();
			return result;
		}
	}

	/**
	 * Executes the given action within a {@link Transaction}.
	 * <p>
	 * Uses the {@link PersistencyLayer#getKnowledgeBase() default} {@link KnowledgeBase}.
	 * </p>
	 * <p>
	 * The {@link Transaction} is committed, if the action completes without throwing something. It
	 * is rolled back otherwise.
	 * </p>
	 * 
	 * @param action
	 *        Is not allowed to be null;
	 */
	public static void inTransaction(Runnable action) {
		inTransaction(PersistencyLayer.getKnowledgeBase(), action);
	}

	/**
	 * Executes the given action within a {@link Transaction}.
	 * <p>
	 * The {@link Transaction} is committed, if the action completes without throwing something. It
	 * is rolled back otherwise.
	 * </p>
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} to use.
	 * @param action
	 *        Is not allowed to be null;
	 */
	public static void inTransaction(KnowledgeBase knowledgeBase, Runnable action) {
		try (Transaction transaction = knowledgeBase.beginTransaction()) {
			action.run();
			transaction.commit();
		}
	}

	/**
	 * Deletes all {@link KnowledgeItem} in the given {@link Iterator}.
	 * 
	 * @param elements
	 *        The elements to delete.
	 * 
	 * @return Whether some element was deleted.
	 */
	public static boolean deleteAllKI(Iterator<? extends KnowledgeItem> elements) {
		return deleteAllKI(elements, FilterFactory.trueFilter());
	}

	/**
	 * Deletes all {@link KnowledgeItem} in the given {@link Iterator} that matches the given
	 * filter.
	 * 
	 * @param elements
	 *        The elements to delete.
	 * @param filter
	 *        Filter detecting the elements to delete.
	 * 
	 * @return Whether some element was deleted.
	 */
	public static boolean deleteAllKI(Iterator<? extends KnowledgeItem> elements,
			Predicate<? super KnowledgeItem> filter) {
		while (elements.hasNext()) {
			KnowledgeItem item = elements.next();
			if (!filter.test(item)) {
				continue;
			}
			List<KnowledgeItem> toDelete = null;
			while (elements.hasNext()) {
				KnowledgeItem next = elements.next();
				if (!filter.test(next)) {
					continue;
				}
				if (toDelete == null) {
					toDelete = new ArrayList<>();
					toDelete.add(item);
				}
				toDelete.add(next);
			}
			if (toDelete == null) {
				item.delete();
			} else {
				item.getKnowledgeBase().deleteAll(toDelete);
			}
			return true;
		}
		return false;
	}

	/**
	 * Deletes all {@link TLObject} in the given {@link Iterable}.
	 * 
	 * @param elements
	 *        The elements to delete.
	 * 
	 * @return Whether some element was deleted.
	 * 
	 * @see #deleteAll(Iterator)
	 */
	public static boolean deleteAll(Iterable<? extends TLObject> elements) {
		return deleteAll(elements.iterator());
	}

	/**
	 * Deletes all {@link TLObject} in the given {@link Iterator}.
	 * 
	 * @param elements
	 *        The elements to delete.
	 * 
	 * @return Whether some element was deleted.
	 * 
	 * @see #deleteAll(Iterator, Predicate)
	 */
	public static boolean deleteAll(Iterator<? extends TLObject> elements) {
		return deleteAll(elements, FilterFactory.trueFilter());
	}

	/**
	 * Deletes all {@link TLObject} in the given {@link Iterator} that matches the given filter.
	 * 
	 * @param elements
	 *        The elements to delete.
	 * @param filter
	 *        Filter detecting the elements to delete.
	 * 
	 * @return Whether some element was deleted.
	 */
	public static boolean deleteAll(Iterator<? extends TLObject> elements, Predicate<? super TLObject> filter) {
		while (elements.hasNext()) {
			TLObject item = elements.next();
			if (!mustBeDeleted(item, filter)) {
				continue;
			}
			List<KnowledgeItem> toDelete = null;
			while (elements.hasNext()) {
				TLObject next = elements.next();
				if (!mustBeDeleted(next, filter)) {
					continue;
				}
				if (toDelete == null) {
					toDelete = new ArrayList<>();
					toDelete.add(item.tHandle());
				}
				toDelete.add(next.tHandle());
			}
			if (toDelete == null) {
				item.tDelete();
			} else {
				item.tKnowledgeBase().deleteAll(toDelete);
			}
			return true;
		}
		return false;
	}

	private static boolean mustBeDeleted(TLObject item, Predicate<? super TLObject> filter) {
		if (!item.tValid()) {
			// Already deleted.
			return false;
		}
		return filter.test(item);
	}

}
