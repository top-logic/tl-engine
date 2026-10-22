# tl-ai-views - Living Views

Agents build and change the views (`.view.xml`, the React view layer) of the running application,
and see the page the user has open.

## What it does

- **Check before write** (`ViewCheck`): the engine reads the view with every other module's overlay,
  instantiates it (TL-Script compiles against the model) and renders it once headlessly with the real
  data. Nothing is written while a finding remains; each names its line.
- **Edits that keep the file** (`source/ViewEdit`): set an attribute, replace, insert or remove one
  element, addressed by its path (`/view/split-panel/panes/pane[2]/form`, `nav-item[id=events]`).
  Every other character - comments, indentation - stays.
- **Change sets** (`ViewChanges`, model `tl.ai.views`): every write is recorded with the text of each
  view before and after, committed with the file. `revertViewChange` puts the newest (or a named)
  change back; a view changed since is refused as a conflict.
- **The open page** (`WindowPage`): the engine's headless projection, compact, with the view file of
  each part; after a write the changed parts rebuild and the user stays on their navigation item and
  tab. A change of the start page waits until the run has ended.

## Use it

Depend on `tl-ai-views` and give an agent the skill:

```xml
<config service-class="com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions">
	<instance>
		<agents>
			<agent name="TL_SingleAgent">
				<skills>
					<skill name="TL_buildViews"/>
				</skills>
			</agent>
		</agents>
	</instance>
</config>
```

The module brings the model module `tl.ai.views`, so an existing database needs a fresh start or a
model migration. In tl-ai-demo, switch the skill on with `views-demo.config.xml` in its
`metaConf.txt`.

Writes go to the application's workspace (`FileManager.getIDEFile`), as the engine's view designer
writes them; a view that only a module jar holds is read-only. The window is reached through
`RunWindow` (tl-ai-core), which tl-chat installs.

## Tools

`views.*`: `viewElements`, `listViews`, `readView` (read); `writeView`, `editView`, `deleteView`,
`revertViewChange` (structural, approved). `view-window.whereAmI` (read).
