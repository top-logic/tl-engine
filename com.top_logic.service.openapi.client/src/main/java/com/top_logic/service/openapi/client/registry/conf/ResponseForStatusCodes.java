/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.client.registry.impl.response.HTTPStatusCodes;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandler;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerByExpression;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerFactory;

/**
 * Configuration of a {@link ResponseHandler} to use for a given subset of status codes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ResponseForStatusCodes.STATUS_CODES,
	ResponseForStatusCodes.RESPONSE_HANDLER,
})
public interface ResponseForStatusCodes extends InParameterContext {

	/** Configuration name for {@link #getStatusCodes()}. */
	String STATUS_CODES = "status-codes";

	/** Configuration name for {@link #getResponseHandler()}. */
	String RESPONSE_HANDLER = "response-handler";

	/**
	 * The status codes of the response for which the {@link #getResponseHandler()} is to be
	 * applied. The value is a list of concrete status codes (e.g. 202, 503) or ranges of status
	 * codes ({@value HTTPStatusCodes#STATUS_CODE_RANGE_INFORMATIONAL},
	 * {@value HTTPStatusCodes#STATUS_CODE_RANGE_SUCCESS},
	 * {@value HTTPStatusCodes#STATUS_CODE_RANGE_REDIRECTION},
	 * {@value HTTPStatusCodes#STATUS_CODE_RANGE_CLIENT_ERROR}, or
	 * {@value HTTPStatusCodes#STATUS_CODE_RANGE_SERVER_ERROR}).
	 */
	@Mandatory
	@Name(STATUS_CODES)
	@Format(CommaSeparatedStrings.class)
	@Constraint(StatusCodesConstraint.class)
	List<String> getStatusCodes();

	/**
	 * Post-processor of the response to the API invocation, if the status code of the response is
	 * included in {@link #getStatusCodes()} .
	 */
	@Mandatory
	@Name(RESPONSE_HANDLER)
	@ImplementationClassDefault(ResponseHandlerByExpression.class)
	PolymorphicConfiguration<? extends ResponseHandlerFactory> getResponseHandler();

	/**
	 * Setter for {@link #getResponseHandler()}.
	 */
	void setResponseHandler(PolymorphicConfiguration<? extends ResponseHandlerFactory> value);

	/**
	 * {@link ValueConstraint} for list of status codes.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class StatusCodesConstraint extends ValueConstraint<List<String>> {

		/**
		 * Creates a new {@link StatusCodesConstraint}.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public StatusCodesConstraint() {
			super((Class) List.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<String>> propertyModel) {
			List<String> value = propertyModel.getValue();
			if (value.isEmpty()) {
				return;
			}
			codeLoop:
			for (String code : value) {
				switch (code) {
					case HTTPStatusCodes.STATUS_CODE_RANGE_INFORMATIONAL:
					case HTTPStatusCodes.STATUS_CODE_RANGE_SUCCESS:
					case HTTPStatusCodes.STATUS_CODE_RANGE_REDIRECTION:
					case HTTPStatusCodes.STATUS_CODE_RANGE_CLIENT_ERROR:
					case HTTPStatusCodes.STATUS_CODE_RANGE_SERVER_ERROR:
						break;
					default: {
						int asInt;
						try {
							asInt = Integer.parseInt(code);
						} catch (NumberFormatException ex) {
							propertyModel
								.setProblemDescription(I18NConstants.ERROR_INVALID_STATUS_CODE__CODE.fill(code));
							break codeLoop;
						}
						if (!HTTPStatusCodes.isStatusCode(asInt)) {
							propertyModel
								.setProblemDescription(I18NConstants.ERROR_INVALID_STATUS_CODE__CODE.fill(code));
						}
					}
				}
			}
		}

	}

}

