---
description: Read before deciding on which object a view of the classic layout UI (a tab, a layout, a dialog) checks access - the securityObject of a layout or component (default security root), the single-child rule of BoundLayout (a layout with one child delegates the check to it), isSecurityMaster on a component or layout, and checking the tab of a module against the security root with a same-path overlay that calls com.top_logic/tab.template.xml.
order: 24
---

# Access checks of layouts

Which object a view of the classic layout UI checks the access of the user on, and how a tab
contributed by a module is checked against the security root. Which roles a user holds on that
object is described in [the access configuration article](doc:access-configuration#role-sources-which-roles-a-user-holds-on-an-object).

## The security object of a view

A component or layout is shown only if the user holds, on its **security object**, a role that the
view's role profile grants for the view's default command group (usually `Read`). The security
object is chosen by the attribute `securityObject` (a `SecurityObjectProvider`):

| Value | Security object |
|---|---|
| `securityRoot` (default) | the security root |
| `model`, `model(my.module:MyType, ...)` | the model of the component |
| `modelOrSecRoot` | the model, or the security root without a model |
| `path:...`, `ref:...`, a model specification | the object the path, the referenced provider or the model specification yields |

A layout (`BoundLayout`, e.g. the `securityLayout` of a tab) decides with the following rules,
in this order:

1. **Security master of the layout itself.** With `isSecurityMaster="true"`, the layout checks on
   its own security object (by default the security root). A security master from its inside is
   ignored and the layout reports itself as security master to its parent.
2. **Inner security master.** A component or layout inside the layout marked with
   `isSecurityMaster="true"` decides for the whole layout: if the user may not see it, the
   layout is hidden as well.
3. **Single child.** A layout with exactly one child delegates the check to that child, unless
   the layout configures a `securityObject` other than the security root.
4. Otherwise the layout checks on its own security object.

Because the security root is the default, rule 3 cannot be switched off by
`securityObject="securityRoot"`: a layout with this setting looks exactly like one without, and
still delegates to its single child. To check a layout with one child against the security root,
mark it with `isSecurityMaster="true"`. A `securityObject` other than the security root is used
without the mark.

## Checking the tab of a module against the security root

A tab of a module created with the template `com.top_logic/tab.template.xml` usually has exactly
one child (the content of the tab), so by rule 3 the tab is visible whenever the content is. To make
the visibility of the tab depend on the roles of the user on the security root instead, the
application overlays the module's layout file: a file with the same path in the application's
`WEB-INF/layouts` replaces the module's file. The overlay calls the tab template with the
arguments of the original and adds `isSecurityMaster="true"`.

The module `my.module` contributes the tab `my.module/myTab.layout.xml`:

```xml
<config:template-call
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	template="com.top_logic/tab.template.xml"
>
	<arguments
		helpID="ID_..."
		tabLabel="my.module.myTab"
	>
		<components>
			<layout-reference resource="my.module/myContent.layout.xml"/>
		</components>
	</arguments>
</config:template-call>
```

The application adds `WEB-INF/layouts/my.module/myTab.layout.xml` with the same content and the
mark:

```xml
<config:template-call
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
	template="com.top_logic/tab.template.xml"
>
	<arguments
		helpID="ID_..."
		isSecurityMaster="true"
		tabLabel="my.module.myTab"
	>
		<components>
			<layout-reference resource="my.module/myContent.layout.xml"/>
		</components>
	</arguments>
</config:template-call>
```

The tab is now checked on the security root; its role profile decides which roles held there show
it. To check it on another object, add `securityObject` (e.g. `securityObject="model"`) to the
same arguments. Keep `helpID` and `tabLabel` of the original, so that help pages and translations
stay attached to the tab.

A real case is the tab of the AI assistant in `tl-chat-classic`
(`tl-chat/chat/tabLayout.layout.xml`): its only child is the chat, so without the mark the tab is
shown to every user who may see the chat.

The same parameters are offered in the layout editor: in design mode, *Edit view* on a tab created
from the tab template offers its security object (`securityObject`) and whether it is security
master (`isSecurityMaster`).
