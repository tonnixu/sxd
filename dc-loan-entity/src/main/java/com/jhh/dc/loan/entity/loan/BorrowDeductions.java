package com.jhh.dc.loan.entity.loan;

import com.jhh.id.annotation.IdGenerator;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "b_borrow_deductions")
@Getter @Setter
public class BorrowDeductions implements Serializable {

    @Id
    @IdGenerator(value = "dc_b_borrow_deductions")
    private Integer id;
    private Integer borrId;
    private Integer perId;
    private Integer status;
    private String reason;
    private Date createDate;
    private Date updateDate;
}
