package com.ibm.services.asset.hlfdao;

import static com.ibm.services.asset.ApplicationConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.hlfabric.client.HLFabricClient;
import com.ibm.hlfabric.client.HLFabricRequest;
import com.ibm.hlfabric.client.HLFabricResponse;
import com.ibm.services.asset.resource.ASNDetails;
import com.ibm.services.asset.resource.LineItem;
import com.ibm.services.asset.resource.ManufReturnRequest;
import com.ibm.utils.CommonUtil;
import com.ibm.utils.PropertyManager;

public class AssetManagementHLFDAO {

	private static final Logger _LOGGER = Logger.getLogger(AssetManagementHLFDAO.class.getName());

	private static Map<String, Map<String, String>> _credentialMap = null;
	private static String _hyperLedgerUrl_WARE_HOUSE = null;
	private static String _hyperLedgerUrl_MEDTURN = null;
	private static String _hyperLedgerUrl_DISP_COMP = null;
	private static String _hyperLedgerUrl_MANUFACTURER = null;

	private static String _chainCode = null;

	/**
	 * Updates an existing ASN details
	 * 
	 * @param asnNumber
	 *            ASN Number to update
	 * @param status
	 *            Target Status
	 * @param ts
	 *            TimeStamp for update
	 * @param itemList
	 *            List of items
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse updateASNDetails(String txId, String asnNumber, String status, String ts,
			List<LineItem> itemList, String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest getASNDetailsRequest = buildUpdateASNRequest(txId, asnNumber, status, ts, itemList,
						getUserId(businessEntity));
				response = client.invokeMethod(getASNDetailsRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getASNDetails failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getASNDetails:" + ex.getMessage());
		}
		return response;
	}

	/**
	 * Updates an existing line items with a status
	 * 
	 * @param status
	 *            Target Status
	 * @param ts
	 *            TimeStamp for update
	 * @param itemList
	 *            List of items
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse updateLineItemsBulk(String txId, String status, String ts, List<LineItem> itemList,
			String businessEntity) {
		HLFabricResponse response = null;
		boolean isSuccess = true;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				List<HLFabricRequest> updateLineItemRequestList = buildUpdateLineItemsBulk(txId, status, ts, itemList,
						getUserId(businessEntity));
				for (HLFabricRequest request : updateLineItemRequestList) {
					response = client.invokeMethod(request);
					isSuccess = isSuccess && response.isOk();
					if (!isSuccess) {
						break;
					}
					// Giving a deliberate cool off time;
					waitForSync(1000);
				}
				response = new HLFabricResponse(isSuccess);
				response.setMessage(
						isSuccess ? "Line items are updated successfully " : "Line items are updated failure");
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF updateLineItemsBulk failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in updateLineItemsBulk:" + ex.getMessage());
		}
		return response;
	}

	private static void waitForSync(long timeinMillis) {
		try {
			Thread.sleep(timeinMillis);
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception while waiting ", ex);
		}
	}

	/**
	 * Retrieves all line items based on the status
	 * 
	 * @param status
	 *            String
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse getLineitemsByStatus(String status, String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest getASNDetailsRequest = buildGetLineItemsByStatRequest(status,
						getUserId(businessEntity));
				response = client.invokeMethod(getASNDetailsRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getLineitemsByStatus failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getLineitemsByStatus:" + ex.getMessage());
		}
		return response;
	}

	/**
	 * Retrieves all line item count based on status
	 * 
	 * @param status
	 *            String
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse getLineitemsCount(String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest request = buildGetLineItemsStatusReport(getUserId(businessEntity));
				response = client.invokeMethod(request);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getLineitemsCount failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getLineitemsCount:" + ex.getMessage());
		}
		return response;
	}

	/**
	 * Retrieves all line items for input lineItem id
	 * 
	 * @param lineItemId
	 *            String
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse getLineitemsById(String lineItemId, String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest getASNDetailsRequest = buildGetLineItemsIdtRequest(lineItemId,
						getUserId(businessEntity));
				response = client.invokeMethod(getASNDetailsRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getLineitemsById failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getLineitemsById:" + ex.getMessage());
		}
		return response;
	}

	/**
	 * Retrieves ASN details from ASN Number form HL
	 * 
	 * @param asnNumber
	 *            String
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse getASNDetails(String asnNumber, String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest getASNDetailsRequest = buildGetASNDetailsRequest(asnNumber, getUserId(businessEntity));
				response = client.invokeMethod(getASNDetailsRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getASNDetails failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getASNDetails:" + ex.getMessage());
		}
		return response;
	}
	/**
	 * Retrieves MFR details from MFR Number form HL
	 * 
	 * @param mfrRequest
	 *            String
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse getMFRRequest(String mfrRequest, String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest getASNDetailsRequest = buildGetMFRDetailsRequest(mfrRequest, getUserId(businessEntity));
				response = client.invokeMethod(getASNDetailsRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF getMFRRequest failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in getMFRRequest:" + ex.getMessage());
		}
		return response;
	}
	/**
	 * Creates a new MFR request
	 * 
	 * @param asnDetails
	 *            ASNDetails
	 * @param lineItemList
	 *            List<LineItem>
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse createMFRRequest(String trxnId,ManufReturnRequest mfrDetails,List<String> liIdList,
			String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest newMFRRequest = buildNewMFRRequest(trxnId,mfrDetails, liIdList, getUserId(businessEntity));
				response = client.invokeMethod(newMFRRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF createMFRRequest failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in createMFRRequest:" + ex.getMessage());
		}
		return response;
	}
	/**
	 * Creates a new ASN
	 * 
	 * @param asnDetails
	 *            ASNDetails
	 * @param lineItemList
	 *            List<LineItem>
	 * @param businessEntity
	 *            String
	 * @return HLFabricResponse
	 */
	public static HLFabricResponse createNewASN(ASNDetails asnDetails, List<LineItem> lineItemList,
			String businessEntity) {
		HLFabricResponse response = null;
		try {
			HLFabricClient client = getClient(businessEntity);
			if (client.register()) {
				HLFabricRequest newASNRequest = buildNewASNRequest(asnDetails, lineItemList, getUserId(businessEntity));
				response = client.invokeMethod(newASNRequest);
			} else {
				response = new HLFabricResponse(false);
				response.setMessage("User registration failed");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "HLF createNewASN failed ", ex);
			response = new HLFabricResponse(false);
			response.setMessage("Exception in createNewASN:" + ex.getMessage());
		}
		return response;
	}

