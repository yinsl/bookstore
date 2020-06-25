package com.maxus.tsp.common.util;

import javax.crypto.Cipher;
import cn.com.jit.assp.dsign.DSign;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.security.*;

/**
 * @ClassName     PKICAUtil.java
 * @Description:    用于PKI/CA 签名、验签  加密、减密
 * @Author:         zhuna
 * @CreateDate:     2019/3/4 11:08
 * @Version:        1.0
 */
public class PKICAUtil {

    private static final Logger logger = LogManager.getLogger(PKICAUtil.class);

    //pki CA配置文件
    //private static final String PKI_CA_CONFIG_PATH = PKICAUtil.class.getResource("/").getPath() + "cssconfig.properties";
    private static final String PKI_CA_CONFIG_PATH = "/etc/pkica/cssconfig.properties";

    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /** 加密算法 RSA */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * @method      私钥签名
     * @description
     * @param priKey 密钥
     * @return
     * @author      zhuna
     * @date        2019/3/4 13:39
     */
    public static byte[] sign(PrivateKey priKey, byte[] data) throws Exception{
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(priKey);
        sig.update(data);
        byte[] result = sig.sign();
        return result;
    }

    public static boolean verify(PublicKey pubKey, byte[] originData, byte[] signData)throws Exception{
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(pubKey);
        sig.update(originData);
        boolean b=sig.verify(signData);
        return b;
    }

