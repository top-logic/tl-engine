import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatComposer } from './chat-composer'
import { TLChatSessions } from './chat-sessions'
import { TLChatThread } from './chat-thread'
import './styles.css'

register('TLAgentChat', App)
register('TLChatSessions', TLChatSessions)
register('TLChatThread', TLChatThread)
register('TLChatComposer', TLChatComposer)
