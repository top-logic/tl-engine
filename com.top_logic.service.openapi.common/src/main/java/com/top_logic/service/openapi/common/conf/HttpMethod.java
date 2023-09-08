/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.conf;

import java.util.Locale;
import java.util.Objects;

/**
 * A HTTP method.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum HttpMethod {

	/**
	 * The <code>GET</code> method means retrieve whatever information (in the form of an entity) is
	 * identified by the Request-URI. If the Request-URI refers to a data-producing process, it is
	 * the produced data which shall be returned as the entity in the response and not the source
	 * text of the process, unless that text happens to be the output of the process.
	 */
	GET,

	/**
	 * The <code>HEAD</code> method is identical to GET except that the server MUST NOT return a
	 * message-body in the response. The metainformation contained in the HTTP headers in response
	 * to a HEAD request SHOULD be identical to the information sent in response to a GET request.
	 * This method can be used for obtaining metainformation about the entity implied by the request
	 * without transferring the entity-body itself. This method is often used for testing hypertext
	 * links for validity, accessibility, and recent modification.
	 */
	HEAD,

	/**
	 * The <code>PUT</code> method requests that the enclosed entity be stored under the supplied
	 * Request-URI. If the Request-URI refers to an already existing resource, the enclosed entity
	 * SHOULD be considered as a modified version of the one residing on the origin server. If the
	 * Request-URI does not point to an existing resource, and that URI is capable of being defined
	 * as a new resource by the requesting user agent, the origin server can create the resource
	 * with that URI. If a new resource is created, the origin server MUST inform the user agent via
	 * the 201 (Created) response. If an existing resource is modified, either the 200 (OK) or 204
	 * (No Content) response codes SHOULD be sent to indicate successful completion of the request.
	 * If the resource could not be created or modified with the Request-URI, an appropriate error
	 * response SHOULD be given that reflects the nature of the problem. The recipient of the entity
	 * MUST NOT ignore any Content-* (e.g. Content-Range) headers that it does not understand or
	 * implement and MUST return a 501 (Not Implemented) response in such cases.
	 */
	PUT {

		@Override
		public boolean supportsBody() {
			return true;
		}

	},

	/**
	 * The <code>POST</code> method is used to request that the origin server accept the entity
	 * enclosed in the request as a new subordinate of the resource identified by the Request-URI in
	 * the Request-Line. POST is designed to allow a uniform method to cover the following
	 * functions:
	 * 
	 * <ul>
	 * <li>Annotation of existing resources;</li>
	 * 
	 * <li>Posting a message to a bulletin board, newsgroup, mailing list, or similar group of
	 * articles;</li>
	 * 
	 * <li>Providing a block of data, such as the result of submitting a form, to a data-handling
	 * process;</li>
	 * 
	 * <li>Extending a database through an append operation.</li>
	 * </ul>
	 * 
	 * <p>
	 * The actual function performed by the POST method is determined by the server and is usually
	 * dependent on the Request-URI. The posted entity is subordinate to that URI in the same way
	 * that a file is subordinate to a directory containing it, a news article is subordinate to a
	 * newsgroup to which it is posted, or a record is subordinate to a database.
	 * </p>
	 * 
	 * <p>
	 * The action performed by the POST method might not result in a resource that can be identified
	 * by a URI. In this case, either 200 (OK) or 204 (No Content) is the appropriate response
	 * status, depending on whether or not the response includes an entity that describes the
	 * result.
	 * </p>
	 */
	POST {

		@Override
		public boolean supportsBody() {
			return true;
		}

	},

	/**
	 * The <code>DELETE</code> method requests that the origin server delete the resource identified
	 * by the Request-URI. This method MAY be overridden by human intervention (or other means) on
	 * the origin server. The client cannot be guaranteed that the operation has been carried out,
	 * even if the status code returned from the origin server indicates that the action has been
	 * completed successfully. However, the server SHOULD NOT indicate success unless, at the time
	 * the response is given, it intends to delete the resource or move it to an inaccessible
	 * location.
	 */
	DELETE,

	/**
	 * The <code>OPTIONS</code> method represents a request for information about the communication
	 * options available on the request/response chain identified by the Request-URI. This method
	 * allows the client to determine the options and/or requirements associated with a resource, or
	 * the capabilities of a server, without implying a resource action or initiating a resource
	 * retrieval.
	 */
	OPTIONS {

		@Override
		public boolean supportsBody() {
			return true;
		}

	},

	/**
	 * The HTTP <code>PATCH</code> request method applies partial modifications to a resource.
	 * 
	 * <p>
	 * A <code>PATCH</code> request is considered a set of instructions on how to modify a resource.
	 * Contrast this with {@link #PUT}; which is a complete representation of a resource.
	 * </p>
	 * 
	 * <p>
	 * A <code>PATCH</code> is not necessarily idempotent, although it can be. Contrast this with
	 * {@link #PUT}; which is always idempotent. The word "idempotent" means that any number of
	 * repeated, identical requests will leave the resource in the same state. For example if an
	 * auto-incrementing counter field is an integral part of the resource, then a {@link #PUT} will
	 * naturally overwrite it (since it overwrites everything), but not necessarily so for
	 * <code>PATCH</code>.
	 * </p>
	 * 
	 * <p>
	 * <code>PATCH</code> (like {@link #POST}) may have side-effects on other resources.
	 * </p>
	 */
	PATCH,

	/**
	 * The <code>TRACE</code> method is used to invoke a remote, application-layer loop- back of the
	 * request message. The final recipient of the request SHOULD reflect the message received back
	 * to the client as the entity-body of a 200 (OK) response. The final recipient is either the
	 * origin server or the first proxy or gateway to receive a Max-Forwards value of zero (0) in
	 * the request (see section 14.31). A TRACE request MUST NOT include an entity.
	 */
	TRACE;

	/**
	 * Whether this method type supports a a request body.
	 * 
	 * <p>
	 * If a body is not allowed and a body was sent, the behaviour is either undefined or forces an
	 * error.
	 * </p>
	 * 
	 * @see "RFC7231"
	 */
	public boolean supportsBody() {
		return false;
	}

	/**
	 * Returns the {@link HttpMethod} for a normalized {@code value} of a method name.
	 *
	 * @param method
	 *        A method name like {@code "delete"}, {@code "DELETE"}, or any mixed-case variant.
	 * @return The {@link HttpMethod} for the given method name.
	 */
	public static HttpMethod normalizedValueOf(final String method) {
		return valueOf(Objects.requireNonNull(method, "method").toUpperCase(Locale.ROOT));
	}

}
