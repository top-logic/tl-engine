/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.util.Date;

/** 
 * Helper class for TestPersitency.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class InstanceHelper  {
    
	private int			i1;
    private int         i2;
    private byte        b1;
    private byte        b2;
    private String 		s1;
    private String		s2;
    private float		r1;
    private	double 		r2;
    private	Date 		d1;

	/** For Debugging output.
     */
	@Override
	public String toString()  {
    	return "i1 = "  + i1 
            + ",i2= "   + i2 
            + ",b1= "   + b1 
            + ",b2= "   + b2 
        	+ ",s1 = '" + s1 + "'"
        	+ ",s2 = '" + s2 + "'"
        	+ ",r1 = "  + r1 
        	+ ",r2 = "  + r2
        	+ ",d1 = "  + d1; 
	}
    
    /** Accessor function */
    public void setI1(int i) 	{ i1 = i; }

    /** Accessor function */
    public void setI2(int i)    { i2 = i; }

    /** Accessor function */
    public void setB1(int b)   { b1 = (byte) b; }

    /** Accessor function */
    public void setB2(int b)   { b2 = (byte) b; }

    /** Accessor function */
    public void setS1(String s) { s1 = s; }

    /** Accessor function */
    public void setS2(String s) { s2 = s; }

    /** Accessor function */
    public void setR1(float f) 	{ r1 = f; }

    /** Accessor function */
    public void setR2(double d) { r2 = d; }
    
    /** Accessor function */
    public void setD1(Date d)   { d1 = d; }
}
