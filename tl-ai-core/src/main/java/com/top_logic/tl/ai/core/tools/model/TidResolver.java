/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;

/**
 * Resolves the {@code "TypeName:id"} identifiers that tools exchange with agents into
 * {@link TLObject}s.
 *
 * <p>
 * The format is the external form of an object's {@link TLObject#tId() object key}, so anything a
 * tool reported as a TID can be fed back in. The optional {@code #branch} and {@code @revision}
 * suffixes an object key may carry are stripped: tools always operate on the current revision of
 * the trunk.
 * </p>
 */
public final class TidResolver {

	private TidResolver() {
		// Static utility.
	}

	/**
	 * Resolves the given TID.
	 *
	 * @param tid
	 *        The identifier in the format {@code "TypeName:id"}.
	 * @return The resolved object, never {@code null}.
	 * @throws IllegalArgumentException
	 *         If the TID is malformed or names no existing object.
	 */
	public static TLObject resolve(String tid) {
		if (tid == null || tid.isEmpty()) {
			throw new IllegalArgumentException("TID is required");
		}
		String[] split = tid.split(":", 2);
		if (split.length != 2) {
			throw new IllegalArgumentException(
				"Invalid TID format. Expected 'TypeName:id', got: " + tid);
		}
		TLID id = IdentifierUtil.fromExternalForm(stripQualifiers(split[1]));
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		KnowledgeObject ko = kb.getKnowledgeObject(split[0], id);
		if (ko == null) {
			throw new IllegalArgumentException("Object with TID '" + tid + "' not found");
		}
		return WrapperFactory.getWrapper(ko);
	}

	/**
	 * Cuts off the {@code #branch} / {@code @revision} suffixes of an object key's external form.
	 */
	private static String stripQualifiers(String id) {
		int end = id.length();
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (c == '#' || c == '@') {
				end = i;
				break;
			}
		}
		return id.substring(0, end);
	}
}
