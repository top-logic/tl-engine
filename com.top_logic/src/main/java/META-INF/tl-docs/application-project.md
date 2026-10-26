---
description: Read before creating or restructuring the Maven project of a TopLogic application or fragment module - generating it with the archetype tl-archetype-app (properties ui, moduleType, useAI, useBPE, ... and their defaults), the POM without parent (property tl.version, import of the BOM tl-bom, packaging tl-app / tl-fragment of tl-maven-plugin with extensions), a company parent POM, the feature starters tl-starter-react / -classic / -ai / -ai-search and the single feature modules, an application depending on a fragment, the tl.deb.* properties of the Debian package, switching an existing project from the parent tl-parent-app to the packaging, and inheriting tl-parent-app instead.
order: 10
---

# Setting up an application or fragment project

A TopLogic project is a Maven project of one of two kinds:

- an **application**: a runnable web application (packaging `tl-app`), and
- a **fragment**: a library module contributing model, configuration and UI to the applications
  depending on it (packaging `tl-fragment`).

Neither needs a TopLogic parent POM. The project imports the versions from the BOM `tl-bom`, and
the build plugin `tl-maven-plugin` provides the packaging, which builds the project with the
configuration of the TopLogic parent POM `tl-parent-app` (application) or `tl-parent-core`
(fragment). Inheriting `tl-parent-app` directly works as well, see
[Inheriting tl-parent-app](#inheriting-tl-parent-app).

## Generating a project with the archetype

The archetype `tl-archetype-app` generates a ready-to-build project. Use a 3.x version of the
`maven-archetype-plugin`: it runs the post-generate script of the archetype, which checks the
property combination and removes the files not belonging to the chosen kind of project and user
interface.

```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate \
    -DarchetypeGroupId=com.top-logic \
    -DarchetypeArtifactId=tl-archetype-app \
    -DarchetypeVersion=8.0.0 \
    -DgroupId=com.example \
    -DartifactId=my-app \
    -Dversion=1.0-SNAPSHOT \
    -Dui=react \
    -DuseAI=true \
    -DinteractiveMode=false
```

Without `-DinteractiveMode=false`, Maven asks for every property, proposing its default.

| Property | Default | Meaning |
|---|---|---|
| `moduleType` | `app` | `app` (packaging `tl-app`) or `fragment` (packaging `tl-fragment`). |
| `ui` | `react` | `react`: the views of `tl-layout-view`, entry point `/view/`. `classic`: the layouts of the `LayoutServlet`. `both`: the React entry point, with the classic UI reachable at `/servlet/LayoutServlet`. |
| `useChartJS` | `true` | Charts: `tl-layout-react-chartjs` (React), `tl-chart-chartjs` (classic). |
| `useSelfService` | `true` | Account self-service: `tl-layout-view-selfservice` (React), `tl-security-selfservice` (classic). |
| `useBPE` | `false` for `ui=react`, else `true` | The process engine `tl-bpe-app`. It has a classic UI only. |
| `useApiClient` | `true` | Calling REST services through OpenAPI definitions: `tl-service-openapi-client`. |
| `useApiServer` | `true` | Providing REST services through OpenAPI definitions: `tl-service-openapi-server`. |
| `useAI` | `false` | The AI agents: `tl-starter-ai`, with `ui=classic` or `ui=both` also the chat tab `tl-chat-classic`. |
| `useAISearch` | `false` | The hybrid (full-text and semantic) search: `tl-starter-ai-search`. Requires `useAI=true`. |
| `useSSO` | `false` | Single sign-on through pac4j (OpenID Connect, SAML, ...): `tl-security-auth-pac4j`. |
| `ProfessionalEdition` | `true` | `true`: license `BOS-TopLogic-1.0` and the dependency `tl-license-professional`. `false`: license AGPL-3.0, the dependency commented out. Applies to applications only. |
| `tlVersion` | the archetype version | The TopLogic version, written to the property `tl.version`. |
| `package` | `groupId` (`-` as `_`) + `.` + `artifactId` (`-` as `.`) | The Java package of the module. |
| `module` | `artifactId` (`-` as `.`) | The name of the model module (`WEB-INF/model/<module>.model.xml`). |
| `projectName` | `artifactId` without `-` | Prefix of the configuration and resource files (`WEB-INF/conf/<projectName>.conf.xml`). |
| `DisplayName` | the words of `artifactId`, capitalized | Name of the application and of the launch configurations. |
| `CopyrightHolder` | `My Company` | Holder in the copyright headers. |

The archetype rejects these combinations:

- a `ui` other than `react`, `classic` or `both`, and a `moduleType` other than `app` or
  `fragment`;
- `ui=react` with `useBPE=true`: the process engine has no React UI, use `ui=both`;
- `useAISearch=true` without `useAI=true`.

The generated application contains the model (`WEB-INF/model`), the knowledge base declarations
(`WEB-INF/kbase`), the configuration (`WEB-INF/conf`), the shell `WEB-INF/views/app.view.xml`
(React) and/or the folder `WEB-INF/layouts` (classic), the deployment configurations
`deploy/local` and `deploy/prod`, the Debian package sources `src/main/deb`, a Docker build in
`src/main/docker`, a test `TestApp`, Eclipse project files and launch configurations (*Start*,
*Setup*, *Test*, *Generate*, ...), and the developer overlay `.mvn/jvm.config`,
`local.conf.xml.template` for local secrets.

A fragment has no deployment, no start configuration and no shell of its own: `web.xml`,
`app.view.xml`, `deploy`, `src/main/deb`, `src/main/docker`, `local.conf.xml.template`,
`.mvn/jvm.config` and the *Start* / *Setup* launch configurations are left out; instead it has a
`META-INF/web-fragment.xml`, and its `metaConf.txt` lists only its own configuration.

## The project POM

The POM of a generated application, reduced to the TopLogic parts:

```xml
<project>
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example</groupId>
	<artifactId>my-app</artifactId>
	<version>1.0-SNAPSHOT</version>

	<packaging>tl-app</packaging>

	<properties>
		<!-- The version of the BOM tl-bom and of the tl-maven-plugin. -->
		<tl.version>8.0.0</tl.version>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>com.top-logic</groupId>
				<artifactId>tl-bom</artifactId>
				<version>${tl.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<dependencies>
		<dependency>
			<groupId>com.top-logic</groupId>
			<artifactId>tl-license-professional</artifactId>
		</dependency>
		<dependency>
			<groupId>com.top-logic</groupId>
			<artifactId>tl-starter-react</artifactId>
			<type>pom</type>
		</dependency>
		<!-- further features, test dependencies tl-basic / tl-core (test-jar) -->
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>com.top-logic</groupId>
				<artifactId>tl-maven-plugin</artifactId>
				<version>${tl.version}</version>
				<!-- Provides the packaging of this project. -->
				<extensions>true</extensions>
			</plugin>
		</plugins>
	</build>

	<!-- repositories / pluginRepositories tl-releases and tl-snapshots -->
</project>
```

- **`tl.version`** is the single place naming the TopLogic version. Use it for both the BOM import
  and the plugin version: the packaging builds the project with the parent configuration of the
  plugin's own version, so BOM and plugin must not differ: the build stops with an error if the
  version of `tl-maven-plugin` managed by the imported BOM differs from the version of the plugin.
  Upgrading the engine means changing this property.
- **`tl-bom`** manages the versions of all TopLogic modules and of the libraries they are built and
  tested with, so dependencies are declared without `<version>`. The project must import it
  itself; the packaging contributes no `dependencyManagement`. An imported BOM is interpolated in
  its own context, so setting a version property of the BOM (e.g. `jetty.version`) in the project
  has no effect; to use another version of a library, declare that dependency with the version in
  the project's own `dependencyManagement`, which wins over the import.
- **`<packaging>tl-app</packaging>`** / **`tl-fragment`** together with the plugin declared with
  `<extensions>true</extensions>`: the plugin rebuilds the project as if `tl-parent-app` /
  `tl-parent-core` were the parent of the top-most POM of its parent chain. The project takes the
  build section (plugins, plugin management, resources), properties, dependencies, repositories,
  reporting and the profiles activated there; its own POM and its own parents override them as with
  inheritance. Group, artifact, version, name, licenses, SCM, distribution management and modules
  stay as the project declares them. The main artifact is a jar either way.
- **Repositories**: the generated POM declares the TopLogic repositories `tl-releases` and
  `tl-snapshots` for dependencies and plugins, so it builds without Maven settings, also against a
  TopLogic snapshot version.

## Using a company parent POM

Because the packaging needs no TopLogic parent, the project may inherit the parent POM of the
company (distribution management, company plugins, code-style checks, ...):

```xml
<parent>
	<groupId>com.example</groupId>
	<artifactId>company-parent</artifactId>
	<version>3</version>
</parent>
<packaging>tl-app</packaging>
```

The TopLogic configuration is placed above the top-most POM of the chain, so both the company
parent and the project override it. The project still declares `tl.version`, the BOM import and
the `tl-maven-plugin` with extensions. A project whose parent chain already contains a TopLogic
parent POM is built by plain inheritance; the packaging then adds nothing.

## Features: starters and single modules

A feature starter is a POM bundling the TopLogic modules of a feature. It is declared with
`<type>pom</type>` and without a version:

```xml
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-starter-ai</artifactId>
	<type>pom</type>
</dependency>
```

| Starter | Modules |
|---|---|
| `tl-starter-react` | `tl-layout-view`, `tl-layout-view-contact`, `tl-layout-react-codeedit`, `tl-layout-react-wysiwyg`, `tl-model-wysiwyg-core`, `tl-model-search-react`, `tl-react-flow-server`, `tl-dev-tools` |
| `tl-starter-classic` | `tl-themes-core`, `tl-layout-scripting-template`, `tl-layout-formeditor`, `tl-layout-themeedit`, `tl-graph-diagramjs-server`, `tl-graphic-blocks-server`, `tl-monitoring`, `tl-doc`, `tl-model-search`, `tl-contact`, `tl-model-wysiwyg` |
| `tl-starter-ai` | `tl-ai-core`, `tl-chat`, `tl-ai-views`, `tl-ai-mcp-server`, `tl-ai-a2a`, `tl-layout-view-agent` |
| `tl-starter-ai-search` | `tl-ai-index`, `tl-ai-index-onnx` |

The HTML attribute types `tl.model.wysiwyg:Html` and `tl.model.i18n:I18NHtml` are defined in
`tl-model-wysiwyg-core`, which contains no user interface and is shared by both starters. The
classic UI edits them with the CKEditor of `tl-model-wysiwyg` (in `tl-starter-classic`), the React
UI with the editor of `tl-layout-react-wysiwyg` (in `tl-starter-react`). A React-only application
therefore contains none of the classic modules `tl-layout-wysiwyg`, `tl-model-wysiwyg` and
`ext-com-ckeditor`.

The remaining features are single modules, declared like any other dependency (without version):

| Feature | React UI | Classic UI |
|---|---|---|
| Charts | `tl-layout-react-chartjs` | `tl-chart-chartjs` |
| Account self-service | `tl-layout-view-selfservice` | `tl-security-selfservice` |
| Process engine | - | `tl-bpe-app` |
| Chat tab of the AI agents | (part of `tl-starter-ai`) | `tl-chat-classic` |
| Single sign-on (pac4j) | `tl-security-auth-pac4j` | `tl-security-auth-pac4j` |
| OpenAPI client / server | `tl-service-openapi-client` / `tl-service-openapi-server` | same |

A feature is added later by adding its dependency and removed by deleting it; a module brings its
own configuration, model and views along. Switching the user interface also means
adding the UI files: the shell `WEB-INF/views/app.view.xml` for React, the folder
`WEB-INF/layouts` for the classic UI (generate a scratch project with the target `ui` to copy
them from).

An application using the AI agents must configure the LLM providers its agents use (API keys,
models); nothing is preconfigured. The services `LLMProviderService` and `AgentRuntimeService`
are described in the article `ai/providers` of `tl-ai-core`.

## Fragment modules

A fragment is generated with `-DmoduleType=fragment` and declares its own TopLogic dependencies
(starters, single modules) exactly like an application, but no license module. An application uses
a fragment by an ordinary dependency on its jar; the fragment is not managed by `tl-bom`, so the
dependency carries a version:

```xml
<dependency>
	<groupId>com.example</groupId>
	<artifactId>my-fragment</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

The fragment's configuration, model and views are picked up from its jar through its
`META-INF/web-fragment.xml` and its `metaConf.txt`. Application and fragments can live in one
multi-module build with an aggregator POM listing them as modules; Maven builds each fragment
before the application depending on it.

## Debian package

An application builds a Debian package from `src/main/deb` (control scripts in
`src/main/deb/control`, the Tomcat context, the Apache site and the database setup script in
`src/main/deb/data`) in the phase `package`, and can upload it to an APT repository. Both are part
of the configuration of `tl-parent-app` and are switched off by default. The properties, set in the
application POM:

| Property | Default | Meaning |
|---|---|---|
| `tl.deb.skip` | `true` | `false` builds `target/<artifactId>_<version>_all.deb`. |
| `tl.deb.attach` | `false` | `true` attaches the package to the project artifacts. |
| `tl.deb.maintainer` | `info@<groupId>` | Field `Maintainer` of the package. |
| `tl.deb.section` | `misc` | Field `Section`. |
| `tl.deb.priority` | `optional` | Field `Priority`. |
| `tl.deb.distribution` | `development` | Field `Distribution`. |
| `tl.deb.installDir` | `/opt/<artifactId>` | Installation directory of the war and the configuration. |
| `tl.deb.contextName` | `<artifactId>` | Name of the Tomcat context and the Apache site. |
| `tl.deb.dbSchema` | `<artifactId>` | Database schema created by the setup script. |
| `tl.deb.dbUser` | `<artifactId>` | Database user created by the setup script. |
| `tl.deb.skipDeploy` | `true` | `false` uploads the package to the APT repository. |
| `tl.deb.repositoryUrl` | `Enter URL here.` | URL of the APT repository. |
| `tl.deb.repositoryPath` | `/` | Path within the repository. |
| `tl.deb.serverId` | `tl-deb` | ID of the `server` entry in the Maven settings holding the credentials. |

The package depends on `mariadb-server`, `tomcat10`, `libmariadb-java`, `apache2` and `apg`. An
application that configures the `jdeb` plugin with an execution without ID changes this execution
instead of adding a second one.

## Switching a project from tl-parent-app to the packaging

A project inheriting `tl-parent-app` (or, as a fragment, `tl-parent-core`) keeps building
unchanged; switching to the packaging is optional and is worthwhile when the project should get a
company parent or join an existing multi-module build. The steps, in the project POM:

1. **Parent**: remove the `<parent>` naming `tl-parent-app` / `tl-parent-core`. Declare
   `<groupId>` and `<version>` explicitly if they were inherited from the TopLogic parent so far.
2. **Packaging**: set `<packaging>tl-app</packaging>` (fragment: `tl-fragment`) and declare the
   `tl-maven-plugin` with `<version>${tl.version}</version>` and
   `<extensions>true</extensions>` in `<build><plugins>`, see [The project POM](#the-project-pom).
3. **Versions**: add the property `tl.version` with the former parent version and the import of
   `tl-bom` with `<version>${tl.version}</version>` in `dependencyManagement`. Dependencies on
   TopLogic modules and managed libraries stay without `<version>`.
4. **Version-property overrides**: a property such as `jetty.version` set in the project no longer
   changes the version of the library, because the imported BOM is interpolated in its own
   context. Declare each such library with its version in the project's own
   `dependencyManagement` instead; that entry wins over the import.
5. **Repositories**: the TopLogic parent POM was resolved through the Maven settings or the
   project's `repositories`. The BOM, the plugin and the modules need the same repositories:
   declare `tl-releases` and `tl-snapshots` both as `repository` and as `pluginRepository`
   (`https://dev.top-logic.com/nexus/repository/toplogic/` and
   `https://dev.top-logic.com/nexus/repository/toplogic-snapshots/`) unless the settings provide
   them.
