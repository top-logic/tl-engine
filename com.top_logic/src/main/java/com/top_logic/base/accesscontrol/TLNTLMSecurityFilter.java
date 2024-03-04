/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import jcifs.http.NtlmHttpFilter;

import com.top_logic.basic.StringServices;

/**
 * Filter requests and handle NTLM authentication.
 * 
 * NTLM is a Windows-based authentication mechanism that allows for single-sign-on of users via the
 * web browser in Windows platforms. It is built-in in Internet Explorer and can also be used in
 * current versions of Chrome, and Firefox (needs configuration entry
 * network.automatic-ntlm-auth.trusted-uris set to the server URL via about:config) Theoretically we
 * should only have to protect the URL /loginSSO with the filter. But since the browser (at least
 * IE) send a challenge request after a certain idle time (something like 60sec) we also have to
 * intercept these requests.
 * 
 * NOTE: because of the reasons explained above, we have to set the following in the web.xml to make
 * NTLM SSO work: - the 'filter-mapping' of this filter must be set to '/*' so that the filter gets
 * ALL requests - the 'init-param' 'tl.security.sso_url' of this filter has to be the same as the
 * 'url-pattern' of the 'loginExternal' servlet
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TLNTLMSecurityFilter extends NtlmHttpFilter {

	/** Init param in web.xml to set the SSO servlet URL. */
	private static final String SSO_URL = "tl.security.sso_url";

	/** Address of SSO servlet */
	private String protectedURL;
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		super.init(arg0);
		
		this.protectedURL = arg0.getInitParameter(SSO_URL);
		if (StringServices.isEmpty(this.protectedURL)) {
			this.protectedURL = "loginSSO";
		}
	}
	
    /**
     * Separate NTML from <i>TopLogic</i> requests
     * 
     * @see jcifs.http.NtlmHttpFilter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
        if (!this.isNTML(request, response)) {
            chain.doFilter(request, response);
        } else {
            super.doFilter(request, response, chain);
        }
    }

	/**
	 * Check if the given request is an NTLM request
	 * 
	 * @param request		the request
	 * @param response		the response
	 * @return true if the request is an NTLM request
	 */
	private boolean isNTML(ServletRequest request, ServletResponse response) {
		HttpServletRequest theRequest = (HttpServletRequest)request;
		boolean isSSO = theRequest.getRequestURI().indexOf(this.protectedURL) >= 0;
		if (!isSSO) {
			return theRequest.getHeader("authorization") != null || theRequest.getHeader("WWW-Authenticate") != null; //"NTLM)
		}
		return true;
	}
	
}