	/**
	 * Builds a HLF request object for update ASN Status
	 * 
	 * @param trxnId
	 *            String
	 * @param asnNumber
	 *            String
	 * @param status
	 *            String
	 * @param ts
	 *            TimeStamp
	 * @param lineItemList
	 *            List<LineItem>
	 * @param who
	 *            Business Entity
	 * @param userId
	 *            Business Entity Content user
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildUpdateASNRequest(String trxnId, String asnNumber, String status, String ts,
			List<LineItem> lineItemList, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("invoke");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("updateASN");
		List<String> liIdList = new ArrayList<String>();
		for (LineItem li : lineItemList) {
			liIdList.add(li.getString("lineItemId"));
		}
		String payload = CommonUtil.toJsonNoPP(liIdList);
		request.setFunctionArgs(
				new String[] { trxnId, ts, userId, status, "U", "STAT", "Updated status", asnNumber, payload, userId });

		request.setSecureContext(userId);
		return request;
	}

	/**
	 * Builds a HLF request object to update a list of line items with a status
	 * 
	 * @param trxnId
	 *            String
	 * @param status
	 *            String
	 * @param ts
	 *            String
	 * @param lineItemList
	 *            List<LineItem>
	 * @param userId
	 *            String
	 * @return List<HLFabricRequest>
	 */
	private static List<HLFabricRequest> buildUpdateLineItemsBulk(String trxnId, String status, String ts,
			List<LineItem> lineItemList, String userId) {
		List<HLFabricRequest> requestList = new ArrayList<>();
		for (LineItem li : lineItemList) {
			HLFabricRequest request = new HLFabricRequest();
			request.setMethod("invoke");
			request.setChainCodeName(_chainCode);
			request.setCallFunction("updateLineItem");
			String lineItemId = li.getString("lineItemId");
			request.setFunctionArgs(
					new String[] { trxnId, ts, lineItemId, userId, status, "U", "STAT", "Updated status" });
			request.setSecureContext(userId);
			requestList.add(request);
		}
		return requestList;

	}

