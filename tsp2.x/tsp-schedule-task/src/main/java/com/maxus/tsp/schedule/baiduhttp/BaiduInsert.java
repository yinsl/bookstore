package com.maxus.tsp.schedule.baiduhttp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.model.MaintenanceStationInfo;
import com.maxus.tsp.schedule.service.client.DaTong2TspClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: BaiduInsert.java
 * @Description: 每天定时更新百度云数据
 * @author 胡宗明
 * @version V1.0
 * @Date 2017年10月30日 下午1:39:20
 */
@Component
public class BaiduInsert extends HttpCommon {
	private static final Logger logger = LogManager.getLogger(BaiduInsert.class);
	@Autowired
	private DaTong2TspClient datong2tspClient;
	// 百度云创建数据url
	@Value("${baidu.poi.create.url}")
	String baiduCreateUrl;
	// 百度云ak
	@Value("${baidu.ak}")
	String ak;
	// 百度云表的id
	@Value("${baidu.geotable_id}")
	String geotableId;
	// 百度云处理类型
	@Value("${baidu.coord_type}")
	String coordType;
	// 百度云修改数据url
	@Value("${baidu.poi.update.url}")
	String upUrl;
	// 环境标志
	@Value("${environmentLabel}")
	String eLabel;

	// public static void main(String[] args) throws Exception {
	// updategeotable();
	// updateColumn();
	// listColumns();
	// detail();
	// deletePoi2("4102012");
	// listPois();s
	// listP();
	// li();
	// BaiduInsert baiduInsert = new BaiduInsert();
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// HttpEntity reqEntity =null;
	// Map<String, String> baiduMap = new HashMap<String, String>();
	// List<String> oo=new ArrayList<String>();
	//
	// JSONObject dev=JSONObject.fromObject(li());
	// JSONArray array=JSONArray.fromObject(dev.get("pois"));
	// System.out.println(dev.get("pois")+"--"+(dev.get("pois") ==
	// "null")+","+(dev.get("pois") ==
	// null)+","+("".equals(dev.get("pois")))+","+(dev.get("pois").equals(ObjectUtils.NULL)));
	// System.out.println(dev.get("pois")==null ||
	// "".equals(dev.get("pois").toString()) ||
	// "null".equals(dev.get("pois").toString()));
	// System.out.println(dev.get("pois")+"================");
	// if(dev.get("pois") == null || "".equals(dev.get("pois")) ||
	// dev.get("pois").equals(null)){
	// System.out.println(dev.get("pois").equals(null));
	// }else{System.out.println("fuck");}
	// for (int i = 0; i < array.size(); i++) {
	// JSONObject obj=JSONObject.fromObject(array.get(i));
	// oo.add(obj.getString("title"));
	// System.out.println(obj.getString("title"));
	// }
	// String temp = "";
	// for (int i = 0; i < oo.size() - 1; i++)
	// {
	// temp = oo.get(i);
	// for (int j = i + 1; j < oo.size(); j++)
	// {
	// if (temp.equals(oo.get(j)))
	// {
	// System.out.println("第" + (i + 1) + "个跟第" + (j + 1) + "个重复，值是：" + temp);
	// //deletePoi2(temp);
	// }
	// }
	// }
	// }
	// 上传百度云
	@Scheduled(cron = "0 0 0/1 * * ?") // 每1小时执行一次
	private void insertBaidu() throws Exception {
		if ("1".equals(eLabel)) {// 环境标志，只在p环境执行
			BaiduInsert baiduInsert = new BaiduInsert();
			List<NameValuePair> formparams = null;
			List<MaintenanceStationInfo> maintenanceStationInfoList = datong2tspClient.getDAIList();
			HttpEntity reqEntity = null;
			Date date = null;
			Calendar calendar = null;
			Date dt1 = null;
			Date dt2 = null;
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Map<String, String> baiduMap = new HashMap<String, String>();
			JSONObject dev = JSONObject.fromObject(listPois());
			if (dev.get("pois") == null || "".equals(dev.get("pois").toString())
					|| "null".equals(dev.get("pois").toString())) {// 判断百度云数据是否为空，为空直接上传数据库数据
				logger.info("百度云数据空的，直接上传数据");
				for (MaintenanceStationInfo maintenanceStationInfo : maintenanceStationInfoList) {
					if (!"".equals(maintenanceStationInfo.getBd09Lat())
							&& !"".equals(maintenanceStationInfo.getBd09Lng())) {
						formparams = new ArrayList<NameValuePair>();
						formparams
								.add(new BasicNameValuePair("tags", ascNameSplit(maintenanceStationInfo.getAscName())));
						formparams.add(new BasicNameValuePair("title", maintenanceStationInfo.getAscName()));
						formparams.add(new BasicNameValuePair("address", maintenanceStationInfo.getCompanyAddress()));
						formparams.add(new BasicNameValuePair("latitude", maintenanceStationInfo.getBd09Lat()));
						formparams.add(new BasicNameValuePair("longitude", maintenanceStationInfo.getBd09Lng()));
						formparams.add(new BasicNameValuePair("coord_type", coordType));
						formparams.add(new BasicNameValuePair("geotable_id", geotableId));
						formparams.add(new BasicNameValuePair("ak", ak));
						formparams.add(new BasicNameValuePair("reservationTelephone",
								maintenanceStationInfo.getBookingPhone()));
						formparams.add(new BasicNameValuePair("hotline", maintenanceStationInfo.getHotLine()));
						formparams
								.add(new BasicNameValuePair("serviceCall", maintenanceStationInfo.getBusinessPhone()));
						formparams.add(new BasicNameValuePair("postalCode", maintenanceStationInfo.getPostalCode()));
						formparams.add(new BasicNameValuePair("ascFullName", maintenanceStationInfo.getAscFullName()));
						formparams.add(new BasicNameValuePair("ascCode", maintenanceStationInfo.getAscCode()));
						reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
						baiduInsert.post(baiduCreateUrl, reqEntity);
					}
				}
			} else {// 否则对应asccod数据是否有相等，有则更新，无则新增
				logger.info("不为空");
				JSONArray array = JSONArray.fromObject(dev.get("pois"));
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = JSONObject.fromObject(array.get(i));
					baiduMap.put(obj.get("ascCode").toString(), obj.get("id").toString());
				}
				for (MaintenanceStationInfo maintenanceStationInfo : maintenanceStationInfoList) {
					formparams = new ArrayList<NameValuePair>();
					if (baiduMap.containsKey(maintenanceStationInfo.getAscCode())) {
						date = new Date();// 获取当前时间

						calendar = Calendar.getInstance();
						calendar.setTime(date);
						calendar.add(Calendar.DATE, -1);// 当前时间减去一天，即一天前的时间
						dt1 = sdf.parse(maintenanceStationInfo.getSlkUpdateDate());
						dt2 = sdf.parse(sdf.format(calendar.getTime()));
						logger.info(date + "kkkk" + maintenanceStationInfo.getSlkUpdateDate());
						if (dt1.getTime() > dt2.getTime()) {// 因为对应数据库表数据日期只到天，所以只能判断一天之内是否有数据修改
							logger.info("数据库有数据更改");
							formparams.add(
									new BasicNameValuePair("tags", ascNameSplit(maintenanceStationInfo.getAscName())));
							formparams.add(new BasicNameValuePair("title", maintenanceStationInfo.getAscName()));
							formparams
									.add(new BasicNameValuePair("address", maintenanceStationInfo.getCompanyAddress()));
							formparams.add(new BasicNameValuePair("latitude", maintenanceStationInfo.getBd09Lat()));
							formparams.add(new BasicNameValuePair("longitude", maintenanceStationInfo.getBd09Lng()));
							formparams.add(new BasicNameValuePair("geotable_id", geotableId));
							formparams.add(new BasicNameValuePair("ak", ak));
							formparams.add(new BasicNameValuePair("reservationTelephone",
									maintenanceStationInfo.getBookingPhone()));
							formparams.add(new BasicNameValuePair("hotline", maintenanceStationInfo.getHotLine()));
							formparams.add(
									new BasicNameValuePair("serviceCall", maintenanceStationInfo.getBusinessPhone()));
							formparams
									.add(new BasicNameValuePair("postalCode", maintenanceStationInfo.getPostalCode()));
							formparams.add(
									new BasicNameValuePair("ascFullName", maintenanceStationInfo.getAscFullName()));
							formparams.add(new BasicNameValuePair("ascCode", maintenanceStationInfo.getAscCode()));
							formparams.add(
									new BasicNameValuePair("id", baiduMap.get(maintenanceStationInfo.getAscCode())));
							reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");

							baiduInsert.post(upUrl, reqEntity);
						}

					} else {
						logger.info("有新数据或者有格式不对上传不了的数据");
						if (!"".equals(maintenanceStationInfo.getBd09Lat())
								&& !"".equals(maintenanceStationInfo.getBd09Lng())) {
							formparams.add(
									new BasicNameValuePair("tags", ascNameSplit(maintenanceStationInfo.getAscName())));
							formparams.add(new BasicNameValuePair("title", maintenanceStationInfo.getAscName()));
							formparams
									.add(new BasicNameValuePair("address", maintenanceStationInfo.getCompanyAddress()));
							formparams.add(new BasicNameValuePair("latitude", maintenanceStationInfo.getBd09Lat()));
							formparams.add(new BasicNameValuePair("longitude", maintenanceStationInfo.getBd09Lng()));
							formparams.add(new BasicNameValuePair("coord_type", coordType));
							formparams.add(new BasicNameValuePair("geotable_id", geotableId));
							formparams.add(new BasicNameValuePair("ak", ak));
							formparams.add(new BasicNameValuePair("reservationTelephone",
									maintenanceStationInfo.getBookingPhone()));
							formparams.add(new BasicNameValuePair("hotline", maintenanceStationInfo.getHotLine()));
							formparams.add(
									new BasicNameValuePair("serviceCall", maintenanceStationInfo.getBusinessPhone()));
							formparams
									.add(new BasicNameValuePair("postalCode", maintenanceStationInfo.getPostalCode()));
							formparams.add(
									new BasicNameValuePair("ascFullName", maintenanceStationInfo.getAscFullName()));
							formparams.add(new BasicNameValuePair("ascCode", maintenanceStationInfo.getAscCode()));
							reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
							baiduInsert.post(baiduCreateUrl, reqEntity);
						}
						;

					}
				}
			}
		} else {
			return;
		}
	}
	// //去重复
	// @Scheduled(cron = "0 0/59 * * * ?") // 每59分钟执行一次
	// public void toRepeat() throws Exception{
	// List<String> codeList=new ArrayList<String>();
	// JSONObject dev=JSONObject.fromObject(listPois());
	// JSONArray array=JSONArray.fromObject(dev.get("pois"));
	// for (int i = 0; i < array.size(); i++) {
	// JSONObject obj=JSONObject.fromObject(array.get(i));
	// codeList.add(obj.getString("ascCode"));
	// }
	// String temp = "";
	// for (int i = 0; i < codeList.size() - 1; i++)
	// {
	// temp = codeList.get(i);
	// for (int j = i + 1; j < codeList.size(); j++)
	// {
	// if (temp.equals(codeList.get(j)))
	// {
	// //System.out.println("第" + (i + 1) + "个跟第" + (j + 1) + "个重复，值是：" + temp);
	// deletePoi(temp);
	// }
	// }
	// }
	// }
	// 新增数据
	// private void createPoi() throws UnsupportedEncodingException {
	// BaiduInsert baiduInsert = new BaiduInsert();
	// HttpEntity reqEntity=null;
	// String url = "http://api.map.baidu.com/geodata/v3/poi/create";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("title", "达州富强"));
	// formparams.add(new BasicNameValuePair("address",
	// "达州市北外通川区北外肖公庙吴家沟富强汽修厂"));
	// formparams.add(new BasicNameValuePair("longitude", "121.333306"));
	// formparams.add(new BasicNameValuePair("latitude", "31.199079"));
	// formparams.add(new BasicNameValuePair("coord_type", "3"));
	// formparams.add(new BasicNameValuePair("geotable_id", "175898"));
	// formparams.add(new BasicNameValuePair("ak",
	// "WHRTqcC90kSuGni6gDolya0oEGn02PhC"));
	// formparams.add(new BasicNameValuePair("reservationTelephone",
	// "12345678"));
	// formparams.add(new BasicNameValuePair("Hotline", "23456789"));
	// formparams.add(new BasicNameValuePair("serviceCall", "34567890"));
	// formparams.add(new BasicNameValuePair("postalCode", "201104"));
	// formparams.add(new BasicNameValuePair("ascFullName", "达州市通川区富强汽修厂"));
	// formparams.add(new BasicNameValuePair("ascCode", "4109006"));
	// reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	//
	// baiduInsert.post(url,reqEntity);
	//
	//
	//
	// }

	// 查询百度云所有数据
	private String listPois() throws Exception {
		BaiduInsert baiduInsert = new BaiduInsert();

		String url = "http://api.map.baidu.com/geodata/v3/poi/list?geotable_id=" + geotableId + "&ak=" + ak
				+ "&coord_type=" + coordType + "&page_size=10000";
		String a = baiduInsert.httpGet(url);
		System.out.println(a);
		return a;
	}

	// 将ascName进行拆分合并
	private String ascNameSplit(String ascName) {
		String an = "";
		int al = ascName.length();
		try {
			for (int i = 0; i < al; i++) {
				for (int j = i + 1; j <= al; j++) {
					an = an + ascName.subSequence(i, j) + " ";
				}
			}
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return an;
	}

	// private static String li() throws Exception {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url =
	// "http://api.map.baidu.com/geodata/v3/poi/list?geotable_id=175898&ak=eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k&coord_type=3&page_size=10000";
	// String a = baiduInsert.httpGet(url);
	// System.out.println(a);
	// return a;
	// }
	// //更新
	// private void updatePoi() throws UnsupportedEncodingException {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String upurl = "http://api.map.baidu.com/geodata/v3/poi/update";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("geotable_id", "175898"));
	// formparams.add(new BasicNameValuePair("ak",
	// "WHRTqcC90kSuGni6gDolya0oEGn02PhC"));
	// formparams.add(new BasicNameValuePair("id", ""));
	// HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	// baiduInsert.post(upurl,reqEntity);
	// }
	//
	// private void deletePoi(String demp) throws UnsupportedEncodingException {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url = "http://api.map.baidu.com/geodata/v3/poi/delete";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("geotable_id", geotableId));
	// formparams.add(new BasicNameValuePair("ak", ak));
	// formparams.add(new BasicNameValuePair("is_total_del", "1"));
	// formparams.add(new BasicNameValuePair("ascCode", demp));
	// //formparams.add(new BasicNameValuePair("id", "2387686556"));
	// HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	// baiduInsert.post(url,reqEntity);
	// }
	//
	// private static void deletePoi2(String t) throws
	// UnsupportedEncodingException {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url = "http://api.map.baidu.com/geodata/v3/poi/delete";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("geotable_id", "175898"));
	// formparams.add(new BasicNameValuePair("ak",
	// "eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k"));
	// formparams.add(new BasicNameValuePair("is_total_del", "1"));
	// //formparams.add(new BasicNameValuePair("ascCode", t));
	// //formparams.add(new BasicNameValuePair("id", "2387686556"));
	// HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	// baiduInsert.post(url,reqEntity);
	// }
	//
	// private static void updateColumn() throws UnsupportedEncodingException {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url = "http://api.map.baidu.com/geodata/v3/column/update";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("geotable_id", "175898"));
	// formparams.add(new BasicNameValuePair("ak",
	// "eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k"));
	// formparams.add(new BasicNameValuePair("id", "308806"));
	// formparams.add(new BasicNameValuePair("is_sortfilter_field", "0"));
	// formparams.add(new BasicNameValuePair("is_search_field", "1"));
	// formparams.add(new BasicNameValuePair("is_index_field", "1"));
	// formparams.add(new BasicNameValuePair("is_unique_field", "1"));
	// HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	// baiduInsert.post(url,reqEntity);
	// }
	//
	// //修改表
	// private static void updategeotable() throws UnsupportedEncodingException
	// {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url = "http://api.map.baidu.com/geodata/v3/geotable/update";
	//
	// List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	// formparams.add(new BasicNameValuePair("id", "175898"));
	// formparams.add(new BasicNameValuePair("ak",
	// "eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k"));
	// formparams.add(new BasicNameValuePair("is_published", "1"));
	// HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");
	//
	// baiduInsert.post(url,reqEntity);
	// }
	////
	// private static void listColumns() throws Exception {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url =
	// "http://api.map.baidu.com/geodata/v3/column/list?geotable_id=175898&ak=eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k";
	// String a = baiduInsert.httpGet(url);
	// System.out.println(a);
	// }
	// //查询指定id表
	// private static void detail() throws Exception {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url =
	// "http://api.map.baidu.com/geodata/v3/geotable/detail?id=175898&ak=eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k";
	// String a = baiduInsert.httpGet(url);
	// System.out.println(a);
	// }
	//
	//
	// private static void listP() throws Exception {
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url =
	// "http://api.map.baidu.com/geodata/v3/poi/list?geotable_id=175898&ak=eHpnSZfbckB1SEfjVDmWc6rLQ8GCLV9k&coord_type=3&ascCode=4101001";
	// String a = baiduInsert.httpGet(url);
	//
	// System.out.println(a);
	// }
	//
	// private static void sogoulist() throws Exception{
	// BaiduInsert baiduInsert = new BaiduInsert();
	//
	// String url =
	// "http://api.map.baidu.com/geodata/v3/geotable/detail?id=175898&ak=WHRTqcC90kSuGni6gDolya0oEGn02PhC";
	// String a = baiduInsert.httpGet(url);
	//
	// System.out.println(a);
	// }
	//
}
