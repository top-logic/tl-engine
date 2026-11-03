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
    
    /**
     * Country.
     */
    public String Lnd;
    /**
     * First name line.
     */
    public String Name1;
    /**
     * Second name line.
     */
    public String Name2;
    /**
     * City.
     */
    public String Ort;
    /**
     * District.
     */
    public String Ortsteil;
    /**
     * Post office box.
     */
    public String Postfach;
    /**
     * ZIP code of the post office box.
     */
    public String PLZPostf;
    /**
     * ZIP code.
     */
    public String Postleitz;
    /**
     * Region.
     */
    public String Rg;
    /**
     * Search term.
     */
    public String SuchBegr;
    /**
     * Street.
     */
    public String Strasse;
    /**
     * Address.
     */
    public String Adresse;
    /**
     * First name line of the post office box address.
     */
    public String PlzName1;
    /**
     * Second name line of the post office box address.
     */
    public String PlzName2;
    /**
     * City of the post office box.
     */
    public String PlzOrt;
    /**
     * Industry sector.
     */
    public String Branche;
    /**
     * Telebox number.
     */
    public String Telebox;
    /**
     * First phone number.
     */
    public String Telefon1;
    /**
     * Second phone number.
     */
    public String Telefon2;
    /**
     * Fax number.
     */
    public String Telefaxnummer;
    /**
     * Teletex number.
     */
    public String Teletexnummer;
    /**
     * Telex number.
     */
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
     * Fill up given {@code sapNr} with zeros to 10 places.
     */
    public static final String getSAPNr(String sapNr) {
        return ZEROS.substring(0, ZEROS.length() - sapNr.length()) + sapNr;
    }
    
}