	/**
	 * Builds a HLF request object for retrieving line items for the input
	 * lineItemId
	 * 
	 * @param lineItemId
	 *            String
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildGetLineItemsIdtRequest(String lineItemId, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("query");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("getLineitem");

		request.setFunctionArgs(new String[] { lineItemId, userId });

		request.setSecureContext(userId);
		return request;
	}

	/**
	 * Builds a HLF request object for retrieving line items counts
	 * 
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildGetLineItemsStatusReport(String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("query");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("getLineitemCountByStatus");

		request.setFunctionArgs(new String[] { userId });

		request.setSecureContext(userId);
		return request;
	}

	/**
	 * Builds a HLF request object for retrieving line items for the input
	 * status
	 * 
	 * @param status
	 *            String
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildGetLineItemsByStatRequest(String status, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("query");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("getLineitemByStatus");

		request.setFunctionArgs(new String[] { status, userId });

		request.setSecureContext(userId);
		return request;
	}

	/**
	 * Builds a HLF request object for retrieving ASN Details
	 * 
	 * @param asnNumber
	 *            String
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildGetASNDetailsRequest(String asnNumber, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("query");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("getASNDetails");

		request.setFunctionArgs(new String[] { asnNumber, userId });

		request.setSecureContext(userId);
		return request;
	}
	/**
	 * Builds a HLF request object for retrieving MFR request
	 * 
	 * @param mfrRequest
	 *            String
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildGetMFRDetailsRequest(String mfrRequest, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("query");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("getMRRDetails");

		request.setFunctionArgs(new String[] { mfrRequest, userId });

		request.setSecureContext(userId);
		return request;
	}
	/**
	 * Builds a HLF request object for new MFR creation
	 * 
	 * @param mfrRequest
	 *            ManufReturnRequest
	 * @param lineItemList
	 *            List<String> to be included as a part of it.
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildNewMFRRequest(String trxnId,ManufReturnRequest mfrRequest, 
			List<String> liIdList, String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("invoke");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("createMRR");
		String idListPayload = CommonUtil.toJsonNoPP(liIdList);
		request.setFunctionArgs(new String[] { mfrRequest.getString("requestNumber"),
				mfrRequest.getString("createTimestamp"),mfrRequest.getString("createTimestamp"),userId,
				 mfrRequest.getString("status"), idListPayload,trxnId , mfrRequest.getString("remarks"),userId});
		_LOGGER.log(Level.INFO, "Payload :  " + idListPayload);
		request.setSecureContext(userId);
		return request;
	}
	/**
	 * Builds a HLF request object for new ASN creation
	 * 
	 * @param asnDetails
	 *            ASNDetails
	 * @param lineItemList
	 *            List<LineItem> to be included as a part of it.
	 * @param userId
	 *            String
	 * @return HLFabricRequest
	 */
	private static HLFabricRequest buildNewASNRequest(ASNDetails asnDetails, List<LineItem> lineItemList,
			String userId) {
		HLFabricRequest request = new HLFabricRequest();
		request.setMethod("invoke");
		request.setChainCodeName(_chainCode);
		request.setCallFunction("createNewASN");
		List<String> liIds = new ArrayList<>();
		for (LineItem li : lineItemList) {
			li.remove("objType");
			li.put("qty", li.get("qty").toString());
			liIds.add(li.getString("lineItemId"));
		}

		String payload = CommonUtil.toJsonNoPP(lineItemList);
		String idListPayload = CommonUtil.toJsonNoPP(liIds);
		request.setFunctionArgs(new String[] { asnDetails.getString("asnNumber"),
				asnDetails.getString("createTimestamp"), asnDetails.getString("updateTimestamp"),
				asnDetails.getString("updatedBy"), asnDetails.getString("status"), payload, userId, idListPayload });
		_LOGGER.log(Level.INFO, "Payload :  " + payload);
		request.setSecureContext(userId);
		return request;
	}

