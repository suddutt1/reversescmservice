package com.ibm.services.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.hlfabric.client.HLFabricResponse;
import com.ibm.services.asset.dao.AssetDataDAO;
import com.ibm.services.asset.hlfdao.AssetManagementHLFDAO;
import com.ibm.services.asset.resource.ASNDetails;
import com.ibm.services.asset.resource.ManufReturnRequest;
import com.ibm.services.asset.resource.LineItem;
import com.ibm.services.common.ServiceResponse;
import com.ibm.services.common.Status;
import com.ibm.utils.CommonUtil;
import com.ibm.utils.RandomDataGenUtil;


@Path("data")
public class AssetProcessingService {

	private static Logger _LOGGER = Logger.getLogger(AssetProcessingService.class.getName());
	private static final boolean _saveInDB = false;
	private static Map<String, String> _STATUS_MAP = new HashMap<>();
	private static Map<String, String> _DEFAULT_STAT = new HashMap<>();

	static {
		_STATUS_MAP.put("STATUS_SHT_MED", "Line shipped to Disposal Company");
		_STATUS_MAP.put("STATUS_RCVD_MED", "Line received at Disposal Company");
		_STATUS_MAP.put("STATUS_INP_MED", "Inspection in progress at Disposal Company");
		_STATUS_MAP.put("STATUS_SHT_MF", "Shipped to Manufacturer");
		_STATUS_MAP.put("STATUS_RCVD_MF", "Received at Manufacturer");
		_STATUS_MAP.put("STATUS_SHT_DD", "Shipped for Destroy/Donate");
		_STATUS_MAP.put("STATUS_RCVD_DD", "Line received at Destroy/Donate");
		_STATUS_MAP.put("STATUS_DESTORY", "Destroyed or Donated");
		_STATUS_MAP.put("STATUS_RET_WH", "Returned to Warehouse");
		_STATUS_MAP.put("STATUS_RCVD_WH", "Received at Warehouse");

		_DEFAULT_STAT.put("STATUS_SHT_MED", "0");
		_DEFAULT_STAT.put("STATUS_RCVD_MED", "0");
		_DEFAULT_STAT.put("STATUS_INP_MED", "0");
		_DEFAULT_STAT.put("STATUS_SHT_MF", "0");
		_DEFAULT_STAT.put("STATUS_RCVD_MF", "0");
		_DEFAULT_STAT.put("STATUS_SHT_DD", "0");
		_DEFAULT_STAT.put("STATUS_RCVD_DD", "0");
		_DEFAULT_STAT.put("STATUS_DESTORY", "0");
		_DEFAULT_STAT.put("STATUS_RET_WH", "0");
		_DEFAULT_STAT.put("STATUS_RCVD_WH", "0");

	}

