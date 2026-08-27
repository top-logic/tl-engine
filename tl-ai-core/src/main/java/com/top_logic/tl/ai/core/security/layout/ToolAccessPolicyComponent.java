/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.AllToolNames;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.RoleTemplate;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.TriState;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/*
 * Issue #297, proposals D1, E and F of doc-internal concept sec-config-ux.
 *
 * The class comment below is exported to META-INF/messages_*.properties by the build's doclet and shown
 * as this component's tooltip in the layout editor, so it is written for an administrator and stays
 * short - TestSecurityConfigPresentation enforces a length limit on everything under this package
 * prefix. The design notes live here, where the doclet does not export them.
 *
 * Why an editor of its own rather than the generic service editor, which can already write this policy:
 * that editor renders the stored format, and the stored format is not the decision an administrator
 * makes. They decide "may this role change data" and get a list of permission boxes each holding two
 * mutually exclusive inputs; they decide "does everybody get read access by default" and get an optional
 * element whose absent, empty and filled states look the same. This component asks the three questions
 * that actually exist - per role and capability: refused, unmentioned, granted; for the baseline and the
 * background limit: which of three named states - and writes exactly the same XML.
 *
 * Every control edits PolicyDraft, which is a copy of the service configuration; nothing is written
 * until the apply command has run the checks that would otherwise refuse the next application start.
 *
 * On rebuilding: a change of *structure* - a role added, an entry switched between naming a group and
 * naming tools - drops the form context, drops the rendering control and invalidates the component.
 * Dropping the rendering control is the part that is easy to miss: ContentControl asks
 * getRenderingControl() again on every repaint, so a cached control would render the members of the form
 * context that was just thrown away. A change of *value* - a tri-state, a picked group - repaints only
 * the blocks that depend on it, so the field the user is in is not rebuilt underneath them.
 */

/**
 * Configures which security roles may use which agent tools.
 *
 * <p>
 * Writes the policy of the tool access service. What is saved takes effect when that service is
 * restarted, which this view offers.
 * </p>
 *
 * @see ToolCatalogModelBuilder
 * @see EffectiveToolAccessComponent
 */
public class ToolAccessPolicyComponent extends FormComponent implements ControlRepresentable {

	/** Name of the field choosing which role to add to the policy. */
	public static final String FIELD_NEW_ROLE = "newRole";

	/** Name of the field choosing what a newly added role starts with. */
	public static final String FIELD_TEMPLATE = "template";

	/** Name of the field choosing what every account gets. */
	public static final String FIELD_BASELINE_GROUPS = "baselineGroups";

	/** Name of the field choosing what an unattended run may reach. */
	public static final String FIELD_BACKGROUND_GROUPS = "backgroundGroups";

	/**
	 * Configuration options for {@link ToolAccessPolicyComponent}.
	 */
	public interface Config extends FormComponent.Config {

		@Override
		@ClassDefault(ToolAccessPolicyComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/** The policy being edited, or <code>null</code> if the saved one cannot be read. */
	private PolicyDraft _draft;

	/** Why the last apply was refused, empty when the last one succeeded or none has run. */
	private List<ResKey> _refusals = List.of();

	private Control _renderingControl;

	/** The block listing what the draft does that may surprise; repainted on every value change. */
	private DynamicFragment _warnings;

	/** The line saying whether there is anything unsaved; repainted on every value change. */
	private DynamicFragment _dirty;

	/** The notes of the capability rows, by the name of the field they belong to. */
	private final Map<String, DynamicFragment> _rowNotes = new TreeMap<>();

	/** The one-line summaries of the entries naming single tools, by field name. */
	private final Map<String, DynamicFragment> _entrySummaries = new TreeMap<>();

	/**
	 * The entries the "Erweitert" block of a role shows, by identity.
	 *
	 * <p>
	 * An entry belongs there because somebody added it there - not because of what it currently names.
	 * Without this, switching an entry from single tools to a whole capability group would make it
	 * disappear from the block it is in: it would then be a group entry, and group entries are the
	 * tri-state rows above. Vanishing on a click is the one thing a control offering a choice must not
	 * do.
	 * </p>
	 */
	private final Set<Grant> _addedEntries = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Creates a {@link ToolAccessPolicyComponent} from configuration.
	 */
	@CalledByReflection
	public ToolAccessPolicyComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// The view is about the application's policy; there is no business object it could be opened on.
		return object == null;
	}

	@Override
	public FormContext createFormContext() {
		_rowNotes.clear();
		_entrySummaries.clear();
		FormContext context = new FormContext(this);
		if (_draft == null) {
			_draft = PolicyDraft.ofSavedPolicy();
		}
		if (_draft == null) {
			return context;
		}
		addRoleFields(context);
		addBaselineFields(context);
		addBackgroundFields(context);
		addApplyFields(context);
		if (!mayWrite()) {
			// Reading the policy is what the two other tabs are for; writing it is an administrator's
			// job, and the buttons of a form are not command handlers, so nothing would have checked.
			for (Iterator<? extends FormMember> members = context.getMembers(); members.hasNext();) {
				members.next().setImmutable(true);
			}
		}
		return context;
	}

