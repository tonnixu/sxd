package com.jhh.dc.loan.api.white;

public interface RiskWhiteService {

    boolean[] isWhite(String... phone);

    boolean isWhite(String phone);

}
