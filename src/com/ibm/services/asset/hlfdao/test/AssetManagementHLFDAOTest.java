package com.ibm.services.asset.hlfdao.test;

import java.util.ArrayList;
import java.util.List;

import com.ibm.hlfabric.client.HLFabricResponse;
import com.ibm.services.asset.ApplicationConstants;
import com.ibm.services.asset.hlfdao.AssetManagementHLFDAO;
import com.ibm.services.asset.resource.ASNDetails;
import com.ibm.services.asset.resource.LineItem;
import com.ibm.services.asset.resource.ManufReturnRequest;
import com.ibm.utils.CommonUtil;
import com.ibm.utils.PropertyManager;
import com.ibm.utils.RandomDataGenUtil;

public class AssetManagementHLFDAOTest {

	private static final int TIME_OUT = 30000;
	private static List<LineItem> _liIds = null;
	private static List<String> _liIdStrList = null;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		PropertyManager.initProperty(ApplicationConstants.APP_PROPS_BUNDLE, "application.properties", true);
		//
		// getASNDetails("ASN-FUQ-61GTF", ApplicationConstants.BE_WAREHOUSE);
		// --Create and update Flow
		createAndUpdateFlow();
		getLineItemsByStatus(ApplicationConstants.STATUS_SHT_MED,ApplicationConstants.BE_MEDTURN);
		getLineItemByIdFlow();
		createAndUpdateLineItemsFlow();
		getLineItemsStat();
		createMMRFlow();
	}

	private static boolean createMMRFlow() throws Exception {
		String asnNumber = createNewASNFlow();
		String ts = CommonUtil.getTimeStamp();
		String requestNumber = "MFR-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 4, 4 });
		ManufReturnRequest req = buildMFRDetails(requestNumber, ApplicationConstants.STATUS_SHT_MF, ts,
				ApplicationConstants.BE_MEDTURN);
		Thread.sleep(TIME_OUT);
		getASNDetails(asnNumber,ApplicationConstants.BE_MEDTURN);
		Thread.sleep(TIME_OUT);
		
		HLFabricResponse resp = AssetManagementHLFDAO.createMFRRequest(
				"TRXN-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 3, 5 }), req, _liIdStrList,
				ApplicationConstants.BE_MEDTURN);
		Thread.sleep(TIME_OUT);
		if (resp.isOk()) {
			resp = AssetManagementHLFDAO.getMFRRequest(requestNumber, ApplicationConstants.BE_MANUFACTURER);
			if(resp.isOk()){
				System.out.println("After MFR: "+ resp.getMessage());
			}
		}
		return false;
	}

	private static boolean getLineItemsStat() throws Exception {
		createAndUpdateLineItemsFlow();
		Thread.sleep(TIME_OUT);
		HLFabricResponse resp = AssetManagementHLFDAO.getLineitemsCount(ApplicationConstants.BE_MEDTURN);
		if (resp.isOk()) {

			System.out.println(resp.getMessage());
			return true;
		}
		return false;
	}

	private static boolean getLineItemByIdFlow() throws Exception {
		String asnNumber = createNewASNFlow();
		Thread.sleep(TIME_OUT);
		if (asnNumber != null) {
			HLFabricResponse hlfResponse = AssetManagementHLFDAO.getLineitemsById(_liIds.get(0).getString("lineItemId"),
					ApplicationConstants.BE_MANUFACTURER);
			if (hlfResponse.isOk()) {
				System.out.println("Line Item ID:" + hlfResponse.getMessage());
				return true;
			}
		}
		return false;
	}

	private static boolean createAndUpdateFlow() throws Exception {
		String asnNumber = createNewASNFlow();
		if (asnNumber != null) {
			Thread.sleep(TIME_OUT);
			if (getASNDetails(asnNumber, ApplicationConstants.BE_WAREHOUSE)) {
				String trxnId = RandomDataGenUtil.generateRandomUUID(null, new int[] { 4, 3, 4 });
				String ts = CommonUtil.getTimeStamp();
				AssetManagementHLFDAO.updateASNDetails(trxnId, asnNumber, ApplicationConstants.STATUS_RCVD_MED, ts,
						_liIds, ApplicationConstants.BE_MEDTURN);
				Thread.sleep(TIME_OUT);
				return getASNDetails(asnNumber, ApplicationConstants.BE_WAREHOUSE);

			}
		}
		return false;
	}

	private static boolean createAndUpdateLineItemsFlow() throws Exception {
		String asnNumber = createNewASNFlow();
		if (asnNumber != null) {
			Thread.sleep(TIME_OUT);
			if (getASNDetails(asnNumber, ApplicationConstants.BE_WAREHOUSE)) {
				String trxnId = RandomDataGenUtil.generateRandomUUID(null, new int[] { 4, 3, 4 });
				String ts = CommonUtil.getTimeStamp();
				HLFabricResponse resp = AssetManagementHLFDAO.updateLineItemsBulk(trxnId,
						ApplicationConstants.STATUS_RCVD_MED, ts, _liIds.subList(0, _liIds.size() - 1),
						ApplicationConstants.BE_MEDTURN);
				if (resp.isOk()) {
					Thread.sleep(TIME_OUT);
					return getASNDetails(asnNumber, ApplicationConstants.BE_WAREHOUSE);
				}
			}
		}
		return false;
	}

	private static boolean getLineItemsByStatus(String status, String businessEntity) {
		HLFabricResponse resp = AssetManagementHLFDAO.getLineitemsByStatus(status, businessEntity);
		if (resp.isOk()) {
			System.out.println("Line Items Retrieved  ");
			System.out.println(resp.getMessage());
			return true;
		} else {
			System.out.println("Could not find line items for  ...." + status);
			return false;
		}
	}

	private static boolean getASNDetails(String asnNumber, String who) {
		HLFabricResponse resp = AssetManagementHLFDAO.getASNDetails(asnNumber, who);
		if (resp.isOk()) {
			System.out.println("ASN retrived " + asnNumber);
			System.out.println(resp.getMessage());
			return true;
		} else {
			System.out.println("Could not find asn  ...." + asnNumber);
			return false;
		}
	}

	private static String createNewASNFlow() {
		String asnNumber = "ASN-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 3, 5 });
		String ts = CommonUtil.getTimeStamp();
		String who = "WARE_HOUSE";
		String status = ApplicationConstants.STATUS_SHT_MED;
		ASNDetails asnDetails = buildASNDetails(asnNumber, status, ts, who);
		List<LineItem> lineItemList = buildLineItems(asnNumber, 5, status, ts, who);
		HLFabricResponse resp = AssetManagementHLFDAO.createNewASN(asnDetails, lineItemList,
				ApplicationConstants.BE_WAREHOUSE);
		if (resp.isOk()) {
			_liIds = lineItemList;
			System.out.println("ASN created " + asnNumber);
			return asnNumber;
		} else {
			System.out.println("Could not create ....");
			return null;
		}
	}

	private static ASNDetails buildASNDetails(String asnNumber, String status, String ts, String who) {
		ASNDetails asnDetails = new ASNDetails();
		asnDetails.append("asnNumber", asnNumber);
		asnDetails.append("createTimestamp", ts);
		asnDetails.append("updateTimestamp", ts);
		asnDetails.append("updatedBy", who);
		asnDetails.append("status", status);

		return asnDetails;
	}

	private static ManufReturnRequest buildMFRDetails(String requestNumber, String status, String ts, String who) {
		ManufReturnRequest mfrDetails = new ManufReturnRequest();
		mfrDetails.append("requestNumber", requestNumber);
		mfrDetails.append("createTimestamp", ts);
		mfrDetails.append("updateTimestamp", ts);
		mfrDetails.append("updatedBy", who);
		mfrDetails.append("status", status);
		mfrDetails.append("remarks", "UDPATED ");
		

		return mfrDetails;
	}

	private static List<LineItem> buildLineItems(String asnNumber, int count, String status, String ts, String who) {
		List<LineItem> retList = new ArrayList<>(count);
		_liIdStrList = new ArrayList<String>();
		for (int index = 0; index < count; index++) {
			LineItem li = new LineItem();
			String num = String.valueOf(10 * (index + 1));
			String liid = asnNumber + "/" + num;
			_liIdStrList.add(liid);
			li.append("lineItemId", liid);
			li.append("itemId", RandomDataGenUtil.pickupFromList(new String[] { "001", "002", "003", "004" }));
			li.append("description", "TEST DESC");
			li.append("qty", RandomDataGenUtil.pickupFromList(new String[] { "100", "250", "1000", "500" }));
			li.append("unit", "EA");
			li.append("status", status);
			li.append("createTs", ts);
			li.append("updateTs", ts);
			li.append("updatedBy", who);
			retList.add(li);

		}
		return retList;
	}

}
