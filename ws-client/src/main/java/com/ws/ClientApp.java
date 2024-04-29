package com.ws;

import com.ws.thread.ThreadSend;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 */
@Slf4j
public class ClientApp {

    public static void main(String[] args) {
        ThreadSend threadSend = new ThreadSend();
        threadSend.start();
    }


}
