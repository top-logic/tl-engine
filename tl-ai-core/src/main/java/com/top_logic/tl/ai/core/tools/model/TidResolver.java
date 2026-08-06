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
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;

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
 *
 * <p>
 * <b>Every resolution is a read check.</b> Which object a tool reaches is decided by a TID the
 * language model produced, so this is the point at which an untrusted identifier turns into a live
 * model object, and it is the only such point in this module. The {@link ModelSecurity} parameter is
 * therefore mandatory and there is no single-argument form: a tool cannot reach an object with the
 * run's identity without having said whose rights apply. What it then discloses about the object is
 * {@link ReadFilter}'s job.
 * </p>
 */
public final class TidResolver {

	private TidResolver() {
		// Static utility.
	}

	/**
	 * Resolves the given TID, if the principal may read what it names.
	 *
	 * <p>
	 * An object the principal may not read is reported exactly like one that does not exist.
	 * "It exists and you may not see it" would disclose its existence, which is the disclosure the
	 * read filter is there to prevent, so the two answers are deliberately spelled the same way.
	 * </p>
	 *
	 * @param tid
	 *        The identifier in the format {@code "TypeName:id"}.
	 * @param security
	 *        The rights of the run's principal.
	 * @return The resolved object, never {@code null}.
	 * @throws IllegalArgumentException
	 *         If the TID is malformed, names no existing object, or names one the principal may not
	 *         read.
	 */
	public static TLObject resolve(String tid, ModelSecurity security) {
		if (tid == null || tid.isEmpty()) {
			throw new IllegalArgumentException("TID is required");
		}
		if (security == null) {
			throw new IllegalStateException("Resolving a TID is a read check, and this call names no"
				+ " principal to check it for. Pass the ModelSecurity of the agent run.");
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
			throw new IllegalArgumentException(notFound(tid));
		}
		TLObject result = WrapperFactory.getWrapper(ko);
		if (!security.canRead(result)) {
			throw new IllegalArgumentException(notFound(tid));
		}
		return result;
	}

	/**
	 * The one answer for "no such object" and "not yours to see".
	 */
	private static String notFound(String tid) {
		return "Object with TID '" + tid + "' not found";
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
