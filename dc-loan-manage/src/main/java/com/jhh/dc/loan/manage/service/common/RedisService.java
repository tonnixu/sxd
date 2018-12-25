
package com.jhh.dc.loan.manage.service.common;

public interface RedisService {


    void saveQueryTotalItem(String type, String keySuffix, long totalCount);

    long selectQueryTotalItem(String type, String field);

    int selectDownloadCount();

    String selectCollertorsUserName(String userId);

    String getRedisByKey(String settlementSwitch);
}
