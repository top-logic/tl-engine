---
description: Read before changing the object tools updateObject / createObject / deleteObject (ObjectEditTool, group data.write) or reasoning about what they check - read check through TidResolver, canWrite per attribute, canCreate for every created part, canDelete over the composition subtree and DeleteChecker, model constraints, isAllowedInitial for initial values, and the refusal to run in a system context.
order: 32
---

# Object tools

`updateObject`, `createObject` and `deleteObject` (`ObjectEditTool`, group `data.write`, mode `DATA_WRITE`) are the write path for one object the agent knows by its id. They are core tools of the entry-point agent, so they reach exactly the accounts whose roles hold `data.write` on the security root (L1), and the approval prompt of a `DATA_WRITE` tool comes on top. What they check below L1:

| Question | Where it is asked |
|---|---|
| May the account read the object it names? | `TidResolver` - an object it may not read is "not found", like one that does not exist; also every object a reference value names |
| May it change this object, and **each** attribute in the call? | `canWrite(obj)` and `canWrite(obj, attr)` for every attribute, all before the first write; the refusal names the withheld attributes |
| May it create this type here? | `canCreate(type, container)`, and `canCreate(container, composition)` when the object is created inside another one - for **every** object a `createObject` call creates, the parts given in its `values` included |
| May it delete this object **and everything it contains**? | `canDelete` for the object and, by type, for every object of its composition subtree; then the engine's own `DeleteChecker` veto |
| Do the values satisfy the model? | the checks `ModelService` vetoes a commit with (mandatory, `TLSize`, `TLRange`, error-level `TLConstraints`), asked on the changed object - for a new object with its parts once the whole tree exists, so a mandatory composition can be satisfied; a violation anywhere undoes the call's whole write |
| What does the answer show? | only attributes the account may read; a written attribute it may not read is reported as written, without its value |

**Every refusal happens before anything is written** (or after the call undid its own write), so it is reported as a failed tool call that the turn does not roll back - other permitted changes of the same turn survive. The [pre-commit guard](doc:ai/security/pre-commit-guard) still asks the same questions for whatever the turn changed.

**Never in a system context.** `ModelAccessRights` grants every right when the thread runs in a system context (`ThreadContext.isSystemContext()`), whatever account the run names, and the knowledge base records the context's id as the author of the commit. So the tools refuse to run there instead of writing something nobody checked and nobody authored. In a chat the tools run in the user's sub-session, and the revision names the user (`SecDemoObjectEditTest` asserts the author). A scheduled `AgentTask` binds its configured account to its sub-session, so it acts - and is recorded - as that account, bounded by the `<background-allowlist>`.

## Engine facts the tools inherit

- **A new object's attributes are checked as the create dialogs check them.** `isAllowed` allows every operation on an object that is not committed yet, because the roles that decide it are derived at commit time. The create dialogs of the React UI ask `ModelAccessRights.isAllowedInitial` instead, and so does `createObject`, per attribute it sets, once the object stands in its container (`ModelSecurity.canSetInitial`): a `<part>` Write grant restricts an initial value where the type has an access parent, decided on the committed object that parent leads to; for a type that decides by its own roles, the right to create the object covers its initial values, because those roles exist only once the object does. The agent never has more rights than the user has in the UI.
- **A part of a specialized type is asked as that type.** The composition check (`ModelSecurity.canCreate(parent, composition, type)`, engine `isAllowedCreate` with a type) asks CREATE for the type actually created, not for the composition's declared target type.
- **A part created with its container is checked by the container's creation.** For a part whose container is created by the same call, the engine answers the create question with yes: a context built in the current transaction has no computed roles yet, and its own creation was asked (`SecurityConfigurationService.isAllowedCreate`). Since the calls of a turn share one transaction, creating the tree in one call changes the number of approvals, not the answer.
- **Contained objects take their rights from their container** unless the application configures an access parent or a role source for them: deleting a session of an event is writing the event. A `deleteObject` that also removes contained objects is therefore within the rights of an account that may write the container, exactly as the delete command of the UI is.
