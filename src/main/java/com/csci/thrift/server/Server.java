package com.csci.thrift.server;

import com.csci.thrift.democaptcha.CaptchaImpl;
import com.csci.thrift.democaptcha.DemoCaptcha;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

/**
 * Created by Sai on 2/21/2016.
 */
public class Server {
    public static final int PORT = 9988;

    public static void main(String[] args) {
        try {
            TServerSocket serverSocket = new TServerSocket(PORT);
            TProtocolFactory protocalFactory = new TCompactProtocol.Factory();
            TMultiplexedProcessor mp = new TMultiplexedProcessor();
            mp.registerProcessor("Hello", new DemoCaptcha.Processor<>(new CaptchaImpl()));

            final TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverSocket).protocolFactory(protocalFactory).processor(mp));

            new Thread(() -> {
                //这个方法会阻塞线程
                server.serve();
            }).start();
            System.out.println("Server started on port " + PORT + "...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
