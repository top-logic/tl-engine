/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.sql.ObjectNameSource;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.DefaultEventWriter;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.HistoryQueryArguments;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.model.TLObject;

/**
 * This is the general interface for all possible types of knowledge bases.
 *
 * A KnowledgeBase serves as a container for KnowledgeObjects and their
 * KnowledgeAssociations. By executing quieries on the KnowledgeBase
 * it is possible to find KnowledgeObjects matching to the request.
 *
 * @author  Jörg Connotte
 */
public interface KnowledgeBase extends KABasedCacheManager, ObjectNameSource {

	/**
	 * Property name for configuring {@link #getName()}.
	 */
	public static final String NAME_PROPERTY = "name";


    /**
	 * Initialize {@link KnowledgeBase} with given configuration.
	 * 
	 * @param protocol
	 *        The protocol to report errors to.
	 * @param config
	 *        configuration of this {@link KnowledgeBase}.
	 */
	void initialize(Protocol protocol, KnowledgeBaseConfiguration config);

	/**
	 * Must be called before the first query is issued against this
	 * {@link KnowledgeBase}.
	 * @param protocol
	 *        The protocol to report errors to.
	 */
    void startup(Protocol protocol);
    
	/**
	 * Ends this {@link KnowledgeBase}. After shutting it down it is not longer usable.
	 */
	void shutDown();

    /**
     * The name of the KnowledgeBase.
     *
     * @see #NAME_PROPERTY
     */
    public String getName ();

    /**
     * returns a KnowledgeObject for a given type of MetaObject type and Identifier
     *
     * @param       aType Name of MetaObject of KnowledgeObject to return.
     * @param       objectName the id of the object to fetch
     * @return      a KnowledgeObject which matches to the Identifier, or null
     * 
     * TODO should throw KnowledgeBaseException in case of error.
     */
    public KnowledgeObject getKnowledgeObject (String aType, TLID objectName);        

    /**
     * returns a KnowledgeAssociation for a given type of MetaObject type and Identifier
     *
     * @param   aType        Name of MetaObject of KnowledgeAssociation to return.
     * @param   objectName the id of the object to fetch
     * @return  a KnowledgeAssociation which matches to the Identifier and type or null
     */
    public KnowledgeAssociation getKnowledgeAssociation (String aType, TLID objectName);

    /**
     * Returns a DataObject for a given type of MetaObject, Attribute and value.
     * <p>
     * In case there is more than one Object with this value the first one will 
     * silently be returned and a Warning will be logged,
     * </p>
     *
     * @param   aType       Name of MetaObject of KnowledgeObject/KnowledgeAssociation to return.
     * @param   attrName    Name of an Atrribute
     * @param   value       Must be comparable to the desired value
     * @return  a KnowledgeObject or KnowledgeAssociation where
     *          <code>getAttributeValue(anIdentifier).equals(value)</code> holds.
     */
    public DataObject getObjectByAttribute (String aType, String attrName, Object value);

    /**
	 * Returns all {@link KnowledgeItem}s for a given type of {@link MetaObject} type, attribute
	 * name and attribute value.
	 *
	 * <p>
	 * Note: This is the same as {@link #getObjectsByAttribute(String, String[], Object[])} with a
	 * single attribute name and value.
	 * </p>
	 * 
	 * @param aType
	 *        Name of MetaObject of KnowledgeObjects/KnowledgeAssociations to return.
	 * @param attrName
	 *        Name of an attribute.
	 * @param value
	 *        Must be comparable to the desired value
	 * @return An {@link Iterator} over {@link KnowledgeItem}s for which
	 *         {@link KnowledgeItem#getAttributeValue(String)} is equal to the given value for the
	 *         given attribute name.
	 *
	 * @throws UnknownTypeException
	 *         in case aType is not known.
	 */
    public Iterator<KnowledgeItem> getObjectsByAttribute (String aType, String attrName, Object value)
        throws UnknownTypeException;

	/**
	 * Searches all {@link KnowledgeItem}s with the given values in the given
	 * attributes in the given order.
	 * 
	 * @param type
	 *        Name of the type in which to search.
	 * @param attributeNames
	 *        Names of attributes.
	 * @param values
	 *        Corresponding values.
	 * @return The matched items in the requested order.
	 */
    public Iterator<KnowledgeItem> getObjectsByAttribute (String type, String[] attributeNames, Object values[])
           throws UnknownTypeException, NoSuchAttributeException;

