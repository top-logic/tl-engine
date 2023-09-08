/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.device.db;

import java.util.Hashtable;
import java.util.Iterator;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.Committable;

/**
 * Make DBUserRepository part of commit procession.
 * 
 * This implementation caches write accesses to the DBUser repository and writes
 * them to the DB upon the next commit for a given thread
 * 
 * @author    <a href="mailto:tri@top-logic.com">Tomate Richter</a>
 */
public class CommitableDBUserRepository implements Committable {

	/**
	 * Each thread will get its own instance of this class
	 */
	private static ThreadLocal instance = new ThreadLocal();
	
	/**
	 * Cache of changed/updated user data objects
	 */
	private Hashtable changed;

	/**
	 * Cache of deleted user data objects
	 */
	private Hashtable deleted;
	
	/**
	 * Cache of newly created user data objects
	 */
	private Hashtable created;

	private DBUserRepository _repository;

	/**
	 * CTor, used by getInstance() only
	 */
	private CommitableDBUserRepository(DBUserRepository repository){
		super();
		_repository = repository;
		this.changed = new Hashtable();
		this.deleted = new Hashtable();
		this.created = new Hashtable();
	}

	/**
	 * upon finish of the thread (rollback or commit),
	 * it cleans up the instance that was created for the thread
	 */
	private void cleanup() {
        synchronized (CommitableDBUserRepository.class) {
            instance.set(null); // remove me from the Thread ...
        }
		this.changed = null;
		this.deleted = null;
		this.created = null;
	}

	/* (non-Javadoc)
	 * @see com.top_logic.knowledge.service.Committable#prepare(java.lang.Object)
	 */
	@Override
	public boolean prepare(CommitContext aContext) {
        return doCreate(aContext) && doUpdate(aContext) && doDelete(aContext);
	}

	/** 
	 * @see com.top_logic.knowledge.service.Committable#complete(CommitContext)
	 */
	@Override
	public void complete(CommitContext aContext) {
	}
	
	/* (non-Javadoc)
	 * @see com.top_logic.knowledge.service.Committable#prepareDelete(java.lang.Object)
	 */
	@Override
	public boolean prepareDelete(CommitContext aContext) {
		// not supported, dont spoil the commit, though
		return true;
	}