	/**
	 * Probes the service for configuration check up.
	 * @return Response
	 */
	@GET
	@Path("probe")
	@Produces(MediaType.APPLICATION_JSON)
	public Response probe(){ 
		ServiceResponse resp = null;
		HLFabricResponse hlfResponse = null;
		try {
			hlfResponse = AssetManagementHLFDAO.getLineitemsCount(ApplicationConstants.BE_DISPOAL_COMP);
			if (hlfResponse.isOk()) {
				resp = new ServiceResponse(Status.SUCCESS, "Probed succesfully ", hlfResponse.getMessage());
			} else {

				resp = new ServiceResponse(Status.SUCCESS, "Block chain not responding ",
						hlfResponse.getMessage());
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in probe", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side probe",
					CommonUtil.serializeThowable(ex));
		}
		return Response.ok().entity(resp).build();
	}
	/**
	 * Returns line item status statistics
	 * 
	 * @return Response
	 */
	@GET
	@Path("lineStatictics")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLineStatictics() {
		ServiceResponse resp = null;
		HLFabricResponse hlfResponse = null;
		try {
			hlfResponse = AssetManagementHLFDAO.getLineitemsCount(ApplicationConstants.BE_DISPOAL_COMP);
			if (hlfResponse.isOk()) {
				resp = new ServiceResponse(Status.SUCCESS, "Status retrieved successfully ", hlfResponse.getMessage());
			} else {

				resp = new ServiceResponse(Status.SUCCESS, "Status could not be retrieved successfully ",
						_DEFAULT_STAT);
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in getLineStatus", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side getLineitems",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Retrieves line items based on various search criteria
	 * 
	 * @param postBody
	 *            String
	 * @return Response
	 */
	@POST
	@Path("getLineitems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLineItems(String postBody) {
		ServiceResponse resp = null;
		HLFabricResponse hlfResponse = null;
		try {
			JsonObject requestObject = CommonUtil.fromJson(postBody, JsonObject.class);
			String searchType = requestObject.get("srchType").getAsString();
			String value = requestObject.get("value").getAsString();
			if (searchType != null && value != null) {
				List<LineItem> list = null;
				if ("STAT".equals(searchType) && _STATUS_MAP.containsKey(value)) {
					// list =
					// AssetDataDAO.getAllLineItemsforStatus(_STATUS_MAP.get(value));
					hlfResponse = AssetManagementHLFDAO.getLineitemsByStatus(_STATUS_MAP.get(value),
							ApplicationConstants.BE_MEDTURN);
					if (hlfResponse.isOk()) {
						list = paseLineItemArryFromHLFResponse(hlfResponse.getMessage());
					}
				} else if ("LI_ID".equals(searchType)) {
					// list = AssetDataDAO.getLineItems(value);
					hlfResponse = AssetManagementHLFDAO.getLineitemsById(value, ApplicationConstants.BE_MEDTURN);
					if (hlfResponse.isOk()) {
						LineItem li = CommonUtil.fromJson(hlfResponse.getMessage(), LineItem.class);
						list = new ArrayList<>();
						list.add(li);
					}
				} else if ("ASN_NO".equals(searchType)) {
					// list = AssetDataDAO.getAllLineItemsforASN(value);
					hlfResponse = AssetManagementHLFDAO.getASNDetails(value, ApplicationConstants.BE_MEDTURN);
					if (hlfResponse.isOk()) {
						list = paseLineItemArryFromHLFResponse(hlfResponse.getMessage());
					}
				} else if ("MFR_REQ_NO".equals(searchType)) {
					// list = AssetDataDAO.getAllLineItemsforDispReq(value);
					hlfResponse = AssetManagementHLFDAO.getMFRRequest(value.trim(),
							ApplicationConstants.BE_MANUFACTURER);
					if (hlfResponse.isOk()) {
						list = buildMFRDetailsFromFabricResponse(hlfResponse.getMessage());
					}
				} else {
					resp = new ServiceResponse(Status.INVALID_INPUT,
							"Invalid seararch status. Valid values are (STATUS,LI_ID,ASN_NO)", "");
				}
				if (list != null && list.size() > 0) {
					resp = new ServiceResponse(Status.SUCCESS, "Successful search", list);
				} else {
					resp = new ServiceResponse(Status.NOT_FOUND, "No results found", "");
				}

			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Invalid input parameter", "");
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in getLineitems", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side getLineitems",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Parse line item details from HL Response
	 * 
	 * @param input
	 *            JSON String
	 * @return List<LineItem>
	 */
	private List<LineItem> paseLineItemArryFromHLFResponse(String input) {
		JsonObject rslt = CommonUtil.fromJson(input, JsonObject.class);
		JsonArray lineItemJSON = rslt.getAsJsonArray("itemDetail");
		LineItem[] lineItemArray = CommonUtil.fromJson(CommonUtil.toJson(lineItemJSON), LineItem[].class);
		List<LineItem> list = Arrays.asList(lineItemArray);
		return list;
	}

	

	/**
	 * Returns ASN details based on input ASN number
	 * 
	 * @param asnNumber
	 *            String
	 * @return
	 */
	@GET
	@Path("asndetails/{asnNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response asndetailsById(@PathParam("asnNumber") String asnNumber) {
		ServiceResponse resp = null;
		try {
			if (asnNumber != null && asnNumber.trim().length() > 0) {
				// ASNDetails asnInformation =
				// AssetDataDAO.getASNDetails(asnNumber.trim());
				HLFabricResponse response = AssetManagementHLFDAO.getASNDetails(asnNumber.trim(),
						ApplicationConstants.BE_WAREHOUSE);
				if (response.isOk()) {
					ASNDetails asnDetails = buildASNDetailsFromFabricResponse(response.getMessage());
					resp = new ServiceResponse(Status.SUCCESS, "Successful search", asnDetails);
				} else {
					resp = new ServiceResponse(Status.NOT_FOUND, "No results found", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Invalid status", "");
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in asndetailsById", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side asndetailsById",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * @param jsonString
	 *            From Fabric transaction
	 * @return ASNDetails
	 */
	private ASNDetails buildASNDetailsFromFabricResponse(String jsonString) {
		JsonObject requestObject = CommonUtil.fromJson(jsonString, JsonObject.class);
		JsonObject asn = requestObject.getAsJsonObject("asnDetail");
		JsonArray lineItems = requestObject.getAsJsonArray("itemDetail");
		ASNDetails asnDetails = CommonUtil.fromJson(asn, ASNDetails.class);
		List<String> lineItemIdList = new ArrayList<>();

		for (int index = 0; index < lineItems.size(); index++) {
			JsonObject lineItem = lineItems.get(index).getAsJsonObject();
			lineItemIdList.add(lineItem.get("lineItemId").getAsString());
		}
		asnDetails.append("lineItemIds", lineItemIdList);
		return asnDetails;
	}

	/**
	 * Creates a new ASN
	 * 
	 * @param postBody
	 *            Post body in json format
	 * @return Response
	 */
	@POST
	@Path("createNewASN")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewASN(String postBody) {
		ServiceResponse resp;
		try {
			JsonObject requestObject = CommonUtil.fromJson(postBody, JsonObject.class);
			JsonObject asn = requestObject.getAsJsonObject("asnDetails");
			JsonArray lineItems = requestObject.getAsJsonArray("lineItems");
			List<LineItem> lineItemList = new ArrayList<>();
			for (int index = 0; index < lineItems.size(); index++) {
				JsonObject lineItem = lineItems.get(index).getAsJsonObject();
				_LOGGER.log(Level.INFO, lineItem.toString());
				LineItem li = CommonUtil.fromJson(lineItem, LineItem.class);
				li.put("createTs", CommonUtil.getTimeStamp());
				li.put("updateTs", CommonUtil.getTimeStamp());
				li.put("updatedBy", ApplicationConstants.BE_WAREHOUSE);
				li.put("status", ApplicationConstants.STATUS_SHT_MED);
				lineItemList.add(li);
			}
			if (asn != null && lineItems != null) {
				// Add the time stamps etc
				ASNDetails asnDetails = CommonUtil.fromJson(asn, ASNDetails.class);
				asnDetails.put("createTimestamp", CommonUtil.getTimeStamp());
				asnDetails.put("updateTimestamp", CommonUtil.getTimeStamp());
				asnDetails.put("updatedBy", ApplicationConstants.BE_WAREHOUSE);
				asnDetails.put("status", ApplicationConstants.STATUS_SHT_MED);
				// Save this

				boolean dbSaveSuccess = true;
				if (_saveInDB) {
					dbSaveSuccess = AssetDataDAO.saveASNDetails(asnDetails);
					dbSaveSuccess = dbSaveSuccess && AssetDataDAO.saveLineItems(lineItemList);
				}
				// Now saving in Block chain
				if (dbSaveSuccess) {
					if (AssetManagementHLFDAO.createNewASN(asnDetails, lineItemList, ApplicationConstants.BE_WAREHOUSE)
							.isOk()) {
						resp = new ServiceResponse(Status.SUCCESS, "ASN Created successfully", "");
					} else {
						resp = new ServiceResponse(Status.FAILED_HLF, "Failed to store ASN is Hyperledger Fabric", "");
					}
				} else {
					resp = new ServiceResponse(Status.FAILED_DB, "Failed to store Shipment data is database", "");
				}

			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "ASN or Line Items are missing", "");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in createNewASN", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side createNewASN",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Updates an existing ASN
	 * 
	 * @param postBody
	 *            Post body in json format
	 * @return Response
	 */
	@POST
	@Path("updateASN")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateASN(String postBody) {
		ServiceResponse resp;
		try {
			JsonObject jsObject = CommonUtil.fromJson(postBody, JsonObject.class);
			jsObject.remove("objType");
			jsObject.remove("_id");

			ASNDetails requestObject = CommonUtil.fromJson(CommonUtil.toJson(jsObject), ASNDetails.class);
			_LOGGER.info("Post body received" + postBody);
			if (requestObject != null) {
				// Add the time stamps etc
				String updTS = CommonUtil.getTimeStamp();
				String targetStat = requestObject.getString("status");
				String who = requestObject.getString("updatedBy");
				_LOGGER.info("Status received : " + targetStat);
				// First remove unnecessary fields

				requestObject.remove("createTimestamp");

				List<String> lineItems = (List<String>) requestObject.remove("lineItemIds");

				requestObject.put("updateTimestamp", updTS);
				requestObject.put("status", _STATUS_MAP.get(targetStat));
				// Create the line item ids
				List<LineItem> lineItemList = new ArrayList<>();
				for (String lineItemId : lineItems) {
					LineItem li = new LineItem();
					li.put("lineItemId", lineItemId);
					li.put("updateTs", updTS);
					li.put("updatedBy", who);
					li.put("status", _STATUS_MAP.get(targetStat));
					lineItemList.add(li);
				}
				// Save this

				boolean isSaveDBSuccess = true;
				if (_saveInDB) {
					isSaveDBSuccess = AssetDataDAO.updateASN(requestObject);
					isSaveDBSuccess = isSaveDBSuccess && AssetDataDAO.updateLineItems(lineItemList);
				}
				if (isSaveDBSuccess) {
					HLFabricResponse hlfResponse = AssetManagementHLFDAO.updateASNDetails(
							"TRXN-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 3, 5 }),
							requestObject.getString("asnNumber"), _STATUS_MAP.get(targetStat), updTS, lineItemList,
							who);
					if (hlfResponse.isOk()) {
						resp = new ServiceResponse(Status.SUCCESS, "ASN upated successfully", "");
					} else {
						resp = new ServiceResponse(Status.FAILED_HLF, "ASN upation in HL Fabric failed", "");
					}
				} else {
					resp = new ServiceResponse(Status.FAILED_DB, "Failed to update in DB", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "ASN or Line Items are missing", "");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in updateASN", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side updateASN", ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Updates the line items
	 * 
	 * @param postBody
	 *            String
	 * @return Response
	 */
	@POST
	@Path("updateLineItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLineItems(String postBody) {
		ServiceResponse resp;
		try {
			_LOGGER.info("Post body received" + postBody);
			JsonObject requestObject = CommonUtil.fromJson(postBody, JsonObject.class);
			String mode = requestObject.get("mode").getAsString();
			if ("UPD_STATUS".equals(mode)) {
				JsonArray lineItems = requestObject.getAsJsonArray("lineitemList");
				String stat = requestObject.get("status").getAsString();
				String who = requestObject.get("who").getAsString();
				String updTS = CommonUtil.getTimeStamp();
				if (_STATUS_MAP.containsKey(stat)) {
					String targetStatus = _STATUS_MAP.get(stat);

					List<LineItem> updateLineItemList = new ArrayList<>();
					for (int index = 0; index < lineItems.size(); index++) {
						LineItem li = new LineItem();
						String lineItemId = lineItems.get(index).getAsString();
						li.put("lineItemId", lineItemId);
						li.put("updateTs", updTS);
						li.put("updatedBy", who);
						li.put("status", targetStatus);
						updateLineItemList.add(li);
					}
					// Save this
					boolean isSaveDBSuccess = true;
					if (_saveInDB) {
						isSaveDBSuccess = AssetDataDAO.updateLineItems(updateLineItemList);
					}
					if (isSaveDBSuccess) {
						HLFabricResponse hlfResponse = AssetManagementHLFDAO.updateLineItemsBulk(
								"TRXN-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 3, 5 }), targetStatus,
								updTS, updateLineItemList, who);
						if (hlfResponse.isOk()) {
							resp = new ServiceResponse(Status.SUCCESS,
									"Line items are updated succesfully at Hyperledger Fabric", "");
						} else {
							resp = new ServiceResponse(Status.FAILED_HLF,
									"Line items are not updated at Hyperledger Fabric", "");
						}
					} else {
						resp = new ServiceResponse(Status.FAILED_DB, "Failed to update line items in DB", "");
					}
				} else {
					resp = new ServiceResponse(Status.INVALID_INPUT, "Invalid status to update", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Invalid update mode specified", "");
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in updateASN", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side updateASN", ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Creates a new Return to manufacturer return
	 * 
	 * @param postBody
	 *            Post body in json format
	 * @return Response
	 */
	@POST
	@Path("createNewManufacturerReturn")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewManufacturerReturn(String postBody) {
		ServiceResponse resp;
		try {
			JsonObject requestObject = CommonUtil.fromJson(postBody, JsonObject.class);
			String requestNumber = requestObject.get("requestNumber").getAsString();
			JsonArray lineItems = requestObject.getAsJsonArray("lineitems");
			List<LineItem> lineItemList = new ArrayList<>();
			List<String> lineItemIdList = new ArrayList<>();
			String updTs = CommonUtil.getTimeStamp();
			String who = requestObject.get("createdBy").getAsString();
			for (int index = 0; index < lineItems.size(); index++) {
				String lineItemId = lineItems.get(index).getAsString();
				LineItem li = new LineItem();
				li.put("lineItemId", lineItemId);
				li.put("updateTs", CommonUtil.getTimeStamp());
				li.put("updatedBy", who);
				li.put("status", ApplicationConstants.STATUS_SHT_DD);
				li.put("ddrUniqueid", requestNumber);
				lineItemList.add(li);
				lineItemIdList.add(lineItemId);
			}
			if (requestObject != null && lineItems != null) {
				// Add the time stamps etc
				ManufReturnRequest mfrRequest = new ManufReturnRequest();
				mfrRequest.put("requestNumber", requestNumber);
				mfrRequest.put("createTimestamp", updTs);
				mfrRequest.put("updateTimestamp", updTs);
				mfrRequest.put("updatedBy", who);
				mfrRequest.put("status", ApplicationConstants.STATUS_SHT_MF);
				mfrRequest.put("lineitems", lineItemIdList);
				mfrRequest.put("remarks", requestObject.get("remarks").getAsString());
				// Save this
				boolean isDBSaveSucces = true;
				if (_saveInDB) {
					isDBSaveSucces = AssetDataDAO.saveManufReturnRequest(mfrRequest);
					isDBSaveSucces = isDBSaveSucces && AssetDataDAO.updateLineItems(lineItemList);
				}
				if (isDBSaveSucces) {
					HLFabricResponse lhfResponse = AssetManagementHLFDAO.createMFRRequest(
							"TRXN-" + RandomDataGenUtil.generateRandomUUID(null, new int[] { 3, 5 }), mfrRequest,
							lineItemIdList, ApplicationConstants.BE_MEDTURN);
					if (lhfResponse.isOk()) {
						resp = new ServiceResponse(Status.SUCCESS, "MFR request created successfully", "");
					} else {
						resp = new ServiceResponse(Status.FAILED_HLF, "MFR request could not be created in HLF", "");
					}
				} else {
					resp = new ServiceResponse(Status.FAILED_DB, "Failed to Save in DB", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Return request or Line Items are missing", "");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in createNewManufacturerReturn", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side createNewManufacturerReturn",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}

	/**
	 * Returns Disposal request details from input request number
	 * 
	 * @param requestNumber
	 *            String
	 * @return
	 */
	/*@GET
	@Path("mfrRequest/{requestNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMFRRequestById(@PathParam("requestNumber") String requestNumber) {
		ServiceResponse resp = null;
		
		try {
			if (requestNumber != null && requestNumber.trim().length() > 0) {
				
				HLFabricResponse response = AssetManagementHLFDAO.getMFRRequest(requestNumber.trim(),
						ApplicationConstants.BE_WAREHOUSE);
				if (response.isOk()) {
					List<LineItem> lineItems = buildMFRDetailsFromFabricResponse(response.getMessage());
					resp = new ServiceResponse(Status.SUCCESS, "Successful search", lineItems);
				} else {
					resp = new ServiceResponse(Status.NOT_FOUND, "No results found", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Invalid status", "");
			}

		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in getMFRRequestById", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side getMFRRequestById",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}*/

	/**
	 * @param jsonString
	 *            From Fabric transaction
	 * @return List<LineItem>
	 */
	private List<LineItem> buildMFRDetailsFromFabricResponse(String jsonString) {
		JsonObject requestObject = CommonUtil.fromJson(jsonString, JsonObject.class);
		JsonArray lineItems = requestObject.getAsJsonArray("itemDetail");
		
		List<LineItem> lineItemIdList = new ArrayList<>();

		for (int index = 0; index < lineItems.size(); index++) {
			JsonObject lineItem = lineItems.get(index).getAsJsonObject();
			lineItemIdList.add(CommonUtil.fromJson(lineItem, LineItem.class));
		}
		
		return lineItemIdList;
	}
	/**
	 * Updates an existing Disposal Request
	 * 
	 * @param postBody
	 *            Post body in json format
	 * @return Response
	 */
	/*@POST
	@Path("updateDisposalRequest")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDisposalRequest(String postBody) {
		ServiceResponse resp;
		try {
			ManufReturnRequest requestObject = CommonUtil.fromJson(postBody, ManufReturnRequest.class);
			_LOGGER.info("Post body received" + postBody);
			if (requestObject != null) {
				// Add the time stamps etc
				String updTS = CommonUtil.getTimeStamp();
				String targetStat = requestObject.getString("status");
				String who = requestObject.getString("updatedBy");
				_LOGGER.info("Status received : " + targetStat);
				// First remove unnecessary fields
				requestObject.remove("_id");
				requestObject.remove("createTimestamp");

				List<String> lineItems = (List<String>) requestObject.remove("lineitems");

				requestObject.put("updateTimestamp", updTS);
				requestObject.put("status", _STATUS_MAP.get(targetStat));
				// Create the line item ids
				List<LineItem> lineItemList = new ArrayList<>();
				for (String lineItemId : lineItems) {
					LineItem li = new LineItem();
					li.put("lineItemId", lineItemId);
					li.put("updateTs", updTS);
					li.put("updatedBy", who);
					li.put("status", _STATUS_MAP.get(targetStat));
					lineItemList.add(li);
				}
				// Save this
				if (AssetDataDAO.updateDisposalRequest(requestObject) && AssetDataDAO.updateLineItems(lineItemList)) {
					resp = new ServiceResponse(Status.SUCCESS, "Disposal request updated  successfully", "");
				} else {
					resp = new ServiceResponse(Status.FAILED_DB, "Failed to update in DB", "");
				}
			} else {
				resp = new ServiceResponse(Status.INVALID_INPUT, "Disposal request or Line items are missing", "");
			}
		} catch (Exception ex) {
			_LOGGER.log(Level.WARNING, "Exception thrown in updateDisposalRequest", ex);
			resp = new ServiceResponse(Status.EXCEPTION, "Exception thrown at server side updateDisposalRequest",
					ex.getMessage());
		}
		return Response.ok().entity(resp).build();
	}*/
}
