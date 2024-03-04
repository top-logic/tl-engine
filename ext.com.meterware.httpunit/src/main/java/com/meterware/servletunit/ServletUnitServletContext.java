package com.meterware.servletunit;
/********************************************************************************************************************
* $Id: ServletUnitServletContext.java 318923 2023-03-07 14:22:07Z bhu $
*
* Copyright (c) 2000-2008, Russell Gold
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;



/**
 * This class is a private implementation of the ServletContext class.
 **/
public class ServletUnitServletContext implements ServletContext {

    private PrintStream _logStream = System.out;


    ServletUnitServletContext( WebApplication application ) {
        _application = application;
    }


    /**
     * Returns a ServletContext object that corresponds to a specified URL on the server.
     * <p>
     * This method allows servlets to gain access to the context for various parts of the server,
     * and as needed obtain RequestDispatcher objects from the context. The given path must be
     * absolute (beginning with "/") and is interpreted based on the server's document root.
     * <p>
     * In a security conscious environment, the servlet container may return null for a given URL.
     **/
    public jakarta.servlet.ServletContext getContext(java.lang.String A) {
        return null;
    }


    /**
     * Returns the major version of the Java Servlet API that this servlet container supports.
     * All implementations that comply with Version 2.4 must have this method return the integer 2.
     **/
    public int getMajorVersion() {
        return 2;
    }


    /**
     * Returns the minor version of the Servlet API that this servlet container supports.
     * All implementations that comply with Version 2.4 must have this method return the integer 4.
     **/
    public int getMinorVersion() {
        return 4;
    }


    /**
     * Returns the MIME type of the specified file, or null if the MIME type is not known.
     * The MIME type is determined by the configuration of the servlet container, and
     * may be specified in a web application deployment descriptor. Common MIME types are
     * "text/html" and "image/gif".
     **/
    public java.lang.String getMimeType( String filePath ) {
        return URLConnection.getFileNameMap().getContentTypeFor(filePath);
    }


