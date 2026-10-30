import { React, rootClassName, subscribe, unsubscribe } from 'tl-react-bridge';

/** How long to keep waiting for the GWT module to register itself, and how often to look. */
const MOUNT_TIMEOUT_MS = 10000;

const MOUNT_RETRY_MS = 50;

interface TLFlowDiagramProps {
    controlId: string;
    state: Record<string, unknown>;
}

/**
 * Minimal React lifecycle wrapper for the GWT-rendered flow diagram.
 * All diagram logic runs in GWT-compiled Java. This component only
 * manages mount/unmount lifecycle.
 *
 * The GWT control is mounted once per control. It receives the state updates of the control
 * itself, also the serialization replacing the displayed diagram: the patches the server sends
 * after that serialization belong to the new diagram, and only a control applying the updates in
 * the order they were sent gets each patch to the diagram it was made for. The updates arriving
 * while the GWT module is still loading are kept and handed to the control once it is mounted.
 */
function TLFlowDiagram({ controlId, state }: TLFlowDiagramProps) {
    const ref = React.useRef<HTMLDivElement>(null);
    const controlRef = React.useRef<any>(null);

    const diagramJson = typeof state.diagram === 'string'
        ? state.diagram
        : JSON.stringify(state.diagram);

    // Subscribed in a layout effect, so that no update arriving after the render that mounts the
    // control is missed.
    React.useLayoutEffect(() => {
        const div = ref.current;
        if (!div) return;

        // The serialization the control is mounted with: the one of the render mounting it. Later
        // ones reach the control as state updates.
        const mountJson = diagramJson;

        let cancelled = false;
        let retry: number | undefined;
        let waited = 0;

        const pending: Record<string, unknown>[] = [];
        const buffer = (update: Record<string, unknown>) => {
            pending.push(update);
        };
        subscribe(controlId, buffer);

        const mount = () => {
            if (cancelled) return;

            const gwtApi = (window as any).GWT_FlowDiagram;
            if (!gwtApi) {
                // The GWT module registers itself asynchronously, and a freshly loaded page renders
                // this component before that has happened. Waiting for it is the whole point: giving
                // up here left the diagram blank for good, because this effect only re-runs when the
                // control changes.
                if (waited >= MOUNT_TIMEOUT_MS) {
                    unsubscribe(controlId, buffer);
                    console.error('[TLFlowDiagram] GWT_FlowDiagram not loaded');
                    return;
                }
                waited += MOUNT_RETRY_MS;
                retry = window.setTimeout(mount, MOUNT_RETRY_MS);
                return;
            }

            const windowName = document.body.dataset.windowName || 'main';
            const contextPath = document.body.dataset.contextPath || '';

            // The control subscribes to the updates of its own while mounting.
            unsubscribe(controlId, buffer);
            const control = gwtApi.mount(
                div, controlId, windowName, contextPath, mountJson
            );
            controlRef.current = control;
            for (const update of pending) {
                control.onServerState(update);
            }
        };
        mount();

        return () => {
            cancelled = true;
            unsubscribe(controlId, buffer);
            if (retry !== undefined) {
                window.clearTimeout(retry);
            }
            if (controlRef.current) {
                controlRef.current.destroy();
                controlRef.current = null;
            }
        };
    }, [controlId]);

    // A serialization can also reach the component without an update of the control's own, as
    // part of a parent's update rendering the control again. The control ignores the serialization
    // it already displays.
    React.useLayoutEffect(() => {
        if (controlRef.current) {
            controlRef.current.onServerState({ diagram: diagramJson });
        }
    }, [diagramJson]);

    return <div ref={ref} className={rootClassName(state)} style={{ width: '100%', height: '100%' }} />;
}

export default TLFlowDiagram;
