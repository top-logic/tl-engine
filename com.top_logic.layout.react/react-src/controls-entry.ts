// Control registration for the tl-react-controls bundle.
//
// IMPORTANT: All control components MUST import React from 'tl-react-bridge' (not 'react').
// The controls bundle externalizes 'tl-react-bridge' so it shares the single React instance
// provided by the bridge.  Importing from 'react' directly bundles a duplicate React copy,
// which breaks hooks at runtime ("useState is null").
//
// Use:   import { React, useTLState, useTLCommand } from 'tl-react-bridge';
//        React.useState(...)   React.useRef(...)   React.useCallback(...)
import { register } from 'tl-react-bridge';

import TLTextInput from './controls/TLTextInput';
import TLNumberInput from './controls/TLNumberInput';
import TLDatePicker from './controls/TLDatePicker';
import TLSelect from './controls/TLSelect';
import TLCheckbox from './controls/TLCheckbox';
import TLTable from './controls/TLTable';
import TLButton from './controls/TLButton';
import TLToggleButton from './controls/TLToggleButton';
import TLCounter from './controls/TLCounter';
import TLTabBar from './controls/TLTabBar';
import TLFieldList from './controls/TLFieldList';
import TLAudioRecorder from './controls/TLAudioRecorder';
import TLAudioPlayer from './controls/TLAudioPlayer';
import TLFileUpload from './controls/TLFileUpload';
import TLDownload from './controls/TLDownload';
import TLPhotoCapture from './controls/TLPhotoCapture';
import TLPhotoViewer from './controls/TLPhotoViewer';
import TLSplitPanel from './controls/TLSplitPanel';
import TLPanel from './controls/TLPanel';
import TLMaximizeRoot from './controls/TLMaximizeRoot';
import TLDeckPane from './controls/TLDeckPane';
import TLSidebar from './controls/TLSidebar';
import TLStack from './controls/TLStack';
import TLGrid from './controls/TLGrid';
import TLCard from './controls/TLCard';
import TLAppBar from './controls/TLAppBar';
import TLBreadcrumb from './controls/TLBreadcrumb';
import TLBottomBar from './controls/TLBottomBar';
import TLDialog from './controls/TLDialog';
import TLDialogManager from './controls/TLDialogManager';
import TLWindow from './controls/TLWindow';
import TLDrawer from './controls/TLDrawer';
import TLContextMenuRegion from './controls/TLContextMenuRegion';
import TLSnackbar from './controls/TLSnackbar';
import TLMenu from './controls/TLMenu';
import TLAppShell from './controls/TLAppShell';
import TLText from './controls/TLText';
import TLTableView from './controls/TLTableView';
import TLFormLayout from './controls/TLFormLayout';
import TLFormGroup from './controls/TLFormGroup';
import TLFormField from './controls/TLFormField';
import TLResourceCell from './controls/TLResourceCell';
import TLTreeView from './controls/TLTreeView';
import TLDropdownSelect from './controls/TLDropdownSelect';
import TLColorInput from './controls/TLColorInput';
import TLIconSelect from './controls/TLIconSelect';
import TLDashboard from './controls/TLDashboard';


register('TLButton', TLButton);
register('TLToggleButton', TLToggleButton);
register('TLTextInput', TLTextInput);
register('TLNumberInput', TLNumberInput);
register('TLDatePicker', TLDatePicker);
register('TLSelect', TLSelect);
register('TLCheckbox', TLCheckbox);
register('TLTable', TLTable);
register('TLCounter', TLCounter);
register('TLTabBar', TLTabBar);
register('TLFieldList', TLFieldList);
register('TLAudioRecorder', TLAudioRecorder);
register('TLAudioPlayer', TLAudioPlayer);
register('TLFileUpload', TLFileUpload);
register('TLDownload', TLDownload);
register('TLPhotoCapture', TLPhotoCapture);
register('TLPhotoViewer', TLPhotoViewer);
register('TLSplitPanel', TLSplitPanel);
register('TLPanel', TLPanel);
register('TLMaximizeRoot', TLMaximizeRoot);
register('TLDeckPane', TLDeckPane);
register('TLSidebar', TLSidebar);
register('TLStack', TLStack);
register('TLGrid', TLGrid);
register('TLCard', TLCard);
register('TLAppBar', TLAppBar);
register('TLBreadcrumb', TLBreadcrumb);
register('TLBottomBar', TLBottomBar);
register('TLDialog', TLDialog);
register('TLDialogManager', TLDialogManager);
register('TLWindow', TLWindow);
register('TLDrawer', TLDrawer);
register('TLContextMenuRegion', TLContextMenuRegion);
register('TLSnackbar', TLSnackbar);
register('TLMenu', TLMenu);
register('TLAppShell', TLAppShell);
register('TLText', TLText);
register('TLTableView', TLTableView);
register('TLFormLayout', TLFormLayout);
register('TLFormGroup', TLFormGroup);
register('TLFormField', TLFormField);
register('TLResourceCell', TLResourceCell);
register('TLTreeView', TLTreeView);
register('TLDropdownSelect', TLDropdownSelect);
register('TLColorInput', TLColorInput);
register('TLIconSelect', TLIconSelect);
register('TLDashboard', TLDashboard);
