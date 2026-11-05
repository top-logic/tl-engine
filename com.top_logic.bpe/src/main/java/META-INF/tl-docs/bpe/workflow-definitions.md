---
description: Read before creating, importing, updating, copying, exporting, releasing or revoking workflow definitions (tl.bpe.bpml:Collaboration) from Java (BPMLDefinitions, StateTransition) or TL-Script (bpmlCreate, bpmlImport, bpmlUpdate, bpmlUpdateFromDiagram, bpmlCopy, bpmlExport, bpmlRelease, bpmlRevoke, bpmlCanRelease, bpmlCanRevoke, bpmlCanDelete).
order: 10
---

# Administrating workflow definitions

A workflow definition is a `tl.bpe.bpml:Collaboration` with its participants, processes, lanes, nodes and edges. `BPMLDefinitions` (`com.top_logic.bpe.bpml`) offers the operations of a workflow administration; the classic modeler's commands and the TL-Script functions of `BPMLFunctions` delegate to it.

## Transactions

No operation opens a transaction: an operation modifying the knowledge base runs in the transaction of its caller. A classic command handler opens one itself, a `.view.xml` calls the TL-Script functions inside a `<with-transaction>` of its action chain.

## Operations

| Java (`BPMLDefinitions`) | TL-Script | Effect |
|---|---|---|
| `createCollaboration(name)` | `bpmlCreate(name)` | new workflow from the default diagram (`default-diagram.bpmn.xml`: start event, two manual tasks, end event); the collaboration and its participants get the name |
| `importCollaboration(data)` | `bpmlImport(data)` | new workflow from a BPMN file; a workflow without a name is named after the file |
| `updateCollaboration(c, data, updateExtensions)` | `bpmlUpdate(c, data, updateExtensions: false)` | makes the workflow structurally equal to the file; elements keep their identity by their BPMN ID (`extId`); the application specific extensions (forms, actors, ...) are taken from the file only with `updateExtensions` |
| `updateFromDiagram(c, xml)` | `bpmlUpdateFromDiagram(c, xml)` | the same for the XML a bpmn-js editor serialized, keeping the extensions; adds the diagram namespace declarations the bpmn-js serializer may omit (`fixDiagramNamespaces`) |
| `copyCollaboration(c, name)` | `bpmlCopy(c, name)` | independent deep copy in state `Development` |
| `export(c, pretty)`, `exportXml(c, out, pretty)` | `bpmlExport(c, pretty: true)` | BPMN XML with extensions, as `BinaryData` named `<name>.bpml` (for a `<download>`) or written to a stream |
| `canDelete(c)`, `isInUse(c)` | `bpmlCanDelete(c)` | whether the workflow may be deleted: no process execution, running or finished, refers to it or to one of its processes; a workflow that was used is revoked instead |
| `release(c)`, `canRelease(c)` | `bpmlRelease(c)`, `bpmlCanRelease(c)` | `Development` or `Retired` to `Released` |
| `revoke(c)`, `canRevoke(c)` | `bpmlRevoke(c)`, `bpmlCanRevoke(c)` | `Released` to `Retired` |

The modifying TL-Script functions result in the workflow they created or changed. An invalid BPMN file and a refused state transition fail with a `TopLogicException` the user can read.

## State transitions

The approval state is the attribute `state` of type `tl.bpe.bpml:ApprovalState`. `releaseTransition()` and `revokeTransition()` describe the two transitions as `StateTransition` (`com.top_logic.bpe.util`): a type, an enumeration attribute, the allowed source states and the target state. `isApplicable(obj)` tests a transition, `apply(obj)` performs it or refuses it. The classic `StateBasedHandler` of the modeler builds the same `StateTransition` from its configuration.

All workflows are ``all(`tl.bpe.bpml:Collaboration`)``; no function is needed for that.
