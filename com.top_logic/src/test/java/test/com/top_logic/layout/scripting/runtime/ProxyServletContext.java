/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Standard proxy implementation: Redirects all calls to the given "real" {@link ServletContext}.
 * Subclass this class and overwrite all methods that you have to change.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("deprecation")
class ProxyServletContext implements ServletContext {

	private final ServletContext impl;

	public ProxyServletContext(ServletContext impl) {
		this.impl = impl;
	}

	@Override
	public Object getAttribute(String name) {
		return impl.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return impl.getAttributeNames();
	}

	@Override
	public ServletContext getContext(String uripath) {
		return impl.getContext(uripath);
	}

	@Override
	public String getInitParameter(String name) {
		return impl.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return impl.getInitParameterNames();
	}

	@Override
	public int getMajorVersion() {
		return impl.getMajorVersion();
	}

	@Override
	public String getMimeType(String file) {
		return impl.getMimeType(file);
	}

	@Override
	public int getMinorVersion() {
		return impl.getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return impl.getNamedDispatcher(name);
	}

	@Override
	public String getRealPath(String path) {
		return impl.getRealPath(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return impl.getRequestDispatcher(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return impl.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return impl.getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return impl.getResourcePaths(path);
	}

	@Override
	public String getServerInfo() {
		return impl.getServerInfo();
	}

	@Override
	@Deprecated
	public Servlet getServlet(String name) throws ServletException {
		return impl.getServlet(name);
	}

	@Override
	public String getServletContextName() {
		return impl.getServletContextName();
	}

	@Override
	@Deprecated
	public Enumeration<String> getServletNames() {
		return impl.getServletNames();
	}

	@Override
	@Deprecated
	public Enumeration<Servlet> getServlets() {
		return impl.getServlets();
	}

	@Override
	public void log(String msg) {
		impl.log(msg);
	}

	@Override
	@Deprecated
	public void log(Exception exception, String msg) {
		impl.log(exception, msg);
	}

	@Override
	public void log(String exception, Throwable throwable) {
		impl.log(exception, throwable);
	}

	@Override
	public void removeAttribute(String name) {
		impl.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		impl.setAttribute(name, object);
	}

	@Override
	public String getContextPath() {
		return impl.getContextPath();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return impl.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return impl.getEffectiveMinorVersion();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return impl.setInitParameter(name, value);
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		return impl.addServlet(servletName, className);
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		return impl.addServlet(servletName, servlet);
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		return impl.addServlet(servletName, servletClass);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
		return impl.createServlet(c);
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return impl.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return impl.getServletRegistrations();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		return impl.addFilter(filterName, className);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		return impl.addFilter(filterName, filter);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		return impl.addFilter(filterName, filterClass);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
		return impl.createFilter(c);
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return impl.getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return impl.getFilterRegistrations();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return impl.getSessionCookieConfig();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) throws IllegalStateException,
			IllegalArgumentException {
		impl.setSessionTrackingModes(sessionTrackingModes);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return impl.getDefaultSessionTrackingModes();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return impl.getEffectiveSessionTrackingModes();
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		impl.addListener(listenerClass);
	}

	@Override
	public void addListener(String className) {
		impl.addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		impl.addListener(t);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
		return impl.createListener(c);
	}

	@Override
	public void declareRoles(String... roleNames) {
		impl.declareRoles(roleNames);
	}

	@Override
	public ClassLoader getClassLoader() {
		return impl.getClassLoader();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return impl.getJspConfigDescriptor();
	}

	@Override
	public String getVirtualServerName() {
		return impl.getVirtualServerName();
	}

	@Override
	public Dynamic addJspFile(String servletName, String jspFile) {
		return impl.addJspFile(servletName, jspFile);
	}

	@Override
	public int getSessionTimeout() {
		return impl.getSessionTimeout();
	}

	@Override
	public void setSessionTimeout(int sessionTimeout) {
		impl.setSessionTimeout(sessionTimeout);
	}

	@Override
	public String getRequestCharacterEncoding() {
		return impl.getRequestCharacterEncoding();
	}

	@Override
	public void setRequestCharacterEncoding(String encoding) {
		impl.setRequestCharacterEncoding(encoding);
	}

	@Override
	public String getResponseCharacterEncoding() {
		return impl.getResponseCharacterEncoding();
	}

	@Override
	public void setResponseCharacterEncoding(String encoding) {
		impl.setRequestCharacterEncoding(encoding);
	}

}
