---
description: Read before adding the assistant Albert to an existing application - the steps in order (dependencies tl-ai-core / tl-chat / tl-chat-classic or tl-starter-ai, the provider key secret tl_<provider>_api_key and tl_ai_default_model, the shipped React page tl-chat/albert.view.xml in a <nav-item> and its overlay (ids, rail sections by name, composer channels chatDraft / chatReference / chatQuote, a <chat-dock> embedding the page with pageSelection) or the classic Albert tab, the shipped rights on own chats, the AiToolAccessService policy per role with <agent-access>, domain knowledge as TextPromptContributor "application-context"), how to check the result with accounts that are no administrators, the operation of the installation and the pitfalls.
order: 15
---

# Adding Albert to an application

Albert is the chat assistant of TopLogic. Its technical name is `TL_SingleAgent`. This article gives the steps that add Albert to an existing application. Do the steps in the given sequence. Each step links the reference article that has the details.

Paths are relative to `src/main/webapp/WEB-INF/` of the application. Exception: `pom.xml` is in the project folder.

An application has one of two user interfaces, or both:

- **React UI**: views in `views/*.view.xml`, entry point `/view/`. The archetype `tl-archetype-app` makes this UI by default.
- **Classic UI**: the layouts of the `LayoutServlet`. Albert is a tab of the main tab bar.

A step marked **[React]** or **[Classic]** applies only to that UI.

An application made with `tl-archetype-app` and `useAI=true` already has steps 1 and 3. It also has a template for step 5 in `autoconf/com.top_logic.tl.ai.core.security.AiToolAccessService.config.xml`.

## Before you start

Albert acts as the logged-in account. Albert reads only the objects that the account can read. Albert changes only the objects that the account can change. Thus the model-based security of the application must be complete before you add Albert:

- Each type that a role must read or change has its grants in the `<security-config>` of the `SecurityConfigurationService`. A type without a grant is not readable for a role-based account. See [access configuration](doc:access-configuration).
- Each role has two assignments with two different jobs. Make both:
  - **On the security root**, for step 5: a `<singleton-rule>` with `target="SecurityStructure#ROOT"`. `<tool-access>` and `<agent-access>` find the roles of an account only on the security root. A role that the role rules give only on business objects gets no tools and no agent.
  - **On the business objects**, for the `<security-config>`: a `<rule type="reference">` on the type. A role on the security root gives no rights on objects, because the security root is not a security parent of the business objects (`use-default-security-parent="false"`, the default).

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<role-rules>
			<rules>
				<!-- For <tool-access> and <agent-access>: the role on the security root. -->
				<singleton-rule id="tier-reader" target="SecurityStructure#ROOT" role="SecurityStructure.Reader">
					<path>
						<group-with-name name="readers"/>
					</path>
				</singleton-rule>
				<!-- For <security-config>: the same role on every object of the type. -->
				<rule id="type-reader" meta-element="my.app:Order" role="SecurityStructure.Reader" type="reference">
					<path>
						<group-with-name name="readers"/>
					</path>
				</rule>
			</rules>
		</role-rules>
	</instance>
