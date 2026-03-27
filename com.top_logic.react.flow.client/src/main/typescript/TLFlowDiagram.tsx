import { React, useTLState } from 'tl-react-bridge';

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

    React.useEffect(() => {
        const div = ref.current;
        if (!div) return;

        const gwtApi = (window as any).GWT_FlowDiagram;
        if (!gwtApi) {
            console.error('[TLFlowDiagram] GWT_FlowDiagram not loaded');
            return;
        }

        const windowName = document.body.dataset.windowName || 'main';
        const contextPath = document.body.dataset.contextPath || '';
        const diagramJson = typeof state.diagram === 'string'
            ? state.diagram
            : JSON.stringify(state.diagram);

        controlRef.current = gwtApi.mount(
            div, controlId, windowName, contextPath, diagramJson
        );

        return () => {
            if (controlRef.current) {
                controlRef.current.destroy();
                controlRef.current = null;
            }
        };
    }, [controlId]);

    return <div ref={ref} style={{ width: '100%', height: '100%' }} />;
}

export default TLFlowDiagram;
