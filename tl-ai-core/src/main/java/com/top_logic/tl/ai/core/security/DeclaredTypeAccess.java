/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The pre-flight check of what a tool declares it works on - {@link ToolMethod#types()} - against the
 * READ rights of the run's principal (issue #258).
 *
 * <p>
 * One question, asked in two places and answered the same way both times: the schema filter leaves a
 * tool out of the provider request, and the invocation gate refuses a call to it. Both go through
 * {@link com.top_logic.tl.ai.core.agent.ToolPermissionFilter}, which is why this class has a quiet
 * {@link #permits(Tool)} beside the {@link #denialReason(Tool)} that logs - enumerating a catalog must
 * not write an audit line per tool for calls nobody made.
 * </p>
 *
 * <p>
 * <b>A tool is refused when it declares types and the principal may read none of them.</b> Not "one
 * of them is unreadable": a tool that names {@code Customer} and {@code Contract} for a user who may
 * read customers can still do the customer half of its job, and refusing it would take away work the
 * user is allowed to do. The two uses are deliberately the same predicate - a rule that denied on one
 * unreadable type while the filter dropped only fully unreadable tools would advertise a tool that
 * every call to it refuses, which the model can only discover by calling it.
 * </p>
 *
 * <p>
 * <b>This reads rights on the security root</b>, through
 * {@link ModelSecurity#accessibleTypes(com.top_logic.tool.boundsec.BoundCommandGroup)} - the same
 * computation the model digest and the read filter use (issue #295). It is an L1 check and has L1's
 * scope: a role a rule derives <em>per object</em> is invisible here. That is what makes declaring a
 * type a decision rather than a formality, and {@link ToolMethod#types()} says which types may be
 * declared and which may not.
 * </p>
 *
 * <p>
 * Never a substitute for the per-object checks. A tool can declare a type it does not touch, or touch
 * one it did not declare, so what actually protects the data is {@link ModelSecurity#canRead} on each
 * object, the read filtering of the results and the pre-commit guard. This layer exists so that a
 * refusal the user could not have got past arrives as a sentence naming the type instead of as an
 * empty result the model reports to the user as "there are none".
 * </p>
 *
 * <p>
 * See {@code doc/security/agent-security-architecture.md}, section 3.3.3.
 * </p>
 */
public class DeclaredTypeAccess {

	/**
	 * What the application model and the principal's rights say about one declared type name.
	 */
	public enum Verdict {

		/** The principal may read the type, or a type below it. */
		READABLE,

		/** The type exists and the principal may not read it. */
		UNREADABLE,

		/**
		 * This application's model has no such type.
		 *
		 * <p>
		 * A broken declaration, and deliberately not a denial: a typo is not a statement about
		 * rights, and turning it into one would take a working tool away from every user for
		 * nothing.
		 * </p>
		 */
		NO_SUCH_TYPE;
	}

	/**
	 * What a declared type name is worth for one principal.
	 *
	 * <p>
	 * The seam between the rule - which declarations refuse a tool - and the model lookup that
	 * answers it. Production uses {@link #modelRights(ModelSecurity)}; a test supplies its own and
	 * needs no application model.
	 * </p>
	 */
	@FunctionalInterface
	public interface TypeReadAccess {

		/**
		 * The verdict for the given qualified type name, e.g. {@code "myapp:Customer"}.
		 */
		Verdict readAccess(String qualifiedTypeName);
	}

	/**
	 * Declared names that resolved to no type, so that a broken declaration is reported once per
	 * process rather than once per tool call.
	 */
	private static final Set<String> UNRESOLVED_REPORTED = ConcurrentHashMap.newKeySet();

	private final TypeReadAccess _access;

	/**
	 * Creates a {@link DeclaredTypeAccess} over the model rights of one run.
	 *
	 * @param modelSecurity
	 *        The run's model access. Never <code>null</code>.
	 */
	public DeclaredTypeAccess(ModelSecurity modelSecurity) {
		this(modelRights(modelSecurity));
	}

	/**
	 * Creates a {@link DeclaredTypeAccess} over the given answers.
	 *
	 * @param access
	 *        What each declared type name is worth for the principal. Never <code>null</code>.
	 */
	public DeclaredTypeAccess(TypeReadAccess access) {
		_access = access;
	}

	/**
	 * Whether the run's principal may use the given tool as far as its declared types go, without
	 * logging.
	 *
	 * <p>
	 * For the schema filter. <code>true</code> for every tool that declares nothing, which is the
	 * overwhelming majority, and answered without asking for any right at all - a catalog of
	 * undeclared tools costs nothing here.
	 * </p>
	 *
	 * @param tool
	 *        The tool to check.
	 * @return Whether the declared types permit the tool.
	 */
	public boolean permits(Tool tool) {
		return unreadableTypes(tool).isEmpty();
	}

	/**
	 * The types the given tool declares and the run's principal may not read, without logging.
	 *
	 * <p>
	 * The same decision {@link #permits(Tool)} makes, for a caller that has to <em>name</em> what
	 * stands in the way rather than only know that something does - the capability view composes a
	 * skill's and an agent's refusal out of the types of all its tools. Empty means permitted, and
	 * that includes the two cases the rule deliberately lets pass: one readable declared type is
	 * enough, and a declaration this application's model does not know is not a denial.
	 * </p>
	 *
	 * @param tool
	 *        The tool to check.
	 * @return The unreadable declared type names, in declaration order; empty if the tool is
	 *         permitted.
	 */
	public List<String> unreadableTypes(Tool tool) {
		List<String> declared = tool.types();
		if (declared == null || declared.isEmpty()) {
			return List.of();
		}
		List<String> unreadable = new ArrayList<>(declared.size());
		for (String name : declared) {
			Verdict verdict = _access.readAccess(name);
			if (verdict == Verdict.READABLE) {
				return List.of();
			}
			if (verdict == Verdict.UNREADABLE) {
				unreadable.add(name);
			} else {
				report(tool, name);
			}
		}
		// An empty list here means every declared name was unresolvable: the tool declares nothing
		// this application knows, which leaves it exactly as unrestricted as declaring nothing would.
		return unreadable;
	}

	/**
	 * Why the run's principal may not use the given tool's declared types, recording the refusal.
	 *
	 * <p>
	 * For the invocation gate, where a refusal is a real event. Logged at info rather than warn: an
	 * account without a read right is an ordinary, configured state and not an operator's problem.
	 * </p>
	 *
	 * @param tool
	 *        The tool being called.
	 * @return A sentence naming the types and the missing right, safe to feed back to the model, or
	 *         <code>null</code> if the tool is permitted.
	 */
	public String denialReason(Tool tool) {
		String reason = decide(tool);
		if (reason != null) {
			Logger.info("Tool access denied by declared types: tool='" + tool.name()
				+ "' types=" + tool.types() + " reason=" + reason, DeclaredTypeAccess.class);
		}
		return reason;
	}

	/**
	 * The refusal for the given tool, or <code>null</code> if it is permitted. No side effect.
	 */
	private String decide(Tool tool) {
		List<String> unreadable = unreadableTypes(tool);
		return unreadable.isEmpty() ? null : refusal(tool, unreadable);
	}

	/**
	 * The sentence the model is given for a tool all of whose declared types are out of reach.
	 *
	 * <p>
	 * It names the types, says that a model access right and not a tool permission is the cause, and
	 * closes the two wrong conclusions: retrying, and reporting the refusal to the user as "there is
	 * no such data" - which is the very misreading the declaration exists to prevent.
	 * </p>
	 */
	private static String refusal(Tool tool, List<String> unreadable) {
		boolean one = unreadable.size() == 1;
		return "Permission denied: tool '" + tool.name() + "' works on " + quoted(unreadable)
			+ ", and you have no read access to " + (one ? "that type" : "any of them")
			+ ". This is a model access right rather than a tool permission: an administrator would"
			+ " have to grant Read on " + (one ? "that type" : "one of those types")
			+ " to a role this account holds. Do not retry, and do not report this as 'there is no"
			+ " such data' - the data may well exist.";
	}

	private static void report(Tool tool, String name) {
		if (UNRESOLVED_REPORTED.add(name)) {
			Logger.warn("Tool '" + tool.name() + "' declares the type '" + name + "', which this"
				+ " application's model does not have. The declaration is ignored; what the tool may"
				+ " touch is decided per object, as for a tool that declares nothing.",
				DeclaredTypeAccess.class);
		}
	}

	/**
	 * The declared type names as they read inside a sentence: quoted, comma-separated, the last two
	 * joined by "and".
	 *
	 * <p>
	 * Public because the refusal of a skill or an agent is composed elsewhere - see
	 * {@code AgentCapabilities} - and two spellings of the same list would read as two different
	 * rules.
	 * </p>
	 *
	 * @param names
	 *        The type names to list. Never empty.
	 */
	public static String namedTypes(Collection<String> names) {
		return quoted(new ArrayList<>(names));
	}

	private static String quoted(List<String> names) {
		List<String> quoted = new ArrayList<>(names.size());
		for (String name : names) {
			quoted.add("'" + name + "'");
		}
		if (quoted.size() == 1) {
			return quoted.get(0);
		}
		return String.join(", ", quoted.subList(0, quoted.size() - 1))
			+ " and " + quoted.get(quoted.size() - 1);
	}

	/**
	 * The production answer: the application model resolves the name, and the principal's READ rights
	 * on the security root decide.
	 *
	 * <p>
	 * The accessible set is resolved on first use and then kept - a provider request asks this for
	 * every tool of the catalog, and the answer cannot change within one request - so an instance
	 * belongs to one request and is not shared between runs.
	 * </p>
	 */
	private static TypeReadAccess modelRights(ModelSecurity modelSecurity) {
		return new TypeReadAccess() {

			private Set<TLClass> _readable;

			@Override
			public Verdict readAccess(String qualifiedTypeName) {
				TLClass type = resolve(qualifiedTypeName);
				if (type == null) {
					return Verdict.NO_SUCH_TYPE;
				}
				return isReadable(type) ? Verdict.READABLE : Verdict.UNREADABLE;
			}

			/**
			 * Whether the principal may read the given declared type, or any type below it.
			 *
			 * <p>
			 * A declaration names the type the tool is about, which may be the abstract base of the
			 * types the grants are written on. A user who may read {@code myapp:PrivateCustomer} may
			 * use a tool that declares {@code myapp:Customer} and finds nothing but private customers
			 * in its answer.
			 * </p>
			 */
			private boolean isReadable(TLClass type) {
				if (_readable == null) {
					_readable = modelSecurity.accessibleTypes(SimpleBoundCommandGroup.READ);
				}
				if (_readable.contains(type)) {
					return true;
				}
				for (TLClass specialization : TLModelUtil.getConcreteReflexiveTransitiveSpecializations(type)) {
					if (_readable.contains(specialization)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	private static TLClass resolve(String qualifiedTypeName) {
		TLType type;
		try {
			type = TLModelUtil.findType(qualifiedTypeName);
		} catch (RuntimeException ex) {
			// findType reports an unknown module, an unknown type and a malformed name all as an
			// exception; all three are the same broken declaration here.
			return null;
		}
		return type instanceof TLClass result ? result : null;
	}

}
