package com.jhh.dc.loan.manage.mapper;


import com.jhh.dc.loan.entity.app.ProductCompanyExt;
import org.apache.ibatis.annotations.Param;

public interface ProductCompanyExtMapper {
    int insert(ProductCompanyExt record);

    int insertSelective(ProductCompanyExt record);

    /**
     * 根据产品Id主体和key查询value
     * @return
     */
    String selectValueByProductId(@Param("productId") int productId, @Param("property") String property);
}