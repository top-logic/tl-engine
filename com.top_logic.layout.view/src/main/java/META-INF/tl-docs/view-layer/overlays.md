---
description: Read before extending a view of another module with a same-path overlay (WEB-INF/views/<same path>.view.xml) - addressing an element by its id with config:ref wherever it sits, adding, repositioning, replacing or removing one element of a nested list, config:position/config:reference, config:override, config:operation, config:index, anonymous elements, and which ids an extensible view gives its elements.
order: 85
---

# Extending a view with a same-path overlay

## Same-path overlays

A module extends a view of a module it depends on without copying it: it ships a view of the **same path** in its own `src/main/webapp` (`WEB-INF/views/user-menu.view.xml` beside the one of `tl-layout-view`, say). The `ViewLoader` reads every copy of the path the `FileManager` finds and folds them in dependency order: the copy of the lowest module is the base, each copy of a depending module updates what the copies before it produced. The base knows nothing of its contributors; a fragment is in effect where its module is deployed and absent where it is not.

- In a development workspace the fold happens when the view is loaded (and again when any of its copies changes). A deployed WAR has only one file per path, so the application build pre-computes the fold (`ViewOverlayMerger`) and writes the merged view into the web application; both use the same merge.
- The lowest copy must be a real base. A copy that uses `config:ref` or `config:operation` is a fragment, and a fragment found at the bottom of the stack is rejected ("No base view found ... the lowest-priority copy is itself an overlay") - typically the base view was renamed or removed while fragments still target its path.
- The attributes steering the merge are in the namespace `xmlns:config="http://www.top-logic.com/ns/config/6.0"`, declared on the `<view>` of the fragment.

A fragment names the element it changes and writes only the change. The element is addressed by its id, wherever it sits in the base.

## Addressing an element: `config:ref`

Every element of a view, and every tab, pane, case, sidebar item and menu group, carries an optional `id`, and **an id names one thing in the whole view**: it is unique among all of them, in the base and its fragments together. A fragment addresses the element by writing an element of the same kind with `config:ref="<id>"`, anywhere above the element - directly in the `<view>`, as a rule:

```xml
<!-- WEB-INF/views/devices.view.xml of the module for virtual devices -->
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <tab-bar config:ref="device-tabs">
        <tabs>
            <tab id="virtual-device" config:position="after" config:reference="general">
                <label><en>Virtualization</en></label>
                <view-ref view="devices/virtualization.view.xml"/>
            </tab>
        </tabs>
    </tab-bar>
</view>
```

The referencing element is not an entry of the element it is written in; it stands for the element with that id, wherever the base keeps it - here a tab bar inside a split pane, a switch and a case. What it contains is merged into that element exactly as if the fragment had written the whole path to it: a tab with a new id is added to the `<tabs>`, an attribute replaces the base's value, a tab with an existing id is updated. The base may move the tab bar into another pane or wrap it in a stack; the fragment is unaffected as long as the id stays.

- `config:ref` addresses an **existing** element. An unknown id is an error, which makes a fragment fail loudly when the base no longer has the element it extends.
- **`config:override="true"`** on the referencing element replaces the element: the base's content of it is gone, the fragment's is the whole element. The element keeps its id.
- **`config:operation="remove"`** removes the element.
- **`config:position`** moves the element within its list - `begin`, `end`, or `before` / `after` the sibling named by its id in `config:reference`.
- The tag of the referencing element is the tag of the element it addresses (`<tab-bar>`, `<tab>`, `<menu>`); it determines the configuration type, as the tag of a list entry does. The referencing element may repeat the id, but not change it.

**Nesting.** A referencing element is written in an element above the one it addresses, and that element bounds the search: the root `<view>`, an element reached by its path (see below), or another referencing element. Fragments therefore nest the way the base nests, with the uninteresting layers left out - and the nesting is checked: a `<tab-bar config:ref="device-tabs">` inside a `<pane config:ref="list">` is an error when the tab bar sits in the detail pane. A fragment that sketches the base in its own terms stays readable, and it breaks when the base changes the containment it relies on, not silently:

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <switch config:ref="kind">
        <!-- Both below the switch; the pane and the case between them are not written. -->
        <tab-bar config:ref="device-tabs">
            <tabs>
                <tab id="virtual-device">...</tab>
            </tabs>
        </tab-bar>
        <text config:ref="nothing-selected" config:operation="remove"/>
    </switch>
