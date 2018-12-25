package com.jhh.dc.loan.service.loan;

import com.alibaba.fastjson.JSONObject;
import com.jhh.dc.loan.api.loan.CompanyAService;
import com.jhh.dc.loan.common.util.HttpsUtil;
import com.jhh.dc.loan.entity.manager_vo.PrivateVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class CompanyAServiceImpl implements CompanyAService {
    @Value("${queryUserInfoByCompanyAUrl}")
    private  String url;

    @Value("${baseUrl}")
    private String baseUrl;
    //根据身份证号获取用户详细信息
    @Override
    public PrivateVo queryUserPrivateByPhone(String phone) {
        PrivateVo privateVo=null;
        Map param=new HashMap();
        param.put("phone",phone);
        String result= HttpsUtil.sendPost(url,param);
        if(StringUtils.isEmpty(result)){
            return null;
        }
        JSONObject jsonObject=JSONObject.parseObject(result);
        if(!"1".equals(jsonObject.getString("code"))){
            return null;
        }
        if(StringUtils.isEmpty(jsonObject.getString("object"))){
            return null;
        }
        privateVo=JSONObject.parseObject(jsonObject.getString("object"),PrivateVo.class);
        try {
            if (privateVo != null) {
                if (StringUtils.isNotEmpty(privateVo.getImageZ())) {
                    privateVo.setImageZ(baseUrl + URLEncoder.encode(privateVo.getImageZ(), "UTF-8"));
                }
                if (StringUtils.isNotEmpty(privateVo.getImageF())) {
                    privateVo.setImageF(baseUrl + URLEncoder.encode(privateVo.getImageF(), "UTF-8"));
                }
            }
        }catch(Exception e){

        }
        return privateVo;
    }
    public static void main(String[] args){
        String url="http://172.16.11.10:8080/platform-manage/user/getUserInfoByPhone.action";
        PrivateVo privateVo=null;
        Map param=new HashMap();
        param.put("phone","1111");
        String result=HttpsUtil.sendPost(url,param);
        //System.out.println(result);
        JSONObject jsonObject=JSONObject.parseObject(result);
        jsonObject=JSONObject.parseObject(jsonObject.getString("object"));
        PrivateVo privateVo1=JSONObject.parseObject(jsonObject.toString(),PrivateVo.class);
        System.out.println(privateVo1);
        System.out.println(JSONObject.toJSONString(privateVo1));
    }
}
