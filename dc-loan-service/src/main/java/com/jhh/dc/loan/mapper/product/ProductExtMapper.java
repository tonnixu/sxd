package com.jhh.dc.loan.mapper.product;


import com.jhh.dc.loan.entity.app.ProductExt;
import org.apache.ibatis.annotations.Param;

public interface ProductExtMapper {
    int insert(ProductExt record);

    int insertSelective(ProductExt record);

    /**
     * 根据产品Id和key查询value
     * @return
     */
    String selectValue(@Param("productId") int productId, @Param("property") String property);
}