    /**
	 * Deletes the given {@link KnowledgeItem} from the {@link KnowledgeBase}.
	 * 
	 * Nothing will happen when the object was not contained in the KB.
	 * 
	 * @param item
	 *        the object to be deleted
	 * @throws DataObjectException
	 *         if delete fails for internal reasons
	 * @throws com.top_logic.knowledge.service.event.CommitVetoException
	 *         if delete is vetoed.
	 * 
	 * @see #deleteAll(Collection) Deletion of multiple {@link KnowledgeItem}. This should be used
	 *      for performance reasons when deleting multiple items.
	 */
	public void delete(KnowledgeItem item) throws DataObjectException;

	/**
	 * Deletes all given {@link KnowledgeItem}s from the {@link KnowledgeBase}.
	 * 
	 * @param items
	 *        The objects to be deleted
	 * @throws DataObjectException
	 *         if delete fails for internal reasons
	 * @throws com.top_logic.knowledge.service.event.CommitVetoException
	 *         if delete is vetoed.
	 * 
	 * @see KBUtils#deleteAll(Iterable) Bulk deletion of multiple {@link TLObject}.
	 */
	public void deleteAll(Collection<? extends KnowledgeItem> items) throws DataObjectException;

    /**
     * Gets a MORepository instance.
     *
     * @return the MetaObjectRepository for this KnowledgeBase
     */
    public MORepository getMORepository ();

    /** Return a (read only) Copy of all KnowledgeObject in the Base 
     *<p>
     *  This fucntion should not be used often. It should be used for
     *  classes like Exporters that need to acces these fast.
     *</p>
     */
    public Collection<KnowledgeObject> getAllKnowledgeObjects();
    
    /** Return all KnowlegeObjects of the given type.
     * 
     * @param type valid name of a KnowlegeObjecttype found in the KB.
     * @return A (possibly empty) Collection of  KnolwedgeObjects.
     */
    public Collection<KnowledgeObject> getAllKnowledgeObjects(String type);

    /** Return a (read only) Copy of all KnowledgeAssociations in the Base. 
     *<p>
     *  This function should not be used often. It should be used for
     *  classes like Exporters that need to access these fast.
     *</p>
     */
    public Collection<KnowledgeAssociation> getAllKnowledgeAssociations();

    /**
     * Begin a (potentially nested) transaction in this {@link KnowledgeBase}.
     * 
     * @return the new transaction.
     * 
     * @see KnowledgeBase#beginTransaction(Message) Starting a named transaction.
     * @see Transaction#commit() Committing a transaction.
     * 
     * @since TL 5.7
     */
	public Transaction beginTransaction();
	
	/**
	 * Begin a (potentially nested) transaction in this {@link KnowledgeBase}.
	 * 
	 * @param commitMessage
	 *        The commit message that will be associated with a commit of the
	 *        new transaction.
	 * 
	 * @return the new transaction.
	 * 
	 * @see Transaction#commit() Committing a transaction.
	 * 
	 * @since TL 5.7
	 */
    public Transaction beginTransaction(Message commitMessage);
    
    /** 
     * Explicitly begin a (nested) Transaction in the KnowledgeBase.
     * <p>
     * For older KBases this is actually a noop. As of now
     * only the new DBKnolwedgeBase will actually support this.
     * </p>
     * @return true when Transactions are ok.
	 *
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link #beginTransaction(Message)}.
     */
    @Deprecated
    public boolean begin(); 

    /**
     * Permanently saves the changes of the KnowledgeBase.
     *<p>
     *  It can be assumed that by this call any changed data
     *  (for some context) will be saved. Anything else is up to
     *  the actual implementation. In case the commit fails, 
     *  this is equivalent to a rollback.
     *</p>
     * @return  true if the commit was successful false otherwise.
     *
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link Transaction#commit()}, see {@link #beginTransaction(Message)}.
     */
    @Deprecated
    public boolean commit();