	/** Whether the acting account may change the policy rather than only look at it. */
	private boolean mayWrite() {
		return BoundChecker.allowCommand(this, SimpleBoundCommandGroup.WRITE, getModel());
	}

	@Override
	protected void installFormContext(FormContext newFormContext) {
		super.installFormContext(newFormContext);
		// The rendering control wraps the fields of the form context that is being replaced.
		_renderingControl = null;
	}

	@Override
	public Control getRenderingControl() {
		if (_renderingControl == null) {
			_renderingControl = new FragmentControl(view());
		}
		return _renderingControl;
	}

	/**
	 * Rebuilds the form and repaints the whole view.
	 *
	 * <p>
	 * For every change that adds, removes or replaces a field. Dropping the rendering control is what
	 * makes the repaint show the new fields rather than the old ones.
	 * </p>
	 */
	private HandlerResult rebuild() {
		removeFormContext();
		_renderingControl = null;
		invalidate();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Repaints the blocks that depend on what the policy grants, without rebuilding a field.
	 *
	 * <p>
	 * Called from the value listeners, which run while the user is inside the field that changed:
	 * rebuilding the form there would replace that field underneath them.
	 * </p>
	 */
	private void refreshNotes() {
		if (_warnings != null) {
			_warnings.requestRepaint();
		}
		if (_dirty != null) {
			_dirty.requestRepaint();
		}
		for (DynamicFragment note : _rowNotes.values()) {
			note.requestRepaint();
		}
		for (DynamicFragment summary : _entrySummaries.values()) {
			summary.requestRepaint();
		}
	}

	// ---------------------------------------------------------------- the fields

	/** One tri-state per configured role and grantable capability, plus the structural commands. */
	private void addRoleFields(FormContext context) {
		List<String> roleNames = _draft.roleNames();
		List<String> groups = _draft.grantableGroups();
		for (int r = 0; r < roleNames.size(); r++) {
			String roleName = roleNames.get(r);
			for (int g = 0; g < groups.size(); g++) {
				context.addMember(triStateField(roleName, groups.get(g), triStateName(r, g)));
			}
			addEntryFields(context, roleName, r, false);
			addEntryFields(context, roleName, r, true);

			String role = roleName;
			context.addMember(button("addGrant_" + r, I18NConstants.COMMAND_ADD_GRANT_ENTRY, () -> {
				_addedEntries.add(_draft.addToolEntry(role, false));
				return rebuild();
			}));
			context.addMember(button("addDeny_" + r, I18NConstants.COMMAND_ADD_DENY_ENTRY, () -> {
				_addedEntries.add(_draft.addToolEntry(role, true));
				return rebuild();
			}));
			context.addMember(button("removeRole_" + r, I18NConstants.COMMAND_REMOVE_ROLE, () -> {
				_draft.removeRole(role);
				return rebuild();
			}));
		}
		context.addMember(newRoleField());
		context.addMember(templateField());
		context.addMember(button("addRole", I18NConstants.COMMAND_ADD_ROLE, this::addChosenRole));
	}

	private SelectField triStateField(String roleName, String group, String fieldName) {
		SelectField field = FormFactory.newSelectField(fieldName,
			List.of(TriState.DENIED, TriState.UNSET, TriState.GRANTED), false, true, false, null);
		field.setAsSingleSelection(_draft.triState(roleName, group));
		field.setOptionLabelProvider(TRI_STATE_LABELS);
		field.setLabel(ToolAccessLabels.groupLabel(group));
		field.addValueListener((changed, oldValue, newValue) -> {
			_draft.setTriState(roleName, group, single(newValue, TriState.UNSET));
			refreshNotes();
		});
		return field;
	}

	/**
	 * The entries of one role shown in its "Erweitert" block: the either-or of proposal D1.
	 */
	private void addEntryFields(FormContext context, String roleName, int roleIndex, boolean denies) {
		List<Grant> entries = entriesOf(roleName, denies);
		for (int n = 0; n < entries.size(); n++) {
			Grant entry = entries.get(n);
			String prefix = entryName(roleIndex, denies, n);

			context.addMember(button(prefix + "_kindGroup", I18NConstants.ENTRY_KIND_GROUP, () -> {
				PolicyDraft.setNamesGroup(entry, true);
				return rebuild();
			}));
			context.addMember(button(prefix + "_kindTools", I18NConstants.ENTRY_KIND_TOOLS, () -> {
				PolicyDraft.setNamesGroup(entry, false);
				return rebuild();
			}));
			context.addMember(button(prefix + "_remove", I18NConstants.COMMAND_REMOVE_ENTRY, () -> {
				_draft.removeEntry(roleName, denies, entry);
				_addedEntries.remove(entry);
				return rebuild();
			}));

			if (PolicyDraft.namesWholeGroup(entry)) {
				context.addMember(entryGroupField(prefix + "_group", entry));
			} else {
				context.addMember(entryToolsField(prefix + "_tools", entry));
			}
		}
	}

	private SelectField entryGroupField(String fieldName, Grant entry) {
		SelectField field = groupField(fieldName, false, I18NConstants.FIELD_ENTRY_GROUP);
		field.setAsSingleSelection(entry.getGroup());
		field.addValueListener((changed, oldValue, newValue) -> {
			entry.setGroup(single(newValue, ToolGroups.MODEL_READ));
			refreshNotes();
		});
		return field;
	}

	private SelectField entryToolsField(String fieldName, Grant entry) {
		SelectField field = FormFactory.newSelectField(fieldName, allToolNames(), true, false, false, null);
		field.setAsSelection(entry.getTools());
		field.setLabel(resources().getString(I18NConstants.FIELD_ENTRY_TOOLS));
		field.addValueListener((changed, oldValue, newValue) -> {
			entry.setTools(new ArrayList<>(strings(newValue)));
			refreshNotes();
		});
		return field;
	}

	/** The three baseline states, and the capabilities of the one that has a list. */
	private void addBaselineFields(FormContext context) {
		for (BaselineChoice choice : BaselineChoice.values()) {
			context.addMember(button("baseline_" + choice.name(),
				ToolAccessLabels.baselineChoice(choice), () -> {
					_draft.setBaselineChoice(choice);
					return rebuild();
				}));
		}
		if (_draft.baselineChoice() == BaselineChoice.CUSTOM) {
			SelectField field =
				groupField(FIELD_BASELINE_GROUPS, true, I18NConstants.POLICY_BASELINE_HEADING);
			field.setAsSelection(new ArrayList<>(_draft.baselineGroups()));
			field.addValueListener((changed, oldValue, newValue) -> {
				setGroups(strings(newValue), _draft.baselineGroups(), _draft::toggleBaselineGroup);
				refreshNotes();
			});
			context.addMember(field);
		}
	}

	/** The three states of the background limit, and the capabilities of the one that has a list. */
	private void addBackgroundFields(FormContext context) {
		for (BackgroundChoice choice : BackgroundChoice.values()) {
			context.addMember(button("background_" + choice.name(),
				ToolAccessLabels.backgroundChoice(choice), () -> {
					_draft.setBackgroundChoice(choice);
					return rebuild();
				}));
		}
		if (_draft.backgroundChoice() == BackgroundChoice.CUSTOM) {
			SelectField field =
				groupField(FIELD_BACKGROUND_GROUPS, true, I18NConstants.POLICY_BACKGROUND_HEADING);
			field.setAsSelection(new ArrayList<>(_draft.backgroundGroups()));
			field.addValueListener((changed, oldValue, newValue) -> {
				setGroups(strings(newValue), _draft.backgroundGroups(), _draft::toggleBackgroundGroup);
				refreshNotes();
			});
			context.addMember(field);
		}
	}

	private void addApplyFields(FormContext context) {
		context.addMember(button("apply", I18NConstants.COMMAND_APPLY, this::apply));
		context.addMember(button("discard", I18NConstants.COMMAND_DISCARD, this::discard));
		context.addMember(button("restart", I18NConstants.COMMAND_RESTART, this::restart));
	}

	private SelectField newRoleField() {
		List<String> options = offeredRoles();
		SelectField field = FormFactory.newSelectField(FIELD_NEW_ROLE, options, false, false, false, null);
		field.setOptionLabelProvider(ROLE_LABELS);
		// The list is ordered by kind, and a selection field sorts by label unless told otherwise -
		// which would put the framework's own roles between the application's.
		field.setOptionComparator(BY_ROLE_KIND);
		field.setLabel(resources().getString(I18NConstants.FIELD_NEW_ROLE));
		// Deliberately nothing preselected: with the application's own roles already configured, the
		// first option is a role of the framework, and a preselected one of those is a suggestion.
		field.addValueListener((changed, oldValue, newValue) -> refreshNotes());
		return field;
	}

	private SelectField templateField() {
		SelectField field = FormFactory.newSelectField(FIELD_TEMPLATE,
			List.of(RoleTemplate.values()), false, false, false, null);
		field.setAsSingleSelection(RoleTemplate.READ_ONLY);
		field.setOptionLabelProvider(TEMPLATE_LABELS);
		field.setLabel(resources().getString(I18NConstants.FIELD_TEMPLATE));
		return field;
	}

	private SelectField groupField(String fieldName, boolean multiple, ResKey label) {
		SelectField field = FormFactory.newSelectField(fieldName, _draft.grantableGroups(), multiple,
			false, false, null);
		field.setOptionLabelProvider(GROUP_LABELS);
		field.setLabel(resources().getString(label));
		return field;
	}

	/**
	 * The roles the picker offers: the ones the application has and the ones the policy already names,
	 * minus the ones that are already configured.
	 */
	private List<String> offeredRoles() {
		List<String> configured = _draft.roleNames();
		List<String> result = new ArrayList<>();
		for (String roleName : existingRoleNames()) {
			if (!configured.contains(roleName)) {
				result.add(roleName);
			}
		}
		// Application roles first, then the framework's own, then the ones a grant does nothing for -
		// P8: the picker used to offer all three in one alphabetical list with nothing telling them apart.
		result.sort((left, right) -> {
			int byKind = SecurityRoleKind.of(left).ordinal() - SecurityRoleKind.of(right).ordinal();
			return byKind != 0 ? byKind : left.compareToIgnoreCase(right);
		});
		return result;
	}

	private static List<String> existingRoleNames() {
		List<String> result = new ArrayList<>();
		if (PersistencyLayer.Module.INSTANCE.isActive()) {
			for (BoundedRole role : BoundedRole.getAll()) {
				if (role.getName() != null && !role.getName().isBlank()) {
					result.add(role.getName());
				}
			}
		}
		return result;
	}

	private HandlerResult addChosenRole() {
		FormContext context = getFormContext();
		String roleName = single(((SelectField) context.getMember(FIELD_NEW_ROLE)).getValue(), null);
		RoleTemplate template =
			single(((SelectField) context.getMember(FIELD_TEMPLATE)).getValue(), RoleTemplate.NONE);
		if (roleName != null) {
			_draft.addRole(roleName, template);
		}
		return rebuild();
	}

	// ---------------------------------------------------------------- apply, discard, restart

	/**
	 * The entries of one role that the "Erweitert" block shows.
	 *
	 * <p>
	 * Every entry that names single tools, plus the ones added here that currently name a capability
	 * group. A group entry of a hand-written policy is <em>not</em> in it: it is the tri-state row above,
	 * and showing it twice would let one decision be edited in two places.
	 * </p>
	 *
	 * @param roleName
	 *        The role to read.
	 * @param denies
	 *        Whether to read the refusals rather than the permissions.
	 */
	private List<Grant> entriesOf(String roleName, boolean denies) {
		RoleAccess role = _draft.role(roleName);
		if (role == null) {
			return List.of();
		}
		List<Grant> result = new ArrayList<>();
		for (Grant entry : denies ? role.getDenies() : role.getGrants()) {
			if (!PolicyDraft.namesWholeGroup(entry) || _addedEntries.contains(entry)) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Checks the draft and, if nothing would stop the next start, writes it.
	 *
	 * <p>
	 * Proposal E. The refusal is the whole point: the raw editor accepts a policy that makes the next
	 * application start fail, hours later and for somebody else.
	 * </p>
	 */
	private HandlerResult apply() {
		_refusals = PolicyChecks.refusals(_draft.config(), ToolAccessPolicyComponent::roleExists);
		if (!_refusals.isEmpty()) {
			return rebuild();
		}
		PolicyStore.save(_draft.config());
		_draft = PolicyDraft.ofSavedPolicy();
		_addedEntries.clear();
		InfoService.showInfo(I18NConstants.APPLY_SAVED);
		return rebuild();
	}

	private HandlerResult discard() {
		_refusals = List.of();
		_draft = PolicyDraft.ofSavedPolicy();
		_addedEntries.clear();
		InfoService.showInfo(I18NConstants.DISCARDED);
		return rebuild();
	}

	private HandlerResult restart() {
		PolicyStore.restartService();
		_draft = PolicyDraft.ofSavedPolicy();
		_addedEntries.clear();
		InfoService.showInfo(I18NConstants.RESTART_DONE);
		return rebuild();
	}

	private static boolean roleExists(String roleName) {
		return BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName) != null;
	}

	// ---------------------------------------------------------------- the view

	/** The whole editor, from the banner down to the apply bar. */
	private HTMLFragment view() {
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return div("aiSecurityView", p(message(I18NConstants.SERVICE_NOT_RUNNING)));
		}
		FormContext context = getFormContext();
		if (_draft == null) {
			return div("aiSecurityView", p(message(I18NConstants.POLICY_UNREADABLE)));
		}
		_warnings = new DynamicFragment("aiSecurityWarnings", this::warnings);
		_dirty = new DynamicFragment("aiSecurityDirty",
			() -> message(_draft.isModified() ? I18NConstants.DIRTY_UNSAVED : I18NConstants.DIRTY_NONE));

		return div("aiSecurityView",
			banner(context),
			p("aiSecurityNote", message(I18NConstants.POLICY_INTRO)),
			mayWrite() ? empty() : p("aiSecurityNote", message(I18NConstants.POLICY_READ_ONLY)),
			refusals(),
			_warnings,
			roleSection(context),
			baselineSection(context),
			backgroundSection(context),
			toolGroupSection(),
			div("aiSecurityApplyBar",
				action(context, "apply"),
				action(context, "discard"),
				_dirty));
	}

	/**
	 * The banner saying that what is saved is not what the application applies.
	 *
	 * <p>
	 * Together with the restart action next to it, this is the honest answer to "is what I am looking
	 * at live". The catalog and this tab report the saved policy; the effective-permissions view reports
	 * the running one, and until the service is restarted the two disagree.
	 * </p>
	 */
	private HTMLFragment banner(FormContext context) {
		if (!PolicyDraft.differsFromRunningService(AiToolAccessService.savedPolicy())) {
			return empty();
		}
		return div("aiSecurityBanner",
			span(message(I18NConstants.BANNER_SAVED_NOT_EFFECTIVE)),
			action(context, "restart"));
	}

	/** Why the last apply was refused, or nothing while none was. */
	private HTMLFragment refusals() {
		if (_refusals.isEmpty()) {
			return empty();
		}
		return div("aiSecurityRefusals",
			p(message(I18NConstants.REFUSALS_HEADING)),
			messageList(_refusals));
	}

	/**
	 * What the draft does that its author probably did not mean.
	 *
	 * <p>
	 * Coalesced, and computed from the whole policy: every one of these is about two settings at once,
	 * so none of them can be shown next to a single control.
	 * </p>
	 */
	private HTMLFragment warnings() {
		List<ResKey> messages = PolicyChecks.warnings(_draft.config(), rolesWithModelRights());
		if (messages.isEmpty()) {
			return empty();
		}
		return div("aiSecurityWarning",
			p(message(I18NConstants.WARNINGS_HEADING)),
			messageList(messages));
	}

	/** The roles named in a Read, Write or Create grant of the model access rights. */
	private static Set<String> rolesWithModelRights() {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			return Set.of();
		}
		return AiToolAccessService.rolesWithModelRights(
			SecurityConfigurationService.Module.INSTANCE.getImplementationInstance().getConfig());
	}

