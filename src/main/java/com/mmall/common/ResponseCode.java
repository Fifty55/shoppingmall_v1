package com.mmall.common;

import lombok.Getter;

/**
 * Created by yuchi on 3/31/19.
 */
public enum ResponseCode {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    @Getter private final int code;
    @Getter private final String desc;

    ResponseCode(int statusCode, String msg) {
        this.code = statusCode;
        this.desc = msg;
    }

}
