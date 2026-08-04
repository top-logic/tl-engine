/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Holds the administrator-authored mapping of security roles to the agent tools they may use.
 *
 * <p>
 * Deliberately shaped like the engine's {@code SecurityConfigurationService} (ticket #29088) so that
 * an administrator meets one idiom rather than two: static application configuration, role names
 * resolved and validated at boot, deny-by-default semantics. See
 * {@code doc/security/tool-access-configuration.md} for the five-line version an administrator starts
 * from, and section 4.1 of {@code doc/security/agent-security-architecture.md} for the full design.
 * </p>
 *
 * <p>
 * <b>Enforcement is opt-in.</b> Writing a non-empty {@code <tool-access>} is what turns it on; until
 * then the effective mode is {@link AiSecurityMode#LEGACY} and every agent behaves exactly as it did
 * before this service existed. That asymmetry is intentional and explained on
 * {@link AiSecurityMode#resolve(ToolAccessPolicy)}: both this layer and the engine's model security
 * deny by default, so switching them on for an application that has authored no policy would turn
 * every existing agent into one that can do nothing. Once a policy <em>is</em> written, every
 * decision inside it fails closed - including the {@code <background-allowlist>}, whose absence is a
 * boot error rather than a permissive default.
 * </p>
 *
 * @see RoleToolAccess
 * @see ToolAuthorization
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	// Roles must exist before the policy naming them is validated - the same dependency
	// SecurityConfigurationService declares, and for the same reason.
	InitialRolesManager.Module.class,
	// The roles a person holds on the security root are read through these two.
	AccessManager.Module.class,
	BoundHelper.Module.class,
	// Tool group declarations are read from the registry.
	ToolRegistryService.Module.class,
})
public class AiToolAccessService extends ConfiguredManagedClass<AiToolAccessService.Config<?>> {

	/**
	 * Configuration of the {@link AiToolAccessService}.
	 *
	 * <p>
	 * The policy itself is {@link ToolAccessPolicy}; this interface only adds the service plumbing, so
	 * that the XML an administrator writes is the policy and nothing else.
	 * </p>
	 */
	public interface Config<I extends AiToolAccessService>
			extends ConfiguredManagedClass.Config<I>, ToolAccessPolicy {
		// Sum of the service configuration and the policy.
	}

	private final InstantiationContext _context;

	private AiSecurityMode _ceiling;

	private RoleToolAccess _access;

	/**
	 * Creates a new {@link AiToolAccessService} from configuration.
	 */
	public AiToolAccessService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		Config<?> policy = getConfig();
		_ceiling = resolveCeiling(policy);
		_access = new RoleToolAccess(policy, AiToolAccessService::declaredGroupOf);

		checkRolesExist(policy);
		checkBackgroundAllowlistDeclared(policy);
		warnAboutUnknownGroups(policy);

