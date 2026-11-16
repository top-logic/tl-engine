/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey5;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No redirect URI is given.
	 * @de Es ist keine Redirect-URI angegeben.
	 */
	public static ResKey ERROR_NO_REDIRECT_URI;

	/**
	 * @en The redirect URI "{0}" is not an absolute URI with a host.
	 * @de Die Redirect-URI "{0}" ist keine absolute URI mit Host.
	 */
	public static ResKey1 ERROR_REDIRECT_URI_NOT_ABSOLUTE__URI;

	/**
	 * @en The redirect URI "{0}" has a fragment.
	 * @de Die Redirect-URI "{0}" hat ein Fragment.
	 */
	public static ResKey1 ERROR_REDIRECT_URI_WITH_FRAGMENT__URI;

	/**
	 * @en The redirect URI "{0}" is neither an https URI nor an http URI of the loopback interface.
	 * @de Die Redirect-URI "{0}" ist weder eine https-URI noch eine http-URI der Loopback-
	 *     Schnittstelle.
	 */
	public static ResKey1 ERROR_REDIRECT_URI_NOT_HTTPS__URI;

	/**
	 * @en The redirect URI "{0}" is not registered for the client "{1}".
	 * @de Die Redirect-URI "{0}" ist für den Client "{1}" nicht registriert.
	 */
	public static ResKey2 ERROR_REDIRECT_URI_NOT_REGISTERED__URI_CLIENT;

	/**
	 * @en The client ID metadata document "{0}" cannot be retrieved: {1}
	 * @de Das Client-ID-Metadatendokument "{0}" kann nicht abgerufen werden: {1}
	 */
	public static ResKey2 ERROR_CLIENT_METADATA_UNREACHABLE__URL_PROBLEM;

	/**
	 * @en The client ID metadata document "{0}" was answered with the status {1}.
	 * @de Die Abfrage des Client-ID-Metadatendokuments "{0}" wurde mit dem Status {1} beantwortet.
	 */
	public static ResKey2 ERROR_CLIENT_METADATA_STATUS__URL_STATUS;

	/**
	 * @en The client ID metadata document "{0}" is larger than {1} bytes.
	 * @de Das Client-ID-Metadatendokument "{0}" ist größer als {1} Bytes.
	 */
	public static ResKey2 ERROR_CLIENT_METADATA_TOO_LARGE__URL_MAX;

	/**
	 * @en The client ID metadata document "{0}" is not fetched, because its host is in a private
	 *     network.
	 * @de Das Client-ID-Metadatendokument "{0}" wird nicht abgerufen, weil sein Host in einem privaten
	 *     Netz liegt.
	 */
	public static ResKey1 ERROR_CLIENT_METADATA_PRIVATE_HOST__URL;

	/**
	 * @en The client ID metadata document "{0}" did not arrive within the time limit.
	 * @de Das Client-ID-Metadatendokument "{0}" ist nicht innerhalb des Zeitlimits angekommen.
	 */
	public static ResKey1 ERROR_CLIENT_METADATA_TIMEOUT__URL;

	/**
	 * @en No client ID is given.
	 * @de Es ist keine Client-ID angegeben.
	 */
	public static ResKey ERROR_NO_CLIENT_ID;

	/**
	 * @en The client "{0}" identifies itself by a client ID metadata document, which this server does
	 *     not accept.
	 * @de Der Client "{0}" weist sich mit einem Client-ID-Metadatendokument aus, das dieser Server
	 *     nicht annimmt.
	 */
	public static ResKey1 ERROR_CLIENT_METADATA_DOCUMENTS_DISABLED__CLIENT;

	/**
	 * @en There is no client "{0}".
	 * @de Es gibt keinen Client "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_CLIENT__CLIENT;

	/**
	 * @en The metadata of the client "{0}" is not acceptable: {1}
	 * @de Die Metadaten des Clients "{0}" sind nicht annehmbar: {1}
	 */
	public static ResKey2 ERROR_INVALID_CLIENT_METADATA__CLIENT_PROBLEM;

	/**
	 * @en The client ID metadata document "{0}" names the client ID "{1}" instead of its own URL.
	 * @de Das Client-ID-Metadatendokument "{0}" nennt die Client-ID "{1}" statt seiner eigenen URL.
	 */
	public static ResKey2 ERROR_CLIENT_ID_MISMATCH__URL_CLIENT;

	/**
	 * @en The client ID metadata document "{0}" contains a client secret.
	 * @de Das Client-ID-Metadatendokument "{0}" enthält ein Client-Secret.
	 */
	public static ResKey1 ERROR_CLIENT_METADATA_WITH_SECRET__CLIENT;

	/**
	 * @en The client ID "{0}" is no https URL with a path, or is not in normal form.
	 * @de Die Client-ID "{0}" ist keine https-URL mit Pfad oder nicht in Normalform.
	 */
	public static ResKey1 ERROR_INVALID_METADATA_DOCUMENT_URL__CLIENT;

	/**
	 * @en The token endpoint authentication method "{0}" is not supported; only public clients
	 *     ("none") are.
	 * @de Die Authentifizierungsmethode "{0}" am Token-Endpunkt wird nicht unterstützt, nur
	 *     öffentliche Clients ("none").
	 */
	public static ResKey1 ERROR_ONLY_PUBLIC_CLIENTS__METHOD;

	/**
	 * @en The grant types {0} are not supported; a client uses "authorization_code" and optionally
	 *     "refresh_token".
	 * @de Die Grant-Typen {0} werden nicht unterstützt; ein Client verwendet "authorization_code" und
	 *     optional "refresh_token".
	 */
	public static ResKey1 ERROR_UNSUPPORTED_GRANT_TYPES__TYPES;

	/**
	 * @en The response types {0} are not supported; only "code" is.
	 * @de Die Response-Typen {0} werden nicht unterstützt, nur "code".
	 */
	public static ResKey1 ERROR_UNSUPPORTED_RESPONSE_TYPES__TYPES;

	/**
	 * @en The response type "{0}" is not supported; only "code" is.
	 * @de Der Response-Typ "{0}" wird nicht unterstützt, nur "code".
	 */
	public static ResKey1 ERROR_UNSUPPORTED_RESPONSE_TYPE__TYPE;

	/**
	 * @en The authorization request has no PKCE code challenge.
	 * @de Die Autorisierungsanfrage hat keine PKCE-Code-Challenge.
	 */
	public static ResKey ERROR_NO_CODE_CHALLENGE;

	/**
	 * @en The code challenge method "{0}" is not supported; only "S256" is.
	 * @de Die Code-Challenge-Methode "{0}" wird nicht unterstützt, nur "S256".
	 */
	public static ResKey1 ERROR_UNSUPPORTED_CODE_CHALLENGE_METHOD__METHOD;

	/**
	 * @en The authorization request must name exactly one resource.
	 * @de Die Autorisierungsanfrage muss genau eine Ressource nennen.
	 */
	public static ResKey ERROR_NOT_ONE_RESOURCE;

	/**
	 * @en The resource "{0}" is not part of this application.
	 * @de Die Ressource "{0}" gehört nicht zu dieser Anwendung.
	 */
	public static ResKey1 ERROR_UNKNOWN_RESOURCE__RESOURCE;

	/**
	 * @en The grant type "{0}" is not supported.
	 * @de Der Grant-Typ "{0}" wird nicht unterstützt.
	 */
	public static ResKey1 ERROR_UNSUPPORTED_GRANT_TYPE__TYPE;

	/**
	 * @en Client authentication is not supported; only public clients are.
	 * @de Client-Authentifizierung wird nicht unterstützt, nur öffentliche Clients.
	 */
	public static ResKey ERROR_CLIENT_AUTHENTICATION_NOT_SUPPORTED;

	/**
	 * @en The authorization code is unknown.
	 * @de Der Autorisierungscode ist unbekannt.
	 */
	public static ResKey ERROR_UNKNOWN_CODE;

	/**
	 * @en The authorization code was already used; the authorization created with it is revoked.
	 * @de Der Autorisierungscode wurde bereits verwendet; die damit erteilte Autorisierung ist
	 *     widerrufen.
	 */
	public static ResKey ERROR_CODE_REUSED;

	/**
	 * @en The authorization code has expired.
	 * @de Der Autorisierungscode ist abgelaufen.
	 */
	public static ResKey ERROR_CODE_EXPIRED;

	/**
	 * @en The authorization code was issued to another client.
	 * @de Der Autorisierungscode wurde einem anderen Client ausgestellt.
	 */
	public static ResKey ERROR_CODE_OF_OTHER_CLIENT;

	/**
	 * @en The redirect URI differs from the one of the authorization request.
	 * @de Die Redirect-URI weicht von der der Autorisierungsanfrage ab.
	 */
	public static ResKey ERROR_REDIRECT_URI_MISMATCH;

	/**
	 * @en The token request has no PKCE code verifier.
	 * @de Die Token-Anfrage hat keinen PKCE-Code-Verifier.
	 */
	public static ResKey ERROR_NO_CODE_VERIFIER;

	/**
	 * @en The PKCE code verifier does not match the code challenge.
	 * @de Der PKCE-Code-Verifier passt nicht zur Code-Challenge.
	 */
	public static ResKey ERROR_CODE_VERIFIER_MISMATCH;

	/**
	 * @en The resources {0} differ from the resource the authorization was granted for.
	 * @de Die Ressourcen {0} weichen von der Ressource ab, für die die Autorisierung erteilt wurde.
	 */
	public static ResKey1 ERROR_RESOURCE_MISMATCH__RESOURCES;

	/**
	 * @en The refresh token is unknown or revoked.
	 * @de Das Refresh-Token ist unbekannt oder widerrufen.
	 */
	public static ResKey ERROR_UNKNOWN_REFRESH_TOKEN;

	/**
	 * @en The refresh token was already exchanged; the authorization is revoked.
	 * @de Das Refresh-Token wurde bereits eingetauscht; die Autorisierung ist widerrufen.
	 */
	public static ResKey ERROR_REFRESH_TOKEN_REUSED;

	/**
	 * @en The refresh token has expired.
	 * @de Das Refresh-Token ist abgelaufen.
	 */
	public static ResKey ERROR_REFRESH_TOKEN_EXPIRED;

	/**
	 * @en The refresh token was issued to another client.
	 * @de Das Refresh-Token wurde einem anderen Client ausgestellt.
	 */
	public static ResKey ERROR_REFRESH_TOKEN_OF_OTHER_CLIENT;

	/**
	 * @en The scope "{0}" exceeds the granted scope.
	 * @de Der Scope "{0}" geht über den gewährten Scope hinaus.
	 */
	public static ResKey1 ERROR_SCOPE_EXCEEDS_GRANT__SCOPE;

	/**
	 * @en The request carries no bearer token.
	 * @de Die Anfrage enthält kein Bearer-Token.
	 */
	public static ResKey ERROR_NO_BEARER_TOKEN;

	/**
	 * @en The OAuth authorization server of the application is not active.
	 * @de Der OAuth-Autorisierungsserver der Anwendung ist nicht aktiv.
	 */
	public static ResKey ERROR_AUTHORIZATION_SERVER_INACTIVE;

	/**
	 * @en The bearer token is no access token of this application.
	 * @de Das Bearer-Token ist kein Access-Token dieser Anwendung.
	 */
	public static ResKey ERROR_NOT_AN_ACCESS_TOKEN;

	/**
	 * @en The access token is unknown or revoked.
	 * @de Das Access-Token ist unbekannt oder widerrufen.
	 */
	public static ResKey ERROR_UNKNOWN_ACCESS_TOKEN;

	/**
	 * @en The access token has expired.
	 * @de Das Access-Token ist abgelaufen.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_EXPIRED;

	/**
	 * @en The access token was not issued for the resource "{0}".
	 * @de Das Access-Token wurde nicht für die Ressource "{0}" ausgestellt.
	 */
	public static ResKey1 ERROR_ACCESS_TOKEN_FOR_OTHER_RESOURCE__RESOURCE;

	/**
	 * @en Register OAuth client
	 * @de OAuth-Client registrieren
	 */
	public static ResKey TX_REGISTER_CLIENT;

	/**
	 * @en Issue OAuth authorization code
	 * @de OAuth-Autorisierungscode ausstellen
	 */
	public static ResKey TX_ISSUE_CODE;

	/**
	 * @en Exchange OAuth authorization code
	 * @de OAuth-Autorisierungscode eintauschen
	 */
	public static ResKey TX_EXCHANGE_CODE;

	/**
	 * @en Exchange OAuth refresh token
	 * @de OAuth-Refresh-Token eintauschen
	 */
	public static ResKey TX_REFRESH;

	/**
	 * @en Record use of OAuth authorization
	 * @de Verwendung einer OAuth-Autorisierung vermerken
	 */
	public static ResKey TX_RECORD_USE;

	/**
	 * @en Revoke OAuth authorization
	 * @de OAuth-Autorisierung widerrufen
	 */
	public static ResKey TX_REVOKE;

	/**
	 * @en OAuth authorization server
	 * @de OAuth-Autorisierungsserver
	 */
	public static ResKey LLMS_TITLE;

	/**
	 * @en The application is an OAuth 2.1 authorization server for its own endpoints (MCP, A2A, REST).
	 *     A client signs in its user with the authorization code flow with PKCE (S256) and names the
	 *     endpoint it wants to call as resource (RFC 8707); the access token it receives is accepted
	 *     only there.
	 * @de Die Anwendung ist ein OAuth-2.1-Autorisierungsserver für ihre eigenen Endpunkte (MCP, A2A,
	 *     REST). Ein Client meldet seinen Benutzer mit dem Authorization-Code-Flow mit PKCE (S256) an
	 *     und nennt den Endpunkt, den er aufrufen will, als Ressource (RFC 8707); das Access-Token,
	 *     das er erhält, wird nur dort angenommen.
	 */
	public static ResKey LLMS_INTRO;

	/**
	 * @en Authorization server metadata: {0}
	 * @de Metadaten des Autorisierungsservers: {0}
	 */
	public static ResKey1 LLMS_METADATA__URL;

	/**
	 * @en Issuer: {0}
	 * @de Aussteller: {0}
	 */
	public static ResKey1 LLMS_ISSUER__URL;

	/**
	 * @en Clients may use the https URL of their client ID metadata document as client ID.
	 * @de Clients können die https-URL ihres Client-ID-Metadatendokuments als Client-ID verwenden.
	 */
	public static ResKey LLMS_CLIENT_ID_METADATA_DOCUMENTS;

	/**
	 * @en Dynamic client registration: {0}
	 * @de Dynamische Client-Registrierung: {0}
	 */
	public static ResKey1 LLMS_REGISTRATION__URL;

	/**
	 * @en No further client can be registered at the moment.
	 * @de Zurzeit kann kein weiterer Client registriert werden.
	 */
	public static ResKey ERROR_TOO_MANY_CLIENTS;

	/**
	 * @en The authorization code or refresh token was exchanged by another request at the same time.
	 * @de Der Autorisierungscode oder das Refresh-Token wurde gleichzeitig von einer anderen Anfrage
	 *     eingetauscht.
	 */
	public static ResKey ERROR_CONCURRENT_EXCHANGE;

	/**
	 * @en Clean up OAuth authorizations
	 * @de OAuth-Autorisierungen aufräumen
	 */
	public static ResKey TX_CLEAN_UP;

	/**
	 * @en Removed {0} pending authorizations, {1} authorization codes, {2} tokens, {3} authorizations
	 *     and {4} clients.
	 * @de {0} ausstehende Autorisierungen, {1} Autorisierungscodes, {2} Tokens, {3} Autorisierungen
	 *     und {4} Clients entfernt.
	 */
	public static ResKey5 CLEAN_UP_RESULT__PENDING_CODES_TOKENS_GRANTS_CLIENTS;

	/**
	 * @en Store OAuth authorization request
	 * @de OAuth-Autorisierungsanfrage speichern
	 */
	public static ResKey TX_PENDING_AUTHORIZATION;

	/**
	 * @en Deny OAuth authorization request
	 * @de OAuth-Autorisierungsanfrage ablehnen
	 */
	public static ResKey TX_DENY;

	/**
	 * @en There is no such authorization request, or it has been answered already. Start the sign-in
	 *     from the application again.
	 * @de Diese Autorisierungsanfrage gibt es nicht oder sie wurde bereits beantwortet. Starten Sie die
	 *     Anmeldung erneut aus der Anwendung.
	 */
	public static ResKey ERROR_UNKNOWN_PENDING_AUTHORIZATION;

	/**
	 * @en The authorization request has expired. Start the sign-in from the application again.
	 * @de Die Autorisierungsanfrage ist abgelaufen. Starten Sie die Anmeldung erneut aus der
	 *     Anwendung.
	 */
	public static ResKey ERROR_PENDING_AUTHORIZATION_EXPIRED;

	/**
	 * @en Only a logged-in account can authorize a client.
	 * @de Nur ein angemeldetes Konto kann einen Client autorisieren.
	 */
	public static ResKey ERROR_CONSENT_WITHOUT_LOGIN;

	/**
	 * @en The authorization request was refused
	 * @de Die Autorisierungsanfrage wurde abgelehnt
	 */
	public static ResKey AUTHORIZATION_REFUSED_TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
