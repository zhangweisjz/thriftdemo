package com.cqz.dm;

import com.csci.thrift.democaptcha.DemoCaptcha;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class ImgRecTianyiTest {
    public static String DLLPATH = "lib\\CaptchaOCR";   //DLL 路径
    public static int netIndex = 0;

    static {
        System.out.println(DLLPATH);
        netIndex = JPYZM.INSTANCE.VcodeInit("11E9F0B73D8850F4A54539332F1B5DFA");
        System.out.println("netIndex:" + netIndex);
    }

    public static void init() {
    }

    public interface JPYZM extends StdCallLibrary {
        JPYZM INSTANCE = (JPYZM) Native.loadLibrary(DLLPATH, JPYZM.class);

        int VcodeInit(String pwd);

        boolean GetVcode(int index, byte[] img, int len, byte[] code);
    }

    public static byte[] getContent(String picPath) throws IOException {
        File file = UUAPI.openImgFile(picPath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }

    public static void main(String[] args) throws IOException {
        //这里是用的本地图片读取的 也可以用网络读取的图片 只要把图片类型转换成 byte[]
        byte[] bs = getContent("C:\\temp\\pic\\temp.jpg");
        getCode(bs);
    }

    public static String getCode(byte[] imgbs) throws IOException {
        System.out.println("imgbs.length:" + imgbs.length);
        long begin = System.currentTimeMillis();
//		System.out.println(netIndex);
        byte[] code = new byte[20];
        String rtnCode = null;
        boolean result = JPYZM.INSTANCE.GetVcode(netIndex, imgbs, imgbs.length, code);
        System.out.println(result);
        if (result) {
            long end = System.currentTimeMillis();
            rtnCode = new String(code);
            System.out.println("识别时间：" + (end - begin) + "ms 识别结果:" + rtnCode);
        }
        return rtnCode;
    }
}
