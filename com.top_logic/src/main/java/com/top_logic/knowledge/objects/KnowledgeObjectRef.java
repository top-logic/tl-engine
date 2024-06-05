/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * A Proxy/Reference to a KnowledgeObject that is Serializable.
 * 
 * Save and restore will only work when used in the same security
 * context. null KOs will be handled correctly. Deleted KOs will
 * be restored as null. This class is immutable.
 *
 * @since TopLogic 4.0
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class KnowledgeObjectRef implements Serializable {
    
    /** Unique ID of the KnowledgeObject (e.g. "WebFolder"). */
	TLID koId;

    /** Type of the KnowledgeObject (e.g. "WebFolder").     */
    String koType;

    /** Name of the KnowledgeBase, null for the default KB. */
    String kbName;
    
    /** The KO actually held */
	transient KnowledgeItem ko;
    
    /** 
     * Create a new Reference by using a given KnowledgeObject 
     */
	public KnowledgeObjectRef(KnowledgeItem ko) {
        this.ko = ko;
        if (ko != null) {
            this.kbName = ko.getKnowledgeBase().getName();
			this.koId = ko.getObjectName();
            this.koType = ko.tTable().getName();
        }
    } 
    
    /** 
     * Create a new Reference to a KO in a given KnowledgeBase.
     * 
     * @param kb     The Knowledgebase to fetch the KO from
     * @param type   Type of the referenced KO
     * @param id     Unique id of the referenced KO in the KB
     */
	public KnowledgeObjectRef(KnowledgeBase kb, String type, TLID id) {
        this.kbName = kb.getName();
        this.koId   = id;
        this.koType = type;
    } 

    /** 
     * Create a new Reference to a KO in a given KnowledgeBase.
     * 
     * @param kbName name of Knowledgebase to fetch the KO from
     * @param type   Type of the referenced KO
     * @param id     Unique id of the referenced KO in the KB
     */
	public KnowledgeObjectRef(String kbName, String type, TLID id) {
        this.kbName = kbName;
        this.koId   = id;
        this.koType = type;
    } 

    /**
     * Try to fetch the refencenced KO, if possible.
     * 
     * @return null if resolveing of KO failed because ko is no in knowledge base.
     * @throws  InvalidLinkException in case the knowledge base is not present. 
     */
	public KnowledgeItem getKnowledgeObject() throws InvalidLinkException {
        if (ko == null) {      
            KnowledgeBase kb; 
            if (kbName == null)
				kb = PersistencyLayer.getKnowledgeBase();
            else
				kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(kbName);
            
            if (koId == null) {
            	return null;
            }
            
            if (kb != null)
                ko = kb.getKnowledgeObject(koType, koId);
            else {
                String info = "Invalid KBaseName '" + kbName + "'";
                Logger.info(info, this);
                throw new InvalidLinkException(info);
            }
        }   
        return ko;
    }

    /** 
     * Return the name of the KB for this Object, null indicates default KB.
     **/
    public String getKBName() {
        return kbName;   
    }
    
    /** 
     * Return the id of the Object.
     **/
	public TLID getKOId() {
        return koId;   
    }
    
    /** 
     * Return the typeof the Object.
     **/
    public String getKOType() {
        return koType;   
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
        if (obj instanceof KnowledgeObjectRef) {
            KnowledgeObjectRef other = (KnowledgeObjectRef) obj;
            other.fetchStrings();
            this .fetchStrings();

            if (this.koId == null)
                return other.koId == null;
                
            if (kbName != null)
                return this.koId  .equals(other.koId)
                    && this.koType.equals(other.koType)
                    && this.kbName.equals(other.kbName);
            else
                return this.koId  .equals(other.koId)
                    && this.koType.equals(other.koType);
            
        }
        return false;   // dunno how to compare this
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        fetchStrings();
        return koId != null ? koId.hashCode() : 0;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        fetchStrings();
        
        if (kbName == null)
            return "KnowledgeObjectRef to " + koType + ':' + koId;
        else            
            return "KnowledgeObjectRef to " + koType + ':' + koId 
                 + " in " + kbName;
    }
    

    // "Implementation" of Serializable
    
    /** Write out the KO using KBName, KOType and Id */
    private void writeObject(ObjectOutputStream out) {
        
        fetchStrings();

        try {
			out.writeObject(IdentifierUtil.toExternalForm(koId));
            if (koId != null) {
                out.writeUTF(koType);
                out.writeUTF(kbName);
            }
        }
        catch (IOException iox) {
            Logger.error("writeObject() failed, writeObject()", iox, this);
        }
    }
    
    /** Read the KBName, KOType and Id, Ko may be restored later. */
    private void readObject(ObjectInputStream in) {

        try {
			koId = IdentifierUtil.fromExternalForm((String) in.readObject());
            if (koId != null) {
                koType = in.readUTF();
                kbName = in.readUTF();
                // ko will be resolved on Demand
            }        
        }
        catch (IOException iox) {
            Logger.error("readObject() failed", iox, this);
        }
        catch (ClassNotFoundException cnfx) { // Would be String ....
            Logger.error("readObject() failed", cnfx, this);
        }
    }

    /** extract the descriptive Strings from the KO (when needed) */
    protected final void fetchStrings() {
        if (ko != null && koId == null) {
			if (ko.isAlive()) // deleted KO ?
            {
				KnowledgeBase kb = ko.getKnowledgeBase();
				koId = ko.getObjectName();
                koType = ko.tTable().getName();
                kbName = kb.getName();
            }
        }
    }

}