</config>
```

  See [tool access configuration](doc:ai/security/tool-access-configuration#the-rules-in-one-place) and [the hinge of the worked example](doc:ai/security/role-model-worked-example#the-hinge-that-makes-or-breaks-this).

The examples below use the roles `SecurityStructure.Reader` (read only) and `SecurityStructure.Writer` (read and write). Use the roles of your application.

The [worked example](doc:ai/security/role-model-worked-example) builds a `<security-config>` and a tool access policy for four roles from the start.

## 1. Add the dependencies

Add the AI modules to `pom.xml`. Do not write a version: the BOM `tl-bom` gives the engine version.

```xml
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-ai-core</artifactId>
</dependency>
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-chat</artifactId>
</dependency>
```

- **[Classic]** Also add `tl-chat-classic`. This module adds the Albert tab. Do not add it to an application that has only the React UI: it sets the start page and the login page to the `LayoutServlet`.
- As an alternative, use the starter `tl-starter-ai` (`<type>pom</type>`). It also brings `tl-ai-views`, `tl-ai-mcp-server`, `tl-ai-a2a` and `tl-layout-view-agent`. Use the starter only if you need these modules.
- An older application can have the property `tl.ai.version` and `<version>${tl.ai.version}</version>` on the AI dependencies. Remove them.
- `tl-ai-core` does not bring `tl-license-professional`. If the application uses the Professional Edition, make it a direct dependency.

The [module table](doc:ai#modules) tells what each module gives.

## 2. Give the key of the LLM provider

`tl-ai-core` has four providers: `anthropic`, `openai`, `mistral` and `xai`. A provider without a key is inactive. The application starts without a key, but Albert does not answer.

1. Select the provider and the model together with the owner of the data. The data that Albert reads goes to the provider.
2. Set the key as the named secret of the provider: `tl_anthropic_api_key`, `tl_openai_api_key`, `tl_mistral_api_key` or `tl_xai_api_key`. The default secret store reads the environment variable or system property of that name. The value is the plain key, without the prefix `unencrypted:`.
3. Optional: set the model of the agents with `tl_ai_default_model`. Without it, Albert uses the first model of the first provider that has a key. A model that is not in the default model list of its provider also needs the variable `tl_<provider>_models`, for example `tl_openai_models`. Without it, the start logs the error "no provider exposes model ...".

```bash
export tl_openai_api_key="sk-..."
export tl_openai_models="gpt-5.5"           # optional: the models of the provider
export tl_ai_default_model="gpt-5.5"
```

For local development, you can also write the key into the file `~/.top-logic/secrets/<name of the secret>`. This needs the line `devel-secrets.xml` in `deploy/local/webapp/WEB-INF/conf/metaConf.txt`. The archetype writes this line. See [development secrets](doc:credentials/secret-stores#development-secrets).

An own provider (another endpoint, a gateway, Amazon Bedrock) is configured on `LLMProviderService`. Give its key as `<api-key name="..."/>`, not as an alias. See [configuring the LLM providers](doc:ai#configuring-the-llm-providers) and [providers and failover](doc:ai/providers).

## 3. Show Albert to the users

### [React] The Albert page

`tl-chat` ships the page `views/tl-chat/albert.view.xml`. It shows:

- the chats of the logged-in account, grouped by day, with the commands *New chat*, *Rename* and *Delete*;
- the conversation;
- a rail with the plan of the agent (*Progress*), the cited *Sources* and the *Memory* of the agent about the account. The rail also shows the tables, charts and forms that Albert presents, and the object that the user opens from them.

A new chat is about the object that the pane of the rail shows. While the pane shows an object that has chats of the account, the chat list shows only these chats and the open chat. Else the chat list shows all chats of the account.

The page has the channels `chatDraft`, `chatReference` and `chatQuote` for the composer: the text, an object as a reference, and a passage of an object as a quote. A command on another page writes one of these channels to put a question into the composer. The user then sends the question. To connect the channels, declare a channel in `app.view.xml` and bind it on the `<view-ref>` of the page and of the other page: `<bind channel="chatDraft" to="chatDraft"/>`. The module documentation of `tl-chat` describes the channels.

Add one sidebar item to the overlay `views/app.view.xml` of the application. The `id` must be unique in the complete view.

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<sidebar config:ref="sidebar">
		<items>
			<section id="application">
				<nav-item id="albert"
					icon="css:bi bi-chat-dots"
				>
					<label>
						<en>Albert</en>
						<de>Albert</de>
					</label>
					<view-ref view="tl-chat/albert.view.xml"/>
				</nav-item>
			</section>
		</items>
	</sidebar>
</view>
```

[The application shell](doc:view-layer/app-shell) tells how the overlay of `app.view.xml` works.

**Optional: show the page only to the Albert roles.** Do the three parts together. If one part is missing, the application does not start.

1. Register the operation on `CommandGroupRegistry` in `conf/<app>.conf.config.xml`:

   ```xml
   <config service-class="com.top_logic.tool.boundsec.simple.CommandGroupRegistry">
   	<instance>
   		<group name="AlbertChat" type="read"/>
   	</instance>
   </config>
   ```

