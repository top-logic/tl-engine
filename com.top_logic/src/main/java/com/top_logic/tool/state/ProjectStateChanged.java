/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import java.util.EventObject;

/**
 * TODO KHA/TGI reimplement when EventHandling is in place.
 * 
 * @author    <a href="mailto:tgi@top-logic.com></a>
 */
public class ProjectStateChanged extends EventObject {

	public static final int PROJECT_STATE_EVENT         = 0x0020000;
	public static final int PROJECT_STATE_CHANGED_EVENT = 0x0020001;
   
    /** 
     * Create a new ProjectStateChanged ...
     * 
     * @param aSource TODO KHA use Project/StructuredElement ?
     */
    public ProjectStateChanged(Object aSource) {
        super(aSource);
    }

    /*
	public static void sendStateChangedEvent(State oldState, State newState, Wrapper project) {
		if (project == null) {
			return;
		}
		String aMessage = "pos.project.state.lasstatenull";
		if (newState != null) {
			aMessage =newState.getKey();
		}
		if(oldState !=null){
			aMessage=aMessage+".from."+oldState.getKey();	
		}

		try {
			SPElement spSource = project;
			Logger.info(
				"sendPOSEvent(" + project + "," + aMessage + "," + PROJECT_STATE_CHANGED_EVENT + ")",
				POSUtil.class);
			Sender theSender = getPOSEventSender();
			POSEvent theEvent = new ProjectStateChanged(theSender, spSource, spSource, aMessage);
			theSender.send(theEvent);
			//Logger.info("theSender.send(theEvent)  sender:"	+ theSender	+ " event" + theEvent, POSUtil.class);
		} catch (Exception e) {
			Logger.error(
				"Problem sending POSEvent for "
					+ "Trigger: <"
					+ newState
					+ "> Source(ParentObj): <"
					+ project
					+ "> Type: <"
					+ PROJECT_STATE_CHANGED_EVENT
					+ "> Message: <"
					+ aMessage
					+ ">",
				POSUtil.class);
		}

	}

	private static Sender getPOSEventSender() {
		if (sender == null) {
			synchronized (ProjectStateChanged.class) {
				if (sender == null) {
					sender = new Sender(POSReceiver.DEFAULT_NAMESPACE, POSReceiver.DEFAULT_NAME);
				}
			}
		}
		return sender;
	}

	public ProjectStateChanged(Sender aSender, Wrapper aTrigger, POSBase aSource, String aMessage) {
		super(aSender, aTrigger, aSource, PROJECT_STATE_CHANGED_EVENT, aMessage);
	}

    
    */

    /**
     * TODO KHA always false for now
     */
    public static boolean isStateChange(EventObject anEvent) {
        return false;
	}
}
