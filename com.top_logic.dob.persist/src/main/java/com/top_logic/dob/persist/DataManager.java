/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.persist;


import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.NamedValues;

/**
 * A Generic interface to store and load DataObjects into some storage.
 * 
 * Usually this is a database but the time coming will show how this
 * can be used otherwise ;-).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class DataManager extends ManagedClass {

    /** The default number of Attributes assumed */
    public static final int DEFAULT_SIZE = 16;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DataManager}.
	 */
	public DataManager(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * Implicit super constructor for subclasses for non-configuration
	 * constructors.
	 */
    protected DataManager() {
    	// Default constructor.
    }

    /**
	 * Create an empty DataObject that an be used optimally by this DataManager.
	 * <p>
	 * (It is assumed, that the id and type will match those of a KnowledgeObject as it is used in
	 * the FlexWrapper)
	 * </p>
	 * 
	 * @param aType
	 *        An arbitrary type for the new DataObject.
	 * @param anID
	 *        An arbitrary id for the new DataObject.
	 * @param size
	 *        A hint how many Attributes you expect.
	 */
    public abstract DataObject createDataObject(String aType, TLID anID, int size);

    /**
	 * Load the object defined by the given information.
	 * 
	 * This method load the data object defined by the given ID and type. The returned DataObject
	 * should act as the one stored for returning data.
	 * 
	 * @param aType
	 *        The type of the meta object of the data object.
	 * @param anID
	 *        The ID of the data object to be found.
	 * @return The found DataObject or <code>null</code>, if no such object defined in the
	 *         persistency.
	 * 
	 * @throws SQLException
	 *         When loading the object failed for database access failures.
	 */
    public abstract DataObject load(String aType, TLID anID) throws SQLException;

    /**
	 * Store the given data object.
	 * 
	 * Should be implemented as atomic operation.
	 * 
	 * @param anObject
	 *        The object to be stored.
	 * @return <code>true</code>, if storing succeeds.
	 * 
	 * @throws SQLException
	 *         When storing the object failed for database access failures.
	 */
	public abstract boolean store(DataObject anObject) throws SQLException;

    /**
	 * Store the given data object.
	 * 
	 * Should be implemented as atomic operation.
	 * 
	 * @param anObject
	 *        The object to be stored.
	 * @param aContext
	 *        The context to be used for storage.
	 * @return <code>true</code>, if storing succeeds.
	 * 
	 * @throws SQLException
	 *         When storing the object failed for database access failures.
	 */
	public abstract boolean store(DataObject anObject, CommitContext aContext) throws SQLException;

    /**
	 * Delete the given data object.
	 * 
	 * Should be implemented as atomic operation.
	 * 
	 * @param anObject
	 *        The object to be deleted, must not be null
	 * @return <code>true</code>, if deleting succeeds, false indicates that nothing was deleted
	 *         (which still may be ok.)
	 * 
	 * @throws SQLException
	 *         When deleting the object failed for database access failures.
	 */
    public abstract boolean delete(DataObject anObject) throws SQLException;

    /**
	 * Delete the given data object.
	 * 
	 * Should be implemented as atomic operation.
	 * 
	 * @param anObject
	 *        The object to be deleted, must not be null
	 * @param aContext
	 *        The context to be used for storage.
	 * @return <code>true</code>, if deleting succeeds, false indicates that nothing was deleted
	 *         (which still may be ok.)
	 * 
	 * @throws SQLException
	 *         When deleting the object failed for database access failures.
	 */
    public abstract boolean delete(DataObject anObject, CommitContext aContext) throws SQLException;

    /**
	 * Close this {@link DataManager} by removing all its resources.
	 * 
	 * After calling close() the DataManager cannot be used any more.
	 */
    public abstract void close();

    public static DataManager getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Stores the given values by associating them with an object with the given type and id.
	 * 
	 * <p>
	 * Essentially the same as {@link #store(DataObject, CommitContext)} but using the given type
	 * and ID instead of the identification provided by the {@link DataObject}.
	 * </p>
	 * 
	 * @param context
	 *        The context in which the store should happen.
	 * @param type
	 *        The type of the objects the given values should be associated with.
	 * @param id
	 *        The ID of the objects the given values should be associated with.
	 * @param values
	 *        The values to store
	 * @return Whether storing succeeded.
	 * 
	 * @throws SQLException
	 *         When storing the values fail for database access failures.
	 */
	public abstract boolean storeValues(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException;

	/**
	 * Delete all values associated with the object with the given type and id.
	 * 
	 * <p>
	 * Essentially the same as {@link #delete(DataObject, CommitContext)} but using the given type
	 * and ID instead of the identification provided by the {@link DataObject}.
	 * </p>
	 * 
	 * @param context
	 *        The context in which the store should happen.
	 * @param type
	 *        The type of the objects the given values should be associated with.
	 * @param id
	 *        The ID of the objects the given values should be associated with.
	 * @param values
	 *        The current values (required for recursively deleting nested values, which is not
	 *        supported anyway).
	 * @return Whether deleting succeeded.
	 * 
	 * @throws SQLException
	 *         When deleting the values fail for database access failures.
	 */
	public abstract boolean deleteValues(CommitContext context, String type, TLID id, NamedValues values)
			throws SQLException;
	
	@Override
	protected void shutDown() {
		close();
		super.shutDown();
	}
	
	/**
	 * Module for {@link DataManager}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<DataManager> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<DataManager> getImplementation() {
			return DataManager.class;
		}
	}

}