	private HTMLFragment roleSection(FormContext context) {
		List<HTMLFragment> content = new ArrayList<>();
		content.add(h4(message(I18NConstants.POLICY_ROLES_HEADING)));
		List<String> roleNames = _draft.roleNames();
		if (roleNames.isEmpty()) {
			content.add(p("aiSecurityNote", message(I18NConstants.POLICY_NO_ROLES)));
		}
		for (int r = 0; r < roleNames.size(); r++) {
			content.add(roleCard(context, roleNames.get(r), r));
		}
		content.add(div("aiSecurityAddRole",
			labelWithColon(context.getMember(FIELD_NEW_ROLE)),
			value(context.getMember(FIELD_NEW_ROLE)),
			new DynamicFragment("aiSecurityNote", () -> roleNote(context)),
			labelWithColon(context.getMember(FIELD_TEMPLATE)),
			value(context.getMember(FIELD_TEMPLATE)),
			action(context, "addRole")));
		return div("aiSecurityRoles", fragments(content));
	}

	/** What is known about the role the picker currently shows - P8's "who holds this". */
	private HTMLFragment roleNote(FormContext context) {
		String roleName = single(((SelectField) context.getMember(FIELD_NEW_ROLE)).getValue(), null);
		if (roleName == null) {
			return empty();
		}
		List<HTMLFragment> notes = new ArrayList<>();
		if (SecurityRoleKind.EVERY_ACCOUNT.equals(roleName)) {
			notes.add(message(I18NConstants.ROLE_NOTE_EVERY_ACCOUNT));
		} else if (SecurityRoleKind.ADMINISTRATOR.equals(roleName)) {
			notes.add(message(I18NConstants.ROLE_NOTE_ADMINISTRATOR));
		} else if (SecurityRoleKind.of(roleName) == SecurityRoleKind.INTERNAL) {
			notes.add(message(I18NConstants.ROLE_NOTE_INTERNAL));
		}
		String description = roleDescription(roleName);
		if (description != null) {
			notes.add(text(description));
		}
		if (notes.isEmpty()) {
			return empty();
		}
		return span(fragments(notes));
	}

