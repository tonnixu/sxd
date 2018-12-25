package com.jhh.dc.loan.app.web.form;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.api.loan.JdCardService;
import com.jhh.dc.loan.app.common.exception.CommonException;
import com.jhh.dc.loan.app.web.exception.ExceptionPageController;
import com.jhh.dc.loan.entity.app_vo.JdCardDetailVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 2018/7/30.
 */
@Controller
@RequestMapping("/form")
@Slf4j
public class JumpJDController extends ExceptionPageController {

    @Reference
    private JdCardService jdCardService;

    @ApiOperation(value = "查看京东卡")
    @RequestMapping("/jdCard/{phone}/{borrNum}")
    public String searchJDCard(@PathVariable("phone") String phone, @PathVariable("borrNum") String borrNum,
                               HttpServletRequest request,@RequestParam String prodType, Map<String,Object> map) throws CommonException {
        request.setAttribute("phone", phone);
        request.setAttribute("prodType", prodType);
        ResponseDo<JdCardDetailVO> cardInfo = jdCardService.getCardDetailByCardId(phone, borrNum);
        if (200 ==cardInfo.getCode()) {
            request.setAttribute("prodType", cardInfo.getData().getProdType());
            map.put("phone", phone);
            map.put("cardInfo", cardInfo.getData());
        }else {
            throw new CommonException(201,cardInfo.getInfo());
        }
        return "enjoyShop/JDCard";
    }
}
