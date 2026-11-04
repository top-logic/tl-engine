// react-src/controls/TLBpmnEditor.tsx
import {
  React,
  TLControlContext,
  rootClassName,
  useFill,
  useTLCommand,
  useTLFieldValue,
  useTLState,
  useTokenSwitch,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import Modeler from 'bpmn-js/lib/Modeler';
import NavigatedViewer from 'bpmn-js/lib/NavigatedViewer';
import OutlineModule from 'bpmn-js/lib/features/outline';

/** State key of the BPMN XML of the diagram to show, see `BpmnEditorControl.XML`. */
const XML = 'xml';

/** State key of the number counting the diagrams the server asked to show, see `BpmnEditorControl.REVISION`. */
const REVISION = 'revision';

/** State key of the switch between modeler and viewer, see `BpmnEditorControl.EDITABLE`. */
const EDITABLE = 'editable';

/** State key of the BPMN ID of the selected element, see `BpmnEditorControl.SELECTED`. */
const SELECTED = 'selected';

/** Command reporting the element the user clicked, see `BpmnEditorControl.CMD_SELECT`. */
const CMD_SELECT = 'select';

/** Argument of {@link CMD_SELECT}: the BPMN ID of the clicked element, see `BpmnEditorControl.ID`. */
const ID = 'id';

/**
 * Time a change of the diagram is held back before its XML is sent: a sequence of edits made in quick succession is
 * sent once. An action command sends a held back diagram before it is executed itself.
 */
const DIAGRAM_DEBOUNCE_MS = 500;

/**
 * Priority of the click handler: below the selection behavior of diagram-js, which toggles a selected element off when
 * it is clicked again. The application keeps the clicked element selected, so the canvas does the same.
 */
const CLICK_PRIORITY = 500;

/** The trigger of the `commandStack.changed` event fired when an import empties the command stack. */
const TRIGGER_CLEAR = 'clear';

/** The colors the diagram is drawn with, read from the theme tokens. */
interface DiagramColors {
  fill: string;
  stroke: string;
}

function readColors(element: Element): DiagramColors {
  const style = getComputedStyle(element);
  return {
    fill: style.getPropertyValue('--tl-surface-layer').trim(),
    stroke: style.getPropertyValue('--tl-text-primary').trim(),
  };
}

function sameColors(a: DiagramColors, b: DiagramColors): boolean {
  return a.fill === b.fill && a.stroke === b.stroke;
}

/** The ID of the element of the diagram that is drawn as the canvas (the collaboration or the process). */
function rootId(diagram: any): string | null {
  const root = diagram.get('canvas').getRootElement();
  return root?.businessObject?.id ?? null;
}

/**
 * Shows the diagram's element with the given BPMN ID as the only selected one; the canvas itself (the root element)
 * and an ID without element show no selection.
 */
function showSelection(diagram: any, id: string | null): void {
  const selection = diagram.get('selection');
  const canvas = diagram.get('canvas');
  const element = id == null ? null : diagram.get('elementRegistry').get(id);
  if (element == null || element === canvas.getRootElement()) {
    selection.select(null);
    return;
  }
  if (!selection.isSelected(element) || selection.get().length !== 1) {
    selection.select(element);
  }
}

/**
 * A BPMN diagram drawn with bpmn-js, see `BpmnEditorControl`.
 *
 * The server sends the diagram as BPMN XML ({@link XML}) and asks for it to be drawn anew by raising
 * {@link REVISION}. With {@link EDITABLE}, the diagram is shown in the bpmn-js modeler, and every change is sent back
 * as the XML of the whole diagram (debounced, as the value of a field). A click on an element reports its BPMN ID
 * ({@link CMD_SELECT}); the element named by {@link SELECTED} is shown selected.
 */
const TLBpmnEditor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const ctx = React.useContext(TLControlContext);
  const [, setDiagramXml] = useTLFieldValue({ debounceMs: DIAGRAM_DEBOUNCE_MS });

  const xml = (state[XML] as string | null | undefined) ?? null;
  const revision = (state[REVISION] as number | undefined) ?? 0;
  const editable = state[EDITABLE] === true;
  const selected = (state[SELECTED] as string | null | undefined) ?? null;

  const fillClass = useFill(true);
  const canvasRef = React.useRef<HTMLDivElement | null>(null);
  const diagramRef = React.useRef<any>(null);

  const colors = useTokenSwitch<DiagramColors>(
    () => readColors(canvasRef.current ?? document.documentElement),
    () => readColors(document.documentElement),
    sameColors,
  );

  // The latest values for the event handlers of the bpmn-js instance, which outlives a render.
  const selectedRef = React.useRef(selected);
  selectedRef.current = selected;
  const setDiagramXmlRef = React.useRef(setDiagramXml);
  setDiagramXmlRef.current = setDiagramXml;
  const sendCommandRef = React.useRef(sendCommand);
  sendCommandRef.current = sendCommand;

  // Whether the instance currently shows the diagram the state asks for (import is asynchronous).
  const [imported, setImported] = React.useState<{ diagram: any, revision: number } | null>(null);

  // The bpmn-js instance: a modeler or a viewer, drawn in the colors of the theme.
  React.useEffect(() => {
    const container = canvasRef.current;
    if (container == null) {
      return undefined;
    }
    const options = {
      container,
      bpmnRenderer: {
        defaultFillColor: colors.fill || undefined,
        defaultStrokeColor: colors.stroke || undefined,
        defaultLabelColor: colors.stroke || undefined,
      },
    };
    // The viewer tracks the selection but draws no outline for it without the outline module.
    const diagram: any = editable
      ? new Modeler(options)
      : new NavigatedViewer({ ...options, additionalModules: [OutlineModule] });
    diagramRef.current = diagram;

    const eventBus = diagram.get('eventBus');
    eventBus.on('element.click', CLICK_PRIORITY, (event: any) => {
      const element = event.element;
      const selection = diagram.get('selection');
      if (element !== diagram.get('canvas').getRootElement() && !selection.isSelected(element)) {
        selection.select(element);
      }
      const id: string | null = element?.businessObject?.id ?? element?.id ?? null;
      if (id === selectedRef.current) {
        return;
      }
      // Shown at once; the server answers with the selection it accepted.
      ctx?.store.applyPatch({ [SELECTED]: id });
      void sendCommandRef.current(CMD_SELECT, { [ID]: id });
    });
    if (editable) {
      eventBus.on('commandStack.changed', (event: any) => {
        if (event?.trigger === TRIGGER_CLEAR) {
          return;
        }
        diagram.saveXML({ format: false })
          .then((result: { xml?: string }) => {
            if (diagramRef.current === diagram && result.xml != null) {
              setDiagramXmlRef.current(result.xml);
            }
          })
          .catch((err: unknown) => console.error('Cannot serialize BPMN diagram.', err));
      });
    }

    return () => {
      if (diagramRef.current === diagram) {
        diagramRef.current = null;
      }
      setImported(null);
      diagram.destroy();
    };
  }, [editable, colors]);

  // Draws the diagram the state asks for. A diagram drawn anew with the same root keeps the section of the canvas
  // shown, any other one is fitted into the canvas.
  React.useEffect(() => {
    const diagram = diagramRef.current;
    if (diagram == null) {
      return undefined;
    }
    let cancelled = false;
    const canvas = diagram.get('canvas');
    const previousRoot = imported?.diagram === diagram ? rootId(diagram) : null;
    const viewbox = previousRoot != null ? canvas.viewbox() : null;
    if (xml == null) {
      diagram.clear();
      setImported({ diagram, revision });
      return undefined;
    }
    diagram.importXML(xml)
      .then(() => {
        if (cancelled || diagramRef.current !== diagram) {
          return;
        }
        if (viewbox != null && rootId(diagram) === previousRoot) {
          canvas.viewbox(viewbox);
        } else {
          canvas.zoom('fit-viewport', 'auto');
        }
        showSelection(diagram, selectedRef.current);
        setImported({ diagram, revision });
      })
      .catch((err: unknown) => console.error('Cannot import BPMN diagram.', err));
    return () => {
      cancelled = true;
    };
    // `imported` is read for the section shown before, it does not trigger a new drawing.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [xml, revision, editable, colors]);

  // Shows the selection the server asks for.
  React.useEffect(() => {
    const diagram = diagramRef.current;
    if (diagram == null || imported?.diagram !== diagram) {
      return;
    }
    showSelection(diagram, selected);
  }, [selected, imported]);

  return (
    <div id={controlId} className={rootClassName(state, 'tlBpmnEditor', !editable && 'tlBpmnEditor--readOnly', fillClass)}>
      <div ref={canvasRef} className="tlBpmnEditor__canvas" />
    </div>
  );
};

export default TLBpmnEditor;