	/** The description an administrator gave the role, or <code>null</code> if there is none. */
	private static String roleDescription(String roleName) {
		if (!PersistencyLayer.Module.INSTANCE.isActive()) {
			return null;
		}
		BoundedRole role = BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName);
		if (role == null) {
			return null;
		}
		Object description = role.getValue(BoundedRole.ATTRIBUTE_DESCRIPTION);
		if (!(description instanceof String text) || text.isBlank()) {
			return null;
		}
		return text;
	}

	/**
	 * One card per configured role: a row per capability group, then the entries naming single tools.
	 */
	private HTMLFragment roleCard(FormContext context, String roleName, int roleIndex) {
		List<HTMLFragment> rows = new ArrayList<>();
		rows.add(div("aiSecurityRoleHead",
			span("aiSecurityRoleName", text(roleName)),
			span("aiSecurityRoleKind", message(ToolAccessLabels.roleKind(SecurityRoleKind.of(roleName)))),
			action(context, "removeRole_" + roleIndex)));

		for (String group : PolicyDraft.implicitGroups()) {
			// The marker column stays empty here although these groups carry one: it would say "granted
			// implicitly" next to a state that already says "always granted".
			rows.add(div("aiSecurityTriRow",
				span("aiSecurityGroupLabel", text(ToolAccessLabels.groupLabel(group))),
				span("aiSecurityRisk", empty()),
				span("aiSecurityRowState", message(I18NConstants.ROW_ALWAYS_GRANTED)),
				span("aiSecurityGroupNote", text(groupSentence(group)))));
		}

		List<String> groups = _draft.grantableGroups();
		for (int g = 0; g < groups.size(); g++) {
			String group = groups.get(g);
			String fieldName = triStateName(roleIndex, g);
			DynamicFragment note =
				new DynamicFragment("aiSecurityGroupNote", () -> rowNote(roleName, group));
			_rowNotes.put(fieldName, note);
			rows.add(div("aiSecurityTriRow",
				span("aiSecurityGroupLabel", text(ToolAccessLabels.groupLabel(group))),
				span("aiSecurityRisk", text(ToolAccessLabels.risk(group))),
				span("aiSecurityRowState", baselineState(group), value(context.getMember(fieldName))),
				note));
		}

		rows.add(entrySection(context, roleName, roleIndex));
		rows.add(p("aiSecurityNote", message(I18NConstants.POLICY_IMPLICIT_NOTE)));
		return div("aiSecurityRoleCard", fragments(rows));
	}

