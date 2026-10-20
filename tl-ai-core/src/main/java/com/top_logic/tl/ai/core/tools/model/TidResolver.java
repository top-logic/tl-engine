/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
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
 * tool reported as a TID can be fed back in - including a whole {@code label [id:TID]} entry as a
 * text result writes it. The optional {@code #branch} and {@code @revision} suffixes an object key
 * may carry are stripped: tools always operate on the current revision of the trunk.
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
 *
 * <p>
 * <b>Every refusal is a diagnosis.</b> An identifier that does not resolve is classified by
 * {@link TidDiagnostics}, which says which kind of wrong identifier it is - a uuid, a printed
 * object, an unknown type, a code this application speaks - and what to use instead. The
 * classification runs on the error path only.
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
	 * read filter is there to prevent, so the two answers are deliberately spelled the same way -
	 * by the same call, so that they cannot drift apart.
	 * </p>
	 *
	 * @param tid
	 *        The identifier in the format {@code "TypeName:id"}.
	 * @param security
	 *        The rights of the run's principal.
	 * @return The resolved object, never {@code null}.
	 * @throws IllegalArgumentException
	 *         If the TID is malformed, names no existing object, or names one the principal may not
	 *         read. The message says which of those it is; see {@link TidDiagnostics}.
	 */
	public static TLObject resolve(String tid, ModelSecurity security) {
		if (tid == null || tid.isEmpty()) {
			throw new IllegalArgumentException(TidDiagnostics.explain(tid, TidDiagnostics.Types.NONE));
		}
		if (security == null) {
			throw new IllegalStateException("Resolving a TID is a read check, and this call names no"
				+ " principal to check it for. Pass the ModelSecurity of the agent run.");
		}
		String identifier = objectId(tid);
		String[] split = identifier.split(":", 2);
		if (split.length != 2) {
			// No knowledge base is asked for: an identifier without a colon has no type name to ask
			// about, and a malformed one must be answered where there is no persistency layer, too.
			throw new IllegalArgumentException(TidDiagnostics.explain(identifier, TidDiagnostics.Types.NONE));
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLID id;
		try {
			id = IdentifierUtil.fromExternalForm(TidDiagnostics.stripQualifiers(split[1]));
		} catch (RuntimeException ex) {
			// The engine's parser refused the part behind the colon - a NumberFormatException,
			// which no caller of this method is documented to catch.
			throw new IllegalArgumentException(diagnosis(identifier, kb), ex);
		}
		if (id == null) {
			throw new IllegalArgumentException(diagnosis(identifier, kb));
		}
		KnowledgeObject ko;
		try {
			ko = kb.getKnowledgeObject(split[0], id);
		} catch (RuntimeException ex) {
			if (types(kb).stateOf(split[0]) == TidDiagnostics.TypeState.INSTANTIABLE) {
				// The type exists and has instances, and the id parsed: whatever failed in the
				// lookup is this installation's problem, not a wrong identifier. Reporting it as
				// "not found" would hide an outage behind a sentence about the model's mistake.
				throw ex;
			}
			// An unknown type name or an abstract table: the model named the wrong thing, and it
			// is told which, rather than handed the engine's own exception.
			throw new IllegalArgumentException(diagnosis(identifier, kb), ex);
		}
		if (ko == null) {
			throw new IllegalArgumentException(diagnosis(identifier, kb));
		}
		TLObject result = WrapperFactory.getWrapper(ko);
		if (!security.canRead(result)) {
			throw new IllegalArgumentException(diagnosis(identifier, kb));
		}
		return result;
	}

	/**
	 * The identifier in something the model wrote, which may be a whole {@code label [id:TID]}.
	 *
	 * <p>
	 * That is how a tool result written as text names an object
	 * ({@link com.top_logic.tl.ai.core.security.ReadFilter#withId(String, String)}), so a model that
	 * copies the entry it just read back into a tool call is right rather than wrong. Anything
	 * without the marker is returned trimmed and otherwise unchanged - a bare TID is what every
	 * JSON result carries.
	 * </p>
	 *
	 * @param value
	 *        What the model named. Must not be <code>null</code>.
	 * @return The identifier part; never <code>null</code>.
	 */
	public static String objectId(String value) {
		int start = value.indexOf("[id:");
		if (start < 0) {
			return value.trim();
		}
		int end = value.indexOf(']', start);
		return (end < 0 ? value.substring(start + 4) : value.substring(start + 4, end)).trim();
	}

	/**
	 * The one answer for every identifier that does not resolve - a malformed one, one naming no
	 * object, and one naming an object that is not this principal's to see.
	 *
	 * <p>
	 * One method, called from every refusing branch, so that "not yours to see" and "no such
	 * object" are not two texts that a later edit can tell apart.
	 * </p>
	 */
	private static String diagnosis(String identifier, KnowledgeBase kb) {
		return TidDiagnostics.explain(identifier, types(kb));
	}

	/**
	 * What the knowledge base can say about a type name, without reaching the database.
	 *
	 * @see TidDiagnostics.Types
	 */
	private static TidDiagnostics.Types types(KnowledgeBase kb) {
		MORepository repository = kb.getMORepository();
		return typeName -> {
			MetaObject type = repository.getTypeOrNull(typeName);
			if (type == null) {
				return TidDiagnostics.TypeState.UNKNOWN;
			}
			if (type instanceof MOClass clazz && clazz.isAbstract()) {
				return TidDiagnostics.TypeState.ABSTRACT;
			}
			return TidDiagnostics.TypeState.INSTANTIABLE;
		};
	}
}
