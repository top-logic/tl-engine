/*
 * Pull-request build of the TopLogic engine.
 *
 * The pipeline builds and tests only the reactor modules a pull request affects
 * (see ci/affected-modules.sh for the selection and the Maven invocations per
 * mode), builds them with a parallel reactor without running tests, and then
 * runs the module tests, the scripted tests in parallel shards, and SpotBugs
 * concurrently. Since the build does not run tests, the tests of a module do not
 * delay the build of its dependents.
 *
 * Stages:
 *   Checkout        Fresh workspace, checkout of the branch together with the
 *                   target branch master (for the merge base).
 *   Select          ci/affected-modules.sh decides MODE (full, partial, none),
 *                   the changed modules, the affected modules, and the modules
 *                   with scripted tests.
 *   Build           partial: (1) compile-only install of the changed modules and
 *                   their dependencies, (2) clean install without running tests
 *                   of the changed modules and their dependents.
 *                   full: step (2) for the whole reactor. none: nothing.
 *   Test            Concurrent branches: the module tests of the affected
 *                   modules whose packaging runs tests (scripted tests excluded),
 *                   SpotBugs over the modules of build step (2), and the scripted
 *                   tests of the affected modules in SHARDS Maven runs, each with
 *                   its own scratch directory and test ports.
 *   Check sources   Fails the build if the build or the tests modified versioned
 *                   sources.
 *   (post)          Test results, SpotBugs issues (without Git blame), and the log rules
 *                   ci/build-log.rules (build warnings make the build
 *                   UNSTABLE, test JVM crashes make it FAILED).
 *
 * Site configuration:
 *   Everything specific to the build server is kept in the Jenkins credential
 *   CI_ENV_CREDENTIALS (kind "Secret file"). The file is a shell environment
 *   file of KEY=value lines, sourced by bash before each Maven invocation with
 *   all variables exported (a value containing spaces is quoted as in the
 *   shell, KEY="a b"):
 *     - Variables of the test configuration, which TopLogic resolves through
 *       its ${env:<name>} aliases from system properties or environment
 *       variables, e.g. the database connections (<db>_host, <db>_user,
 *       <db>_passwd, <db>_schema, ...), the mail servers (imap_*, smtp_*), and
 *       mail_domain. The test JVMs forked by Surefire inherit them.
 *     - MAVEN_ARGS: options Maven (>= 3.9) appends to every invocation, e.g.
 *       the Maven settings (-s <file>) and Maven properties of the build
 *       (-D<name>=<value>). The pipeline appends the local repository in the
 *       workspace to it.
 *
 * Job configuration:
 *   "Pipeline script from SCM" with the repository and credentials of the
 *   Git server, branch ${BRANCH}, and a non-lightweight checkout (parameters are
 *   expanded in the SCM definition only then). The checkout stage reuses this
 *   SCM definition.
 */

import groovy.transform.Field

/** Id of the "Secret file" credential with the site configuration, see above. */
@Field final String CI_ENV_CREDENTIALS = 'tl-engine-ci-env'

/** Variable holding the path of the site configuration file during a Maven invocation. */
@Field final String CI_ENV_VARIABLE = 'TL_CI_ENV'

/** Refspec fetching the target branch in addition to the refspecs of the job's SCM definition. */
@Field final String TARGET_REFSPEC = '+refs/heads/master:refs/remotes/origin/master'

/** JVM options of the test JVMs forked by Surefire. */
@Field final String TEST_ARG_LINE = '-Xmx4096m -XX:-OmitStackTraceInFastThrow'

/** Rules of the log parser, relative to the workspace. */
@Field final String LOG_RULES = 'ci/build-log.rules'

/** Output of ci/affected-modules.sh. */
@Field final String AFFECTED_FILE = 'affected.env'