	/* (non-Javadoc)
	 * @see com.top_logic.knowledge.service.Committable#commit(java.lang.Object)
	 */
	@Override
	public boolean commit(CommitContext aContext) {
		cleanup();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.top_logic.knowledge.service.Committable#rollback(java.lang.Object)
	 */
	@Override
	public boolean rollback(CommitContext aContext) {
		cleanup();
		return true;
	}

	/**
	 * Create a new User user within the user repository.
	 * Creation is done transient, that means the DO is just added to
	 * the cache
	 * @param anUserDO  The user to store in the Repository.
	 */
	public void createUser(DataObject anUserDO) throws DataObjectException {
		this.created.put(anUserDO.getAttributeValue(PersonAttributes.USER_NAME),anUserDO);
		this.deleted.remove(anUserDO.getAttributeValue(PersonAttributes.USER_NAME));
		this.changed.remove(anUserDO.getAttributeValue(PersonAttributes.USER_NAME));
	}

	/** 
	 * Delete the given user DO from the Database
	 * Deletion is done transient, that means the DO is just added to
	 * the deleted - cache
	 */
	public boolean deleteUser(DataObject anUserDO) throws DataObjectException {
        Object name = anUserDO.getAttributeValue(PersonAttributes.USER_NAME);
		this.deleted.put   (name,anUserDO);
		this.changed.remove(name);
		this.created.remove(name);
		return true;
	}

	/**
	 * true if the given name is known to be deleted, but deletion has not been commited yet
	 */
	public boolean isDeleted(String aName){
		return this.deleted.containsKey(aName);
	}
	
	/** 
	 * Return a single User matching the given CN.
	 * Searches for the user in the transient cache, that means it will be found
	 * if it was newly created or changed, but not commited yet
	 * @return null in case no such user exists,
	 */
	public DataObject searchUser(String cn) {
		Object theResult = this.created.get(cn);
		if(theResult==null){
			theResult=this.changed.get(cn);
		}
		return (DataObject)theResult;
	}

	/**
	 * Updates the given userDo
	 * Update is done transient, that means the DO is just added to
	 * the cache
	 * @param anUserDO - the DO with changed attribute values
	 */
	public void updateUser(DataObject anUserDO) throws DataObjectException {
		if(!this.deleted.containsKey(anUserDO.getAttributeValue(PersonAttributes.USER_NAME))){
			this.changed.put(anUserDO.getAttributeValue(PersonAttributes.USER_NAME),anUserDO);	
		}
	
	}

	/**
	 * Performs the actual user creation on the database for all cached DOs.
	 * This method is called upon commit() of this thread.
	 * @param context the {@link CommitContext} to be used
	 * @return true if succeeded
	 */
	private boolean doCreate(CommitContext context){
		boolean result = true;
		Iterator it = this.created.values().iterator();
		while(it.hasNext()){
			try {
				result = result && doCreate(context,(DataObject)it.next());
			} catch (Exception e) {
				result = false;
				Logger.error("Problem updating DO",e,this);
                break;
			}
		}
		return result;
	}

	/**
	 * Performs the actual user creation on the database for the given DO.
	 * This method is called for each of the cached DOs in the create - cache.
	 * @param context the {@link CommitContext} to be used
	 * @param anUserDO - the DO to be written
	 * @return true if succeeded
	 */
	private boolean doCreate(CommitContext context, DataObject anUserDO) throws Exception{
		PooledConnection con = context.getConnection();
		boolean success = _repository.createUser(con, anUserDO);
		if (success) {
			con.commit();
		} else {
			con.rollback();
		}
		
		return success;
	}
	
	/**
	 * Performs the actual user deletion on the database for all cached DOs.
	 * This method is called upon commit() of this thread.
	 * @param context the {@link CommitContext} to be used
	 * @return true if succeeded
	 */	
	private boolean doDelete(CommitContext context){
		Iterator it = this.deleted.values().iterator();
		boolean result = true;
		while(it.hasNext()){
			try {
				result = result && doDelete(context,(DataObject)it.next());
			} catch (Exception e) {
				Logger.error("Problem updating DO",e,this);
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Performs the actual user deletion on the database for the given DO.
	 * This method is called for each of the cached DOs in the delete - cache.
	 * @param context the {@link CommitContext} to be used
	 * @param aUserDO - the DO to be deleted
	 * @return true if succeeded
	 */	
	private boolean doDelete(CommitContext context, DataObject aUserDO) throws Exception{
		PooledConnection con = context.getConnection();
		boolean success = _repository.deleteUser(con, aUserDO);
		if (success) {
			con.commit();
		} else {
			con.rollback();
		}
		return success;
	}

	/**
	 * Performs the actual user update on the database for all cached DOs.
	 * This method is called upon commit() of this thread.
	 * @param context the {@link CommitContext} to be used
	 * @return true if succeeded
	 */	
	private boolean doUpdate(CommitContext context){
		boolean result = true;
		Iterator it = this.changed.values().iterator();
		while(it.hasNext() && result){
			try {
				result = doUpdate(context,(DataObject)it.next());
			} catch (Exception e) {
				Logger.error("Problem updating DO",e,this);
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Performs the actual user update on the database for the given DO.
	 * This method is called for each of the cached DOs in the update cache.
	 * @param context the {@link CommitContext} to be used
	 * @param anUserDO - the DO to be deleted
	 * @return true if succeeded
	 */		
	private boolean doUpdate(CommitContext context, DataObject anUserDO) throws Exception{
		PooledConnection con = context.getConnection();
		boolean success = _repository.updateUser(con, anUserDO);
		if (success) {
			con.commit();
		} else {
			con.rollback();
		}
		return success;
	}

    /**
     * the instance for the current thread, if none exists, it returns null
     */
    static CommitableDBUserRepository getForCurrentThread(){
        return (CommitableDBUserRepository) instance.get();
    }
    
    /**
     * an instance of this class for the current thread. If such an instance
     * already exists, it is returned, otherwise a new instance will be created.
     * NOTE: it is imperative that after calling this method either a commit or a rollback is
     * called within this thread, to make sure the created instances are properly cleaned up and 
     * are not accidently reused later. So take care when to create such an instance ! ... thats
     * why this is only package scope.
     */
	static synchronized CommitableDBUserRepository getInstance(CommitHandler context, DBUserRepository userRepository) {
        CommitableDBUserRepository theInstance = (CommitableDBUserRepository) instance.get();
        if(theInstance==null){
			theInstance = new CommitableDBUserRepository(userRepository);
            instance.set(theInstance);
        }
        context.addCommittable(theInstance);
        return theInstance;
    }



}
