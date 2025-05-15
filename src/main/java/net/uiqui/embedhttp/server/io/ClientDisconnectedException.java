package net.uiqui.embedhttp.server.io;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class ClientDisconnectedException extends IOException {
    public ClientDisconnectedException() {
        super("Connection closed");
    }

    public ClientDisconnectedException(SocketTimeoutException e) {
        super("Read timeout", e);
    }
}
