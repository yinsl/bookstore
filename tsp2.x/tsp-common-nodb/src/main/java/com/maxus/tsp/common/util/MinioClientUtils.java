package com.maxus.tsp.common.util;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import jar.org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.util.conf.OssConfig;

@Component
@EnableConfigurationProperties(OssConfig.class)
public class MinioClientUtils {
    private final static Logger logger = LogManager.getLogger(MinioClientUtils.class);
    
    @Autowired
	private OssConfig ossConfig;

    private String endpoint = "http://saicmotorcv-s3-for-qa.nj-uat-dce.saicstack.com";
    private String accessKey = "ACCESSKEY123";
    private String secretKey = "SECRETKEYHERE123";
    private String bucketName = "cert-uat";

    public void setupDefaultConfig(OssConfig ossConfig) {
        this.endpoint = ossConfig.getEndpoint();
        this.accessKey = ossConfig.getAccessKey();
        this.secretKey = ossConfig.getSecretKey();
        this.bucketName = ossConfig.getBucketName();
    }

    public void setupDefaultConfig(String endpoint, String accessKey, String secretKey, String bucketName) {
    	this.endpoint = endpoint;
    	this.accessKey = accessKey;
    	this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    // Create a minioClient with the Endpoint, Access key and Secret key.
    public MinioClient newMinioClient(String endpoint, String accessKey, String secretKey) {
        try {
            return new MinioClient(endpoint, accessKey, secretKey);
        } catch (InvalidEndpointException e) {
            logger.error("Error occurred: " + e);
        } catch (InvalidPortException e) {
            logger.error("Error occurred: " + e);
        }
        return null;
    }

    public MinioClient newMinioClient() {
        return newMinioClient(endpoint, accessKey, secretKey);
    }

    public void checkBucket() throws Exception {
        MinioClient minioClient = newMinioClient();
        if (minioClient == null) return;
        boolean found = minioClient.bucketExists(bucketName);
        if (found) {
            logger.info("Bucket {} exists", bucketName);
        } else {
            logger.info("Bucket does not exist, makeBucket: {}", bucketName);
            minioClient.makeBucket(bucketName);
        }
    }

    public void uploadFileToBucket(String objectName, String filePath) throws Exception {
        MinioClient minioClient = newMinioClient();
        if (minioClient == null) return;
        minioClient.putObject(bucketName, objectName, filePath);
    }

    public void downloadFileFromBucket(String objectName, String saveToFilePath) throws Exception {
        MinioClient minioClient = newMinioClient();
        if (minioClient == null) return;
        InputStream inputStream = minioClient.getObject(bucketName, objectName);
        File targetFile = new File(saveToFilePath);
        FileUtils.copyInputStreamToFile(inputStream, targetFile);
    }

    public String putBase64CertData(String vinOrSn, String base64CertData) throws Exception {
        final String contentType = "text/plain";
        MinioClient minioClient = newMinioClient();
        if (minioClient == null) return null;
        InputStream inputStream = new ByteArrayInputStream(base64CertData.getBytes(StandardCharsets.UTF_8));
        minioClient.putObject(bucketName, vinOrSn, inputStream, contentType);
        return vinOrSn;
    }

    public String getBase64CertData(String vinOrSn) throws Exception {
        MinioClient minioClient = newMinioClient();
        logger.info("minioClient: {}", minioClient);
        if (minioClient == null) return null;
        InputStream inputStream = minioClient.getObject(bucketName, vinOrSn);
        if (inputStream == null) return null;
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
    	MinioClientUtils minio = new MinioClientUtils();
    	minio.checkBucket();
        //String test = new BASE64Encoder().encode("test".getBytes());
        String test= new String(Base64.encodeBase64("test".getBytes()));
        System.out.println("存入服务器内容：" + test);
        minio.putBase64CertData(" a20170605106055", test);

        String base64String = minio.getBase64CertData(" a20170605106055");
        System.out.println("服务器返回证书结果：" + base64String);
        System.out.println("解码后结果：" + ByteUtil.bytesToString(Base64.decodeBase64(base64String)));
    }

}
