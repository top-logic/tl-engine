---
description: Read to learn what an agent of a TopLogic application can do with the shipped tools - explore and extend the model (model.read, model.structure), read and change data (data.read, data.write, query, changeData), manage views and layouts, read configuration schemas, write TL-Script, explain the application and its access rights, check consistency, refactor and migrate.
order: 20
---

# Capabilities

The agent reaches the application through its tools. Each tool belongs to a capability group (`model.read`, `data.write`, `script`, ...), and an administrator decides per role which groups an agent acting for that role may use - see [tool authorization](doc:ai/security/tool-authorization#tool-groups). What the groups add up to:

```
+-----------------------------------------------------------+
|                      the agent                            |
|                                                           |
|   "Which types are there?"   "Create a table"             |
|   "Run this script"          "Add a new type"             |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                      the tools                            |
|                                                           |
|   explore        read and        manage                   |
|   the model      change data     views                    |
|                                                           |
|   read config    run             extend                   |
|   schemas        TL-Script       the model                |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                 the TopLogic application                  |
|                                                           |
|   data model     business data     user interfaces        |
|   configuration  scripts           accounts               |
+-----------------------------------------------------------+
```

## Exploring and extending the data model

The agent reads and understands the application's model: which modules there are, which types they define, which properties and references those have (`listModules`, `listTypes`, `listTypeParts`, `outlineModel`). It searches for model elements, analyzes where a type is used (`findTypeUsages`, `findPartUsages`) - and extends the model: new modules, types, properties and references (`createModule`, `createClass`, `createProperty`, `createReference`, the `model.structure` group).

*Example: "Which properties does the type 'Customer' have?" or "Create a type 'Supplier' with name, address and rating."*

Taken further, the agent builds a complete business application in a conversation: "I need a project management with tasks, milestones and resource planning." It creates modules, types, properties and references, builds matching views and configures forms - no UML, no specification document, no developer.

## Reading and evaluating data

The agent reads the application's data: lists all customers, looks up one record, filters and summarizes (`listInstances`, `getInstance`, `query`). Beyond retrieval it analyzes and interprets: "show me the revenue of the last 12 months per region." It writes a script, runs it, interprets the result and explains it in natural language - or builds a dashboard view right away. `present` opens an object or a result where the page has a place for it.

*Example: "List all active projects" or "Show me the details of customer 4711."*

## Changing data

One object the agent knows by its id is changed with the object tools `updateObject`, `createObject` and `deleteObject` (group `data.write`); a set chosen by a condition is changed with one TL-Script through `changeData`. Every change needs the user's approval, is checked against the user's rights per object and per attribute, and is carried out in one transaction per turn - completely or not at all. [Object tools](doc:ai/security/object-tools) describes the checks.

## Managing user interfaces

The agent knows the available view templates and creates views: it reads the schema of a template (which parameters it expects) and produces a view with the settings asked for (`listLayoutTemplates`, `getLayout`, `createLayout`). With `tl-ai-views` it builds and changes `.view.xml` views of the React UI, checked by the engine before anything is written and recorded as change sets it can undo.

*Example: "Create a table of all open orders" or "Which view templates are there?"*

Users shape their own working environment this way: "make the customer list clearer, add a column for the last contact and sort by priority." The agent changes the configuration without anybody opening an editor.

## Understanding configuration

Every configuration option of TopLogic has a schema that describes which settings are possible. The agent reads these schemas (`getConfigSchema`) and understands how a component can be configured.

*Example: "Which settings does the table component have?" or "What can I configure on a form layout?"*

## Describing business logic

TopLogic has its own script language, TL-Script, for computations, queries and automation. The agent writes scripts, checks their syntax and runs them; it knows the available functions and their documentation (`listTLScriptFunctions`, `getTLScriptFunctionDocumentation`).

*Example: "Compute the sum of all open invoices" or "Write a script that lists all inactive users."*

This also covers business processes: "when an order gets the status 'completed', an invoice shall be created automatically." The agent sets up rules, workflows or script expressions, and the platform runs them - configuration, not deployed code.

## Explaining the application

The agent knows the data model and describes relationships in natural language: "what does this field mean?", "why don't I see this data?", "how are orders and invoices related?" The application explains itself.

## Making access rights understandable

"Why can't I edit this record?" - not only end users ask this, developers do so regularly as well. In a configurable application with roles, group rights, type permissions and object-specific rules the answer is often not obvious; even somebody who knows the security configuration has to walk several levels to see why one access is granted or refused.

The agent follows that chain and explains it:

*End user: "Why don't I see the menu item 'Reports'?" - "You hold the role 'Clerk'. The item 'Reports' is visible to the role 'Team lead' only. Ask your administrator if you need access."*

*Developer: "Why can't user 'meier' create orders?" - "The user holds the role 'Sales reader'. It allows reading the type 'Order', not writing it. The permission is defined in the role group 'Sales' with the restriction 'read only'."*

The agent searches role assignments, group memberships and permission rules and gives a traceable explanation instead of a silent refusal: less frustration and fewer support requests for users, much less time spent debugging the security configuration for developers. The same holds for its own refusals - a denied tool call names the capability the user lacks, see [capability notices](doc:ai/security/capability-notices).

## Assuring quality

The agent checks consistency between model, configuration and views: "are all mandatory fields visible in the forms?", "are there types without a view?" It finds inconsistencies that manual review easily misses.

## Refactoring and migration

"We want to merge 'Supplier' and 'Customer' into 'Business partner'." The agent restructures the model, migrates the data and adapts the views - a task that takes days by hand.
