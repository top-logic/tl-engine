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

/**
 * I18N constants of the "Agent-Sicherheit" administration view.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Provenance: the capability is granted to everyone without being configured.
	 *
	 * @en Granted implicitly - cannot be withheld, not even in an unattended run
	 * @de Implizit gewährt - nicht entziehbar, auch unbeaufsichtigt
	 */
	public static ResKey RULE_IMPLICIT_BASELINE;

	/**
	 * Provenance: the shipped read-only baseline applies because the policy declares none.
	 *
	 * @en Default baseline - the policy declares no baseline of its own
	 * @de Standard-Grundberechtigung - die Richtlinie legt keine eigene fest
	 */
	public static ResKey RULE_DEFAULT_BASELINE;

	/**
	 * Provenance: an entry of the policy's declared baseline, which reaches every account.
	 *
	 * @en Baseline - granted to every account
	 * @de Grundberechtigung - für jedes Konto gewährt
	 */
	public static ResKey RULE_BASELINE;

	/**
	 * Provenance: a grant of the named role.
	 *
	 * @en Grant of role {0}
	 * @de Grant der Rolle {0}
	 */
	public static ResKey1 RULE_ROLE_GRANT__ROLE;

	/**
	 * Provenance: a grant of the named role that names this single tool instead of a group.
	 *
	 * @en Grant of role {0} for this single tool
	 * @de Grant der Rolle {0} für dieses einzelne Werkzeug
	 */
	public static ResKey1 RULE_ROLE_GRANT_TOOL__ROLE;

	/**
	 * Provenance: an explicit deny of the named role, which wins over every grant.
	 *
	 * @en Deny of role {0} - a deny always wins
	 * @de Deny der Rolle {0} - ein Deny gewinnt immer
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
	 * @en Administrator - bypasses the tool layer
	 * @de Administrator - umgeht die Werkzeug-Ebene
	 */
	public static ResKey RULE_ADMIN_BYPASS;

	/**
	 * Provenance: the tool belongs to no capability group and is therefore denied to everyone.
	 *
	 * @en Assigned to no group and granted by name nowhere - denied to everyone
	 * @de Keiner Gruppe zugeordnet und nirgends namentlich gewährt - für alle gesperrt
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
	 * @en the implicit baseline
	 * @de die implizite Grundberechtigung
	 */
	public static ResKey SUBJECT_IMPLICIT_BASELINE;

	/**
	 * Names the shipped default baseline inside a sentence about another rule.
	 *
	 * @en the default baseline
	 * @de die Standard-Grundberechtigung
	 */
	public static ResKey SUBJECT_DEFAULT_BASELINE;

	/**
	 * Names the declared baseline inside a sentence about another rule.
	 *
	 * @en the baseline
	 * @de die Grundberechtigung
	 */
	public static ResKey SUBJECT_BASELINE;

	/**
	 * Names a role inside a sentence about another rule.
	 *
	 * @en role {0}
	 * @de die Rolle {0}
	 */
	public static ResKey1 SUBJECT_ROLE__ROLE;

	/**
	 * Names the administrator status inside a sentence about another rule.
	 *
	 * @en the administrator status
	 * @de der Administratorstatus
	 */
	public static ResKey SUBJECT_ADMIN;

	/**
	 * What the background allowlist took away, appended to the ceiling's own sentence.
	 *
	 * @en {0} grants it, but a run nobody watches may not use it
	 * @de {0} gewährt es, aber ein unbeaufsichtigter Lauf darf es nicht
	 */
	public static ResKey1 CAPPED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What a deny took away, appended to the deny's own sentence.
	 *
	 * @en {0} grants it
	 * @de {0} gewährt es
	 */
	public static ResKey1 DENIED_ALTHOUGH_GRANTED__SUBJECT;

	/**
	 * What the administrator bypass covered up, appended to its own sentence.
	 *
	 * @en the policy itself says: {0}
	 * @de die Richtlinie selbst sagt: {0}
	 */
	public static ResKey1 ALLOWED_ALTHOUGH_POLICY__RULE;

	/**
	 * The extra price of committing through the TL-Script tool, where the account can pay it.
	 *
	 * @en A commit (withCommit=true) additionally costs data.write, which this account holds
	 * @de Ein Commit (withCommit=true) kostet zusätzlich data.write - das hält dieses Konto
	 */
	public static ResKey SCRIPT_COMMIT_ALLOWED;

	/**
	 * The extra price of committing through the TL-Script tool, where the account cannot pay it.
	 *
	 * @en A commit (withCommit=true) additionally costs data.write, which is denied here - so this is
	 *     a read-only grant
	 * @de Ein Commit (withCommit=true) kostet zusätzlich data.write, und das ist hier verweigert -
	 *     also eine reine Lese-Berechtigung
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
	 * @de vom Werkzeug deklariert
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
	 * @en assigned nowhere - denied to everyone
	 * @de nirgends zugeordnet - für alle gesperrt
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
	 * @en No role on the security root - only the baseline applies.
	 * @de Keine Rolle auf der Sicherheits-Wurzel - es gilt nur die Grundberechtigung.
	 */
	public static ResKey HELD_ROLES_NONE;

	/**
	 * The banner an administrator account needs, because its rows prove nothing about the policy.
	 *
	 * @en {0} is an administrator and therefore reaches every tool. This account proves nothing about
	 *     the policy - check it with a non-administrator account.
	 * @de {0} ist Administrator und erreicht damit jedes Werkzeug. Dieses Konto beweist nichts über
	 *     die Richtlinie - prüfen Sie sie mit einem Konto ohne Administratorrechte.
	 */
	public static ResKey1 ADMIN_BANNER__ACCOUNT;

	/**
	 * The sentence that keeps the view from being read as an answer about data.
	 *
	 * @en This view shows the tool layer only. Which data an allowed call then sees is decided by the
	 *     model access rights, per object and per attribute.
	 * @de Diese Ansicht zeigt nur die Werkzeug-Ebene. Welche Daten ein erlaubter Aufruf dann sieht,
	 *     entscheiden die Modellrechte - pro Objekt und pro Attribut.
	 */
	public static ResKey LAYER_ONE_ONLY_NOTE;

	/**
	 * What an unattended run is, where the mode toggle is.
	 *
	 * @en An unattended run has nobody who could answer an approval prompt, so every call counts as
	 *     approved - which is why the background allowlist caps it, administrators included.
	 * @de Ein unbeaufsichtigter Lauf hat niemanden, der eine Bestätigung beantworten könnte, daher
	 *     gilt jeder Aufruf als genehmigt - deshalb begrenzt ihn die Hintergrund-Zulassungsliste
	 *     zusätzlich, auch für Administratoren.
	 */
	public static ResKey BACKGROUND_MODE_NOTE;

	/**
	 * The cross-layer warning about a role with data capabilities and no model access rights.
	 *
	 * @en Role {0} holds {1}, but appears in no Read, Write or Create grant of the model access
	 *     rights: its data tools will see nothing and change nothing, and a refused read comes back
	 *     empty rather than as an error.
	 * @de Die Rolle {0} hält {1}, kommt aber in keinem Read-, Write- oder Create-Grant der
	 *     Modellrechte vor: Ihre Daten-Werkzeuge sehen nichts und ändern nichts, und eine
	 *     verweigerte Leseoperation liefert ein leeres Ergebnis statt eines Fehlers.
	 */
	public static ResKey2 WARN_DATA_ROLE_WITHOUT_MODEL_RIGHTS__ROLE_GROUPS;

	/**
	 * The cross-layer warning about a role nobody can hold where tool access reads roles.
	 *
	 * @en Role {0} is granted tools, but no role rule of this application can produce it on the
	 *     security root, which is where tool access reads roles. Its grants reach nobody.
	 * @de Der Rolle {0} sind Werkzeuge gewährt, aber keine Rollenregel dieser Anwendung kann sie auf
	 *     der Sicherheits-Wurzel erzeugen - dort liest der Werkzeugzugriff die Rollen. Ihre Grants
	 *     erreichen niemanden.
	 */
	public static ResKey1 WARN_ROLE_UNREACHABLE_ON_ROOT__ROLE;

	/**
	 * Shown instead of the rows while the tool access service is not running.
	 *
	 * @en The agent tool access service is not running, so there is no policy to report on.
	 * @de Der Zugangsdienst für KI-Tools läuft nicht, daher gibt es keine Richtlinie zu berichten.
	 */
	public static ResKey SERVICE_NOT_RUNNING;


	static {
		initConstants(I18NConstants.class);
	}
}
