/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.util;

/**
 * Converts (long) Numbers in German Words.
 * 
 * The highest double number to convert is 9.007199254740989E15.
 * Higher double Numbers will result an IllegalArgumentException.
 * the Range of long is fully supported. 
 *
 * @author    <a href="mailto:wba@top-logic.com">Wolfgang Badur</a>
 */
public class NumbersToGermanWords {
         
    /** Singular german numbers */
    private static final String[] NULL_BIS_ZWOELF = 
        {"Null"    , "Eins", "Zwei"    , "Drei", 
         "Vier"    , "Fünf", "Sechs"   , "Sieben", 
         "Acht"    , "Neun", "Zehn"    , "Elf"   , "Zwölf" };
         
    /** (Start of) Plural german numbers (first variant) */
    private static final String[] EINUNDZWANZIG_BIS = 
        {"Null",    "Ein",      "Zwei",   "Drei",   "Vier", "Fünf", 
         "Sechs",   "Sieben",   "Acht",   "Neun",   "Zehn", "Elf", "Zwölf"};
         
    /** (Start of) Plural german numbers (second variant) */
    private static final String[] DREIZEHN_BIS_NEUNZEHN = 
        {"Null",    "Eins",     "Zwei",   "Drei",   "Vier", "Fünf", 
         "Sech",    "Sieb",     "Acht",   "Neun",   "Zehn"};
    
    /** Single Millions,Billions etc. */     
    private static final String[] MEGAPOTENZEN_1 = { 
                                                "Eine Million ", 
                                                "Eine Milliarde ", 
                                                "Eine Billion ",
                                                "Eine Billiarde ",
                                                "Eine Trillion ",
                                                "Eine Trilliarde "
                                                };
    /** Plural Millions,Billions etc. */     
    private static final String[] MEGAPOTENZEN_PLURAL = { 
                                                " Millionen ", 
                                                " Milliarden ", 
                                                " Billionen ",
                                                " Billiarden ",
                                                " Trillionen ",
                                                " Trilliarden "};
    
    /** Largest double number we can possibly convert 
     *  
     *  (double) (Long.MAX_VALUE >> 10) - 1 
     */
    public static final double MAX_DOUBLE = 9.007199254740989E15;

	/**
	 * Wrapper for the conversion of long to String.
	 * 
	 * Checks +/-. Standard for Long Integer. #author wba
	 * 
	 * @param aLong
	 *        the input number
	 * @return output string
	 */
	public static String convert(long aLong) {
                                   
        if (aLong < 0) {
            return "Minus " + numToString(-aLong).trim();
        }
        else {
            return numToString(aLong).trim();
        }
    }

	/**
	 * Wrapper for the conversion of double to String Max is 9.007199254740989E15 (smaller than the
	 * Max. for long). Checks +/-. #author wba
	 * 
	 * @param aDouble
	 *        the input number
	 * @return output as string
	 * @throws IllegalArgumentException
	 *         when aDouble is no valid Number, e.g {@link Double#NaN}
	 */
    public static String convert(double aDouble) 
                                              throws IllegalArgumentException{
        if ((aDouble < 0) && (aDouble >= - MAX_DOUBLE)) {
			return "Minus " + numToString((long) Math.abs(aDouble)).trim();
        }
        else if ((aDouble >= 0) && (aDouble <= MAX_DOUBLE)) {
            return numToString((long) aDouble).trim();   
        }
        else {
            throw new IllegalArgumentException(
                "Cannot convert " + aDouble + " is to large");
        }
    }
                                             
    /** converts long to String.
     * #author wba
     *
     * @param   zahl a positive Number  
     * @return  Number in German Words        
     */
    private static String numToString(long zahl)  {        
		assert zahl >= 0 : "Unexpected negative " + zahl;
        if (zahl < 100) {
            int iZahl = (int) zahl;
            if (zahl <= 12) {
                return NULL_BIS_ZWOELF[iZahl];
            }           
            else if (zahl <= 19){
                return DREIZEHN_BIS_NEUNZEHN[iZahl - 10] + "zehn";
            }            
            else if (zahl == 20) {
                return "Zwanzig";
            }           
            else if (zahl <= 29){
                return EINUNDZWANZIG_BIS[iZahl - 20] + "undzwanzig";
            }            
            else if (zahl == 30) {
                return "Dreißig";
            }
            
            else if (zahl <= 39){
                return EINUNDZWANZIG_BIS[iZahl - 30] + "unddreißig";
            }
            
            else  { //  40 - 99 
                int ersteStelle = iZahl % 10;    
                int zehner      = (iZahl - ersteStelle)/10;
                String teil3 = DREIZEHN_BIS_NEUNZEHN[zehner];
                String wort;
                if (ersteStelle == 0) {
                    wort = teil3 + "zig";
                }
                else {
                    wort = EINUNDZWANZIG_BIS[ersteStelle] + "und" 
                            + teil3.toLowerCase() + "zig";
                }
                return wort;
            }
        } else { // zahl >= 100)
            return overOnehundred(zahl);
        }
    }
    