    /**
     * Returns a URL to the resource that is mapped to a specified path. The path must begin
     * with a "/" and is interpreted as relative to the current context root.
     * <p>
     * This method allows the servlet container to make a resource available to servlets from any source.
     * Resources can be located on a local or remote file system, in a database, or in a .war file.
     * <p>
     * The servlet container must implement the URL handlers and URLConnection objects that are necessary to access the resource.
     * <p>
     * This method returns null if no resource is mapped to the pathname.
     *
     * Some containers may allow writing to the URL returned by this method using the methods of the URL class.
     *
     * The resource content is returned directly, so be aware that requesting a .jsp page returns the JSP source code. Use a
     * RequestDispatcher instead to include results of an execution.
     *
     * This method has a different purpose than java.lang.Class.getResource, which looks up resources based on a class loader. This
     * method does not use class loaders.
     **/
    public java.net.URL getResource( String path ) {
        try {
            File resourceFile = _application.getResourceFile( path );
            // PATCH proposal [ 1592532 ] Invalid ServletUnitServletContext#getResource(String path)
            // by Timo Westkämper
            // return !resourceFile.exists() ? null : 	resourceFile.toURL();            
            return resourceFile == null ? null : resourceFile.toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }


    /**
     * Returns the resource located at the named path as an InputStream object.
     *
     * The data in the InputStream can be of any type or length. The path must be specified according to the rules given in getResource.
     * This method returns null if no resource exists at the specified path.

     * Meta-information such as content length and content type that is available via getResource method is lost when using this method.

     * The servlet container must implement the URL handlers and URLConnection objects necessary to access the resource.

     * This method is different from java.lang.Class.getResourceAsStream, which uses a class loader. This method allows servlet
     * containers to make a resource available to a servlet from any location, without using a class loader.
     **/
    public java.io.InputStream getResourceAsStream( String path ) {
        try {
            File resourceFile = _application.getResourceFile( path );
            return resourceFile == null ? null : new FileInputStream( resourceFile );
        } catch (FileNotFoundException e) {
            return null;
        }
    }


    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path. A RequestDispatcher
     * object can be used to forward a request to the resource or to include the resource in a response. The resource can be dynamic or static.

     * The pathname must begin with a "/" and is interpreted as relative to the current context root. Use getContext to obtain a
     * RequestDispatcher for resources in foreign contexts. This method returns null if the ServletContext cannot return a
     * RequestDispatcher.
     **/
    public jakarta.servlet.RequestDispatcher getRequestDispatcher( String path ) {
        try {
            URL url = new URL( "http", "localhost", _application.getContextPath() + path );
            return new RequestDispatcherImpl( _application, url );
        } catch (ServletException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }


    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the named servlet.
     *
     * Servlets (and JSP pages also) may be given names via server administration or via a web application deployment descriptor. A servlet
     * instance can determine its name using ServletConfig.getServletName().
     *
     * This method returns null if the ServletContext cannot return a RequestDispatcher for any reason.
     **/
    public jakarta.servlet.RequestDispatcher getNamedDispatcher(java.lang.String A) {
        return null;   // XXX not implemented
    }


    /**
     * @deprecated as of Servlet API 2.1
     **/
    public jakarta.servlet.Servlet getServlet(java.lang.String A) {
        return null;
    }


    /**
     * @deprecated as of Servlet API 2.0
     **/
    public java.util.Enumeration getServlets() {
        return EMPTY_VECTOR.elements();
    }


    /**
     * @deprecated as of Servlet API 2.1
     **/
    public java.util.Enumeration getServletNames() {
        return EMPTY_VECTOR.elements();
    }


    /**
     * Writes the specified message to a servlet log file, usually an event log.
     * The name and type of the servlet log file is specific to the servlet container.
     **/
    public void log( String message ) {  
        _logStream.println(message);
    }


    /**
     * @deprecated use log( String, Throwable )
     **/
    public void log( Exception e, String message ) {
        log( message, e );
    }


    /**
     * Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file.
     * The name and type of the servlet log file is specific to the servlet container, usually an event log.
     **/
    public void log( String message, Throwable t ) {
        _logStream.print(message);
        _logStream.print(":");
        if(t != null) {
            t.printStackTrace(_logStream);
        }
    }


    /**
     * Returns a String containing the real path for a given virtual path. For example, the virtual path "/index.html" has a real path of
     * whatever file on the server's filesystem would be served by a request for "/index.html".
     *
     * The real path returned will be in a form appropriate to the computer and operating system on which the servlet container is running,
     * including the proper path separators. This method returns null if the servlet container cannot translate the virtual path to a real path for
     * any reason (such as when the content is being made available from a .war archive).
     **/
    public String getRealPath( String path ) {
        return _application.getResourceFile( path ).getAbsolutePath();
    }
    
    public static final String DEFAULT_SERVER_INFO="ServletUnit test framework";

    /**
     * Returns the name and version of the servlet container on which the servlet is running.

     * The form of the returned string is servername/versionnumber. For example, the JavaServer Web Development Kit may return the
     * string JavaServer Web Dev Kit/1.0.

     * The servlet container may return other optional information after the primary string in parentheses, for example, JavaServer Web
     * Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86).
     **/
    public String getServerInfo() {
        return DEFAULT_SERVER_INFO;
    }


    /**
     * Returns a String containing the value of the named context-wide initialization parameter, or null if the parameter does not exist.
     *
     * This method can make available configuration information useful to an entire "web application". For example, it can provide a
     * webmaster's email address or the name of a system that holds critical data.
     **/
    public java.lang.String getInitParameter( String name ) {
        return (String) getContextParams().get( name );
    }


    /**
     * Returns the names of the context's initialization parameters as an Enumeration of String objects,
     * or an empty Enumeration if the context has no initialization parameters.
     **/
    public java.util.Enumeration getInitParameterNames() {
            return getContextParams().keys();
        }


    /**
     * Returns the servlet container attribute with the given name, or null if there is no attribute by that name.
     * An attribute allows a servlet container to give the servlet additional information not already
     * provided by this interface. See your server documentation for information
     * about its attributes. A list of supported attributes can be retrieved using getAttributeNames.
     **/
    public Object getAttribute( String name ) {
        return _attributes.get( name );
    }


    public Enumeration getAttributeNames() {
        return _attributes.keys();
    }

    public void setAttribute( String name, Object attribute ) {
        if (!_attributes.containsKey( name )) {
            _attributes.put( name, attribute );
            _application.sendAttributeAdded( name, attribute );
        } else {
            Object oldValue = _attributes.get( name );
            _attributes.put( name, attribute );
            _application.sendAttributeReplaced( name, oldValue );
        }
    }


    public void removeAttribute( String name ) {
        Object oldValue = _attributes.get( name );
        _attributes.remove( name );
        _application.sendAttributeRemoved( name, oldValue );
    }



//----------------------------- methods added to ServletContext in JSDK 2.3 --------------------------------------

    /**
     * Returns a directory-like listing of all the paths to resources within the web application
     * whose longest sub-path matches the supplied path argument. Paths indicating subdirectory paths end with a '/'.
     * The returned paths are all relative to the root of the web application and have a leading '/'.
     * For example, for a web application containing
     * <p>
     * /welcome.html<br />
     * /catalog/index.html<br /><br />
     * /catalog/products.html<br />
     * /catalog/offers/books.html<br />
     * /catalog/offers/music.html<br />
     * /customer/login.jsp<br />
     * /WEB-INF/web.xml<br />
     * /WEB-INF/classes/com.acme.OrderServlet.class,<br />
     * <br />
     * getResourcePaths("/") returns {"/welcome.html", "/catalog/", "/customer/", "/WEB-INF/"}<br />
     * getResourcePaths("/catalog/") returns {"/catalog/index.html", "/catalog/products.html", "/catalog/offers/"}.
     *
     * @param path partial path used to match the resources, which must start with a /
     * @return a Set containing the directory listing, or null if there are no resources
     *         in the web application whose path begins with the supplied path.
     * @since HttpUnit 1.3
     */
    public Set getResourcePaths( String path ) {
        return null;
    }


    /**
     * Returns the name of this web application correponding to this ServletContext as specified
     * in the deployment descriptor for this web application by the display-name element.
     *
     * @return The name of the web application or null if no name has been declared in the deployment descriptor
     * @since HttpUnit 1.3
     */
    public String getServletContextName() {
        return _application.getDisplayName();
    }

//-------------------------------------- servlet-api 2.5 additions -----------------------------------------------

    public String getContextPath() {
        return _application.getContextPath();
    }

//-------------------------------------- (partial) servlet-api 3.0 additions -----------------------------------------------

	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
    
//------------------------------------------- package members ----------------------------------------------------


    void setInitParameter( String name, Object initParameter ) {
        getContextParams().put( name, initParameter );
    }


    void removeInitParameter( String name ) {
        getContextParams().remove( name );
    }


//------------------------------------------- private members ----------------------------------------------------

    private final static Vector EMPTY_VECTOR = new Vector();

    private Hashtable      _attributes = new Hashtable();
    private WebApplication _application;


    private Hashtable getContextParams() {
        return _application.getContextParameters();
    }

    /**
     * Allows the test to determine where the log messages should be written.
     * Defaults to {@link System#out}
     * @param logStream where to write the log messages
     * @see #log(String)
     */
    public void setLogStream(PrintStream logStream) {
        this._logStream = logStream;
    }


	@Override
	public int getEffectiveMajorVersion() {
		return getMajorVersion();
	}


	@Override
	public int getEffectiveMinorVersion() {
		return getMinorVersion();
	}


	@Override
	public boolean setInitParameter(String name, String value) {
		return false;
	}


	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}


	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return null;
	}


	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return Collections.emptyMap();
	}


	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		throw new UnsupportedOperationException();
	}


	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		throw new UnsupportedOperationException();
	}


	@Override
	public jakarta.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T extends Filter> T createFilter(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}


	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return null;
	}


	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return Collections.emptyMap();
	}


	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return Collections.singleton(SessionTrackingMode.COOKIE);
	}


	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return getDefaultSessionTrackingModes();
	}


	@Override
	public void addListener(String className) {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T extends EventListener> void addListener(T t) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T extends EventListener> T createListener(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}


	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		final WebApplication application = _application;
		return new JspConfigDescriptor() {
			
			@Override
			public Collection<TaglibDescriptor> getTaglibs() {
				ArrayList<TaglibDescriptor> result = new ArrayList<TaglibDescriptor>();
				{
					Document document = application.getWebXml();
					for (Node child = document.getDocumentElement().getFirstChild(); child != null; child = child.getNextSibling()) {
						if (child.getNodeType() != Node.ELEMENT_NODE) {
							continue;
						}
						if ("jsp-config".equals(child.getNodeName())) {
							for (Node entry = child.getFirstChild(); entry != null; entry = entry.getNextSibling()) {
								if (entry.getNodeType() != Node.ELEMENT_NODE) {
									continue;
								}
								if ("taglib".equals(entry.getNodeName())) {
									String searchUri = null;
									String searchLocation = null;
									
									for (Node property = entry.getFirstChild(); property != null; property = property.getNextSibling()) {
										if (property.getNodeType() != Node.ELEMENT_NODE) {
											continue;
										}
										if ("taglib-uri".equals(property.getNodeName())) {
											searchUri = property.getTextContent();
										}
										else if ("taglib-location".equals(property.getNodeName())) {
											searchLocation = property.getTextContent();
										}
									}
									
									final String uri = searchUri;
									final String location = searchLocation;
									
									result.add(new TaglibDescriptor() {
										@Override
										public String getTaglibURI() {
											return uri;
										}

										@Override
										public String getTaglibLocation() {
											return location;
										}
									});
								}
							}
						}
					}
				}
				return result;
			}
			
			@Override
			public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
				return Collections.emptyList();
			}
		};
	}


	@Override
	public void declareRoles(String... roleNames) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getVirtualServerName() {
		return "localhost";
	}


	@Override
	public Dynamic addJspFile(String servletName, String jspFile) {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getSessionTimeout() {
		return 0;
	}


	@Override
	public void setSessionTimeout(int sessionTimeout) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getRequestCharacterEncoding() {
		return "utf-8";
	}


	@Override
	public void setRequestCharacterEncoding(String encoding) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getResponseCharacterEncoding() {
		return "utf-8";
	}


	@Override
	public void setResponseCharacterEncoding(String encoding) {
		throw new UnsupportedOperationException();
	}
}
