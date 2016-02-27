package com.csci.thrift.client;

import com.csci.thrift.democaptcha.DemoCaptcha;
import com.csci.thrift.democaptcha.FileUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

/**
 * Created by Sai on 2/21/2016.
 */
public class Client {
    //    public static String IP = "192.168.136.22";
    public static String IP = "127.0.0.1";

    public static void main(String[] args) {
        testDamaUsingStream();
//        testDama();

    }

    private static void testDama(){
        TTransport transport = new TSocket(IP, 9988);
        try {
            System.out.println("Please input file name:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            transport.open();
            TProtocol protocal = new TCompactProtocol(transport);
            DemoCaptcha.Client client = new DemoCaptcha.Client(protocal);
            System.out.println(client.getCaptcha(line.trim()));

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }


    private static void testDamaUsingStream(){
        TTransport transport = new TSocket(IP, 9988);
        try {
            System.out.println("Please input file name:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String path = br.readLine().trim();
            transport.open();
            TProtocol protocal = new TCompactProtocol(transport);
            DemoCaptcha.Client client = new DemoCaptcha.Client(protocal);
            ByteBuffer bf = FileUtils.readFileToStream(path);
            System.out.println(client.getCaptchaFromImageBinary(bf));

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }
}