		Logger.info("Agent tool access: mode=" + _ceiling
			+ " roles=" + policy.getToolAccess().size()
			+ " baseline=" + policy.getBaseline().size() + "(+" + RoleToolAccess.IMPLICIT_BASELINE + ")"
			+ " backgroundAllowlist=" + describeBackgroundAllowlist(policy)
			+ (_ceiling.enforcesToolAccess() ? "" : " (not enforced)"), AiToolAccessService.class);
	}

	/**
	 * Refuses an enforcing policy that says nothing about unattended runs.
	 *
	 * <p>
	 * Unattended runs ({@code aiAgentChatAsync}, schedulers, MCP clients) auto-approve every tool
	 * call because there is no human to ask, and {@code <background-allowlist>} is the only control
	 * that stands in for the approval prompt they lost. An administrator who writes a
	 * {@code <tool-access>} matrix and never learns the allowlist exists must therefore not end up
	 * with unattended runs bounded only by the acting user's grants. Silence is not a decision, so it
	 * is rejected rather than interpreted - in either direction.
	 * </p>
	 */
	private void checkBackgroundAllowlistDeclared(ToolAccessPolicy policy) {
		if (!needsBackgroundAllowlist(policy)) {
			return;
		}
		_context.error("Agent tool access is enforced (" + _ceiling
			+ ") but the policy declares no <background-allowlist>. Unattended agent runs approve"
			+ " every tool call themselves, so the allowlist is what bounds them and it has to be"
			+ " stated. Write <background-allowlist> with the groups such runs may use, or"
			+ " <background-allowlist/> to forbid them every tool, or <background-allowlist"
			+ " all=\"true\"/> to accept unattended runs bounded only by the acting user's grants.");
	}

	/**
	 * Whether the given policy enforces tool access without declaring what unattended runs may reach.
	 *
	 * @param policy
	 *        The policy to inspect.
	 * @return Whether the policy is under-specified and must be rejected.
	 */
	static boolean needsBackgroundAllowlist(ToolAccessPolicy policy) {
		return resolveCeiling(policy).enforcesToolAccess() && policy.getBackgroundAllowlist() == null;
	}

	/** The configured background ceiling, for the boot log line. */
	private static String describeBackgroundAllowlist(ToolAccessPolicy policy) {
		ToolAccessPolicy.BackgroundAllowlist allowlist = policy.getBackgroundAllowlist();
		if (allowlist == null) {
			return "(undeclared)";
		}
		return allowlist.isAll() ? "all" : String.valueOf(allowlist.getGrants().size());
	}

	/**
	 * The deployment-wide enforcement ceiling.
	 *
	 * <p>
	 * An explicitly configured {@code security-mode} always wins. Without one, the presence of a
	 * non-empty {@code <tool-access>} is the opt-in signal - writing the matrix is how an
	 * administrator says "enforce this" - and its absence keeps the application on
	 * {@link AiSecurityMode#LEGACY}. This mirrors the design's rule that the presence of a policy is
	 * the opt-in signal (section 3.1).
	 * </p>
	 */
	static AiSecurityMode resolveCeiling(ToolAccessPolicy policy) {
		AiSecurityMode configured = policy.getSecurityMode();
		if (configured != null) {
			return configured;
		}
		return policy.getToolAccess().isEmpty() ? AiSecurityMode.LEGACY : AiSecurityMode.ENFORCED;
	}

	/**
	 * Reports every role name the policy references that does not exist, as a configuration error.
	 *
	 * <p>
	 * The same treatment {@code SecurityConfigurationService.processRights} gives an unknown role, and
	 * for the same reason: a typo in a role name would otherwise silently widen or narrow access -
	 * a grant to a misspelled role reaches nobody, and nothing says so.
	 * </p>
	 */
	private void checkRolesExist(ToolAccessPolicy policy) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		for (String roleName : unknownRoles(policy, name -> BoundedRole.getRoleByName(kb, name) != null)) {
			_context.error("The role " + roleName
				+ " in the agent tool access configuration does not exist.");
		}
	}

	/**
	 * The role names the policy references that {@code exists} does not know.
	 *
	 * @param policy
	 *        The policy to inspect.
	 * @param exists
	 *        Whether a role of the given name exists.
	 * @return The unknown names, in a stable order.
	 */
	static Set<String> unknownRoles(ToolAccessPolicy policy, Predicate<String> exists) {
		Set<String> unknown = new TreeSet<>();
		for (String roleName : policy.getToolAccess().keySet()) {
			if (!exists.test(roleName)) {
				unknown.add(roleName);
			}
		}
		return unknown;
	}

	/**
	 * Warns about group names the policy grants that no tool and no group declaration knows.
	 *
	 * <p>
	 * A warning rather than an error, unlike an unknown role: group names are an open string space by
	 * design - applications, MCP servers and TL-Script tools authored in the running application
	 * contribute their own - so a group that no tool claims at boot is not proof of a typo. It is
	 * still worth saying out loud, because a grant of a misspelled group reaches nothing.
	 * </p>
	 */
	private void warnAboutUnknownGroups(ToolAccessPolicy policy) {
		Set<String> known = new HashSet<>(ToolGroups.ALL);
		known.addAll(policy.getToolGroups().keySet());
		for (Tool tool : ToolRegistryService.getInstance().getTools()) {
			if (!tool.group().isBlank()) {
				known.add(tool.group());
			}
		}

		Set<String> unknown = unknownGroups(policy, known);
		if (!unknown.isEmpty()) {
			Logger.warn("Agent tool access references tool groups that no tool declares and no"
				+ " <tool-groups> entry defines: " + unknown + ". Grants of these groups reach no tool."
				+ " Known groups: " + new TreeSet<>(known) + ".", AiToolAccessService.class);
		}
	}

	/**
	 * The group names the policy grants or denies that are not in {@code known}.
	 *
	 * @param policy
	 *        The policy to inspect.
	 * @param known
	 *        The groups that exist: the framework's own, the ones the policy defines, and the ones the
	 *        registered tools declare.
	 * @return The unknown names, in a stable order.
	 */
	static Set<String> unknownGroups(ToolAccessPolicy policy, Set<String> known) {
		Set<String> unknown = new TreeSet<>();
		collectGroups(policy.getBaseline(), known, unknown);
		ToolAccessPolicy.BackgroundAllowlist background = policy.getBackgroundAllowlist();
		if (background != null) {
			collectGroups(background.getGrants(), known, unknown);
		}
		for (RoleAccess role : policy.getToolAccess().values()) {
			collectGroups(role.getGrants(), known, unknown);
			collectGroups(role.getDenies(), known, unknown);
		}
		return unknown;
	}

	private static void collectGroups(Collection<Grant> entries, Set<String> known, Set<String> unknown) {
		for (Grant entry : entries) {
			String group = entry.getGroup();
			if (group != null && !group.isBlank() && !known.contains(group)) {
				unknown.add(group);
			}
		}
	}

	/** The group the registered tool of the given qualified name declares for itself. */
	private static String declaredGroupOf(String qualifiedToolName) {
		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			return null;
		}
		Tool tool = ToolRegistryService.getInstance().getTool(qualifiedToolName);
		return tool == null ? null : tool.group();
	}

	/**
	 * The deployment-wide enforcement mode, which every run of this application carries.
	 *
	 * <p>
	 * Not the same as {@code getConfig().getSecurityMode()}: that is what the administrator wrote,
	 * this is what it resolves to. See {@link #resolveCeiling(ToolAccessPolicy)}.
	 * </p>
	 */
	public AiSecurityMode getEffectiveMode() {
		return _ceiling;
	}

	/**
	 * The policy evaluation for this deployment.
	 */
	public RoleToolAccess getAccess() {
		return _access;
	}

	/**
	 * The names of the roles the given user holds on the security root.
	 *
	 * <p>
	 * Tool access is a <em>global</em> capability - "may this person use the model editor at all" -
	 * so the roles are read on the security root rather than on a business object. Instance-level
	 * nuance is the model-access layer's job ({@link ModelSecurity}).
	 * </p>
	 *
	 * @param person
	 *        The user, may be <code>null</code>.
	 * @return The role names, never <code>null</code>; empty for a <code>null</code> user, who then
	 *         gets nothing beyond the policy's baseline.
	 */
	public static Set<String> heldRoleNames(Person person) {
		if (person == null) {
			return Set.of();
		}
		BoundObject securityRoot = BoundHelper.getInstance().getDefaultObject();
		Set<String> names = new HashSet<>();
		for (BoundRole role : AccessManager.getInstance().getRoles(person, securityRoot)) {
			names.add(role.getName());
		}
		return names;
	}

	/**
	 * A {@link ToolAuthorization.Identities} resolver for one agent run.
	 *
	 * <p>
	 * Builds its index of the tool registry lazily, on first use, and keeps it for the life of the
	 * returned instance. Per-run rather than per-service on purpose: a tool that authors agents can
	 * make the registry reload mid-application, and a run must not see a half-changed catalog - while
	 * a run-scoped snapshot going stale after the run has ended is irrelevant.
	 * </p>
	 */
	public static ToolAuthorization.Identities identities() {
		return new RegistryIdentities();
	}

	/**
	 * Resolves the qualified registry name of a tool object.
	 *
	 * <p>
	 * By object identity, not by name: two factories may contribute tools of the same simple name, and
	 * the identity is the only thing that distinguishes them.
	 * </p>
	 */
	private static final class RegistryIdentities implements ToolAuthorization.Identities {

		private Map<Tool, String> _qualifiedNames;

		@Override
		public ToolIdentity identify(Tool tool) {
			return new ToolIdentity(tool.name(), index().get(tool), tool.group());
		}


		private Map<Tool, String> index() {
			if (_qualifiedNames == null) {
				_qualifiedNames = buildIndex();
			}
			return _qualifiedNames;
		}

		private static Map<Tool, String> buildIndex() {
			Map<Tool, String> result = new IdentityHashMap<>();
			if (!ToolRegistryService.Module.INSTANCE.isActive()) {
				return result;
			}
			ToolRegistryService registry = ToolRegistryService.getInstance();
			for (String providerName : registry.getToolNames()) {
				Tool tool = registry.getTool(providerName);
				if (tool != null) {
					// The dot-notation name, not the provider-safe registry key: it is what an
					// administrator writes in the policy and what a denial message should show.
					String configName = registry.getConfigName(providerName);
					result.put(tool, configName != null ? configName : providerName);
				}
			}
			return result;
		}
	}

	/**
	 * The tool-authorization decision for one agent run.
	 *
	 * @param principal
	 *        The user the run acts as, or <code>null</code> for a run with no user identity.
	 * @param background
	 *        Whether the run has no human to answer approval prompts, in which case the policy's
	 *        background allowlist applies as an additional ceiling.
	 * @return The authorization to carry on the run's security context, or
	 *         {@link ToolAuthorization#UNCHECKED} if this service is not configured.
	 */
	public static ToolAuthorization authorizationFor(Person principal, boolean background) {
		if (!Module.INSTANCE.isActive()) {
			return ToolAuthorization.UNCHECKED;
		}
		AiToolAccessService service = getInstance();
		return new ToolAuthorization(service.getAccess(), principal, heldRoleNames(principal),
			background, identities());
	}

	/**
	 * The configured policy, or <code>null</code> if this service is not running - which is the
	 * unmigrated application, and therefore {@link AiSecurityMode#LEGACY}.
	 */
	public static ToolAccessPolicy policy() {
		return Module.INSTANCE.isActive() ? getInstance().getConfig() : null;
	}

	/**
	 * The deployment-wide enforcement ceiling, or {@link AiSecurityMode#LEGACY} if this service is not
	 * running.
	 */
	public static AiSecurityMode ceiling() {
		return Module.INSTANCE.isActive() ? getInstance().getEffectiveMode() : AiSecurityMode.LEGACY;
	}

	/**
	 * The singleton {@link AiToolAccessService} instance.
	 */
	public static AiToolAccessService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link AiToolAccessService}.
	 */
	public static final class Module extends TypedRuntimeModule<AiToolAccessService> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AiToolAccessService> getImplementation() {
			return AiToolAccessService.class;
		}
	}
}