2. Declare the operation on the security root and grant it in the `<security-config>`:

   ```xml
   <singleton name="SecurityStructure#ROOT">
   	<operations>
   		<operation name="AlbertChat"/>
   	</operations>
   	<grant operation="AlbertChat" roles="SecurityStructure.Reader, SecurityStructure.Writer"/>
   </singleton>
   ```

3. Add the texts `commandGroup.AlbertChat` and `commandGroup.AlbertChat.tooltip` to `conf/resources/<app>.app-messages_en.properties` and `_de.properties`.

Then add `<access-control operation="AlbertChat"/>` to the `<nav-item>`. See [access control of UI units](doc:view-layer/access-control).

**Optional: Albert on every page.** A `<chat-dock>` in the `<footer>` of the application shell shows Albert as a button in the corner of each page. The dock embeds the Albert page. The page uses the default chat id, so a `<chat-dock>` without `chat-id` shows the same conversation as the page. The demo application `tl-ai-demo` has an example in `views/app.view.xml`.

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<channels>
		<channel name="chatDraft"/>
		<channel name="pageSelection"/>
	</channels>
	<app-shell>
		<footer>
			<chat-channels channels="pageSelection"/>
			<chat-dock icon="css:bi bi-stars">
				<label>
					<en>Albert</en>
					<de>Albert</de>
				</label>
				<view-ref view="tl-chat/albert.view.xml">
					<bind channel="chatDraft" to="chatDraft"/>
					<bind channel="pageSelection" to="pageSelection"/>
				</view-ref>
			</chat-dock>
		</footer>
	</app-shell>
</view>
```

- `pageSelection` tells Albert what the page behind the dock has open. `<chat-channels>` publishes the channel to the pages, and each page writes its object with `<page-selection to="pageSelection">`. The Albert page states this object while its pane shows nothing.
- A conversation in a closed dock offers Albert nothing: no channel for `present`, no page command and no facts. Albert presents tables, charts and forms in the pane that the user sees.
- The dock hides itself while a page shows the same conversation, for example the Albert page. An open dock closes when such a page appears.

**Change the page.** Do not copy the page. Write a same-path overlay `views/tl-chat/albert.view.xml` in the application and address the elements by their ids: `albert` (the `<chat>`), `chats` (the chat list), `conversation` (the `<chat-conversation>`) and `rail` (the `<chat-rail>`). See [overlays](doc:view-layer/overlays).

The sections of the rail have the names `progress`, `sources` and `memories`. The name is the key of a section. A `<section>` with a new name adds a section. Use `config:position` and `config:reference` to put it between the shipped sections. A `<section>` with a shipped name changes that section, and `config:operation="remove"` removes it.

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<chat-conversation config:ref="conversation">
		<placeholder>
			<en>Ask about the orders and their customers.</en>
			<de>Fragen Sie nach den Aufträgen und ihren Kunden.</de>
		</placeholder>
		<controls model-picker="false"/>
	</chat-conversation>
	<chat-rail config:ref="rail">
		<section name="orders" config:position="after" config:reference="sources"
			icon="css:bi bi-receipt"
		>
			<label>
				<en>Orders</en>
				<de>Aufträge</de>
			</label>
			<data-list selection="paneObject" search="false" collapsible="false">
				<lines>
					<line attribute="name"/>
				</lines>
				<rows><![CDATA[all(`my.app:Order`)]]></rows>
			</data-list>
		</section>
		<section name="memories" open="true"/>
	</chat-rail>
</view>
```

A row of the section *Orders* opens the order in the pane of the rail.

| Goal | Change on `conversation` |
|---|---|
| Hide the selection of model, agent, MCP servers, attachments or thinking | `<controls model-picker="false" agent-picker="false" mcp="false" attachments="false" thinking="false"/>` |
| Hide copy, rate or regenerate below the answers | `<actions copy="false" rate="false" regenerate="false"/>` |
| Own text in the input field, first questions | `<placeholder>`, `<prompts>` |
| Tell Albert what the page shows | `<context>`, `<selection>`; for a dock: `<chat-channels>` in the shell and `<page-selection>` in the pages |

The view hides controls only. What an account can do is the decision of the policy of step 5. [The chat UI](doc:ai/chat-ui) and the module documentation of `tl-chat` describe the elements.

