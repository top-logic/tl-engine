/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.top_logic.base.security.UserSecurityException;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * The Event which should be used to make Changes of Documents available
 * to the ApplicationBus
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DocumentEvent extends MonitorEvent {

    /**
     * Creates a new DocumentEvent.
     *
     * @param    aSender      The sender which then sends this event.
     * @param    aDocument    The document which has been changed.
     * @param    aUser        The user who changed the Document.
     * @param    aType        The type of Change (Created,Modified or deleted).
     */
    private DocumentEvent (Sender aSender, 
                           Wrapper aDocument, 
                           UserInterface aUser,   
			String aType) {
		super(aSender, aDocument, getSourceWrapper(aDocument), aUser, aType);

		Logger.debug("DocumentEvent created. Source is <" + getSourceObject() + ">", this);
	}

    /**
	 * Constructs a prototypical BusEvent with source = Sender by calling the superclass´
	 * constructor.
	 *
	 * @param aSender
	 *        The sender (source) on which the event initially occurred.
	 * @param aDocument
	 *        The document subscribed to the service(s).
	 * @param aWebfolder
	 *        The object the act as source (Wrapper) for the document.
	 * @param aUser
	 *        The user performing this action.
	 * @param aDate
	 *        The time, this event occured.
	 * @param aType
	 *        The type of the emssage to be sent.
	 */
    public DocumentEvent (Sender aSender, Wrapper aDocument, 
                         Wrapper aWebfolder, UserInterface aUser,   
                         Date aDate,     String aType) {
		super(aSender, aDocument, aWebfolder, aUser, aDate, aType);
       // Logger.debug("DocumentEvent created.",this);
	}

    /**
     * the Document which has been changed
     */
	public Wrapper getDocument() {
		return (Wrapper) this.getMessage();
    }

    /**
     * the webfolder of the changed Document
     */
	public Wrapper getWebFolder() {
		return (Wrapper) this.getSourceObject();
    }

    /**
     * Since the constructor is private this method should be used
     * to retrieve a new DocumentEvent.
     *
     * @param    aSender      The Sender which sends this event.
     * @param    aDocument    The Document which has been changed.
     * @param    kindOfEvent  The kind of change (Created, Modified or deleted).
     * @return   A new DocumentEvent.
     *
     * @throws    UserSecurityException    When no SecurityContext is set.
     *                                     You cannot create a Document without 
     *                                     SecurityContext, since in this case 
     *                                     there would be noone who performed 
     *                                     the change. ehrm....at all no 
     *                                     changes should be possible without 
     *                                     SecurityContext.
     * @throws    Exception                If the given KnowledgeObject is 
     *                                     no Document.
     */
    public static DocumentEvent getDocumentEvent (Sender aSender,   
                                                  Wrapper aDocument, 
                                                  String kindOfEvent) 
                                                throws UserSecurityException, 
                                                       Exception {
		UserInterface aUser = Person.getUser(TLContext.currentUser());

        if (aUser == null) {
            throw new UserSecurityException ("No current user set in " +
                                             "SecurityContext. Cannot create " +
                                             "DocumentEvent without user.");
        }

		if (!Document.OBJECT_NAME.equals(aDocument.tTable().getName())) {
            throw new Exception ("Given knowledgeobject is not of " +
                                 "type Document!");
        }

        return (new DocumentEvent (aSender, aDocument, aUser, kindOfEvent));
    }

	private static Wrapper getSourceWrapper(Wrapper aDocument) {
		return WrapperFactory.getWrapper(getSourceObject(aDocument.tHandle()));
	}

	/**
	 * Returns the source object of a given Document which should be a Webfolder.
	 *
	 * @param aDocument
	 *        The Document to get the Source From.
	 * @return The Source of the Document (it'sWebfolder) or null if no Source is found.
	 */
	private static KnowledgeObject getSourceObject(KnowledgeObject aDocument) {
    	
		boolean debug = Logger.isDebugEnabled(DocumentEvent.class);
        if (aDocument == null) {
        	if (debug) {
				Logger.debug("getSourceObject(): Given document is null, returning null", Document.class);
            }

			return (null);  
        } 

		if (Document.OBJECT_NAME.equals(aDocument.tTable().getName())) {
            //getting the source of the Document :

        	if (debug) {
				StringBuilder fetchedAssociations = new StringBuilder();
				fetchedAssociations.append("Getting all incoming <");
				fetchedAssociations.append(WebFolder.CONTENTS_ASSOCIATION);
				fetchedAssociations.append("> Assoziations of Document <");
				fetchedAssociations.append(aDocument);
				fetchedAssociations.append(">");
				Logger.debug(fetchedAssociations.toString(), Document.class);
            }
			// at first all incoming WebFolder#CONTENTS_ASSOCIATION Assoziations
			Iterator<KnowledgeAssociation> theIncomingAssociations =
				aDocument.getIncomingAssociations(WebFolder.CONTENTS_ASSOCIATION);
            
			// Vector to hold the source Objects
			ArrayList<KnowledgeObject> theSourceObjects = new ArrayList<>();

            //now collect the SourceObjects
            if ((theIncomingAssociations!=null) && (theIncomingAssociations.hasNext()) ) {
   	        	if (debug) {
					Logger.debug("Assoziations found.", DocumentEvent.class);
					Logger.debug("Collecting all source objects of these assoziations...", Document.class);
   	        	}
				while (theIncomingAssociations.hasNext ()) {

					KnowledgeAssociation theAssociation = theIncomingAssociations.next();
                    try {
                        theSourceObjects.add(theAssociation.getSourceObject ());    
                    } catch (InvalidLinkException ile) {
						StringBuilder invalidLink = new StringBuilder();
						invalidLink.append("InvalidLinkException occured while getting source of Association: ");
						invalidLink.append(theAssociation);
						invalidLink.append("which is an incoming ");
						invalidLink.append(WebFolder.CONTENTS_ASSOCIATION);
						invalidLink.append(" Ass. of KO: ");
						invalidLink.append(aDocument);
						Logger.error(invalidLink.toString(),
							ile, DocumentEvent.class);
                    }
                }
            } else {
   	        	if (debug) {
					Logger.debug("No Associations found.", DocumentEvent.class);
   	        	}
			}

            KnowledgeObject theSourceKO = null;

            //if at least one source has been found
            if (!theSourceObjects.isEmpty ()) {

   	        	if (debug) {
					Logger.debug("Source Objects found: " + theSourceObjects.size(), DocumentEvent.class);
   	        	}
				
				//a Document should have only one Source
                if (theSourceObjects.size () > 1) {
					StringBuilder multipleIncomingAssoc = new StringBuilder();
					multipleIncomingAssoc.append("The Document ");
					multipleIncomingAssoc.append(aDocument);
					multipleIncomingAssoc.append(" has more than one source objects (incoming ");
					multipleIncomingAssoc.append(WebFolder.CONTENTS_ASSOCIATION);
					multipleIncomingAssoc.append("s), DocumentMonitor uses the first webfolder only");
					Logger.warn(multipleIncomingAssoc.toString(), DocumentEvent.class);
                }

				
				Logger.debug("Looking for WebFolder(s) within the Source objects", DocumentEvent.class);

                for (int i = 0; i<theSourceObjects.size ();i++) {

					theSourceKO = theSourceObjects.get(i);
					if (WebFolder.OBJECT_NAME.equals(theSourceKO.tTable().getName())) {
                        break;    
                    }else {
                        theSourceKO = null;
                    }
                }

                if (theSourceKO == null) {
                    Logger.warn ("None of the Sources of Document "+aDocument+" is "+
						"of type WebFolder.", DocumentEvent.class);
                }else {
	   	        	if (debug) {
						Logger.debug("Webfolder found.", DocumentEvent.class);
	   	        	}
				}
            } else if (debug) {
				Logger.debug("No Source objects found.", DocumentEvent.class);
			}        

        	if (debug) {
				Logger.debug("returning...", DocumentEvent.class);
            }
			
			return(theSourceKO);
        } else {
			Logger.warn("Document created with a knowledgeobject which is not of type Document", DocumentEvent.class);
            return(null);   
        }
    }

    /**
     * Method to write MonitorEvent to PrintWriter.
     * 
     * @param aWriter writer to write to
     */ 
    @Override
	public void writeEvent (PrintWriter aWriter) throws IOException {
		Object theSource  = getSourceObject();
		Object theMessage = getMessage();
        aWriter.println (getID 		(theSource));
        aWriter.println (getType 	(theSource));
        aWriter.println (getID 		(theMessage));
        aWriter.println (getType 	(theMessage));
        aWriter.println (getDate 	().getTime ());
        aWriter.println (getType 	());
        aWriter.println (getUser 	(this.getUser ()));      
    }   
}
