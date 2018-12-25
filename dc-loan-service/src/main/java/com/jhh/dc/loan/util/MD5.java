package com.jhh.dc.loan.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

/**
 * MD5签名处理核心文件
 * */

public class MD5 {
    private static final Logger logger = LoggerFactory
            .getLogger(MD5.class);
    /**
     * 签名字符串
     *
     * @param text
     *            需要签名的字符串
     * @param key
     *            密钥
     * @param input_charset
     *            编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String charset) throws Exception {
        text = text + key;
        return DigestUtils.md5Hex(getContentBytes(text, charset));
    }

    /**
     * 签名字符串
     *
     * @param text
     *            需要签名的字符串
     * @param sign
     *            签名结果
     * @param key
     *            密钥
     * @param input_charset
     *            编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String charset) throws Exception {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, charset));
        logger.info("----------text参数为=\n"+text+"--------------------\n接收到的SIGN为\n" +sign+"-----------\n系统计算的mysign为\n"+mysign);
        if (mysign.equals(sign)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}