### [Classic] The Albert tab

`tl-chat-classic` adds the tab `tl-chat/chat/tabLayout.layout.xml` at the start of the main tab bar (`mainTabbar.layout.xml`). You do not add the tab yourself.

- If the main tab bar of the application has another name, add `<layout-reference resource="tl-chat/chat/tabLayout.layout.xml"/>` to it.
- Give each role that uses Albert the read right on the tab in `conf/security.xml`. Without this right, an account that is no administrator does not see the tab.

```xml
<profile name="SecurityStructure.Reader">
	<view name="tl-chat/chat/tabLayout.layout.xml#TabLayout">
		<commandGroup name="Read"/>
	</view>
	<view name="tl-chat/chat/agent.layout.xml#agent">
		<commandGroup name="Read"/>
	</view>
</profile>
```

The tab shows `views/tl-chat-classic/chat.view.xml`. To change it, copy the view into the application and point the component `AgentContent` of `layouts/tl-chat/chat/agent.xml` at the copy. Compare the copy with the original after each update of TopLogic.

## 4. Own chats: nothing to do

The chat list is a TL-Script. A TL-Script result contains only objects that the account can read. `tl-ai-core` ships the rights of an account on its own chats and memories (`tl-ai-core.chatSecurity.config.xml`, role `AiChatOwner`). Do not write role rules or grants for the types of `tl.ai.execution`. See [the shipped access policy for conversations](doc:ai/security/administration#the-shipped-access-policy-for-conversations).

## 5. Set what Albert can do for each role

The policy of `AiToolAccessService` tells which tool groups Albert can use for a role, and which roles can talk to Albert. The `<security-config>` tells which objects a tool can read or change. Both checks apply to each tool call.

Write the policy in `autoconf/com.top_logic.tl.ai.core.security.AiToolAccessService.config.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<services>
		<config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
			<instance class="com.top_logic.tl.ai.core.security.AiToolAccessService">
				<tool-access>
					<role name="SecurityStructure.Reader">
						<grants>
							<grant group="model.read"/>
							<grant group="data.read"/>
						</grants>
						<denies>
							<deny group="data.write"/>
							<deny group="script"/>
							<deny group="egress"/>
							<deny group="model.structure"/>
							<deny group="agent.authoring"/>
						</denies>
					</role>
					<role name="SecurityStructure.Writer">
						<grants>
							<grant group="model.read"/>
							<grant group="data.read"/>
							<grant group="data.write"/>
						</grants>
						<denies>
							<deny group="script"/>
							<deny group="egress"/>
							<deny group="model.structure"/>
							<deny group="agent.authoring"/>
						</denies>
					</role>
				</tool-access>
				<!-- Nothing for all accounts: tools only through a role. -->
				<baseline/>
				<!-- Mandatory when a role is named: the limit of a run without a person. -->
				<background-allowlist>
					<grant group="model.read"/>
					<grant group="data.read"/>
				</background-allowlist>
				<!-- Only these roles talk to Albert. -->
				<agent-access default="none">
					<role name="SecurityStructure.Reader">
						<grants><grant agents="TL_SingleAgent"/></grants>
					</role>
					<role name="SecurityStructure.Writer">
						<grants><grant agents="TL_SingleAgent"/></grants>
					</role>
				</agent-access>
			</instance>
		</config>
	</services>
</application>
```

- Grant `data.write` only to roles that can change data in the UI. Albert asks the account to approve each change.
- Grant `script` only with care. With `data.write`, `changeData` changes any type in one script.
- Without `<agent-access default="none">`, each account that can log in can talk to Albert.
- A role that does not exist stops the start. A policy that names a role and has no `<background-allowlist>` also stops the start.
- **[Classic]** An administrator can also edit the policy in *Administration > Access rights > Agent security*. See [the policy editor](doc:ai/security/tool-access-editor).

The tool groups and the rules are in [tool access configuration](doc:ai/security/tool-access-configuration). The agents, models and MCP servers per role are in [run access](doc:ai/security/run-access). Ready-made policies are in [recipes](doc:ai/security/tool-access-recipes).

## 6. Give Albert the domain knowledge

Albert knows the model from the labels and tooltips of its elements. Give each module, type and attribute a label and a tooltip in English and German. Keep `conf/resources/*.properties` normalized.

Write the terms and the fixed rules of the domain into `autoconf/com.top_logic.tl.ai.core.agent.AgentApplicationService.config.xml`. Keep the text short: each run sends it to the model.

```xml
<?xml version="1.0" encoding="utf-8" ?>
<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<services>
		<config service-class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
			<instance class="com.top_logic.tl.ai.core.agent.AgentApplicationService">
				<prompt-contributors>
					<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
						agents="TL_SingleAgent"
						name="application-context"
					>
						<text>
							<en><![CDATA[## Domain knowledge
An order is 'open' until it is invoiced.]]></en>
							<de><![CDATA[## Fachwissen
Ein Auftrag ist 'offen', bis er abgerechnet ist.]]></de>
						</text>
					</contributor>
				</prompt-contributors>
			</instance>
		</config>
	</services>
</application>
```

The name `application-context` marks the entry that the skill `TL_init` reads and writes. See [prompt contributors](doc:ai/environment#prompt-contributors).

## 7. Check the result

Use a new database. Use accounts that are no administrators: an administrator bypasses the tool policy and the model security, so a test as `root` proves nothing about the roles.

1. Start the application. Make sure that the log has no `ERROR` and has the line `Agent tool access: roles=<n> ...`.
2. Log in as `root`. In the React administration (`/view/`), open *Administration > System > Secrets*. Make sure that the secret of the provider shows *Available*.
3. Log in with one account for each role: at least one role that writes and one role that only reads. For each account, make sure that:
   - the Albert page or the Albert tab is visible;
   - a question gets an answer;
   - the chat list shows only the chats of this account.
4. With the account that only reads, tell Albert to change an object. Make sure that Albert refuses and that the object does not change.
5. Log in with an account without an Albert role. With the optional operation of step 3, the Albert page is not visible. Without it, the chat answers that no agent is available.

## 8. Operate the installation

- Set the secret of the provider in each target environment. Do not commit a key.
- Get the approval of the data owner for the provider, the model and the data flow to the provider.
- Do not set `tl_reset_password` together with the same `tl_initial_password` for each start. The second start fails ("Password has been used before").

## Pitfalls

| Symptom | Cause | Remedy |
|---|---|---|
| The start stops with "The command group ... does not exist". | An `<access-control operation="...">` names an operation that is not registered. | Do all three parts of the optional operation of step 3. |
| The start stops after the upgrade to 8.0.0-alpha12. | `<model-catalog>` still has `<llm-configuration>`. | Remove `<llm-configuration>`. |
| The start stops because a model is ambiguous. | An own provider and a shipped provider have the same model. | Write `<providers config:override="true">`, or name the provider with `default-provider`. |
| Albert does not answer. | The provider has no key. | Set the secret of step 2. The chat names the missing secret. |
| A key does not work as an alias value. | `api-key="%ALIAS%"` reads an encrypted value. | Use `<api-key name="..."/>` (step 2). |
| [Classic] An account that is no administrator does not see the Albert tab. | The role has no read right on the tab. | Add the profile entries of step 3 to `conf/security.xml`. |
| A view fails with "Undefined variable". | A script in a view uses a lambda head, or `rows` of a `<data-list>` uses `$<channel>`. | In views, scripts have no lambda head. Only the row functions of `<data-list>` take their inputs as positional arguments (`session -> ...`). |
| A view overlay stops the start with a duplicate id. | An `id` is used two times in the merged view. | Make each `id` unique in the complete view. |
| Tables of Albert show `[model.<module>.<Type>.<attribute>]`. | The model element has no label. | Add the labels of step 6. |
| Albert reads nothing. | The roles have no grants in the `<security-config>`. | Complete the model security (see "Before you start"). |
| Albert has the tools, but each tool finds no object. | The role is held on the security root only. | Add a role rule that gives the role on the objects (see "Before you start"). |
| The chat answers that no agent is available, for an account with an Albert role. | The role is held on business objects only, not on the security root. | Add a `<singleton-rule>` on `SecurityStructure#ROOT` (see "Before you start"). |
