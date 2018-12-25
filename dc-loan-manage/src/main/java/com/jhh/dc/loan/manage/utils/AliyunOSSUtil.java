package com.jhh.dc.loan.manage.utils;


import com.aliyun.oss.OSSClient;

import java.io.*;
import java.util.Date;

public class AliyunOSSUtil {

    private AliyunOSSUtil() {
    }

    private static String endpoint;


    private static String accessKeyId;


    private static String accessKeySecret;


    private static String bucketName;

    static {
        try {
            endpoint= PropertiesReaderUtil.read("aliyun_oss", "endPointOSS");
            accessKeyId= PropertiesReaderUtil.read("aliyun_oss", "accessKeyIdOSS");
            accessKeySecret= PropertiesReaderUtil.read("aliyun_oss", "accessKeySecretOSS");
            bucketName= PropertiesReaderUtil.read("aliyun_oss", "bucketNameOSS");
        } catch (Exception e) {
            throw new RuntimeException("初始化OSS出错：", e);
        }
    }




    //        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    //        String accessKeyId = "<yourAccessKeyId>";
    //        String accessKeySecret = "<yourAccessKeySecret>";
    public static String updateFileByByte(byte[] content,String fileName){
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId,accessKeySecret);
        ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(content));
        ossClient.shutdown();
        Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 10);
        String url = ossClient.generatePresignedUrl(bucketName, fileName, expiration).toString();
        return url;
    }
    public static void main(String[] args){
//        String endpoint = "oss-cn-shanghai.aliyuncs.com";
//        String accessKeyId = "LTAI4KA5ey8SNYgP";
//        String accessKeySecret = "WxxFKBZBRvKmHtlhd6s6nfCo1SeQyV";
//        OSSClient ossClient = new OSSClient(endpoint, accessKeyId,accessKeySecret);
//        byte[] content =File2byte("d://Desert.jpg");
//        ossClient.putObject("kakatest", "sss.jpg", new ByteArrayInputStream(content));
//        ossClient.shutdown();
//        Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 10);
//        String url = ossClient.generatePresignedUrl("kakatest", "sss.jpg", expiration).toString();
//        System.out.println(url);
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
}
