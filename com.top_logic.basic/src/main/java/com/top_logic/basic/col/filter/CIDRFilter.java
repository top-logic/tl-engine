/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.text.ParseException;
import java.util.StringTokenizer;

import com.top_logic.basic.col.Filter;

/**
 * IP filter that can be specified by a CIDR string.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CIDRFilter implements Filter<String> {

    /** The pattern to be used, never <code>null</code>. */
    private byte[] pattern;

    /** ??? */
    private int maskBitCount;

    /** ??? */
    private byte[] mask;

	/** 
	 * Create a new instance of this class.
	 * 
	 * @param    aPattern         The pattern to be used, must not be <code>null</code>.
	 */
	private CIDRFilter(byte[] aPattern, int aMaskBitCount) {
		this.pattern      = aPattern;
		this.maskBitCount = aMaskBitCount;
		this.mask         = new byte[aPattern.length];

        for (int bitsLeft = aMaskBitCount, cnt = aPattern.length, n = 0; n < cnt; n++) {
			byte maskBits;
			if (bitsLeft < 8) {
				maskBits = ((byte) (~(0xFF >>> bitsLeft)));
			} else {
				maskBits = (byte) 0xFF;
			}
			bitsLeft = Math.max(0, bitsLeft - 8);
			
			this.mask[n] = maskBits;
			this.pattern[n] &= maskBits;
		}
	}

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        StringBuffer theResult = new StringBuffer(this.getClass().getName()).append(" [");

        for (int theCnt = this.pattern.length, thePos = 0; thePos < theCnt; thePos++) {
            if (thePos > 0) {
                theResult.append('.');
            }
            theResult.append(pattern[thePos] & 0xFF);
        }

        if (this.maskBitCount < 32) {
            theResult.append('/');
            theResult.append(this.maskBitCount);
        }
        
        return theResult.append(']').toString();
    }

    /** 
     * Test, if the given parameter matches pattern of this instance.
     * 
     * @param    anObject    The IP address to be checked.
     * @return   <code>true</code>, if pattern matching succeeds.
     */
    @Override
	public boolean accept(String anObject) {
    	return (this.matches(CIDRFilter.getByteArray(anObject)));
    }

    public boolean matches(byte[] address) {
		if (address.length != pattern.length) return false;
		for (int cnt = pattern.length, n = 0; n < cnt; n++) {
			if ((address[n] & mask[n]) != pattern[n]) {
				return false;
			}
		}
		return true;
	}

    /** 
     * Convert the given IP address (V4) into a byte array.
     * 
     * @param    anAddress    The IP address (V4) to be converted.
     * @return   The byte representation of the given IP address (V4) or 
     *           <code>null</code>, if not a valid address.
     */
    public static byte[] getByteArray(String anAddress) {
        StringTokenizer theToken = new StringTokenizer(anAddress, ".");

        if (theToken.countTokens() == 4) {
            byte[] theAddress = new byte[4];
            int    thePos     = 0;

            while (theToken.hasMoreTokens()) {
                theAddress[thePos] = (byte) Integer.valueOf(theToken.nextToken()).intValue();
                thePos++;
            }

            return (theAddress);
        }
        else {
            return (null);
        }
    }

    /** 
	 * Return an instance of this class for the given string.
	 * 
	 * @param    aString    The string pattern to get an instance for, must not be <code>null</code>.
	 * @return   The requested CIDR filter, never <code>null</code>.
	 * @throws   ParseException    If the given pattern is invalid.
	 */
	public static CIDRFilter fromString(String aString) throws ParseException {
		String theSource    = aString.trim();
		int    theSeparator = theSource.indexOf('/');
		
		String thePatternSource;
		String theMaskSource;

        if (theSeparator >= 0) {
			thePatternSource = theSource.substring(0, theSeparator);
			theMaskSource    = theSource.substring(theSeparator + 1);
		} 
        else {
			thePatternSource = theSource;
			theMaskSource    = null;
		}

		String[] theByteSource = thePatternSource.split("\\.");

        if (theByteSource.length != 4) 
			throw new ParseException("Pattern length must be exactly 4 bytes.", thePatternSource.length());
		
		byte[] thePattern = new byte[theByteSource.length];
		for (int theCnt = theByteSource.length, thePos = 0; thePos < theCnt; thePos++) {
			int thePatternByte;

            try {
				thePatternByte = Integer.parseInt(theByteSource[thePos]);
			}
            catch (NumberFormatException ex) {
				throw new ParseException("Illegal pattern: " + ex, thePos);
			}

            if (thePatternByte < 0 || thePatternByte > 255) {
                throw new ParseException("Pattern out of range: " + thePatternByte, thePos);
            }

            thePattern[thePos] = (byte) thePatternByte;
		}

		int theMaskBits;

        if (theMaskSource == null) {
			theMaskBits = 8 * thePattern.length;
		}
        else {
			try {
				theMaskBits = Integer.parseInt(theMaskSource);
			}
            catch (NumberFormatException ex) {
				throw new ParseException("Illegal mask: " + ex, theSeparator + 1);
			}

            if (theMaskBits < 0 || theMaskBits > 32) {
                throw new ParseException("Mask bits out of range: " + theMaskBits, theSeparator + 1);
            }
		}

		return new CIDRFilter(thePattern, theMaskBits);
	}
}
