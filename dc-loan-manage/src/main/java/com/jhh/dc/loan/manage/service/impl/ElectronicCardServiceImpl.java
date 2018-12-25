package com.jhh.dc.loan.manage.service.impl;

import com.jhh.dc.loan.common.util.Base64;
import com.jhh.dc.loan.common.util.rsa.RSAUtil;
import com.jhh.dc.loan.manage.service.excel.ImportJDCardExcelEntity;
import com.jhh.dc.loan.manage.mapper.JdCardInfoMapper;
import com.jhh.dc.loan.manage.service.operations.ElectronicCardService;
import com.jhh.dc.loan.manage.service.refund.RefundRecordService;
import com.jhh.dc.loan.manage.utils.Detect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.List;

@Service
public class ElectronicCardServiceImpl implements ElectronicCardService {

    private static Logger log = LoggerFactory.getLogger(RefundRecordService.class);

    @Autowired
    private JdCardInfoMapper jdCardInfoMapper;

    @Value("${rsa_public_key_path}")
    private String publicKeyPath;

    private RSAPublicKey rsaPublicKey;


    @PostConstruct
    public void initPublicKey(){
        String publicKey = RSAUtil.readKeyFromFile(publicKeyPath);
        if(publicKey == null){
            throw new RuntimeException("读取私钥失败:私钥路径"+publicKeyPath);
        }
        rsaPublicKey = RSAUtil.readPublicKeyFromString(publicKey);
    }

    @Override
    public int importJDCardExcel(List<ImportJDCardExcelEntity> jdCardList) throws Exception {
        if(Detect.notEmpty(jdCardList)){

            for(ImportJDCardExcelEntity jdCardExce : jdCardList){
                // 公钥加密
                String password = RSAUtil.encryptByPublicKey(jdCardExce.getPassword(), rsaPublicKey);
                // 对称加密
                password = Base64.encode(password);
                jdCardExce.setPassword(password);

                jdCardExce.setCreateDate(Calendar.getInstance().getTime());
                jdCardExce.setUpdateDate(Calendar.getInstance().getTime());
            }
            return jdCardInfoMapper.saveCardExcel(jdCardList);
        }

        return 0;
    }
}