	/**
	 * The row state of a capability the baseline already grants everyone.
	 *
	 * <p>
	 * The single clearest explanation of what the baseline does, per the prototype's own feedback: the
	 * three radio buttons alone stay abstract, and this says it where the reader is deciding. The
	 * tri-state stays available underneath, because a refusal has to remain expressible against the
	 * baseline.
	 * </p>
	 */
	private HTMLFragment baselineState(String group) {
		return _draft.baselineCovers(group)
			? span("aiSecurityBaselineState", message(I18NConstants.ROW_BASELINE_GRANTED), br())
			: empty();
	}

	/** What the currently chosen state of one capability row means. */
	private HTMLFragment rowNote(String roleName, String group) {
		TriState state = _draft.triState(roleName, group);
		if (state == TriState.DENIED) {
			return span("aiSecurityWarning", message(I18NConstants.NOTE_DENY_WINS));
		}
		if (state == TriState.GRANTED && ToolGroups.DATA_WRITE.equals(group)) {
			return span(message(I18NConstants.NOTE_DATA_WRITE_APPROVAL));
		}
		return span(text(groupSentence(group)));
	}

	/** The one sentence saying what granting a capability lets an agent do. */
	private static String groupSentence(String group) {
		String sentence = ToolAccessLabels.groupTooltip(group);
		return sentence == null ? "" : sentence;
	}

