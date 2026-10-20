---
description: Read before extending a view of another module with a same-path overlay (WEB-INF/views/<same path>.view.xml) - adding, repositioning, replacing or removing one element of a nested list by id, config:position/config:reference, config:override, config:operation, config:index, anonymous elements, and which ids an extensible view gives its elements.
order: 85
---

# Extending a view with a same-path overlay

## Same-path overlays

A module extends a view of a module it depends on without copying it: it ships a view of the **same path** in its own `src/main/webapp` (`WEB-INF/views/user-menu.view.xml` beside the one of `tl-layout-view`, say). The `ViewLoader` reads every copy of the path the `FileManager` finds and folds them in dependency order: the copy of the lowest module is the base, each copy of a depending module updates what the copies before it produced. The base knows nothing of its contributors; a fragment is in effect where its module is deployed and absent where it is not.

- In a development workspace the fold happens when the view is loaded (and again when any of its copies changes). A deployed WAR has only one file per path, so the application build pre-computes the fold (`ViewOverlayMerger`) and writes the merged view into the web application; both use the same merge.
- The lowest copy must be a real base. A copy that uses `config:operation` is a fragment, and a fragment found at the bottom of the stack is rejected ("No base view found ... the lowest-priority copy is itself an overlay") - typically the base view was renamed or removed while fragments still target its path.
- The attributes steering the merge are in the namespace `xmlns:config="http://www.top-logic.com/ns/config/6.0"`, declared on the `<view>` of the fragment.

A fragment is the path from the root of the view to the place it changes, written with only what it changes: an element on the way is written with the attribute that identifies it, and nothing else.

## The merge rules

The fold is the merge of typed configuration, applied to the view's configuration tree.

**Values replace, elements merge.** An attribute or a value property written in the fragment (`input`, `active-tab`, the `<title>` of an app bar, the `expr` of a channel) replaces the base's. An element in a single-valued property - the content of the `<view>`, for instance - is merged into the base's element: the fragment's `<tab-bar>` at the root of the view updates the base's tab bar, it does not take its place. `config:override="true"` on such an element replaces it as a whole.

**Lists merge by key.** Every list of a view is keyed, so an entry of the fragment finds the entry it means:

| List | Key |
|------|-----|
| the child lists of all elements (`children`, the content of a `<pane>`, a `<case>`, a `<tab>`, the `<default>` of a switch, the slots of an `<app-bar>`, the tiles of a dashboard, ...) | `id` of the element |
| `<panes>` of a `<split-panel>`, the cases of a `<switch>`, the steps of a `<wizard>` | `id` of the pane, case, step source |
| `<tabs>` of a `<tab-bar>`, the sections of an `<accordion>` | `id` (mandatory) |
| the items of a `<sidebar>` and of its groups | `id` |
| the `<groups>` of a `<menu>` | `id` of the group |
| the `<channels>` of a `<view>` | `name` |
| the slots of an `<app-shell>` (`header`, `notices`, `content`, `footer`) | the kind of the element: at most one of a kind |

An entry of the fragment changes the list as follows:

- **An existing key updates the entry**: its properties are merged with the fragment's, recursively - this is how a fragment descends into a nested list. The fragment repeats nothing of the entry but its key.
- **`config:override="true"`** on that entry replaces it: the base's content of the entry is gone, the fragment's is the whole entry.
- **`config:operation="remove"`** removes the entry.
- **A key the list does not have adds the entry**, at the end, or where `config:position="begin|end|before|after"` places it; `before` and `after` name the neighbor by its key in `config:reference="<id>"`. `config:position` on an updated entry moves it.
- **`config:operation`** states what the entry must do: `addOrUpdate` is the default described above, `update` requires the key to exist, `add` requires it not to exist. An `update` on the elements of the path makes a fragment fail loudly when the base no longer has that path, instead of silently appending a stray copy of the element.

**Anonymous elements.** An element without id is anonymous: it is appended in order, it matches no entry of another copy, and any number of anonymous elements may stand in one list. Most elements of a view are anonymous - a `<text>`, a `<stack>` that only arranges, a `<separator/>` of a sidebar. A fragment adds elements beside them, but cannot address them by key.

**Positional addressing.** `config:index="n"` addresses the entry at the zero-based position `n` of the list as merged so far and updates it like a key would (`config:override` replaces it, `config:position` moves it). It is the way into an anonymous element, and an emergency path: the position is a property of the base's current layout, so the fragment silently changes another element once the base inserts or reorders entries before it. A view that is meant to be extended gives the element an id instead. A position out of range and a combination with `add` or `remove` are errors.

