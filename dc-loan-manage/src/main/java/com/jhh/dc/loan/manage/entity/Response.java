package com.jhh.dc.loan.manage.entity;

import java.io.Serializable;

import com.jhh.dc.loan.common.util.CodeReturn;

/**
 * 调用结果反馈
 * code :默认：
 * httpstatus:            code:
 * 200,成功,             2000
 * 201,创建成功                           2001
 * 400,客户端错误                     4000
 * 401,未授权                               4001
 * 404，未找到资源                   4004
 * 500内部错误                             5000
 * @author carl.wan
 *
 */
public class Response implements Serializable{
	private int code;
	private String msg;
	private Object data;
	public Response code(int code){
		this.code = code;
		return this;
	}
	public Response msg(String msg){
		this.msg = msg;
		return this;
	}
	public Response data(Object data){
		this.data = data;
		return this;
	}
	public Response getResponse(int code, String msg, Object data) {
		return new Response().code(code).msg(msg).data(data);
	}
	public int getCode() {
		return code;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

    public static Response failValue(String errorMessage) {
		return new Response().code(CodeReturn.fail).msg(errorMessage);
    }

	public static Response successValue(String successMessage) {
		return new Response().code(CodeReturn.success).msg(successMessage);
	}

    public enum ResponseEnum {
        /**
         * code
         */
		code,
        /**
         * msg
         */
		msg,
        /**
         * data
         */
		data
	}

}
