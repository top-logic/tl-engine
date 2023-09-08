/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.List;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Describes the operations available on a single path. A Path Item MAY be empty, due to ACL
 * constraints. The path itself is still exposed to the documentation viewer but they will not know
 * which operations and parameters are available.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#path-item-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	PathItemObject.PATH,
	PathItemObject.GET,
	PathItemObject.PUT,
	PathItemObject.POST,
	PathItemObject.DELETE,
	PathItemObject.OPTIONS,
	PathItemObject.HEAD,
	PathItemObject.PATCH,
	PathItemObject.TRACE,
	WithServers.SERVERS,
	WithParameters.PARAMETERS,
})
public interface PathItemObject extends WithServers, WithParameters {

	/** Configuration name for the value of {@link #getPath()}. */
	String PATH = "path";

	/** Configuration name for the value of {@link #getGet()}. */
	String GET = "get";

	/** Configuration name for the value of {@link #getPut()}. */
	String PUT = "put";

	/** Configuration name for the value of {@link #getPost()}. */
	String POST = "post";

	/** Configuration name for the value of {@link #getDelete()}. */
	String DELETE = "delete";

	/** Configuration name for the value of {@link #getOptions()}. */
	String OPTIONS = "options";

	/** Configuration name for the value of {@link #getHead()}. */
	String HEAD = "head";

	/** Configuration name for the value of {@link #getPatch()}. */
	String PATCH = "patch";

	/** Configuration name for the value of {@link #getTrace()}. */
	String TRACE = "trace";

	/**
	 * The actual path.
	 */
	@Name(PATH)
	String getPath();

	/**
	 * Setter for {@link #getPath()}.
	 */
	void setPath(String value);

	/**
	 * A definition of a GET operation on this path.
	 */
	@Name(GET)
	OperationObject getGet();

	/**
	 * Setter for {@link #getGet()}.
	 */
	void setGet(OperationObject value);

	/**
	 * A definition of a PUT operation on this path.
	 */
	@Name(PUT)
	OperationObject getPut();

	/**
	 * Setter for {@link #getPut()}.
	 */
	void setPut(OperationObject value);

	/**
	 * A definition of a POST operation on this path.
	 */
	@Name(POST)
	OperationObject getPost();

	/**
	 * Setter for {@link #getPost()}.
	 */
	void setPost(OperationObject value);

	/**
	 * A definition of a DELETE operation on this path.
	 */
	@Name(DELETE)
	OperationObject getDelete();

	/**
	 * Setter for {@link #getDelete()}.
	 */
	void setDelete(OperationObject value);

	/**
	 * A definition of a OPTIONS operation on this path.
	 */
	@Name(OPTIONS)
	OperationObject getOptions();

	/**
	 * Setter for {@link #getOptions()}.
	 */
	void setOptions(OperationObject value);

	/**
	 * A definition of a HEAD operation on this path.
	 */
	@Name(HEAD)
	OperationObject getHead();

	/**
	 * Setter for {@link #getHead()}.
	 */
	void setHead(OperationObject value);

	/**
	 * A definition of a PATCH operation on this path.
	 */
	@Name(PATCH)
	OperationObject getPatch();

	/**
	 * Setter for {@link #getPatch()}.
	 */
	void setPatch(OperationObject value);

	/**
	 * A definition of a TRACE operation on this path.
	 */
	@Name(TRACE)
	OperationObject getTrace();

	/**
	 * Setter for {@link #getTrace()}.
	 */
	void setTrace(OperationObject value);

	/**
	 * A list of parameters that are applicable for all the operations described under this path.
	 * These parameters can be overridden at the operation level, but cannot be removed there. The
	 * list MUST NOT include duplicated parameters. A unique parameter is defined by a combination
	 * of a name and location.
	 */
	@Override
	List<IParameterObject> getParameters();

}