/** Keys of the output of ci/affected-modules.sh. */
@Field final String KEY_MODE = 'MODE'
@Field final String KEY_CHANGED = 'CHANGED'
@Field final String KEY_AFFECTED = 'AFFECTED'
@Field final String KEY_TEST = 'TEST_MODULES'
@Field final String KEY_SCRIPTED = 'SCRIPTED_MODULES'

/** Build modes of ci/affected-modules.sh. */
@Field final String MODE_FULL = 'full'
@Field final String MODE_PARTIAL = 'partial'
@Field final String MODE_NONE = 'none'

/** Test selection property, see test.com.top_logic.basic.util.ShardSelection. */
@Field final String PROP_SCRIPTED = 'TestAll.scripted'
@Field final String SCRIPTED_NONE = 'none'

/** Modules sharing the shards, see test.com.top_logic.basic.util.ShardSelection. */
@Field final String PROP_SHARD_MODULES = 'TestAll.shardModules'

/** Scratch directory property, see test.com.top_logic.basic.ScratchDirectory. */
@Field final String PROP_SCRATCH_DIR = 'TestAll.scratchDir'

/** Base ports of the Kafka tests; a build uses base + BUILD_NUMBER % 100 + 100 * shard. */
@Field final int KAFKA_PORT_BASE = 47000
@Field final int ZOO_KEEPER_PORT_BASE = 48000

/**
 * Threads of the SpotBugs run. SpotBugs is not on the critical path of the Test stage, so it runs
 * with few threads and leaves the CPUs to the module tests and the scripted-test shards.
 */
@Field final int SPOTBUGS_THREADS = 1

/** Port distance between the shards of one build. */
@Field final int SHARD_PORT_OFFSET = 100

/** Selection result of ci/affected-modules.sh, by key. */
@Field Map selection = [:]

