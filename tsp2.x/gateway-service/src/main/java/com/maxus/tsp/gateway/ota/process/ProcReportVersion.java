package com.maxus.tsp.gateway.ota.process;

import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.EcuVersionItResultInfo;
import com.maxus.tsp.gateway.common.model.fota.EcuVersionTboxReqInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;

/**
*@Title ProcReportVersion.java
*@description Tbox发送报文解析
*@time 2019年2月12日 下午2:57:46
*@author wqgzf
*@version 1.0
**/
public class ProcReportVersion extends BaseOtaProc{
	 // 日志
    private static Logger logger = LogManager.getLogger(BaseOtaProc.class);
    
    private EcuVersionItResultInfo itResult;

	public EcuVersionItResultInfo getItResult() {
		return itResult;
	}

	public void setItResult(EcuVersionItResultInfo itResult) {
		this.itResult = itResult;
	}
    
	 // 记录需要注册确认的tbox
    private static Hashtable<String, ProcReportVersion> getReportVersionCheckList = new Hashtable<String, ProcReportVersion>();

    public static ProcReportVersion getProcReportVersion(String serialNumber){
        return getReportVersionCheckList.get(serialNumber);
    }
    
    /**
     * @param kafkaService
     * @param tboxService
     */
    public ProcReportVersion(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    /**
     * @Description 获取版本更新信息
     * @Data 2019年2月12日下午3:05:15
     * @param requestMsg
     * @return
     */
    public byte[] ReportVersionList(OTAMessage requestMsg){
    	byte[] outData = null;
    	try {
    		byte[] length = new byte[2];
    		EcuVersionTboxReqInfo ecuVersionTboxReqInfo = new EcuVersionTboxReqInfo();
            int dataSize = 0;
            String data = "";
            byte[] paramContent = requestMsg.getParam();
            String tboxSn = requestMsg.getSerialNumber();
            System.arraycopy(paramContent, 0, length, 0, 2);
            dataSize = ByteUtil.getUnsignedInt(length);
            if(0 == dataSize){
                logger.warn("TBox({})上行请求版本更新的ECU列表信息数据长度为0", tboxSn);
            }else{
                byte param[] = new byte[dataSize];
                System.arraycopy(paramContent, 2, param, 0, dataSize);
                data = ByteUtil.byteToHex(param);
            }
            ecuVersionTboxReqInfo.setSn(tboxSn);
            ecuVersionTboxReqInfo.setCmd("REPORT_VERSION");
            ecuVersionTboxReqInfo.setDataSize(dataSize);
            ecuVersionTboxReqInfo.setData(data);
            ecuVersionTboxReqInfo.setSeqNo(getSeqNo());
            ecuVersionTboxReqInfo.setEventTime(System.currentTimeMillis());
            boolean result = kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA, ecuVersionTboxReqInfo, tboxSn);
            //版本更新 IT kafka投递成功
            if (result){
            	getReportVersionCheckList.put(tboxSn, ProcReportVersion.this);
            	long startTime = System.currentTimeMillis();
                logger.info("TBox(SN:{})的请求版本更新信息已经投递给it，等待确认响应。 ", tboxSn);
                // 等待it请求版本更新的返回结果，再回复给tbox，超时等待10s,如果超时
                synchronized (ProcReportVersion.this) {
                	ProcReportVersion.this.wait(OperationConstant.ONLINE_COMMAND_WAIT_TIME);
                }
                // 如果等待超时，直接通知tbox
                long endTime = System.currentTimeMillis();
                if (endTime - startTime >= OperationConstant.ONLINE_COMMAND_WAIT_TIME) {
                    logger.warn("TBox(SN:{})的请求版本更新投递IT超时未回复。 ", tboxSn);
                    result = false;
                } else {
                    logger.info("TBox(SN:{})的请求版本更新投递IT成功回复,开始组包回复TBox。Time:{} ", tboxSn, endTime - startTime);
                    outData = reportVersionRespParamMessage();
                }
            }
    	} catch (Exception e) {
    		logger.error("处理请求版本更新报文解析或组包出错：{}",ThrowableUtil.getErrorInfoFromThrowable(e));
    	}
		return outData;    	
    }

	/**
	 * @Description 组装下发给Tbox的报文
	 * @Data 2019年2月12日下午4:20:02
	 * @return
	 */
	private byte[] reportVersionRespParamMessage() {
		int value = this.itResult.getValue();
		int paramSize = (short) this.itResult.getParamSize();
		String param = this.itResult.getParam();
		byte[] respParam = new byte[6 + paramSize];
        byte[] _id = ByteUtil.int2Byte(value);
        byte[] fileListLength = ByteUtil.short2Byte((short) paramSize);
        byte[] fileList = ByteUtil.hexStringToBytes(param);
        System.arraycopy(_id, 0, respParam, 0, FOTAConstant.FOTA_PARAM_ID_OFFSET);
        System.arraycopy(fileListLength, 0, respParam, FOTAConstant.FOTA_PARAM_ID_OFFSET, FOTAConstant.FOTA_PARAM_FILELIST_OFFSET);
        if(paramSize==0 && StringUtils.isBlank(param)){
            logger.info("IT返回的请求版本更新的文件列表长度和信息为0");
        } else {
            System.arraycopy(fileList, 0, respParam, FOTAConstant.FOTA_PARAM_ID_OFFSET + FOTAConstant.FOTA_PARAM_FILELIST_OFFSET, paramSize);
        }
        return respParam;
	}
}
