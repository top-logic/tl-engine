/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The project has no git connection, or its git connection is not available.
	 */
	public static ResKey ERROR_NO_GIT_HOSTING;

	/**
	 * @en The built-in git connection "{0}" names the type "{1}", which is not registered.
	 */
	public static ResKey2 ERROR_UNKNOWN_PROVIDER_TYPE__CONNECTION_TYPE;

	/**
	 * @en The built-in git connection "{0}" has neither credentials nor a token secret.
	 */
	public static ResKey1 ERROR_NO_CREDENTIALS__CONNECTION;

	/**
	 * @en "{0}" is no known type of git hosting service.
	 */
	public static ResKey1 ERROR_UNKNOWN_PROVIDER_TYPE__TYPE;

	/**
	 * @en Choose the git connection the repository of the project lives on.
	 */
	public static ResKey ERROR_NO_GIT_CONNECTION;

	/**
	 * @en The git connection "{0}" is not available for the tenant of the project.
	 */
	public static ResKey1 ERROR_GIT_CONNECTION_NOT_AVAILABLE__CONNECTION;

	/**
	 * @en The git connection "{0}" is not available: its type is not registered, or the built-in
	 *     connection is no longer configured.
	 */
	public static ResKey1 ERROR_CONNECTION_UNAVAILABLE__CONNECTION;

	/**
	 * @en "{0}" is a built-in git connection of the installation; only its configuration changes
	 *     it.
	 */
	public static ResKey1 ERROR_BUILT_IN_CONNECTION__CONNECTION;

	/**
	 * @en The git connection "{0}" cannot be deleted while projects live on it: {1}
	 */
	public static ResKey2 ERROR_CONNECTION_IN_USE__CONNECTION_PROJECTS;

	/**
	 * @en Type, URL and owner of the git connection "{0}" cannot be changed while projects live on
	 *     it: {1}
	 */
	public static ResKey2 ERROR_CONNECTION_SERVER_IN_USE__CONNECTION_PROJECTS;

	/**
	 * @en The git connection needs a name.
	 */
	public static ResKey ERROR_MISSING_CONNECTION_NAME;

	/**
	 * @en The git connection needs the URL of its server.
	 */
	public static ResKey ERROR_MISSING_CONNECTION_URL;

	/**
	 * @en The git connection needs the organization or user its repositories live in.
	 */
	public static ResKey ERROR_MISSING_CONNECTION_OWNER;

	/**
	 * @en The git connection needs an access token.
	 */
	public static ResKey ERROR_MISSING_TOKEN;

	/**
	 * @en The installation has no writable secret store, so the access token cannot be kept.
	 */
	public static ResKey ERROR_NO_WRITABLE_SECRET_STORE;

	/**
	 * @en The access token cannot be stored in the secret store.
	 */
	public static ResKey ERROR_TOKEN_NOT_STORED;

	/**
	 * @en The git hosting service {0} cannot tell the rights of the access token: {1}
	 */
	public static ResKey2 ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM;

	/**
	 * @en Created the git connection "{0}" of tenant "{1}".
	 */
	public static ResKey2 CREATED_CONNECTION__CONNECTION_TENANT;

	/**
	 * @en Changed the git connection "{0}".
	 */
	public static ResKey1 CHANGED_CONNECTION__CONNECTION;

	/**
	 * @en Replaced the access token of the git connection "{0}".
	 */
	public static ResKey1 REPLACED_TOKEN__CONNECTION;

	/**
	 * @en Deleted the git connection "{0}".
	 */
	public static ResKey1 DELETED_CONNECTION__CONNECTION;

	/**
	 * @en Synchronized the built-in git connections with the configuration.
	 */
	public static ResKey SYNCHRONIZED_BUILT_IN_CONNECTIONS;

	/**
	 * @en The repository "{0}" already exists.
	 */
	public static ResKey1 ERROR_REPOSITORY_EXISTS__REPOSITORY;

	/**
	 * @en The repository "{0}" does not exist or is not accessible.
	 */
	public static ResKey1 ERROR_REPOSITORY_NOT_FOUND__REPOSITORY;

	/**
	 * @en The git hosting service does not accept "{0}" as name of a repository.
	 */
	public static ResKey1 ERROR_INVALID_REPOSITORY_NAME__NAME;

	/**
	 * @en The organization or user "{0}" does not exist on the git hosting service.
	 */
	public static ResKey1 ERROR_OWNER_NOT_FOUND__OWNER;

	/**
	 * @en The credentials of the git connection do not permit this operation on "{0}".
	 */
	public static ResKey1 ERROR_PERMISSION_DENIED__REPOSITORY;

	/**
	 * @en The git hosting service {0} rejects the credentials of the git connection.
	 */
	public static ResKey1 ERROR_AUTHENTICATION_FAILED__SERVICE;

	/**
	 * @en The git hosting service {0} cannot be reached: {1}
	 */
	public static ResKey2 ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM;

	/**
	 * @en The git hosting service {0} answered the request {1} with status {2}: {3}
	 */
	public static ResKey4 ERROR_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE;

	/**
	 * @en "{0}" is not the URL of a git hosting service.
	 */
	public static ResKey1 ERROR_INVALID_BASE_URL__URL;

	/**
	 * @en The git hosting service has no repository "{0}", or the credentials of the git connection
	 *     cannot read it.
	 */
	public static ResKey1 ERROR_UNKNOWN_REPOSITORY__REFERENCE;

	/**
	 * @en The workspace has no access to the git hosting service: the workspace proxy is switched
	 *     off or has no git route.
	 */
	public static ResKey ERROR_NO_GIT_ROUTE;

	/**
	 * @en The repository "{0}" has no "pom.xml" in its root directory, it holds no TopLogic
	 *     application.
	 */
	public static ResKey1 ERROR_NO_POM__REPOSITORY;

	/**
	 * @en The "pom.xml" of the repository "{0}" cannot be read: {1}
	 */
	public static ResKey2 ERROR_INVALID_POM__REPOSITORY_PROBLEM;

	/**
	 * @en The application in the repository "{0}" has the artifact ID "{1}", the project expects
	 *     "{2}".
	 */
	public static ResKey3 ERROR_ARTIFACT_MISMATCH__REPOSITORY_FOUND_EXPECTED;

	/**
	 * @en The application in the repository "{0}" is built on TopLogic {1}, the project on {2}.
	 */
	public static ResKey3 ERROR_ENGINE_VERSION_MISMATCH__REPOSITORY_FOUND_EXPECTED;

	static {
		initConstants(I18NConstants.class);
	}
}