pipeline {
	agent any

	options {
		skipDefaultCheckout()
		timestamps()
	}

	tools {
		maven 'Maven 3.9'
		jdk 'Java 21'
	}

	parameters {
		string(name: 'REPO', defaultValue: '',
			description: 'Accepted for the build trigger; the repository is the one of the SCM definition of the job.')
		string(name: 'BRANCH', defaultValue: '',
			description: 'Branch to build, e.g. CWS/CWS_12345_topic or refs/heads/CWS/CWS_12345_topic.')
		string(name: 'ADDITIONAL_BUILD_DESCRIPTION', defaultValue: '',
			description: 'Appended to the build description, e.g. "(PR: 1234)".')
		booleanParam(name: 'SKIP_TESTS', defaultValue: false,
			description: 'Build without running tests.')
		booleanParam(name: 'SKIP_SPOTBUGS', defaultValue: false,
			description: 'Build without SpotBugs analysis.')
		booleanParam(name: 'ONLY_DEFAULT_DB', defaultValue: true,
			description: 'Run database tests only against the default database.')
		string(name: 'DEFAULT_DB', defaultValue: 'h2',
			description: 'Default database of the tests.')
		booleanParam(name: 'tl_test_defaultKbUnversioned', defaultValue: false,
			description: 'Whether the default knowledge base of the tests is unversioned.')
		string(name: 'SHARDS', defaultValue: '4',
			description: 'Number of concurrent Maven runs executing the scripted tests.')
		string(name: 'MAVEN_THREADS', defaultValue: '4',
			description: 'Number of threads of the parallel reactor build (mvn -T).')
		booleanParam(name: 'FORCE_FULL', defaultValue: false,
			description: 'Build and test the whole reactor, independent of the changed files.')
		text(name: 'additionalOptions', defaultValue: '',
			description: 'Additional Maven options, appended to every Maven invocation.')
	}

	environment {
		MAVEN_OPTS = '-Xmx2048m'
	}

	stages {
		stage('Checkout') {
			steps {
				cleanWs()
				checkoutWithTarget()
			}
		}

		stage('Select') {
			steps {
				script {
					currentBuild.description = "PR build - ${params.BRANCH} ${params.ADDITIONAL_BUILD_DESCRIPTION}"
					selectModules()
					currentBuild.description += " [${describeSelection()}]"
				}
			}
		}

		stage('Build') {
			when { expression { selection[KEY_MODE] != MODE_NONE } }
			steps {
				script {
					if (selection[KEY_MODE] == MODE_PARTIAL) {
						maven("-T ${identifier(params.MAVEN_THREADS)} install -pl ${selection[KEY_CHANGED]} -am" +
							' -DskipTests=true -Dmaven.javadoc.skip=true -Dspotbugs.skip=true' +
							' -Dtl.javadoc.aggregate=false', 0)
					}
					maven("-T ${identifier(params.MAVEN_THREADS)} clean install ${builtModules()}" +
						' -DskipTests=true -Dtl.javadoc.aggregate=false', 0)
				}
			}
		}

		stage('Test') {
			when { expression { selection[KEY_MODE] != MODE_NONE } }
			steps {
				script {
					Map branches = [failFast: false]
					if (!params.SKIP_TESTS && count(selection[KEY_TEST]) > 0) {
						branches['module-tests'] = {
							maven("-T ${identifier(params.MAVEN_THREADS)} surefire:test -pl ${selection[KEY_TEST]}" +
								' -DskipTests=false -Dmaven.test.failure.ignore=true' +
								" -D${PROP_SCRIPTED}=${SCRIPTED_NONE}", 0)
						}
					}
					if (!params.SKIP_SPOTBUGS) {
						branches['spotbugs'] = {
							maven("-T ${SPOTBUGS_THREADS} spotbugs:spotbugs ${builtModules()}", 0)
						}
					}
					int shards = params.SKIP_TESTS || count(selection[KEY_SCRIPTED]) == 0 ? 0 : Integer.parseInt(params.SHARDS)
					for (int n = 1; n <= shards; n++) {
						int shard = n
						branches["shard-${shard}".toString()] = {
							maven("surefire:test -pl ${selection[KEY_SCRIPTED]}" +
								' -DskipTests=false -Dmaven.test.failure.ignore=true' +
								" -D${PROP_SCRIPTED}=${shard}/${shards}" +
								" -D${PROP_SHARD_MODULES}=${selection[KEY_SCRIPTED]}" +
								" -D${PROP_SCRATCH_DIR}=tmp/shard-${shard}" +
								" -Dsurefire.reportNameSuffix=shard-${shard}", shard)
						}
					}
					if (branches.size() > 1) {
						parallel branches
					}
				}
			}
		}

		stage('Check sources') {
			when { expression { selection[KEY_MODE] != MODE_NONE } }
			steps {
				catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
					sh '''
						changed="$(git status --short --untracked-files=no | grep -E 'src/main/java' || true)"
						if [ -n "$changed" ]; then
							echo "[ERROR] Files have been changed during build:"
							echo "$changed"
							exit 1
						fi
					'''
				}
			}
		}
	}

	post {
		always {
			junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
			recordIssues tools: [spotBugs()], enabledForFailure: true, skipBlames: true
			logParser parsingRulesPath: '', projectRulePath: LOG_RULES, useProjectRule: true,
				unstableOnWarning: true, failBuildOnError: true
		}
	}
}

/**
 * Checks out the job's SCM definition, additionally fetching the target branch.
 */
void checkoutWithTarget() {
	List remotes = []
	for (def remote : scm.userRemoteConfigs) {
		String name = remote.name ?: 'origin'
		String refspec = remote.refspec ?: "+refs/heads/*:refs/remotes/${name}/*"
		remotes << [name: name, url: remote.url, credentialsId: remote.credentialsId,
			refspec: "${refspec} ${TARGET_REFSPEC}".toString()]
	}
	checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions, userRemoteConfigs: remotes])
}

/**
 * Runs ci/affected-modules.sh and stores its result in the field selection.
 */
