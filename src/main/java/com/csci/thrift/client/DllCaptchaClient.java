package com.csci.thrift.client;

import com.csci.thrift.democaptcha.DemoCaptcha;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sai on 2/21/2016.
 */
public class DllCaptchaClient {
    public static void main(String[] args) {
        TTransport transport = new TSocket("127.0.0.1", 9988);
        try {
            System.out.println("Please input file name:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            transport.open();
            TProtocol protocal = new TCompactProtocol(transport);
            TMultiplexedProtocol mp =new TMultiplexedProtocol(protocal, "Hello");
            DemoCaptcha.Client client = new DemoCaptcha.Client(mp);
            System.out.println(client.getDllCaptcha(line.trim()));

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
