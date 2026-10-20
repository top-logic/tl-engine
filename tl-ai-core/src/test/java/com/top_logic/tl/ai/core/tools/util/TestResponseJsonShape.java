/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.util;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.model.messages.ErrorResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ModelPartResponse;
import com.top_logic.tl.ai.core.tools.model.messages.SearchResponse;

/**
 * What the model actually reads when a tool answers with one of the folded response messages.
 *
 * <p>
 * Six byte-identical model-editor responses became one {@link ModelPartResponse} and two one-field
 * error messages became one {@link ErrorResponse}. That is only free because
 * {@link DataClassSerializer} writes fields and nothing else - no type tag, no message name - so a
 * renamed message is invisible to the model. This pins that: the day the serializer starts emitting
 * a discriminator, six tool answers change their shape at once, and this test says so before a
 * model does.
 * </p>
 */
public class TestResponseJsonShape extends TestCase {

	/** The answer to a created model part, whatever kind of part it was. */
	public void testModelPartResponseCarriesNoTypeTag() {
		ModelPartResponse response = ModelPartResponse.create();
		response.setSuccess(true);
		response.setName("Project");
		response.setTid("pm:Project");
		response.setCreated(true);

		assertEquals("{\"success\":true,\"name\":\"Project\",\"tid\":\"pm:Project\",\"created\":true}",
			DataClassSerializer.toJson(response, TestResponseJsonShape.class, "createClass()"));
	}

	/** The one error shape the model-editor, the model-browser and the TL-Script tools all answer with. */
	public void testErrorResponseCarriesNoTypeTag() {
		ErrorResponse response = ErrorResponse.create();
		response.setError("Module not found: nope");

		assertEquals("{\"error\":\"Module not found: nope\"}",
			DataClassSerializer.toJson(response, TestResponseJsonShape.class, "listTypes()"));
	}

	/**
	 * A search that ran and a search that did not, told apart by the error alone.
	 *
	 * <p>
	 * {@code SearchResponse} was the one message of all of them carrying both {@code success} and
	 * {@code error}. Finding nothing is not a failure, so an empty {@code error} is what says the
	 * search ran.
	 * </p>
	 */
	public void testSearchResponseSaysItFailedByTheErrorAlone() {
		SearchResponse empty = SearchResponse.create();
		empty.setResultCount(0);

		assertEquals("{\"resultCount\":0,\"results\":[],\"error\":\"\"}",
			DataClassSerializer.toJson(empty, TestResponseJsonShape.class, "searchModel()"));

		SearchResponse failed = SearchResponse.create();
		failed.setError("Search failed: boom");

		assertEquals("{\"resultCount\":0,\"results\":[],\"error\":\"Search failed: boom\"}",
			DataClassSerializer.toJson(failed, TestResponseJsonShape.class, "searchModel()"));
	}
}