    /** Rollback any changes made in the current context.
     *<p>
     *  A implementation may choose not to implement this function at all.
     *</p>
     * 
     * TODO #2829: Delete TL 6 deprecation 
     * @deprecated Use {@link Transaction#rollback(Message, Throwable)}, see {@link #beginTransaction(Message)}.
     */
    @Deprecated
    public boolean rollback();

	/**
	 * Reload all objects modified on a remote node since the last refetch.
	 * 
	 * <p>
	 * Like in local commits, changes refetched from remote nodes result in
	 * {@link UpdateEvent}s sent to registered
	 * {@link #addUpdateListener(UpdateListener) update listeners}. Events from
	 * remote nodes are explicitly marked as such, see
	 * {@link UpdateEvent#isRemote()}.
	 * </p>
	 * 
	 * @return the number atomic updates (create, modify, delete) performed on
	 *         other nodes since the last refetch.
	 * 
	 * @throws RefetchTimeout
	 *         If the refetch could not complete due to a timeout while waiting
	 *         for a concurrent commit.
	 */
	public int refetch() throws RefetchTimeout;

    // Event handling 
    
	/**
	 * Registers the given {@link ModificationListener} with this {@link KnowledgeBase}
	 * 
	 * <p>
	 * The given listener is informed about creations and deletions of objects within the current
	 * transaction.
	 * </p>
	 * 
	 * @param listener
	 *        The listener to add
	 * 
	 * @see #removeModificationListener(ModificationListener)
	 */
	public void addModificationListener(ModificationListener listener);

	/**
	 * Removes the given {@link ModificationListener} from this {@link KnowledgeBase}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * 
	 * @see #addModificationListener(ModificationListener)
	 */
	public void removeModificationListener(ModificationListener listener);

	/**
	 * Adds the given {@link CommitChecker} to this {@link KnowledgeBase} to be able to stop a
	 * commit depending on the made changes.
	 * 
	 * @param checker
	 *        The checker to add.
	 * 
	 * @see #removeCommitChecker(CommitChecker)
	 */
	public void addCommitChecker(CommitChecker checker);

	/**
	 * Removes the given {@link CommitChecker} from this {@link KnowledgeBase}.
	 * 
	 * @param checker
	 *        The listener to remove.
	 * 
	 * @see #addCommitChecker(CommitChecker)
	 */
	public void removeCommitChecker(CommitChecker checker);

	/**
	 * Registers the given {@link UpdateListener} with this
	 * {@link KnowledgeBase}.
	 * 
	 * <p>
	 * The given listener is informed of future changes of this
	 * {@link KnowledgeBase} immediately after a local commit or a refetch of a
	 * remote commit succeeds.
	 * </p>
	 * 
	 * @param listener
	 *        The listener to add.
	 * @return Whether the given listener was successfully added. This may
	 *         return <code>false</code>, if the given listener was already
	 *         registered with this {@link KnowledgeBase}.
	 * 
	 * @see #removeUpdateListener(UpdateListener) Removing a listener.
	 * @see #getUpdateChain() Attaching to the asynchronous chain of
	 *      {@link UpdateEvent}s.
	 */
    public boolean addUpdateListener(UpdateListener listener);

	/**
	 * Removes the given {@link UpdateListener} from this {@link KnowledgeBase}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the listener was successfully removed. This method may
	 *         return <code>false</code>, if the given listener was not
	 *         registered with this {@link KnowledgeBase} before.
	 *         
	 * @see #addUpdateListener(UpdateListener) Adding a listener.
	 */
    public boolean removeUpdateListener(UpdateListener listener);

	/**
	 * The tail of the {@link UpdateChain} of this {@link KnowledgeBase} starting with the last
	 * revision of this {@link KnowledgeBase}.
	 * 
	 * <p>
	 * The returned {@link UpdateChain} can be asynchronously queried (e.g. from a UI update thread)
	 * for updates that have occurred since the tail of the {@link UpdateChain} has been requested
	 * from this {@link KnowledgeBase}.
	 * </p>
	 * 
	 * @return The tail of the {@link UpdateChain}. The tail of the {@link UpdateChain} not
	 *         necessarily contains an {@link UpdateChain#getUpdateEvent() update event}. Only if
	 *         {@link UpdateChain#next()} called on the result returns a non-<code>null</code>
	 *         value, this value provides an {@link UpdateEvent}.
	 * 
	 * @see KnowledgeBase#getSessionUpdateChain()
	 */
	UpdateChain getUpdateChain();
    
