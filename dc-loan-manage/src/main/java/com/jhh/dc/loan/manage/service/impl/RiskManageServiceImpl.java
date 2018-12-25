package com.jhh.dc.loan.manage.service.impl;

import com.google.common.collect.Lists;
import com.jhh.dc.loan.entity.app.Reviewers;
import com.jhh.dc.loan.entity.common.ResponseCode;
import com.jhh.dc.loan.entity.manager.AuditsUser;
import com.jhh.dc.loan.manage.entity.Response;
import com.jhh.dc.loan.manage.mapper.ReviewMapper;
import com.jhh.dc.loan.manage.mapper.ReviewersMapper;
import com.jhh.dc.loan.manage.service.risk.RiskManageService;
import com.jhh.dc.loan.manage.service.user.UserService;
import com.jhh.dc.loan.manage.utils.Detect;
import com.jhh.dc.loan.manage.utils.QueryParamUtils;
import com.jhh.dc.loan.common.util.DateUtil;

import com.jinhuhang.risk.client.dto.plan.jsonbean.RiskPerNodeInfoDto;
import com.jinhuhang.risk.client.service.impl.node.UserServiceClient;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.*;

@Log4j
@Service
public class RiskManageServiceImpl implements RiskManageService {

	@Autowired
	private ReviewMapper reviewMapper;
	@Autowired
	private ReviewersMapper reviewersMapper;

	private UserServiceClient nodeClient=new UserServiceClient();

	@Autowired
	UserService userService;

	@Override
	public List auditsforUser(Map<String, String[]> parameterMap) {
		//拿取filter参数
		Map<String, Object> param = QueryParamUtils.getargs(parameterMap,"makeborrDate", "desc");

		if(Detect.notEmpty(parameterMap.get("borrStatus"))){
			param.put("borrStatusValue", parameterMap.get("borrStatus")[0]);
		}

		if (Detect.notEmpty(parameterMap.get("employNum"))) {
			param.put("employNum", parameterMap.get("employNum")[0]);
		}else{
			if(!Detect.notEmpty(param.get("makeborrDate_start") + "")) {
				//审核管理默认展示一月数据
				Calendar calendar = new GregorianCalendar();
				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 30);
				param.put("makeborrDate_start", DateUtil.getDateString(calendar.getTime()));
				Calendar calendar_end = new GregorianCalendar();
				calendar_end.set(Calendar.DATE, calendar_end.get(Calendar.DATE) + 1);
				param.put("makeborrDate_end", DateUtil.getDateString(calendar_end.getTime()));
			}
		}

		List<AuditsUser>  result= reviewMapper.getauditsforUser(param);

		if(result!=null&&result.size()>0){
			List<String> idNumbers = Lists.newArrayList();
			List<String> whiteIdNumbers = Lists.newArrayList();
			// 迭代取出是否认证节点的用户
			for (AuditsUser auditsUser : result) {
				String cardNum = auditsUser.getCardNum();
				if (StringUtils.isNotEmpty(cardNum)) {
					if(Detect.notEmpty(auditsUser.getWhiteList()) && auditsUser.getWhiteList().equals("1")){
						//白名单
						whiteIdNumbers.add(cardNum);
						continue;
					}
					idNumbers.add(cardNum);
				}
			}
			try {
				List<RiskPerNodeInfoDto> riskPerNodeInfoDtos = nodeClient.selectRecentPerNodeRecord(idNumbers, userService.riskNodeProductId());
				//白名单规则
				List<RiskPerNodeInfoDto> whiteRiskPerNodeInfoDtos = nodeClient.selectRecentPerNodeRecord(whiteIdNumbers, userService.riskNodeWhiteProductId());
				//组合list
				riskPerNodeInfoDtos.addAll(whiteRiskPerNodeInfoDtos);
				// 迭代遍历
				for (RiskPerNodeInfoDto dto : riskPerNodeInfoDtos) {
                    for(AuditsUser auditsUser:result){
                    	if(dto.getIdNumber().equals(auditsUser.getCardNum())){
							auditsUser.setDescription(dto.getDescription());
						}
					}
				}
			} catch (Exception e) {
				log.error("查询风控认证节点接口异常",e);
			}
		}

		return  result;
	}


	@Override
	public List getReviewers(String status) {
		Example example = new Example(Reviewers.class);
		if(Detect.notEmpty(status)){
			example.createCriteria().andEqualTo("status", status);
		}
		example.createCriteria().andEqualTo("isDelete", 0);
		return reviewersMapper.selectByExample(example);
	}

	@Override
	public Response modefiyReviewersStatus(String brroIds, String status) {
		Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
		if(Detect.notEmpty(brroIds) && Detect.notEmpty(status)){
			//修改状态
			Reviewers reviewers = new Reviewers();
			reviewers.setStatus(status);
			//条件ids
			String[] ids  = brroIds.split(",");
			Example example = new Example(Reviewers.class);
			example.createCriteria().andIn("id",Arrays.asList(ids));
			reviewersMapper.updateByExampleSelective(reviewers, example);

			response.code(ResponseCode.SUCCESS).msg("操作成功");
		}
		return response;
	}

	@Override
	public Response addReviewers(String employeeName, String employeeNum) {
		Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
		Reviewers reviewers;
		if(Detect.notEmpty(employeeName) && Detect.notEmpty(employeeNum)){
			//添加审核员
			reviewers=new Reviewers();
			reviewers.setEmployNum(employeeNum);
			List<Reviewers> list=reviewersMapper.select(reviewers);
			if(list!=null&&list.size()>0){
				reviewers=list.get(0);
				reviewers.setIsDelete("0");
				reviewers.setStatus("y");
				reviewersMapper.updateByPrimaryKeySelective(reviewers);
			}else {
				reviewers = new Reviewers();
				reviewers.setEmployeeName(employeeName);
				reviewers.setEmployNum(employeeNum);
				reviewers.setStatus("y");
				reviewers.setCreationDate((new SimpleDateFormat("yyyy-MM-dd HH-mm-ss")).format(new Date()));
				reviewers.setIsDelete("0");
				reviewersMapper.insert(reviewers);
			}
			response.code(ResponseCode.SUCCESS).msg("操作成功");
		}else{
			response.code(ResponseCode.FIAL).msg("失败成功，输入信息不完整");
		}
		return response;
	}

	@Override
	public Response addReviewers(Reviewers reviewers) {
		Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
		if(reviewers != null ){
			reviewersMapper.insertSelective(reviewers);
			response.code(ResponseCode.SUCCESS).msg("操作成功");
		}
		return response;
	}

	@Override
	public Response delReviewersByEmployNum(String employNum) {
		Response response = new Response().code(ResponseCode.FIAL).msg("操作失败");
		if(Detect.notEmpty(employNum)){
			reviewersMapper.updateReviewersIsDelete(employNum);
			response.code(ResponseCode.SUCCESS).msg("操作成功");
		}else{
			response.code(ResponseCode.FIAL).msg("失败成功，输入信息不完整");
		}
		return response;
	}

	@Override
	public Response getReviewersByEmployeeNum(String employeeNum){
		Integer size = reviewersMapper.selectReviewsByEmployNum(employeeNum).size();
		Response response = new Response();
		if(size > 0){
			response = new Response().code(ResponseCode.FIAL).msg("审核人已存在");
		}else{
			response = new Response().code(ResponseCode.SUCCESS).msg("审核人不存在");
		}
		return response;
	}

}
