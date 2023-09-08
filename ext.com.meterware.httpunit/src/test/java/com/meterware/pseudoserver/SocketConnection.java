package com.meterware.pseudoserver;
/********************************************************************************************************************
 * $Id: SocketConnection.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2003, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class SocketConnection {
    private Socket _socket;
    private OutputStream _os;
    private InputStream _is;
    private String _host;

    private boolean _isChunking;


    public SocketConnection( String host, int port ) throws IOException, UnknownHostException {
        _host = host;
        _socket = new Socket( host, port );
        _os = _socket.getOutputStream();
        _is = new BufferedInputStream( _socket.getInputStream() );
    }


    void close() throws IOException {
        _socket.close();
    }

    SocketResponse getResponse( String method, String path ) throws IOException {
        if (_isChunking) throw new IllegalStateException( "May not initiate a new request while chunking." );
        sendHTTPLine( method + ' ' + path + " HTTP/1.1" );
        sendHTTPLine( "Host: " + _host );
        sendHTTPLine( "Connection: Keep-Alive" );
        sendHTTPLine( "" );
        return new SocketResponse( _is );
    }


    SocketResponse getResponse( String method, String path, String body ) throws IOException {
        if (_isChunking) throw new IllegalStateException( "May not initiate a new request while chunking." );
        sendHTTPLine( method + ' ' + path + " HTTP/1.1" );
        sendHTTPLine( "Host: " + _host );
        sendHTTPLine( "Connection: Keep-Alive" );
        sendHTTPLine( "Content-Length: " + body.length() );
        sendHTTPLine( "" );
        _os.write( body.getBytes() );
        return new SocketResponse( _is );
    }


    void startChunkedResponse( String method, String path ) throws IOException {
        if (_isChunking) throw new IllegalStateException( "May not initiate a new request while chunking." );
        sendHTTPLine( method + ' ' + path + " HTTP/1.1" );
        sendHTTPLine( "Host: " + _host );
        sendHTTPLine( "Connection: Keep-Alive" );
        sendHTTPLine( "Transfer-Encoding: chunked" );
        sendHTTPLine( "" );
        _isChunking = true;
    }


    public void sendChunk( String chunk ) throws IOException {
        if (!_isChunking) throw new IllegalStateException( "May not send a chunk when not in mid-request." );
        sendHTTPLine( Integer.toHexString( chunk.length() ));
        sendHTTPLine( chunk );
    }


    SocketResponse getResponse() throws IOException {
        if (!_isChunking) throw new IllegalStateException( "Not chunking a request." );
        _isChunking = false;
        sendHTTPLine( "0" );
        sendHTTPLine( "" );
        return new SocketResponse( _is );
    }


    private void sendHTTPLine( final String line ) throws IOException {
        _os.write( line.getBytes() );
        _os.write( 13 );
        _os.write( 10 );
    }


    class SocketResponse extends ReceivedHttpMessage {

        private String _protocol;
        private int    _responseCode;
        private String _message;

        public SocketResponse( InputStream inputStream ) throws IOException {
            super( inputStream );
        }


        void appendMessageHeader( StringBuffer sb ) {
            sb.append( _protocol ).append( ' ' ).append( _responseCode ).append( ' ' ).append( _message );
        }


        void interpretMessageHeader( String messageHeader ) {
            int s1 = messageHeader.indexOf( ' ' );
            int s2 = messageHeader.indexOf( ' ', s1+1 );

            _protocol = messageHeader.substring( 0, s1 );
            _message  = messageHeader.substring( s2+1 );

            try {
                _responseCode = Integer.parseInt( messageHeader.substring( s1+1, s2 ) );
            } catch (NumberFormatException e) {
                _responseCode = -1;
            }
        }


        public int getResponseCode() {
            return _responseCode;
        }
    }

}
