import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatComposer } from './chat-composer'
import { TLChatRail } from './chat-rail'
import { TLChatSessionBinding } from './chat-session-binding'
import { TLChatSessions } from './chat-sessions'
import { TLChatThread } from './chat-thread'
import { TLDataList } from './data-list'
import './styles.css'

register('TLAgentChat', App)
register('TLChatSessions', TLChatSessions)
register('TLChatThread', TLChatThread)
register('TLChatComposer', TLChatComposer)
register('TLChatRail', TLChatRail)
register('TLChatSessionBinding', TLChatSessionBinding)
register('TLDataList', TLDataList)