6. **Debian package**: a project generated with an older archetype configures the plugins `jdeb`
   and `upload-maven-plugin` itself. Their executions merge with those of `tl-parent-app`, so the
   project keeps building one package, but the blocks are now redundant: delete them, and delete
   the `tl.deb.*` properties that equal the defaults in [Debian package](#debian-package).
7. **Starters** (optional): replace the single modules of a feature by its starter, see
   [Features: starters and single modules](#features-starters-and-single-modules). Fragments that
   do not depend on each other may change places in the module order if the project listed them
   in another order than the starter. This changes the order within merged files only
   (`metaConf.txt`, `theme.xml`, theme settings, the tabs of merged views).

To check the switch, compare the output of `mvn help:effective-pom` before and after: plugins,
properties and dependencies must be the same, apart from the removed parent. For an application,
compare the content of `target/<finalName>-app.war` as well.

## Inheriting tl-parent-app

An application may instead inherit the TopLogic parent POM, which carries the same configuration
and imports the versions itself:

```xml
<parent>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-parent-app</artifactId>
	<version>8.0.0</version>
</parent>
```

Such a project uses the packaging `jar`, needs neither the BOM import nor the plugin declaration,
and may override the version properties of `tl-bom` (e.g. `jetty.version`), since it inherits
them. A fragment correspondingly inherits `tl-parent-core`. The project then cannot have a company
parent; without the version of its own it also inherits the TopLogic version as its own version.
