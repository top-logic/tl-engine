package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: WebXMLString.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2001-2004, 2006,2008 Russell Gold
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
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.meterware.httpunit.HttpUnitUtils;


/**
 * A class which allows dynamic creation of Servlet configuration XML
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class WebXMLString {

    private String _displayName;

    private ArrayList _servlets = new ArrayList();
    private ArrayList _mappings = new ArrayList();
    private ArrayList _servletNames = new ArrayList();
    private Hashtable _initParams = new Hashtable();

    private ArrayList _listeners = new ArrayList();

    private ArrayList _filters        = new ArrayList();
    private Hashtable _filterMappings = new Hashtable();
    private ArrayList _filterNames    = new ArrayList();
    private Hashtable _filterParams   = new Hashtable();

    private String _loginConfig = "";
    private Hashtable _resources = new Hashtable();
    private Hashtable _contextParams = new Hashtable();
    private Hashtable _loadOnStartup = new Hashtable();


    ByteArrayInputStream asInputStream() throws UnsupportedEncodingException {
        return new ByteArrayInputStream( asText().getBytes( "UTF-8" ) );
    }

    // the stream for testing the dtd file
    public static InputStream dtdStream=null;
    
    //  Document type definition to be used - should be one of the dtds available in META-INF
    public static String dtd="web-app_2_3.dtd"; 
    
    /**
     * check that the dtd is on the CLASSPATH
     * @return
     */
    public static boolean isDtdOnClasspath() {
    	if (dtdStream==null) {        		
    		dtdStream=WebXMLString.class.getClassLoader().getResourceAsStream(dtd);
    	}	
    	return dtdStream!=null;	
    }
    
    /**
     * return me as a Text/String containing the XML descriptor accoding
     * to the Sun Microsystems's specification
     * if running on Eclipse shorten the DOCTYPE spec as a work-around for a
     * bogus java.net.MalformedURLException (that is in fact caused by a null-pointer exception in handling the dtd part)
     * @return
     */
    String asText() {
        StringBuffer result = new StringBuffer( "<?xml version='1.0' encoding='UTF-8'?>\n" );                
        String doctype="<!DOCTYPE web-app PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN' 'http://java.sun.com/dtd/"+dtd+"'>";
        // if the dtd file is not on the CLASSPATH there will be nasty java.net.MalformedURLException problems
    		// work - around if dtd is not in CLASSPATH so that only the one test case
        // that checks the isDtdOnClassPath condition will fail 
      	if (!isDtdOnClasspath()) {        		
      		doctype="<!DOCTYPE web-app>";
      	}	
        result.append(doctype);
        result.append( "<web-app>\n" );
//        result.append( "<web-app version='2.4' xmlns='http://java.sun.com/xml/ns/j2ee'\n " );
//        result.append( "                       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" );
//        result.append( "                       xsi:schemaLocation='http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd'>\n" );

        if (_displayName != null) result.append( "  <display-name>" ).append( _displayName ).append( "</display-name>" );
        for (Iterator i = _contextParams.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            result.append( "  <context-param>\n    <param-name>" ).append( entry.getKey() );
            result.append( "</param-name>\n    <param-value>" ).append( entry.getValue() ).append( "</param-value>\n  </context-param>\n" );
        }
        for (int i = 0; i < _filters.size(); i++) {
            Object name = _filterNames.get( i );
            result.append( "  <filter>\n    <filter-name>" ).append( name ).append( "</filter-name>\n" );
            result.append( "    <filter-class>" ).append( ((Class) _filters.get( i )).getName() ).append( "</filter-class>\n" );
            appendParams( result, "init-param", (Hashtable) _filterParams.get( name ) );
            result.append( "  </filter>\n" );
        }
        for (int i = 0; i < _filters.size(); i++) {
            result.append( "  <filter-mapping>\n    <filter-name>" ).append( _filterNames.get( i ) ).append( "</filter-name>\n" );
            result.append( "    " ).append( _filterMappings.get( _filterNames.get( i ) ) ).append( "\n  </filter-mapping>\n" );
        }
        for (int i = 0; i < _listeners.size(); i++) {
            Class aClass = (Class) _listeners.get( i );
            result.append( "  <listener><listener-class>" ).append( aClass.getName() );
            result.append( "</listener-class></listener>\n" );
        }
        for (int i = _servlets.size() - 1; i >= 0; i--) {
            Object name = _servletNames.get( i );
            result.append( "  <servlet>\n    <servlet-name>" ).append( name ).append( "</servlet-name>\n" );
            result.append( "    <servlet-class>" ).append( ((Class) _servlets.get( i )).getName() ).append( "</servlet-class>\n" );
            appendParams( result, "init-param", (Hashtable) _initParams.get( name ) );
            appendLoadOnStartup( result, _loadOnStartup.get( name ) );
            result.append( "  </servlet>\n" );
        }
        for (int i = _mappings.size() - 1; i >= 0; i--) {
            result.append( "  <servlet-mapping>\n    <servlet-name>" ).append( _servletNames.get( i ) ).append( "</servlet-name>\n" );
            result.append( "    <url-pattern>" ).append( _mappings.get( i ) ).append( "</url-pattern>\n  </servlet-mapping>\n" );
        }
        for (Enumeration e = _resources.elements(); e.hasMoreElements();) {
            result.append( ((WebResourceSpec) e.nextElement()).asText() );
        }
        result.append( _loginConfig );
        result.append( "</web-app>" );
        return result.toString();
    }


    private void appendLoadOnStartup( StringBuffer result, Object startupOrder ) {
        if (startupOrder == null) return;
        result.append( "    <load-on-startup" );
        if (startupOrder instanceof Number) result.append( ">" ).append( startupOrder ).append( "</load-on-startup>\n" );
        else result.append( "/>\n" );
    }


    private void appendParams( StringBuffer result, String tagName, Hashtable params ) {
        if (params == null) return;
        for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.append( "    <" ).append( tagName ).append( ">\n      <param-name>" ).append( entry.getKey() );
            result.append( "</param-name>\n      <param-value>" ).append( entry.getValue() ).append( "</param-value>\n    </" );
            result.append( tagName ).append( ">\n" );
        }
    }


    void addContextParam( String name, String value ) {
        _contextParams.put( name, value );
    }


    void addServlet( String urlPattern, Class servletClass ) {
        addServlet( "servlet_" + _servlets.size(), urlPattern, servletClass );
    }


    void addServlet( String name, String urlPattern, Class servletClass ) {
        _servlets.add( servletClass );
        _mappings.add( urlPattern );
        _servletNames.add( name );
    }

    
    void addServlet( String name, String urlPattern, Class servletClass, Properties initParams ) {
        _initParams.put( name, initParams );
        addServlet( name, urlPattern, servletClass );
    }


    void setLoadOnStartup( String servletName ) {
        _loadOnStartup.put( servletName, Boolean.TRUE );
    }


    void setLoadOnStartup( String servletName, int i ) {
        _loadOnStartup.put( servletName, Integer.valueOf(i) );
    }


    void addFilterForServlet( String name, Class filterClass, String servletName, Properties initParams ) {
        _filterParams.put( name, initParams );
        addFilterForServlet( name, filterClass, servletName );
    }


    void addFilterForServlet( String name, Class filterClass, String servletName ) {
        addFilter( filterClass, name, "<servlet-name>" + servletName + "</servlet-name>" );
    }


    void addFilterForUrl( String name, Class filterClass, String urlPattern ) {
        addFilter( filterClass, name, "<url-pattern>" + urlPattern + "</url-pattern>" );
    }


    private void addFilter( Class filterClass, String name, String mapping ) {
        _filters.add( filterClass );
        _filterNames.add( name );
        _filterMappings.put( name, mapping );
    }


    void requireBasicAuthorization( String realmName ) {
        _loginConfig = "  <login-config>\n" +
                "    <auth-method>BASIC</auth-method>\n" +
                "    <realm-name>" + realmName + "</realm-name>\n" +
                "  </login-config>\n";
    }


    void requireBasicAuthentication( String realmName ) {
        _loginConfig = "  <login-config>\n" +
                "    <auth-method>BASIC</auth-method>\n" +
                "    <realm-name>" + realmName + "</realm-name>\n" +
                "  </login-config>\n";
    }


    void requireFormAuthentication( String realmName, String loginPagePath, String errorPagePath ) {
        _loginConfig = "  <login-config>\n" +
                "    <auth-method>FORM</auth-method>\n" +
                "    <realm-name>" + realmName + "</realm-name>\n" +
                "    <form-login-config>" +
                "      <form-login-page>" + loginPagePath + "</form-login-page>\n" +
                "      <form-error-page>" + errorPagePath + "</form-error-page>\n" +
                "    </form-login-config>" +
                "  </login-config>\n";
    }


    void addSecureURL( String resourceName, String urlPattern ) {
        getWebResource( resourceName ).addURLPattern( urlPattern );
    }


    void addAuthorizedRole( String resourceName, String roleName ) {
        getWebResource( resourceName ).addAuthorizedRole( roleName );
    }


    void addContextListener( Class aClass ) {
        _listeners.add( aClass );
    }


    private WebResourceSpec getWebResource( String resourceName ) {
        WebResourceSpec result = (WebResourceSpec) _resources.get( resourceName );
        if (result == null) {
            result = new WebResourceSpec( resourceName );
            _resources.put( resourceName, result );
        }
        return result;
    }

    void setDisplayName( String displayName ) {
        _displayName = displayName;
    }
}


class WebResourceSpec {

    WebResourceSpec( String name ) {
        _name = name;
    }


    void addURLPattern( String urlPattern ) {
        _urls.add( urlPattern );
    }


    void addAuthorizedRole( String roleName ) {
        _roles.add( roleName );
    }


    String asText() {
        StringBuffer sb = new StringBuffer();
        sb.append( "  <security-constraint>\n" );
        sb.append( "    <web-resource-collection>\n" );
        sb.append( "      <web-resource-name>" ).append( _name ).append( "</web-resource-name>\n" );
        for (Iterator i = _urls.iterator(); i.hasNext();) {
            sb.append( "      <url-pattern>" ).append( i.next() ).append( "</url-pattern>\n" );
        }
        sb.append( "    </web-resource-collection>\n" );
        sb.append( "    <auth-constraint>\n" );
        for (Iterator i = _roles.iterator(); i.hasNext();) {
            sb.append( "      <role-name>" ).append( i.next() ).append( "</role-name>\n" );
        }
        sb.append( "    </auth-constraint>\n" );
        sb.append( "  </security-constraint>\n" );
        return sb.toString();
    }


    private String _name;
    private ArrayList _urls = new ArrayList();
    private ArrayList _roles = new ArrayList();
}