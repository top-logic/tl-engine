/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.util;

import java.util.Properties;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Thread and cluster save task.
 * 
 * This task will try to get an {@link Lock}. If this succeeds, it'll continue 
 * its work by calling {@link #perform()}. If the token context cannot be aquired, the task will
 * pause and restart as defined in {@link TaskImpl}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class TokenBasedTask<C extends TokenBasedTask.Config<?>> extends TaskImpl<C> implements InContext {

	/**
	 * Special configuration for {@link TokenBasedTask}
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends TokenBasedTask<?>> extends TaskImpl.Config<I> {

		/**
		 * Use begin/commit only when set (default)
		 */
		@BooleanDefault(true)
		boolean isUseCommit();
	}

    /**
     * Default lock period in milli seconds for a token.
     */
    public static final int DEFAULT_LOCK_MILLIS = 5 * 60 * 1000;   // 5 minutes 

    /**
     * Lease time of a token in milli seconds.
     */
    private int lockMillis;

    
    /** Use begin/commit only when set (default) */
	private boolean useCommit;

    /**
     * @param    someProps    The task scheduling properties.
     */
    public TokenBasedTask(Properties someProps) {
        super(someProps);
        useCommit = true;
    }

	/**
	 * Creates a {@link TokenBasedTask} form the given configuration.
	 */
	public TokenBasedTask(InstantiationContext context, C config) {
    	super(context, config);
		this.useCommit = config.isUseCommit();
	}

    /**
     * @param  someProps    The task scheduling properties.
     * @param  aUseCommit   begin/commit only when set 
     */
    public TokenBasedTask(Properties someProps, boolean aUseCommit) {
        super(someProps);
        useCommit = aUseCommit;
    }

    /** 
     * Method to be implemented to do the work.
     * 
     * @return    <code>true</code> when knowledge base has to be commited.
     */
    protected abstract boolean perform();

    /**
	 * Return the name of the lock operation used for locking this task.
	 * 
	 * @return The requested name, must not be <code>null</code>.
	 * @see LockService#createLock(String, Object...)
	 */
    protected abstract String getLockOperation();

    /**
	 * Entry point to start this task's thread.
	 * 
	 * This method will create a valid token context in {@link #lockToken()}. If this succeeds, a
	 * context to work in will be generated. After this setup procedures, {@link #perform()} will be
	 * called to do the work. When everything is finished, this method will do the clean up
	 * {@link #unlockToken(Lock)}.
	 * 
	 * @see java.lang.Runnable#run()
	 */
    @Override
	public synchronized void run() {
        super.run();
        
		getLog().taskStarted();

		InContext job;
		if (this.workInSuperUserMode()) {
			job = new InContext() {
				
				@Override
				public void inContext() {
					ThreadContext.pushSuperUser();
					try {
						TokenBasedTask.this.inContext();
					} finally {
						ThreadContext.popSuperUser();
					}
					
				}
			};
		} else {
			job = this;
		}
		TLContextManager.inPersonContext(fetchTaskPerson(), job);
	}

	private Person fetchTaskPerson() {
		return ThreadContextManager.inSystemInteraction(TokenBasedTask.class, new Computation<Person>() {

			@Override
			public Person run() {
				return getTaskPerson();
			}
		});
	}

	@Override
	public void inContext() {
        Lock theTokenContext = this.lockToken();
        if (theTokenContext != null) {
            Transaction theTX = null;

            if (useCommit) {
            	theTX = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();
            }

            try {
                long start = System.currentTimeMillis();

                if (this.perform() && theTX != null) {
                	try {
                		theTX.commit();
                	}
                	catch (KnowledgeBaseException ex) {
                		getLog().taskEnded(ResultType.ERROR, I18NConstants.TASK_COMMIT_FAILED, ex);

						Logger.error(Resources.getSystemInstance().getString(I18NConstants.TASK_COMMIT_FAILED), ex,
							TokenBasedTask.class);
                		return;
                	}
                }

                long   theDelta = System.currentTimeMillis() - start;
				String duration = DebugHelper.getTime(theDelta);

                if (theDelta > 3000) {
					Logger.info("run() needed " + duration, this);
                }

				getLog().taskEnded(ResultType.SUCCESS, getResultMessageKey(duration));
            }
            catch (Exception ex) {
				Logger.error("run(): Unexpected error during execution", ex, this);

				getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
            }
            finally {
				if (theTX != null) {
					theTX.rollback();
				}
                this.unlockToken(theTokenContext);
            }
        }
    }

	/**
	 * Returns an I18N key that is added to the {@link TaskResult} if the task completes
	 * successfully.
	 * 
	 * @param duration
	 *        A String representation of the duration which the task needs to perform.
	 */
	protected ResKey getResultMessageKey(String duration) {
		return I18NConstants.TASK_COMPLETED_SUCCESSFULLY__DURATION.fill(duration);
	}

    /**
     * Don't interrupt work...
     * 
     * @see com.top_logic.basic.sched.Batch#signalStop()
     */
    @Override
	public boolean signalStopHook() {
        // make sure we are not shut down by force during a cycle 
        // as this could cause corrupted database
        return (false);
        
        // FIXME MGA care for sigalShutdown ...
    }

    /**
	 * Return the context base to be used when requesting a token context.
	 * 
	 * @return The requested context base.
	 * @see LockService#createLock(String, Object...)
	 */
    protected Object getLockObject() {
		return null;
    }

    /** 
     * Return a flag to work in super user mode.
     * 
     * @return    Always <code>true</code> in this implementation.
     */
    protected boolean workInSuperUserMode() {
        return (true);
    }

    /** 
     * Return the person, who performs this task.
     * 
     * @return    The person to run the task, must not be <code>null</code>.
     */
    protected Person getTaskPerson() {
        return PersonManager.getManager().getRoot();
    }

    /** 
     * Get the correct token context.
     * 
     * If getting the token fails for a reason, return <code>null</code>.
     * 
     * @return    The requested token context or <code>null</code>, if getting the token failed.
     * @see       #unlockToken(Lock)
     */
    protected Lock lockToken() {
        Lock theToken = null;
        String               theName  = this.getLockOperation();

        try {
			Object             theBase    = this.getLockObject();

            if (StringServices.isEmpty(theName)) {
                throw new IllegalStateException("No context name available - cannot acquire context");
            }

			theToken = LockService.getInstance().createLock(theName, theBase);
			if (!theToken.tryLock(this.getLockMillis())) {
                theToken = null;
            }
        }
        catch (Exception ex) {
            Logger.error("Unable to lock token '" + theName + "'!", ex, this);
        }

        return theToken;
    }

    /** 
     * Unlock the token context after finishing work.
     * 
     * @param    aContext    The token context to be unlocked.
     * @see      #lockToken()
     */
    protected void unlockToken(Lock aContext) {
        if (aContext != null) {
            try {
                aContext.unlock();
            }
            catch (Exception ex) {
                // Ignore, but make sure the rest of finally is executed...
            }
        }
        else if (this.getLockOperation() != null) {
            Logger.warn("Unable to get token in task, skipping this cycle", this);
        }
    }

    /** 
     * Lease time for the token. 
     * 
     * @return    The lease time.
     * @see       #lockToken()
     */
    protected int getLockMillis() {
        if (this.lockMillis <= 0) {
            this.lockMillis = TokenBasedTask.DEFAULT_LOCK_MILLIS;
        }
        
        return (this.lockMillis);
    }
}
