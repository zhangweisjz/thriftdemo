package com.csci.thrift.democaptcha;

import com.cqz.dm.ImgRecTianyiTest;
import com.cqz.dm.UUAPI;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Sai on 2/21/2016.
 */
public class CaptchaImpl implements DemoCaptcha.Iface {
    public static String error = "Error";
    boolean tested = false;

    public String getCaptcha(String captchaPath) throws org.apache.thrift.TException {
        String captcha = test(captchaPath);
        System.out.println(captcha);
        return captcha;
    }

    @Override
    public String getCaptchaFromImageBinary(ByteBuffer captchaPath) throws TException {
        String captcha=uu(captchaPath);
        System.out.println(captcha);
        return captcha;
    }

    @Override
    public String getDllCaptcha(String captchaPath) throws TException {
        String result = "";
        for (int i = 0; i < 3 && StringUtils.isEmpty(result); i++) {
            byte[] bs = new byte[0];
            try {
                bs = ImgRecTianyiTest.getContent(captchaPath);
                result = ImgRecTianyiTest.getCode(bs);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    @Override
    public String getDllCaptchaFromImageBinary(ByteBuffer path) throws TException {
        byte[] bs = path.array();
        String result = "";
        for (int i = 0; i < 3 && StringUtils.isEmpty(result); i++) {
            try {
                result = ImgRecTianyiTest.getCode(bs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public String uu(ByteBuffer bf) {
        String captcha = "";
        boolean succeeded = false;
        for (int i = 0; i < 3 && !succeeded; i++) {
            try {
                if (!tested) {
                    boolean status = UUAPI.setup();    //校验API，必须调用一次，校验失败，打码不成功
                    if (!status) {
                        System.out.print("API文件校验失败，无法使用打码服务");
                        return error;
                    } else {
                        tested = true;
                    }
                }
                //________________初始化参数结束，上面的操作只需要设置一次________________

                System.out.println("Started...");
                //识别开始
                String result[] = UUAPI.uuDecaptcha(bf, 1004);//picPath是图片路径,1004是codeType,http://www.uuwise.com/price.html

                System.out.println("this img codeID:" + result[0]);
                System.out.println("return recongize Result:" + result[1]);

                captcha = result[1];
                succeeded = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                captcha = error;
            }
        }

        return captcha;

    }

    public String test(String filePath) {
        String captcha = "";
        boolean succeeded = false;
        for (int i = 0; i < 3 && !succeeded; i++) {
            try {
                if (!tested) {
                    boolean status = UUAPI.setup(); //校验API，必须调用一次，校验失败，打码不成功
                    if (!status) {
                        System.out.print("API文件校验失败，无法使用打码服务");
                        return error;
                    } else {
                        tested = true;
                    }
                }
                //________________初始化参数结束，上面的操作只需要设置一次________________

                String picPath = filePath;    //测试图片的位置

                System.out.println("Started...");
                //识别开始
                String result[] = UUAPI.easyDecaptcha(picPath, 1004);//picPath是图片路径,1004是codeType,http://www.uuwise.com/price.html

                System.out.println("this img codeID:" + result[0]);
                System.out.println("return recongize Result:" + result[1]);

                captcha = result[1];
                succeeded = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                captcha = error;
            }
        }

        return captcha;

    }
}