    /** Converts long to String for Numbers >= 1000.
     * 
     * #author wba
     *
     * @param   aZahl a positive Number (should be  >= 1000)
     * @return  Number in German Words        
     */
    private static String overOnehundred(long aZahl) {
        int iZahl = (int) aZahl;
        
        if (aZahl <= 999) {   
            int zehnerrest = iZahl % 100;
            String teil2;
            if (zehnerrest == 0) {
                teil2 = ""; 
            }
            else {
                teil2 = numToString(zehnerrest).toLowerCase(); 
                // e.g 20 EintausendZwanzig -> Eintausendzwanzig
            }
            iZahl = (iZahl - zehnerrest)/100;
            return EINUNDZWANZIG_BIS[iZahl] + "hundert" + teil2;
        }
        else if (aZahl <= 999999) {
            int hunderterrest = iZahl % 1000;
            int tausender = (iZahl - hunderterrest)/1000;
            return kiloAndMore(tausender) + "tausend" 
                                + hunderterrest(hunderterrest);
        }
        //  Bereich bis zur größten darstellbaren Long - Zahl 
        else { // (aZahl >= 1000000L ) 
            return overOneMillion(aZahl);                    
        }
    }
         
    /** converts long to String for Numbers >= 1000000
     * #author wba
     *
     * @param   aZahl a positive Number (should be  >= 1000000)
     * @return  Number in German Words        
     */
    private static String overOneMillion(long aZahl)  {
        String wort = "???"; // Compiler is not clever enough to know 
        long bigNum1 = 1000L;
        long bigNum2 = 1;           
        for (int j=0; j<=3; j++) {
            bigNum1 *= 1000L; // j=0 Million, j=1 Milliarde, j=2 Billion...  
            bigNum2 = (bigNum1 * 1000L -1);
            if ((aZahl >= bigNum1) && (aZahl <= bigNum2) ) {
                long rest = aZahl % bigNum1;
                long big = (aZahl - rest) / bigNum1;
                if (rest == 0) {
                    wort = megaAndMore(big, j);
                }
                else {
                    wort = megaAndMore(big, j) + numToString(rest);
                }
            }
        }
        if (aZahl > bigNum2) {
            bigNum1 *= 1000L; //   Trilliarde           
            long rest = aZahl % bigNum1;
            long big = (aZahl - rest) / bigNum1;
            if (rest == 0) {
                wort =  megaAndMore(big, 4);
            }
            else {
                wort =  megaAndMore(big, 4) + numToString(rest);
            }
        }
        return wort;
    
    } 
     
    /**
     * Cares about plural forms of meagnumbers.
     *
     * @param aNumber a positive Number  
     * @param megaPot   log1000 of ... e.g. 2 -&gt; 1000000
     */
    private static String megaAndMore (long aNumber, int megaPot)  {        
        if (aNumber == 1) {
            return MEGAPOTENZEN_1[megaPot];
        }
        else {  // 1230000000  ->
            // "Einhundertdreinundzwandzig" + " Millionen" 
            return numToString(aNumber) + MEGAPOTENZEN_PLURAL[megaPot];
        }
    }
    
    /** gives substring for the 4th to the 6th digit. 
     * 
     * e.g. 1000 to 999000
     * 
     * #author wba
     *
     * @param   aTausender  a positive Number   
     * @return  Substring        
     */
    private static String kiloAndMore (int aTausender)  {
        String tausenderWort;
        tausenderWort = numToString(aTausender);
        int endIndex = tausenderWort.length();
        if (tausenderWort.endsWith("ins") ) { // Einstausendeins -> Eintausendein
            return tausenderWort.substring(0, endIndex - 1);
        }
        else {
            return tausenderWort;            
        }
    }
    
    /** Gives substring for the 1st to the 3rd digit. 
     * 
     * e.g. 0 to 999
     * #author wba
     *
     * @param   rest  a positive Number   
     * @return  Substring        
     */
    private static String hunderterrest (int rest) {
        if (rest == 0) {
            return "";
        }
        else {
            return numToString(rest).toLowerCase();
        }    
    }

}
