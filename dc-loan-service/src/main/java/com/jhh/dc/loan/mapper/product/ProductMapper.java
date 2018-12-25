package com.jhh.dc.loan.mapper.product;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;

import com.jhh.dc.loan.entity.app.Product;
import com.jhh.dc.loan.entity.app_vo.SignInfo;

/**
 * 2017/12/28.
 */
public interface ProductMapper extends Mapper<Product> {

   public List<Product> selectByDevice(String device);

   public List<Product> getProductAll();

   public SignInfo getSignInfo(String per_id);

}
