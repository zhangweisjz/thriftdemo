package com.csci.thrift.democaptcha;

import com.cqz.dm.ImgRecTianyiTest;
import com.cqz.dm.UUAPI;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sai on 2/21/2016.
 */
public class CaptchaImpl implements DemoCaptcha.Iface {
    public static String error = "Error";
    boolean tested = false;
    public String getCaptcha(String captchaPath) throws org.apache.thrift.TException {
        String captcha=test(captchaPath);
        System.out.println(captcha);
        return captcha;
    }

    @Override
    public String getDllCaptcha(String captchaPath) throws TException {
        String result="";
        for(int i = 0; i<3 && StringUtils.isEmpty(result); i++){
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

    public String test(String filePath)  {
        String captcha="";
        boolean succeeded=false;
        for (int i = 0; i < 3 && !succeeded; i++) {
            try{
                if (!tested) {
                    //________________初始化接口类需要的参数，或者直接写到UUAPI。java文件里面________________

                    UUAPI.SOFTID = 109966;
                    UUAPI.SOFTKEY = "4c1e6682a3c647018e96918aedce1ba6";    //KEY 获取方式：http://dll.uuwise.com/index.php?n=ApiDoc.GetSoftIDandKEY
                    UUAPI.DLLVerifyKey = "3F0CC8F8-5413-4A33-810A-9902CF778BEF";//校验API文件是否被篡改，实际上此值不参与传输，关系软件安全，高手请实现复杂的方法来隐藏此值，防止反编译,获取方式也是在后台获取软件ID和KEY一个地方

                    UUAPI.USERNAME = "zhangweisjz";        //用户帐号和密码(非开发者帐号)，在打码之前，需要先设置好，给用户留一个输入帐号和密码的地方
                    UUAPI.PASSWORD = "sai123456";

                    boolean status = UUAPI.checkAPI();    //校验API，必须调用一次，校验失败，打码不成功

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
                succeeded=true;
            }catch(Exception ex){
                ex.printStackTrace();
                captcha=error;
            }
        }

        return captcha;

    }
}