	/**
	 * The tail of the {@link UpdateChain} of this {@link KnowledgeBase} starting with the current
	 * session revision.
	 * 
	 * <p>
	 * The returned {@link UpdateChain} can be asynchronously queried (e.g. from a UI update thread)
	 * for updates that have occurred since the tail of the {@link UpdateChain} has been requested
	 * from this {@link KnowledgeBase}.
	 * </p>
	 * 
	 * @return The tail of the {@link UpdateChain}. The tail of the {@link UpdateChain} not
	 *         necessarily contains an {@link UpdateChain#getUpdateEvent() update event}. Only if
	 *         {@link UpdateChain#next()} called on the result returns a non-<code>null</code>
	 *         value, this value provides an {@link UpdateEvent}.
	 * 
	 * @see KnowledgeBase#getUpdateChain()
	 */
	UpdateChain getSessionUpdateChain();

    /**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
     * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
     * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createObject(String, Iterable, Class)
	 */
	<T extends TLObject> T createObject(String typeName, Class<T> expectedType) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends TLObject> T createObject(String typeName, Iterable<Entry<String, Object>> initialValues,
			Class<T> expectedType) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createObject(Branch, TLID, String, Iterable)
	 */
	<T extends TLObject> T createObject(Branch branch, String typeName, Class<T> expectedType)
			throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createObject(Branch, TLID, String, Iterable)
	 */
	<T extends TLObject> T createObject(Branch branch, String typeName, Iterable<Entry<String, Object>> initialValues,
			Class<T> expectedType) throws DataObjectException;