	/**
	 * Returns the HLFClient instance based on the input business entity
	 * 
	 * @param context
	 * @return HLFabricClient
	 */
	private static HLFabricClient getClient(String context) {
		HLFabricClient client = null;
		init();
		if (context.equals(BE_WAREHOUSE)) {
			client = new HLFabricClient(_hyperLedgerUrl_WARE_HOUSE, getUserId(context),
					_credentialMap.get(context).get("secret"));
		} else if (context.equals(BE_MEDTURN)) {
			client = new HLFabricClient(_hyperLedgerUrl_MEDTURN, getUserId(context),
					_credentialMap.get(context).get("secret"));
		} else if (context.equals(BE_MANUFACTURER)) {
			client = new HLFabricClient(_hyperLedgerUrl_MANUFACTURER, getUserId(context),
					_credentialMap.get(context).get("secret"));
		} else {
			client = new HLFabricClient(_hyperLedgerUrl_DISP_COMP, getUserId(context),
					_credentialMap.get(context).get("secret"));

		}
		return client;
	}

	/**
	 * Returns the context and user ids
	 * 
	 * @param context
	 * @return users actual user id
	 */
	private static String getUserId(String context) {
		if (_credentialMap == null) {
			init();
		}
		return _credentialMap.get(context).get("uid");
	}

	/**
	 * Initialize properties and urls
	 */
	private static void init() {
		if (_hyperLedgerUrl_WARE_HOUSE == null) {
			_hyperLedgerUrl_WARE_HOUSE = PropertyManager.getStringProperty(APP_PROPS_BUNDLE,
					HL_URL + "." + BE_WAREHOUSE);
			_hyperLedgerUrl_MEDTURN = PropertyManager.getStringProperty(APP_PROPS_BUNDLE, HL_URL + "." + BE_MEDTURN);
			_hyperLedgerUrl_DISP_COMP = PropertyManager.getStringProperty(APP_PROPS_BUNDLE,
					HL_URL + "." + BE_DISPOAL_COMP);
			_hyperLedgerUrl_MANUFACTURER = PropertyManager.getStringProperty(APP_PROPS_BUNDLE,
					HL_URL + "." + BE_MANUFACTURER);
		}
		if (_chainCode == null) {
			_chainCode = PropertyManager.getStringProperty(APP_PROPS_BUNDLE, HL_CHAIN_CODE_ID);
		}
		if (_credentialMap == null) {
			_credentialMap = new HashMap<String, Map<String, String>>();
			String contexts = PropertyManager.getStringProperty(APP_PROPS_BUNDLE, HL_USER_CONTEXT);
			String userIds = PropertyManager.getStringProperty(APP_PROPS_BUNDLE, HL_USER_ID);
			String secrets = PropertyManager.getStringProperty(APP_PROPS_BUNDLE, HL_USER_SECRET);
			String[] secretList = secrets.split(",");
			String[] userList = userIds.split(",");
			String[] contextList = contexts.split(",");
			for (int index = 0; index < contextList.length; index++) {
				Map<String, String> contextMap = new HashMap<>();
				contextMap.put("uid", userList[index]);
				contextMap.put("secret", secretList[index]);
				_credentialMap.put(contextList[index], contextMap);
			}

		}
	}
}