    /**
     * @method      公钥加密
     * @description
     * @param pubKey
     * @return
     * @author      zhuna
     * @date        2019/3/4 14:13
     */
    public static byte[] publicKeyEncrypt(PublicKey pubKey, byte[] data) throws Exception{
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] result = cipher.doFinal(data);
        return result;
    }

    /**
     * @method      私钥解密
     * @description
     * @param priKey
     * @return
     * @author      zhuna
     * @date        2019/3/4 14:16
     */
    public static byte[] privateKeyDecrypt(PrivateKey priKey, byte[] encryptedData ) throws Exception{
        logger.debug("私钥解密原始报文：{}", ByteUtil.byteToHex(encryptedData));
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        byte[] result=cipher.doFinal(encryptedData);
        return result;
    }



    /**
     * @method
     * @description  私钥解密（数字信封）  网关私钥解密
     * @param  encryptedData  密文
     * @return
     * @author      zhuna
     * @date        2019/3/13 16:40
     */
    public static byte[] privateKeyDecrypt(byte[] encryptedData){
        byte[] result = new byte[]{};
        DSign vEnvlopDs = new DSign();
        String encryptedDataBase64 = new String(Base64.encodeBase64(encryptedData));
        //logger.debug("privateKeyDecrypt encryptedDataBase64:{}", encryptedDataBase64);
        //logger.debug("filePath:{}",PKI_CA_CONFIG_PATH);
        vEnvlopDs.initConfig(PKI_CA_CONFIG_PATH);// 读配置文件，获取ServerURL和CertAlias的值
        long longSignReturn = vEnvlopDs.decryptEnvelop(encryptedDataBase64.getBytes());// 验数字信封请求
        if (longSignReturn == 0) {
            result = vEnvlopDs.getByteData();
        }else {
            logger.warn("私钥解密失败，错误码：{}，错误信息：{}",longSignReturn, vEnvlopDs.getErrorMessage());
        }
        return result;
    }

    /**
     * @method      verifySign
     * @description   验签
     * @param verifySignData   签名密文
     * @return
     * @author      zhuna
     * @date        2019/3/14 9:20
     */
    public static byte[] verifyAttachedSign(byte[] verifySignData){
        byte[] result = new byte[]{};
        DSign vSignDs  = new DSign();
        String encryptedDataBase64 = new String(Base64.encodeBase64(verifySignData));
        //logger.debug("verifyAttachedSign encryptedDataBase64:{}", encryptedDataBase64);
        vSignDs.initConfig(PKI_CA_CONFIG_PATH);// 读配置文件，获取ServerURL和CertAlias的值
        //long a = vSignDs.verifyAttachedSign(signReturn.getBytes());//signReturn 为base64格式的签名结果
        long verifySignReturn = vSignDs.verifyAttachedSign(encryptedDataBase64.getBytes());//signReturn 为base64格式的签名结果
        //验签成功
        if(0 == verifySignReturn){
            result = vSignDs.getByteData();
        }else{//验签失败，获取错误码
            logger.warn("验签失败，错误码：{}，错误信息：{}",vSignDs.getErrorCode(), vSignDs.getErrorMessage());
            //vSignDs.getErrorCode();
        }
        return result;
    }

    /**
     * @method   数字签名   网关私钥签名    证书标识
     * @description
     * @param certId
     * @param signData
     * @return
     * @author      zhuna
     * @date        2019/3/14 9:48
     */
    public static byte[] attachSign(String certId, byte[] signData){
        //logger.debug("签名原文数组16进制{}", ByteUtil.byteToHex(signData));
        byte[] result = new byte[]{};
        DSign signDs = new DSign();
        signDs.initConfig(PKI_CA_CONFIG_PATH); //通过配置文件读取参数
        String signReturn = signDs.attachSign(certId, signData);//证书标识由服务器配置决定
        //签名成功
        if(signReturn != null){
            //logger.info("签名成功，签名结果Base64：{}", signReturn);
            result = signReturn.getBytes();
            //logger.info("签名成功，签名结果16进制打印：{}", ByteUtil.byteToHex(result));
        }else {
            logger.warn("签名失败，错误码：{}，错误信息：", signDs.getErrorCode(), signDs.getErrorMessage());
        }
        return result;
    }
    
    /**
     * @method      公钥加密（打数字信封）   TBox公钥加密    证书别名/证书标识可用TBox证书作为入参
     * @description
     * @param certification   TBox证书
     * @param originData   原始数据
     * @return      
     * @author      zhuna
     * @date        2019/3/14 9:54
     */
    public static byte[] publicKeyEncrypt(String certification, byte[] originData){
        logger.debug("公钥加密（打数字信封）原文base64数组16进制：{}", ByteUtil.byteToHex(originData));
        logger.debug("公钥加密（打数字信封）TBox证书：{}", certification);
        byte[] result = new byte[]{};
        DSign envlopDs  = new DSign();
        envlopDs.initConfig(PKI_CA_CONFIG_PATH); //通过配置文件读取参数
        //打不带签名数字信封多接收者证书别名数组
        String[] alias = {certification};
        //打数字信封
        String signReturn = envlopDs.encryptEnvelop(alias, originData); // 打数字信封请求
        //打数字信封失败
        if (signReturn.equals("") || signReturn.length() <= 0) {
            logger.warn("公钥加密（打数字信封）时出现错误，错误号为:{}, 错误描述：{}",envlopDs.getErrorCode(), envlopDs.getErrorMessage());
        } else {
            logger.info("公钥加密（打数字信封）,加密结果Base64：{}", signReturn);
            //Base64.decodeBase64(result);
            result = signReturn.replaceAll("\\r|\\n","").getBytes();
            logger.info("公钥加密（打数字信封）,去除回车换行加密结果：{}", ByteUtil.byteToHex(result));
        }
        return result;
    }

    public static void main(String[] args){
       /* File configfile = null;
        configfile = new File("");
        if (configfile.exists() && !configfile.isDirectory()) {
            System.out.println("获取到配置文件");
        } else {
            System.out.println("获取配置文件失败");
        }*/
       /* String test = "abcdef0123456789";
        String publicKeyBase64 = new String(Base64.encodeBase64(test.getBytes()));
        System.out.println("base64 String:" + publicKeyBase64);
        System.out.println("base64 String getBytes:"+ ByteUtil.byteToHex(publicKeyBase64.getBytes()));
        System.out.println("base64 String to origin bytes:" + ByteUtil.bytesToString(Base64.decodeBase64(publicKeyBase64)));*/

       /* byte[] result = new byte[]{};
        DSign vEnvlopDs = new DSign();
        //logger.debug("demo base64:{}",new BASE64Encoder().encode(encryptedData));
        String PKI_CA_CONFIG_PATH = PKICAUtil.class.getResource("/").getPath() + "cssconfig.properties";
        logger.debug("filePath:{}",PKI_CA_CONFIG_PATH);*/

    }

}
