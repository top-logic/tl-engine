/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.top_logic.base.security.authorisation.roles.ACL;
import com.top_logic.base.user.UserInterface;
import com.top_logic.base.user.UserService;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.PersonManager;

/** 
 * Class that will send E-mails to users/groups in a batch-job.
 * 
 * @author    hbo
 */
public class UserMailBatch extends BatchImpl {
    
    /** String that contains a comma-separated-list of user-name and role-names. */
    private String userList;
    
    /** String, that contains the subject of the sent mail. */
    private String subject;
    
    /** String that contains the mail content. */
    private String body;
    
    /** List of all users. */
    private List allUsers;
    
    private List attachments;
    
    /**
     * Constructor of this class.
     * @param aRepicientList    a comma-separated-list
     *                          of user-names and group-names of the mail-recipients.
     * @param aSubject          the subject of the mail
     * @param aBody             the content of the mail
     */
    public UserMailBatch(String aRepicientList, String aSubject, String aBody){
        super("UserMailBatch:" + aSubject);
        this.userList   = aRepicientList;
        this.subject    = aSubject;
        this.body       = aBody;
    }
    
    /**
     * @param aRecipientList    a comma-separated-list
     *                          of user-names and group-names of the mail-recipients.
     * @param aSubject          the subject of the mail
     * @param aBody             the content of the mail
     * @param anAttachments     a list of @link com.top_logic.knowledge.wrap.Wrapper.
     *                          that wrapps the attachments of the mail.
     */
    public UserMailBatch(String aRecipientList, String aSubject, String aBody, List anAttachments){
        this(aRecipientList, aSubject, aBody);
        this.attachments = anAttachments;
    }
    
    /**
     * Send the EMail when invoked by the Scheduler.
     * 
     * Create a new Mail-instance. Set the mail-recipients, 
     * the subject and the content of mail and invoke the sending.
     */
    @Override
	public void run(){
           if (MailSenderService.isConfigured()) {
               Collection   theRecipients   = this.createRecipientSet();
               MailHelper.getInstance().sendSystemMail(new ArrayList(theRecipients), null, null, this.subject, this.body, this.attachments, MailHelper.CONTENT_TYPE_TEXT);
           }
      }

     /*
      * Create and return a collection of user- and group-names.
      * These names are extracted from the instance-variable userList.
      * @return Collection java.lang.String
      */
    private Collection createNameSet() {
        StringTokenizer theTokenizer = new StringTokenizer(this.userList, ",");
        Collection theNames = new HashSet();
        while (theTokenizer.hasMoreTokens()){
            String theName = theTokenizer.nextToken();
            theNames.add(theName);
        }
        return theNames;
    }
    
    /**
     * Create and return a collection of UserInterfaces.
     * 
     * This collection contains all UserInterfaces of users, that names
     * occurs in the instance-variable userList, and of all users that belongs 
     * to a role that name occurs in the instance-variable userList.
     * 
     * @return a Collection of UserInterface
     */
    protected Collection<UserInterface> createRecipientSet(){
        Collection                  theNames       = this.createNameSet();
        Collection<UserInterface>  theRecipients   = new HashSet<>(theNames.size());
        Iterator                    theNameIter    = theNames.iterator();
        try {
            ThreadContext.pushSuperUser();

            while (theNameIter.hasNext()){
                String        theName = (String) theNameIter.next();
                UserInterface theUser = UserService.getUser(theName);
                if (theUser != null){
                    theRecipients.add(theUser); 
                }
                else{
                    this.checkForRoleRecipients(theRecipients, theName);
                }
            }
            return theRecipients;
        } finally {
        	ThreadContext.popSuperUser();
        }
        
    }
    
    /**
     * Add recipients (UserInterface) to the collection anRecipients.
     * 
     * Every UserInterface that is added to the collection belongs to 
     * the role with the given role-name.
     * 
     * @param anRecipients a collection of UserInterfaces of users the mail 
     *        is sent to.
     * @param aRoleName the name of the role that is checked for users
     *        belong to.
     */
    private void checkForRoleRecipients(Collection anRecipients, String aRoleName){
        List theAllUsers = this.getAllUsers();
        int theSize = theAllUsers.size();
        for (int i = 0; i < theSize; i++){
            UserInterface theUser = (UserInterface)theAllUsers.get(i);
            ACL theUserRoles = theUser.getACLRoles();
            if (theUserRoles.hasAccess(aRoleName)) {
                anRecipients.add(theUser);                       
            }
        }    
    }
    /**
     * Lazy initialization of the instance-variable allUsers.
     */
    private List getAllUsers(){
        if (this.allUsers == null){
            this.allUsers = PersonManager.getManager().getAllAliveUsers();
        }
        return this.allUsers;
    }
}
