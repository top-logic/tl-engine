/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.connect;

import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.URLName;
import jakarta.mail.event.ConnectionListener;
import jakarta.mail.event.FolderListener;
import jakarta.mail.event.MessageChangedListener;
import jakarta.mail.event.MessageCountListener;
import jakarta.mail.search.SearchTerm;

/**
 * Debugging class for simulating a broken communication to the mail server ({@link TLStore#checkConnect(String)}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TLFolder extends Folder {

    private Folder folder;

    /** 
     * Creates a {@link TLFolder}.
     */
    protected TLFolder(TLStore aStore, Folder aFolder) {
        super(aStore);

        this.folder = aFolder;
    }

    @Override
	public void copyMessages(Message amessage[], Folder aFolder) throws MessagingException {
        TLStore.checkConnect("Folder.copyMessages(Message[],Folder)");

        this.folder.copyMessages(amessage, aFolder);
    }

    @Override
	public void fetch(Message amessage[], FetchProfile fetchprofile) throws MessagingException {
        TLStore.checkConnect("Folder.fetch(Message[],FetchProfile)");

        this.folder.fetch(amessage, fetchprofile);
    }

    @Override
	public synchronized Message[] getMessages() throws MessagingException {
        TLStore.checkConnect("Folder.getMessages()");

        return (this.folder.getMessages());
    }

    @Override
	public synchronized Message[] getMessages(int i, int j) throws MessagingException {
        TLStore.checkConnect("Folder.getMessages(int,int)");

        return (this.folder.getMessages(i, j));
    }

    @Override
	public synchronized Message[] getMessages(int ai[]) throws MessagingException {
        TLStore.checkConnect("Folder.getMessages(int[])");

        return (this.folder.getMessages(ai));
    }

    @Override
	public int getMode() {
        return this.folder.getMode();
    }

    @Override
	public synchronized int getNewMessageCount() throws MessagingException {
        TLStore.checkConnect("Folder.getNewMessageCount()");

        return this.folder.getNewMessageCount();
    }

    @Override
	public synchronized int getUnreadMessageCount() throws MessagingException {
        TLStore.checkConnect("Folder.getUnreadMessageCount()");

        return (this.folder.getUnreadMessageCount());
    }

    @Override
	public URLName getURLName() throws MessagingException {
        TLStore.checkConnect("Folder.getURLName()");

        return this.folder.getURLName();
    }

    @Override
	public boolean isSubscribed() {
        return this.folder.isSubscribed();
    }

    @Override
	public void setSubscribed(boolean flag) throws MessagingException {
        TLStore.checkConnect("Folder.setSubscribed(flag)");

        this.folder.setSubscribed(flag);
    }

    @Override
	public Folder[] list() throws MessagingException {
        TLStore.checkConnect("Folder.list()");

        return TLStore.getWrappedFolders(this.folder.list());
    }

    @Override
	public Folder[] listSubscribed(String s) throws MessagingException {
        TLStore.checkConnect("Folder.listSubscribed(String)");

        return TLStore.getWrappedFolders(this.folder.listSubscribed(s));
    }

    @Override
	public Folder[] listSubscribed() throws MessagingException {
        TLStore.checkConnect("Folder.listSubscribed()");

        return TLStore.getWrappedFolders(this.folder.listSubscribed());
    }

    @Override
	public Message[] search(SearchTerm searchterm) throws MessagingException {
        TLStore.checkConnect("Folder.search(SearchTerm)");

        return this.folder.search(searchterm);
    }

    @Override
	public Message[] search(SearchTerm searchterm, Message amessage[]) throws MessagingException {
        TLStore.checkConnect("Folder.search(SearchTerm,Message[])");

        return this.folder.search(searchterm, amessage);
    }

    @Override
	public synchronized void addConnectionListener(ConnectionListener connectionlistener) {
        this.folder.addConnectionListener(connectionlistener);
    }

    @Override
	public synchronized void removeConnectionListener(ConnectionListener connectionlistener) {
        this.folder.removeConnectionListener(connectionlistener);
    }

    @Override
	public synchronized void addFolderListener(FolderListener folderlistener) {
        this.folder.addFolderListener(folderlistener);
    }

    @Override
	public synchronized void removeFolderListener(FolderListener folderlistener) {
        this.folder.removeFolderListener(folderlistener);
    }

    @Override
	public synchronized void addMessageCountListener(MessageCountListener messagecountlistener) {
        this.folder.addMessageCountListener(messagecountlistener);
    }

    @Override
	public synchronized void removeMessageCountListener(MessageCountListener messagecountlistener) {
        this.folder.removeMessageCountListener(messagecountlistener);
    }

    @Override
	public synchronized void addMessageChangedListener(MessageChangedListener messagechangedlistener) {
        this.folder.addMessageChangedListener(messagechangedlistener);
    }

    @Override
	public synchronized void removeMessageChangedListener(MessageChangedListener messagechangedlistener) {
        this.folder.removeMessageChangedListener(messagechangedlistener);
    }

    @Override
	public synchronized void setFlags(Message amessage[], Flags flags, boolean flag) throws MessagingException {
        TLStore.checkConnect("Folder.setFlags(Message[],Flags,boolean)");

        this.folder.setFlags(amessage, flags, flag);
    }

    @Override
	public synchronized void setFlags(int i, int j, Flags flags, boolean flag) throws MessagingException {
        TLStore.checkConnect("Folder.setFlags(int,int,Flags,boolean)");

        this.folder.setFlags(i, j, flags, flag);
    }

    @Override
	public synchronized void setFlags(int ai[], Flags flags, boolean flag) throws MessagingException {
        TLStore.checkConnect("Folder.setFlags(int[],Flags,boolean)");

        this.folder.setFlags(ai, flags, flag);
    }

    @Override
	public void appendMessages(Message[] arg0) throws MessagingException {
        TLStore.checkConnect("Folder.appendMessages(Message[])");

        this.folder.appendMessages(arg0);
    }

    @Override
	public void close(boolean arg0) throws MessagingException {
        TLStore.checkConnect("Folder.close(boolean)");

        this.folder.close(arg0);
    }

    @Override
	public boolean create(int arg0) throws MessagingException {
        TLStore.checkConnect("Folder.create(int)");

        return this.folder.create(arg0);
    }

    @Override
	public boolean delete(boolean arg0) throws MessagingException {
        TLStore.checkConnect("Folder.delete(boolean)");

        return this.folder.delete(arg0);
    }

    @Override
	public boolean exists() throws MessagingException {
        TLStore.checkConnect("Folder.exists()");

        return this.folder.exists();
    }

    @Override
	public Message[] expunge() throws MessagingException {
        TLStore.checkConnect("Folder.expunge()");

        return this.folder.expunge();
    }

    @Override
	public Folder getFolder(String arg0) throws MessagingException {
        TLStore.checkConnect("Folder.getFolder(String)");

        return TLStore.getWrappedFolder(this.folder.getFolder(arg0));
    }

    @Override
	public String getFullName() {
        return this.folder.getFullName();
    }

    @Override
	public Message getMessage(int arg0) throws MessagingException {
        TLStore.checkConnect("Folder.getMessage(int)");

        return this.folder.getMessage(arg0);
    }

    @Override
	public int getMessageCount() throws MessagingException {
        TLStore.checkConnect("Folder.getMessageCount()");

        return this.folder.getMessageCount();
    }

    @Override
	public String getName() {
        return this.folder.getName();
    }

    @Override
	public Folder getParent() throws MessagingException {
        TLStore.checkConnect("Folder.getParent()");

        return TLStore.getWrappedFolder(this.folder.getParent());
    }

    @Override
	public Flags getPermanentFlags() {
        return this.folder.getPermanentFlags();
    }

    @Override
	public char getSeparator() throws MessagingException {
        TLStore.checkConnect("Folder.getSeparator()");

        return this.folder.getSeparator();
    }

    @Override
	public int getType() throws MessagingException {
        TLStore.checkConnect("Folder.getType()");

        return this.folder.getType();
    }

    @Override
	public boolean hasNewMessages() throws MessagingException {
        TLStore.checkConnect("Folder.hasNewMessages()");

        return this.folder.hasNewMessages();
    }

    @Override
	public boolean isOpen() {
        return this.folder.isOpen();
    }

    @Override
	public Folder[] list(String arg0) throws MessagingException {
        TLStore.checkConnect("Folder.list(String)");

        return TLStore.getWrappedFolders(this.folder.list(arg0));
    }

    @Override
	public void open(int arg0) throws MessagingException {
        TLStore.checkConnect("Folder.open(int)");

        this.folder.open(arg0);
    }

    @Override
	public boolean renameTo(Folder arg0) throws MessagingException {
        TLStore.checkConnect("Folder.renameTo(Folder)");

        return this.folder.renameTo(arg0);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof TLFolder) {
            return super.equals(obj);
        }
        else {
            return (this.folder.equals(obj));
        }
    }

    @Override
	public int hashCode() {
        return this.folder.hashCode();
    }

}