	/** The entries of one role that name single tools, each of them an either-or of proposal D1. */
	private HTMLFragment entrySection(FormContext context, String roleName, int roleIndex) {
		List<Grant> grants = entriesOf(roleName, false);
		List<Grant> denies = entriesOf(roleName, true);
		List<HTMLFragment> content = new ArrayList<>();
		content.add(div("aiSecurityAdvancedHead", message(I18NConstants.POLICY_ADVANCED__COUNT
			.fill(Integer.valueOf(grants.size() + denies.size())))));
		for (int n = 0; n < grants.size(); n++) {
			content.add(entryBlock(context, roleName, roleIndex, false, n, grants.get(n)));
		}
		for (int n = 0; n < denies.size(); n++) {
			content.add(entryBlock(context, roleName, roleIndex, true, n, denies.get(n)));
		}
		content.add(div("aiSecurityEntryActions",
			action(context, "addGrant_" + roleIndex),
			action(context, "addDeny_" + roleIndex)));
		return div("aiSecurityAdvanced", fragments(content));
	}

	/**
	 * One entry, rendered as the choice it is.
	 *
	 * <p>
	 * Exactly one input is present - the group picker or the tool picker - and the summary line under it
	 * prints exactly that half. Never the empty other one, which in the raw editor reads as "this group
	 * contains no tools".
	 * </p>
	 */
	private HTMLFragment entryBlock(FormContext context, String roleName, int roleIndex, boolean denies,
			int position, Grant entry) {
		String prefix = entryName(roleIndex, denies, position);
		boolean wholeGroup = PolicyDraft.namesWholeGroup(entry);
		FormMember input = context.getMember(prefix + (wholeGroup ? "_group" : "_tools"));
		DynamicFragment summary =
			new DynamicFragment("aiSecurityEntrySummary", () -> entrySummary(entry));
		_entrySummaries.put(prefix, summary);
		return div("aiSecurityEntry",
			div("aiSecurityEntryHead",
				span(message(denies ? I18NConstants.ENTRY_DENY : I18NConstants.ENTRY_GRANT)),
				kindChoice(context, prefix, wholeGroup),
				action(context, prefix + "_remove")),
			div("aiSecurityEntryInput", labelWithColon(input), value(input)),
			summary);
	}

