/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package com.top_logic.util.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of <code>javax.servlet.Filter</code> used to compress the
 * ServletResponse if it is bigger than a threshold.
 * 
 * @author Amy Roh
 * @author Dmitri Valdin
 */
public class CompressionFilter implements Filter {

    /** Flag that will will disable compression wehne set to "false" */
    public static final String GZIP = "gzip";
    
	/**
	 * The filter configuration object we are associated with. If this value is
	 * null, this filter instance is not currently configured.
	 */
	// private FilterConfig config = null;

	/**
	 * Minimal Number of bytes before we actually start Compression. 
	 */
	public static final int MIN_THRESHOLD = 1024;

	/**
	 * The threshold number to compress
	 */
	protected int compressionThreshold;

	/**
	 * Place this filter into service.
	 * 
	 * @param filterConfig
	 *            The filter configuration object
	 */
	@Override
	public void init(FilterConfig filterConfig) {

        compressionThreshold = MIN_THRESHOLD;
		if (filterConfig != null) {
			
			String str = filterConfig.getInitParameter("compressionThreshold");
			if (str != null) {
				compressionThreshold = Integer.parseInt(str);
				if (compressionThreshold != 0 && compressionThreshold < MIN_THRESHOLD) {
					compressionThreshold = MIN_THRESHOLD;
				}
            }
        }
    }

	/**
	 * Take this filter out of service.
	 */
	@Override
	public void destroy() {
		// nothing to do here ---
	}

	/**
	 * The <code>doFilter</code> method of the Filter is called by the container each time a
	 * request/response pair is passed through the chain due to a client request for a resource at
	 * the end of the chain. The FilterChain passed into this method allows the Filter to pass on
	 * the request and response to the next entity in the chain.
	 * 
	 * <p>
	 * This method first examines the request to check whether the client support compression.
	 * </p>
	 * 
	 * <p>
	 * It simply just pass the request and response if there is no support for compression.
	 * </p>
	 * 
	 * <p>
	 * If the compression support is available, it creates a CompressionServletResponseWrapper
	 * object which compresses the content and modifies the header if the content length is big
	 * enough. It then invokes the next entity in the chain using the FilterChain object
	 * (<code>chain.doFilter()</code>),
	 * </p>
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		if (!supportsCompression((HttpServletRequest)request)) {
			chain.doFilter(request, response);
			return;
		}
		
		CompressionServletResponseWrapper wrappedResponse = new CompressionServletResponseWrapper(
				(HttpServletResponse) response);
		wrappedResponse.setCompressionThreshold(compressionThreshold);
		try {
			chain.doFilter(request, wrappedResponse);
		} finally {
			wrappedResponse.finishResponse();
		}
	}


	/**
	 * Set filter config This function is equivalent to init. 
     * 
     * may be Required by Weblogic 6.1
	 * 
	 * @param filterConfig The filter configuration object
	 */
    /*
	public void setFilterConfig(FilterConfig filterConfig) {
		init(filterConfig);
	}
	*/

    /** 
     * Check if given Request/Response supports compression.
     */
    public static boolean supportsCompression(HttpServletRequest request) {
        if ( ! "false".equals(request.getParameter(GZIP))) {
            Enumeration e = request.getHeaders("Accept-Encoding");
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                if (name.indexOf("gzip") != -1) {
                    return true;
                } 
            }
        }
        return false;
    }

}