	/**
	 * Create a new {@link TLObject}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createObject(Branch, String, Iterable, Class)
	 * @see #createObject(Branch, TLID, String, Iterable)
	 */
	TLObject createObject(Branch branch, String typeName, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException;

	/**
	 * Create a new {@link TLObject}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param objectName
	 *        A predefined identifier for the object. Should be <code>null</code> in almost any
	 *        cases to assign a synthetic ID.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable)
	 */
	TLObject createObject(Branch branch, TLID objectName, String typeName, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException;

    /** 
	 * Create a new {@link KnowledgeObject}.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param objectName
	 *        A predefined identifier for the object. Should be
	 *        <code>null</code> in almost any cases to assign a synthetic ID.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @return The new object that is not yet committed to the database. The
	 *         returned object must only be used by the creating thread up to
	 *         the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @deprecated Use {@link #createKnowledgeObject(Branch, TLID, String)}, or
	 *             {@link #createObject(String, Class)}.
     */
	@Deprecated
    public KnowledgeObject createKnowledgeObject(TLID objectName, String typeName)
        throws DataObjectException;

	/**
	 * Item creation without seting initial values.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createKnowledgeObject(Branch, TLID, String, Iterable)}, or
	 *             {@link #createObject(String, Iterable, Class)}.
	 */
	@Deprecated
	public KnowledgeObject createKnowledgeObject(TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException;

	/**
	 * Item creation without seting initial values.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createKnowledgeObject(Branch, String)}, or
	 *             {@link #createKnowledgeObject(Branch, TLID, String, Iterable)}.
	 */
	@Deprecated
	public KnowledgeObject createKnowledgeObject(Branch branch, TLID objectName, String typeName)
    	throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeObject} on a given {@link Branch}.
	 * 
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable, Class)
	 * @see #createObject(Branch, TLID, String, Iterable)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	public KnowledgeObject createKnowledgeObject(String typeName) throws DataObjectException;
    
    /**
	 * Create a new {@link KnowledgeObject} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable, Class)
	 * @see #createObject(Branch, TLID, String, Iterable)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	public KnowledgeObject createKnowledgeObject(Branch branch, String typeName) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeObject} on a given {@link Branch}.
	 * 
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable, Class)
	 * @see #createObject(Branch, TLID, String, Iterable)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	public KnowledgeObject createKnowledgeObject(String typeName, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeObject} on a given {@link Branch}.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @deprecated Use {@link #createObject(Branch, String, Iterable, Class)},
	 *             {@link #createObject(Branch, TLID, String, Iterable)}, or
	 *             {@link #createKnowledgeItem(Branch, TLID, String, Iterable, Class)}.
	 */
	@Deprecated
	public KnowledgeObject createKnowledgeObject(Branch branch, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException;
    
	/**
	 * Create a new {@link KnowledgeObject} on a given {@link Branch}.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param objectName
	 *        A predefined identifier for the object. Should be <code>null</code> in almost any
	 *        cases to assign a synthetic ID.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @deprecated Use {@link #createObject(Branch, String, Iterable, Class)},
	 *             {@link #createObject(Branch, TLID, String, Iterable)}, or
	 *             {@link #createKnowledgeItem(Branch, TLID, String, Iterable, Class)}.
	 */
	@Deprecated
	public KnowledgeObject createKnowledgeObject(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createKnowledgeItem(Branch, String, Iterable, Class)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends KnowledgeItem> T createKnowledgeItem(String typeName, Class<T> expectedType) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createKnowledgeItem(Branch, String, Iterable, Class)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends KnowledgeItem> T createKnowledgeItem(String typeName, Iterable<Entry<String, Object>> initialValues,
			Class<T> expectedType) throws DataObjectException;
	
	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable, Class)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends KnowledgeItem> T createKnowledgeItem(Branch branch, String typeName, Class<T> expectedType)
			throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * 
	 * @see #createKnowledgeItem(Branch, TLID, String, Iterable, Class)
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends KnowledgeItem> T createKnowledgeItem(Branch branch, String typeName,
			Iterable<Entry<String, Object>> initialValues, Class<T> expectedType) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param objectName
	 *        A predefined identifier for the object. Should be <code>null</code> in almost any
	 *        cases to assign a synthetic ID.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * @param expectedType
	 *        Java implementation type of the result.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	<T extends KnowledgeItem> T createKnowledgeItem(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues, Class<T> expectedType) throws DataObjectException;
	
	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	KnowledgeItem createKnowledgeItem(Branch branch, String typeName) throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	KnowledgeItem createKnowledgeItem(Branch branch, String typeName, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException;

	/**
	 * Create a new {@link KnowledgeItem} on a given {@link Branch}.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param objectName
	 *        A predefined identifier for the object. Should be <code>null</code> in almost any
	 *        cases to assign a synthetic ID.
	 * @param typeName
	 *        The name of the {@link MOClass} type of the object to be created.
	 * @param initialValues
	 *        Values to set during item construction.
	 * 
	 * @return The new object that is not yet committed to the database. The returned object must
	 *         only be used by the creating thread up to the time it is committed.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 * @see #createObject(Branch, String, Iterable, Class)
	 */
	KnowledgeItem createKnowledgeItem(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException;

	/**
	 * Creates a list of {@link KnowledgeItem} from the given {@link ObjectCreation}.
	 * 
	 * @param creations
	 *        A sequence of {@link ObjectCreation} which describe how the newly constructed object
	 *        must be.
	 * @param expectedType
	 *        The common super type of all created Objects.
	 * 
	 * @return A list of new {@link KnowledgeItem} described by the given {@link ObjectCreation}.
	 * 
	 * @throws DataObjectException
	 *         If allocation fails.
	 */
	<T extends KnowledgeItem> List<T> createObjects(Iterable<ObjectCreation> creations, Class<T> expectedType)
			throws DataObjectException;
         
	/**
	 * Create a new KnowledgeAssociation with given type between src and dest. The Association will
	 * be stored in KnowledgeBase.
	 * 
	 * @param src
	 *        the source of the link.
	 * @param dest
	 *        the destination of the link.
	 * @param typeName
	 *        the MetaObject (type) name of the KA to be created.
	 * 
	 * @return The created {@link KnowledgeAssociation} link.
	 * 
	 * @throws DataObjectException
	 *         in case creation fails for some reason.
	 */
	public KnowledgeAssociation createAssociation(KnowledgeItem src, KnowledgeItem dest,
			String typeName) throws DataObjectException;

	/**
	 * Create a new KnowledgeAssociation with given type between src and dest. The Association will
	 * be stored in KnowledgeBase.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param src
	 *        the source of the link.
	 * @param dest
	 *        the destination of the link.
	 * @param typeName
	 *        the MetaObject (type) name of the link to be created.
	 * 
	 * @return The created {@link KnowledgeAssociation} link.
	 * 
	 * @throws DataObjectException
	 *         in case creation fails for some reason.
	 */
	public KnowledgeAssociation createAssociation(Branch branch, KnowledgeItem src, KnowledgeItem dest,
			String typeName) throws DataObjectException;

	/**
	 * Create a new KnowledgeAssociation with given type between src and dest. The Association will
	 * be stored in KnowledgeBase.
	 * 
	 * @param branch
	 *        The branch, on which the object is created.
	 * @param objectName
	 *        A predefined identifier for the object. Should be <code>null</code> in almost any
	 *        cases to assign a synthetic ID.
	 * @param src
	 *        the source of the link.
	 * @param dest
	 *        the destination of the link.
	 * @param typeName
	 *        the MetaObject (type) name of the link to be created.
	 * 
	 * @return The created {@link KnowledgeAssociation} link.
	 * 
	 * @throws DataObjectException
	 *         in case creation fails for some reason.
	 */
	public KnowledgeAssociation createAssociation(Branch branch, TLID objectName, KnowledgeItem src,
			KnowledgeItem dest, String typeName) throws DataObjectException;

    /**
	 * Create a new KnowledgeAssociation with given type between src and dest. The Association will
	 * be stored in KnowledgeBase.
	 *
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @param objectName
	 *        Id of the link, <code>null</code> generates one.
	 * @param srcKO
	 *        the source of the link.
	 * @param destKO
	 *        the destination of the link.
	 * @param typeName
	 *        the MetaObject (type) name of the link to be created.
	 * 
	 * @return the created KnowledgeAssociation
	 * 
	 * @throws DataObjectException
	 *         in case creation fails for some reason.
	 * 
	 * @deprecated Use {@link #createAssociation(KnowledgeItem, KnowledgeItem, String)}, or
	 *             {@link #createAssociation(Branch, TLID, KnowledgeItem, KnowledgeItem, String)} .
	 */
	@Deprecated
	public KnowledgeAssociation createAssociation(TLID objectName, KnowledgeItem srcKO, KnowledgeItem destKO,
			String typeName) throws DataObjectException;

	/**
	 * Lookup all association links in the given type.
	 * 
	 * @param associationTypeName
	 *        The name of the association type to retrieve links from.
	 * @return All association links of the requested type.
	 */
	public Collection<KnowledgeAssociation> getAllKnowledgeAssociations(String associationTypeName);
	
	/**
	 * The {@link HistoryManager} of this {@link KnowledgeBase}.
	 */
	HistoryManager getHistoryManager();

	/**
	 * Resolved the {@link KnowledgeItem#tId()} back to the
	 * {@link KnowledgeItem}.
	 * 
	 * @param objectKey
	 *        The key to resolve the object for.
	 * @return The {@link KnowledgeItem} identified by the given key.
	 */
	public KnowledgeItem resolveObjectKey(ObjectKey objectKey);

	/**
	 * Same as {@link #resolveObjectKey(ObjectKey)} for objects that are currently in cache.
	 * 
	 * @param objectKey
	 *        The key to resolve the object for.
	 * @return The {@link KnowledgeItem} identified by the given key, if already in cache,
	 *         <code>null</code> otherwise.
	 */
	KnowledgeItem resolveCachedObjectKey(ObjectKey objectKey);

	/**
	 * Create a reader that reports {@link ChangeSet} in the given revision range.
	 * 
	 * <p>
	 * Note: This method must be exclusively used with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * r = knowledgeBase.getChangeSetReader(...);
	 * try {
	 *    ...
	 * } finally {
	 *    r.close();
	 * }
	 * </pre>
	 * 
	 * @param readerConfig
	 *        {@link ReaderConfig#getStartRev()} is the minimum revision (inclusive), for which
	 *        events are requested. {@link ReaderConfig#getStopRev()} is the maximum revision
	 *        (inclusive), for which events are requested. {@link ReaderConfig#getStartRev()} must
	 *        not be larger than {@link ReaderConfig#getStopRev()}
	 * 
	 * @return The reader that reports events. The caller is responsible for closing the returned
	 *         reader.
	 * 
	 * @see ReaderConfigBuilder Creating a {@link ReaderConfig}.
	 */
	ChangeSetReader getChangeSetReader(ReaderConfig readerConfig);

	/**
	 * Create a writer that literally applies events to this
	 * {@link KnowledgeBase}.
	 * 
	 * <p>
	 * This writer must only be used during a migration, when the application is
	 * in single-user mode (e.g. during an application startup). All event
	 * information is preserved, especially commit numbers and commit dates.
	 * Therefore, writing will fail, definitively fail, if the events are taken
	 * from the same {@link KnowledgeBase} instance without an additional
	 * processing step.
	 * </p>
	 * 
	 * <p>
	 * Note: This method must be exclusively used with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * r = knowledgeBase.getReplayWriter();
	 * try {
	 *    ...
	 * } finally {
	 *    r.close();
	 * }
	 * </pre>
	 * 
	 * @return The writer that accepts events. The caller is responsible for
	 *         closing the returned writer.
	 * 
	 * @see DefaultEventWriter Applying events without preserving commit
	 *      information.
	 */
	EventWriter getReplayWriter();

	/**
	 * Returns a {@link EventReader} that delivers {@link KnowledgeEvent} to come from the start
	 * revision on the start branch to the stop revision on the stop branch, i.e. the events which
	 * transfer the data on the start branch at start revision into the data which are on the stop
	 * branch at stop revision.
	 * 
	 * <p>
	 * No {@link BranchEvent} will be reported.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b> <br/>
	 * Let b1 be a branch with base branch <code>TRUNK</code> based on revision r1 and b2 a branch
	 * of <code>TRUNK</code> with base revision r2 &lt; r1. Let r3 and r4 be revisions such that r4
	 * &gt; r2, r3 &gt; r1.
	 * 
	 * <br/>
	 * 
	 * If this method is called using r3, b1, r4, and b2, then a {@link EventReader} will be created
	 * returning the following events:
	 * <ul>
	 * <li>inverted events happened on branch b1 from r1 to r3</li>
	 * <li>inverted events happened on <code>TRUNK</code> from r1 to r2</li>
	 * <li>events happened on b2 from r2 to r4</li>
	 * </ul>
	 * 
	 * If these events will be used on b1 at revision r1, then the data on branch b1 are the same as
	 * the data on branch b2 at revision r4.
	 * </p>
	 * 
	 * <p>
	 * <b>Concrete simple example:</b> <code>TRUNK</code> serves as <code>startBranch</code> and
	 * <code>stopBranch</code>
	 * </p>
	 * 
	 * <ol>
	 * <li>Initial: <i>b</i> has a value <code>b=5</code></li>
	 * <li>revision r0</li>
	 * <li><i>b</i> got value <code>b=7</code></li>
	 * <li>revision r1</li>
	 * <li><i>b</i> got value <code>b=9</code></li>
	 * <li>revision r2</li>
	 * </ol>
	 * 
	 * <p>
	 * The returned reader with <code>startRev=r0</code> and <code>stopRev=r2</code> and
	 * <code>detailed=false</code> delivers a change event with old value 5 and new value 9; for
	 * <code>detailed=true</code> the returned reader delivers a change event with old value 5 and
	 * new value 7 and a change event with old value 7 and new value 9.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This method must be exclusively used with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * r = knowledgeBase.getDiffReader(...);
	 * try {
	 *    ...
	 * } finally {
	 *    r.close();
	 * }
	 * </pre>
	 * 
	 * @param startRev
	 *        the revision to start from
	 * @param startBranch
	 *        the start branch from which the events are requested
	 * @param stopRev
	 *        the revision to which should be navigated
	 * @param stopBranch
	 *        the branch to which should be navigated
	 * @param detailed
	 *        whether all events which occurred should be delivered. That includes also
	 *        {@link CommitEvent}. Otherwise it is possible that not each change of objects is
	 *        represented by some event delivered by the returned {@link EventReader}.
	 */
	ChangeSetReader getDiffReader(Revision startRev, Branch startBranch, Revision stopRev, Branch stopBranch, boolean detailed);

	/**
	 * Short cut for {@link #search(HistoryQuery, HistoryQueryArguments)} with default arguments.
	 */
	Map<?, List<LongRange>> search(HistoryQuery query);

	/**
	 * Executes the given {@link HistoryQuery}.
	 * 
	 * @param query
	 *        The query to execute.
	 * @param queryArguments
	 *        The arguments for the given query.
	 * @return The items that match the given query.
	 */
	Map<?, List<LongRange>> search(HistoryQuery query, HistoryQueryArguments queryArguments);

	/**
	 * Short cut for {@link #search(RevisionQuery, RevisionQueryArguments)} with default arguments.
	 */
	<E> List<E> search(RevisionQuery<E> query);

	/**
	 * Executes the given {@link RevisionQuery}.
	 * 
	 * @param query
	 *        The query to execute.
	 * @param queryArguments
	 *        The arguments for the given query.
	 * @return The items that match the given query.
	 */
	<E> List<E> search(RevisionQuery<E> query, RevisionQueryArguments queryArguments);

	<E> CloseableIterator<E> searchStream(RevisionQuery<E> query);

	<E> CloseableIterator<E> searchStream(RevisionQuery<E> query, RevisionQueryArguments queryArguments);

	/**
	 * Processes the given {@link RevisionQuery} to create a {@link CompiledQuery} which is ready to
	 * execute the query with the actual arguments.
	 * 
	 * @param <E>
	 *        the type of the result
	 * @param query
	 *        the query to process
	 * 
	 * @return a compiled variant of the query which can be cashed and processed with different
	 *         arguments.
	 * 
	 * @see KnowledgeBase#getQueryCache() Cache for queries for multiple usage.
	 */
	<E> CompiledQuery<E> compileQuery(RevisionQuery<E> query);

	/**
	 * Compiles a simple query that searches in a special revision whose result is updated with the
	 * current transaction.
	 * 
	 * @param simpleQuery
	 *        the arguments needed to create that simple query
	 * 
	 * @return a {@link CompiledQuery} to execute to get the requested objects
	 * 
	 * @see SimpleQuery definition of the actual query
	 * @see KnowledgeBase#getQueryCache() Cache for queries for multiple usage.
	 */
	<E> CompiledQuery<E> compileSimpleQuery(SimpleQuery<E> simpleQuery);

	/**
	 * Returns the {@link CompiledQueryCache cache} for {@link CompiledQuery} that were complied by
	 * this {@link KnowledgeBase}.
	 * 
	 * @see KnowledgeBase#compileQuery(RevisionQuery)
	 * @see KnowledgeBase#compileSimpleQuery(SimpleQuery)
	 */
	CompiledQueryCache getQueryCache();

	/**
	 * Runs the given job without any local modifications, i.e. if e.g. an item is locally deleted
	 * but not yet committed it can be found during execution of the job.
	 * 
	 * <p>
	 * It is not possible to make modifications during the job or begin any commit.
	 * </p>
	 * 
	 * @param job
	 *        The job to execute.
	 * @return The result of {@link Computation#run()}.
	 */
	<T, E1 extends Throwable, E2 extends Throwable> T withoutModifications(ComputationEx2<T, E1, E2> job) throws E1, E2;

	/**
	 * Runs the given job assuming the current session revision were at some time in the past.
	 * 
	 * <p>
	 * It is not possible to make modifications during the job or begin any commit.
	 * </p>
	 * 
	 * @param rev
	 *        The revision that designates the point in time that should be simulated.
	 * @param job
	 *        The job to execute.
	 * @return The result of {@link Computation#run()}.
	 */
	<T, E1 extends Throwable, E2 extends Throwable> T inRevision(long rev, ComputationEx2<T, E1, E2> job) throws E1, E2;

	/**
	 * Returns the {@link KnowledgeBaseConfiguration} which was used to
	 * {@link #initialize(Protocol, KnowledgeBaseConfiguration)} this {@link KnowledgeBase}.
	 * 
	 */
	KnowledgeBaseConfiguration getConfiguration();

	/**
	 * The {@link SchemaSetup} that is used to create the tables of this {@link KnowledgeBase}.
	 * 
	 * <p>
	 * This method must not be called before the {@link KnowledgeBase} is
	 * {@link #initialize(Protocol, KnowledgeBaseConfiguration) initialised}.
	 * </p>
	 */
	SchemaSetup getSchemaSetup();

	/**
	 * Returns the size of the cache if used. Otherwise 0 is returned.
	 */
	int getCacheSize();
}
