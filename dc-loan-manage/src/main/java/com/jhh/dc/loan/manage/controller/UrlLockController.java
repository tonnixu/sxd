package com.jhh.dc.loan.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/url")
public class UrlLockController {


	@ResponseBody
	@RequestMapping(value ="/url_lock",  method = RequestMethod.POST )
	public String url_lock(String var_text){
		try  
        {  
          byte[] _ssoToken = var_text.getBytes("ISO-8859-1");  
          String name = new String();  
         // char[] _ssoToken = ssoToken.toCharArray();  
          for (int i = 0; i < _ssoToken.length; i++) {  
              int asc = _ssoToken[i];  
              _ssoToken[i] = (byte) (asc + 27);  
              name = name + (asc + 27) + "_";  
          }  
         return name;  
        }catch(Exception e)  
        {  
          e.printStackTrace() ;  
          return "";
        }
	}
	@ResponseBody
	@RequestMapping(value ="/url_open",  method = RequestMethod.POST )
	public String url_open(String var_text_locked){
		 try  
	        {  
	          String name = new String();  
	          java.util.StringTokenizer st=new java.util.StringTokenizer(var_text_locked,"_");  
	          while (st.hasMoreElements()) {  
	            int asc =  Integer.parseInt((String)st.nextElement()) - 27;  
	            name = name + (char)asc;  
	          }  
	  
	         return name  ;
	        }catch(Exception e)  
	        {  
	        	return "";
	        }
		
		
	}
}
