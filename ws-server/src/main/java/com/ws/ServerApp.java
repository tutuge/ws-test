package com.ws;

/**
 * Hello world!
 */
public class ServerApp {
    public static void main(String[] args) {
        WebsocketServer websocketServer = new WebsocketServer();
        websocketServer.netty();

    }


}
