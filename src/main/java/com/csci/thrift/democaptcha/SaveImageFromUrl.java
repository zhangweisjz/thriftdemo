package com.csci.thrift.democaptcha;

/**
 * Created by Sai on 2/21/2016.
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;

public class SaveImageFromUrl {

    public static void main(String[] args) throws Exception {
        String imageUrl = "http://qyxy.baic.gov.cn/checkCodeServlet";

        saveImage(imageUrl);
    }

    public static String saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
        return destinationFile;
    }


    public static String saveImage(String imageUrl) throws IOException {
        long currentMilliSecond = Instant.now().toEpochMilli();
        String destinationFile = "D:\\temp\\captcha\\"+currentMilliSecond+".jpg";

        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
        return destinationFile;
    }
}