	/**
	 * The either-or itself: the chosen half named, the other half offered as the action that swaps it.
	 */
	private HTMLFragment kindChoice(FormContext context, String prefix, boolean wholeGroup) {
		return span("aiSecurityEntryKind",
			wholeGroup
				? span("aiSecurityChosen", message(I18NConstants.ENTRY_KIND_GROUP))
				: action(context, prefix + "_kindGroup"),
			wholeGroup
				? action(context, prefix + "_kindTools")
				: span("aiSecurityChosen", message(I18NConstants.ENTRY_KIND_TOOLS)));
	}

	/** What one entry says, in one line - exactly one half of it. */
	private HTMLFragment entrySummary(Grant entry) {
		if (PolicyDraft.namesWholeGroup(entry)) {
			return span(message(I18NConstants.ENTRY_SUMMARY_GROUP__GROUP
				.fill(ToolAccessLabels.groupLabel(entry.getGroup()))));
		}
		if (entry.getTools().isEmpty()) {
			return span("aiSecurityWarning", message(I18NConstants.ENTRY_NO_TOOL_CHOSEN));
		}
		return span(message(I18NConstants.ENTRY_SUMMARY_TOOLS__TOOLS
			.fill(String.join(", ", entry.getTools()))));
	}

	private HTMLFragment baselineSection(FormContext context) {
		List<HTMLFragment> content = new ArrayList<>();
		content.add(h4(message(I18NConstants.POLICY_BASELINE_HEADING)));
		content.add(choiceRow(context, "baseline_", BaselineChoice.values(),
			_draft.baselineChoice().name()));
		if (context.hasMember(FIELD_BASELINE_GROUPS)) {
			content.add(div("aiSecurityGroupPick",
				value(context.getMember(FIELD_BASELINE_GROUPS))));
		}
		return div("aiSecurityBaseline", fragments(content));
	}

	private HTMLFragment backgroundSection(FormContext context) {
		List<HTMLFragment> content = new ArrayList<>();
		content.add(h4(message(I18NConstants.POLICY_BACKGROUND_HEADING)));
		content.add(p("aiSecurityNote", message(I18NConstants.POLICY_BACKGROUND_NOTE)));
		content.add(choiceRow(context, "background_", BackgroundChoice.values(),
			_draft.backgroundChoice().name()));
		if (context.hasMember(FIELD_BACKGROUND_GROUPS)) {
			content.add(div("aiSecurityGroupPick",
				value(context.getMember(FIELD_BACKGROUND_GROUPS))));
		}
		return div("aiSecurityBackground", fragments(content));
	}

	/**
	 * A row of named states, the current one shown as text and the others as the action that reaches
	 * them.
	 */
	private HTMLFragment choiceRow(FormContext context, String prefix, Enum<?>[] choices,
			String current) {
		List<HTMLFragment> content = new ArrayList<>();
		for (Enum<?> choice : choices) {
			FormMember member = context.getMember(prefix + choice.name());
			content.add(choice.name().equals(current)
				? span("aiSecurityChosen", text(member.getLabel()))
				: value(BUTTONS, member));
		}
		return div("aiSecurityChoices", fragments(content));
	}

	/**
	 * Which tools the policy classifies into which capability group.
	 *
	 * <p>
	 * Read-only here: a group assignment says what a tool <em>is</em>, which is a statement about the
	 * tool and not about a role, and the catalog is where a reader meets it. It is shown because a
	 * policy that grants a capability no tool is in reaches nothing, and this is what explains that.
	 * </p>
	 */
	private HTMLFragment toolGroupSection() {
		List<HTMLFragment> content = new ArrayList<>();
		content.add(h4(message(I18NConstants.POLICY_TOOL_GROUPS_HEADING)));
		content.add(p("aiSecurityNote", message(I18NConstants.POLICY_TOOL_GROUPS_NOTE)));
		Map<String, GroupMembership> assignments = new TreeMap<>(_draft.config().getToolGroups());
		if (assignments.isEmpty()) {
			content.add(p("aiSecurityNote", message(I18NConstants.POLICY_TOOL_GROUPS_NONE)));
		}
		for (Map.Entry<String, GroupMembership> assignment : assignments.entrySet()) {
			content.add(div("aiSecurityAssignment",
				span("aiSecurityGroupLabel", text(ToolAccessLabels.groupLabel(assignment.getKey()))),
				span("aiSecurityAssignedTools",
					text(String.join(", ", assignment.getValue().getTools())))));
		}
		return div("aiSecurityToolGroups", fragments(content));
	}

