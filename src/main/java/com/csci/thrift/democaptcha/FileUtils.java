package com.csci.thrift.democaptcha;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.time.Instant;

/**
 * Created by Sai on 2/26/2016.
 */
public class FileUtils {
    public static int BUFFER_SIZE = 2048;

    public static ByteBuffer readFileToStream(String path) {
        ByteBuffer bf = null;

        if (path.contains("http")) {
            bf= readURLFile(path);
        } else {
            bf = readLocalFile(path);
        }

        return bf;
    }

    private static ByteBuffer readURLFile(String urlPath){
        ByteBuffer bf=null;
        try{
            URL url = new URL(urlPath);
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
            int contentLength = connection.getContentLength();
            ByteArrayOutputStream tmpOut;

            if (contentLength != -1) {
                tmpOut = new ByteArrayOutputStream(contentLength);
            } else {
                tmpOut = new ByteArrayOutputStream(BUFFER_SIZE);
            }

            byte[] b = new byte[BUFFER_SIZE];
            int length;

            while ((length = input.read(b)) != -1) {
                tmpOut.write(b, 0, length);
            }
            input.close();
            tmpOut.close();
            byte[] array = tmpOut.toByteArray();
            bf = ByteBuffer.wrap(array);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bf;
    }

    private static ByteBuffer readLocalFile(String path) {
        ByteBuffer bf = null;
        try {
            File f = new File(path);
            long fileSize = f.length();
            FileInputStream in = new FileInputStream(f);
            byte[] contents = new byte[(int) fileSize];
            in.read(contents);
            bf = ByteBuffer.wrap(contents);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bf;
    }

    public static void main(String[] args) {
        FileUtils fu=new FileUtils();
        fu.test2();
    }

    private void test1(){
        String path = "C:\\temp\\pic\\test.png";
        String destination = "C:\\temp\\captcha\\" + Instant.now().toEpochMilli() + ".jpg";
        ByteBuffer bf = readLocalFile(path);

        byteBufferToFile(bf, destination);
    }

    private void byteBufferToFile(ByteBuffer bf, String destination){
        byte[] contents = bf.array();
        try {
            OutputStream os = new FileOutputStream(destination);
            os.write(contents);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void test2(){
        String path="http://xygs.gsaic.gov.cn/gsxygs/securitycode.jpg";

        String destination = "C:\\temp\\captcha\\" + Instant.now().toEpochMilli() + ".jpg";

        ByteBuffer bf=readFileToStream(path);

        byteBufferToFile(bf, destination);
    }
}
