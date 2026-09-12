import { React, useTLState } from 'tl-react-bridge';

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
 */
function TLFlowDiagram({ controlId, state }: TLFlowDiagramProps) {
    const ref = React.useRef<HTMLDivElement>(null);
    const controlRef = React.useRef<any>(null);

    const diagramJson = typeof state.diagram === 'string'
        ? state.diagram
        : JSON.stringify(state.diagram);

    React.useEffect(() => {
        const div = ref.current;
        if (!div) return;

        let cancelled = false;
        let retry: number | undefined;
        let waited = 0;

        const mount = () => {
            if (cancelled) return;

            const gwtApi = (window as any).GWT_FlowDiagram;
            if (!gwtApi) {
                // The GWT module registers itself asynchronously, and a freshly loaded page renders
                // this component before that has happened. Waiting for it is the whole point: giving
                // up here left the diagram blank for good, because this effect only re-runs when the
                // control or its diagram changes - which, on a page showing what it already showed,
                // never happens. It came back only when something unrelated moved the model.
                if (waited >= MOUNT_TIMEOUT_MS) {
                    console.error('[TLFlowDiagram] GWT_FlowDiagram not loaded');
                    return;
                }
                waited += MOUNT_RETRY_MS;
                retry = window.setTimeout(mount, MOUNT_RETRY_MS);
                return;
            }

            const windowName = document.body.dataset.windowName || 'main';
            const contextPath = document.body.dataset.contextPath || '';

            controlRef.current = gwtApi.mount(
                div, controlId, windowName, contextPath, diagramJson
            );
        };
        mount();

        return () => {
            cancelled = true;
            if (retry !== undefined) {
                window.clearTimeout(retry);
            }
            if (controlRef.current) {
                controlRef.current.destroy();
                controlRef.current = null;
            }
        };
    }, [controlId, diagramJson]);

    return <div ref={ref} style={{ width: '100%', height: '100%' }} />;
}

export default TLFlowDiagram;
