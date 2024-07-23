/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.StringArrayJsonBinding;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;

/**
 * Describes a single API operation on a path.
 * 
 * @see OpenapiDocument
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	OperationObject.SUMMARY,
	OperationObject.DESCRIPTION,
	OperationObject.TAGS,
	WithParameters.PARAMETERS,
	OperationObject.REQUEST_BODY,
	OperationObject.RESPONSES,
	WithSecurity.SECURITY,
	OperationObject.X_TL_IMPLEMENTATION,
})
public interface OperationObject extends Described, WithSecurity, WithParameters {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for the value of {@link #getOperationId()}. */
	String OPERATION_ID = "operationId";

	/** Configuration name for the value of {@link #getResponses()}. */
	String RESPONSES = "responses";

	/** Configuration name for the value of {@link #getSummary()}. */
	String SUMMARY = "summary";

	/** Configuration name for the value of {@link #getRequestBody()}. */
	String REQUEST_BODY = "requestBody";

	/** Configuration name for the value of {@link #getImplementation()}. */
	String X_TL_IMPLEMENTATION = "x-tl-implementation";

	/** Configuration name for the value of {@link #getTags()}. */
	String TAGS = "tags";

	/**
	 * A short summary of what the operation does.
	 */
	@Name(SUMMARY)
	String getSummary();

	/**
	 * Setter for {@link #getSummary()}.
	 */
	void setSummary(String value);

	/**
	 * Unique string used to identify the operation. The id MUST be unique among all operations
	 * described in the API. The <i>operationId</i> value is case-sensitive. Tools and
	 * libraries MAY use the The <i>operationId</i> to uniquely identify an operation,
	 * therefore, it is RECOMMENDED to follow common programming naming conventions.
	 */
	@Name(OPERATION_ID)
	String getOperationId();

	/**
	 * Setter for {@link #getOperationId()}.
	 */
	void setOperationId(String value);

	/**
	 * A list of tags for API documentation control. Tags can be used for logical grouping of
	 * operations by resources or any other qualifier.
	 */
	@JsonBinding(StringArrayJsonBinding.class)
	@Name(TAGS)
	String[] getTags();

	/**
	 * Setter for {@link #getTags()}.
	 */
	void setTags(String[] value);

	/**
	 * A list of parameters that are applicable for this operation.
	 * 
	 * <p>
	 * If a parameter is already defined at the Path Item, the new definition will override it but
	 * can never remove it. The list MUST NOT include duplicated parameters. A unique parameter is
	 * defined by a combination of a name and location. The list can use the Reference Object to
	 * link to parameters that are defined at the <i>OpenAPI</i> Object`s
	 * components/parameters.
	 * </p>
	 */
	@Override
	List<IParameterObject> getParameters();

	/**
	 * A declaration of which security mechanisms can be used for this operation.
	 * 
	 * <p>
	 * The list of values includes alternative security requirement objects that can be used. Only
	 * one of the security requirement objects need to be satisfied to authorize a request. To make
	 * security optional, an empty security requirement ({}) can be included in the array. This
	 * definition overrides any declared top-level security. To remove a top-level security
	 * declaration, an empty array can be used.
	 * </p>
	 */
	@Override
	List<Map<String, List<String>>> getSecurity();

	/**
	 * The list of possible responses as they are returned from executing this operation.
	 */
	@Name(RESPONSES)
	@Key(ResponsesObject.STATUS_CODE)
	@Mandatory
	Map<String, ResponsesObject> getResponses();

	/**
	 * The request body applicable for this operation. The <i>requestBody</i> is only
	 * supported in HTTP methods where the HTTP 1.1 specification [RFC7231] has explicitly defined
	 * semantics for request bodies. In other cases where the HTTP spec is vague,
	 * <i>requestBody</i> SHALL be ignored by consumers.
	 */
	@Name(REQUEST_BODY)
	RequestBodyObject getRequestBody();

	/**
	 * Setter for {@link #getRequestBody()}.
	 */
	void setRequestBody(RequestBodyObject value);

	/**
	 * Implementation for this operation.
	 * 
	 * <p>
	 * It is expected that the value is the serialized configuration of this operation.
	 * </p>
	 */
	@Nullable
	@Name(X_TL_IMPLEMENTATION)
	@Label("Implementation")
	String getImplementation();

	/**
	 * Setter for {@link #getImplementation()}.
	 */
	void setImplementation(String value);

	/**
	 * Stores the given implementation to this {@link OperationObject}.
	 * 
	 * @param impl
	 *        The implementation to store. May be <code>null</code>.
	 * @param storePretty
	 *        Whether the value should be stored pretty.
	 * 
	 * @implNote Serialises the given implementation and store it to
	 *           {@link #setImplementation(String)}.
	 * 
	 * @see #setImplementation(String)
	 * @see #getImpl()
	 */
	default void setImpl(PolymorphicConfiguration<?> impl, boolean storePretty) {
		if (impl == null) {
			setImplementation(null);
			return;
		}
		StringWriter out = new StringWriter();
		try {
			try (ConfigurationWriter configurationWriter = new ConfigurationWriter(out)) {
				configurationWriter.setXMLHeaderWriting(false);
				ConfigurationDescriptor baseType =
					TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class);
				configurationWriter.write("impl", baseType, impl);
			}
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
		String result = out.toString();
		if (storePretty) {
			Config config = XMLPrettyPrinter.newConfiguration();
			config.setIndentChar(' ');
			config.setIndentStep(2);
			config.setXMLHeader(false);
			result = XMLPrettyPrinter.prettyPrint(config, result);
		}
		setImplementation(result);
	}

	/**
	 * Reads the stored implementation from this {@link OperationObject}.
	 * 
	 * @implNote De-serialises the implementation read from {@link #getImplementation()}.
	 * 
	 * @see #getImplementation()
	 * @see #setImpl(PolymorphicConfiguration, boolean)
	 * 
	 * @throws ConfigurationException
	 *         When the stored implementation can not be parsed to a
	 *         {@link PolymorphicConfiguration}.
	 */
	default PolymorphicConfiguration<?> getImpl() throws ConfigurationException {
		String spec = getImplementation();
		if (spec == null) {
			return null;
		}
		return TypedConfiguration.parse("impl", PolymorphicConfiguration.class, CharacterContents.newContent(spec));
	}

}

