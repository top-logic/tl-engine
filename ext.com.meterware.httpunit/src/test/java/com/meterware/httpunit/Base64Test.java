package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: Base64Test.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2001, Russell Gold
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
* documentation files (the "Software"), to deal in the Software without restriction, including without limitation
* the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
* to permit persons to whom the Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or substantial portions
* of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
* THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*******************************************************************************************************************/
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the base64 converter.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:mtarruella@silacom.com">Marcos Tarruella</a> 
 **/
public class Base64Test extends TestCase {

    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( Base64Test.class );
    }


    public Base64Test( String name ) {
        super( name );
    }


    public void testEncode() {
        assertEquals( "Result of encoding", "QWxhZGRpbjpvcGVuIHNlc2FtZQ==", Base64.encode( "Aladdin:open sesame" ) );
        assertEquals( "Result of encoding", "QWRtaW46Zm9vYmFy",             Base64.encode( "Admin:foobar" ) );
    }


    public void testDecode() {
        assertEquals( "Result of decoding", "Aladdin:open sesame", Base64.decode( "QWxhZGRpbjpvcGVuIHNlc2FtZQ==" ) );
        assertEquals( "Result of decoding", "Admin:foobar",        Base64.decode( "QWRtaW46Zm9vYmFy" ) );
    }


    public void testExceptionDecoding() {
        try {
            Base64.decode( "123");
            fail("valid Base64 codes have a multiple of 4 characters");
        } catch (Exception e) {
            assertEquals("valid Base64 codes have a multiple of 4 characters",
                e.getMessage());
        }
    }

}

