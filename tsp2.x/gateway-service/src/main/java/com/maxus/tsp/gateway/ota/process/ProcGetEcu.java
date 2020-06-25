package com.maxus.tsp.gateway.ota.process;

import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.fota.EcuVersionItResultInfo;
import com.maxus.tsp.gateway.common.model.fota.EcuVersionTboxReqInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;

/**
 * @ClassName     ProcGetEcu.java
 * @Description:    获取ECU信息列表，ota报文解析
 * @Author:         zhuna
 * @CreateDate:     2019/1/22 15:11
 * @Version:        1.0
 */
public class ProcGetEcu extends BaseOtaProc {

    // 日志
    private static Logger logger = LogManager.getLogger(BaseOtaProc.class);

    /**
     * Tbox响应的报文处理结果
     */
    private EcuVersionItResultInfo itResult;

    public EcuVersionItResultInfo getItResult() {
        return itResult;
    }

    public void setItResult(EcuVersionItResultInfo itResult) {
        this.itResult = itResult;
    }

    // 记录需要注册确认的tbox
    private static Hashtable<String, ProcGetEcu> getEcuCheckList = new Hashtable<String, ProcGetEcu>();

    public static ProcGetEcu getProcGetEcu(String serialNumber){
        return getEcuCheckList.get(serialNumber);
    }


    /**
     * @method
     * @description 获取ECU列表信息上行指令解析
     * @param
     * @return
     * @author      zhuna
     * @date        2019/1/22 15:11
     */
    public ProcGetEcu(KafkaService kafkaService, TboxService tboxService) {
        super(kafkaService, tboxService);
    }

    public byte[] checkGetEcuList(OTAMessage requestMsg){
        byte[] outData = null;
        try {
            //ECU数据列表长度
//            byte[] length = new byte[1];
//            EcuVersionTboxReqInfo ecuVersionTboxReqInfo = new EcuVersionTboxReqInfo();
//            int dataSize = 0;
//            String data = "";
//            byte[] paramContent = requestMsg.getParam();
//            String tboxSn = requestMsg.getSerialNumber();
//            System.arraycopy(paramContent, 0, length, 0, 1);
//            dataSize = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(length[0]));
//            if(0 == dataSize){
//                logger.warn("TBox({})上行获取ECU列表信息数据长度为0", tboxSn);
//            }else{
//                byte param[] = new byte[dataSize];
//                System.arraycopy(paramContent, 1, param, 0, dataSize);
//                data = ByteUtil.byteToHex(param);
//            }
            //获取ECU列表
            EcuVersionTboxReqInfo ecuVersionTboxReqInfo = new EcuVersionTboxReqInfo();
            String data = "";
            int dataSize = 0;
            byte[] length = new byte[2];
            byte[] paramContent = requestMsg.getParam();
            System.arraycopy(paramContent, 0, length, 0, 2);
            String tboxSn = requestMsg.getSerialNumber();
            dataSize = ByteUtil.getUnsignedInt(length);
            if(0 == dataSize){
                logger.warn("TBox({})上行获取ECU列表信息数据长度为0", tboxSn);
            } else {
                byte param[] = new byte[dataSize];
                System.arraycopy(paramContent, 2, param, 0, dataSize);
                data = ByteUtil.byteToHex(param);
            }

            ecuVersionTboxReqInfo.setSn(tboxSn);
            ecuVersionTboxReqInfo.setCmd("GET_ECU_LIST");
            ecuVersionTboxReqInfo.setDataSize(dataSize);
            ecuVersionTboxReqInfo.setData(data);
            ecuVersionTboxReqInfo.setSeqNo(getSeqNo());
            ecuVersionTboxReqInfo.setEventTime(System.currentTimeMillis());
            boolean result = kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA, ecuVersionTboxReqInfo, tboxSn);
            //获取ECU列表 IT kafka投递成功
            if (result){
                getEcuCheckList.put(tboxSn, ProcGetEcu.this);
                long startTime = System.currentTimeMillis();
                logger.info("TBox(SN:{})的获取ECU列表信息已经投递给it，等待确认响应。 ", tboxSn);
                // 等待it获取ECU列表的返回结果，再回复给tbox，超时等待10s,如果超时，
                synchronized (ProcGetEcu.this) {
                    ProcGetEcu.this.wait(OperationConstant.ONLINE_COMMAND_WAIT_TIME);
                }
                // 如果等待超时，直接通知tbox，
                long endTime = System.currentTimeMillis();
                if (endTime - startTime >= OperationConstant.ONLINE_COMMAND_WAIT_TIME) {
                    logger.warn("TBox(SN:{})的获取ECU列表信息投递IT超时未回复。 ", tboxSn);
                    result = false;
                } else {
                    logger.info("TBox(SN:{})的获取ECU列表信息投递IT成功回复,开始组包回复TBox。Time:{} ", tboxSn, endTime - startTime);
                    outData = ecuRespParamMessage();
                    logger.info("TBox(SN:{})获取ECU列表信息回复组包成功, 回复报文:{}", tboxSn, ByteUtil.byteToHex(outData));
                }
            }
        }catch (Exception e){
            logger.error("处理获取ECU列表信息报文解析或组包出错：{}",ThrowableUtil.getErrorInfoFromThrowable(e));
        }finally {
            getEcuCheckList.remove(requestMsg.getSerialNumber());
        }
        return outData;
    }

    public byte[] ecuRespParamMessage(){
        int value = this.itResult.getValue();
        int paramSize = (short) this.itResult.getParamSize();
        String param = this.itResult.getParam();
        byte[] respParam = new byte[3 + paramSize];
        //**********    获取ECU列表参数构造       **********
        byte[] isUpdate = new byte[1];
        isUpdate[0] = (byte)value;
        byte[] jsonLength = ByteUtil.short2Byte((short) paramSize);
        byte[] ecuList = ByteUtil.hexStringToBytes(param);
        System.arraycopy(isUpdate, 0, respParam, 0, 1);
        System.arraycopy(jsonLength, 0, respParam, 1, 2);
        if(paramSize == 0 && StringUtils.isBlank(param)) {
            logger.warn("IT返回的请求ECU列表长度和信息为0");
        } else {
            System.arraycopy(ecuList, 0, respParam, 3, paramSize);
        }
        return respParam;
    }
}
