/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

/**
 * Simple Value holder for SAP-Records for the {@link com.top_logic.contact.mandatoraware.imp.SAP_CSVSupplierImporter}.
 * 
 * This is a Customer specific class and needs modifications for generic 
 * (if possible) usage. 
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPSupplierRecord {

    /** 10 zeros to fill up SAP-Nr */
    static final String ZEROS = "0000000000";
    
    /** Kreditor in original record, but actually is SAP-Nr */
    public Integer sapNr;
    
    public String Lnd;
    public String Name1;
    public String Name2;
    public String Ort;
    public String Ortsteil;
    public String Postfach;
    public String PLZPostf;
    public String Postleitz;
    public String Rg;
    public String SuchBegr;
    public String Strasse;
    public String Adresse;
    public String PlzName1;
    public String PlzName2;
    public String PlzOrt;
    public String Branche;
    public String Telebox;
    public String Telefon1;
    public String Telefon2;
    public String Telefaxnummer;
    public String Teletexnummer;
    public String Telexnummer;

    /** 
     * TODO kha Create a new SAPSupplierRecord ...
     */
    public SAPSupplierRecord() {
        super();
    }
    
    /**
     * Return SAP-Nr as zero filled 10 digit String.
     */
    public String getSAPNr() {
        return getSAPNr(sapNr.toString());
    }

    /**
     * Fill up given sapNr with zeros to 10 places.
     */
    public static final String getSAPNr(String sapNr) {
        return ZEROS.substring(0, ZEROS.length() - sapNr.length()) + sapNr;
    }
    
}
