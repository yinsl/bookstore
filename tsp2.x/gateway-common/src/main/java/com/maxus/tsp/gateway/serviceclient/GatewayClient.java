package com.maxus.tsp.gateway.serviceclient;

import com.maxus.tsp.gateway.common.model.BlueToothRequestInfo;
import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmReq;
import com.maxus.tsp.gateway.common.model.UpLoadFileMo;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 网关自身feign调用接口，主要是用于接收RVM控制请求
 *
 * @author uwczo
 */
@FeignClient("tsp-gateway-service")
public interface GatewayClient {

    /**
     * RVM远程控制接口
     * @param serialNumber
     * @param comd
     * @param value
     * @return
     */
    //@RequestMapping(value = "/tsp/appapi/remoteControlRVM", method = RequestMethod.GET)
//	public AppJsonResult remoteControlRVM(@RequestParam(value = "serialNumber") String serialNumber,
//                                          @RequestParam(value = "comd") String comd,
//                                          @RequestParam(value = "value") String value);

    /**
     * RVM拍照接口
     *
     * @param tboxSN
     * @param cameraList
     * @return
     */
    @RequestMapping(value = "/tsp/appapi/takePhotoForRVM", method = RequestMethod.GET)
    public AppJsonResult takePhotoForRVM(@RequestParam("tboxSN") String tboxSN, @RequestParam("cameraList") String cameraList);

    /**
     * RVM远程配置
     *
     * @param comd
     * @param value
     * @param serialNum
     * @param eventTime
     * @param seqNo
     * @param vin
     * @return
     */
    @RequestMapping(value = "/tsp/api/remoteConfigRVM", method = RequestMethod.GET)
    public AppJsonResult remoteConfigRVM(@RequestParam("comd") String comd, @RequestParam("value") String value, @RequestParam("serialNum") String serialNum, @RequestParam("eventTime") long eventTime, @RequestParam("seqNo") String seqNo, @RequestParam("vin") String vin);

    /**
     * POI接口
     *
     * @param serialNumber
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    @RequestMapping(value = "/tsp/appapi/poiConfigRVM", method = RequestMethod.GET)
    public AppJsonResult poiConfigRVM(@RequestParam("serialNumber") String serialNumber,
                                      @RequestParam("gPSType") int gPSType,
                                      @RequestParam("posType") int posType,
                                      @RequestParam("longitude") int longitude,
                                      @RequestParam("latitude") int latitude,
                                      @RequestParam("address") String address);

    /**
     * 国家平台直连操作
     *
     * @param serialNumber
     * @param value
     * @param eventTime
     * @return
     */
    @RequestMapping(value = "/tsp/appapi/directReportRVM", method = RequestMethod.GET)
    public AppJsonResult directReportRVM(@RequestParam("serialNumber") String serialNumber, @RequestParam("value") String value, @RequestParam("eventTime") long eventTime);

    /**
     * 蓝牙操作
     *
     * @param blueToothInfo
     */
    @RequestMapping(value = "/tsp/api/blueToothRVM", method = RequestMethod.POST)
    public AppJsonResult blueToothRVM(@RequestBody BlueToothRequestInfo blueToothInfo);


    /**
     * TBOX远程升级
     *
     * @param tboxUpdateRvmReq
     * @return
     */
    @RequestMapping(value = "/tsp/appapi/tboxUpdateRvm", method = RequestMethod.POST)
    public AppJsonResult tboxRemoteUpdateRVM(@RequestBody TboxUpdateRvmReq tboxUpdateRvmReq);

    /**
     * TBOX获取车况
     *
     * @param sn
     * @param value
     */
    @RequestMapping(value = "/tsp/appapi/tboxGetVehicleStatusRvm", method = RequestMethod.GET)
    public AppJsonResult tboxGetVehicleStatus(@RequestParam("sn") String sn, @RequestParam("value") String value, @RequestParam("eventTime") long eventTime);

    /**
     * 文件下载
     *
     * @param downLoadFile
     * @return
     */
    @RequestMapping(value = "/tsp/api/downLoadFileRVM", method = RequestMethod.POST)
    public AppJsonResult tboxDownLoadFileRVM(@RequestBody DownLoadFileMo downLoadFile);

    /**
     * 文件上传
     *
     * @param upLoadFile
     * @return
     */
    @RequestMapping(value = "/tsp/api/upLoadFileRVM", method = RequestMethod.POST)
    public AppJsonResult tboxUpLoadFileRVM(@RequestBody UpLoadFileMo upLoadFile);
}
