package com.ibm.services.asset;

public interface ApplicationConstants {

	String BE_WAREHOUSE = "WARE_HOUSE";
	String BE_MEDTURN = "DISPOSER";
	String BE_DISPOAL_COMP = "DISPOSAL";
	String BE_MANUFACTURER = "MANUFACTURER";
	String STATUS_SHT_MED="Line shipped to Disposal Company";
	String STATUS_RCVD_MED="Line received at Disposal Company";
	String STATUS_INP_MED="Inspection in progress at Disposal Company";
	String STATUS_SHT_MF="Shipped to Manufacturer";
	String STATUS_RCVD_MF="Received at Manufacturer";
	String STATUS_SHT_DD="Shipped for Destroy/Donate";
	String STATUS_RCVD_DD="Line received at Destroy/Donate";
	String STATUS_DESTORY="Destroyed or Donated";
	String STATUS_RET_WH="Returned to Warehouse";
	String STATUS_RCVD_WH="Received at Warehouse";
	
	int ACTION_SUCESS = 0;
	int ACTION_ERROR = 1;
	int ACTION_EXCEPTION = 2;
	int ACTION_INVALID_INPUT =3;
	
	
	String APP_PROPS_BUNDLE = "APP_PROPS";
	String APP_USER = "app.user";
	String APP_USER_ROLE = "app.user.role";
	String APP_LOGIN_AUTH_TOKEN = "auth_token";
	String APP_ACTION_RESPONSE="actionResponse";
	
	String HL_URL = "hyperledger.server.url";
	String HL_CHAIN_CODE_ID = "hyperledger.chaincode.id";
	String HL_USER_CONTEXT ="hyperledger.user.context";
	String HL_USER_ID ="hyperledger.user.id";
	String HL_USER_SECRET ="hyperledger.user.secret";
}
