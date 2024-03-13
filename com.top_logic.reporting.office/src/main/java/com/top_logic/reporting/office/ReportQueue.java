/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.InContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;


/**
 * A queue mechanism to process reporthandlers. This queue provides a list where
 * the scheduled reportHandler are located and a map with all the report
 * currently in the queue.
 * 
 * TODO JCO shutting down mechanism!
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportQueue extends Thread {
    
    /** a list of ReportTokens with the tokens for the jobs to execute */
	private ConcurrentLinkedQueue<ReportToken> tokenQueue = new ConcurrentLinkedQueue<>();
    
    /** a Map with [ReportToken|ReportHandler] with the executed report jobs */
	private ConcurrentMap<ReportToken, ReportHandler> processedJobs;

	private volatile int killStatus = 0;

	PersonManager _personManager;
    
    /**
	 * @param someName
	 *        see {@link Thread#getName()}
	 */
    public ReportQueue(String someName) {
        super(someName);
		_personManager = PersonManager.getManager();
        initialize();
        this.start();
    }
    
    /**
     * Initialize the queue and the map and set the thread to daemon status
     */
    private void initialize (){
		this.processedJobs = new ConcurrentHashMap<>();
        this.setDaemon(true);
    }
    
    /**
     * The run method waits until a notification is send, that a job ist to execute.
     * Then the executeJobs method is called.
     * @see java.lang.Runnable#run()
     */
    @Override
	public void run () {
        while (true) {
            if (!this.tokenQueue.isEmpty()) {
                this.executeJobs();
            }

			synchronized (this) {
				if (terminationRequested()) {
					break;
				}
				if (this.tokenQueue.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException ex) {
						Logger.debug("ReportQueue " + this + " was interrupted externally.", ex, ReportQueue.class);
					}
				}
            }
        }
		Logger.info("Finished reporting queue ", ReportQueue.class);
    }
    
    /**
     * To execute the jobs in the queue an iteration over the entries in the
     * tokenQueue is performed. For every token a new ReportHandler is created and then
     * the process executed by calling {@link ReportHandler#prepare()},
     * {@link ReportHandler#processReport()} and
     * {@link ReportHandler#finishReport()} subsequently. After execution the
     * processed report is then moved (together with its token) to the
     * proccedJobs map and the token is removed from the queue, then the next token is
     * processed.
     */
    protected void executeJobs() {
		ReportToken tokenToExecute = this.tokenQueue.peek();
        while (tokenToExecute != null) {
			final ReportHandler jobToExecute = ReportGenerator.getInstance().createReportHandler(tokenToExecute);
			final UserInterface requestUser = tokenToExecute.getRequestUser();
			TLContext.inSystemContext(ReportQueue.class, new InContext() {
				@Override
				public void inContext() {
					TLSubSessionContext session = TLContextManager.getSubSession();
					session.setPerson(requestUser == null ? null : Person.byName(requestUser.getUserName()));
					jobToExecute.prepare();
					jobToExecute.processReport();
					jobToExecute.finishReport();
				}
			});

            // if anything works fine until here we move the job to the processed map.
			tokenQueue.poll();
			processedJobs.put(tokenToExecute, jobToExecute);
			if (immediatelyTerminationRequested()) {
				if (!tokenQueue.isEmpty()) {
					StringBuilder msg = new StringBuilder("Missing process of following tokens: ");
					msg.append(tokenQueue);
					Logger.warn(msg.toString(), ReportQueue.class);
				}
				return;
			}
			// get the next job to process:
			tokenToExecute = tokenQueue.peek();
        }
    }
    
    /**
     * Adding a new report job to the queue is done by providing a token.
     * After adding the token to the queue the queue is notified.
     * @param aToken the token to represent the report.
     */
    public synchronized void addJob(ReportToken aToken) {
		if (terminationRequested()) {
			throw new IllegalStateException("Queue was requested to terminate.");
		}
		tokenQueue.offer(aToken);
        this.notifyAll();
    }
    
    /**
     * Get the executed reportHandler for the given token from the list of
     * processed jobs. By this method the entry is removed from the map!
     * 
     * @param aToken the ReportToken to identify the report
     * @return the executed ReportHandler or <code>null</code> if no report
     *         handler for the token is found.
     */
	public ReportHandler getExecutedJob(ReportToken aToken) {
		return processedJobs.remove(aToken);
    }
    
    /**
     * @param aToken the handle to identify a reporting job
     * @return <code>true</code> if the report is being processed or in the queue to be processed.
     */
    public boolean jobQueued (ReportToken aToken) {
		return tokenQueue.contains(aToken);
    }
    
    /**
     * @param aToken the handle to identify a reporting job
     * @return <code>true</code> if the report is already in the executed map.
     */
    public boolean jobExecuted (ReportToken aToken) {
        if (processedJobs.containsKey(aToken)) {
            return true;
        }
        return false;            
    }

	/**
	 * Notifies the queue to terminate as soon as possible.
	 */
	public synchronized void terminateImmediately() {
		if (!tokenQueue.isEmpty()) {
			Logger.warn("Queue was requested to terminate as soon as possible whereas jobs present.",
				ReportQueue.class);
		}
		killStatus = 2;
		this.notifyAll();
	}

	/**
	 * Ends all current jobs and terminates.
	 */
	public synchronized void terminate() {
		Logger.info("Queue was requested to terminate.", ReportQueue.class);
		killStatus = 1;
		this.notifyAll();
	}

	private boolean terminationRequested() {
		return killStatus > 0;
	}

	private boolean immediatelyTerminationRequested() {
		return killStatus > 1;
	}
}
