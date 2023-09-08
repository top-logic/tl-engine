/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Produces a {@link CallBuilder} constructing a request body in JSON format.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSONRequestBody extends AbstractConfiguredInstance<JSONRequestBody.Config<?>> implements CallBuilderFactory {

	/**
	 * Configuration options for {@link JSONRequestBody}.
	 */
	@TagName("json-body")
	public interface Config<I extends JSONRequestBody> extends CallBuilderFactory.Config<I> {

		/** Configuration name of {@link #getJson()}. */
		String JSON = "json";

		/**
		 * Expression constructing a JSON value.
		 * 
		 * <p>
		 * The expression has access to all parameter values that are implicitly defined as
		 * TL-Script variables.
		 * </p>
		 * 
		 * <p>
		 * If no expression is given, a JSON object is constructed that contains a property for each
		 * function parameter. Each property gets the value assigned that is passed as argument to
		 * the TL-Script function for the corresponding parameter.
		 * </p>
		 */
		@PropertyEditor(PlainEditor.class)
		Expr getJson();

		/**
		 * Setter for {@link #getJson()}.
		 */
		void setJson(Expr value);
	}

	/**
	 * Creates a {@link JSONRequestBody} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public JSONRequestBody(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public CallBuilder createRequestModifier(MethodSpec method) {
		List<String> parameters = method.getParameterNames();

		Expr expr = getConfig().getJson();
		if (expr == null) {
			return new JsonBodyBuilder() {
				@Override
				protected String jsonBody(ClassicHttpRequest request, Call call) {
					Map<String, Object> value = new HashMap<>();
					for (int n = 0, cnt = parameters.size(); n < cnt; n++) {
						value.put(parameters.get(n), call.getArgument(n));
					}
					return JSON.toString(value);
				}
			};
		} else {
			// Note: The last argument is passed to the innermost function. Therefore, the lambdas
			// must
			// be constructed in reverse order.
			for (int n = parameters.size() - 1; n >= 0; n--) {
				expr = lambda(parameters.get(n), expr);
			}

			QueryExecutor json = QueryExecutor.compile(expr);

			return new JsonBodyBuilder() {
				@Override
				protected String jsonBody(ClassicHttpRequest request, Call call) {
					Object[] args = new Object[parameters.size()];
					for (int n = 0, cnt = parameters.size(); n < cnt; n++) {
						args[n] = call.getArgument(n);
					}
					Object value = json.execute(args);
					return JSON.toString(value);
				}
			};
		}
	}

	private static Expr lambda(String param, Expr expr) {
		Define result = TypedConfiguration.newConfigItem(Define.class);
		result.setName(param);
		result.setExpr(expr);
		return result;
	}

	private abstract static class JsonBodyBuilder implements CallBuilder {

		private static final String CONTENT_TYPE_HEADER = "content-type";

		@Override
		public void buildRequest(ClassicHttpRequest request, Call call) {
			HttpEntity body = new StringEntity(jsonBody(request, call));
			request.setEntity(body);
			ensureContentType(request);
		}

		private void ensureContentType(ClassicHttpRequest request) {
			/* Ensure that content type is set if not yet present. If a later CallBuilder wants to
			 * set the "content-type" header, our setting will be overridden. */
			Header[] headers = request.getHeaders(CONTENT_TYPE_HEADER);
			if (headers == null || headers.length == 0) {
				request.setHeader(CONTENT_TYPE_HEADER, JsonUtilities.JSON_CONTENT_TYPE);
			}
		}

		/**
		 * Creates the actual body string.
		 */
		protected abstract String jsonBody(ClassicHttpRequest request, Call call);

	}
}
