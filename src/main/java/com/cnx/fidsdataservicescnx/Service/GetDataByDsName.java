package com.cnx.fidsdataservicescnx.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.simple.JSONObject;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class GetDataByDsName {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FIDSDataMsgBuilderCNX fidsDataMsgBuilderCNX;

    public String getData(String dsName, String hopo) {
        JSONObject invalidHOPO = new JSONObject();
        invalidHOPO.put("MESSAGE", "INVALID HOPO");

        if (!"CNX".equalsIgnoreCase(hopo.toUpperCase())) {
            return invalidHOPO.toJSONString();
        }

        try (Connection conn = dataSource.getConnection()) {
            switch (dsName.toUpperCase()) {
                case "ARR":
                    return fidsDataMsgBuilderCNX.getArrData(hopo, conn).toString();
                case "DEP":
                    return fidsDataMsgBuilderCNX.getDepData(hopo, conn).toString();
                case "GATE":
                    return fidsDataMsgBuilderCNX.getGateData(hopo, conn).toString();
                case "COUNTER":
                    return fidsDataMsgBuilderCNX.getCounterData(hopo, conn).toString();
                case "BELT":
                    return fidsDataMsgBuilderCNX.getBeltData(hopo, conn).toString();
                default:
                    JSONObject invalidDS = new JSONObject();
                    invalidDS.put("MESSAGE", "DS Not found!!");
                    return invalidDS.toJSONString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("MESSAGE", "Error occurred while processing the request.");
            return error.toJSONString();
        }
    }
}