**Errors.** The merge fails when the fragment updates or removes a key the list does not have, writes `update`/`remove` or a `config:reference` without key, references a key the list does not have, or one copy writes the same id twice in one list.

## A worked example: a tab in a nested tab bar

A base module shows devices in a master-detail page whose detail switches on the kind of the selection:

```xml
<!-- WEB-INF/views/devices.view.xml of the base module -->
<view>
    <channels>
        <derived-channel name="devices" expr="all(`devices:Device`)"/>
        <channel name="selection"/>
    </channels>
    <split-panel id="main">
        <panes>
            <pane id="list">
                <table input="devices" selection="selection"/>
            </pane>
            <pane id="detail">
                <switch id="kind" input="selection">
                    <case id="device" test="x -> $x.instanceOf(`devices:Device`)">
                        <tab-bar id="device-tabs" active-tab="general">
                            <tabs>
                                <tab id="general">
                                    <label><en>General</en></label>
                                    <form input="selection"/>
                                </tab>
                            </tabs>
                        </tab-bar>
                    </case>
                    <default>
                        <text><label><en>Nothing selected</en></label></text>
                    </default>
                </switch>
            </pane>
        </panes>
    </split-panel>
</view>
```

A module for virtual devices adds a tab to the device case. Its copy of the path names every element on the way by its id and writes the new tab:

```xml
<!-- WEB-INF/views/devices.view.xml of the module for virtual devices -->
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

The merged view has one split panel, one detail pane, one switch with the case `device` and its default, and a tab bar with the tabs `general` and `virtual-device`. The `input` of the switch and the content of the `general` tab come from the base, because the fragment does not write them. `TestViewOverlayKeyedLists` pins this behavior, including the positional path into an anonymous element.

## The engine's example: an entry of the user menu

`tl-layout-view` ships the account area of the app bar as `WEB-INF/views/user-menu.view.xml`: a `<switch id="session">` that offers a Login button in its case `anonymous` and the `<menu id="account">` in its `<default>`. The `<groups>` of a menu are keyed by `id` (see [commands](doc:view-layer/commands)); the menu ships the group `account`. The overlay of `com.top_logic.demo.react` (`WEB-INF/views/user-menu.view.xml` of that module) contributes a group `demo` with an "About this demo" entry:

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <switch id="session">
        <default>
            <menu id="account" config:operation="update">
                <groups>
                    <group id="demo" config:position="after" config:reference="account">
                        <generic-command image="css:bi bi-info-circle">
                            <label><en>About this demo</en><de>Über diese Demo</de></label>
                            <notify display="dialog" expr="x -> #('...'@en, '...'@de)"/>
                        </generic-command>
                    </group>
                </groups>
            </menu>
        </default>
    </switch>
</view>
```

The same overlay redefines the channel `accountName` of the base: its `<derived-channel name="accountName">` matches the base's channel by name and replaces its expression. A group with the id `account` instead of `demo` would add its commands to the base's group; the commands inside a group are not keyed, so they are appended.

Further shipped views written for overlays: the shell `app.view.xml` (see [the application shell](doc:view-layer/app-shell#the-application-shell-extending-appviewxml)), whose trailing app-bar elements are the view references `dev-menu` and `user-menu`; `dev-menu.view.xml` with the groups `page`, `automation` and `reference`; `settings-tabs.view.xml` with the tabs of the settings dialog; and `admin/admin.view.xml`, whose sections each hold a tab bar with the id `views`, so a fragment adds a section to the outer tabs or a view inside a section (`<tab id="system"><tab-bar id="views"><tabs><tab id="...">`).

## Writing a view others extend

Give ids to the elements on the path an extension needs and to the elements a fragment positions against; leave the rest anonymous.

- An element whose content a module plausibly extends - a switch and its cases, the panes of a split panel, a menu, a tab bar, a stack holding a list of extension-worthy entries - gets a short id that names its role (`session`, `detail`, `account`), unique among its siblings. Ids of different lists may repeat (`views` in every admin section).
- A purely arranging element (`<stack>`, `<inset>`) on the path needs an id too, otherwise a fragment reaches what is inside only through `config:index`.
- Texts, buttons and other leaves stay anonymous unless a fragment is to place something before or after them.
- Document the path in the view's header comment, and name the ids a fragment uses, so a contributor finds the way without reading the merge rules. Collapsible sections and tabs share their keyed `id` (see [accordion](doc:view-layer/layout#collapsible-sections-accordion)).
