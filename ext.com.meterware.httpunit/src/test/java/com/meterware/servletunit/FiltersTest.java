package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: FiltersTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004, Russell Gold
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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.InputSource;


/**
 * Tests the servlet filtering capability added in Servlet 2.3.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class FiltersTest extends TestCase {


    final static FilterMetaData FILTER1 = new FilterMetaDataImpl(1);
    final static FilterMetaData FILTER2 = new FilterMetaDataImpl(2);
    final static FilterMetaData FILTER3 = new FilterMetaDataImpl(3);
    final static FilterMetaData FILTER4 = new FilterMetaDataImpl(4);
    final static FilterMetaData FILTER5 = new FilterMetaDataImpl(5);
    final static FilterMetaData FILTER6 = new FilterMetaDataImpl(6);


    private static boolean _servletCalled;


    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( FiltersTest.class );
    }


    public FiltersTest( String name ) {
        super( name );
    }


    /**
     * Verifies that the no-filter case is handled by servlet metadata.
     */
    public void testNoFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        WebApplication application = new WebApplication( HttpUnitUtils.newParser().parse( new InputSource( wxs.asInputStream() ) ), null );

        ServletMetaData metaData = application.getServletRequest( new URL( "http://localhost/SimpleServlet" ) );
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals( "number of associated filters", 0, filters.length );
    }


    /**
     * Verifies that a simple filter is associated with a servlet by its name.
     */
    public void testNameFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Trivial", TrivialFilter.class, "Simple" );
        WebApplication application = new WebApplication( HttpUnitUtils.newParser().parse( new InputSource( wxs.asInputStream() ) ), null );

        ServletMetaData metaData = application.getServletRequest( new URL( "http://localhost/SimpleServlet" ) );
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals( "number of associated filters", 1, filters.length );
        assertEquals( "filter class", TrivialFilter.class, filters[0].getFilter().getClass() );
    }


    /**
     * Verifies that a simple filter will be called before a servlet with the same URL.
     */
    public void testFilterByNameInvocation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Trivial", TrivialFilter.class, "Simple" );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );
        assertTrue( "Did not find a filter", ic.isFilterActive() );

        Filter filter = ic.getFilter();
        assertNotNull( "Filter is null", filter );
        assertEquals( "Filter class", TrivialFilter.class, filter.getClass() );
        ic.pushFilter( ic.getRequest(), ic.getResponse() );
        assertFalse( "Did not switch to servlet", ic.isFilterActive() );
        assertEquals( "Servlet class", SimpleGetServlet.class, ic.getServlet().getClass() );
        ic.popRequest();
        assertTrue( "Did not pop back to filter", ic.isFilterActive() );
        assertSame( "Restored filter", filter, ic.getFilter() );
    }


    /**
     * Verifies that a simple filter will be called before a servlet with the same URL.
     */
    public void testNamedFilterOrder() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Trivial", TrivialFilter.class, "Simple" );
        wxs.addFilterForServlet( "Attribute", AttributeFilter.class, "Simple" );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );

        Filter filter1 = ic.getFilter();
        assertEquals( "Filter 1 class", TrivialFilter.class, filter1.getClass() );
        ic.pushFilter( ic.getRequest(), ic.getResponse() );

        assertTrue( "Did not find a filter", ic.isFilterActive() );
        Filter filter2 = ic.getFilter();
        assertEquals( "Filter 2 class", AttributeFilter.class, filter2.getClass() );
        ic.pushFilter( ic.getRequest(), ic.getResponse() );

        assertFalse( "Did not switch to servlet", ic.isFilterActive() );
        assertEquals( "Servlet class", SimpleGetServlet.class, ic.getServlet().getClass() );
        ic.popRequest();
        assertSame( "Restored filter 2", filter2, ic.getFilter() );
        ic.popRequest();
        assertSame( "Restored filter 1", filter1, ic.getFilter() );
    }


    /**
     * Verifies that request / response wrappering for filters is supported.
     */
    public void testFilterRequestWrapping() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Trivial", TrivialFilter.class, "Simple" );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();

        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );
        HttpServletRequest originalRequest = ic.getRequest();
        HttpServletResponse originalResponse = ic.getResponse();

        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper( originalResponse );
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper( originalRequest );

        ic.pushFilter( requestWrapper, responseWrapper );
        assertFalse( "Did not switch to servlet", ic.isFilterActive() );
        assertSame( "Servlet request", requestWrapper, ic.getRequest() );
        assertSame( "Servlet response", responseWrapper, ic.getResponse() );

        ic.popRequest();
        assertSame( "Filter request", originalRequest, ic.getRequest() );
        assertSame( "Filter response", originalResponse, ic.getResponse() );
    }


    /**
     * Verifies that the filter chain invokes the servlet.
     */
    public void testFilterChain() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Trivial", TrivialFilter.class, "Simple" );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );
        _servletCalled = false;
        HttpServletResponse originalResponse = ic.getResponse();
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper( originalResponse );
        ic.getFilterChain().doFilter( ic.getRequest(), responseWrapper );
        assertTrue( "Servlet was not called", _servletCalled );
        assertTrue( "Filter marked as active", ic.isFilterActive() );
        assertSame( "Response object after doFilter", originalResponse, ic.getResponse() );
    }


    /**
     * Verifies that filters are automatically called.
     */
    public void testFilterInvocation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForServlet( "Attribute", AttributeFilter.class, "Simple" );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();
        WebResponse wr = wc.getResponse( "http://localhost/SimpleServlet" );
        assertEquals( "Filtered response ", "by-filter", wr.getText().trim() );
    }


    /**
     * Verifies that a simple filter is associated with a url pattern.
     */
    public void testUrlFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/helpMe/SimpleServlet", SimpleGetServlet.class );
        wxs.addFilterForUrl( "Trivial", TrivialFilter.class, "/helpMe/*" );
        wxs.addFilterForUrl( "Other", AttributeFilter.class, "/Simple" );
        WebApplication application = new WebApplication( HttpUnitUtils.newParser().parse( new InputSource( wxs.asInputStream() ) ), null );

        ServletMetaData metaData = application.getServletRequest( new URL( "http://localhost/helpMe/SimpleServlet" ) );
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals( "number of associated filters", 1, filters.length );
        assertEquals( "filter class", TrivialFilter.class, filters[0].getFilter().getClass() );
    }


    public void testFilterMapping() throws Exception {
        FilterUrlMap map = new FilterUrlMap();
        map.put( "/foo/bar/*", FILTER1 );
        map.put( "/baz/*",     FILTER2 );
        map.put( "/catalog",   FILTER3 );
        map.put( "*.bop",      FILTER4 );
        map.put( "/foo/bar/*", FILTER5 );
        map.put( "/foo/*",     FILTER6 );

        checkMapping( map, "/catalog",             new FilterMetaData[] { FILTER3 } );
        checkMapping( map, "/catalog/racecar.bop", new FilterMetaData[] { FILTER4 }  );
        checkMapping( map, "/index.bop",           new FilterMetaData[] { FILTER4 }  );
        checkMapping( map, "/foo/bar/index.html",  new FilterMetaData[] { FILTER1, FILTER5, FILTER6 } );
        checkMapping( map, "/foo/index.bop",       new FilterMetaData[] { FILTER4, FILTER6 }  );
        checkMapping( map, "/baz",                 new FilterMetaData[] { FILTER2 }  );
        checkMapping( map, "/bazel",               new FilterMetaData[0] );
        checkMapping( map, "/baz/index.html",      new FilterMetaData[] { FILTER2 }  );
        checkMapping( map, "/something/else",      new FilterMetaData[0] );
    }


    private void checkMapping( FilterUrlMap map, String urlString, FilterMetaData[] expectedFilters ) {
        assertEquals( "Filters selected for '" + urlString + "'",
                      Arrays.asList( expectedFilters),
                      Arrays.asList( map.getMatchingFilters( urlString ) ) );
    }


    public void testFilterInitialization() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( "Simple", "/SimpleServlet", SimpleGetServlet.class );
        Properties params = new Properties();
        params.setProperty( "color", "red" );
        params.setProperty( "age", "12" );
        wxs.addFilterForServlet( "Config", AttributeFilter.class, "Simple", params );
        ServletRunner sr = new ServletRunner( wxs.asInputStream() );
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation( "http://localhost/SimpleServlet" );

        AttributeFilter filter = (AttributeFilter) ic.getFilter();
        FilterConfig filterConfig = filter._filterConfig;
        assertNotNull( "Filter was not initialized", filterConfig );
        assertEquals( "Filter name", "Config", filterConfig.getFilterName() );
        assertNotNull( "No servlet context provided", filterConfig.getServletContext() );

        assertNull( "init parameter 'gender' should be null", filterConfig.getInitParameter( "gender" ) );
        assertEquals( "init parameter 'red'", "red", filterConfig.getInitParameter( "color" ) );

        ArrayList names = new ArrayList();
        for (Enumeration e = filterConfig.getInitParameterNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            names.add( name );
        }
        assertEquals( "Number of names in enumeration", 2, names.size() );
        assertTrue( "'color' not found in enumeration", names.contains( "color" ) );
        assertTrue( "'age' not found in enumeration", names.contains( "age" ) );
    }


    // TODO combination of named and url filters (url filters go first)
    // TODO filter shutdown
    // TODO filters with request dispatchers
    // TODO filters throwing UnavailableException


    static class AttributeFilter implements Filter {

        private FilterConfig _filterConfig;


        public void init( FilterConfig filterConfig ) throws ServletException {
            _filterConfig = filterConfig;
        }

        public void destroy() {}

        public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain ) throws IOException, ServletException {
            servletRequest.setAttribute( "called", "by-filter" );
            filterChain.doFilter( servletRequest, servletResponse );
        }

    }



    static class TrivialFilter implements Filter {

        public void init( FilterConfig filterConfig ) throws ServletException {}
        public void destroy() {}

        public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain ) throws IOException, ServletException {
            servletRequest.setAttribute( "called", "trivially" );
            filterChain.doFilter( servletRequest, servletResponse );
        }

    }


    static class SimpleGetServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( req.getAttribute( "called" ) );
            pw.close();
            _servletCalled = true;
        }
    }


    static class FilterMetaDataImpl implements FilterMetaData {

        private int _index;

        public FilterMetaDataImpl( int index ) {
            _index = index;
        }

        public Filter getFilter() throws ServletException {
            return null;
        }

        public String toString() {
            return "Filter" + _index;
        }
    }
}
