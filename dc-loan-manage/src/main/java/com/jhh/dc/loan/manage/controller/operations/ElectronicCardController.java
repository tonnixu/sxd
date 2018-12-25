package com.jhh.dc.loan.manage.controller.operations;

import com.jhh.dc.loan.manage.service.excel.ImportJDCardExcelEntity;
import com.jhh.dc.loan.manage.service.excel.verifyhandler.ElctronicCardVerifyHandler;
import com.jhh.dc.loan.common.util.ExcelUtils;
import com.jhh.dc.loan.manage.entity.Response;
import com.jhh.dc.loan.manage.service.operations.ElectronicCardService;
import com.jhh.dc.loan.manage.utils.Assertion;
import com.jhh.dc.loan.manage.utils.Detect;
import com.jhh.dc.loan.manage.utils.FileUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.result.ExcelImportResult;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by wanzezhong on 2018/7/24.
 */
@Controller
@RequestMapping("elctronicCard")
public class ElectronicCardController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ElectronicCardController.class);

    @Autowired
    ElctronicCardVerifyHandler elctronicCardVerifyHandler;

    @Autowired
    ElectronicCardService electronicCardService;
    /**
     * 上传电子卡
     */
    @RequestMapping(value = "/upload")
    @ResponseBody
    public Response getProducts(MultipartHttpServletRequest request, HttpServletResponse response) {
        try {
            if(null != request){
                MultipartFile listImgUrlFile = request.getMultiFileMap().getFirst("elctronicCard");
                if(null != listImgUrlFile && null != listImgUrlFile.getInputStream() && listImgUrlFile.getInputStream().read() > 0){
                    String fileName= FileUtil.reBuildFileName(listImgUrlFile.getOriginalFilename());
                    ImportParams params = new ImportParams();
                    params.setVerifyHanlder(elctronicCardVerifyHandler);
                    params.setNeedVerfiy(true);
                    params.setImportFields(new String[]{"卡号","卡密","有效开始日期","有效结束日期"});
                    ExcelImportResult<ImportJDCardExcelEntity> result = ExcelImportUtil.importExcelVerify(listImgUrlFile.getInputStream(),ImportJDCardExcelEntity.class, params);
                    Assertion.notNull(result, "导入失败,请核实数据是否正确！");
                    Workbook workbook = result.getWorkbook();
                    if(!result.isVerfiyFail()){
                        if(Detect.notEmpty(result.getList())){
                            List<ImportJDCardExcelEntity> jdCardList = result.getList();
                            int count = electronicCardService.importJDCardExcel(jdCardList);
                            if(Detect.isPositive(count)){
                                log.info("ProductController.importCoupons================={},导入的数量{}","导入成功",count);
                                return new Response().code(2000).msg("导入成功!导入的数量:" + count);
                            }
                            log.info("ProductController.importCoupons================={}","导入保存失败");
                            return new Response().code(4001).msg("导入失败,请检查数据是否已经导入!");
                        }
                    }
                    log.info("ProductController.importCoupons================={}","导入失败");
                    ExcelUtils.excelWriteDownloadFile(workbook, fileName, request, response);
                    return new Response().code(4001).msg("导入失败！");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return new Response().code(4001).msg("导入异常，请确认模版是否合规");
        }
        log.info("ProductController.importCoupons================={}","导入失败 请选择文件后再上传");
        return new Response().code(4001).msg("导入失败 请选择文件后再上传");
    }
}