</view>
```

A referencing element cannot stand on the root `<view>` or as an entry of a list property (inside `<tabs>`, `<panes>`, `<groups>`, `<channels>`); there, entries are matched by their key as described next. A view a `<view-ref>` embeds is a view of its own, with ids of its own: a fragment of `devices.view.xml` reaches nothing inside `devices/virtualization.view.xml`, that view has its own fragments.

## The merge rules

The fold is the merge of typed configuration, applied to the view's configuration tree. `config:ref` is the short way to a place in that tree; what happens there, and what a fragment does where it writes the path, are the same rules.

**Values replace, elements merge.** An attribute or a value property written in the fragment (`input`, `active-tab`, the `<title>` of an app bar, the `expr` of a channel) replaces the base's. An element in a single-valued property - the content of the `<view>`, for instance - is merged into the base's element: the fragment's `<tab-bar>` at the root of the view updates the base's tab bar, it does not take its place. `config:override="true"` on such an element replaces it as a whole.

**Lists merge by key.** Every list of a view is keyed, so an entry of the fragment finds the entry it means:

| List | Key |
|------|-----|
| the child lists of all elements (`children`, the content of a `<pane>`, a `<case>`, a `<tab>`, the `<default>` of a switch, the slots of an `<app-bar>`, the tiles of a dashboard, ...) | `id` of the element |
| `<panes>` of a `<split-panel>`, the cases of a `<switch>` | `id` of the pane, case |
| `<tabs>` of a `<tab-bar>`, the sections of an `<accordion>` | `id` (mandatory) |
| the items of a `<sidebar>` and of its groups, the items of a `<bottom-bar>` | `id` |
| the `<groups>` of a `<menu>` | `id` of the group |
| the steps of a `<wizard>` | `id` of the step source (unique within the wizard, not addressable by `config:ref`) |
| the `<channels>` of a `<view>` | `name` |
| the slots of an `<app-shell>` (`header`, `notices`, `content`, `footer`) | the kind of the element: at most one of a kind |

An entry of the fragment changes the list as follows:

- **An existing key updates the entry**: its properties are merged with the fragment's, recursively - this is how a fragment descends into a nested list by writing the path. The fragment repeats nothing of the entry but its key.
- **`config:override="true"`** on that entry replaces it: the base's content of the entry is gone, the fragment's is the whole entry.
- **`config:operation="remove"`** removes the entry.
- **A key the list does not have adds the entry**, at the end, or where `config:position="begin|end|before|after"` places it; `before` and `after` name the neighbor by its key in `config:reference="<id>"`. `config:position` on an updated entry moves it.
- **`config:operation`** states what the entry must do: `addOrUpdate` is the default described above, `update` requires the key to exist, `add` requires it not to exist.

**Anonymous elements.** An element without id is anonymous: it is appended in order, it matches no entry of another copy, and any number of anonymous elements may stand in one list. Most elements of a view are anonymous - a `<text>`, a `<stack>` that only arranges, a `<separator/>` of a sidebar. A fragment adds elements beside them, but cannot address them.

**Positional addressing.** `config:index="n"` on an entry addresses the entry at the zero-based position `n` of the list as merged so far and updates it like a key would (`config:override` replaces it, `config:position` moves it). It is the way into an anonymous element, and an emergency path: the position is a property of the base's current layout, so the fragment silently changes another element once the base inserts or reorders entries before it. A view that is meant to be extended gives the element an id instead. A position out of range and a combination with `add`, `remove` or `config:ref` are errors.

**Errors.** The merge fails when a `config:ref` names an id the enclosing element does not contain, when two elements of the view carry the same id, when the fragment updates or removes a key the list does not have, writes `update`/`remove` or a `config:reference` without key, or references a key the list does not have.

## The path form

A fragment that writes the path repeats, from the root of the view, every element on the way to the place it changes, each written with the attribute that identifies it and nothing else. The example at the top in the path form:

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <split-panel id="main">
        <panes>
            <pane id="detail">
                <switch id="kind">
                    <case id="device" config:operation="update">
                        <tab-bar id="device-tabs">
                            <tabs>
                                <tab id="virtual-device" config:position="after" config:reference="general">
                                    <label><en>Virtualization</en></label>
                                    <view-ref view="devices/virtualization.view.xml"/>
                                </tab>
                            </tabs>
                        </tab-bar>
                    </case>
                </switch>
            </pane>
        </panes>
    </split-panel>
</view>
```

