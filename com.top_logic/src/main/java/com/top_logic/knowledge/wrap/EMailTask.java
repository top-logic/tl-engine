/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.base.mail.UserMailBatch;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Generic Task to send EMails to some List of useres.
 * 
 * In addition to "normal" Task a EMailTask can be disabled.
 * 
 * nice to have: We might save an Attachment in the PhysicalResource
 * (which is not supported, yet).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class EMailTask extends TaskWrapper {

    /** Type of KnowledgeObject wrapped by this class */
    public static final String  OBJECT_NAME = "EMailTask";

    /** The Object that actually will send the mails */
    protected UserMailBatch mailBatch;

    /**
     * CTor used to support creation by WrapperFactory.
     */
    public EMailTask(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * Make superclass CTor accessible.
     */
    public static EMailTask create(
        KnowledgeBase aKB,  String name,
        int daytype     , int daymask,
			int hour, int minute) {
		EMailTask theTW =
			(EMailTask) WrapperFactory.getWrapper(createKO(aKB, OBJECT_NAME, name, daytype, daymask, hour, minute));
        theTW.setActive(true);
        return theTW;
    }

    @Override
	public long calcNextShed(long notBefore) {
        long result = 0;
		if (tValid() && isActive()) {
			result = super.calcNextShed(notBefore);
        }
        return result;   
    }
    
    /** now actually send the Mail(s) . */
    @Override
	public void run () {
        super.run();
		Computation<Void> mailBatchRun = new Computation<>() {
			@Override
			public Void run() {
				getMailBatch().run();
				return null;
			}
		};
		ThreadContext.inSystemContext(EMailTask.class, mailBatchRun);
    }
    
    /**
     * Overriden to clear the mailBatch when we go away.
     */
    @Override
	protected void handleDelete() {
		super.handleDelete();
		mailBatch = null;
    }

    /** Indicates whether EMails should be sent or not. */    
	public boolean isActive() {
		return tGetDataBooleanValue("active");
    }

    /** Set whether EMails should be sent or not. */    
	public void setActive(boolean isActive) {
		tSetData("active", Boolean.valueOf(isActive));
    }

    /** the (, seperated) list of users and grous to send the mail to. */    
	public String getUsers() {
		return tGetDataString("users");
    }

    /** Set the (, seperated) list of users and grous to send the mail to. */    
	public void setUsers(String aUserList) {
		tSetData("users", aUserList);
        mailBatch = null;   // reset for lazy re-initialisation
    }

    /** the subject of the message to be sent. */    
	public String getSubject() {
		return tGetDataString("subject");
    }

    /** Set the subject of the message to be sent. */    
	public void setSubject(String aMessage) {
		tSetData("subject", aMessage);
        mailBatch = null;   // reset for lazy re-initialisation
    }

    /** the message to be sent. */    
	public String getMesssage() {
		return tGetDataString("message");
    }

    /** Set the message to be sent. */    
	public void setMessage(String aMessage) {
		tSetData("message", aMessage);
        mailBatch = null;   // reset for lazy re-initialisation
    }

    
    /** Lazy accessor to the BatchJob doing the real work */
	protected UserMailBatch getMailBatch() {
        if (mailBatch == null) {
            mailBatch = new UserMailBatch(
                getUsers(),getSubject(), getMesssage());
        }
        return mailBatch;
        
    }

    /**
     * Get the single wrapper for the given knowledge object.
     *
     * @param    aKO    The KnowledgeObject, must not be <code>null</code>.
     * @return   The requested wrapper.
     */
	public static EMailTask getInstance(KnowledgeObject aKO) {
        return ((EMailTask) WrapperFactory.getWrapper(aKO));
    }
}
