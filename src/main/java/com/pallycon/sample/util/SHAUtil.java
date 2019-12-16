/**
 * @Class Name : SHAUtil.java
 * @Description : SHAUtil Class
 * 
 * @author 
 * @since 2014. 09. 14.
 * @version 1.0
 * @see 
 * @Copyright ⓒ 2011  
 * <pre>
 * ------------------------------------------------------------------
 * Modification Information 
 * ------------------------------------------------------------------
 *   수정일              수정자              수정내용
 *  
 * ------------------------------------------------------------------
 *                
 *   
 * </pre>  
 */
package com.pallycon.sample.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHAUtil {

	public static String encrypt(String str){
		String SHA;
		try{
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes("UTF-8"));
			byte byteData[] = sh.digest();

			SHA = Base64.getEncoder().encodeToString(byteData);
		}catch(NoSuchAlgorithmException e){
			e.toString();
			SHA = null;
		}catch (UnsupportedEncodingException e){
			e.toString();
			SHA = null;
		}
		return SHA;
	}
}