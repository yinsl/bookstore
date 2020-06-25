package com.maxus.tsp.common.util.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@Data
//@Component
@ConfigurationProperties(prefix = "app.oss", ignoreUnknownFields = false)
public class OssConfig {
    //@Value("${app.oss.endpoint:http://saicmotorcv-s3-for-qa.nj-uat-dce.saicstack.com}")
    private String endpoint;

    //@Value("${app.oss.accessKey:ACCESSKEY123}")
    private String accessKey;

    //@Value("${app.oss.secretKey:SECRETKEYHERE123}")
    private String secretKey;

    //@Value("${app.oss.bucketName:cert-uat}")
    private String bucketName;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }


    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