Both produce the same merged view (`TestViewOverlayKeyedLists` pins that, with the base of the example). The path form couples the fragment to everything between the root and the target - the `update` on the case makes it fail loudly when the base restructures, without it the fragment appends a stray copy - and it is the only way to an anonymous element, through `config:index`. A referencing element inside an element written by its path is allowed; it addresses an element below that one.

## The engine's examples

`tl-layout-view` ships the account area of the app bar as `WEB-INF/views/user-menu.view.xml`: a `<switch id="session">` that offers a Login button in its case `anonymous` and the `<menu id="account-menu">` in its `<default>`. The `<groups>` of a menu are keyed by `id` (see [commands](doc:view-layer/commands)); the menu ships the group `account`. The overlay of `com.top_logic.demo.react` (`WEB-INF/views/user-menu.view.xml` of that module) contributes a group `demo` with an "About this demo" entry:

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <menu config:ref="account-menu">
        <groups>
            <group id="demo" config:position="after" config:reference="account">
                <generic-command image="css:bi bi-info-circle">
                    <label><en>About this demo</en><de>Über diese Demo</de></label>
                    <notify display="dialog" expr="x -> #('...'@en, '...'@de)"/>
                </generic-command>
            </group>
        </groups>
    </menu>
</view>
```

The same overlay redefines the channel `accountName` of the base: its `<derived-channel name="accountName">` matches the base's channel by name and replaces its expression. A `<group config:ref="account">` would add its commands to the base's group instead; the commands inside a group are not keyed, so they are appended.

Further shipped views written for overlays:

- the shell `app.view.xml` (see [the application shell](doc:view-layer/app-shell#the-application-shell-extending-appviewxml)): `<sidebar config:ref="sidebar">` and `<app-bar config:ref="app-bar">`, the trailing app-bar elements `dev-menu` and `user-menu` to position against;
- `dev-menu.view.xml`: the menu `dev-menu` with the groups `page`, `automation` and `reference`;
- `settings-tabs.view.xml`: the tab bar `settings-tabs` of the settings dialog;
- `admin/admin.view.xml`: the tab bar `sections` of the administration, and inside each section a tab bar of its views - `access-control-views`, `monitoring-views`, `system-views` - so a fragment adds a section (`<tab-bar config:ref="sections">`) or a view inside one (`<tab-bar config:ref="system-views">`); the views of the sections name their panes, panels, tables and forms (`main`, `list`, `detail`, `list-panel`, `table`, `form`, ...) for a fragment that adds a command to a toolbar or a field to a form.
- `admin/system/services/service-detail.view.xml`: the panel `detail` of the page of one service in the administration (System → Services). A module adds commands for its own kind of service to its toolbar with `<panel config:ref="detail"><commands>…</commands></panel>`, each guarded by `<visible-if expr="serviceInstanceOf($module, '<class or interface>')"/>` - the TL-Script function `serviceInstanceOf` tests the running instance of the service, so the command is shown only for a service of that kind, and only while it runs. A command taking `inputs="module, services, job as jobState"` re-evaluates that test after a restart of the service, and runs long work as `<start-job job="job">`: the page shows the channel `job` in its job status. The engine's own commands there - rebuilding and re-indexing the full-text index, rebuilding the security storage of the access manager - are written that way.

## Writing a view others extend

Give ids to the elements a fragment plausibly addresses, and to the elements a fragment positions against; leave the rest anonymous.

- An element whose content a module plausibly extends - a tab bar, a menu, a panel with a toolbar, a form, a switch and its cases, the panes of a split panel - gets a short id that names its role (`sections`, `detail`, `account-menu`, `form`), unique in the whole view. A tab's id is its route segment; choose it accordingly.
- Elements that only arrange (`<stack>`, `<inset>`, `<full-page>`) need no id: a fragment reaches what is inside with `config:ref`, not by the path.
- Texts, buttons and other leaves stay anonymous unless a fragment is to place something before or after them.
- Document the ids a fragment uses in the view's header comment, so a contributor finds the way without reading the merge rules. Collapsible sections and tabs share their keyed `id` (see [accordion](doc:view-layer/layout#collapsible-sections-accordion)).
