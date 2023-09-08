/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Date;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class DefaultExport implements Export {

    private final String handlerID;
    private final Wrapper model;
    private final String technology;
    private final String duration;
    private final Person person;
    
    private Document document;
    private Date timeRunning;
    private Date timeFinished;
    private Date timeQueued;
    private Date timeFailed;

	private ResKey failureKey;

	private ResKey displaynameKey;
    private String fileExtension;
    private State state = State.INITIALIZED;

    public DefaultExport(String aHandlerID, Wrapper aModel, Person aPerson) {
        this.handlerID = aHandlerID;
        this.model     = aModel;
        ExportHandler  theHandler = ExportHandlerRegistry.getInstance().getHandler(aHandlerID);
        this.technology = theHandler.getExportTechnology();
        this.duration   = theHandler.getExportDuration();
        this.person     = aPerson;
    }
    
    private Tuple createTuple() {
        Object[] objects = new Object[13];
        objects[0] = this.handlerID;
        objects[1] = this.model;
        objects[2] = this.state;
        objects[3] = this.technology;
        objects[4] = this.duration;
        objects[5] = this.document;
        objects[6] = this.timeRunning;
        objects[7] = this.timeFinished;
        objects[8] = this.timeQueued;
        objects[9] = this.timeFailed;
		objects[10] = ResKey.encode(this.failureKey);
		objects[11] = ResKey.encode(this.displaynameKey);
        objects[12] = this.fileExtension;
        
        return TupleFactory.newTuple(objects);
    }
    
    @Override
    public int hashCode() {
        return TupleFactory.hashCode(this.createTuple());
    }
    
    @Override
    public boolean equals(Object aObj) {
		if (aObj == this) {
			return true;
		}
        return (aObj instanceof DefaultExport) && TupleFactory.equalsTuple(this.createTuple(), ((DefaultExport) aObj).createTuple());
    }
    
    @Override
	public ResKey getDisplayNameKey() {
        return this.displaynameKey;
    }

    @Override
	public Document getDocument() {
        return this.document;
    }

    @Override
	public String getExportDuration() {
        return this.duration;
    }

    @Override
	public String getExportHandlerID() {
        return this.handlerID;
    }

    @Override
	public String getExportTechnology() {
        return this.technology;
    }

    @Override
	public ResKey getFailureKey() {
        return this.failureKey;
    }

    @Override
	public String getFileExtension() {
        return this.fileExtension;
    }

    @Override
	public Object getModel() {
        return this.model;
    }

    @Override
	public State getState() {
        return this.state;
    }

    @Override
	public Date getTimeFailed() {
        return this.timeFailed;
    }

    @Override
	public Date getTimeFinished() {
        return this.timeFinished;
    }

    @Override
	public Date getTimeQueued() {
        return this.timeQueued;
    }

    @Override
	public Date getTimeRunning() {
        return this.timeRunning;
    }

    @Override
	public Person getPerson() {
    	return this.person;
    }
    
    @Override
	public boolean setStateFailed(ResKey aFailureKey) {
        
        if (State.FAILED == this.state) {
            return false;
        }
        
        this.state = State.FAILED;
        this.failureKey = aFailureKey;
        this.timeFailed = new Date();
        return true;
    }

    @Override
	public boolean setStateFinished(Document aDocument, ResKey aDisplayNameKey, String aFileExtension) {
        
        if (State.FINISHED == this.state) {
            return false;
        }
        
        this.state = State.FINISHED;
        this.document = aDocument;
        this.displaynameKey = aDisplayNameKey;
        this.fileExtension  = aFileExtension;
        this.timeFinished   = new Date();
        return true;
    }

    @Override
	public boolean setStateQueued() {
        
        if (State.QUEUED == this.state || State.RUNNING == this.state) {
            return false;
        }
        
        this.state = State.QUEUED;
        this.timeQueued = new Date();
        this.timeFailed = null;
        this.timeFinished = null;
        this.timeRunning  = null;
        this.failureKey   = null;
        return true;
    }

    @Override
	public boolean setStateRunning() {
        
        if (State.RUNNING == this.state) {
            return false;
        }
        
        this.state = State.RUNNING;
        this.timeRunning = new Date();
        return true;
    }

}