void selectModules() {
	withSiteConfig("ci/affected-modules.sh ${params.FORCE_FULL ? '--full ' : ''}--output ${AFFECTED_FILE}")

	Map result = [:]
	for (String line : readFile(AFFECTED_FILE).split('\n')) {
		int separator = line.indexOf('=')
		if (separator > 0) {
			result[line.substring(0, separator)] = line.substring(separator + 1).trim()
		}
	}
	if (!(result[KEY_MODE] in [MODE_FULL, MODE_PARTIAL, MODE_NONE])) {
		error("Invalid module selection in ${AFFECTED_FILE}: ${result}")
	}
	selection = result
}

/**
 * The module options of build step (2), which the SpotBugs branch of the Test stage reuses: the
 * changed modules and their dependents for a partial build, the whole reactor otherwise.
 */
String builtModules() {
	return selection[KEY_MODE] == MODE_PARTIAL ? "-pl ${selection[KEY_CHANGED]} -amd" : ''
}

/**
 * Short description of the module selection for the build description.
 */
String describeSelection() {
	String mode = selection[KEY_MODE]
	if (mode == MODE_NONE) {
		return mode
	}
	return "${mode}: ${count(selection[KEY_AFFECTED])} modules, ${count(selection[KEY_SCRIPTED])} with scripted tests"
}

/**
 * Number of entries in a comma-separated module list.
 */
int count(String modules) {
	return modules ? modules.split(',').length : 0
}

/**
 * Runs a shell command in the environment of the site configuration.
 *
 * The site configuration file is sourced with command tracing switched off, so that its values
 * are not written to the build log. The local Maven repository in the workspace is appended to
 * the MAVEN_ARGS of the site configuration.
 */
void withSiteConfig(String command) {
	withCredentials([file(credentialsId: CI_ENV_CREDENTIALS, variable: CI_ENV_VARIABLE)]) {
		sh """
			set +x
			set -a
			. "\$${CI_ENV_VARIABLE}"
			set +a
			export MAVEN_ARGS="\${MAVEN_ARGS:-} -Dmaven.repo.local=\$WORKSPACE/.repository"
			set -x
			${command}
		"""
	}
}

/**
 * Runs Maven with the options common to all invocations of the build.
 *
 * Boolean parameters and the validated DEFAULT_DB are inserted from params, which always holds
 * their values (also in a build triggered before the job knew the parameters of this file). The
 * free-text parameter additionalOptions is referenced as shell variable, so that its value is not
 * interpreted as part of the script; it is deliberately unquoted: its value is a
 * whitespace-separated list of options.
 *
 * @param args
 *        Goals and invocation-specific options.
 * @param shard
 *        Number of the scripted-test shard the invocation runs, 0 for the main build. Selects the
 *        ports of the Kafka tests, so that concurrent invocations never share a port.
 */
void maven(String args, int shard) {
	int portOffset = Integer.parseInt(env.BUILD_NUMBER) % 100 + SHARD_PORT_OFFSET * shard
	withSiteConfig('mvn -B -e' +
		" \"-DargLine=${TEST_ARG_LINE}\"" +
		' -Dtl.developerMode=true -Dtl_developerMode=true' +
		" -Dtl_test_onlyDefaultDB=${params.ONLY_DEFAULT_DB}" +
		" -Dtl_test_defaultKbUnversioned=${params.tl_test_defaultKbUnversioned}" +
		" -Dtl_test_defaultDB=${identifier(params.DEFAULT_DB)}" +
		" -Dkafka_port=${KAFKA_PORT_BASE + portOffset}" +
		" -Dzoo_keeper_port=${ZOO_KEEPER_PORT_BASE + portOffset}" +
		' $additionalOptions ' +
		args)
}

/**
 * The given parameter value, if it is a plain identifier; fails the build otherwise.
 */
String identifier(String value) {
	if (!(value ==~ /[A-Za-z0-9_]+/)) {
		error("Invalid parameter value: '${value}'")
	}
	return value
}
