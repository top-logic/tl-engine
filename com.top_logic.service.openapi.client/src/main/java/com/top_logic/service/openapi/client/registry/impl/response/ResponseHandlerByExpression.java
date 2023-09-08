/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.client.registry.conf.InParameterContext;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerByExpression.Config.ResponseFormat;
import com.top_logic.service.openapi.client.registry.impl.value.ParameterValue;

/**
 * {@link ResponseHandlerFactory} reading the response body in a configured format and post
 * processes the answer using an {@link Expr}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("TL-Script")
public class ResponseHandlerByExpression extends AbstractConfiguredInstance<ResponseHandlerByExpression.Config<?>> implements ResponseHandlerFactory {

	/**
	 * Configuration options for {@link ResponseHandlerByExpression}.
	 */
	@DisplayOrder({
		Config.FORMAT,
		Config.ADDITIONAL_ARGUMENTS,
		Config.POST_PROCESSING
	})
	public interface Config<I extends ResponseHandlerByExpression> extends PolymorphicConfiguration<I>, InParameterContext {

		/** Configuration name for {@link #getPostProcessing()}. */
		String POST_PROCESSING = "post-processing";

		/** Configuration name for {@link #getFormat()}. */
		String FORMAT = "format";

		/** Configuration name for {@link #getAdditionalArguments()}. */
		String ADDITIONAL_ARGUMENTS = "additional-arguments";

		/**
		 * The format in which the response must be processed.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		enum ResponseFormat {

			/** The response is parsed as JSON. */
			JSON,

			/** The response is processed as text. */
			TEXT,

			/** The response is processed as binary content. */
			BINARY;
		}

		/**
		 * Additional arguments passed to {@link #getPostProcessing()}.
		 */
		@ImplementationClassDefault(ParameterValue.class)
		@Name(ADDITIONAL_ARGUMENTS)
		@DisplayMinimized
		List<PolymorphicConfiguration<? extends ResponseExtractorFactory>> getAdditionalArguments();

		/**
		 * Function computing the actual result of the service method invocation.
		 * 
		 * <p>
		 * The function expects the response object in the configured {@link #getFormat()} from the
		 * external API as first argument. If {@link #getAdditionalArguments()} are configured they
		 * are also passed to this function.
		 * </p>
		 * 
		 * <p>
		 * If no function is specified, the formatted response is returned directly.
		 * </p>
		 */
		@Name(POST_PROCESSING)
		Expr getPostProcessing();

		/**
		 * The format in which the content of the message must be processed.
		 */
		@Name(FORMAT)
		ResponseFormat getFormat();
	}

	/**
	 * Creates a {@link ResponseHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResponseHandlerByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ResponseHandler create(MethodSpec method) {
		QueryExecutor postProcessor = QueryExecutor.compileOptional(getConfig().getPostProcessing());
		List<ResponseExtractor> arguments =
			TypedConfigUtil.createInstanceList(getConfig().getAdditionalArguments())
				.stream().map(x -> x.newExtractor(method)).collect(Collectors.toList());

		return new Handler(postProcessor, arguments, getConfig().getFormat());
	}

	/**
	 * Implementation of {@link ResponseHandlerByExpression}.
	 */
	protected class Handler implements ResponseHandler {
		private QueryExecutor _postProcessor;

		private List<ResponseExtractor> _arguments;

		private ResponseFormat _format;

		/**
		 * Creates a {@link Handler}.
		 */
		public Handler(QueryExecutor postProcessor, List<ResponseExtractor> arguments, ResponseFormat format) {
			_postProcessor = postProcessor;
			_arguments = arguments;
			_format = format;
		}

		@Override
		public Object handle(MethodDefinition method, Call call, ClassicHttpResponse response) throws Exception {
			HttpEntity entity = response.getEntity();
			Object result = "";
			switch (_format) {
				case JSON:
					if (entity == null) {
						result = new HashMap<>();
					} else {
						String body = EntityUtils.toString(entity);
						result = JSON.fromString(body);
					}
					break;
				case BINARY:
					if (entity == null) {
						result = ArrayUtil.EMPTY_BYTE_ARRAY;
					} else {
						result = EntityUtils.toByteArray(entity);
					}
					break;
				case TEXT:
					if (entity == null) {
						result = StringServices.EMPTY_STRING;
					} else {
						result = EntityUtils.toString(entity);
					}
					break;
			}

			if (_postProcessor != null) {
				int cnt = _arguments.size();
				if (cnt > 0) {
					Object[] arguments = new Object[cnt + 1];
					arguments[0] = result;
					for (int n = 0; n < cnt; n++) {
						arguments[n + 1] = _arguments.get(n).extract(call, response);
					}
					result = _postProcessor.execute(arguments);
				} else {
					result = _postProcessor.execute(result);
				}
			}

			return result;
		}
	}

}
