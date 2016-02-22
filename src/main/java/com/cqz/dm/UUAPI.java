package com.cqz.dm;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.imageio.ImageIO;

import com.csci.thrift.democaptcha.SaveImageFromUrl;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class UUAPI {

	public static String	USERNAME	= "zhangweisjz";			//UU用户名
	public static String	PASSWORD	= "sai123456";						//UU密码
	public static String	DLLPATH		= "lib\\UUWiseHelper_x64";					//DLL
	//public static String	IMGPATH		= "img\\test.png";
	public static int		SOFTID		= 109966;									//软件ID 获取方式：http://dll.uuwise.com/index.php?n=ApiDoc.GetSoftIDandKEY
	public static String	SOFTKEY		= "4c1e6682a3c647018e96918aedce1ba6";	//软件KEY 获取方式：http://dll.uuwise.com/index.php?n=ApiDoc.GetSoftIDandKEY
	public static String	DLLVerifyKey="3F0CC8F8-5413-4A33-810A-9902CF778BEF";	//校验API文件是否被篡改，实际上此值不参与传输，关系软件安全，高手请实现复杂的方法来隐藏此值，防止反编译,获取方式也是在后台获取软件ID和KEY一个地方
	public static boolean	checkStatus = false;

	
	public interface UUDLL extends Library		//载入优优云的静态库
	{

		UUDLL	INSTANCE	= (UUDLL) Native.loadLibrary(DLLPATH, UUDLL.class);		
		public int uu_reportError(int id);		
		public int uu_setTimeOut(int nTimeOut);
		public int uu_loginA(String UserName, String passWord);
		public int uu_recognizeByCodeTypeAndBytesA(byte[] picContent, int piclen, int codeType, byte[] returnResult);
		public void uu_getResultA(int nCodeID, String pCodeResult);
		public int uu_getScoreA(String UserName, String passWord);	//查题分
		public int uu_easyRecognizeFileA(int softid, String softkey, String userName, String password, String imagePath, int codeType, byte[] returnResult);//一键识别函数
		public int uu_easyRecognizeBytesA(int softid, String softkey, String username, String pasword, byte[] picContent, int piclen, int codeType, byte[] returnResult);
		public void uu_CheckApiSignA(int softID, String softKey, String guid, String filemd5, String fileCRC, byte[] returnResult); //api校验函数
	}

	
	public static int getScore()
	{
		return UUDLL.INSTANCE.uu_getScoreA(USERNAME, PASSWORD);
	}
		
	public static String[] easyDecaptcha(String picPath,int codeType) throws IOException, URISyntaxException {
		if(!checkStatus){

			String rs[]={"-19004","API校验失败,或未校验"};
			return rs;
		}
        File f;
        String tempPath;
		if(picPath.toLowerCase().startsWith("http")){
            tempPath = SaveImageFromUrl.saveImage(picPath);
            f = new File(tempPath);
        }else {
            f = new File(picPath);
        }
		byte[] by = null;
		try {
			by = toByteArray(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		byte[] resultBtye=new byte[100];		//为识别结果申请内存空间
		int codeID=UUDLL.INSTANCE.uu_easyRecognizeBytesA(SOFTID, SOFTKEY, USERNAME, PASSWORD, by, by.length, codeType, resultBtye);
		String resultResult = null;
		try {
			resultResult = new String(resultBtye,"GB2312");//如果是乱码，这改成UTF-8试试
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
		resultResult=resultResult.trim();
		
		//下面这两条是为了防止被破解	
		
		String rs[]={String.valueOf(codeID),checkResult(resultResult, codeID)};
		return rs;
	}
	
	
	public static boolean checkAPI() throws IOException
	{
		String FILEMD5=GetFileMD5(DLLPATH+".dll"); //API文件的MD5值
		String FILECRC=doChecksum(DLLPATH+".dll");	//API文件的CRC32值
		String GUID=Md5(Long.toString(Math.round(Math.random()*11111+99999)));	//随机值，此值一定要每次运算都变化
		
		//本地验证结果:	
		String okStatus=Md5(SOFTID+(DLLVerifyKey.toUpperCase())+GUID.toUpperCase()+FILEMD5.toUpperCase()+FILECRC.toUpperCase());

		byte[] CheckResultBtye=new byte[512];
		/**
		 * uu_CheckApiSignA用于防止别人替换优优云的API文件
		 * 后面对结果再进行校验则是避免被HOOK，从而防止恶意盗码
		 * */
		UUDLL.INSTANCE.uu_CheckApiSignA(SOFTID, SOFTKEY.toUpperCase(), GUID.toUpperCase(), FILEMD5.toUpperCase(), FILECRC.toUpperCase(), CheckResultBtye);

		String  checkResultResult = new String(CheckResultBtye,"UTF-8");
		checkResultResult=checkResultResult.trim();
		
		
		checkStatus=true;
		return checkResultResult.equals(okStatus);
	}
	
	
	public static String checkResult(String dllResult, int CodeID)
	{
		//dll返回的是错误代码
		if(dllResult.indexOf("_") < 0)
			return dllResult;
		
		//对结果进行校验
		String[] re = dllResult.split("_");
		String verify = re[0];
		String code = re[1];
		String localMd5 = null;
		try {
			localMd5 = Md5(SOFTID + DLLVerifyKey + CodeID + code.toUpperCase()).toUpperCase();
			//System.out.println("local checkValue:"+localMd5+"code:"+code);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		if(localMd5.equals(verify))	//判断本地验证结果和服务器返回的验证结果是否一至，防止API被hook
			return code;
		else
			return "校验失败";
	}
	public static byte[] toByteArray(File imageFile) throws Exception
	{
		BufferedImage img = ImageIO.read(imageFile);
		ByteArrayOutputStream buf = new ByteArrayOutputStream((int) imageFile.length());
		try
		{
			ImageIO.write(img, "jpg", buf);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return buf.toByteArray();
	}

	public static byte[] toByteArrayFromFile(String imageFile) throws Exception
	{
		InputStream is = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			is = new FileInputStream(imageFile);
			byte[] b = new byte[1024];
			int n;
			while ((n = is.read(b)) != -1)
			{
				out.write(b, 0, n);
			}// end while

		} catch (Exception e)
		{
			throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
		} finally
		{

			if (is != null)
			{
				try
				{
					is.close();
				} catch (Exception e)
				{}// end try
			}// end if

		}// end try
		return out.toByteArray();
	}
	//CRC32函数开始
	
	public static String doChecksum(String fileName) {

        try {

            CheckedInputStream cis = null;
            try {
                // Computer CRC32 checksum
                cis = new CheckedInputStream(
                        new FileInputStream(fileName), new CRC32());
                
            } catch (FileNotFoundException e) {
                //System.err.println("File not found.");
                //System.exit(1);
            }
            
            byte[] buf = new byte[128];
            while(cis.read(buf) >= 0) {
            }

            long checksum = cis.getChecksum().getValue();
            cis.close();
            //System.out.println( Integer.toHexString(new Long(checksum).intValue()));
            return Integer.toHexString(new Long(checksum).intValue());
            
        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(1);
        }
        
        return null;

    }
	//CRC32函数结束
	
	
	//MD5校验函数开始
    /**
     * 获取指定文件的MD5值
     * 
     * @param inputFile
     *            文件的相对路径
     */
	public static String GetFileMD5(String inputFile) throws IOException {
		int bufferSize = 256 * 1024;
		FileInputStream fileInputStream = null;
		DigestInputStream digestInputStream = null;
		try {
			MessageDigest messageDigest =MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(inputFile);
			digestInputStream = new DigestInputStream(fileInputStream,messageDigest);
			byte[] buffer =new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0);
			messageDigest= digestInputStream.getMessageDigest();
			byte[] resultByteArray = messageDigest.digest();
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}finally {
			try {
				digestInputStream.close();
			}catch (Exception e) {
				
			}try {
				fileInputStream.close();
			}catch (Exception e) {
				
			}
		}
	}
	public static String Md5(String s) throws IOException{
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			return byteArrayToHex(md);
		}catch (Exception e) {
            e.printStackTrace();
            return null;
        }
		
	}
	public static String byteArrayToHex(byte[] byteArray) {
		char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'a','b','c','d','e','f' };
		char[] resultCharArray =new char[byteArray.length * 2];
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b& 0xf];
		}
		return new String(resultCharArray);
	}
	
	//MD5校验函数结束
}
