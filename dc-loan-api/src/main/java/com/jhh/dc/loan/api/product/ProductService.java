package com.jhh.dc.loan.api.product;

import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.app.Banner;
import com.jhh.dc.loan.entity.app.Product;

import java.util.List;
import java.util.Map;

/**
 *  产品相关操作
 */
public interface ProductService {

    /**
     *  根据设备查询产品信息 和按钮信息
     * @param device 产品Id
     * @return
     */
    public ResponseDo<Map<String,Object>> getProduct(String device);
    ResponseDo<Map<String,Object>> getProduct(String device, String productId);

    public ResponseDo<List<Product>> getProductAll();

    /**
     * 删除产品
     * @param productId
     * @return
     */
    public ResponseDo delProduct(String productId);

    /**
     *  添加新产品
     * @param product
     * @return
     */
    public ResponseDo saveProduct(Product product);

    /**
     * 修改产品信息
     * @param product
     * @return
     */
    public ResponseDo updateProduct(Product product);


    public ResponseDo<Map<String,Object>> getBanner();
}
