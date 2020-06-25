package com.maxus.tsp.gateway.common.constant;

/**
 * @ClassName FOTAConstant
 * @Description FOTA 功能相关常量
 * @Author zijhm
 * @Date 2019/1/22 16:13
 * @Version 1.0
 **/
public class FOTAConstant {
    //sn长度
    public static final int SERIAL_NUMBER_LENGTH = 16;
    //入参seqNo长度
    public static final int SEQ_NO_LENGTH = 22;
    //版本升级请求下发报文参数长度
//    public static final int VERSION_UPGRADE_DOWN_PARAM_OFFSET = 5;
    public static final int VERSION_UPGRADE_DOWN_PARAM_OFFSET = 12;
    //证书更新请求下发报文参数长度
    public static final int CERTIFICATION_UPGRADE_DOWN_PARAM_OFFSET = 5;
    //TBox接收fota请求入参id长度
    public static final int FOTA_PARAM_ID_OFFSET = 4;
    //TBox接收fota请求入参operate长度
    public static final int FOTA_PARAM_OPERATE_OFFSET = 1;
    //TBox接收fota请求入参DataTime长度
    public static final int FOTA_PARAM_DATA_TIME_OFFSET = 7;

    //版本继续升级请求下发报文参数长度
    public static final int UPGRADE_RESUME_DOWN_PARAM_OFFSET = 4;
    //请求版本更新FileListLength
    public static final int FOTA_PARAM_FILELIST_OFFSET = 2;


    //TBox接收fota请求入参type长度
    public static final int FOTA_PARAM_TYPE_OFFSET = 1;
    //TBox接收fota请求入参size长度
    public static final int FOTA_PARAM_SIZE_OFFSET = 2;
    //TBox接收fota请求入参certification长度
    public static final int FOTA_PARAM_CERTIFICATION_OFFSET = 2;
    //TBOX接收Fota请求入参PinSize的长度
    public static final int FOTA_PIN_SIZE_OFFSET = 1;
    //TBox上报下载进度cmd
    public static final String DOWNLOAD_PROGRESS_CMD = "DOWNLOAD_PROGRESS";
    //TBox上报升级进度cmd
    public static final String UPGRADE_RESULT_CMD = "UPGRADE_RESULT";
}
