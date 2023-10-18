/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Renderer} that renders the content of the error dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ErrorRenderer implements Renderer<HandlerResult> {

	/**
	 * Pattern that describes a line break.
	 * 
	 * <p>
	 * Note: For compatibility, unencoded, or HTML encoded line breaks are accepted.
	 * </p>
	 * 
	 * @see ErrorRenderUtil#NEWLINE_PATTERN
	 */
	protected static final Pattern NEWLINE_PATTERN = ErrorRenderUtil.NEWLINE_PATTERN;

	/**
	 * Creates a {@link ErrorRenderer}.
	 * 
	 * @param context
	 *        context to instantiate sub configurations.
	 * @param config
	 *        configuration of the render
	 */
	public ErrorRenderer(InstantiationContext context, PolymorphicConfiguration<? extends Renderer<?>> config) {
		// no special here
	}

	/**
	 * Renders the errors of the given {@link HandlerResult}
	 */
	@Override
	public void write(DisplayContext context, TagWriter out, HandlerResult handlerResult) throws IOException {
		I18NRuntimeException exception = handlerResult.getException();
		HTMLFragment errorMessageDescription = createErrorDescription(handlerResult, context.getResources(), exception);
		errorMessageDescription.write(context, out);
	}

	private HTMLFragment createErrorDescription(HandlerResult result, Resources resources, I18NRuntimeException exception) {
		List<HTMLFragment> description = new LinkedList<>();

		getErrorSummary(result, resources, exception).ifPresent(summary -> description.add(summary));
		getExceptionCauses(resources, exception).ifPresent(cause -> description.add(cause));
		getEncodedErrors(result, resources).ifPresent(encodedErrors -> description.add(encodedErrors));

		return Fragments.concat(description.toArray(new HTMLFragment[description.size()]));
	}

	private Optional<HTMLFragment> getErrorSummary(HandlerResult result, Resources resources, I18NRuntimeException exception) {
		Optional<ResKey> summary = getErrorSummaryInternal(result, exception);

		return summary.flatMap(summaryKey -> createLinesFragment(resources.getString(summaryKey)));
	}

	private Optional<ResKey> getErrorSummaryInternal(HandlerResult result, I18NRuntimeException exception) {
		if (exception != null) {
			return Optional.ofNullable(exception.getDetails());
		}

		return Optional.ofNullable(result.getErrorMessage());
	}

	private Optional<HTMLFragment> getEncodedErrors(HandlerResult result, Resources resources) {
		List<ResKey> encodedErrors = result.getEncodedErrors();

		if (!encodedErrors.isEmpty()) {
			return getEncodedErrorsInternal(resources, encodedErrors);
		}

		return Optional.empty();
	}

	private Optional<HTMLFragment> getEncodedErrorsInternal(Resources resources, List<ResKey> encodedErrorKeys) {
		List<HTMLFragment> encodedErrors = new LinkedList<>();

		for (ResKey encodedErrorMessage : encodedErrorKeys) {
			Optional<HTMLFragment> lineFragment = createLinesFragment(resources.getString(encodedErrorMessage));

			lineFragment.ifPresent(fragment -> encodedErrors.add(Fragments.li(fragment)));
		}

		return Optional.of(Fragments.ul(encodedErrors.toArray(new HTMLFragment[encodedErrors.size()])));
	}

	private Collection<HTMLFragment> getExceptionCausesInternal(Resources resources, Throwable exception) {
		if (exception != null) {
			Throwable cause = exception.getCause();

			if (cause != null && cause != exception && !(cause instanceof StackTrace)) {
				return createExceptionCauseMessages(resources, cause);
			}
		}

		return Collections.emptySet();
	}

	private Collection<HTMLFragment> createExceptionCauseMessages(Resources resources, Throwable cause) {
		List<HTMLFragment> exceptionCauses = new LinkedList<>();

		Optional<HTMLFragment> lineFragment = createLinesFragment(getCauseMessage(resources, cause));

		lineFragment.ifPresent(fragment -> exceptionCauses.add(fragment));
		exceptionCauses.addAll(getExceptionCausesInternal(resources, cause));

		return exceptionCauses;
	}

	private Optional<HTMLFragment> getExceptionCauses(Resources resources, Throwable exception) {
		if (exception != null) {
			List<HTMLFragment> causes = new ArrayList<>();

			Throwable baseException = exception;
			do {
				Throwable cause = baseException.getCause();

				if (cause != null && cause != baseException) {
					createLinesFragment(getCauseMessage(resources, cause)).ifPresent(causes::add);
					baseException = cause;
				} else {
					break;
				}
			} while (true);
			switch (causes.size()) {
				case 0: {
					return Optional.empty();
				}
				case 1: {
					return Optional.of(causes.get(0));
				}
				default: {
					return Optional.of(Fragments.ul(causes.stream().map(Fragments::li).toArray(HTMLFragment[]::new)));
				}
			}
		}

		return Optional.empty();
	}

	private String getCauseMessage(Resources resources, Throwable cause) {
		if (cause instanceof I18NFailure) {
			return getI18NFailureMessage(resources, (I18NFailure) cause);
		} else {
			return getDefaultCauseMessage(cause);
		}
	}

	private String getDefaultCauseMessage(Throwable cause) {
		String innerMessage = cause.getMessage();

		if (StringServices.isEmpty(innerMessage)) {
			return cause.getClass().getSimpleName();
		}

		return innerMessage;
	}

	private String getI18NFailureMessage(Resources resources, I18NFailure i18nFailure) {
		String message = resources.getString(i18nFailure.getErrorKey());

		if (message == null) {
			return resources.getString(I18NConstants.NO_EXCEPTION_MESSAGE);
		}

		return message;
	}

	private Optional<HTMLFragment> createLinesFragment(String text) {
		if (!StringServices.isEmpty(text)) {
			List<HTMLFragment> lineFragments = createSplittedLineFragments(text);

			return Optional.of(Fragments.p(lineFragments.toArray(new HTMLFragment[lineFragments.size()])));
		}

		return Optional.empty();
	}

	private List<HTMLFragment> createSplittedLineFragments(String text) {
		List<HTMLFragment> lineFragments = new LinkedList<>();

		String[] textByLineBreak = NEWLINE_PATTERN.split(text);

		for (int i = 0; i < textByLineBreak.length; i++) {
			if (i != 0) {
				lineFragments.add(Fragments.br());
			}

			lineFragments.add(Fragments.text(textByLineBreak[i]));
		}

		return lineFragments;
	}

}

