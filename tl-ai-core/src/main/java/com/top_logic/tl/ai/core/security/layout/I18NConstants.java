/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * I18N constants of the "Agent-Sicherheit" administration view.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Provenance: the capability is granted to everyone without being configured.
	 *
	 * @en Granted implicitly, not withholdable
	 * @de Implizit gewährt, nicht entziehbar
	 */
	public static ResKey RULE_IMPLICIT_BASELINE;

	/**
	 * Provenance: the shipped read-only baseline applies because the policy declares none.
	 *
	 * @en Default basic permissions, because the policy declares none
	 * @de Standard-Grundberechtigung, weil die Richtlinie keine eigene festlegt
	 */
	public static ResKey RULE_DEFAULT_BASELINE;

	/**
	 * Provenance: an entry of the policy's declared baseline, which reaches every account.
	 *
	 * @en Basic permissions, granted to every account
	 * @de Grundberechtigung, für jedes Konto gewährt
	 */
	public static ResKey RULE_BASELINE;

	/**
	 * Provenance: a grant of the named role.
	 *
	 * @en Granted by role {0}
	 * @de Berechtigung der Rolle {0}
	 */
	public static ResKey1 RULE_ROLE_GRANT__ROLE;

	/**
	 * Provenance: a grant of the named role that names this single tool instead of a group.
	 *
	 * @en Granted by role {0} for this single tool
	 * @de Berechtigung der Rolle {0} für dieses einzelne Tool
	 */
	public static ResKey1 RULE_ROLE_GRANT_TOOL__ROLE;

	/**
	 * Provenance: an explicit deny of the named role, which wins over every grant.
	 *
	 * @en Forbidden by role {0}. A prohibition always wins.
	 * @de Verbot der Rolle {0}. Ein Verbot gewinnt immer.
	 */
	public static ResKey1 RULE_ROLE_DENY__ROLE;

	/**
	 * Provenance: the background allowlist caps the run.
	 *
	 * @en Capped by the background allowlist
	 * @de Durch die Hintergrund-Zulassungsliste begrenzt
	 */
	public static ResKey RULE_BACKGROUND_CEILING;

	/**
	 * Provenance: the account is an administrator and bypasses the tool layer.
	 *
	 * @en Administrator, bypasses the tool layer
	 * @de Administrator, umgeht die Tool-Ebene
	 */
	public static ResKey RULE_ADMIN_BYPASS;

	/**
	 * Provenance: the tool belongs to no capability group and is therefore denied to everyone.
	 *
	 * @en In no group and granted by name nowhere, so denied to everyone
	 * @de In keiner Gruppe und nirgends namentlich gewährt, daher für alle gesperrt
	 */
	public static ResKey RULE_UNCLASSIFIED;

	/**
	 * Provenance: nothing in the policy grants the capability.
	 *
	 * @en No role grants it
	 * @de Keine Rolle gewährt es
	 */
	public static ResKey RULE_NOT_GRANTED;

	/**
	 * Names the implicit baseline inside a sentence about another rule.
	 *
	 * @en The implicit basic permissions
	 * @de Die implizite Grundberechtigung
	 */
	public static ResKey SUBJECT_IMPLICIT_BASELINE;

	/**
	 * Names the shipped default baseline inside a sentence about another rule.
	 *
	 * @en The default basic permissions
	 * @de Die Standard-Grundberechtigung
	 */
	public static ResKey SUBJECT_DEFAULT_BASELINE;

	/**
	 * Names the declared baseline inside a sentence about another rule.
	 *
	 * @en The basic permissions
	 * @de Die Grundberechtigung
	 */
	public static ResKey SUBJECT_BASELINE;

	/**
	 * Names a role inside a sentence about another rule.
	 *
	 * @en Role {0}
	 * @de Die Rolle {0}
	 */
	public static ResKey1 SUBJECT_ROLE__ROLE;

	/**
	 * Names the administrator status inside a sentence about another rule.
	 *
	 * @en Administrator status
	 * @de Der Administratorstatus
	 */
	public static ResKey SUBJECT_ADMIN;

	/**
	 * What the background allowlist took away, appended to the ceiling's own sentence.
	 *
	 * @en {0} grants it, but a run nobody watches may not use it.
	 * @de {0} gewährt es, aber ein unbeaufsichtigter Lauf darf es nicht.
	 */
	public static ResKey1 CAPPED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What a deny took away, appended to the deny's own sentence.
	 *
	 * @en {0} grants it.
	 * @de {0} gewährt es.
	 */
	public static ResKey1 DENIED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What the administrator bypass covered up, appended to its own sentence.
	 *
	 * @en The policy itself says: {0}.
	 * @de Die Richtlinie selbst sagt: {0}.
	 */
	public static ResKey1 ALLOWED_ALTHOUGH_POLICY__RULE;

	/**
	 * The extra price of committing through the TL-Script tool, where the account can pay it.
	 *
	 * @en A commit (withCommit=true) additionally costs data.write, which this account holds
	 * @de Ein Commit (withCommit=true) kostet zusätzlich data.write, und das hält dieses Konto
	 */
	public static ResKey SCRIPT_COMMIT_ALLOWED;

	/**
	 * The extra price of committing through the TL-Script tool, where the account cannot pay it.
	 *
	 * @en A commit (withCommit=true) additionally costs data.write, which is denied here
	 * @de Ein Commit (withCommit=true) kostet zusätzlich data.write, und das ist hier verweigert
	 */
	public static ResKey SCRIPT_COMMIT_DENIED;

	/**
	 * Verdict word for a capability the account may use.
	 *
	 * @en Allowed
	 * @de Gewährt
	 */
	public static ResKey VERDICT_ALLOWED;

	/**
	 * Verdict word for a capability the account may not use.
	 *
	 * @en Denied
	 * @de Verweigert
	 */
	public static ResKey VERDICT_DENIED;

	/**
	 * Group source: the tool declares its capability group itself, in its own code.
	 *
	 * @en declared by the tool
	 * @de vom Tool deklariert
	 */
	public static ResKey GROUP_SOURCE_DECLARED;

	/**
	 * Group source: a group assignment of the policy puts the tool into its group.
	 *
	 * @en assigned by the policy
	 * @de per Richtlinie zugeordnet
	 */
	public static ResKey GROUP_SOURCE_CONFIGURED;

	/**
	 * Group source: nothing classified the tool, which is why nobody but an administrator reaches it.
	 *
	 * @en assigned nowhere, denied to everyone
	 * @de nirgends zugeordnet, für alle gesperrt
	 */
	public static ResKey GROUP_SOURCE_NONE;

	/**
	 * The capability group of a tool that has none.
	 *
	 * @en NO GROUP
	 * @de KEINE GRUPPE
	 */
	public static ResKey GROUP_NONE;

	/**
	 * Risk marker of a capability whose tools can express any other capability.
	 *
	 * @en privileged
	 * @de privilegiert
	 */
	public static ResKey RISK_PRIVILEGED;

	/**
	 * Risk marker of a capability whose tools send data out of the application.
	 *
	 * @en sends data outward
	 * @de sendet Daten nach außen
	 */
	public static ResKey RISK_EGRESS;

	/**
	 * Risk marker of a capability every account holds whether or not it is configured.
	 *
	 * @en granted implicitly
	 * @de implizit gewährt
	 */
	public static ResKey RISK_IMPLICIT;

	/**
	 * Reachability of a tool that every account reaches through the baseline.
	 *
	 * @en everyone (baseline)
	 * @de alle (Grundberechtigung)
	 */
	public static ResKey REACHABLE_EVERYONE;

	/**
	 * Reachability of a tool that no configured role and no baseline reaches.
	 *
	 * @en nobody
	 * @de niemand
	 */
	public static ResKey REACHABLE_NOBODY;

	/**
	 * The run mode with a user who can answer an approval prompt.
	 *
	 * @en interactive
	 * @de interaktiv
	 */
	public static ResKey MODE_INTERACTIVE;

	/**
	 * The run mode with nobody to answer an approval prompt.
	 *
	 * @en background (unattended)
	 * @de Hintergrund (unbeaufsichtigt)
	 */
	public static ResKey MODE_BACKGROUND;

	/**
	 * How a hypothetical role is offered in the account picker.
	 *
	 * @en Anybody holding {0}
	 * @de Wer {0} hält
	 */
	public static ResKey1 SUBJECT_ROLE_OPTION__ROLE;

	/**
	 * The roles the chosen account holds on the security root, shown next to the picker.
	 *
	 * @en Roles on the security root: {0}
	 * @de Rollen auf der Sicherheits-Wurzel: {0}
	 */
	public static ResKey1 HELD_ROLES__ROLES;

	/**
	 * Shown for an account that holds no role at all beyond what every account holds.
	 *
	 * @en No role on the security root. Only the basic permissions apply.
	 * @de Keine Rolle auf der Sicherheits-Wurzel. Es gilt nur die Grundberechtigung.
	 */
	public static ResKey HELD_ROLES_NONE;

	/**
	 * The banner an administrator account needs, because its rows prove nothing about the policy.
	 *
	 * @en Administrators bypass the tool layer, so {0} reaches every tool. Check the policy with a
	 *     non-administrator account.
	 * @de Administratoren umgehen die Tool-Ebene, daher erreicht {0} jedes Tool. Prüfen Sie die Richtlinie
	 *     mit einem Konto ohne Administratorrechte.
	 */
	public static ResKey1 ADMIN_BANNER__ACCOUNT;

	/**
	 * The sentence that keeps the view from being read as an answer about data.
	 *
	 * @en This view shows the tool layer only. Which data an allowed call then sees is decided by the
	 *     model access rights.
	 * @de Diese Ansicht zeigt nur die Tool-Ebene. Welche Daten ein erlaubter Aufruf dann sieht,
	 *     entscheiden die Modellrechte.
	 */
	public static ResKey LAYER_ONE_ONLY_NOTE;

	/**
	 * What an unattended run is, where the mode toggle is.
	 *
	 * @en An unattended run has nobody to answer an approval prompt, so every call counts as approved. The
	 *     background allowlist is what caps it, administrators included.
	 * @de Ein unbeaufsichtigter Lauf hat niemanden, der eine Bestätigung beantworten kann, daher gilt
	 *     jeder Aufruf als genehmigt. Begrenzt wird er durch die Hintergrund-Zulassungsliste, auch für
	 *     Administratoren.
	 */
	public static ResKey BACKGROUND_MODE_NOTE;

	/**
	 * The cross-layer warning about a role with data capabilities and no model access rights.
	 *
	 * @en Role {0} holds {1}, but appears in no Read, Write or Create grant of the model access
	 *     rights: its data tools will see nothing and change nothing, and a refused read comes back
	 *     empty rather than as an error.
	 * @de Die Rolle {0} hält {1}, kommt aber in keinem Read-, Write- oder Create-Grant der
	 *     Modellrechte vor: Ihre Daten-Tools sehen nichts und ändern nichts, und eine
	 *     verweigerte Leseoperation liefert ein leeres Ergebnis statt eines Fehlers.
	 */
	public static ResKey2 WARN_DATA_ROLE_WITHOUT_MODEL_RIGHTS__ROLE_GROUPS;

	/**
	 * The cross-layer warning about a role nobody can hold where tool access reads roles.
	 *
	 * @en Role {0} is granted tools, but no role rule of this application can produce it on the security
	 *     root. Its permissions reach nobody.
	 * @de Der Rolle {0} sind Tools gewährt, aber keine Rollenregel dieser Anwendung kann sie auf der
	 *     Sicherheits-Wurzel erzeugen. Ihre Berechtigungen erreichen niemanden.
	 */
	public static ResKey1 WARN_ROLE_UNREACHABLE_ON_ROOT__ROLE;

	/**
	 * Shown instead of the rows while the tool access service is not running.
	 *
	 * @en The agent tool access service is not running, so there is no policy to report on.
	 * @de Der Zugangsdienst für KI-Tools läuft nicht, daher gibt es keine Richtlinie zu berichten.
	 */
	public static ResKey SERVICE_NOT_RUNNING;

	/**
	 * Baseline state: the shipped read-only default applies.
	 *
	 * @en Standard: read the model and the data
	 * @de Standard: Modell und Daten lesen
	 * @tooltip No element is written, so every account may read the model and the business data.
	 */
	public static ResKey BASELINE_DEFAULT;

	/**
	 * Baseline state: nothing is granted to everyone.
	 *
	 * @en Nothing
	 * @de Nichts
	 * @tooltip Without a role nobody gets anything, apart from the two capabilities no policy can
	 *     withhold.
	 */
	public static ResKey BASELINE_NOTHING;

	/**
	 * Baseline state: an own list of capabilities, replacing the default.
	 *
	 * @en Own list
	 * @de Eigene Liste
	 * @tooltip Exactly what is chosen below, replacing the standard. An empty list is the same as Nothing.
	 */
	public static ResKey BASELINE_CUSTOM;

	/**
	 * Background limit state: nothing is stated, which no policy naming a role may leave.
	 *
	 * @en Not stated
	 * @de Nicht festgelegt
	 * @tooltip Refused as soon as the policy names a role. What an unattended run may reach has to be
	 *     said.
	 */
	public static ResKey BACKGROUND_UNDECLARED;

	/**
	 * Background limit state: no limit of its own.
	 *
	 * @en Everything the roles allow
	 * @de Alles, was die Rollen erlauben
	 * @tooltip Gives up the only control that stands in for the missing confirmation.
	 */
	public static ResKey BACKGROUND_ALL;

	/**
	 * Background limit state: an own list of capabilities.
	 *
	 * @en Own list
	 * @de Eigene Liste
	 * @tooltip Only what is chosen below is reachable unattended, administrators included.
	 */
	public static ResKey BACKGROUND_CUSTOM;

	/**
	 * Role group of the picker: the roles of this application.
	 *
	 * @en Application roles
	 * @de Anwendungsrollen
	 */
	public static ResKey ROLE_KIND_APPLICATION;

	/**
	 * Role group of the picker: roles of the framework whose meaning has to be known.
	 *
	 * @en Framework role
	 * @de Framework-Rolle
	 */
	public static ResKey ROLE_KIND_FRAMEWORK;

	/**
	 * Role group of the picker: roles a tool grant does nothing for.
	 *
	 * @en Internal role, reaches no account
	 * @de Interne Rolle, erreicht kein Konto
	 */
	public static ResKey ROLE_KIND_INTERNAL;

	/**
	 * Explains the role every logged-in account holds.
	 *
	 * @en Every logged-in account holds this role. A permission here applies to all users.
	 * @de Diese Rolle hält jedes angemeldete Konto. Eine Berechtigung hier gilt für alle Benutzer.
	 */
	public static ResKey ROLE_NOTE_EVERY_ACCOUNT;

	/**
	 * Explains the role administrators hold.
	 *
	 * @en Administrators bypass the tool layer anyway.
	 * @de Administratoren umgehen die Tool-Ebene ohnehin.
	 */
	public static ResKey ROLE_NOTE_ADMINISTRATOR;

	/**
	 * Explains a role the framework uses for its own user interface.
	 *
	 * @en A role of the framework user interface. A tool permission for it reaches nobody.
	 * @de Eine Rolle der Framework-Oberfläche. Eine Tool-Berechtigung für sie erreicht niemanden.
	 */
	public static ResKey ROLE_NOTE_INTERNAL;

	/**
	 * Warns in the picker about a role no rule can produce where tool access reads roles.
	 *
	 * @en No role rule of this application can produce this role on the security root. Its permissions
	 *     reach nobody.
	 * @de Keine Rollenregel dieser Anwendung kann diese Rolle auf der Sicherheits-Wurzel erzeugen. Ihre
	 *     Berechtigungen erreichen niemanden.
	 */
	public static ResKey ROLE_NOTE_UNREACHABLE;

	/**
	 * Template that grants nothing.
	 *
	 * @en Empty
	 * @de Leer beginnen
	 */
	public static ResKey TEMPLATE_NONE;

	/**
	 * Template of a role that reads and changes nothing.
	 *
	 * @en Read-only assistant
	 * @de Nur-Lese-Assistent
	 */
	public static ResKey TEMPLATE_READ_ONLY;

	/**
	 * Template of a role that also changes business data.
	 *
	 * @en Case worker
	 * @de Sachbearbeiter
	 */
	public static ResKey TEMPLATE_CLERK;

	/**
	 * Template of a role that also runs TL-Script.
	 *
	 * @en Developer
	 * @de Entwickler
	 */
	public static ResKey TEMPLATE_DEVELOPER;

	/**
	 * Refusal: the policy names a role the application does not have.
	 *
	 * @en The role {0} does not exist. The next application start would be refused.
	 * @de Die Rolle {0} existiert nicht. Der nächste Anwendungsstart würde abgewiesen.
	 */
	public static ResKey1 REFUSE_UNKNOWN_ROLE__ROLE;

	/**
	 * Refusal: an entry names neither a capability group nor tools.
	 *
	 * @en {0}, entry {1} names nothing. An entry names either a whole capability or single tools.
	 * @de {0}, Eintrag {1} benennt nichts. Ein Eintrag nennt entweder eine ganze Tool-Gruppe oder einzelne
	 *     Tools.
	 */
	public static ResKey2 REFUSE_ENTRY_NAMES_NOTHING__WHERE_POSITION;

	/**
	 * Refusal: an entry names a capability group and tools at once.
	 *
	 * @en {0}, entry {1} names a capability and single tools at once. An entry names exactly one of the
	 *     two.
	 * @de {0}, Eintrag {1} benennt eine Tool-Gruppe und einzelne Tools zugleich. Ein Eintrag nennt genau
	 *     eines von beiden.
	 */
	public static ResKey2 REFUSE_ENTRY_NAMES_BOTH__WHERE_POSITION;

	/**
	 * Refusal: the policy names roles and says nothing about unattended runs.
	 *
	 * @en No limit is stated for runs where nobody can confirm a call. A policy that names roles has to
	 *     state one.
	 * @de Für Läufe, in denen niemand einen Aufruf bestätigen kann, ist keine Obergrenze festgelegt. Eine
	 *     Richtlinie, die Rollen nennt, muss eine festlegen.
	 */
	public static ResKey REFUSE_BACKGROUND_ALLOWLIST_UNDECLARED;

	/**
	 * Names the baseline inside a message about one of its entries.
	 *
	 * @en Basic permissions
	 * @de Grundberechtigungen
	 */
	public static ResKey WHERE_BASELINE;

	/**
	 * Names the background limit inside a message about one of its entries.
	 *
	 * @en Background allow list
	 * @de Hintergrund-Zulassungsliste
	 */
	public static ResKey WHERE_BACKGROUND_ALLOWLIST;

	/**
	 * Names a role's permissions inside a message about one of its entries.
	 *
	 * @en Role {0}, permissions
	 * @de Rolle {0}, Berechtigungen
	 */
	public static ResKey1 WHERE_ROLE_GRANTS__ROLE;

	/**
	 * Names a role's refusals inside a message about one of its entries.
	 *
	 * @en Role {0}, refusals
	 * @de Rolle {0}, Verbote
	 */
	public static ResKey1 WHERE_ROLE_DENIES__ROLE;

	/**
	 * Warning: a role may run TL-Script and change data, which together express every change.
	 *
	 * @en Role {0} may run TL-Script and change data: every data change is expressible as a script.
	 *     Each one still asks the user to confirm.
	 * @de Die Rolle {0} darf TL-Script ausführen und Daten ändern: Per Skript ist jede
	 *     Datenänderung ausdrückbar. Jede bleibt im Gespräch bestätigungspflichtig.
	 */
	public static ResKey1 WARN_SCRIPT_WITH_DATA_WRITE__ROLE;

	/**
	 * Warning: a role may run TL-Script but not change data, so scripts cannot persist anything.
	 *
	 * @en Role {0} may execute TL-Script but not change data. Persisting a change made by a script is
	 *     refused.
	 * @de Die Rolle {0} darf TL-Script ausführen, aber keine Daten ändern. Das Persistieren von
	 *     Skript-Änderungen wird verweigert.
	 */
	public static ResKey1 WARN_SCRIPT_WITHOUT_DATA_WRITE__ROLE;

	/**
	 * Warning: a role may read data and send data outward.
	 *
	 * @en Role {0} may read data and send data out of the application: what it reads can leave.
	 * @de Die Rolle {0} darf Daten lesen und Daten nach außen senden: Gelesenes kann die Anwendung
	 *     verlassen.
	 */
	public static ResKey1 WARN_DATA_AND_EGRESS__ROLE;

	/**
	 * Warning: a role may author agents, and can therefore grant itself anything.
	 *
	 * @en Role {0} may create and wire agents. An agent acting for it can grant itself every tool.
	 * @de Die Rolle {0} darf Agenten anlegen und verknüpfen. Ein Agent in ihrem Auftrag kann sich jedes
	 *     Tool selbst gewähren.
	 */
	public static ResKey1 WARN_AGENT_AUTHORING__ROLE;

	/**
	 * Warning: the policy configures a role the framework uses internally.
	 *
	 * @en Role {0} is a role of the framework's user interface. Its entry reaches nobody and is
	 *     probably a mistake.
	 * @de Die Rolle {0} ist eine Rolle der Framework-Oberfläche. Ihr Eintrag erreicht niemanden und
	 *     ist vermutlich ein Versehen.
	 */
	public static ResKey1 WARN_INTERNAL_ROLE__ROLE;

	/**
	 * Warning: the policy grants something to the role every account holds.
	 *
	 * @en Every logged-in account holds role {0}. These permissions apply to all users.
	 * @de Die Rolle {0} hält jedes angemeldete Konto. Diese Berechtigungen gelten für alle Benutzer.
	 */
	public static ResKey1 WARN_EVERY_ACCOUNT_ROLE__ROLE;

	/**
	 * Warning: unattended runs have no limit of their own.
	 *
	 * @en Unattended runs may use every tool. Every call approves itself, writing and outward-sending ones
	 *     included.
	 * @de Unbeaufsichtigte Läufe dürfen jedes Tool. Jeder Aufruf genehmigt sich selbst, auch schreibende
	 *     und nach außen sendende.
	 */
	public static ResKey WARN_BACKGROUND_ALL;

	/**
	 * Warning: unattended runs may run TL-Script but not persist what it changes.
	 *
	 * @en Runs nobody watches may run TL-Script. They cannot persist what a script changes, because
	 *     that needs the permission to change data, which is not on the list.
	 * @de Unbeaufsichtigte Läufe dürfen TL-Script ausführen. Persistieren können sie nichts, denn
	 *     dafür braucht es das Ändern von Daten, und das steht nicht auf der Liste.
	 */
	public static ResKey WARN_BACKGROUND_SCRIPT;

	/**
	 * Warning: unattended runs may run TL-Script and persist what it changes.
	 *
	 * @en Unattended runs may execute TL-Script and change data. They can persist every change a script
	 *     expresses, with nobody confirming.
	 * @de Unbeaufsichtigte Läufe dürfen TL-Script ausführen und Daten ändern. Sie können jede per Skript
	 *     ausdrückbare Änderung persistieren, ohne Bestätigung.
	 */
	public static ResKey WARN_BACKGROUND_SCRIPT_AND_DATA_WRITE;

	/**
	 * Heading of the block listing what a policy does that its author probably did not mean.
	 *
	 * @en What this policy does that may surprise
	 * @de Hinweise zur Wirkung dieser Richtlinie
	 */
	public static ResKey WARNINGS_HEADING;

	/**
	 * Heading of the block listing why an apply was refused.
	 *
	 * @en Not applied. This policy would stop the next application start:
	 * @de Nicht übernommen. Diese Richtlinie würde den nächsten Anwendungsstart verhindern:
	 */
	public static ResKey REFUSALS_HEADING;

	/**
	 * What the policy tab is for, above everything else.
	 *
	 * @en Who may use which capability. Every change is checked when you apply it.
	 * @de Wer welche Tool-Gruppe benutzen darf. Jede Änderung wird beim Übernehmen geprüft.
	 */
	public static ResKey POLICY_INTRO;

	/**
	 * Heading of the block choosing what every account gets.
	 *
	 * @en Basic permissions for every account
	 * @de Grundberechtigung für jedes Konto
	 * @tooltip What every account may do without holding any role.
	 */
	public static ResKey POLICY_BASELINE_HEADING;

	/**
	 * Heading of the block choosing the limit for unattended runs.
	 *
	 * @en Limit for unattended runs
	 * @de Obergrenze für unbeaufsichtigte Läufe
	 * @tooltip What a scheduled or asynchronous run may reach.
	 */
	public static ResKey POLICY_BACKGROUND_HEADING;

	/**
	 * What the limit for unattended runs is, where it is chosen.
	 *
	 * @en A run with nobody to answer a confirmation approves every call itself. This limit applies to
	 *     everyone, administrators included.
	 * @de Ein Lauf, in dem niemand eine Bestätigung beantworten kann, genehmigt jeden Aufruf selbst. Diese
	 *     Obergrenze gilt für alle, auch für Administratoren.
	 */
	public static ResKey POLICY_BACKGROUND_NOTE;

	/**
	 * Heading of the block listing which tools the policy classifies into which group.
	 *
	 * @en Group assignments
	 * @de Gruppen-Zuordnungen
	 * @tooltip Which tools belong to which capability group. This says what a tool is, not what a role may
	 *     do.
	 */
	public static ResKey POLICY_TOOL_GROUPS_HEADING;

	/**
	 * What the group assignments are, where they are shown.
	 *
	 * @en Tools of a connected server and tools authored in the running application declare no group of
	 *     their own. An assignment here is what makes them reachable at all.
	 * @de Tools eines verbundenen Servers und in der Anwendung angelegte Tools deklarieren selbst keine
	 *     Gruppe. Erst eine Zuordnung hier macht sie überhaupt erreichbar.
	 */
	public static ResKey POLICY_TOOL_GROUPS_NOTE;

	/**
	 * Shown where the group assignments would be, when there are none.
	 *
	 * @en No assignment. Every tool is in the group it declares itself.
	 * @de Keine Zuordnung. Jedes Tool steht in der Gruppe, die es selbst deklariert.
	 */
	public static ResKey POLICY_TOOL_GROUPS_NONE;

	/**
	 * Shown instead of the role cards while the policy configures no role.
	 *
	 * @en No role is configured. No account reaches more than the basic permissions.
	 * @de Keine Rolle konfiguriert. Kein Konto erreicht mehr als die Grundberechtigung.
	 */
	public static ResKey POLICY_NO_ROLES;

	/**
	 * Row state of a capability no policy can withhold.
	 *
	 * @en Always granted.
	 * @de Immer gewährt.
	 */
	public static ResKey ROW_ALWAYS_GRANTED;

	/**
	 * Row state of a capability the basic permissions already grant to everyone.
	 *
	 * @en granted to everyone (basic permissions)
	 * @de für alle gewährt (Grundberechtigung)
	 */
	public static ResKey ROW_BASELINE_GRANTED;

	/**
	 * The note a granted data-changing row carries.
	 *
	 * @en Every call still asks the user to confirm.
	 * @de Jeder Aufruf bleibt für den Nutzer bestätigungspflichtig.
	 */
	public static ResKey NOTE_DATA_WRITE_APPROVAL;

	/**
	 * Label of an entry that permits something.
	 *
	 * @en Permission
	 * @de Berechtigung
	 */
	public static ResKey ENTRY_GRANT;

	/**
	 * Label of an entry that refuses something.
	 *
	 * @en Refusal
	 * @de Verbot
	 */
	public static ResKey ENTRY_DENY;

	/**
	 * The either-or choice of an entry: a whole capability group.
	 *
	 * @en whole group
	 * @de ganze Gruppe
	 */
	public static ResKey ENTRY_KIND_GROUP;

	/**
	 * The either-or choice of an entry: single tools.
	 *
	 * @en single tools
	 * @de einzelne Tools
	 */
	public static ResKey ENTRY_KIND_TOOLS;

	/**
	 * Shown on an entry that names single tools and has none chosen yet.
	 *
	 * @en No tool chosen yet. This entry names nothing and would be refused.
	 * @de Noch kein Tool gewählt. Dieser Eintrag benennt nichts und wird beim Übernehmen abgewiesen.
	 */
	public static ResKey ENTRY_NO_TOOL_CHOSEN;

	/**
	 * The button that makes the saved policy take effect.
	 *
	 * @en Restart the service now
	 * @de Dienst jetzt neu starten
	 */
	public static ResKey COMMAND_RESTART;

	/**
	 * The button that adds an entry for a role.
	 *
	 * @en Add role
	 * @de Rolle hinzufügen
	 */
	public static ResKey COMMAND_ADD_ROLE;

	/**
	 * The button that removes a role's entry.
	 *
	 * @en Remove role
	 * @de Rolle entfernen
	 */
	public static ResKey COMMAND_REMOVE_ROLE;

	/**
	 * The button that adds an entry naming single tools to permit.
	 *
	 * @en Add tool permission
	 * @de Tool-Berechtigung hinzufügen
	 */
	public static ResKey COMMAND_ADD_GRANT_ENTRY;

	/**
	 * The button that adds an entry naming single tools to refuse.
	 *
	 * @en Add tool refusal
	 * @de Tool-Verbot hinzufügen
	 */
	public static ResKey COMMAND_ADD_DENY_ENTRY;

	/**
	 * The button that removes one entry.
	 *
	 * @en Remove
	 * @de Entfernen
	 */
	public static ResKey COMMAND_REMOVE_ENTRY;

	/**
	 * Label of the role picker of the add-role row.
	 *
	 * @en Role
	 * @de Rolle
	 * @tooltip The security role to take into the policy.
	 */
	public static ResKey FIELD_NEW_ROLE;

	/**
	 * Label of the template picker of the add-role row.
	 *
	 * @en Template
	 * @de Vorlage
	 * @tooltip Prefills the capabilities of the new role. Every one of them can be changed afterwards.
	 */
	public static ResKey FIELD_TEMPLATE;

	/**
	 * Label of the capability group of an entry.
	 *
	 * @en Capability
	 * @de Tool-Gruppe
	 * @tooltip The capability group this entry is about.
	 */
	public static ResKey FIELD_ENTRY_GROUP;

	/**
	 * Label of the tools of an entry.
	 *
	 * @en Tools
	 * @de Tools
	 * @tooltip The tools this entry is about, by their qualified name.
	 */
	public static ResKey FIELD_ENTRY_TOOLS;

	/**
	 * Shown next to the apply button while nothing has been changed.
	 *
	 * @en No unsaved change.
	 * @de Keine ungespeicherten Änderungen.
	 */
	public static ResKey DIRTY_NONE;

	/**
	 * Shown next to the apply button while there are unsaved changes.
	 *
	 * @en Unsaved changes. Apply checks and saves them.
	 * @de Ungespeicherte Änderungen. Übernehmen prüft und speichert.
	 */
	public static ResKey DIRTY_UNSAVED;

	/**
	 * The confirmation of a successful apply, which also says what was overwritten.
	 *
	 * @en Policy checked and saved. It overwrites the whole tool access policy of the configuration
	 *     files and takes effect when the service is restarted.
	 * @de Richtlinie geprüft und gespeichert. Sie überschreibt die gesamte Richtlinie der
	 *     Konfigurationsdateien und wird mit dem Neustart des Dienstes wirksam.
	 */
	public static ResKey APPLY_SAVED;

	/**
	 * The confirmation that the changes were thrown away.
	 *
	 * @en Changes discarded.
	 * @de Änderungen verworfen.
	 */
	public static ResKey DISCARDED;

	/**
	 * The confirmation that the saved policy is now the one being applied.
	 *
	 * @en The service was restarted. The saved policy is now in effect.
	 * @de Der Dienst wurde neu gestartet. Die gespeicherte Richtlinie ist jetzt wirksam.
	 */
	public static ResKey RESTART_DONE;

	/**
	 * The banner shown while the saved policy is not the one being applied.
	 *
	 * @en Policy saved, but not yet in effect. The service keeps applying the policy of its last
	 *     start, and so do all agent runs.
	 * @de Richtlinie gespeichert, aber noch nicht wirksam. Der Dienst wendet weiterhin die Richtlinie
	 *     seines letzten Starts an, und alle Agentenläufe ebenso.
	 */
	public static ResKey BANNER_SAVED_NOT_EFFECTIVE;

	/**
	 * Reported when the policy cannot be written.
	 *
	 * @en The tool access policy could not be written. Nothing was changed.
	 * @de Die Richtlinie konnte nicht geschrieben werden. Es wurde nichts geändert.
	 */
	public static ResKey POLICY_SAVE_FAILED;

	/**
	 * Reported when the service does not come up again.
	 *
	 * @en The tool access service did not start again, so no agent run is possible. The reason is in the
	 *     application log.
	 * @de Der Zugangsdienst für KI-Tools ist nicht wieder gestartet, daher ist kein Agentenlauf möglich.
	 *     Der Grund steht im Anwendungsprotokoll.
	 */
	public static ResKey POLICY_RESTART_FAILED;

	/**
	 * Shown instead of the editor while the saved configuration cannot be read.
	 *
	 * @en The saved tool access configuration cannot be read. There is nothing to edit here.
	 * @de Die gespeicherte Konfiguration des Tool-Zugriffs kann nicht gelesen werden. Hier gibt es nichts
	 *     zu bearbeiten.
	 */
	public static ResKey POLICY_UNREADABLE;

	/**
	 * Marks a tool description that is the text the language model is given rather than an
	 * administrator-facing one.
	 *
	 * @en (Description for the model, EN) {0}
	 * @de (Beschreibung für das Modell, EN) {0}
	 */
	/*
	 * The marker comes first because the cell it lands in truncates: appended, it is the part a reader
	 * never sees, which is the opposite of what marking a fallback is for.
	 */
	public static ResKey1 DESCRIPTION_FROM_MODEL__TEXT;

	/**
	 * Shown instead of the buttons for an account that may look at the policy but not change it.
	 *
	 * @en You may look at this policy but not change it. Changing it needs write permission on the
	 *     security root.
	 * @de Sie dürfen diese Richtlinie ansehen, aber nicht ändern. Dafür ist die Schreibberechtigung
	 *     auf der Sicherheits-Wurzel nötig.
	 */
	public static ResKey POLICY_READ_ONLY;

	static {
		initConstants(I18NConstants.class);
	}
	/**
	 * What a collapsed role section says about its content.
	 *
	 * @en {0} granted, {1} forbidden, {2} tool entries
	 * @de {0} gewährt, {1} verboten, {2} Tool-Einträge
	 */
	public static ResKey3 ROLE_SUMMARY__GRANTED_DENIED_ENTRIES;

	/**
	 * How many entries a collapsed section holds.
	 *
	 * @en ({0})
	 * @de ({0})
	 */
	public static ResKey1 SECTION_COUNT__COUNT;

	/**
	 * What the three states of a capability mean, once at the top of a role's section.
	 *
	 * @en Empty means this role says nothing about it. Ticked grants it, the third state forbids it and
	 *     beats every other role and the basic permissions.
	 * @de Leer heißt, dass diese Rolle nichts dazu sagt. Angehakt gewährt, der dritte Zustand verbietet
	 *     und schlägt jede andere Rolle und die Grundberechtigung.
	 */
	public static ResKey CAPABILITY_STATES_NOTE;

	/**
	 * Heading of the section holding the entries of one role that name single tools.
	 *
	 * @en Single tools
	 * @de Einzelne Tools
	 * @tooltip Permissions and prohibitions for named tools instead of a whole capability.
	 */
	public static ResKey SECTION_TOOL_ENTRIES;

	/**
	 * Heading of the section that takes another role into the policy.
	 *
	 * @en Add a role
	 * @de Rolle aufnehmen
	 * @tooltip Takes another security role into the policy. What it may do is then decided in its own
	 *     section.
	 */
	public static ResKey SECTION_ADD_ROLE;

	/**
	 * Row state of a capability this role says nothing about, granted by another configured role.
	 *
	 * @en granted by {0}
	 * @de wird von {0} gewährt
	 */
	public static ResKey1 ROW_GRANTED_BY_ROLE__ROLE;

	/**
	 * Label of the choice deciding what one entry names.
	 *
	 * @en Names
	 * @de Benennt
	 * @tooltip Whether this entry is about a whole capability or about named tools.
	 */
	public static ResKey FIELD_ENTRY_KIND;

	/**
	 * Label of the choice deciding what every account gets.
	 *
	 * @en Every account gets
	 * @de Jedes Konto bekommt
	 * @tooltip What every account may do before any role is taken into account.
	 */
	public static ResKey FIELD_BASELINE_CHOICE;

	/**
	 * Label of the capabilities every account gets.
	 *
	 * @en Capabilities
	 * @de Tool-Gruppen
	 * @tooltip The capabilities every account gets. Nothing chosen means nobody gets anything without a
	 *     role.
	 */
	public static ResKey FIELD_BASELINE_GROUPS;

	/**
	 * Label of the single tools every account gets.
	 *
	 * @en Single tools
	 * @de Einzelne Tools
	 * @tooltip Named tools every account may call, in addition to the capabilities above.
	 */
	public static ResKey FIELD_BASELINE_TOOLS;

	/**
	 * Label of the choice deciding what an unattended run may reach.
	 *
	 * @en Unattended runs may use
	 * @de Unbeaufsichtigte Läufe dürfen
	 * @tooltip The upper limit for a run that has nobody to confirm a call.
	 */
	public static ResKey FIELD_BACKGROUND_CHOICE;

	/**
	 * Label of the capabilities an unattended run may reach.
	 *
	 * @en Capabilities
	 * @de Tool-Gruppen
	 * @tooltip The capabilities an unattended run may reach.
	 */
	public static ResKey FIELD_BACKGROUND_GROUPS;

	/**
	 * Label of the single tools an unattended run may call.
	 *
	 * @en Single tools
	 * @de Einzelne Tools
	 * @tooltip Named tools an unattended run may call, in addition to the capabilities above.
	 */
	public static ResKey FIELD_BACKGROUND_TOOLS;

	/**
	 * Label of the capability group an assignment puts tools into.
	 *
	 * @en Capability
	 * @de Tool-Gruppe
	 * @tooltip The capability group the tools next to it belong to.
	 */
	public static ResKey FIELD_ASSIGNMENT_GROUP;

	/**
	 * Label of the tools an assignment puts into a capability group.
	 *
	 * @en Tools
	 * @de Tools
	 * @tooltip The tools in this group. Removing the last one removes the assignment.
	 */
	public static ResKey FIELD_ASSIGNMENT_TOOLS;

	/**
	 * The button that starts another group assignment.
	 *
	 * @en Add an assignment
	 * @de Zuordnung hinzufügen
	 */
	public static ResKey COMMAND_ADD_ASSIGNMENT;

	/**
	 * The button that removes one group assignment.
	 *
	 * @en Remove
	 * @de Entfernen
	 */
	public static ResKey COMMAND_REMOVE_ASSIGNMENT;

	/**
	 * Shown when every known capability group already has an assignment.
	 *
	 * @en Every known capability group already has an assignment.
	 * @de Jede bekannte Tool-Gruppe hat bereits eine Zuordnung.
	 */
	public static ResKey ASSIGNMENT_NOTHING_TO_ADD;

}