	// ---------------------------------------------------------------- small helpers

	private CommandField button(String name, ResKey label, Supplier<HandlerResult> action) {
		CommandField field = FormFactory.newCommandField(name, (Command) context -> action.get());
		field.setLabel(resources().getString(label));
		return field;
	}

	private CommandField button(String name, ResKey label, Command command) {
		CommandField field = FormFactory.newCommandField(name, command);
		field.setLabel(resources().getString(label));
		return field;
	}

	/** One action, rendered as a labelled button rather than as an unlabelled icon. */
	private static Control action(FormContext context, String name) {
		return value(BUTTONS, context.getMember(name));
	}

	private static HTMLFragment fragments(List<HTMLFragment> content) {
		return concat(content.toArray(new HTMLFragment[content.size()]));
	}

	private static String triStateName(int roleIndex, int groupIndex) {
		return "tri_" + roleIndex + "_" + groupIndex;
	}

	private static String entryName(int roleIndex, boolean denies, int position) {
		return "entry_" + roleIndex + (denies ? "_d_" : "_g_") + position;
	}

	@SuppressWarnings("unchecked")
	private static <T> T single(Object value, T fallback) {
		if (value instanceof List<?> list && !list.isEmpty()) {
			return (T) list.get(0);
		}
		return fallback;
	}

	@SuppressWarnings("unchecked")
	private static List<String> strings(Object value) {
		return value instanceof List<?> list ? (List<String>) list : List.of();
	}

	/** Brings the chosen set of groups about by toggling exactly what differs. */
	private static void setGroups(List<String> chosen, Set<String> current,
			java.util.function.Consumer<String> toggle) {
		for (String group : new ArrayList<>(current)) {
			if (!chosen.contains(group)) {
				toggle.accept(group);
			}
		}
		for (String group : chosen) {
			if (!current.contains(group)) {
				toggle.accept(group);
			}
		}
	}

	private static List<String> allToolNames() {
		return new AllToolNames().apply();
	}

	private static Resources resources() {
		return Resources.getInstance();
	}

	/** How the three states of a capability row are named. */
	private static final LabelProvider TRI_STATE_LABELS = model -> model instanceof TriState state
		? ToolAccessLabels.triState(state) : String.valueOf(model);

	/** How the templates of a new role entry are named. */
	private static final LabelProvider TEMPLATE_LABELS = model -> model instanceof RoleTemplate template
		? resources().getString(ToolAccessLabels.roleTemplate(template)) : String.valueOf(model);

	/** How a capability group is named: display name and technical name, as everywhere else. */
	private static final LabelProvider GROUP_LABELS = model -> model instanceof String group
		? ToolAccessLabels.groupLabel(group) : String.valueOf(model);

	/**
	 * The order the role picker offers its options in: the application's own roles, then the framework's,
	 * then the ones a tool grant does nothing for.
	 */
	private static final Comparator<Object> BY_ROLE_KIND = (left, right) -> {
		String leftName = String.valueOf(left);
		String rightName = String.valueOf(right);
		int byKind = SecurityRoleKind.of(leftName).ordinal() - SecurityRoleKind.of(rightName).ordinal();
		return byKind != 0 ? byKind : leftName.compareToIgnoreCase(rightName);
	};

	/**
	 * How a role is offered: its name, plus what kind of role it is where that is not obvious.
	 */
	private static final LabelProvider ROLE_LABELS = model -> {
		if (!(model instanceof String roleName)) {
			return String.valueOf(model);
		}
		SecurityRoleKind kind = SecurityRoleKind.of(roleName);
		if (kind == SecurityRoleKind.APPLICATION) {
			return roleName;
		}
		return roleName + " (" + resources().getString(ToolAccessLabels.roleKind(kind)) + ")";
	};

	/** Renders a command as a button carrying its label, not as an icon nobody can read. */
	private static final ControlProvider BUTTONS = (model, name) -> {
		FormMember member = (FormMember) model;
		if (member instanceof CommandField command) {
			return new ButtonControl(command, DefaultButtonRenderer.INSTANCE);
		}
		return value(member);
	};

	/**
	 * A block of the view that is recomputed and repainted when a value below it changes.
	 *
	 * <p>
	 * Needed because the surrounding view is written once: a plain fragment has no identity in the page
	 * and therefore cannot be replaced. Only the blocks that actually depend on a value carry one.
	 * </p>
	 */
	private static final class DynamicFragment extends AbstractConstantControl {

		private final String _cssClass;

		private final Supplier<HTMLFragment> _content;

		DynamicFragment(String cssClass, Supplier<HTMLFragment> content) {
			_cssClass = cssClass;
			_content = content;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.endBeginTag();
			_content.get().write(context, out);
			out.endTag(DIV);
		}

		@Override
		protected String getTypeCssClass() {
			return _cssClass;
		}
	}

}
