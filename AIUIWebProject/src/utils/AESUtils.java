package utils;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Security;


public class AESUtils {
    // u8 charset
    protected static final Charset CHARSET_U8     = Charset.forName("utf-8");
    // u8 string
    protected static final String  CHARSET_U8_STR = "utf-8";

    /**
     * AES加密
     *
     * @param serectKey 秘钥
     * @param ivKey     向量偏移量
     * @param content   要加密的文本
     * @return
     */
    public static String encrypt ( String serectKey, String ivKey, String content) {
        String encryptMode = "AES/CBC/PKCS7Padding";
        String encyptedContent = null;
        try {
            String encyptType = encryptMode;
            SecretKeySpec keyspec = new SecretKeySpec(serectKey.getBytes(CHARSET_U8), "AES");
            Cipher cipher = Cipher.getInstance(encyptType,"BC");
            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes(CHARSET_U8));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, iv);
            byte[] byte_encode = content.getBytes(CHARSET_U8_STR);
            byte_encode = cipher.doFinal(byte_encode);
            encyptedContent = new String(byte_encode);
        } catch (Exception e) {
           
        }
        return encyptedContent;
    }

    public static String decrypt(String secretKey, String ivKey, byte[] content){
        String encryptMode = "AES/CBC/PKCS7Padding";
        
        byte[] secrecKeyByte = secretKey.getBytes(CHARSET_U8);
       
        String decryptContent = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            SecretKeySpec keyspec = new SecretKeySpec(secrecKeyByte, "AES");
            Cipher cipher = Cipher.getInstance(encryptMode, "BC");
            
            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes(CHARSET_U8));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
            byte[] byte_content = cipher.doFinal(content);

            decryptContent = new String(byte_content, CHARSET_U8_STR);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return decryptContent;
    }
}
