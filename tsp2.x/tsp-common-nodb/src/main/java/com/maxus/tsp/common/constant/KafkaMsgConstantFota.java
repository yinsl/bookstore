package com.maxus.tsp.common.constant;

/**
 * @ClassName     KafkaMsgConstantFota.java
 * @Description:   系统常量，用于Fota模块，主要包括和IT kafka投递数据的Topic定义
 * @Author:         zhuna
 * @CreateDate:     2019/1/23 14:33
 * @Version:        1.0
 */
public class KafkaMsgConstantFota {

    //网关向it投递信息，主要是ECU列表、请求版本更新
    public static final String TOPIC_IT_INFORMATION_DATA = "information_data";
    //it信息返回，主要是ECU列表、请求版本更新
    public static final String TOPIC_IT_INFORMATION_DATA_RESULT = "information_data_result";
    //网关自身topic,主要是ECU列表
//    public static final String TOPIC_SELF_INFORMATION_DATA_UP = "information-data-up-topic";
    public static final String TOPIC_SELF_INFORMATION_DATA_DOWN = "information-data-down-topic";
    //网关自身topic,主要是请求版本更新
//    public static final String TOPIC_SELF_REPORT_VERSION_UP = "report-version-up-topic";
    public static final String TOPIC_SELF_REPORT_VERSION_DOWN = "report-version-down-topic";

    //平台请求网关TBox版本查询主题
    public static final String TOPIC_IT_VERSION_QUERY_REQ = "version_query_request";
    //平台请求网关TBox版本查询结果返回主题
    public static final String TOPIC_IT_VERSION_QUERY_RESP = "version_query_response";
    //网关kafka topic用于版本查询内部通信
    public static final String TOPIC_SELF_VERSION_QUERY_DOWN = "version_query_down";
    public static final String TOPIC_SELF_VERSION_QUERY_UP = "version_query_up";

    //平台请求网关车主答复下发主题(版本更新请求)
    public static final String TOPIC_IT_VERSION_UPGRADE_REQ = "version_upgrade_request";
    //平台请求收到车主答复的应答结果返回主题(版本更新)
    public static final String TOPIC_IT_VERSION_UPGRADE_RESP = "version_upgrade_response";
    //版本升级请求 网关kafka
    public static final String TOPIC_SELF_VERSION_UPGRADE_UP = "version_upgrade_up";
    public static final String TOPIC_SELF_VERSION_UPGRADE_DOWN = "version_upgrade_down";


    //平台请求网关继续升级下发主题
    public static final String TOPIC_IT_UPGRADE_RESUME_REQ = "upgrade_resume_request";
    //平台请求网关继续升级结果返回主题
    public static final String TOPIC_IT_UPGRADE_RESUME_RESP = "upgrade_resume_response";
    //继续升级请求 网关kafka
    public static final String TOPIC_SELF_UPGRADE_RESUME_UP = "upgrade_resume_up";
    public static final String TOPIC_SELF_UPGRADE_RESUME_DOWN = "upgrade_resume_down";

    //平台请求网关证书更新主题
    public static final String TOPIC_IT_CERTIFICATION_UPGRADE_REQ = "certificate_upgrade_request";
    //平台请求网关证书更新返回主题
    public static final String TOPIC_IT_CERTIFICATION_UPGRADE_RESP ="certificate_upgrade_response";
    //证书更新 网关kafka
    public static final String TOPIC_SELF_CERTIFICATION_UPGRADE_UP ="certificate_upgrade_up";
    public static final String TOPIC_SELF_CERTIFICATION_UPGRADE_DOWN ="certificate_upgrade_down";

    //平台请求网关车主avn端是否同意升级主题
    public static final String TOPIC_IT_AGREE_UPGRADE = "agree_upgrade";
    //平台请求网关车主avn端是否同意升级返回主题
    public static final String TOPIC_IT_AGREE_UPGRADE_RESULT = "agree_upgrade_result";
    //是否同意升级 网关kafka
//    public static final String TOPIC_SELF_AGREE_UPGRADE_UP = "agree_upgrade_up";
    public static final String TOPIC_SELF_AGREE_UPGRADE_DOWN = "agree_upgrade_down";

    //网关向it投递信息，主要是请求获取PIN码
    public static final String TOPIC_IT_GET_PIN = "get_pin";
    //it信息返回，主要是PIN码信息
    public static final String TOPIC_IT_GET_PIN_RESULT = "get_pin_result";
    //网关自身topic,主要是PIN码
//    public static final String TOPIC_SELF_GET_PIN_UP = "get_pin_up";
    public static final String TOPIC_SELF_GET_PIN_DOWN = "get_pin_down";



    //进度上报主题, 包含下载进度上报以及升级进度上报
    public static final String TOPIC_IT_PROGRESS_REPORT = "progress_report";
    //进度上报结果返回主题, 包含下载进度上报结果以及升级进度上报结果
    public static final String TOPIC_IT_PROGRESS_REPORT_RESULT = "progress_report_result";
    //进度上报网关kafka 主要用于接收与TBox不在同一节点时进行广播
    public static final String TOPIC_SELF_PROGRESS_REPORT_DOWN = "progress_report_down";
}
