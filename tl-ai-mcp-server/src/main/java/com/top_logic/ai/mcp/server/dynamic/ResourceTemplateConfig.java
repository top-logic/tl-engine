/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.dynamic;

import java.util.Map;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration for a dynamic MCP resource template that uses TL-Script expressions.
 *
 * <p>
 * Defines a resource template with static metadata (name, title, description, MIME type)
 * and dynamic content generation through a TL-Script expression. The script receives
 * parameters extracted from the resource URI template.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 * <pre>
 * &lt;resource-template class="com.top_logic.ai.mcp.server.ConfigurableResourceTemplate"
 *     uri-template="myapp://data/{itemId}"
 *     name="item-data-{itemId}"
 *     title="Item Data"
 *     description="Retrieve data for a specific item"
 *     mime-type="application/json"&gt;
 *     &lt;content&gt;
 *         &lt;!-- TL-Script expression to compute resource content --&gt;
 *         &lt;!-- Parameters from URI are available as variables --&gt;
 *         itemId -> $item.toJson()
 *     &lt;/content&gt;
 * &lt;/resource-template&gt;
 * </pre>
 *
 * @author Bernhard Haumacher
 */
@DisplayOrder({
	ResourceTemplateConfig.TITLE,
	ResourceTemplateConfig.DESCRIPTION,
	ResourceTemplateConfig.URI_TEMPLATE,
	ResourceTemplateConfig.NAME,
	ResourceTemplateConfig.CONTENT,
	ResourceTemplateConfig.MIME_TYPE,
})
public interface ResourceTemplateConfig extends PolymorphicConfiguration<ConfigurableResourceTemplate> {

	/**
	 * Configuration property name for URI template.
	 *
	 * @see #getUriTemplate()
	 */
	String URI_TEMPLATE = "uri-template";

	/**
	 * Configuration property name for resource name template.
	 *
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * Configuration property name for resource title.
	 *
	 * @see #getTitle()
	 */
	String TITLE = "title";

	/**
	 * Configuration property name for resource description.
	 *
	 * @see #getDescription()
	 */
	String DESCRIPTION = "description";

	/**
	 * Configuration property name for MIME type.
	 *
	 * @see #getMimeType()
	 */
	String MIME_TYPE = "mime-type";

	/**
	 * Configuration property name for content expression.
	 *
	 * @see #getContent()
	 */
	String CONTENT = "content";

	/**
	 * URI template with parameter placeholders.
	 *
	 * <p>
	 * The template uses the format {@code scheme://path/{param1}/{param2}} where parameters
	 * are enclosed in curly braces. These parameters will be extracted from the URI and passed
	 * to the TL-Script expression.
	 * </p>
	 *
	 * <p>
	 * Example: {@code "myapp://data/{itemId}"}
	 * </p>
	 */
	@Name(URI_TEMPLATE)
	@Mandatory
	String getUriTemplate();

	/**
	 * Resource name template.
	 *
	 * <p>
	 * Can include parameter placeholders like {@code "item-data-{itemId}"} which will be
	 * replaced with actual values from the URI.
	 * </p>
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * Human-readable title for the resource.
	 *
	 * <p>
	 * This is displayed to users/agents browsing available resources.
	 * </p>
	 */
	@Name(TITLE)
	@Nullable
	String getTitle();

	/**
	 * Description of what this resource provides.
	 *
	 * <p>
	 * Should explain what data the resource returns and what parameters it expects.
	 * </p>
	 */
	@Name(DESCRIPTION)
	@Nullable
	@ControlProvider(MultiLineText.class)
	String getDescription();

	/**
	 * MIME type of the resource content.
	 *
	 * <p>
	 * Common values:
	 * </p>
	 * <ul>
	 * <li>{@code "application/json"} - JSON data</li>
	 * <li>{@code "text/plain"} - Plain text</li>
	 * <li>{@code "text/html"} - HTML content</li>
	 * <li>{@code "text/markdown"} - Markdown content</li>
	 * </ul>
	 */
	@Name(MIME_TYPE)
	@Nullable
	String getMimeType();

	/**
	 * TL-Script expression to compute the resource content.
	 *
	 * <p>
	 * The expression has access to parameters extracted from the URI template as implicit
	 * variables. The expression is automatically wrapped in a function that declares parameters
	 * matching the URI template parameter names, making them available within the expression.
	 * </p>
	 *
	 * <p>
	 * For example, if the URI template is {@code "myapp://item/{itemId}/version/{version}"}
	 * and a request comes for {@code "myapp://item/12345/version/v2"}, the expression is
	 * executed as if wrapped in: {@code itemId -> version -> yourExpression}, where
	 * {@code $itemId} has value {@code "12345"} and {@code $version} has value {@code "v2"}.
	 * </p>
	 *
	 * <p>
	 * The expression can return different types, which are handled as follows:
	 * </p>
	 * <ul>
	 * <li>{@link java.util.Collection} - Each element is converted to a separate
	 * {@link io.modelcontextprotocol.spec.McpSchema.ResourceContents} item in the result list,
	 * allowing a single resource template to return multiple resource contents</li>
	 * <li>{@link com.top_logic.basic.io.binary.BinaryDataSource} - Content type is checked:
	 * text-based types (e.g., {@code text/*}, {@code application/json}, {@code application/xml})
	 * are served as text, while binary types (e.g., {@code application/pdf}, {@code image/*})
	 * are base64-encoded and served as blob</li>
	 * <li>{@link Map} - Serialized as JSON with {@code application/json} content type</li>
	 * <li>{@link com.top_logic.base.services.simpleajax.HTMLFragment} - Rendered to HTML string
	 * with {@code text/html} content type</li>
	 * <li>Any other value - Converted to string using {@code toString()} with
	 * {@code text/plain} content type</li>
	 * </ul>
	 *
	 * <p>
	 * If a MIME type is explicitly configured via {@link #getMimeType()}, it overrides the
	 * default content type for the return type.
	 * </p>
	 *
	 * <p>
	 * Example for single parameter (URI template: {@code "myapp://item/{itemId}"}):
	 * </p>
	 * <pre>
	 * $myService.getItem($itemId).toJson()
	 * </pre>
	 *
	 * <p>
	 * Example for multiple parameters (URI template:
	 * {@code "myapp://item/{itemId}/version/{version}"}):
	 * </p>
	 * <pre>
	 * $myService.getItem($itemId, $version).toJson()
	 * </pre>
	 *
	 * <p>
	 * Example returning a Map (will be serialized as JSON):
	 * </p>
	 * <pre>
	 * map("id" -> $itemId, "data" -> $myService.getItem($itemId))
	 * </pre>
	 */
	@Name(CONTENT)
	@Mandatory
	@PropertyEditor(PlainEditor.class)
	@Constraint(value = CheckWithTemplateParams.class, args = @Ref(URI_TEMPLATE))
	Expr getContent();
}
