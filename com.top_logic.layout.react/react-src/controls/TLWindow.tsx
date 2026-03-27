import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef, useState } = React;

const I18N_KEYS = {
  'js.window.close': 'Close',
};

type ResizeDir = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw';

const RESIZE_HANDLES: ResizeDir[] = ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw'];

/**
 * Window chrome: title bar, close button, scrollable body, footer actions, resize handles.
 *
 * State:
 * - title: string
 * - width: string (CSS value, e.g. "500px")
 * - height: string | null
 * - resizable: boolean
 * - child: ChildDescriptor
 * - actions: ChildDescriptor[]
 */
const TLWindow: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const title = (state.title as string) ?? '';
  const serverWidth = (state.width as string) ?? '32rem';
  const serverHeight = (state.height as string | null) ?? null;
  const resizable = state.resizable === true;
  const child = state.child;
  const actions = (state.actions as unknown[]) ?? [];
  const toolbarButtons = (state.toolbarButtons as unknown[]) ?? [];

  // Local dimensions during resize (null = use server values).
  const [localWidth, setLocalWidth] = useState<number | null>(null);
  const [localHeight, setLocalHeight] = useState<number | null>(null);

  // Position state: null = centered by flexbox, set = absolute positioning.
  const [position, setPosition] = useState<{ x: number; y: number } | null>(null);
  const positionRef = useRef<{ x: number; y: number } | null>(null);

  // Refs to track latest local dimensions inside event handlers (avoids stale closures).
  const localWidthRef = useRef<number | null>(null);
  const localHeightRef = useRef<number | null>(null);

  const windowRef = useRef<HTMLDivElement>(null);
  const dragState = useRef<{
    dir: ResizeDir;
    startX: number;
    startY: number;
    startW: number;
    startH: number;
  } | null>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleMouseDown = useCallback((dir: ResizeDir, e: React.MouseEvent) => {
    e.preventDefault();
    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    dragState.current = {
      dir,
      startX: e.clientX,
      startY: e.clientY,
      startW: rect.width,
      startH: rect.height,
    };

    const handleMouseMove = (ev: MouseEvent) => {
      const ds = dragState.current;
      if (!ds) return;
      const dx = ev.clientX - ds.startX;
      const dy = ev.clientY - ds.startY;
      let w = ds.startW;
      let h = ds.startH;
      if (ds.dir.includes('e')) w = ds.startW + dx;
      if (ds.dir.includes('w')) w = ds.startW - dx;
      if (ds.dir.includes('s')) h = ds.startH + dy;
      if (ds.dir.includes('n')) h = ds.startH - dy;
      const newW = Math.max(200, w);
      const newH = Math.max(100, h);
      localWidthRef.current = newW;
      localHeightRef.current = newH;
      setLocalWidth(newW);
      setLocalHeight(newH);
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      const lw = localWidthRef.current;
      const lh = localHeightRef.current;
      if (lw != null || lh != null) {
        sendCommand('resize', {
          ...(lw != null ? { width: Math.round(lw) + 'px' } : {}),
          ...(lh != null ? { height: Math.round(lh) + 'px' } : {}),
        });
        localWidthRef.current = null;
        localHeightRef.current = null;
        setLocalWidth(null);
        setLocalHeight(null);
      }
      dragState.current = null;
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, [sendCommand]);

  const handleTitleMouseDown = useCallback((e: React.MouseEvent) => {
    // Only left mouse button; ignore clicks on buttons inside the header.
    if (e.button !== 0 || (e.target as HTMLElement).closest('button')) return;
    e.preventDefault();

    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();

    // If first drag, initialize position from current rendered position.
    const startPos = positionRef.current ?? { x: rect.left, y: rect.top };
    const offsetX = e.clientX - startPos.x;
    const offsetY = e.clientY - startPos.y;

    const handleMouseMove = (ev: MouseEvent) => {
      const viewW = window.innerWidth;
      const viewH = window.innerHeight;
      let newX = ev.clientX - offsetX;
      let newY = ev.clientY - offsetY;

      // Constrain to viewport.
      const elW = el.offsetWidth;
      const elH = el.offsetHeight;
      if (newX + elW > viewW) newX = viewW - elW;
      if (newY + elH > viewH) newY = viewH - elH;
      if (newX < 0) newX = 0;
      if (newY < 0) newY = 0;

      const pos = { x: newX, y: newY };
      positionRef.current = pos;
      setPosition(pos);
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  }, []);

  const style: React.CSSProperties = {
    width: localWidth != null ? localWidth + 'px' : serverWidth,
    ...(localHeight != null
      ? { height: localHeight + 'px' }
      : serverHeight != null
        ? { height: serverHeight }
        : {}),
    maxHeight: position ? '100vh' : '80vh',
    ...(position
      ? { position: 'absolute' as const, left: position.x + 'px', top: position.y + 'px' }
      : {}),
  };

  const titleId = controlId + '-title';

  return (
    <div
      id={controlId}
      className="tlWindow"
      style={style}
      ref={windowRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
    >
      <div className="tlWindow__header" onMouseDown={handleTitleMouseDown}>
        <span className="tlWindow__title" id={titleId}>{title}</span>
        {toolbarButtons.length > 0 && (
          <div className="tlWindow__toolbar">
            {toolbarButtons.map((btn, i) => (
              <span key={i} className="tlWindow__toolbarButton">
                <TLChild control={btn} />
              </span>
            ))}
          </div>
        )}
        <button
          type="button"
          className="tlWindow__closeBtn"
          onClick={handleClose}
          title={i18n['js.window.close']}
        >
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" />
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" />
          </svg>
        </button>
      </div>
      <div className="tlWindow__body">
        <TLChild control={child} />
      </div>
      {actions.length > 0 && (
        <div className="tlWindow__footer">
          {actions.map((action, i) => (
            <TLChild key={i} control={action} />
          ))}
        </div>
      )}
      {resizable && RESIZE_HANDLES.map(dir => (
        <div
          key={dir}
          className={`tlWindow__resizeHandle tlWindow__resizeHandle--${dir}`}
          onMouseDown={(e) => handleMouseDown(dir, e)}
        />
      ))}
    </div>
  );
};

export default TLWindow;
