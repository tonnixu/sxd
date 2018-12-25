package com.jhh.dc.loan.mapper.app;

import java.util.List;
import java.util.Map;

import com.jhh.dc.loan.api.entity.DetailsDo;
import com.jhh.dc.loan.api.entity.ResponseDo;
import com.jhh.dc.loan.entity.app.BorrowStatusLog;
import com.jhh.dc.loan.entity.app_vo.BorrowListVO;
import com.jhh.dc.loan.entity.app_vo.SignInfo;
import org.apache.ibatis.annotations.Param;

import com.jhh.dc.loan.entity.app.BorrowList;
import com.jhh.dc.loan.entity.app.ProdMode;
import com.jhh.dc.loan.entity.app_vo.MyBorrow;
import com.jhh.dc.loan.entity.contract.IdEntity;
import com.jhh.dc.loan.entity.loan_vo.RobotData;
import com.jhh.dc.loan.entity.utils.BorrPerInfo;
import com.jhh.dc.loan.entity.utils.RepaymentDetails;

import tk.mybatis.mapper.common.Mapper;

public interface BorrowListMapper extends Mapper<BorrowList> {

    //根据per_id查询用户当前的的borrow_list
    BorrowList selectNow(Integer per_id);

    //根据per_id查询用户所有borrow_list
    List<BorrowList> selectByPerId(@Param("per_id") Integer per_id);

    //根据per_id,借款状态查询borrow_list
    List<BorrowList> selectBorrowingByPerId(@Param("per_id") Integer per_id, @Param("borr_status") String borr_status);

    List<MyBorrow> getMyBorrowList(String userId, int start, int end);

    List<ProdMode> getProdModeByBorrId(String borrId);


    RepaymentDetails getRepaymentDetails(String borrId);

    BorrPerInfo selectByBorrId(Integer borrId);

    List<BorrowList> getMingtianhuankuanId(String date, String date1);


    int selectDoing(Integer per_id);

    BorrowList selectByBorrNum(String borr_num);

    int rejectManualReview();

    List<BorrowList> getBorrList(Map<String, Object> map);

    List<RobotData> getRobotData();

    List<Integer> syncWhiteList();
    
    List<String> syncPhoneWhiteList();
    /**查询该用户有无借款*/
    List<BorrowList> selectBorrowPay(@Param("perId") Integer per_id);
    /*****根据Id查询身份信息*******/
    IdEntity queryIdentityById(Integer borrId);


    /**
     * 查询结清状态ID
     * @param borrIds
     * @return
     */
    List selectIdsBySettleStatus(List borrIds);

    String getTotalLeft(String borrId);

    BorrowList getBorrowListByBorrId(@Param("id") int i);
    //根据id查询催款人
    String selectCollectionUser(int borrId);
    //根据id把borrow_list催款人置空
    void resetCollectionUser(int borrId);
    /**
     * 查询该用户是否已经放过款
     * @param perId
     * @return
     */
    List<BorrowList> queryBorrListByPerIdAndStauts(Integer perId);

    /**
     *  获取签约页面信息
     * @param borrId
     * @return
     */
    SignInfo getSignInfo(String borrId);

    /**
     *  详情页
     * @param
     * @return
     */
    DetailsDo getDetails(String borrNum);

    List<BorrowListVO> getBorrListVoByPhone(String phone);

    Map<String,String> getContractInfoByBorrId(Integer borrId);

    int insertSelective(BorrowList borrowList);

    String getPersonPhoneByBorrId(Integer borrId);

    List<BorrowList> getBorrowByFinalStatus();

    List<BorrowListVO> getBorrowListVOByPersonId(Integer personId);

    void updateStatusByBorrNum(@Param("borrNum") String borrNum,@Param("status") String status);

    //Mybatis 拦截器使用
    void insertBorrowStatus(BorrowStatusLog borrowStatusLog);

    List<BorrowList> getBorrowByOverdue();

    /**
     * 根据合同id更改合同状态
     * @param borrId
     * @param status
     */
    void updateStatusById(@Param("borrId") Integer borrId, @Param("status") String status);

    /**
     * 查询ID根据状态
     * @param borrIds
     * @return
     */
    List selectIdsByStatus(List borrIds, List status);

    /**
     * 查询用户已完成合同数
     * @param perId
     * @return
     */
    Long getCompletedHistoryCount(Integer perId);

    /**
     * 查询到期合同
     * @return
     */
    List<BorrowList> selectExpireBorrow();
}
