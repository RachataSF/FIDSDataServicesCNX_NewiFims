package com.cnx.fidsdataservicescnx.Service;

import java.io.File;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

@Service
public class FIDSDataMsgBuilderCNX {
    private final String FIDSRepo = "/ceda3/FIDSRepository";

    public JSONArray getDepData(String HOPO, Connection crep) {
        try {
            JSONObject weatherinfo = getWeatherInfo(crep);
            JSONObject CKICTERMList = getCKICTerminalList(HOPO, crep);
            JSONObject remarkobj = initRemark(crep);

            String SQL = "select aft.URNO,aft.AOBT,case when aft.ALC2 is not null and aft.ALC2!='  ' then aft.ALC2 else TRIM(substr(FLNO,0,3)) end ALC2,aft.SOBT,to_char(to_date(aft.SOBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') as std,aft.FLNO,aft.JFNO,aft.EOBT,aft.ETOD, "
                    + " case when EOBT !='              ' and EOBT is not null then EOBT else SOBT end as LASTD,\r\n" +
                    " case\r\n" +
                    "   when (aft.ATOT!='              '  and aft.ATOT is not null) then to_char(to_date(aft.ATOT,'yyyymmddhh24miss')+(7/24),'hh24:mi')\r\n"
                    +
                    "   when (aft.ETOD!='              '  and aft.ETOD is not null) then to_char(to_date(aft.ETOD,'yyyymmddhh24miss')+(7/24),'hh24:mi') else ' ' end as ETD,  apt.APSN,apt.APTT,aft.DES3,REMP,nvl(substr(VIAL,2,3),' ') as VIA,nvl(aptv.APSN,' ') as APSNV,nvl(aptv.APTT,' ') as APTTV, \r\n"
                    +
                    "case				   \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end)\r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYDOM, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM, \r\n" +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYINT, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT, \r\n" +
                    " case \r\n" +
                    "   when aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) else (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) end as CITYALL,  \r\n"
                    +
                    " case \r\n" +
                    "   when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as CITYNAMEALL," +
                    "  case when TTYP in ('15','42','46','52','55') then GTD2 else GTD1 end as GATEDOM,  \r\n" +
                    "  GTD1 as GATEINT,  \r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('A1   ','A1A  ','A1B  ','A1C  ','A1D  ','A2   ','A2A  ','A3   ','A3A  ','A4   ','A4A  ','A5   ','A6   ','A7   ','A8   ','A9   ') then GTD1\r\n"
                    +
                    "   when GTD2 in ('A1   ','A1A  ','A1B  ','A1C  ','A1D  ','A2   ','A2A  ','A3   ','A3A  ','A4   ','A4A  ','A5   ','A6   ','A7   ','A8   ','A9   ') then GTD2\r\n"
                    +
                    " else ' ' end as GATEA, \r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('B1   ','B1B  ','B1C  ','B1D  ','B2   ','B2A  ','B2B  ','B3   ','B3A  ','B4   ','B4A  ','B5   ','B5A  ','B6   ','B6A  ','B7   ','B8   ','B9   ') then GTD1\r\n"
                    +
                    "   when GTD2 in ('B1   ','B1B  ','B1C  ','B1D  ','B2   ','B2A  ','B2B  ','B3   ','B3A  ','B4   ','B4A  ','B5   ','B5A  ','B6   ','B6A  ','B7   ','B8   ','B9   ') then GTD2\r\n"
                    +
                    " else ' ' end as GATEB,\r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('C1   ','C10  ','C1A  ','C2   ','C2A  ','C3   ','C4   ','C5   ','C6   ','C7   ','C8   ','C9   ') then GTD1\r\n"
                    +
                    "   when GTD2 in ('C1   ','C10  ','C1A  ','C2   ','C2A  ','C3   ','C4   ','C5   ','C6   ','C7   ','C8   ','C9   ') then GTD2\r\n"
                    +
                    " else ' ' end as GATEC,\r\n" +
                    "case\r\n" +
                    "   when GTD1 in ('D1   ','D1A  ','D2   ','D3   ','D4   ','D5   ','D6   ','D7   ','D8   ','D8A  ') then GTD1\r\n"
                    +
                    "   when GTD2 in ('D1   ','D1A  ','D2   ','D3   ','D4   ','D5   ','D6   ','D7   ','D8   ','D8A  ') then GTD2\r\n"
                    +
                    " else ' ' end as GATED,\r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('E1   ','E10  ','E1A  ','E2   ','E2A  ','E3   ','E4   ','E5   ','E6   ','E7   ','E8   ','E9   ') then GTD1\r\n"
                    +
                    "   when GTD2 in ('E1   ','E10  ','E1A  ','E2   ','E2A  ','E3   ','E4   ','E5   ','E6   ','E7   ','E8   ','E9   ') then GTD2\r\n"
                    +
                    " else ' ' end as GATEE,\r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('F1   ','F1A  ','F2   ','F2A  ','F3   ','F4   ','F5   ','F6   ') then GTD1\r\n" +
                    "   when GTD2 in ('F1   ','F1A  ','F2   ','F2A  ','F3   ','F4   ','F5   ','F6   ') then GTD2\r\n" +
                    " else ' ' end as GATEF,\r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('G1   ','G1A  ','G2   ','G3   ','G4   ','G5   ') then GTD1\r\n" +
                    "   when GTD2 in ('G1   ','G1A  ','G2   ','G3   ','G4   ','G5   ') then GTD2\r\n" +
                    " else ' ' end as GATEG,\r\n" +
                    // " FLTI as DOMINT, \r\n" +
                    " case\r\n" +
                    "   when GTD1 in ('A1   ','A1A  ','A1B  ','A1C  ','A1D  ','A2   ','A2A  ','A3   ','A3A  ','A4   ','A4A  ','A5   ','A6   ','A7   ','A8   ','A9   ','ARR  ','B1   ','B1B  ','B1C  ','B1D  ','B2   ','B2A  ','B2B  ','B3   ','B3A  ','B4   ','B4A  ','B5   ','B5A  ','B6   ','B6A  ','B7   ','B8   ','B9   ') then  	 \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED'   \r\n" +
                    "   when (GD1Y!='              '  and GD1Y is not null) then 'CLOSE'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP='FNC ' then 'FINAL CALL'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP='BOA ' then 'BOARDING'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP!='FNC ' and (BOAC='              '  Or BOAC Is  Null)  and (BOAO='              '  Or BOAO Is  Null)  and (GD1X!='              '  and GD1X is not null) then 'OPEN'  	  else ' ' end \r\n"
                    +
                    "   when GTD2 in ('A1   ','A1A  ','A1B  ','A1C  ','A1D  ','A2   ','A2A  ','A3   ','A3A  ','A4   ','A4A  ','A5   ','A6   ','A7   ','A8   ','A9   ','ARR  ','B1   ','B1B  ','B1C  ','B1D  ','B2   ','B2A  ','B2B  ','B3   ','B3A  ','B4   ','B4A  ','B5   ','B5A  ','B6   ','B6A  ','B7   ','B8   ','B9   ') then  	 \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED'   \r\n" +
                    "   when (GD2Y!='              '  and GD2Y is not null) then 'CLOSE'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='FNC ' then 'FINAL CALL'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='BOA ' then 'BOARDING'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP!='FNC ' and (BOAC='              '  Or BOAC Is  Null)  and (BOAO='              '  Or BOAO Is  Null)  and (GD2X!='              '  and GD2X is not null) then 'OPEN'  	  else ' ' end   else ' ' end as REMGATEDOM, \r\n"
                    +
                    " case\r\n" +
                    "   when GTD1 in ('C1   ','C10  ','C1A  ','C2   ','C2A  ','C3   ','C4   ','C5   ','C6   ','C7   ','C8   ','C9   ','CGO  ','D1   ','D1A  ','D2   ','D3   ','D4   ','D5   ','D6   ','D7   ','D8   ','D8A  ','E1   ','E10  ','E1A  ','E2   ','E2A  ','E3   ','E4   ','E5   ','E6   ','E7   ','E8   ','E9   ','F1   ','F1A  ','F2   ','F2A  ','F3   ','F4   ','F5   ','F6   ','G1   ','G1A  ','G2   ','G3   ','G4   ','G5   ') then  	 \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED'   \r\n" +
                    "   when (GD1Y!='              '  and GD1Y is not null) then 'CLOSE'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP='FNC ' then 'FINAL CALL'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP='BOA ' then 'BOARDING'  	 \r\n" +
                    "   when (GD1Y='              '  Or GD1Y Is  Null)  and REMP!='FNC ' and (BOAC='              '  Or BOAC Is  Null)  and (BOAO='              '  Or BOAO Is  Null)  and (GD1X!='              '  and GD1X is not null) then 'OPEN'  	  else ' ' end \r\n"
                    +
                    "   when GTD2 in ('C1   ','C10  ','C1A  ','C2   ','C2A  ','C3   ','C4   ','C5   ','C6   ','C7   ','C8   ','C9   ','CGO  ','D1   ','D1A  ','D2   ','D3   ','D4   ','D5   ','D6   ','D7   ','D8   ','D8A  ','E1   ','E10  ','E1A  ','E2   ','E2A  ','E3   ','E4   ','E5   ','E6   ','E7   ','E8   ','E9   ','F1   ','F1A  ','F2   ','F2A  ','F3   ','F4   ','F5   ','F6   ','G1   ','G1A  ','G2   ','G3   ','G4   ','G5   ') then  	 \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED'   \r\n" +
                    "   when (GD2Y!='              '  and GD2Y is not null) then 'CLOSE'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='FNC ' then 'FINAL CALL'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='BOA ' then 'BOARDING'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP!='FNC ' and (BOAC='              '  Or BOAC Is  Null)  and (BOAO='              '  Or BOAO Is  Null)  and (GD2X!='              '  and GD2X is not null) then 'OPEN'   	  else ' ' end   else\r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED' else ' ' end end as REMGATEINT, \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) and (to_date(ATOT,'yyyymmddhh24miss')+(30/86400))<sysdate-(7/24) then 1 else 0 end as rolloff1, \r\n"
                    +
                    " case\r\n" +
                    "   when FTYP='X' and (to_date(SOBT,'yyyymmddhh24miss')+(10/1440))<sysdate-(7/24) then 1 else 0 end as rolloff2  ,aft.ATOT,aft.FTYP,aft.GTD1,aft.GTD2, "
                    +
                    "  case when TTYP in ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','43','44','45','77','83','91') then 'I' \r\n"
                    +
                    " when TTYP in ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76') then 'D' \r\n"
                    +
                    " when TTYP in ('15','42','46','52','55') then 'M' \r\n" +
                    " else FLTI end FLTI," +
                    "  case when TTYP in ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','43','44','45','77','83','91') then 'I' \r\n"
                    +
                    " when TTYP in ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76') then 'D' \r\n"
                    +
                    " when TTYP in ('15','42','46','52','55') then 'M' \r\n" +
                    " else FLTI end DOMINT," +
                    " case\r\n" +
                    "   when (ATOT !='              ' and ATOT is not null) then 'DEPARTED'  \r\n" +
                    "   when (GD1Y !='              ' and GD1Y is not null) then 'CLOSE'  	\r\n" +
                    "   when (GD1Y ='              ' or GD1Y is null) and REMP='FNC ' then 'FINAL CALL'  	\r\n" +
                    "   when (GD1Y ='              ' or GD1Y is null) and REMP='BOA ' then 'BOARDING'  	\r\n" +
                    "   when (GD1Y ='              ' or GD1Y is null) and REMP!='FNC ' and (GD1X !='              ' and GD1X is not null) then 'OPEN'  	  else ' ' end as REMGATE1, 	 \r\n"
                    +
                    " case\r\n" +
                    "   when (ATOT!='              '  and ATOT is not null) then 'DEPARTED'   \r\n" +
                    "   when (GD2Y!='              '  and GD2Y is not null) then 'CLOSE'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='FNC ' then 'FINAL CALL'  	 \r\n" +
                    "   when (GD2Y='              '  Or GD2Y Is  Null)  and REMP='BOA ' then 'BOARDING'  	 \r\n" +
                    "   When (Gd2y='              '  Or Gd2y Is  Null)  And Remp!='FNC ' And (Boac='              '  Or Boac Is  Null)  And (Boao='              '  Or Boao Is  Null)  And (Gd2x!='              '  And Gd2x Is Not Null) Then 'OPEN'   	  Else ' ' End  As REMGATE2  ,\r\n"
                    +
                    " case\r\n" +
                    "   when (atot!='              '  and atot is not null) and (to_date(atot,'yyyymmddhh24miss') + (5/1440))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when (eobt!='              '  and eobt is not null) and (to_date(eobt,'yyyymmddhh24miss') + (5/1440))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when (to_date(sobt,'yyyymmddhh24miss') + (5/1440))<sysdate-(7/24) then 1 else 0 end as rolloff_dmk1v5,\r\n"
                    +
                    " case\r\n" +
                    "   when (atot!='              '  and atot is not null) and (to_date(atot,'yyyymmddhh24miss') + (10/1440))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when (eobt!='              '  and eobt is not null) and (to_date(eobt,'yyyymmddhh24miss') + (10/1440))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when (to_date(sobt,'yyyymmddhh24miss') + (10/1440))<sysdate-(7/24) then 1 else 0 end as rolloff_dmk1v10\r\n"
                    +
                    " ,case\r\n" +
                    "   when FTYP='X' and (eobt!='              '  and eobt is not null) and (to_date(eobt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when FTYP='X' and (to_date(sobt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 else 0 end as rolloff_dmk3 ,case\r\n"
                    +
                    "   when FLTI='D' then 'Terminal 2' else 'Terminal 1' end as terminal,aft.GD1Y,aft.GD2Y "
                    + " from FIDS_AFTTAB aft   inner join Fids_APTTAB apt on aft.DES4=apt.APC4  left outer join Fids_APTTAB aptv on substr(aft.VIAL,5,4)=aptv.APC4  "
                    + " where aft.ADID='D' and "
                    + " (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) and "// Case if Airline is WE and flight
                                                                                   // is cancel then it not show.
                    // + " aft.SOBT between to_char(sysdate-(7/24)-2,'yyyymmddhh24miss') and
                    // to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') "
                    + " ((aft.SOBT between to_char(sysdate-(7/24)-2,'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') and aft.ftyp!='X') or "
                    + " (aft.SOBT between to_char(sysdate-(7/24)-(1/24),'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') and aft.ftyp='X') ) "

                    + " and ((aft.ATOT is null or aft.ATOT = '              ') or (aft.ATOT is not null and aft.ATOT > to_char(sysdate-(7/24)-(10/1440),'yyyymmddhh24miss'))) "

                    + " and aft.hopo='" + HOPO
                    + "' and (aft.FLNO!='              '  and aft.FLNO is not null)  And aft.FTYP!='N' "
                    + " and aft.ttyp in ('01','04','05','06','15','16','31','33','41','42','43','44','45','46','52','55','56','61','65','66') "
                    + " and (aft.stev is null or aft.stev!='9') "
                    + " order by aft.SOBT,aft.FLNO";
            // System.out.println(SQL);
            PreparedStatement pstmt = crep.prepareStatement(SQL);
            // System.out.println("Execute!!");
            ResultSet rs = pstmt.executeQuery();
            JSONArray result = new JSONArray();

            int count = 0;
            String dom = "(Domestic)";
            String intl = "(International)";
            while (rs.next() && (result.size() < 150)) {
                JSONObject row = new JSONObject();
                String URNO = rs.getString("URNO") == null ? "" : rs.getString("URNO");
                String ALC2 = rs.getString("ALC2") == null ? "" : rs.getString("ALC2");
                String STD = rs.getString("SOBT") == null ? "              " : rs.getString("SOBT");
                String GTD1 = rs.getString("GTD1") == null ? "" : rs.getString("GTD1").trim();
                String GTD2 = rs.getString("GTD2") == null ? "" : rs.getString("GTD2").trim();
                String DES3 = rs.getString("DES3") == null ? "" : rs.getString("DES3");
                // String DOMINT = rs.getString("DOMINT")==null?"":rs.getString("DOMINT");
                String WEATHER = "";
                String FLTI = rs.getString("FLTI") == null ? "" : rs.getString("FLTI");
                String TERMINAL = FLTI.equals("D") ? getRemarkDelay("DEP", "TERMINAL2", remarkobj)
                        : getRemarkDelay("DEP", "TERMINAL1", remarkobj);
                String TERMINAL1 = getRemarkDelay("DEP", "TERMINAL1", remarkobj);
                String TERMINAL2 = getRemarkDelay("DEP", "TERMINAL2", remarkobj);
                String FlightRemark = "";
                if (weatherinfo.get(DES3) != null) {
                    WEATHER = (String) weatherinfo.get(DES3);
                }

                String AOBT = rs.getString("AOBT") == null ? "              " : rs.getString("AOBT");
                String GD1Y = rs.getString("GD1Y") == null ? "              " : rs.getString("GD1Y");
                String GD2Y = rs.getString("GD2Y") == null ? "              " : rs.getString("GD2Y");

                String CounterINT = "";
                String CounterDOM = "";
                String CounterRemarkINT = "";
                String CounterRemarkDOM = "";
                String ctrstartINT = "";
                String ctrendINT = "";
                String ctrstartDOM = "";
                String ctrendDOM = "";

                JSONObject dcResultINT = getDedicateCounterByTERM(URNO, crep, "I");
                String dedicateResultINT = (String) dcResultINT.get("ctrstatus");
                String[] cstatusINT = dedicateResultINT.split("\\|");
                if (!dedicateResultINT.equals("")) {
                    ctrstartINT = (String) dcResultINT.get("ctrstart");
                    ctrendINT = (String) dcResultINT.get("ctrend");

                    ctrstartINT = UTC2LOCAL(ctrstartINT);
                    ctrendINT = UTC2LOCAL(ctrendINT);

                    if (cstatusINT.length > 1) {
                        CounterINT = cstatusINT[0];
                        CounterRemarkINT = cstatusINT[1];
                    } else {
                        CounterINT = cstatusINT[0];
                        CounterRemarkINT = "";
                    }
                }
                if (CounterINT.length() <= 0) {// NO Dedicate Get Common
                    dcResultINT = getCommonCounterByTERM(ALC2, STD, crep, HOPO, "I");
                    dedicateResultINT = (String) dcResultINT.get("ctrstatus");
                    cstatusINT = dedicateResultINT.split("\\|");
                    SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date stddate = aodbDate.parse(STD);
                    Calendar cal = Calendar.getInstance(); // creates calendar
                    cal.setTime(stddate); // sets calendar time/date
                    cal.add(Calendar.HOUR_OF_DAY, 7 - 3);
                    ctrstartINT = aodbDate.format(cal.getTime());

                    cal = Calendar.getInstance(); // creates calendar
                    cal.setTime(stddate); // sets calendar time/date
                    cal.add(Calendar.MINUTE, (7 * 60) - 30);
                    ctrendINT = aodbDate.format(cal.getTime());
                    if (!dedicateResultINT.equals("")) {
                        if (cstatusINT.length > 1) {
                            CounterINT = cstatusINT[0];
                            CounterRemarkINT = cstatusINT[1];
                        } else {
                            CounterINT = cstatusINT[0];
                            CounterRemarkINT = "";
                        }
                    } else {// NO INT Data set Default
                        CounterINT = "";
                        CounterRemarkINT = "";
                    }
                }

                JSONObject dcResultDOM = getDedicateCounterByTERM(URNO, crep, "D");
                String dedicateResultDOM = (String) dcResultDOM.get("ctrstatus");
                String[] cstatusDOM = dedicateResultDOM.split("\\|");
                if (!dedicateResultDOM.equals("")) {
                    ctrstartDOM = (String) dcResultDOM.get("ctrstart");
                    ctrendDOM = (String) dcResultDOM.get("ctrend");

                    ctrstartDOM = UTC2LOCAL(ctrstartDOM);
                    ctrendDOM = UTC2LOCAL(ctrendDOM);

                    if (cstatusDOM.length > 1) {
                        CounterDOM = cstatusDOM[0];
                        CounterRemarkDOM = cstatusDOM[1];
                    } else {
                        CounterDOM = cstatusDOM[0];
                        CounterRemarkDOM = "";
                    }
                }
                if (CounterDOM.length() <= 0) {// NO Dedicate Get Common
                    dcResultDOM = getCommonCounterByTERM(ALC2, STD, crep, HOPO, "D");
                    dedicateResultDOM = (String) dcResultDOM.get("ctrstatus");
                    cstatusDOM = dedicateResultDOM.split("\\|");
                    SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date stddate = aodbDate.parse(STD);
                    Calendar cal = Calendar.getInstance(); // creates calendar
                    cal.setTime(stddate); // sets calendar time/date
                    cal.add(Calendar.HOUR_OF_DAY, 7 - 3);
                    ctrstartDOM = aodbDate.format(cal.getTime());

                    cal = Calendar.getInstance(); // creates calendar
                    cal.setTime(stddate); // sets calendar time/date
                    cal.add(Calendar.MINUTE, (7 * 60) - 30);
                    ctrendDOM = aodbDate.format(cal.getTime());

                    if (!dedicateResultDOM.equals("")) {
                        // if(ALC2.equals("FD") && STD.equals("20230615053500")) {
                        // System.out.println(cstatusDOM[0]);
                        // }
                        if (cstatusDOM.length > 1) {
                            CounterDOM = cstatusDOM[0];
                            CounterRemarkDOM = cstatusDOM[1];
                        } else {
                            CounterDOM = cstatusDOM[0];
                            CounterRemarkDOM = "";
                        }
                    } else {// NO INT Data set Default
                        CounterDOM = "";
                        CounterRemarkDOM = "";
                    }
                }

                // String REMGATEINT =
                // GateRemarkChange((rs.getString("REMGATEINT")==null?"":rs.getString("REMGATEINT")).trim(),crep);
                // String REMGATEDOM =
                // GateRemarkChange((rs.getString("REMGATEDOM")==null?"":rs.getString("REMGATEDOM")).trim(),crep);

                String REMGATE1 = "";// GateRemarkChange((rs.getString("REMGATE1")==null?"":rs.getString("REMGATE1")).trim(),remarkobj);
                String REMGATE2 = "";// GateRemarkChange((rs.getString("REMGATE2")==null?"":rs.getString("REMGATE2")).trim(),remarkobj);

                String REMDMKROW = TERMINAL;

                // System.out.println(ctrstart+" "+ctrend);
                boolean isfirstgatechange = false; // checkFirstGateChangeByURNO(URNO, ctrstart, ctrend,crep);
                boolean issecondgatechange = false; // checkSecondGateChangeByURNO(URNO, ctrstart, ctrend,crep);

                if (FLTI.equals("D")) {
                    isfirstgatechange = checkFirstGateChangeByURNO(URNO, ctrstartDOM, ctrendDOM, crep);
                } else if (FLTI.equals("I")) {
                    isfirstgatechange = checkFirstGateChangeByURNO(URNO, ctrstartINT, ctrendINT, crep);
                } else if (FLTI.equals("M")) {
                    isfirstgatechange = checkFirstGateChangeByURNO(URNO, ctrstartINT, ctrendINT, crep);
                    issecondgatechange = checkFirstGateChangeByURNO(URNO, ctrstartDOM, ctrendDOM, crep);
                }

                if (isfirstgatechange) {
                    String MSG = getRemarkDelay("DEP", "NEW GATE", remarkobj);
                    if (REMGATE1.equals("")) {
                        REMGATE1 = MSG;
                    } else {
                        REMGATE1 += "|" + MSG;
                    }
                }
                if (issecondgatechange) {
                    String MSG = getRemarkDelay("DEP", "NEW GATE", remarkobj);
                    if (REMGATE2.equals("")) {
                        REMGATE2 = MSG;
                    } else {
                        REMGATE2 += "|" + MSG;
                    }
                }

                String REMGATE1TMP = GateRemarkChange(
                        (rs.getString("REMGATE1") == null ? "" : rs.getString("REMGATE1")).trim(), remarkobj);
                String REMGATE2TMP = GateRemarkChange(
                        (rs.getString("REMGATE2") == null ? "" : rs.getString("REMGATE2")).trim(), remarkobj);

                if (REMGATE1.length() > 0 && REMGATE1TMP.length() > 0) {
                    REMGATE1 = REMGATE1TMP;// Replace NEWGATE
                } else if (REMGATE1.equals("") && REMGATE1TMP.length() > 0) {
                    REMGATE1 = REMGATE1TMP;
                }

                if (REMGATE2.length() > 0 && REMGATE2TMP.length() > 0) {
                    REMGATE2 = REMGATE2TMP;// Replace NEWGATE
                } else if (REMGATE2.equals("") && REMGATE1TMP.length() > 0) {
                    REMGATE2 = REMGATE2TMP;
                }

                String REMP = rs.getString("REMP") == null ? "" : rs.getString("REMP");
                String DELAYMSG = "";

                if (CounterRemarkINT.equals("OPEN")) {
                    CounterRemarkINT = getRemarkDelay("DEP", "OPEN", remarkobj);// "Ck-in
                                                                                // Open|เน€เธ�เธฒเธ—เน�เน€เธ•เธญเธฃเน�เน€เธ�เธดเธ”";

                } else if (CounterRemarkINT.equals("CLOSE")) {
                    CounterRemarkINT = getRemarkDelay("DEP", "CLOSE", remarkobj);// "Ck-in
                                                                                 // Close|เน€เธ�เธฒเธ—เน�เน€เธ•เธญเธฃเน�เธ�เธดเธ”";
                }

                if (CounterRemarkDOM.equals("OPEN")) {
                    CounterRemarkDOM = getRemarkDelay("DEP", "OPEN", remarkobj);// "Ck-in
                                                                                // Open|เน€เธ�เธฒเธ—เน�เน€เธ•เธญเธฃเน�เน€เธ�เธดเธ”";

                } else if (CounterRemarkDOM.equals("CLOSE")) {
                    CounterRemarkDOM = getRemarkDelay("DEP", "CLOSE", remarkobj);// "Ck-in
                                                                                 // Close|เน€เธ�เธฒเธ—เน�เน€เธ•เธญเธฃเน�เธ�เธดเธ”";
                }

                if (REMP.equals("DEL ")) {
                    DELAYMSG = getRemarkDelay("ARR", "DELAYED", remarkobj);// "DELAYED|เธฅเน�เธฒเธ�เน�เธฒ";

                } else if (REMP.equals("BWE ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Bad Weather", remarkobj);// "Bad
                                                                               // Weather|เธชเธ เธฒเธ�เธญเธฒเธ�เธฒเธจ";

                } else if (REMP.equals("IND ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Indef.Delayed", remarkobj);// "Indef.Delayed|เธฅเน�เธฒเธ�เน�เธฒเน�เธกเน�เธกเธตเธ�เธณเธซเธ�เธ”";

                } else if (REMP.equals("NTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "New Time", remarkobj);// "New
                                                                            // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒเน�เธซเธกเน�";

                } else if (REMP.equals("OTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "On Time", remarkobj);// "On Time|เธ•เธฃเธ�เน€เธงเธฅเธฒ";

                } else if (REMP.equals("RTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "RETIMED", remarkobj);// "Re
                                                                           // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒ";
                }
                if (DELAYMSG.length() > 0) {
                    if (CounterRemarkINT.equals("")) {
                        CounterRemarkINT = DELAYMSG;
                    } else {
                        CounterRemarkINT += "|" + DELAYMSG;
                    }
                    if (CounterRemarkDOM.equals("")) {
                        CounterRemarkDOM = DELAYMSG;
                    } else {
                        CounterRemarkDOM += "|" + DELAYMSG;
                    }
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMDMKROW.equals("")) {
                        REMDMKROW = DELAYMSG;
                    } else {
                        REMDMKROW += "|" + DELAYMSG;
                    }
                    if (FlightRemark.equals("")) {
                        FlightRemark = DELAYMSG;
                    } else {
                        FlightRemark += "|" + DELAYMSG;
                    }
                }

                String FTYP = rs.getString("FTYP") == null ? "" : rs.getString("FTYP");
                if (FTYP.equals("X")) {
                    String tmp = getRemarkDelay("ARR", "Cancelled", remarkobj);
                    if (CounterRemarkINT.equals("")) {
                        CounterRemarkINT = tmp;
                    } else {
                        CounterRemarkINT += "|" + tmp;
                    }
                    if (CounterRemarkDOM.equals("")) {
                        CounterRemarkDOM = tmp;
                    } else {
                        CounterRemarkDOM += "|" + tmp;
                    }
                    REMGATE1 = tmp;
                    REMGATE2 = tmp;
                    REMDMKROW = tmp;
                    FlightRemark = tmp;
                }

                String ATOT = rs.getString("ATOT") == null ? "              " : rs.getString("ATOT");
                if (!ATOT.equals("              ")) {
                    String tmp = getRemarkDelay("ARR", "DEPARTED", remarkobj);// "DEPARTED|เน€เธ�เธฃเธทเน�เธญเธ�เธ�เธถเน�เธ�";
                    CounterRemarkDOM = tmp;
                    CounterRemarkINT = tmp;
                    REMGATE1 = tmp;
                    REMGATE2 = tmp;
                    REMDMKROW = tmp;
                    FlightRemark = tmp;
                }

                String FLNO = rs.getString("FLNO") == null ? "" : rs.getString("FLNO");
                String JFNO = rs.getString("JFNO") == null ? " " : rs.getString("JFNO");
                if (JFNO == null) {
                    JFNO = " ";
                }
                String TERMFLTREMARK = TERMINAL;
                String TERMFLTREMARK1 = TERMINAL1;
                String TERMFLTREMARK2 = TERMINAL2;
                if (FlightRemark != null && FlightRemark.length() > 0) {
                    TERMFLTREMARK += "|" + FlightRemark;
                    TERMFLTREMARK1 += "|" + FlightRemark;
                    TERMFLTREMARK2 += "|" + FlightRemark;
                }
                String TERMGATREMARK1 = TERMINAL;
                String TERM1GATREMARK1 = TERMINAL1;
                String TERM2GATREMARK1 = TERMINAL2;
                if (REMGATE1 != null && REMGATE1.length() > 0) {
                    TERMGATREMARK1 += "|" + REMGATE1;
                    TERM1GATREMARK1 += "|" + REMGATE1;
                    TERM2GATREMARK1 += "|" + REMGATE1;
                }
                String TERMGATREMARK2 = TERMINAL;
                String TERM1GATREMARK2 = TERMINAL1;
                String TERM2GATREMARK2 = TERMINAL2;
                if (REMGATE2 != null && REMGATE2.length() > 0) {
                    TERMGATREMARK2 += "|" + REMGATE2;
                    TERM1GATREMARK2 += "|" + REMGATE2;
                    TERM2GATREMARK2 += "|" + REMGATE2;
                }

                // String GATEINT =
                // rs.getString("GATEINT")==null?"":rs.getString("GATEINT").trim();
                // try {
                // if((HOPO.equals("HKT")||HOPO.equals("CNX"))&&GTD1.length()>=3) {
                // GTD1=GTD1.substring(0, 2);
                // }
                // GTD1 = Integer.parseInt(GTD1)+"";
                // }catch(Exception ex) {
                // }
                // //String GATEDOM =
                // rs.getString("GATEDOM")==null?"":rs.getString("GATEDOM").trim();
                // try {
                // if((HOPO.equals("HKT")||HOPO.equals("CNX"))&&GTD2.length()>=3) {
                // GTD2=GTD2.substring(0, 2);
                // }
                // GTD2 = Integer.parseInt(GTD2)+"";
                // }catch(Exception ex) {
                // }

                if ((HOPO.equals("HKT") || HOPO.equals("CNX"))) {
                    // GTD1="B30D";
                    GTD1 = getGate(GTD1);
                    GTD2 = getGate(GTD2);
                    // System.out.println(GTD1);
                }
                String FULLFLNO = "";
                String FULLLOGO = "";
                String CounterALL = FLTI.equals("D") ? CounterDOM : CounterINT;
                if (FLTI.equals("D")) {
                    if ((CounterDOM == null || CounterDOM.length() <= 0)) {
                        CounterALL = CounterINT;
                    } else {
                        CounterALL = CounterDOM;
                    }
                } else {
                    if ((CounterINT == null || CounterINT.length() <= 0)) {
                        CounterALL = CounterDOM;
                    } else {
                        CounterALL = CounterINT;
                    }
                }

                String isFinalCall = "0";
                if (REMP.trim().equals("FNC") || GD1Y.trim().length() > 0 || GD2Y.trim().length() > 0
                        || AOBT.trim().length() > 0 || ATOT.trim().length() > 0) {
                    isFinalCall = "1";
                }

                String CTREND = "";

                if (ctrendINT.trim().length() == 14 && ctrendDOM.trim().length() == 14) {// BOTH
                    SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");
                    if (aodbDate.parse(ctrendINT).after(aodbDate.parse(ctrendDOM))) {// INT
                        CTREND = LOCAL2UTC(ctrendINT);
                    } else {// DOM
                        CTREND = LOCAL2UTC(ctrendDOM);
                    }
                } else if (ctrendINT.trim().length() == 14) {// INT
                    CTREND = LOCAL2UTC(ctrendINT);
                } else if (ctrendDOM.trim().length() == 14) {// DOM
                    CTREND = LOCAL2UTC(ctrendDOM);
                }

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") && ALC2.equals("DD")) {// For
                                                                                                                  // Nokair
                                                                                                                  // airline
                                                                                                                  // when
                                                                                                                  // FLNO
                                                                                                                  // begin
                                                                                                                  // 0
                                                                                                                  // then
                                                                                                                  // cutoff
                                                                                                                  // 0
                            FLNO = substringForNokair(FLNO);
                            row.put("FLNO", FLNO.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                if (JFNO.equals(" ")) {
                    FULLFLNO = FLNO.trim();
                    FULLLOGO = FLNO.substring(0, 3).trim();
                    row.put("FULLFLNO", FULLFLNO);
                    row.put("LOGO", FULLLOGO);
                    row.put("CHKROW", CounterALL);
                    row.put("CHKROW2", CounterALL);
                    row.put("CHKROWHKT", CounterALL);
                    row.put("CHKROWCNX", CounterALL);
                    row.put("CHKROW3", CounterALL);
                    row.put("REMGATE1", REMGATE1);
                    if (FLTI.equals("I")) {
                        if (CounterRemarkINT == null || CounterRemarkINT.length() <= 0) {
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            row.put("REMCTR", "");
                        } else {
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                    } else if (FLTI.equals("D")) {
                        if (CounterRemarkDOM == null || CounterRemarkDOM.length() <= 0) {
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            row.put("REMCTR", "");
                            // if(ALC2.equals("FD") && STD.equals("20230126094500")) {
                            // System.out.println(CounterRemarkDOM);
                            // }
                        } else {
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }
                    }

                    row.put("ALLFLNO", FULLFLNO);
                    row.put("ALLLOGO", FULLLOGO);
                    row.put("WEATHER", WEATHER);
                    row.put("MAINFLNO", "1");
                    row.put("TERMINAL", TERMINAL);
                    row.put("TERMFLTREMARK", TERMFLTREMARK);
                    row.put("TERMGATREMARK", TERMGATREMARK1);
                    row.put("FLIGHTREMARK", FlightRemark);
                    row.put("REMDMKROW", REMDMKROW);

                    row.put("DOMINT", rs.getString("FLTI"));

                    row.put("GATE", GTD1);
                    row.put("REMGATE", REMGATE1);

                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        String CITYDOM = rs.getString("CITYDOM") == null ? "" : rs.getString("CITYDOM");
                        String CITYNAMEDOM = rs.getString("CITYNAMEDOM") == null ? "" : rs.getString("CITYNAMEDOM");
                        row.put("CITYALL", CITYINT);
                        row.put("CITYNAMEALL", CITYNAMEINT);
                        row.put("DOMINT", "I");
                        if (ALC2.equals("TG")) {
                            row.put("CHKROW", "HJ");
                        }
                        row.put("GATE", GTD1);
                        row.put("REMGATE", REMGATE1);
                        row.put("TERMGATREMARK", TERM1GATREMARK1);
                        row.put("TERMFLTREMARK", TERMFLTREMARK1);
                        row.put("CHKROW2", CounterINT);
                        row.put("CHKROWHKT", CounterINT);
                        if (CounterINT == null || CounterINT.length() <= 0) {
                            row.put("CHKROWCNX", CounterDOM);// INT BLANK
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            row.put("REMCTR", "");
                        } else {
                            row.put("CHKROWCNX", CounterINT);
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                        row.put("TERMINAL", TERMINAL1);
                        row.put("MIXFLNO", "0");
                        row.put("TEST", CounterINT + "|" + CounterDOM);
                        row.put("ISFINALCALL", isFinalCall);
                        row.put("CTREND", CTREND);
                        result.add(row);
                        String rowtext = row.toJSONString();
                        JSONObject rowmix = new JSONObject();
                        JSONParser jp = new JSONParser();
                        rowmix = (JSONObject) jp.parse(rowtext);
                        rowmix.put("CITYALL", CITYDOM);
                        rowmix.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmix.put("DOMINT", "D");
                        if (ALC2.equals("TG")) {
                            rowmix.put("CHKROW", "C");
                        }

                        rowmix.put("GATE", GTD2);
                        rowmix.put("REMGATE", REMGATE2);
                        rowmix.put("TERMGATREMARK", TERM2GATREMARK2);
                        rowmix.put("TERMFLTREMARK", TERMFLTREMARK2);
                        rowmix.put("CHKROW2", CounterDOM);
                        rowmix.put("CHKROWHKT", CounterDOM);
                        if (CounterDOM == null || CounterDOM.length() <= 0) {
                            rowmix.put("CHKROWCNX", CounterINT);
                            // rowmix.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            rowmix.put("REMCTR", "");
                        } else {
                            rowmix.put("CHKROWCNX", CounterDOM);
                            rowmix.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }
                        rowmix.put("TERMINAL", TERMINAL2);
                        rowmix.put("MIXFLNO", "1");
                        rowmix.put("TEST", CounterINT + "|" + CounterDOM);
                        rowmix.put("ISFINALCALL", isFinalCall);
                        rowmix.put("CTREND", CTREND);
                        result.add(rowmix);

                    } else {
                        row.put("MIXFLNO", "0");
                        row.put("TEST", CounterINT + "|" + CounterDOM);
                        row.put("ISFINALCALL", isFinalCall);
                        row.put("CTREND", CTREND);
                        result.add(row);
                    }

                } else {
                    FULLFLNO = FLNO.trim();
                    FULLLOGO = FLNO.substring(0, 3).trim();
                    JFNO = JFNO.replace("   ", "  ");
                    String SHAREFLNO = JFNO.replace("  ", "|");
                    String SHARELOGO = "";
                    String[] JLIST = JFNO.split("  ");
                    for (int i = 0; i < JLIST.length; i++) {
                        if (i == 0) {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        } else {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += "|" + JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        }
                    }
                    row.put("FULLFLNO", FULLFLNO);
                    row.put("LOGO", FULLLOGO);
                    row.put("CHKROW", CounterALL);
                    row.put("CHKROW2", CounterALL);
                    row.put("CHKROWHKT", CounterALL);
                    row.put("CHKROWCNX", CounterALL);
                    row.put("CHKROW3", CounterALL);
                    row.put("REMGATE1", REMGATE1);
                    if (FLTI.equals("I")) {
                        if (CounterRemarkINT == null || CounterRemarkINT.length() <= 0) {
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            row.put("REMCTR", "");
                        } else {
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                    } else if (FLTI.equals("D")) {
                        if (CounterRemarkDOM == null || CounterRemarkDOM.length() <= 0) {
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            row.put("REMCTR", "");
                        } else {
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }
                    }
                    row.put("ALLFLNO", FULLFLNO + "|" + SHAREFLNO);
                    row.put("ALLLOGO", FULLLOGO + "|" + SHARELOGO);
                    row.put("WEATHER", WEATHER);
                    row.put("MAINFLNO", "1");
                    row.put("TERMINAL", TERMINAL);
                    row.put("TERMFLTREMARK", TERMFLTREMARK);
                    row.put("REMDMKROW", REMDMKROW);
                    row.put("FLIGHTREMARK", FlightRemark);

                    row.put("DOMINT", rs.getString("FLTI"));

                    row.put("GATE", GTD1);
                    row.put("REMGATE", REMGATE1);
                    row.put("TERMGATREMARK", TERMGATREMARK1);
                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        row.put("CITYALL", CITYINT);
                        row.put("CITYNAMEALL", CITYNAMEINT);
                        row.put("DOMINT", "I");
                        if (ALC2.equals("TG")) {
                            row.put("CHKROW", "HJ");
                        }
                        row.put("GATE", GTD1);
                        row.put("REMGATE", REMGATE1);
                        row.put("TERMGATREMARK", TERM1GATREMARK1);
                        row.put("TERMFLTREMARK", TERMFLTREMARK1);
                        row.put("CHKROW2", CounterINT);
                        row.put("CHKROWHKT", CounterINT);
                        if (CounterINT == null || CounterINT.length() <= 0) {
                            row.put("CHKROWCNX", CounterDOM);
                            // row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            row.put("REMCTR", "");
                        } else {
                            row.put("CHKROWCNX", CounterINT);
                            row.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                        row.put("TERMINAL", TERMINAL1);

                        row.put("MIXFLNO", "0");
                        row.put("TEST", CounterINT + "|" + CounterDOM);
                        row.put("ISFINALCALL", isFinalCall);
                        row.put("CTREND", CTREND);
                        result.add(row);
                    } else {
                        row.put("MIXFLNO", "0");
                        row.put("TEST", CounterINT + "|" + CounterDOM);
                        row.put("ISFINALCALL", isFinalCall);
                        row.put("CTREND", CTREND);
                        result.add(row);
                    }

                    JSONObject rowJ = new JSONObject();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        rowJ.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                    }
                    rowJ.put("FULLFLNO", SHAREFLNO);
                    rowJ.put("LOGO", SHARELOGO);
                    rowJ.put("CHKROW", CounterALL);
                    rowJ.put("CHKROW2", CounterALL);
                    rowJ.put("CHKROWHKT", CounterALL);
                    rowJ.put("CHKROWCNX", CounterALL);
                    rowJ.put("CHKROW3", CounterALL);
                    rowJ.put("REMGATE1", REMGATE1);
                    if (FLTI.equals("I")) {
                        if (CounterRemarkINT == null || CounterRemarkINT.length() <= 0) {
                            // rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            rowJ.put("REMCTR", "");
                        } else {
                            rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                    } else if (FLTI.equals("D")) {
                        if (CounterRemarkDOM == null || CounterRemarkDOM.length() <= 0) {
                            // rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            rowJ.put("REMCTR", "");
                        } else {
                            rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }
                    }
                    rowJ.put("ALLFLNO", FULLFLNO + "|" + SHAREFLNO);
                    rowJ.put("ALLLOGO", FULLLOGO + "|" + SHARELOGO);
                    rowJ.put("WEATHER", WEATHER);
                    rowJ.put("MAINFLNO", "0");
                    rowJ.put("TERMINAL", TERMINAL);
                    rowJ.put("TERMFLTREMARK", TERMFLTREMARK);
                    rowJ.put("REMDMKROW", REMDMKROW);
                    rowJ.put("FLIGHTREMARK", FlightRemark);
                    rowJ.put("DOMINT", rs.getString("FLTI"));

                    rowJ.put("GATE", GTD1);
                    rowJ.put("REMGATE", REMGATE1);

                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        rowJ.put("CITYALL", CITYINT);
                        rowJ.put("CITYNAMEALL", CITYNAMEINT);
                        rowJ.put("DOMINT", "I");

                        rowJ.put("GATE", GTD1);
                        rowJ.put("REMGATE", REMGATE1);
                        rowJ.put("TERMGATREMARK", TERM1GATREMARK1);
                        rowJ.put("TERMFLTREMARK", TERMFLTREMARK1);
                        rowJ.put("CHKROW2", CounterINT);
                        rowJ.put("CHKROWHKT", CounterINT);
                        if (CounterINT == null || CounterINT.length() <= 0) {
                            rowJ.put("CHKROWCNX", CounterDOM);
                            // rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, intl));
                            rowJ.put("REMCTR", "");
                        } else {
                            rowJ.put("CHKROWCNX", CounterINT);
                            rowJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, intl));
                        }
                        rowJ.put("TERMINAL", TERMINAL1);

                        if (ALC2.equals("TG")) {
                            rowJ.put("CHKROW", "HJ");
                        }
                        rowJ.put("MIXFLNO", "0");
                        rowJ.put("TEST", CounterINT + "|" + CounterDOM);
                        rowJ.put("ISFINALCALL", isFinalCall);
                        rowJ.put("CTREND", CTREND);
                        result.add(rowJ);
                    } else {
                        rowJ.put("MIXFLNO", "0");
                        rowJ.put("TEST", CounterINT + "|" + CounterDOM);
                        rowJ.put("ISFINALCALL", isFinalCall);
                        rowJ.put("CTREND", CTREND);
                        result.add(rowJ);
                    }

                    if (FLTI.equals("M")) {
                        String CITYDOM = rs.getString("CITYDOM") == null ? "" : rs.getString("CITYDOM");
                        String CITYNAMEDOM = rs.getString("CITYNAMEDOM") == null ? "" : rs.getString("CITYNAMEDOM");

                        String rowtext = row.toJSONString();
                        JSONObject rowmix = new JSONObject();
                        JSONParser jp = new JSONParser();
                        rowmix = (JSONObject) jp.parse(rowtext);
                        rowmix.put("CITYALL", CITYDOM);
                        rowmix.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmix.put("DOMINT", "D");
                        rowmix.put("MAINFLNO", "1");

                        rowmix.put("GATE", GTD2);
                        rowmix.put("REMGATE", REMGATE2);
                        rowmix.put("TERMGATREMARK", TERM2GATREMARK2);
                        rowmix.put("TERMFLTREMARK", TERMFLTREMARK2);
                        rowmix.put("CHKROW2", CounterDOM);
                        rowmix.put("CHKROWHKT", CounterDOM);
                        if (CounterDOM == null || CounterDOM.length() <= 0) {
                            rowmix.put("CHKROWCNX", CounterINT);
                            // rowmix.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            rowmix.put("REMCTR", "");
                        } else {
                            rowmix.put("CHKROWCNX", CounterDOM);
                            rowmix.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }

                        rowmix.put("TERMINAL", TERMINAL2);

                        if (ALC2.equals("TG")) {
                            rowmix.put("CHKROW", "C");
                        }
                        rowmix.put("MIXFLNO", "1");
                        rowmix.put("TEST", CounterINT + "|" + CounterDOM);
                        rowmix.put("ISFINALCALL", isFinalCall);
                        rowmix.put("CTREND", CTREND);
                        result.add(rowmix);

                        String rowtextJ = rowJ.toJSONString();
                        JSONObject rowmixJ = new JSONObject();

                        rowmixJ = (JSONObject) jp.parse(rowtextJ);
                        rowmixJ.put("CITYALL", CITYDOM);
                        rowmixJ.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmixJ.put("DOMINT", "D");
                        rowmixJ.put("MAINFLNO", "0");

                        rowmixJ.put("GATE", GTD2);
                        rowmixJ.put("REMGATE", REMGATE2);
                        rowmixJ.put("TERMGATREMARK", TERM2GATREMARK2);
                        rowmixJ.put("TERMFLTREMARK", TERMFLTREMARK2);
                        rowmixJ.put("CHKROW2", CounterDOM);
                        rowmixJ.put("CHKROWHKT", CounterDOM);
                        if (CounterDOM == null || CounterDOM.length() <= 0) {
                            rowmixJ.put("CHKROWCNX", CounterINT);
                            // rowmixJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkINT, dom));
                            rowmixJ.put("REMCTR", "");
                        } else {
                            rowmixJ.put("CHKROWCNX", CounterDOM);
                            rowmixJ.put("REMCTR", AddCounterRemarkSuffix(CounterRemarkDOM, dom));
                        }
                        rowmixJ.put("TERMINAL", TERMINAL2);

                        if (ALC2.equals("TG")) {
                            rowmixJ.put("CHKROW", "C");
                        }
                        rowmixJ.put("MIXFLNO", "1");
                        rowmixJ.put("TEST", CounterINT + "|" + CounterDOM);
                        rowmixJ.put("ISFINALCALL", isFinalCall);
                        rowmixJ.put("CTREND", CTREND);
                        result.add(rowmixJ);
                    }

                }
                count++;

            }
            rs.close();
            pstmt.close();
            // c.close();
            crep.close();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONArray getArrData(String HOPO, Connection c) {
        try {
            JSONObject remarkobj = initRemark(c);
            String SQL = "select aft.URNO,aft.SIBT," +
                    "  case when TTYP in ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','43','44','45','77','83','91') then 'I' \r\n"
                    +
                    " when TTYP in ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76') then 'D' \r\n"
                    +
                    " when TTYP in ('15','42','46','52','55') then 'M' \r\n" +
                    " else FLTI end FLTI,"
                    + " case when aft.ALC2 is not null and aft.ALC2!='  ' then aft.ALC2 else TRIM(substr(FLNO,0,3)) end ALC2,to_char(to_date(aft.SIBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') as sta,aft.FLNO,aft.JFNO,aft.EIBT,aft.ETOA,  "
                    + " case when EIBT !='              ' and EIBT is not null then EIBT else SIBT end as LASTA,\r\n" +
                    " case\r\n" +
                    "   when (aft.ALDT!='              '  and aft.ALDT is not null)  then to_char(to_date(aft.ALDT,'yyyymmddhh24miss')+(7/24),'hh24:mi') \r\n"
                    +
                    "   when (aft.ETOA!='              '  and aft.ETOA is not null)  then to_char(to_date(aft.ETOA,'yyyymmddhh24miss')+(7/24),'hh24:mi') else ' ' end as ETA,   apt.APSN,apt.APTT,aft.ORG3,REMP,nvl(substr(VIAL,2,3),' ') as VIA,nvl(aptv.APSN,' ') as APSNV,nvl(aptv.APTT,' ') as APTTV, \r\n"
                    +
                    " case				   \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end)\r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) end as CITYDOM, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM, \r\n" +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) end as CITYINT, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT, \r\n" +
                    " case \r\n" +
                    "   when aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) else (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) end as CITYALL,  \r\n"
                    +
                    " case \r\n" +
                    "   when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as CITYNAMEALL," +
                    "  B1BA,BAS1,BAE1,B1EA,B2BA,BAS2,BAE2,B2EA,AIBT,FTYP, \r\n" +
                    " case\r\n" +
                    "   when (aft.AIBT!='              '  and aft.AIBT is not null) and (aft.BAS1='              '  or aft.BAS1 is  null)  and (aft.BAE1='              '  or aft.BAE1 is  null)  and (aft.B1EA='              '  or aft.B1EA is  null)  then 'OPEN' \r\n"
                    +
                    "   when (aft.BAS1!='              '  and aft.BAS1 is not null)and (aft.BAE1='              '  or aft.BAE1 is  null)  and (aft.B1EA='              '  or aft.B1EA is  null)  then 'FIRST BAG' \r\n"
                    +
                    "   when (aft.BAE1!='              '  and aft.BAE1 is not null)and (aft.B1EA='              '  or aft.B1EA is  null)  then 'LAST BAG' \r\n"
                    +
                    "   when (aft.B1EA!='              '  and aft.B1EA is not null) then 'CLOSE' else ' ' end as BELTSTATUS,  GTA1 as GATE ,BLT1 ,BLT2,   B1BA,BAS1,BAE1,B1EA,AIBT,FTYP, \r\n"
                    +
                    " case\r\n" +
                    "   when (B1BA!='              '  and B1BA is not null)   and (BAS1='              '  or BAS1 is  null)  and (BAE1='              '  or BAE1 is  null)   and sysdate > (to_date(B1BA,'yyyymmddhh24miss')+(7/24)+(120/1440)) then 1 else 0 end as rolloff1, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAS1!='              '  and BAS1 is not null)   and (BAE1='              '  or BAE1 is  null)    and sysdate > (to_date(BAS1,'yyyymmddhh24miss')+(7/24)+(90/1440)) then 1 else 0 end as rolloff2_1, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAE1!='              '  and BAE1 is not null)    and sysdate > (to_date(BAE1,'yyyymmddhh24miss')+(7/24)+(90/1440)) then 1 else 0 end as rolloff2_2, \r\n"
                    +
                    " case\r\n" +
                    "   when (B1EA!='              '  and B1EA is not null)  then 1 else 0 end as rolloff3_1, \r\n" +
                    " case\r\n" +
                    "   when (AIBT!='              '  and AIBT is not null)  and (B1BA='              '  or B1BA is  null)  and (BAS1='              '  or BAS1 is  null)  and (BAE1='              '  or BAE1 is  null)  and (B1EA='              '  or B1EA is  null)   and sysdate > (to_date(AIBT,'yyyymmddhh24miss')+(7/24)+((120)/1440)) then 1 else 0 end as rolloff3_2, \r\n"
                    +
                    " case\r\n" +
                    "   when FTYP='X' and sysdate > (to_date(SIBT,'yyyymmddhh24miss')+(7/24)+(10/1440)) then 1 else 0 end as rolloff4, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAS1!='              '  and BAS1 is not null)   and (BAE1='              '  or BAE1 is  null)    and sysdate > (to_date(BAS1,'yyyymmddhh24miss')+(7/24)+(45/1440)) then 1 else 0 end as rolloff2_1v45, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAE1!='              '  and BAE1 is not null)    and sysdate > (to_date(BAE1,'yyyymmddhh24miss')+(7/24)+(45/1440)) then 1 else 0 end as rolloff2_2v45, \r\n"
                    +
                    " case\r\n" +
                    "   when (B1BA!='              '  and B1BA is not null) and (BAS1='              '  or BAS1 is  null)  and (BAE1='              '  or BAE1 is  null)   and sysdate > (to_date(B1BA,'yyyymmddhh24miss')+(7/24)+(60/1440)) then 1 else 0 end as rolloff1v60, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAS1!='              '  and BAS1 is not null)   and (BAE1='              '  or BAE1 is  null)    and sysdate > (to_date(BAS1,'yyyymmddhh24miss')+(7/24)+(60/1440)) then 1 else 0 end as rolloff2_1v60, \r\n"
                    +
                    " case\r\n" +
                    "   when (BAE1!='              '  and BAE1 is not null)    and sysdate > (to_date(BAE1,'yyyymmddhh24miss')+(7/24)+(30/1440)) then 1 else 0 end as rolloff2_2v30, \r\n"
                    +
                    " case\r\n" +
                    "   when (AIBT!='              '  and AIBT is not null)  and (B1BA='              '  or B1BA is  null)  and (BAS1='              '  or BAS1 is  null)  and (BAE1='              '  or BAE1 is  null)  and (B1EA='              '  or B1EA is  null)   and sysdate > (to_date(AIBT,'yyyymmddhh24miss')+(7/24)+((60)/1440)) then 1 else 0 end as rolloff3_2V60, \r\n"
                    +
                    " case\r\n" +
                    "   when (AIBT!='              '  and AIBT is not null)  \r\n" +
                    " and sysdate > (to_date(AIBT,'yyyymmddhh24miss')+(7/24)+((60)/1440)) then 1 else 0 end as rolloff_dmk1\r\n"
                    +
                    " ,case\r\n" +
                    "   when (BAE1!='              '  and BAE1 is not null)  \r\n" +
                    " and sysdate > (to_date(BAE1,'yyyymmddhh24miss')+(7/24)+(30/1440)) then 1 else 0 end as rolloff_dmk2\r\n"
                    +
                    " ,case\r\n" +
                    "   when FTYP='X' and (eibt!='              '  and eibt is not null) and (to_date(eibt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 \r\n"
                    +
                    "   when FTYP='X' and (to_date(sibt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 else 0 end as rolloff_dmk3, \r\n"
                    +
                    " case\r\n" +
                    "   when FTYP='X' then 'Cancelled'\r\n" +
                    "   when (ALDT!='              '  and ALDT is not null)  then 'LANDED' \r\n" +
                    "   when FTYP='D' then 'DIVERTED'\r\n" +
                    "   when remp='DEL ' then 'DELAYED'\r\n" +
                    "   when (ETOA!='              '  and ETOA is not null and (remp is null or remp='    '))  then 'Confirmed'\r\n"
                    +
                    "   when remp='RTI ' then 'RETIMED'\r\n" +
                    "   when remp='IND ' then 'Indef.Delayed'\r\n" +
                    "   when remp='BWE ' then 'Bad Weather'\r\n" +
                    "   when remp='NTI ' then 'New Time'\r\n" +
                    " else ' ' end as remark,   \r\n" +
                    " case \r\n" +
                    "     when (aft.B1EA!='              '  and aft.B1EA is not null) then 'CLOSE' \r\n" +
                    "     when (aft.BAE1!='              '  and aft.BAE1 is not null)and (aft.B1EA='              '  or aft.B1EA is  null)  then 'LAST BAG' \r\n"
                    +
                    "     when (aft.BAS1!='              '  and aft.BAS1 is not null)and (aft.BAE1='              '  or aft.BAE1 is  null)  and (aft.B1EA='              '  or aft.B1EA is  null)  then 'FIRST BAG' \r\n"
                    +
                    "     when aft.REMP='BDL  ' then 'BAG DELAY' \r\n" +
                    "     when (Aft.Aibt!='              '  And Aft.Aibt Is Not Null) And (Aft.Bas1='              '  Or Aft.Bas1 Is  Null)  And (Aft.Bae1='              '  Or Aft.Bae1 Is  Null)  And (Aft.B1ea='              '  Or Aft.B1ea Is  Null)  Then 'OPEN'     Else '' End As REMBELT1  ,\r\n"
                    +
                    " case \r\n" +
                    "     when (aft.B2EA!='              '  and aft.B2EA is not null) then 'CLOSE' \r\n" +
                    "     when (aft.BAE2!='              '  and aft.BAE2 is not null)and (aft.B2EA='              '  or aft.B2EA is  null)  then 'LAST BAG' \r\n"
                    +
                    "     when (aft.BAS2!='              '  and aft.BAS2 is not null)and (aft.BAE2='              '  or aft.BAE2 is  null)  and (aft.B2EA='              '  or aft.B2EA is  null)  then 'FIRST BAG' \r\n"
                    +
                    "     when aft.REMP='BDL  ' then 'BAG DELAY' \r\n" +
                    "     when (Aft.Aibt!='              '  And Aft.Aibt Is Not Null) And (Aft.Bas2='              '  Or Aft.Bas2 Is  Null)  And (Aft.Bae2='              '  Or Aft.Bae2 Is  Null)  And (Aft.B2ea='              '  Or Aft.B2ea Is  Null)  Then 'OPEN'     Else '' End As REMBELT2  ,\r\n"
                    +
                    "   case\r\n" +
                    "   when (Aft.Aibt!='              '  And Aft.Aibt Is Not Null) Then 1 Else 0 End As Isata , ALDT,\r\n"
                    +
                    " case when BLT1 in ('01  ','02   ','03   ','04   ','05   ') then 'A' when BLT1 in ('01  ','02   ','03   ','04   ','05   ','06  ','07   ','08   ','09   ','10   ','11  ','12   ','13   ','14   ','15   ','16  ','17   ','18   ','19   ','20   ','21  ','22   ','23   ') then 'B' else ' ' end as EXIT1, "
                    +
                    " case when BLT2 in ('01  ','02   ','03   ','04   ','05   ') then 'A' when BLT2 in('01  ','02   ','03   ','04   ','05   ','06  ','07   ','08   ','09   ','10   ','11  ','12   ','13   ','14   ','15   ','16  ','17   ','18   ','19   ','20   ','21  ','22   ','23   ') then 'B' else ' ' end as EXIT2 "
                    +
                    "From Fids_Afttab Aft Inner Join   Fids_Apttab Apt On Aft.Org4=Apt.Apc4   Left Outer Join Fids_Apttab Aptv On Substr(Aft.Vial,5,4)=Aptv.Apc4  \r\n"
                    +
                    " where (aft.ADID='A' or (aft.FTYP='B' and aft.REMP='DSP ') )   "
                    + " and (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) "// Case if Airline is WE and flight
                                                                                   // is cancel then it not show.
                    // + " and aft.SIBT between to_char(sysdate-(7/24)-2,'yyyymmddhh24miss') and
                    // to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') "
                    + " and ((aft.SIBT between to_char(sysdate-(7/24)-2,'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') and aft.ftyp!='X') or  "
                    + "  (aft.SIBT between to_char(sysdate-(7/24)-(1/24),'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(22/24),'yyyymmddhh24miss') and aft.ftyp='X') ) "
                    + " and ((aft.AIBT is null or aft.AIBT = '              ') or (aft.AIBT is not null and aft.AIBT > to_char(sysdate-(7/24)-(60/1440),'yyyymmddhh24miss'))) "
                    + "  and aft.hopo='" + HOPO
                    + "'  and (aft.FLNO!='              '  and aft.FLNO is not null)  And aft.FTYP!='N' " +
                    " and aft.ttyp in ('01','04','05','06','15','16','31','33','41','42','43','44','45','46','52','55','56','61','65','66') "
                    + " and (aft.stev is null or aft.stev!='9') "
                    + " order by aft.SIBT,aft.FLNO";

            System.out.println(SQL);
            PreparedStatement pstmt = c.prepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
            JSONArray result = new JSONArray();

            int count = 0;
            while (rs.next() && (count < 200)) {
                JSONObject row = new JSONObject();
                String REMARK = rs.getString("remark") == null ? "" : rs.getString("remark");
                String newREMARK = getRemarkDelay("ARR", REMARK, remarkobj);

                String FTYP = rs.getString("FTYP") == null ? "" : rs.getString("FTYP");
                String REMP = rs.getString("REMP") == null ? "" : rs.getString("REMP");
                if (FTYP.equals("B") && REMP.equals("DSP ")) {
                    newREMARK = "";
                }

                String ALC2 = rs.getString("ALC2") == null ? " " : rs.getString("ALC2");
                String FLNO = rs.getString("FLNO") == null ? "" : rs.getString("FLNO");
                String JFNO = rs.getString("JFNO") == null ? " " : rs.getString("JFNO");
                String FLTI = rs.getString("FLTI") == null ? "" : rs.getString("FLTI");
                String TERMINAL = FLTI.equals("D") ? getRemarkDelay("DEP", "TERMINAL2", remarkobj)
                        : getRemarkDelay("DEP", "TERMINAL1", remarkobj);
                String TERMINAL1 = getRemarkDelay("DEP", "TERMINAL1", remarkobj);
                String TERMINAL2 = getRemarkDelay("DEP", "TERMINAL2", remarkobj);
                String TERMFLTREMARK = TERMINAL;
                if (newREMARK != null && newREMARK.length() > 0) {
                    TERMFLTREMARK += "|" + newREMARK;
                }
                String TERM1FLTREMARK = TERMINAL1;
                if (newREMARK != null && newREMARK.length() > 0) {
                    TERM1FLTREMARK += "|" + newREMARK;
                }
                String TERM2FLTREMARK = TERMINAL2;
                if (newREMARK != null && newREMARK.length() > 0) {
                    TERM2FLTREMARK += "|" + newREMARK;
                }
                String BLT1 = rs.getString("BLT1") == null ? "" : rs.getString("BLT1");

                String BLT2 = rs.getString("BLT2") == null ? "" : rs.getString("BLT2");

                try {
                    if (BLT1.length() > 0) {
                        BLT1 = Integer.parseInt(BLT1.trim()) + "";
                    }
                } catch (Exception ex) {

                }
                try {
                    if (BLT2.length() > 0) {
                        BLT2 = Integer.parseInt(BLT2.trim()) + "";
                    }
                } catch (Exception ex) {

                }

                String REMBELT1 = rs.getString("REMBELT1") == null ? "" : rs.getString("REMBELT1");
                String REMBELT2 = rs.getString("REMBELT2") == null ? "" : rs.getString("REMBELT2");
                BLT1 = BLT1.trim();
                BLT2 = BLT2.trim();
                String EXIT1 = rs.getString("EXIT1") == null ? "" : rs.getString("EXIT1");
                String EXIT2 = rs.getString("EXIT2") == null ? "" : rs.getString("EXIT2");

                String FULLFLNO = "";
                String LOGO = "";
                String FLTN = "";

                String LASTBAG1 = rs.getString("BAE1") == null ? "" : rs.getString("BAE1").trim();
                String LASTBAG2 = rs.getString("BAE2") == null ? "" : rs.getString("BAE2").trim();

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") && ALC2.equals("DD")) {// For
                                                                                                                  // Nokair
                                                                                                                  // airline
                                                                                                                  // when
                                                                                                                  // FLNO
                                                                                                                  // begin
                                                                                                                  // 0
                                                                                                                  // then
                                                                                                                  // cutoff
                                                                                                                  // 0
                            FLNO = substringForNokair(FLNO);
                            FLTN = Integer.parseInt(FLNO.substring(3).trim()) + "";
                            row.put("FLNO", FLNO.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                if (JFNO == null || JFNO.equals(" ")) {
                    FULLFLNO = FLNO.trim();
                    LOGO = FLNO.substring(0, 3).trim();
                    if (FLTN.equals("")) {
                        FLTN = FLNO.substring(3, 7).trim();
                    }
                    row.put("FULLFLNO", FULLFLNO);
                    row.put("LOGO", LOGO);
                    row.put("FLTN", FLTN);
                    row.put("REMARK", newREMARK);
                    row.put("MAINFLNO", "1");
                    row.put("ALLFLNO", FULLFLNO);
                    row.put("ALLLOGO", LOGO);
                    row.put("ALLFLTN", FLTN);

                    row.put("TERMINAL", TERMINAL);
                    row.put("TERMFLTREMARK", TERMFLTREMARK);
                    row.put("FLIGHTREMARK", newREMARK);

                    row.put("BELT", BLT1);
                    row.put("REMBELT", REMBELT1);
                    row.put("DOMINT", FLTI);
                    row.put("EXITBKK", EXIT1);
                    row.put("LASTBAG", LASTBAG1);

                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        String CITYDOM = rs.getString("CITYDOM") == null ? "" : rs.getString("CITYDOM");
                        String CITYNAMEDOM = rs.getString("CITYNAMEDOM") == null ? "" : rs.getString("CITYNAMEDOM");
                        row.put("CITYALL", CITYINT);
                        row.put("CITYNAMEALL", CITYNAMEINT);
                        row.put("DOMINT", "I");
                        row.put("BELT", BLT1);
                        row.put("REMBELT", REMBELT1);
                        row.put("EXITBKK", EXIT1);
                        row.put("TERMINAL", TERMINAL1);
                        row.put("TERMFLTREMARK", TERM1FLTREMARK);
                        row.put("LASTBAG", LASTBAG1);
                        row.put("MIXFLNO", "0");
                        result.add(row);
                        String rowtext = row.toJSONString();
                        JSONObject rowmix = new JSONObject();
                        JSONParser jp = new JSONParser();
                        rowmix = (JSONObject) jp.parse(rowtext);
                        rowmix.put("CITYALL", CITYDOM);
                        rowmix.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmix.put("DOMINT", "D");
                        rowmix.put("TERMINAL", TERMINAL);
                        rowmix.put("BELT", BLT2);
                        rowmix.put("REMBELT", REMBELT2);
                        rowmix.put("EXITBKK", EXIT2);
                        rowmix.put("TERMINAL", TERMINAL2);
                        rowmix.put("TERMFLTREMARK", TERM2FLTREMARK);
                        rowmix.put("LASTBAG", LASTBAG2);
                        rowmix.put("MIXFLNO", "1");
                        result.add(rowmix);
                    } else {
                        row.put("MIXFLNO", "0");
                        result.add(row);
                    }
                } else {
                    FULLFLNO = FLNO.trim();
                    JFNO = JFNO.replace("   ", "  ");
                    String JFULLFLNO = JFNO.replace("  ", "|");
                    String ALLFLNO = FULLFLNO + "|" + JFULLFLNO;
                    LOGO = FLNO.substring(0, 3).trim();
                    FLTN = FLNO.substring(3, 7).trim();

                    String SHARELOGO = "";
                    String SHAREFLTN = "";

                    String[] JLIST = JFNO.split("  ");
                    for (int i = 0; i < JLIST.length; i++) {
                        if (JLIST[i].length() < 7) {
                            JLIST[i] = JLIST[i] + "       ";
                        }
                        if (i == 0) {
                            SHARELOGO += JLIST[i].substring(0, 3).trim();
                            SHAREFLTN += JLIST[i].substring(3, 7).trim();
                        } else {
                            SHARELOGO += "|" + JLIST[i].substring(0, 3).trim();
                            SHAREFLTN += "|" + JLIST[i].substring(3, 7).trim();
                        }
                    }

                    row.put("FULLFLNO", FULLFLNO);
                    row.put("LOGO", LOGO);
                    row.put("FLTN", FLTN);
                    row.put("REMARK", newREMARK);
                    row.put("MAINFLNO", "1");
                    row.put("ALLFLNO", ALLFLNO);
                    row.put("ALLLOGO", LOGO + "|" + SHARELOGO);
                    row.put("ALLFLTN", FLTN + "|" + SHAREFLTN);

                    row.put("TERMINAL", TERMINAL);
                    row.put("TERMFLTREMARK", TERMFLTREMARK);
                    row.put("FLIGHTREMARK", newREMARK);

                    row.put("BELT", BLT1);
                    row.put("REMBELT", REMBELT1);
                    row.put("DOMINT", FLTI);
                    row.put("EXITBKK", EXIT1);
                    row.put("LASTBAG", LASTBAG1);

                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        row.put("CITYALL", CITYINT);
                        row.put("CITYNAMEALL", CITYNAMEINT);
                        row.put("DOMINT", "I");
                        row.put("BELT", BLT1);
                        row.put("REMBELT", REMBELT1);
                        row.put("EXITBKK", EXIT1);
                        row.put("TERMINAL", TERMINAL1);
                        row.put("TERMFLTREMARK", TERM1FLTREMARK);
                        row.put("LASTBAG", LASTBAG1);
                        row.put("MIXFLNO", "0");
                        result.add(row);
                    } else {
                        row.put("MIXFLNO", "0");
                        result.add(row);
                    }

                    JSONObject rowJ = new JSONObject();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        rowJ.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                    }

                    rowJ.put("FULLFLNO", JFULLFLNO);
                    rowJ.put("LOGO", SHARELOGO);
                    rowJ.put("FLTN", SHAREFLTN);
                    rowJ.put("REMARK", newREMARK);
                    rowJ.put("MAINFLNO", "0");
                    rowJ.put("ALLFLNO", ALLFLNO);
                    rowJ.put("ALLLOGO", LOGO + "|" + SHARELOGO);
                    rowJ.put("ALLFLTN", FLTN + "|" + SHAREFLTN);
                    rowJ.put("TERMINAL", TERMINAL);
                    rowJ.put("TERMFLTREMARK", TERMFLTREMARK);
                    rowJ.put("FLIGHTREMARK", newREMARK);
                    rowJ.put("BELT", BLT1);
                    rowJ.put("REMBELT", REMBELT1);
                    rowJ.put("DOMINT", FLTI);
                    rowJ.put("EXITBKK", EXIT1);
                    rowJ.put("LASTBAG", LASTBAG1);
                    if (FLTI.equals("M")) {
                        String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                        String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT");
                        rowJ.put("CITYALL", CITYINT);
                        rowJ.put("CITYNAMEALL", CITYNAMEINT);
                        rowJ.put("DOMINT", "I");
                        rowJ.put("BELT", BLT1);
                        rowJ.put("REMBELT", REMBELT1);
                        rowJ.put("EXITBKK", EXIT1);
                        rowJ.put("TERMINAL", TERMINAL1);
                        rowJ.put("TERMFLTREMARK", TERM1FLTREMARK);
                        rowJ.put("LASTBAG", LASTBAG1);
                        rowJ.put("MIXFLNO", "0");
                        result.add(rowJ);
                    } else {
                        rowJ.put("MIXFLNO", "0");
                        result.add(rowJ);
                    }

                    if (FLTI.equals("M")) {
                        String CITYDOM = rs.getString("CITYDOM") == null ? "" : rs.getString("CITYDOM");
                        String CITYNAMEDOM = rs.getString("CITYNAMEDOM") == null ? "" : rs.getString("CITYNAMEDOM");

                        String rowtext = row.toJSONString();
                        JSONObject rowmix = new JSONObject();
                        JSONParser jp = new JSONParser();
                        rowmix = (JSONObject) jp.parse(rowtext);
                        rowmix.put("CITYALL", CITYDOM);
                        rowmix.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmix.put("DOMINT", "D");
                        rowmix.put("MAINFLNO", "1");
                        rowmix.put("TERMINAL", TERMINAL);
                        rowmix.put("BELT", BLT2);
                        rowmix.put("REMBELT", REMBELT2);
                        rowmix.put("EXITBKK", EXIT2);
                        rowmix.put("TERMINAL", TERMINAL2);
                        rowmix.put("TERMFLTREMARK", TERM2FLTREMARK);
                        rowmix.put("LASTBAG", LASTBAG2);
                        rowmix.put("MIXFLNO", "1");
                        result.add(rowmix);

                        String rowtextJ = rowJ.toJSONString();
                        JSONObject rowmixJ = new JSONObject();

                        rowmixJ = (JSONObject) jp.parse(rowtextJ);
                        rowmixJ.put("CITYALL", CITYDOM);
                        rowmixJ.put("CITYNAMEALL", CITYNAMEDOM);
                        rowmixJ.put("DOMINT", "D");
                        rowmixJ.put("MAINFLNO", "0");
                        rowmixJ.put("TERMINAL", TERMINAL);
                        rowmixJ.put("BELT", BLT2);
                        rowmixJ.put("REMBELT", REMBELT2);
                        rowmixJ.put("EXITBKK", EXIT2);
                        rowmixJ.put("TERMINAL", TERMINAL2);
                        rowmixJ.put("TERMFLTREMARK", TERM2FLTREMARK);
                        rowmixJ.put("LASTBAG", LASTBAG2);
                        rowmixJ.put("MIXFLNO", "1");
                        result.add(rowmixJ);
                    }

                }

                count++;
            }
            rs.close();
            pstmt.close();
            c.close();

            JSONArray removelist = new JSONArray();
            for (int i = 0; i < result.size(); i++) {
                JSONObject row = (JSONObject) result.get(i);
                if (row.get("FTYP") != null) {
                    if (row.get("FTYP").equals("D")) {

                        boolean isremove = false;
                        for (int j = 0; j < result.size(); j++) {
                            JSONObject rowcheck = (JSONObject) result.get(j);
                            if (!rowcheck.get("FTYP").equals("D") &&
                                    rowcheck.get("FLNO").equals(row.get("FLNO")) &&
                                    rowcheck.get("SIBT").equals(row.get("SIBT"))) {
                                isremove = true;
                                break;
                            }
                        }
                        if (isremove) {
                            removelist.add(i);
                        }

                    }
                }
            }

            for (int i = removelist.size() - 1; i >= 0; i--) {
                int removeindex = (int) removelist.get(i);
                result.remove(removeindex);
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONArray getGateData(String HOPO, Connection crep) {
        try {
            JSONArray result = new JSONArray();
            JSONObject remarkobj = initRemark(crep);
            String SQLGATECHANGE = "SELECT GATECHANGE.URNO,GATECHANGE.FLNO,GATECHANGE.NEWGATE,GATECHANGE.OLDGATE\r\n" +
                    "from \r\n" +
                    "(select DISTINCT FIDS_GATE_HISTORY.URNO, FIDS_GATE_HISTORY.FLNO,currentgate.NEWGATE1 as NEWGATE,FIDS_GATE_HISTORY.OLDGATE1 as OLDGATE\r\n"
                    +
                    "from FIDS_GATE_HISTORY inner join\r\n" +
                    "    (select FIDS_GATE_HISTORY.URNO,FIDS_GATE_HISTORY.NEWGATE1\r\n" +
                    "        from FIDS_GATE_HISTORY inner join\r\n" +
                    "        (select max(UPDATE_TIME) as MAXUPDATE_TIME,URNO\r\n" +
                    "            from FIDS_GATE_HISTORY \r\n" +
                    "            where update_time>sysdate-(3/24) and HOPO='" + HOPO
                    + "' and OLDGATE1!='     ' group by URNO) lastgate \r\n" +
                    "         on FIDS_GATE_HISTORY.UPDATE_TIME=lastgate.MAXUPDATE_TIME \r\n" +
                    "        and FIDS_GATE_HISTORY.URNO=lastgate.URNO) currentgate\r\n" +
                    "on FIDS_GATE_HISTORY.URNO=currentgate.URNO\r\n" +
                    "where FIDS_GATE_HISTORY.update_time>sysdate-(3/24) and FIDS_GATE_HISTORY.HOPO='" + HOPO
                    + "' and FIDS_GATE_HISTORY.OLDGATE1!='     ' and currentgate.NEWGATE1!=FIDS_GATE_HISTORY.OLDGATE1\r\n"
                    +
                    "\r\n" +
                    "UNION\r\n" +
                    "\r\n" +
                    "select DISTINCT FIDS_GATE_HISTORY.URNO,FIDS_GATE_HISTORY.FLNO,currentgate.NEWGATE2 as NEWGATE,FIDS_GATE_HISTORY.OLDGATE2 as OLDGATE\r\n"
                    +
                    "from FIDS_GATE_HISTORY inner join\r\n" +
                    "    (select FIDS_GATE_HISTORY.URNO,FIDS_GATE_HISTORY.NEWGATE2\r\n" +
                    "        from FIDS_GATE_HISTORY inner join\r\n" +
                    "        (select max(UPDATE_TIME) as MAXUPDATE_TIME,URNO\r\n" +
                    "            from FIDS_GATE_HISTORY \r\n" +
                    "            where update_time>sysdate-(3/24) and HOPO='" + HOPO
                    + "' and OLDGATE2!='     ' group by URNO) lastgate \r\n" +
                    "         on FIDS_GATE_HISTORY.UPDATE_TIME=lastgate.MAXUPDATE_TIME \r\n" +
                    "        and FIDS_GATE_HISTORY.URNO=lastgate.URNO) currentgate\r\n" +
                    "on FIDS_GATE_HISTORY.URNO=currentgate.URNO\r\n" +
                    "where FIDS_GATE_HISTORY.update_time>sysdate-(3/24) and FIDS_GATE_HISTORY.HOPO='" + HOPO
                    + "' and FIDS_GATE_HISTORY.OLDGATE2!='     ' and currentgate.NEWGATE2!=FIDS_GATE_HISTORY.OLDGATE2) GATECHANGE\r\n"
                    +
                    "inner join FIDS_AFTTAB aft on GATECHANGE.URNO=aft.URNO\r\n" +
                    "where aft.ADID='D' " +
                    " and (aft.ATOT is null or aft.ATOT='              ')  \r\n" +
                    " and (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) " + // Case if Airline is WE and flight
                                                                                    // is cancel then it not show.
                    " and case when (aft.EOBT is null or aft.EOBT='              ') then aft.SOBT else aft.EOBT end < to_char((sysdate-(7/24))+(3/24),'yyyymmddhh24miss') \r\n"
                    +
                    " and (aft.stev is null or aft.stev!='9') ";
            Statement stmt = crep.createStatement();
            ResultSet rsgate = stmt.executeQuery(SQLGATECHANGE);
            while (rsgate.next()) {
                if (rsgate.getString("OLDGATE") == null) {
                    continue;
                }
                String FLNO = rsgate.getString("FLNO");
                String NEWGATE1 = rsgate.getString("NEWGATE").trim();
                // if((HOPO.equals("HKT")||HOPO.equals("CNX"))&&NEWGATE1!=null&&NEWGATE1.length()>=3){
                // NEWGATE1=NEWGATE1.substring(0, 2);
                // try {
                // NEWGATE1=Integer.parseInt(NEWGATE1)+"";
                // }catch(Exception ex) {
                //
                // }
                // }
                String GATE = rsgate.getString("OLDGATE").trim();
                String DOMINT = "";
                if ((HOPO.equals("HKT") || HOPO.equals("CNX")) && GATE != null && GATE.length() >= 3) {
                    DOMINT = GATE.substring(2, 3);
                    NEWGATE1 = getGate(NEWGATE1);
                    GATE = getGate(GATE);
                    // GATE=GATE.substring(0, 2);
                    // try {
                    // GATE=Integer.parseInt(GATE)+"";
                    // }catch(Exception ex) {
                    //
                    // }
                }
                JSONObject row = new JSONObject();
                String ALC2 = FLNO.substring(0, 3).trim();
                if (ALC2.equals("DD")) {
                    FLNO = substringForNokair(FLNO);
                }
                row.put("NEWFLNO", FLNO);
                row.put("NEWGATE", NEWGATE1);
                row.put("GATE", GATE);
                row.put("DOMINT", DOMINT);
                row.put("TYPE", "GATECHANGE");
                if (GATE.equals(NEWGATE1)) {
                    // Same Gate Didnt Show
                } else {
                    result.add(row);
                }
            }
            stmt.close();

            String SQL = "select aft.FLNO,aft.JFNO,aft.ETOD,aft.GD1Y,aft.GD2Y,aft.FTYP,aft.PSTD," +
                    "  case when TTYP in ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','43','44','45','77','83','91') then 'I' \r\n"
                    +
                    " when TTYP in ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76') then 'D' \r\n"
                    +
                    " when TTYP in ('15','42','46','52','55') then 'M' \r\n" +
                    " else FLTI end FLTI," +
                    " aft.DES3,aft.AOBT,apt.apsn,aft.VIAL,nvl(substr(aft.VIAL,2,3),' ') as VIA,nvl(aptv.APSN,' ') as APSNV,\r\n"
                    +
                    " aft.SOBT,to_char(to_date(aft.SOBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') as std,\r\n" +
                    " aft.eobt,case when (aft.ETOD!='              ' and aft.ETOD is not null) then to_char(to_date(aft.ETOD,'yyyymmddhh24miss')+(7/24),'hh24:mi') else ' ' end as ETD,\r\n"
                    +
                    " aft.gtd1,aft.gtd2,\r\n" +
                    " case when ETOD !='              ' and ETOD is not null then ETOD else SOBT end as LASTD,\r\n" +
                    "case				   \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end)\r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYDOM, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM, \r\n" +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYINT, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT, \r\n" +
                    " case \r\n" +
                    "   when aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) else (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) end as CITYALL,  \r\n"
                    +
                    " case \r\n" +
                    "   when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as CITYNAMEALL," +
                    " case when (AOBT!='              ' and AOBT is not null) and (to_date(AOBT,'yyyymmddhh24miss')+(60/86400))<sysdate-(7/24) then 1 else 0 end as rolloff1, aft.REMP, aft.ATOT\r\n"
                    +
                    " ,Case When Aptv.Aptt Is Null Then Aft.Des3 Else Aft.Des3||'|'||Substr(Vial,2,3) End As Cityall  ,\r\n"
                    +
                    " Case When Aptv.Aptt Is Null Then Apt.Apsn Else Apt.Apsn||'|'||Aptv.Apsn End As Citynameall   ,\r\n"
                    +
                    " case when (GD1Y!='              ' and GD1Y is not null) then 'CLOSE'\r\n" +
                    "  when (GD1Y='              ' or GD1Y  is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
                    "  When (Gd1y='              ' Or Gd1y  Is Null) And Remp='BOA ' Then 'BOARDING'   \r\n" +
                    "  when (GD1Y='              ' or GD1Y  is null) and REMP!='FNC ' and (BOAC='              ' or BOAC  is null) and (BOAO='              ' or BOAO  is null) and (GD1X!='              ' and GD1X is not null) then 'OPEN' \r\n"
                    +
                    "  Else ' ' End As Remgate1   ,\r\n" +
                    "  case when (GD2Y!='              ' and GD2Y is not null) then 'CLOSE'\r\n" +
                    "  when (GD2Y='              ' or GD2Y  is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
                    "  When (Gd2y='              ' Or Gd2y  Is Null) And Remp='BOA ' Then 'BOARDING'   \r\n" +
                    "  when (GD2Y='              ' or GD2Y  is null) and REMP!='FNC ' and (BOAC='              ' or BOAC  is null) and (BOAO='              ' or BOAO  is null) and (GD2X!='              ' and GD2X is not null) then 'OPEN' \r\n"
                    +
                    "  else ' ' end AS REMGATE2,aft.URNO \r\n" +
                    " ,case when FTYP='X' and (eobt!='              ' and eobt is not null) and (to_date(eobt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 \r\n"
                    +
                    "  when FTYP='X' and (to_date(sobt,'yyyymmddhh24miss'))<sysdate-(7/24) then 1 else 0 end as rolloff_dmk3 "
                    + " from FIDS_afttab aft\r\n" +
                    " inner join Fids_apttab apt on aft.des4=apt.APC4\r\n" +
                    " left outer join Fids_APTTAB aptv on substr(aft.VIAL,5,4)=aptv.APC4\r\n" +
                    " where adid='D'\r\n" +
                    // " and ((TTYP in
                    // ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76')
                    // and (case when EOBT !=' ' and EOBT is not null then EOBT else SOBT end)
                    // between to_char(sysdate-(7/24)-(10/1440),'yyyymmddhh24miss') and
                    // to_char(sysdate-(7/24)+(60/1440),'yyyymmddhh24miss') ) or\r\n" +
                    // " (TTYP in
                    // ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','42','43','44','45','46','77','83','91','15','52','55')
                    // and (case when EOBT !=' ' and EOBT is not null then EOBT else SOBT end)
                    // between to_char(sysdate-(7/24)-(10/1440),'yyyymmddhh24miss') and
                    // to_char(sysdate-(7/24)+(60/1440),'yyyymmddhh24miss') ) )\r\n" +
                    " and (TTYP in ('01','04','05','06','15','16','31','33','41','42','43','44','45','46','52','55','56','61','65','66') and (case when EOBT !='              ' and EOBT is not null then EOBT else SOBT end) between to_char(sysdate-(7/24)-(10/1440),'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(60/1440),'yyyymmddhh24miss') )  \r\n"
                    +
                    // " And Aft.Hopo='"+HOPO+"' And (Aft.Gtd1!=' ' and Aft.Gtd1 is not null) And
                    // (Aft.Flno!=' ' and Aft.Flno is not null) And aft.FTYP!='N' \r\n" +
                    " And Aft.Hopo='" + HOPO
                    + "' And (Aft.Flno!='         ' and Aft.Flno is not null) And aft.FTYP!='N' \r\n" +
                    " and (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) " + // Case if Airline is WE and flight
                                                                                    // is cancel then it not show.
                    " and (ATOT='              ' or ATOT is null) " +
                    " and (aft.stev is null or aft.stev!='9') " +
                    " order by case when EOBT !='              ' and EOBT is not null then EOBT else SOBT end,aft.FLNO";

            // System.out.println(SQL);
            PreparedStatement pstmt = crep.prepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
            JSONObject NEXTFlightCheck = new JSONObject();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmss");
                row.put("TYPE", "GATEDATA");

                String FLNO = rs.getString("FLNO") == null ? "" : rs.getString("FLNO");
                String ALC2 = FLNO.substring(0, 3).trim();
                String JFNO = rs.getString("JFNO") == null ? "" : rs.getString("JFNO");

                String GD1Y = rs.getString("GD1Y") == null ? "" : rs.getString("GD1Y");
                String GD2Y = rs.getString("GD2Y") == null ? "" : rs.getString("GD2Y");
                String LASTD = rs.getString("LASTD") == null ? "" : rs.getString("LASTD");
                String PSTD = rs.getString("PSTD") == null ? "" : rs.getString("PSTD").trim();
                if (PSTD.startsWith("0")) {
                    PSTD = PSTD.substring(1);
                }
                String JETBRIDGE = "B" + PSTD;
                row.put("JETBRIDGE", JETBRIDGE);

                boolean GATE1CLOSED = false;
                boolean GATE2CLOSED = false;
                Calendar now = Calendar.getInstance();
                if (GD1Y.trim().length() == 14) {// Has value
                    Calendar g1 = Calendar.getInstance();
                    g1.setTime(sp.parse(GD1Y));
                    g1.add(Calendar.HOUR, 7);
                    g1.add(Calendar.MINUTE, 5);
                    if (g1.before(now)) {
                        GATE1CLOSED = true;
                    }
                }
                if (GD2Y.trim().length() == 14) {// Has value
                    Calendar g2 = Calendar.getInstance();
                    g2.setTime(sp.parse(GD2Y));
                    g2.add(Calendar.HOUR, 7);
                    g2.add(Calendar.MINUTE, 5);
                    if (g2.before(now)) {
                        GATE2CLOSED = true;
                    }
                }

                // if(LASTD.trim().length()==14) {//Has value
                // Calendar g2 = Calendar.getInstance();
                // g2.setTime(sp.parse(LASTD));
                // g2.add(Calendar.HOUR, 7);
                // g2.add(Calendar.MINUTE, 10);// LASTD + 10
                // if(g2.before(now)) {
                // GATE1CLOSED=true;
                // GATE2CLOSED=true;
                // }
                // }

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") && ALC2.equals("DD")) {// For
                                                                                                                  // Nokair
                                                                                                                  // airline
                                                                                                                  // when
                                                                                                                  // FLNO
                                                                                                                  // begin
                                                                                                                  // 0
                                                                                                                  // then
                                                                                                                  // cutoff
                                                                                                                  // 0
                            FLNO = substringForNokair(FLNO);
                            row.put("FLNO", FLNO.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                String LOGO = "";
                if (JFNO == null || JFNO.equals(" ")) {
                    LOGO = FLNO.substring(0, 3).trim();
                    row.put("LOGO", LOGO);
                    row.put("SHAREFLNO", "");
                    row.put("SHARELOGO", "");
                    row.put("ALLFLNO", FLNO.trim());
                    row.put("ALLLOGO", LOGO);

                } else {
                    LOGO = FLNO.substring(0, 3).trim();
                    JFNO = JFNO.replace("   ", "  ");
                    String SHAREFLNO = JFNO.replace("  ", "|");
                    String SHARELOGO = "";
                    String[] JLIST = JFNO.split("  ");
                    for (int i = 0; i < JLIST.length; i++) {
                        if (i == 0) {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        } else {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += "|" + JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        }
                    }
                    row.put("LOGO", LOGO);
                    row.put("SHAREFLNO", SHAREFLNO);
                    row.put("SHARELOGO", SHARELOGO);
                    row.put("ALLFLNO", FLNO.trim() + "|" + SHAREFLNO);
                    row.put("ALLLOGO", LOGO + "|" + SHARELOGO);

                }

                String GTD1 = rs.getString("GTD1") == null ? "" : rs.getString("GTD1").trim();
                String GTD2 = rs.getString("GTD2") == null ? "" : rs.getString("GTD2").trim();

                String GTD1Display = GTD1;
                String GTD2Display = GTD2;
                // String GTD1Display = "B12D";
                String lastIndex = "";
                // if(((HOPO.equals("HKT")||HOPO.equals("CNX")) && GTD1Display.length()>=3)) {
                if ((HOPO.equals("HKT") || HOPO.equals("CNX"))) {
                    // GTD1Display=GTD1Display.substring(0, 2);
                    GTD1Display = getGate(GTD1Display);
                    GTD2Display = getGate(GTD2Display);
                    // if(GTD1Display.length()>0) {
                    // lastIndex = GTD1Display.substring(GTD1Display.length()-1);
                    // if(lastIndex.equals("I")||lastIndex.equals("D")) {//Remove I,D
                    // ex.B03I,13I,B03D,13D
                    // GTD1Display = GTD1Display.substring(0,GTD1Display.length()-1);//B03,13,B03,13
                    // }
                    // }
                    // if(GTD2Display.length()>0) {
                    // lastIndex = GTD2Display.substring(GTD2Display.length()-1);
                    // if(lastIndex.equals("I")||lastIndex.equals("D")) {//Remove I,D
                    // ex.B03I,13I,B03D,13D
                    // GTD2Display = GTD2Display.substring(0,GTD2Display.length()-1);//B03,13,B03,13
                    // }
                    // }
                    // GTD1Display=GTD1Display.replaceAll("0","");//Remove 0 ex.B3,13
                    // GTD2Display=GTD2Display.replaceAll("0","");//Remove 0 ex.B3,13
                }
                // if((HOPO.equals("HKT")||HOPO.equals("CNX")) && GTD2Display.length()>=3) {
                // }

                String REMGATE1 = ""; // GateRemarkChange((rs.getString("REMGATE1")==null?"":rs.getString("REMGATE1")).trim(),remarkobj);
                String REMGATE2 = ""; // GateRemarkChange((rs.getString("REMGATE2")==null?"":rs.getString("REMGATE2")).trim(),remarkobj);

                String REMP = rs.getString("REMP") == null ? "" : rs.getString("REMP");
                String DELAYMSG = "";

                if (REMP.equals("DEL ")) {
                    DELAYMSG = getRemarkDelay("ARR", "DELAYED", remarkobj);// "DELAYED|เธฅเน�เธฒเธ�เน�เธฒ";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("BWE ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Bad Weather", remarkobj);// "Bad
                                                                               // Weather|เธชเธ เธฒเธ�เธญเธฒเธ�เธฒเธจ";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("IND ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Indef.Delayed", remarkobj);// "Indef.Delayed|เธฅเน�เธฒเธ�เน�เธฒเน�เธกเน�เธกเธตเธ�เธณเธซเธ�เธ”";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("NTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "New Time", remarkobj);// "New
                                                                            // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒเน�เธซเธกเน�";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("OTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "On Time", remarkobj);// "On Time|เธ•เธฃเธ�เน€เธงเธฅเธฒ";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("RTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "RETIMED", remarkobj);// "Re
                                                                           // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒ";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                }
                if ((rs.getString("REMGATE1") == null ? "" : rs.getString("REMGATE1")).trim().length() > 0) {
                    REMGATE1 = GateRemarkChange(
                            (rs.getString("REMGATE1") == null ? "" : rs.getString("REMGATE1")).trim(), remarkobj);
                }
                if ((rs.getString("REMGATE2") == null ? "" : rs.getString("REMGATE2")).trim().length() > 0) {
                    REMGATE2 = GateRemarkChange(
                            (rs.getString("REMGATE2") == null ? "" : rs.getString("REMGATE2")).trim(), remarkobj);
                }

                String AOBT = rs.getString("AOBT") == null ? "              " : rs.getString("AOBT");
                if (!AOBT.equals("              ")) {
                    REMGATE1 = getRemarkDelay("ARR", "DEPARTED", remarkobj);
                    REMGATE2 = getRemarkDelay("ARR", "DEPARTED", remarkobj);
                }

                String URNO = rs.getString("URNO");
                String rolloffdmk_fnc = "0";
                Date fncdate = getFinalCallTime(URNO, crep);
                if (fncdate != null) {
                    Calendar current = Calendar.getInstance();
                    current.setTime(new Date());
                    Calendar fnc = Calendar.getInstance();
                    fnc.setTime(fncdate);
                    fnc.add(Calendar.MINUTE, 2);
                    if (fnc.before(current)) {// Final Call +10
                                              // เธ�เน�เธญเธขเธ�เธงเน�เธฒเน€เธงเธฅเธฒเธ�เธฑเธ�เธ�เธธเธ�เธฑเธ�
                        rolloffdmk_fnc = "1";
                    }
                }
                row.put("ROLLOFFDMK_FNC", rolloffdmk_fnc);

                String FLTI = rs.getString("FLTI");
                Calendar startINT = Calendar.getInstance(); // creates calendar
                startINT.add(Calendar.MINUTE, 105);
                Calendar startDOM = Calendar.getInstance(); // creates calendar
                startDOM.add(Calendar.MINUTE, 90);

                // SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmss");
                Calendar sobt = Calendar.getInstance();
                sobt.setTime(sp.parse(rs.getString("SOBT")));
                sobt.add(Calendar.HOUR, 7);

                boolean isDOMDISPLAY = sobt.before(startDOM);
                boolean isINTDISPLAY = sobt.before(startINT);

                if (FLTI != null && FLTI.equals("M")) {
                    String TMPG = GTD1.trim();
                    if ((TMPG.length() > 2) && HOPO.equals("BKK")) {
                        TMPG = TMPG.substring(0, 2);
                    }
                    Integer GCount = NEXTFlightCheck.get(TMPG) != null ? ((Integer) NEXTFlightCheck.get(TMPG)) : 0;
                    row.put("GATE", GTD1Display.trim());
                    row.put("REMGATE", REMGATE1);
                    row.put("DOMINT", "I");
                    if (GCount > 0) {
                        row.put("TYPE", "NEXTFLIGHT" + GCount);
                        row.put("GATE", GTD1Display.trim());
                    } else {
                        row.put("TYPE", "GATEDATA");
                        row.put("GATE", GTD1Display.trim());
                    }
                    row.put("CITYALL", rs.getString("CITYINT"));
                    row.put("CITYNAMEALL", rs.getString("CITYNAMEINT"));
                    row.put("DES3", rs.getString("DES3"));
                    row.put("VIA", "");
                    if (GATE1CLOSED && !HOPO.equals("BKK")) {
                        // Do nothing
                    } else {
                        result.add(row);
                        NEXTFlightCheck.put(TMPG, GCount + 1);
                    }

                    String rowtext = row.toJSONString();
                    JSONParser jp = new JSONParser();
                    JSONObject rowmix = (JSONObject) jp.parse(rowtext);

                    TMPG = GTD2.trim();
                    if (TMPG.length() > 2 && HOPO.equals("BKK")) {
                        TMPG = TMPG.substring(0, 2);
                    }
                    GCount = NEXTFlightCheck.get(TMPG) != null ? ((Integer) NEXTFlightCheck.get(TMPG)) : 0;

                    rowmix.put("GATE", GTD2Display.trim());
                    rowmix.put("REMGATE", REMGATE2);
                    rowmix.put("DOMINT", "D");
                    if (GCount > 0) {
                        rowmix.put("TYPE", "NEXTFLIGHT" + GCount);
                        rowmix.put("GATE", GTD2Display.trim());
                    } else {
                        rowmix.put("TYPE", "GATEDATA");
                        rowmix.put("GATE", GTD2Display.trim());
                    }
                    rowmix.put("CITYALL", rs.getString("CITYDOM"));
                    rowmix.put("CITYNAMEALL", rs.getString("CITYNAMEDOM"));
                    rowmix.put("DES3", rs.getString("VIA").trim().equals("") ? rs.getString("CITYDOM")
                            : rs.getString("VIA").trim());
                    rowmix.put("VIA", "");
                    if (GATE2CLOSED && !HOPO.equals("BKK")) {
                        // Do nothing
                    } else {
                        result.add(rowmix);
                        NEXTFlightCheck.put(TMPG, GCount + 1);
                    }
                } else {
                    String TMPG = GTD1.trim();
                    if (TMPG.length() > 2 && HOPO.equals("BKK")) {
                        TMPG = TMPG.substring(0, 2);
                    }
                    Integer GCount = NEXTFlightCheck.get(TMPG) != null ? ((Integer) NEXTFlightCheck.get(TMPG)) : 0;

                    row.put("GATE", GTD1Display.trim());
                    row.put("REMGATE", REMGATE1);
                    row.put("DOMINT", FLTI);
                    if (GCount > 0) {
                        row.put("TYPE", "NEXTFLIGHT" + GCount);
                        row.put("GATE", GTD1Display.trim());
                    }
                    row.put("CITYALL", rs.getString("CITYALL"));
                    row.put("DES3", rs.getString("DES3"));
                    row.put("VIA", rs.getString("VIA").trim());
                    if (GATE1CLOSED && !HOPO.equals("BKK")) {
                        // Do nothing
                    } else {
                        result.add(row);
                        NEXTFlightCheck.put(TMPG, GCount + 1);
                    }
                }

            }
            rs.close();
            pstmt.close();

            crep.close();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // public JSONArray getCounterData(String HOPO,Connection c ) {
    // try {
    // JSONObject CKICTERMList = getCKICTerminalList(HOPO,c);
    // JSONObject CLASSDB = initClassDB(c);
    // JSONObject remarkobj = initRemark(c);
    //
    // JSONArray result = new JSONArray();
    // JSONObject commonCodeShare = new JSONObject();
    //
    // String SQLDedicate = "select cca.DISP,aft.FLNO,aft.JFNO,CCA.CKIC,\r\n" +
    // "case \r\n" +
    // " when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end)||'|'||(case when
    // substr(VIAL,2,3) is null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),'
    // ') else nvl(substr(VIAL,2,3),' ') end)\r\n" +
    // " when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is
    // null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),' ') else
    // nvl(substr(VIAL,2,3),' ') end) \r\n" +
    // " when apt.APTT='D' and aptv.APTT='I' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) \r\n" +
    // " when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end)||'|'||(case when
    // substr(VIAL,2,3) is null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),'
    // ') else nvl(substr(VIAL,2,3),' ') end) \r\n" +
    // " when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) \r\n" +
    // " when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) end as CITYDOM, \r\n" +
    // " case \r\n" +
    // " when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
    // " when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
    // " when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM,
    // \r\n" +
    // " case \r\n" +
    // " when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end)||'|'||(case when
    // substr(VIAL,2,3) is null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),'
    // ') else nvl(substr(VIAL,2,3),' ') end) \r\n" +
    // " when apt.APTT='I' and aptv.APTT='D' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) \r\n" +
    // " when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is
    // null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),' ') else
    // nvl(substr(VIAL,2,3),' ') end) \r\n" +
    // " when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end)||'|'||(case when
    // substr(VIAL,2,3) is null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),'
    // ') else nvl(substr(VIAL,2,3),' ') end) \r\n" +
    // " when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) \r\n" +
    // " when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end) end as CITYINT, \r\n" +
    // " case \r\n" +
    // " when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
    // " when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
    // " when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
    // " when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT,
    // \r\n" +
    // " case \r\n" +
    // " when aptv.APTT is null then (case when aft.DES3 is null or aft.DES3=' '
    // then aft.DES4 else aft.DES3 end) else (case when aft.DES3 is null or
    // aft.DES3=' ' then aft.DES4 else aft.DES3 end)||'|'||(case when
    // substr(VIAL,2,3) is null or substr(VIAL,2,3)=' ' then nvl(substr(VIAL,5,4),'
    // ') else nvl(substr(VIAL,2,3),' ') end) end as CITYALL, \r\n" +
    // " case \r\n" +
    // " when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as
    // CITYNAMEALL,"+
    // " to_char(to_date(aft.SOBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') as
    // std,aft.GTD1,aft.GTD2,cca.REMA, case when aft.ALC2 is not null and
    // aft.ALC2!=' ' then aft.ALC2 else TRIM(substr(aft.FLNO,0,3)) end ALC2 \r\n" +
    // " ,aft.eobt,case when (aft.ETOD!=' ' and aft.ETOD is not null) then
    // to_char(to_date(aft.ETOD,'yyyymmddhh24miss')+(7/24),'hh24:mi') else ' ' end
    // as ETD,\r\n" +
    // " case when (GD1Y!=' ' and GD1Y is not null) then 'CLOSE'\r\n" +
    // " when (GD1Y=' ' or GD1Y is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
    // " when (GD1Y=' ' or GD1Y is null) and REMP='BOA ' then 'BOARDING' "+
    // " when (GD1Y=' ' or GD1Y is null) and REMP!='FNC ' and (BOAC=' ' or BOAC is
    // null) and (BOAO=' ' or BOAO is null) and (GD1X!=' ' and GD1X is not null)
    // then 'OPEN' \r\n" +
    // " else ' ' end AS REMGATE1 " +
    // " ,case when (GD2Y!=' ' and GD2Y is not null) then 'CLOSE'\r\n" +
    // " when (GD2Y=' ' or GD2Y is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
    // " when (GD2Y=' ' or GD2Y is null) and REMP='BOA ' then 'BOARDING' "+
    // " when (GD2Y=' ' or GD2Y is null) and REMP!='FNC ' and (BOAC=' ' or BOAC is
    // null) and (BOAO=' ' or BOAO is null) and (GD2X!=' ' and GD2X is not null)
    // then 'OPEN' \r\n" +
    // " else ' ' end AS REMGATE2,aft.REMP,aft.AOBT \r\n" +
    // " from FIDS_afttab aft\r\n" +
    // " inner join fids_ccatab cca on aft.urno=cca.flnu\r\n" +
    // " inner join Fids_apttab apt on aft.des4=apt.APC4\r\n" +
    // " left outer join Fids_APTTAB aptv on substr(aft.VIAL,5,4)=aptv.APC4\r\n"
    // +
    // "where aft.adid='D' and aft.hopo='"+HOPO+"' and (ctyp=' ' or ctyp is
    // null)\r\n" +
    // " and (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) "+//Case if
    // Airline is WE and flight is cancel then it not show.
    // " and ckbs is not null and ckbs!=' ' and ckes is not null and ckes!=' '"+
    // " and (CKIC!=' ' and CKIC is not null) "
    // + " and to_char(sysdate-(7/24),'yyyymmddhh24miss') between ckbs and (case
    // when cca.CKEA !=' ' and cca.CKEA is not null then cca.ckea else ckes END) And
    // aft.FTYP!='N'"+
    // " and (aft.stev is null or aft.stev!='9') ";
    // PreparedStatement pstmt = c.prepareStatement(SQLDedicate);
    // ResultSet rs = pstmt.executeQuery();
    // while(rs.next()){
    // JSONObject row = new JSONObject();
    // row.put("DEDICATE", "1");
    //
    // String CKIC = rs.getString("CKIC")==null?"":rs.getString("CKIC");
    // CKIC=CKIC.trim();
    // String TERMID = CKICTERMList.get(CKIC)==null?"":CKICTERMList.get(CKIC)+"";
    // String Airline = rs.getString("ALC2")==null?"":rs.getString("ALC2");
    // String FLNO = rs.getString("FLNO")==null?"":rs.getString("FLNO");
    // String JFNO = rs.getString("JFNO")==null?"":rs.getString("JFNO");
    // String REMGATE1 =
    // GateRemarkChange((rs.getString("REMGATE1")==null?"":rs.getString("REMGATE1")).trim(),remarkobj);
    // String REMGATE2 =
    // GateRemarkChange(rs.getString("REMGATE2")==null?"":rs.getString("REMGATE2").trim(),remarkobj);
    // String REMCTR = "";
    // String REMP = rs.getString("REMP")==null?"":rs.getString("REMP");
    // String DELAYMSG = "";
    //
    //
    // if(REMP.equals("DEL ")){
    // DELAYMSG=getRemarkDelay("ARR", "DELAYED",
    // remarkobj);//"DELAYED|เธฅเน�เธฒเธ�เน�เธฒ";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }else if(REMP.equals("BWE ")){
    // DELAYMSG=getRemarkDelay("ARR", "Bad Weather", remarkobj);//"Bad
    // Weather|เธชเธ เธฒเธ�เธญเธฒเธ�เธฒเธจ";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }else if(REMP.equals("IND ")){
    // DELAYMSG=getRemarkDelay("ARR", "Indef.Delayed",
    // remarkobj);//"Indef.Delayed|เธฅเน�เธฒเธ�เน�เธฒเน�เธกเน�เธกเธตเธ�เธณเธซเธ�เธ”";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }else if(REMP.equals("NTI ")){
    // DELAYMSG=getRemarkDelay("ARR", "New Time", remarkobj);//"New
    // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒเน�เธซเธกเน�";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }else if(REMP.equals("OTI ")){
    // DELAYMSG=getRemarkDelay("ARR", "On Time", remarkobj);//"On
    // Time|เธ•เธฃเธ�เน€เธงเธฅเธฒ";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }else if(REMP.equals("RTI ")){
    // DELAYMSG=getRemarkDelay("ARR", "RETIMED", remarkobj);//"Re
    // Time|เน€เธ�เธฅเธตเน�เธขเธ�เน€เธงเธฅเธฒ";
    // if(REMGATE1.equals("")){
    // REMGATE1=DELAYMSG;
    // }else{
    // REMGATE1+="|"+DELAYMSG;
    // }
    // if(REMGATE2.equals("")){
    // REMGATE2=DELAYMSG;
    // }else{
    // REMGATE2+="|"+DELAYMSG;
    // }
    // if(REMCTR.equals("")){
    // REMCTR=DELAYMSG;
    // }else{
    // REMCTR+="|"+DELAYMSG;
    // }
    // }
    //
    // String AOBT = rs.getString("AOBT")==null?" ":rs.getString("AOBT");
    // if(!AOBT.equals(" ")) {
    // REMGATE1=getRemarkDelay("ARR", "DEPARTED",
    // remarkobj);//"DEPARTED|เน€เธ�เธฃเธทเน�เธญเธ�เธ�เธถเน�เธ�";
    // REMGATE2=getRemarkDelay("ARR", "DEPARTED", remarkobj);
    // REMCTR=getRemarkDelay("ARR", "DEPARTED", remarkobj);
    // }
    //
    //
    //
    // for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
    // if(rs.getString(i)!=null) {
    // if(rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") &&
    // Airline.equals("DD")) {//For Nokair airline when FLNO begin 0 then cutoff 0
    // FLNO = substringForNokair(FLNO);
    // row.put("FLNO", FLNO.trim());
    // }else {
    // row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
    // (rs.getString(i)==null?"":rs.getString(i)).trim());
    // }
    // }else {
    // row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
    // }
    // }
    //
    // row.put("REMGATE1", REMGATE1);
    // row.put("REMGATE2", REMGATE2);
    // row.put("REMCTR", REMCTR);
    //
    // String LOGO="";
    // if(JFNO==null||JFNO.equals(" ")){
    // LOGO=FLNO.substring(0, 3).trim();
    // row.put("LOGO", LOGO);
    // row.put("SHAREFLNO", "");
    // row.put("SHARELOGO", "");
    // row.put("ALLFLNO",FLNO.trim());
    // row.put("ALLLOGO",LOGO);
    //
    // }else{
    //
    // LOGO=FLNO.substring(0, 3).trim();
    // JFNO=JFNO.replace(" ", " ");
    // String SHAREFLNO = JFNO.replace(" ", "|");
    // String SHARELOGO="";
    // String[] JLIST = JFNO.split(" ");
    // for(int i=0;i<JLIST.length;i++){
    // if(i==0){
    // if(JLIST[i].length()>=3) {
    // SHARELOGO+=JLIST[i].substring(0, 3).trim();
    // }else {
    // SHARELOGO+=JLIST[i];
    // }
    // }else{
    // if(JLIST[i].length()>=3) {
    // SHARELOGO+="|"+JLIST[i].substring(0, 3).trim();
    // }else {
    // SHARELOGO+=JLIST[i];
    // }
    // }
    // }
    // row.put("LOGO", LOGO);
    // row.put("SHAREFLNO", SHAREFLNO);
    // row.put("SHARELOGO", SHARELOGO);
    // row.put("ALLFLNO",FLNO.trim()+"|"+SHAREFLNO);
    // row.put("ALLLOGO",LOGO+"|"+SHARELOGO);
    //
    // }
    //
    // String GTD1 = rs.getString("GTD1")==null?"":rs.getString("GTD1").trim();
    // if((HOPO.equals("HKT")||HOPO.equals("CNX"))&&GTD1.length()>=3) {
    // GTD1=GTD1.substring(0, 2);
    // try {
    // GTD1=Integer.parseInt(GTD1)+"";
    // }catch(Exception ex) {
    //
    // }
    // }
    // String GTD2 = rs.getString("GTD2")==null?"":rs.getString("GTD2").trim();
    // if((HOPO.equals("HKT")||HOPO.equals("CNX"))&&GTD2.length()>=3) {
    // GTD2=GTD2.substring(0, 2);
    // try {
    // GTD2=Integer.parseInt(GTD2)+"";
    // }catch(Exception ex) {
    //
    // }
    // }
    // String allgate = "";
    // if(!GTD1.equals("")) {
    // if(!GTD2.equals("")) {
    // allgate=GTD1+"|"+GTD2;
    // }else {
    // allgate=GTD1;
    // }
    // }
    // row.put("ALLGATE", allgate);
    //
    // if(HOPO.equals("CNX")||HOPO.equals("HKT")) {
    // if(TERMID.equals("I")) {
    // row.put("REMGATEBYTERM", REMGATE1);
    // row.put("ALLGATEBYTERM", GTD1);
    // row.put("CITYALL", rs.getString("CITYINT"));
    // row.put("CITYNAMEALL", rs.getString("CITYNAMEINT"));
    // }else if(TERMID.equals("D")) {
    // if(GTD2.length()>0) {
    // row.put("REMGATEBYTERM", REMGATE2);
    // row.put("ALLGATEBYTERM", GTD2);
    // }else {
    // row.put("REMGATEBYTERM", REMGATE1);
    // row.put("ALLGATEBYTERM", GTD1);
    // }
    // row.put("CITYALL", rs.getString("CITYDOM"));
    // row.put("CITYNAMEALL", rs.getString("CITYNAMEDOM"));
    // }
    // }
    //
    //
    // //Check File Exist
    // String DISP = row.get("DISP")!=null?(String)row.get("DISP"):"DEFAULT";
    // if(DISP.length()<=0) {
    // DISP="DEFAULT";
    // }
    // String FilePathCheck = FIDSRepo+"/counter/"+HOPO+"/"+Airline+"_"+DISP+".jpg";
    // File f =new File (FilePathCheck);
    // if(f.exists()) {
    // row = new JSONObject();
    // row.put("CKIC", (rs.getString("CKIC")==null?"":rs.getString("CKIC")).trim());
    // row.put("REMA", Airline+"_"+DISP);
    // row.put("", "1");
    // row.remove("DEDICATE");
    // }else {//File Not Found
    // //Use Default Page
    // row.remove("REMA");
    // }
    //
    // if(row.get("DISP")!=null) {
    // String Key = Airline+"_"+row.get("DISP")+"_"+HOPO;
    // String DefaultKey = "-_"+row.get("DISP")+"_"+HOPO;
    // if(CLASSDB.containsKey(Key)) {
    // row.put("DISP", CLASSDB.get(Key));
    // }else if(CLASSDB.containsKey(DefaultKey)) {
    // row.put("DISP", CLASSDB.get(DefaultKey));
    // }
    // }
    //
    // result.add(row);
    // }
    //
    // String SQLCommon = "select cca.DISP,cca.FLNO,cca.CKIC,cca.REMA \r\n" +
    // " from fids_ccatab cca\r\n" +
    // " where (cca.CKIC!=' ' and cca.CKIC is not null) and cca.hopo='"+HOPO+"' and
    // ctyp='C' and to_char(sysdate-(7/24),'yyyymmddhh24miss') between ckbs and
    // (case when cca.CKEA !=' ' and cca.CKEA is not null then cca.ckea else ckes
    // END)";
    // pstmt = c.prepareStatement(SQLCommon);
    // rs = pstmt.executeQuery();
    // while(rs.next()){
    // JSONObject row = new JSONObject();
    // row.put("COMMON", "1");
    //
    // String Airline = (rs.getString("FLNO")==null?"":rs.getString("FLNO")).trim();
    //
    // for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
    // if(rs.getString(i)!=null) {
    // if(rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO")) {//For
    // Nokair airline when FLNO begin 0 then cutoff 0
    // if(Airline.length()>2) {
    // String ALC2 = Airline.substring(0,3).trim();
    // if(ALC2.equals("DD")) {
    // Airline = substringForNokair(Airline);
    // }
    // }
    // row.put("FLNO", Airline.trim());
    // }else {
    // row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
    // (rs.getString(i)==null?"":rs.getString(i)).trim());
    // }
    // }else {
    // row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
    // }
    // }
    //
    // if(commonCodeShare.get(Airline)==null) {
    // commonCodeShare.put(Airline, getCODESHAREByAirline(Airline, c,HOPO));
    // }
    // if(commonCodeShare.get(Airline)==null) {
    // row.put("CODESHARE","");
    // }else {
    // row.put("CODESHARE", commonCodeShare.get(Airline));
    // }
    //
    //
    // //Check File Exist
    // String DISP = row.get("DISP")!=null?(String)row.get("DISP"):"DEFAULT";
    // if(DISP.length()<=0) {
    // DISP="DEFAULT";
    // }
    // String FilePathCheck = FIDSRepo+"/counter/"+HOPO+"/"+Airline+"_"+DISP+".jpg";
    // File f =new File (FilePathCheck);
    // if(f.exists()) {
    // row = new JSONObject();
    // row.put("CKIC", (rs.getString("CKIC")==null?"":rs.getString("CKIC")).trim());
    // row.put("REMA", Airline+"_"+DISP);
    // row.put("", "1");
    // row.remove("COMMON");
    // }else {//File Not Found
    // //Use Default Page
    // row.remove("REMA");
    // }
    //
    // if(row.get("DISP")!=null) {
    // String Key = Airline+"_"+row.get("DISP")+"_"+HOPO;
    // String DefaultKey = "-_"+row.get("DISP")+"_"+HOPO;
    // if(CLASSDB.containsKey(Key)) {
    // row.put("DISP", CLASSDB.get(Key));
    // }else if(CLASSDB.containsKey(DefaultKey)) {
    // row.put("DISP", CLASSDB.get(DefaultKey));
    // }
    // }
    // result.add(row);
    // }
    //
    //
    // rs.close();
    // pstmt.close();
    // c.close();
    // return result;
    // }catch(Exception ex) {
    // ex.printStackTrace();
    // }
    // return null;
    // }

    public JSONArray getCounterData(String HOPO, Connection c) {
        try {
            JSONObject CKICTERMList = getCKICTerminalList(HOPO, c);
            JSONObject CLASSDB = initClassDB(c);
            JSONObject remarkobj = initRemark(c);
            JSONArray result = new JSONArray();

            String SQLDedicate = "select cca.DISP,aft.FLNO,aft.JFNO,CCA.CKIC,\r\n" +
                    "case				   \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end)\r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYDOM, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM, \r\n" +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) end as CITYINT, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT, \r\n" +
                    " case \r\n" +
                    "   when aptv.APTT is null then (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end) else (case when aft.DES3 is null or aft.DES3='   ' then aft.DES4 else aft.DES3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) end as CITYALL,  \r\n"
                    +
                    " case \r\n" +
                    "   when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as CITYNAMEALL," +
                    " to_char(to_date(aft.SOBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') as std,aft.GTD1,aft.GTD2,cca.REMA, case when aft.ALC2 is not null and aft.ALC2!='  ' then aft.ALC2 else TRIM(substr(aft.FLNO,0,3)) end ALC2 \r\n"
                    +
                    " ,aft.eobt,case when (aft.ETOD!='              ' and aft.ETOD is not null) then to_char(to_date(aft.ETOD,'yyyymmddhh24miss')+(7/24),'hh24:mi') else ' ' end as ETD,\r\n"
                    +
                    "  case when (GD1Y!='              ' and GD1Y is not null) then 'CLOSE'\r\n" +
                    "  when (GD1Y='              ' or GD1Y is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
                    "  when (GD1Y='              ' or GD1Y is null) and REMP='BOA ' then 'BOARDING' " +
                    "  when (GD1Y='              ' or GD1Y is null) and REMP!='FNC ' and (BOAC='              ' or BOAC is null) and (BOAO='              ' or BOAO is null) and (GD1X!='              ' and GD1X is not null) then 'OPEN' \r\n"
                    +
                    "  else ' ' end AS REMGATE1 " +
                    "  ,case when (GD2Y!='              ' and GD2Y is not null) then 'CLOSE'\r\n" +
                    "  when (GD2Y='              ' or GD2Y is null) and REMP='FNC ' then 'FINAL CALL'\r\n" +
                    "  when (GD2Y='              ' or GD2Y is null) and REMP='BOA ' then 'BOARDING' " +
                    "  when (GD2Y='              ' or GD2Y is null) and REMP!='FNC ' and (BOAC='              ' or BOAC is null) and (BOAO='              ' or BOAO is null) and (GD2X!='              ' and GD2X is not null) then 'OPEN' \r\n"
                    +
                    "  else ' ' end AS REMGATE2,aft.REMP,aft.AOBT \r\n" +
                    " from FIDS_afttab aft\r\n" +
                    " inner join fids_ccatab cca on aft.urno=cca.flnu\r\n" +
                    " inner join Fids_apttab apt on aft.des4=apt.APC4\r\n" +
                    " left outer join Fids_APTTAB aptv on substr(aft.VIAL,5,4)=aptv.APC4\r\n" +
                    " where aft.adid='D' and aft.hopo='" + HOPO + "' and (ctyp=' ' or ctyp is null)\r\n" +
                    " and ckbs is not null and ckbs!='              ' and ckes is not null and ckes!='              '" +
                    " and (CKIC!='     ' and CKIC is not null) "
                    + " and to_char(sysdate-(7/24),'yyyymmddhh24miss') between ckbs and (case when cca.CKEA !='              ' and cca.CKEA is not null then cca.ckea else ckes END) And aft.FTYP!='N'"
                    +
                    " and (aft.stev is null or aft.stev!='9') ";
            PreparedStatement pstmt = c.prepareStatement(SQLDedicate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("DEDICATE", "1");

                String CKIC = rs.getString("CKIC") == null ? "" : rs.getString("CKIC");
                CKIC = CKIC.trim();
                String TERMID = CKICTERMList.get(CKIC) == null ? "" : CKICTERMList.get(CKIC) + "";
                String Airline = rs.getString("ALC2") == null ? "" : rs.getString("ALC2");
                String FLNO = rs.getString("FLNO") == null ? "" : rs.getString("FLNO");
                String JFNO = rs.getString("JFNO") == null ? "" : rs.getString("JFNO");
                String REMGATE1 = GateRemarkChange(
                        (rs.getString("REMGATE1") == null ? "" : rs.getString("REMGATE1")).trim(), remarkobj);
                String REMGATE2 = GateRemarkChange(
                        rs.getString("REMGATE2") == null ? "" : rs.getString("REMGATE2").trim(), remarkobj);
                String REMCTR = "";
                String REMP = rs.getString("REMP") == null ? "" : rs.getString("REMP");
                String DELAYMSG = "";

                if (REMP.equals("DEL ")) {
                    DELAYMSG = getRemarkDelay("ARR", "DELAYED", remarkobj);// "DELAYED|à¸¥à¹ˆà¸²à¸Šà¹‰à¸²";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("BWE ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Bad Weather", remarkobj);// "Bad
                                                                               // Weather|à¸ªà¸ à¸²à¸žà¸­à¸²à¸�à¸²à¸¨";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("IND ")) {
                    DELAYMSG = getRemarkDelay("ARR", "Indef.Delayed", remarkobj);// "Indef.Delayed|à¸¥à¹ˆà¸²à¸Šà¹‰à¸²à¹„à¸¡à¹ˆà¸¡à¸µà¸�à¸³à¸«à¸™à¸”";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("NTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "New Time", remarkobj);// "New
                                                                            // Time|à¹€à¸›à¸¥à¸µà¹ˆà¸¢à¸™à¹€à¸§à¸¥à¸²à¹ƒà¸«à¸¡à¹ˆ";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("OTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "On Time", remarkobj);// "On Time|à¸•à¸£à¸‡à¹€à¸§à¸¥à¸²";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                } else if (REMP.equals("RTI ")) {
                    DELAYMSG = getRemarkDelay("ARR", "RETIMED", remarkobj);// "Re
                                                                           // Time|à¹€à¸›à¸¥à¸µà¹ˆà¸¢à¸™à¹€à¸§à¸¥à¸²";
                    if (REMGATE1.equals("")) {
                        REMGATE1 = DELAYMSG;
                    } else {
                        REMGATE1 += "|" + DELAYMSG;
                    }
                    if (REMGATE2.equals("")) {
                        REMGATE2 = DELAYMSG;
                    } else {
                        REMGATE2 += "|" + DELAYMSG;
                    }
                    if (REMCTR.equals("")) {
                        REMCTR = DELAYMSG;
                    } else {
                        REMCTR += "|" + DELAYMSG;
                    }
                }

                String AOBT = rs.getString("AOBT") == null ? "              " : rs.getString("AOBT");
                if (!AOBT.equals("              ")) {
                    REMGATE1 = getRemarkDelay("ARR", "DEPARTED", remarkobj);// "DEPARTED|à¹€à¸„à¸£à¸·à¹ˆà¸­à¸‡à¸‚à¸¶à¹‰à¸™";
                    REMGATE2 = getRemarkDelay("ARR", "DEPARTED", remarkobj);
                    REMCTR = getRemarkDelay("ARR", "DEPARTED", remarkobj);
                }

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") && Airline.equals("DD")) {// For
                                                                                                                     // Nokair
                                                                                                                     // airline
                                                                                                                     // when
                                                                                                                     // FLNO
                                                                                                                     // begin
                                                                                                                     // 0
                                                                                                                     // then
                                                                                                                     // cutoff
                                                                                                                     // 0
                            FLNO = substringForNokair(FLNO);
                            row.put("FLNO", FLNO.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                row.put("REMGATE1", REMGATE1);
                row.put("REMGATE2", REMGATE2);
                row.put("REMCTR", REMCTR);

                String LOGO = "";
                if (JFNO == null || JFNO.equals(" ")) {
                    LOGO = FLNO.substring(0, 3).trim();// FLNO.split(" ")[0];
                    row.put("LOGO", LOGO);
                    row.put("SHAREFLNO", "");
                    row.put("SHARELOGO", "");
                    row.put("ALLFLNO", FLNO.trim());
                    row.put("ALLLOGO", LOGO);

                } else {

                    LOGO = FLNO.substring(0, 3).trim();// FLNO.split(" ")[0];
                    JFNO = JFNO.replace("   ", "  ");
                    String SHAREFLNO = JFNO.replace("  ", "|");
                    String SHARELOGO = "";
                    String[] JLIST = JFNO.split("  ");
                    for (int i = 0; i < JLIST.length; i++) {
                        if (i == 0) {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        } else {
                            if (JLIST[i].length() >= 3) {
                                SHARELOGO += "|" + JLIST[i].substring(0, 3).trim();
                            } else {
                                SHARELOGO += JLIST[i];
                            }
                        }
                    }
                    row.put("LOGO", LOGO);
                    row.put("SHAREFLNO", SHAREFLNO);
                    row.put("SHARELOGO", SHARELOGO);
                    row.put("ALLFLNO", FLNO.trim() + "|" + SHAREFLNO);
                    row.put("ALLLOGO", LOGO + "|" + SHARELOGO);

                }

                String GTD1 = rs.getString("GTD1") == null ? "" : rs.getString("GTD1").trim();
                if ((HOPO.equals("HKT") || HOPO.equals("CNX")) && GTD1.length() >= 3) {
                    GTD1 = GTD1.substring(0, 2);
                    try {
                        GTD1 = Integer.parseInt(GTD1) + "";
                    } catch (Exception ex) {

                    }
                }
                String GTD2 = rs.getString("GTD2") == null ? "" : rs.getString("GTD2").trim();
                if ((HOPO.equals("HKT") || HOPO.equals("CNX")) && GTD2.length() >= 3) {
                    GTD2 = GTD2.substring(0, 2);
                    try {
                        GTD2 = Integer.parseInt(GTD2) + "";
                    } catch (Exception ex) {

                    }
                }
                String allgate = "";
                if (!GTD1.equals("")) {
                    if (!GTD2.equals("")) {
                        allgate = GTD1 + "|" + GTD2;
                    } else {
                        allgate = GTD1;
                    }
                }
                row.put("ALLGATE", allgate);

                if (HOPO.equals("DMK") || HOPO.equals("CNX")) {
                    if (TERMID.equals("1")) {
                        row.put("REMGATEBYTERM", REMGATE1);
                        row.put("ALLGATEBYTERM", GTD1);
                        row.put("CITYALL", rs.getString("CITYINT"));
                        row.put("CITYNAMEALL", rs.getString("CITYNAMEINT"));
                    } else if (TERMID.equals("2")) {
                        if (GTD2.length() > 0) {
                            row.put("REMGATEBYTERM", REMGATE2);
                            row.put("ALLGATEBYTERM", GTD2);
                        } else {
                            row.put("REMGATEBYTERM", REMGATE1);
                            row.put("ALLGATEBYTERM", GTD1);
                        }
                        row.put("CITYALL", rs.getString("CITYDOM"));
                        row.put("CITYNAMEALL", rs.getString("CITYNAMEDOM"));
                    }
                }

                if (HOPO.equals("HKT")) {
                    if (TERMID.equals("I")) {
                        row.put("REMGATEBYTERM", REMGATE1);
                        row.put("ALLGATEBYTERM", GTD1);
                        row.put("CITYALL", rs.getString("CITYINT"));
                        row.put("CITYNAMEALL", rs.getString("CITYNAMEINT"));
                    } else if (TERMID.equals("D")) {
                        if (GTD2.length() > 0) {
                            row.put("REMGATEBYTERM", REMGATE2);
                            row.put("ALLGATEBYTERM", GTD2);
                        } else {
                            row.put("REMGATEBYTERM", REMGATE1);
                            row.put("ALLGATEBYTERM", GTD1);
                        }
                        row.put("CITYALL", rs.getString("CITYDOM"));
                        row.put("CITYNAMEALL", rs.getString("CITYNAMEDOM"));
                    }
                }

                if (HOPO.equals("BKK") || HOPO.equals("DMK") || HOPO.equals("CNX")) {// Using CMID Airport
                    // if(rs.getString("CKIC").startsWith("Y")) {
                    // row = new JSONObject();
                    // row.put("CKIC", rs.getString("CKIC")==null?"":rs.getString("CKIC").trim());
                    // String DISP = "";
                    // if(rs.getString("DISP")!=null&&rs.getString("DISP").length()>0) {
                    // DISP=rs.getString("DISP")+"";
                    // DISP=DISP.trim();
                    // if(DISP.length()>0) {
                    // DISP="_"+DISP;
                    // }else {
                    // DISP="";
                    // }
                    // }
                    // row.put("REMA",
                    // (rs.getString("ALC2")==null?"":rs.getString("ALC2").trim())+DISP+"_TRANSFERDEFAULT");
                    // }else {
                    // if((rs.getString("REMA")==null?"":rs.getString("REMA").trim()).equals("")) {
                    // row.remove("REMA");
                    // }else {
                    // row = new JSONObject();
                    // row.put("CKIC", rs.getString("CKIC")==null?"":rs.getString("CKIC").trim());
                    // row.put("REMA",
                    // (rs.getString("ALC2")==null?"":rs.getString("ALC2").trim())+"_"+(rs.getString("REMA")==null?"":rs.getString("REMA").trim()));
                    // }
                    // }
                    if (rs.getString("CKIC").startsWith("Y")) {
                        row = new JSONObject();
                        row.put("CKIC", rs.getString("CKIC") == null ? "" : rs.getString("CKIC").trim());
                        String DISP = "";
                        if (rs.getString("DISP") != null && rs.getString("DISP").length() > 0) {
                            DISP = rs.getString("DISP") + "";
                            DISP = DISP.trim();
                            if (DISP.length() > 0) {
                                DISP = "_" + DISP;
                            } else {
                                DISP = "";
                            }
                        }
                        row.put("REMA", (rs.getString("ALC2") == null ? "" : rs.getString("ALC2").trim()) + DISP
                                + "_TRANSFERDEFAULT");
                        row.put("", "1");
                    } else {
                        if ((rs.getString("REMA") == null ? "" : rs.getString("REMA").trim()).equals("")) {
                            row.remove("REMA");
                        } else {
                            row = new JSONObject();
                            // System.out.println(rs.getString("ALC2"));
                            // System.out.println(rs.getString("REMA"));
                            row.put("CKIC", rs.getString("CKIC") == null ? "" : rs.getString("CKIC").trim());
                            row.put("REMA", (rs.getString("ALC2") == null ? "" : rs.getString("ALC2").trim()) + "_"
                                    + (rs.getString("REMA") == null ? "" : rs.getString("REMA").trim()));
                            row.put("", "1");
                        }
                    }
                } else { // NON CMID AIRPORT
                         // Check File Exist
                    String DISP = row.get("DISP") != null ? (String) row.get("DISP") : "DEFAULT";
                    if (DISP.length() <= 0) {
                        DISP = "DEFAULT";
                    }
                    String FilePathCheck = FIDSRepo + "/counter/" + HOPO + "/" + Airline + "_" + DISP + ".jpg";
                    File f = new File(FilePathCheck);
                    if (f.exists()) {
                        row = new JSONObject();
                        row.put("CKIC", (rs.getString("CKIC") == null ? "" : rs.getString("CKIC")).trim());
                        row.put("REMA", Airline + "_" + DISP);
                        row.put("", "1");
                        row.remove("DEDICATE");
                    } else {// File Not Found
                            // Use Default Page
                        row.remove("REMA");
                    }
                }
                if (row.get("DISP") != null) {
                    String Key = Airline + "_" + row.get("DISP") + "_" + HOPO;
                    String DefaultKey = "-_" + row.get("DISP") + "_" + HOPO;
                    if (CLASSDB.containsKey(Key)) {
                        row.put("DISP", CLASSDB.get(Key));
                    } else if (CLASSDB.containsKey(DefaultKey)) {
                        row.put("DISP", CLASSDB.get(DefaultKey));
                    }
                }

                result.add(row);
            }

            String SQLCommon = "select cca.DISP,cca.FLNO,cca.CKIC,cca.REMA \r\n" +
                    " from fids_ccatab cca\r\n" +
                    " where (cca.CKIC!='     ' and cca.CKIC is not null) and cca.hopo='" + HOPO
                    + "' and ctyp='C' and to_char(sysdate-(7/24),'yyyymmddhh24miss') between ckbs and (case when cca.CKEA !='              ' and cca.CKEA is not null then cca.ckea else ckes END)";
            pstmt = c.prepareStatement(SQLCommon);
            rs = pstmt.executeQuery();

            JSONObject commonCodeShare = new JSONObject();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("COMMON", "1");

                String Airline = (rs.getString("FLNO") == null ? "" : rs.getString("FLNO")).trim();

                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO")) {// For Nokair airline when
                                                                                             // FLNO begin 0 then cutoff
                                                                                             // 0
                            if (Airline.length() > 2) {
                                String ALC2 = Airline.substring(0, 3).trim();
                                if (ALC2.equals("DD")) {
                                    Airline = substringForNokair(Airline);
                                }
                            }
                            row.put("FLNO", Airline.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                if (commonCodeShare.get(Airline) == null) {
                    commonCodeShare.put(Airline, getCODESHAREByAirline(Airline, c, HOPO));
                }
                if (commonCodeShare.get(Airline) == null) {
                    row.put("CODESHARE", "");
                } else {
                    row.put("CODESHARE", commonCodeShare.get(Airline));
                }

                if (HOPO.equals("BKK") || HOPO.equals("DMK") || HOPO.equals("CNX")) {// Using CMID Airport
                    // if(rs.getString("CKIC").startsWith("Y")) {
                    // row = new JSONObject();
                    // row.put("CKIC", rs.getString("CKIC")==null?"":rs.getString("CKIC").trim());
                    // String DISP = "";
                    // if(rs.getString("DISP")!=null&&rs.getString("DISP").length()>0) {
                    // DISP=rs.getString("DISP")+"";
                    // DISP=DISP.trim();
                    // if(DISP.length()>0) {
                    // DISP="_"+DISP;
                    // }else {
                    // DISP="";
                    // }
                    // }
                    // row.put("REMA",
                    // (rs.getString("FLNO")==null?"":rs.getString("FLNO").trim())+DISP+"_TRANSFERDEFAULT");
                    // }else {
                    // if((rs.getString("REMA")==null?"":rs.getString("REMA")).trim().equals("")) {
                    // row.remove("REMA");
                    // }else {
                    // row = new JSONObject();
                    // row.put("CKIC", (rs.getString("CKIC")==null?"":rs.getString("CKIC")).trim());
                    // row.put("REMA",
                    // (rs.getString("FLNO")==null?"":rs.getString("FLNO")).trim()+"_"+(rs.getString("REMA")==null?"":rs.getString("REMA")).trim());
                    // }
                    // }
                    if (rs.getString("CKIC").startsWith("Y")) {
                        row = new JSONObject();
                        row.put("CKIC", rs.getString("CKIC") == null ? "" : rs.getString("CKIC").trim());
                        String DISP = "";
                        if (rs.getString("DISP") != null && rs.getString("DISP").length() > 0) {
                            DISP = rs.getString("DISP") + "";
                            DISP = DISP.trim();
                            if (DISP.length() > 0) {
                                DISP = "_" + DISP;
                            } else {
                                DISP = "";
                            }
                        }
                        row.put("REMA", (rs.getString("FLNO") == null ? "" : rs.getString("FLNO").trim()) + DISP
                                + "_TRANSFERDEFAULT");
                        row.put("", "1");
                    } else {
                        if ((rs.getString("REMA") == null ? "" : rs.getString("REMA")).trim().equals("")) {
                            row.remove("REMA");
                        } else {
                            row = new JSONObject();
                            row.put("CKIC", (rs.getString("CKIC") == null ? "" : rs.getString("CKIC")).trim());
                            row.put("REMA", (rs.getString("FLNO") == null ? "" : rs.getString("FLNO")).trim() + "_"
                                    + (rs.getString("REMA") == null ? "" : rs.getString("REMA")).trim());
                            row.put("", "1");
                        }
                    }
                } else { // NON CMID AIRPORT
                         // Check File Exist
                    String DISP = row.get("DISP") != null ? (String) row.get("DISP") : "DEFAULT";
                    if (DISP.length() <= 0) {
                        DISP = "DEFAULT";
                    }
                    String FilePathCheck = FIDSRepo + "/counter/" + HOPO + "/" + Airline + "_" + DISP + ".jpg";
                    File f = new File(FilePathCheck);
                    if (f.exists()) {
                        row = new JSONObject();
                        row.put("CKIC", (rs.getString("CKIC") == null ? "" : rs.getString("CKIC")).trim());
                        row.put("REMA", Airline + "_" + DISP);
                        row.put("", "1");
                        row.remove("COMMON");
                    } else {// File Not Found
                            // Use Default Page
                        row.remove("REMA");
                    }
                }
                if (row.get("DISP") != null) {
                    String Key = Airline + "_" + row.get("DISP") + "_" + HOPO;
                    String DefaultKey = "-_" + row.get("DISP") + "_" + HOPO;
                    if (CLASSDB.containsKey(Key)) {
                        row.put("DISP", CLASSDB.get(Key));
                    } else if (CLASSDB.containsKey(DefaultKey)) {
                        row.put("DISP", CLASSDB.get(DefaultKey));
                    }
                }
                result.add(row);
            }

            rs.close();
            pstmt.close();
            c.close();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONArray getBeltData(String HOPO, Connection c) {
        try {
            JSONObject remarkobj = initRemark(c);
            SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmss");
            String SQL = "select aft.FLNO,aft.JFNO," +
                    "  case when TTYP in ('01','02','03','04','08','09','16','22','28','30','31','33','34','41','43','44','45','77','83','91') then 'I' \r\n"
                    +
                    " when TTYP in ('05','06','50','51','56','58','59','60','61','62','63','64','65','66','68','76') then 'D' \r\n"
                    +
                    " when TTYP in ('15','42','46','52','55') then 'M' \r\n" +
                    " else FLTI end FLTI," +
                    " case when aft.ALC2 is not null and aft.ALC2!='  ' then aft.ALC2 else TRIM(substr(FLNO,0,3)) end LOGO, aft.ORG3,apt.APSN,aft.ETOA,aft.AIBT,aft.URNO,\r\n"
                    +
                    "aft.SIBT,case when (aft.AIBT!='              ' and aft.AIBT is not null) then to_char(to_date(aft.AIBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') when (ETOA != '              ' and ETOA is not null) then to_char(to_date(aft.EIBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') else to_char(to_date(aft.SIBT,'yyyymmddhh24miss')+(7/24),'hh24:mi') end as sta,\r\n"
                    +
                    " case when (aft.ALDT!='              ' and aft.ALDT is not null) then to_char(to_date(aft.ALDT,'yyyymmddhh24miss')+(7/24),'hh24:mi') else '' end as ATA, "
                    +
                    "case when FTYP='X' then 'Cancelled'\r\n" +
                    "when (ALDT!='              ' and ALDT is not null) then 'LANDED' \r\n" +
                    "when FTYP='D' then 'DIVERTED'\r\n" +
                    "when (ETOA!='              ' and ETOA is not null) then 'Confirmed'\r\n" +
                    "when remp='DEL ' then 'DELAYED'\r\n" +
                    "when remp='RTI ' then 'RETIMED'\r\n" +
                    "when remp='IND ' then 'Indef.Delayed'\r\n" +
                    "when remp='BWE ' then 'Bad Weather'\r\n" +
                    "when remp='NTI ' then 'New Time'\r\n" +
                    "else ' ' end as remark,aft.BLT1,aft.BLT2,\r\n" +
                    " case				   \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end)\r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) end as CITYDOM, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEDOM, \r\n" +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='D' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT='I' then (case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT='I' then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) \r\n"
                    +
                    "   when apt.APTT='D' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) \r\n"
                    +
                    "   when apt.APTT='I' and aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) end as CITYINT, \r\n"
                    +
                    " case \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='D' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='D' then apt.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT='I' then aptv.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT='I' then apt.APSN||'|'||aptv.APSN \r\n" +
                    "   when apt.APTT='D' and aptv.APTT is null then apt.APSN \r\n" +
                    "   when apt.APTT='I' and aptv.APTT is null then apt.APSN end as CITYNAMEINT, \r\n" +
                    " case \r\n" +
                    "   when aptv.APTT is null then (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end) else (case when aft.ORG3 is null or aft.ORG3='   ' then aft.ORG4 else aft.ORG3 end)||'|'||(case when substr(VIAL,2,3) is null or  substr(VIAL,2,3)='   ' then nvl(substr(VIAL,5,4),'   ') else nvl(substr(VIAL,2,3),'   ') end) end as CITYALL,  \r\n"
                    +
                    " case \r\n" +
                    "   when aptv.APTT is null then apt.APSN else apt.APSN||'|'||aptv.APSN end as CITYNAMEALL," +

                    "case when FTYP='X' and (eibt!='              ' and eibt is not null) and (to_date(eibt,'yyyymmddhh24miss'))<sysdate-(7/24)-(20/1440) then 1 \r\n"
                    +
                    " when FTYP='X' and (to_date(sibt,'yyyymmddhh24miss'))<sysdate-(7/24)-(20/1440) then 1 else 0 end as rolloff_dmk1,\r\n"
                    +
                    "case when  "
                    + "to_char(sysdate-(7/24) ,'yyyymmddhh24miss') between " +
                    " case when b1ba!='              ' and b1ba is not null then b1ba when b1bs!='              ' and b1bs is not null then b1bs else '19810101000000' end "
                    +
                    " and case when b1ea!='              ' and b1ea is not null then b1ea when b1es!='              ' and b1es is not null then b1es else '19810101000000' end "
                    +
                    " then 'OPEN' else 'CLOSE' end as BELT1STATUS," +
                    "case when  "
                    + "to_char(sysdate-(7/24) ,'yyyymmddhh24miss') between " +
                    " case when b2ba!='              ' and b2ba is not null then b2ba when b2bs!='              ' and b2bs is not null then b2bs else '19810101000000' end "
                    +
                    " and case when b2ea!='              ' and b2ea is not null then b2ea when b2es!='              ' and b2es is not null then b2es else '19810101000000' end "
                    +
                    " then 'OPEN' else 'CLOSE' end as BELT2STATUS, " +
                    " case when b1ba!='              ' and b1ba is not null then b1ba when b1bs!='              ' and b1bs is not null then b1bs else '19810101000000' end as BELT1START,"
                    +
                    " case when b1ea!='              ' and b1ea is not null then b1ea when b1es!='              ' and b1es is not null then b1es else '19810101000000' end as BELT1END, "
                    +
                    " case when b2ba!='              ' and b2ba is not null then b2ba when b2bs!='              ' and b2bs is not null then b2bs else '19810101000000' end as BELT2START,"
                    +
                    " case when b2ea!='              ' and b2ea is not null then b2ea when b2es!='              ' and b2es is not null then b2es else '19810101000000' end as BELT2END "
                    +
                    " from FIDS_AFTTAB aft inner join \r\n" +
                    "  Fids_APTTAB apt on aft.ORG4=apt.APC4 \r\n" +
                    "  left outer join Fids_APTTAB aptv on substr(aft.VIAL,5,4)=aptv.APC4\r\n" +
                    "where aft.HOPO='" + HOPO + "' and ADID='A' \r\n" +
                    " and aft.ALDT !='              ' and aft.ALDT is not null \r\n" +
                    " and (to_char(sysdate-(7/24) ,'yyyymmddhh24miss') between  \r\n" +
                    " case when b1ba!='              ' and b1ba is not null then b1ba when b1bs!='              ' and b1bs is not null then b1bs else '19810101000000' end  \r\n"
                    +
                    " and case when b1ea!='              ' and b1ea is not null then b1ea when b1es!='              ' and b1es is not null then b1es else '19810101000000' end  \r\n"
                    +
                    " or  \r\n" +
                    " to_char(sysdate-(7/24) ,'yyyymmddhh24miss') between  \r\n" +
                    " case when b2ba!='              ' and b2ba is not null then b2ba when b2bs!='              ' and b2bs is not null then b2bs else '19810101000000' end  \r\n"
                    +
                    " and case when b2ea!='              ' and b2ea is not null then b2ea when b2es!='              ' and b2es is not null then b2es else '19810101000000' end ) "
                    +
                    " and (aft.BLT1!='     ' and aft.BLT1 is not null)  And aft.FTYP!='N'\r\n" +
                    " and (aft.ALC2!='WE' or (aft.ALC2='WE' and aft.FTYP!='X')) " + // Case if Airline is WE and flight
                                                                                    // is cancel then it not show.
                    " and (aft.stev is null or aft.stev!='9') " +
                    "ORDER BY case when EIBT !='              ' and EIBT is not null then EIBT else SIBT end,aft.FLNO";
            // System.out.println(SQL);
            PreparedStatement pstmt = c.prepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
            JSONArray result = new JSONArray();
            while (rs.next()) {
                JSONObject row = new JSONObject();

                String REMARK = rs.getString("remark") == null ? "" : rs.getString("remark");

                String FLNO = rs.getString("FLNO") == null ? "" : rs.getString("FLNO");
                String JFNO = rs.getString("JFNO") == null ? " " : rs.getString("JFNO");
                String FLTI = rs.getString("FLTI") == null ? "" : rs.getString("FLTI");
                String BLT1 = rs.getString("BLT1") == null ? "" : rs.getString("BLT1");
                String BLT2 = rs.getString("BLT2") == null ? "" : rs.getString("BLT2");
                String BELT1STATUS = rs.getString("BELT1STATUS") == null ? "" : rs.getString("BELT1STATUS");
                String BELT2STATUS = rs.getString("BELT2STATUS") == null ? "" : rs.getString("BELT2STATUS");
                String CITYDOM = rs.getString("CITYDOM") == null ? "" : rs.getString("CITYDOM");
                String CITYNAMEDOM = rs.getString("CITYNAMEDOM") == null ? "" : rs.getString("CITYNAMEDOM").trim();
                String CITYINT = rs.getString("CITYINT") == null ? "" : rs.getString("CITYINT");
                String CITYNAMEINT = rs.getString("CITYNAMEINT") == null ? "" : rs.getString("CITYNAMEINT").trim();
                String CITYALL = rs.getString("CITYALL") == null ? "" : rs.getString("CITYALL");
                String CITYNAMEALL = rs.getString("CITYNAMEALL") == null ? "" : rs.getString("CITYNAMEALL").trim();
                String FULLFLNO = "";
                String LOGO = "";
                // String FLTN="";

                String BELT1START = rs.getString("BELT1START");
                String BELT1END = rs.getString("BELT1END");
                String BELT2START = rs.getString("BELT2START");
                String BELT2END = rs.getString("BELT2END");

                boolean BELT1CLOSE = false;
                boolean BELT2CLOSE = false;

                Calendar now = Calendar.getInstance();
                if (BELT1END.trim().length() == 14) {// Has value
                    Calendar g1 = Calendar.getInstance();
                    g1.setTime(sp.parse(BELT1END));
                    g1.add(Calendar.HOUR, 7);
                    if (g1.before(now)) {
                        BELT1CLOSE = true;
                    }
                }
                if (BELT2END.trim().length() == 14) {// Has value
                    Calendar g2 = Calendar.getInstance();
                    g2.setTime(sp.parse(BELT2END));
                    g2.add(Calendar.HOUR, 7);
                    if (g2.before(now)) {
                        BELT2CLOSE = true;
                    }
                }

                String ALC2 = FLNO.substring(0, 3).trim();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getString(i) != null) {
                        if (rs.getMetaData().getColumnName(i).toUpperCase().equals("FLNO") && ALC2.equals("DD")) {// For
                                                                                                                  // Nokair
                                                                                                                  // airline
                                                                                                                  // when
                                                                                                                  // FLNO
                                                                                                                  // begin
                                                                                                                  // 0
                                                                                                                  // then
                                                                                                                  // cutoff
                                                                                                                  // 0
                            FLNO = substringForNokair(FLNO);
                            row.put("FLNO", FLNO.trim());
                        } else {
                            row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                                    (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                        }
                    } else {
                        row.put(rs.getMetaData().getColumnName(i).toUpperCase(), "");
                    }
                }

                String newREMARK = getRemarkDelay("ARR", REMARK, remarkobj);
                row.put("REMARK", newREMARK);

                if (JFNO == null || JFNO.equals(" ")) {
                    FULLFLNO = FLNO.trim();
                    LOGO = FLNO.substring(0, 3).trim();
                    // FLTN=FLNO.substring(3,7).trim();
                    row.put("JALLFLNO", "");
                    row.put("JALLLOGO", "");
                    row.put("ALLFLNO", FULLFLNO);
                    row.put("ALLLOGO", LOGO);
                    row.put("BELT", BLT1);
                    row.put("DOMINT", FLTI);
                    row.put("CITY", CITYALL);
                    row.put("CITYNAME", CITYNAMEALL);
                    row.put("BELTSTATUS", BELT1STATUS);
                    if (FLTI.equals("M")) {
                        row.put("BELT", BLT1);
                        row.put("DOMINT", "I");
                        row.put("CITY", CITYINT);
                        row.put("CITYNAME", CITYNAMEINT);
                        row.put("BELTSTATUS", BELT1STATUS);
                        if (!BELT1CLOSE) {
                            result.add(row);
                        }
                        String rowtext = row.toJSONString();
                        JSONParser jp = new JSONParser();
                        JSONObject row2 = (JSONObject) jp.parse(rowtext);
                        row2.put("BELT", BLT2);
                        row2.put("DOMINT", "D");
                        row2.put("CITY", CITYDOM);
                        row2.put("CITYNAME", CITYNAMEDOM);
                        row2.put("BELTSTATUS", BELT2STATUS);
                        if (!BELT2CLOSE) {
                            result.add(row2);
                        }
                    } else {
                        if (!BELT1CLOSE) {
                            result.add(row);
                        }
                    }
                } else {
                    FULLFLNO = FLNO.trim();
                    JFNO = JFNO.replace("   ", "  ");
                    String JFULLFLNO = JFNO.replace("  ", "|");
                    String ALLFLNO = FULLFLNO + "|" + JFULLFLNO;
                    LOGO = FLNO.substring(0, 3).trim();
                    // FLTN=FLNO.substring(3,7).trim();

                    String SHARELOGO = "";
                    String[] JLIST = JFNO.split("  ");
                    for (int i = 0; i < JLIST.length; i++) {
                        if (JLIST[i].length() < 7) {
                            JLIST[i] = JLIST[i] + "       ";
                        }
                        if (i == 0) {
                            SHARELOGO += JLIST[i].substring(0, 3).trim();
                        } else {
                            SHARELOGO += "|" + JLIST[i].substring(0, 3).trim();
                        }
                    }
                    row.put("JALLFLNO", JFULLFLNO);
                    row.put("JALLLOGO", SHARELOGO);
                    row.put("ALLFLNO", ALLFLNO);
                    row.put("ALLLOGO", LOGO + "|" + SHARELOGO);
                    row.put("BELT", BLT1);
                    row.put("DOMINT", FLTI);
                    row.put("CITY", CITYALL);
                    row.put("CITYNAME", CITYNAMEALL);
                    row.put("BELTSTATUS", BELT1STATUS);

                    if (FLTI.equals("M")) {
                        row.put("BELT", BLT1);
                        row.put("DOMINT", "I");
                        row.put("CITY", CITYINT);
                        row.put("CITYNAME", CITYNAMEINT);
                        row.put("BELTSTATUS", BELT1STATUS);
                        if (!BELT1CLOSE) {
                            result.add(row);
                        }
                        String rowtext = row.toJSONString();
                        JSONParser jp = new JSONParser();
                        JSONObject row2 = (JSONObject) jp.parse(rowtext);
                        row2.put("BELT", BLT2);
                        row2.put("DOMINT", "D");
                        row2.put("CITY", CITYDOM);
                        row2.put("CITYNAME", CITYNAMEDOM);
                        row2.put("BELTSTATUS", BELT2STATUS);
                        if (!BELT2CLOSE) {
                            result.add(row2);
                        }
                    } else {
                        if (!BELT1CLOSE) {
                            result.add(row);
                        }
                    }
                }
            }
            rs.close();
            pstmt.close();
            c.close();

            for (int i = 0; i < result.size(); i++) {
                JSONObject row = (JSONObject) result.get(i);
                JSONObject nextflightrow = null;
                JSONObject nextflightrow2 = null;
                String BELT = (String) row.get("BELT");
                boolean setnextint = false;
                boolean setnextdom = false;
                boolean setnext = false;
                boolean setnext2 = false;
                for (int j = i + 1; j < result.size(); j++) {// Find Next Filght
                    JSONObject row2 = (JSONObject) result.get(j);
                    if (!BELT.equals("") && !row2.get("BELT").equals("")) {
                        if (row2.get("BELT").equals(BELT)) {
                            if (setnext == false) {
                                nextflightrow = row2;
                                setnext = true;
                            } else if (setnext == true && setnext2 == false) {
                                nextflightrow2 = row2;
                                setnext2 = true;
                            }
                        }
                    }
                }

                if (nextflightrow != null) {
                    row.put("NEXT_STA", nextflightrow.get("STA"));
                    row.put("NEXT_LOGO", nextflightrow.get("LOGO"));
                    row.put("NEXT_FLNO", nextflightrow.get("FLNO"));
                    row.put("NEXT_CITY", nextflightrow.get("CITY"));
                    row.put("NEXT_CITYNAME", nextflightrow.get("CITYNAME"));
                    row.put("NEXT_BELTSTATUS", nextflightrow.get("BELTSTATUS"));
                    row.put("NEXT_ORG3", nextflightrow.get("ORG3"));
                    row.put("NEXT_APSN", nextflightrow.get("APSN"));
                    row.put("NEXT_REMARK", nextflightrow.get("REMARK"));
                    row.put("NEXT_ALLFLNO", nextflightrow.get("ALLFLNO"));
                    row.put("NEXT_JALLFLNO", nextflightrow.get("JALLFLNO"));
                    row.put("NEXT_JALLLOGO", nextflightrow.get("JALLLOGO"));
                    row.put("NEXT_ALLLOGO", nextflightrow.get("ALLLOGO"));
                    if (nextflightrow2 != null) {
                        row.put("NEXT2_STA", nextflightrow2.get("STA"));
                        row.put("NEXT2_LOGO", nextflightrow2.get("LOGO"));
                        row.put("NEXT2_FLNO", nextflightrow2.get("FLNO"));
                        row.put("NEXT2_CITY", nextflightrow2.get("CITY"));
                        row.put("NEXT2_CITYNAME", nextflightrow2.get("CITYNAME"));
                        row.put("NEXT2_BELTSTATUS", nextflightrow2.get("BELTSTATUS"));
                        row.put("NEXT2_ORG3", nextflightrow2.get("ORG3"));
                        row.put("NEXT2_APSN", nextflightrow2.get("APSN"));
                        row.put("NEXT2_REMARK", nextflightrow2.get("REMARK"));
                        row.put("NEXT2_ALLFLNO", nextflightrow2.get("ALLFLNO"));
                        row.put("NEXT2_JALLFLNO", nextflightrow2.get("JALLFLNO"));
                        row.put("NEXT2_JALLLOGO", nextflightrow2.get("JALLLOGO"));
                        row.put("NEXT2_ALLLOGO", nextflightrow2.get("ALLLOGO"));
                    } else {
                        row.put("NEXT2_STA", "");
                        row.put("NEXT2_LOGO", "");
                        row.put("NEXT2_FLNO", "");
                        row.put("NEXT2_CITY", "");
                        row.put("NEXT2_CITYNAME", "");
                        row.put("NEXT2_BELTSTATUS", "");
                        row.put("NEXT2_ORG3", "");
                        row.put("NEXT2_APSN", "");
                        row.put("NEXT2_REMARK", "");
                        row.put("NEXT2_ALLFLNO", "");
                        row.put("NEXT2_JALLFLNO", "");
                        row.put("NEXT2_JALLLOGO", "");
                        row.put("NEXT2_ALLLOGO", "");
                    }
                } else {
                    row.put("NEXT_STA", "");
                    row.put("NEXT_LOGO", "");
                    row.put("NEXT_FLNO", "");
                    row.put("NEXT_CITY", "");
                    row.put("NEXT_CITYNAME", "");
                    row.put("NEXT_BELTSTATUS", "");
                    row.put("NEXT_ORG3", "");
                    row.put("NEXT_APSN", "");
                    row.put("NEXT_REMARK", "");
                    row.put("NEXT_ALLFLNO", "");
                    row.put("NEXT_JALLFLNO", "");
                    row.put("NEXT_JALLLOGO", "");
                    row.put("NEXT_ALLLOGO", "");
                    row.put("NEXT2_STA", "");
                    row.put("NEXT2_LOGO", "");
                    row.put("NEXT2_FLNO", "");
                    row.put("NEXT2_CITY", "");
                    row.put("NEXT2_CITYNAME", "");
                    row.put("NEXT2_BELTSTATUS", "");
                    row.put("NEXT2_ORG3", "");
                    row.put("NEXT2_APSN", "");
                    row.put("NEXT2_REMARK", "");
                    row.put("NEXT2_ALLFLNO", "");
                    row.put("NEXT2_JALLFLNO", "");
                    row.put("NEXT2_JALLLOGO", "");
                    row.put("NEXT2_ALLLOGO", "");
                }
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public JSONArray getCityCode(Connection c) {
        try {
            // Class.forName("oracle.jdbc.OracleDriver");
            // String conn = "jdbc:oracle:thin:@10.121.0.5:1521/ufisaodb";
            // Properties p = new Properties();
            // p.put("user", "cedarepdb");
            // p.put("password", "CEDA");
            // Connection c = DriverManager.getConnection(conn,p);
            // Connection c = getConnection();
            String SQL = "select * from FIDS_AIRPORT where APSN2 is not null";
            PreparedStatement pstmt = c.prepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
            JSONArray result = new JSONArray();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnName(i).toUpperCase(),
                            (rs.getString(i) == null ? "" : rs.getString(i)).trim());
                }
                result.add(row);
            }
            rs.close();
            pstmt.close();
            c.close();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getGate(String gate) {
        String startIndex = "";
        String lastIndex = "";
        if (gate.length() > 0) {
            startIndex = gate.substring(0, 1);
            lastIndex = gate.substring(gate.length() - 1);
            if (lastIndex.equals("I") || lastIndex.equals("D")) {// Remove I,D ex.B03I,13I,B03D,13D
                gate = gate.substring(0, gate.length() - 1);// B03,13,B03,13
                lastIndex = gate.substring(gate.length() - 1);// Update last index
            }
            if (startIndex.replaceAll("[0-9]", "").length() == 1) {// Start with String ex.B02
                if (lastIndex.replaceAll("[0-9]", "").length() == 1) {// End with String ex.B02C
                    gate = startIndex + Integer.parseInt(gate.substring(1, gate.length() - 1)) + lastIndex;// B2C
                } else {// End with Number ex.B02
                    gate = startIndex + Integer.parseInt(gate.substring(1, gate.length()));// B2
                }
            } else {// Start with Number ex. 02C
                if (lastIndex.replaceAll("[0-9]", "").length() == 1) {// End with String ex. 02C
                    gate = Integer.parseInt(gate.substring(0, gate.length() - 1)) + lastIndex;// 2C
                } else {// End with Number ex. 02
                    gate = Integer.parseInt(gate.substring(0, gate.length())) + "";// 2
                }
            }
        }
        return gate;
    }

    public JSONObject getCommonCounterByAIRLINE(String Airline, String std, Connection c, String HOPO,
            JSONObject CKICTERM) throws SQLException, ParseException {

        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        // String SQL = "select min(case when ckba!=' ' then ckba else ckbs end) as
        // ctrstart,min(case when ckba!=' ' then ckba else ckbs end) as ctractstart,"
        // + " max(case when ckea!=' ' then ckea else ckes end) as ctrend,max(case when
        // ckea!=' ' then ckea else ckes end) as ctractend,"
        String SQL = "select min(ckbs) as ctrstart,max(ckes) as ctrend,"
                + " listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNO,min(ckic) as minckic,max(ckic) as maxckic "
                +
                " from fids_ccatab  " +
                " where hopo='" + HOPO + "' and ctyp='C' and flno='" + Airline + "' and ckbs<to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(45/1440),'yyyyMMddhh24miss') " +
                " and ckes>to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(3/24),'yyyyMMddhh24miss') and (CKIC !='     ' and CKIC is not null)" +
                " group by FLNO";
        // System.out.println(SQL);
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";
        String result2 = "";
        String result2TER1 = "";
        String result2TER2 = "";
        String result3 = "";
        String resultHKTINT = "";
        String resultHKTDOM = "";
        String resultCNXINT = "";
        String resultCNXDOM = "";
        boolean YCheck = false;
        if (rs.next()) {
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");
            HashSet<String> tmp1 = new HashSet<String>();
            HashSet<String> tmp2 = new HashSet<String>();
            HashSet<String> tmp2TER1 = new HashSet<String>();
            HashSet<String> tmp2TER2 = new HashSet<String>();
            ArrayList<Integer> tmpINT = new ArrayList<Integer>();
            HashSet<String> tmpHKTINT = new HashSet<String>();
            HashSet<String> tmpHKTDOM = new HashSet<String>();
            HashSet<String> tmpCNXINT = new HashSet<String>();
            HashSet<String> tmpCNXDOM = new HashSet<String>();

            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                if (cname.length() >= 2) {// 01
                    tmp1.add(cname.substring(0, 1));
                    String row2 = cname.substring(0, 2);

                    if (CKICTERM.get(cname) != null) {
                        String TERM = CKICTERM.get(cname) + "";
                        TERM = TERM.trim();
                        if (TERM.equals("1") || TERM.equals("I")) {
                            tmp2TER1.add(row2);
                            tmpHKTINT.add(cname.substring(0, 1));
                            tmpCNXINT.add(cname);
                        }
                        if (TERM.equals("2") || TERM.equals("D")) {
                            tmp2TER2.add(row2);
                            tmpHKTDOM.add(row2);
                            tmpCNXDOM.add(cname);
                        }
                    }
                    tmp2.add(row2);
                }
                try {
                    int cint = Integer.parseInt(cname);
                    tmpINT.add(cint);
                } catch (Exception ex) {

                }
            }

            // counterlist = new HashSet<String>(Arrays.asList(counterlist)).toArray(new
            // String[0]);

            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            String[] counter2 = tmp2.toArray(new String[0]);
            Arrays.sort(counter2);
            String[] counter2TER1 = tmp2TER1.toArray(new String[0]);
            Arrays.sort(counter2TER1);
            String[] counter2TER2 = tmp2TER2.toArray(new String[0]);
            Arrays.sort(counter2TER2);
            String[] counterHKTINT = tmpHKTINT.toArray(new String[0]);
            Arrays.sort(counterHKTINT);
            String[] counterHKTDOM = tmpHKTDOM.toArray(new String[0]);
            Arrays.sort(counterHKTDOM);
            String[] counterCNXINT = tmpCNXINT.toArray(new String[0]);
            Arrays.sort(counterCNXINT);
            String[] counterCNXDOM = tmpCNXDOM.toArray(new String[0]);
            Arrays.sort(counterCNXDOM);

            for (String counter : counter1) {
                result1 += counter;
            }
            if (result1.equals("Y")) {
                YCheck = true;
            }
            result1 = result1.replace("Y", "");

            for (String counter : counter2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception ex) {

                }
                if (result2.equals("")) {
                    result2 = counter;
                } else {
                    result2 += "," + counter;
                }
            }

            for (String counter : counter2TER1) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception ex) {

                }
                if (result2TER1.equals("")) {
                    result2TER1 = counter;
                } else {
                    result2TER1 += "," + counter;
                }
            }
            for (String counter : counter2TER2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception ex) {

                }
                if (result2TER2.equals("")) {
                    result2TER2 = counter;
                } else {
                    result2TER2 += "," + counter;
                }
            }

            for (String counter : counterHKTINT) {
                resultHKTINT += counter;
            }
            ArrayList<Integer> ctrHKTDOM = new ArrayList<Integer>();
            for (String counter : counterHKTDOM) {
                try {
                    ctrHKTDOM.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultHKTDOM = getSumString(ctrHKTDOM.toArray(new Integer[0]));

            ArrayList<Integer> ctrCNXINT = new ArrayList<Integer>();
            for (String counter : counterCNXINT) {
                try {
                    ctrCNXINT.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultCNXINT = getSumString(ctrCNXINT.toArray(new Integer[0]));

            ArrayList<Integer> ctrCNXDOM = new ArrayList<Integer>();
            for (String counter : counterCNXDOM) {
                try {
                    ctrCNXDOM.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultCNXDOM = getSumString(ctrHKTDOM.toArray(new Integer[0]));

            result3 = getSumString(tmpINT.toArray(new Integer[0]));

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);

            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Date cmstart = aodbDate.parse(ctrstart);
            Calendar cal3 = Calendar.getInstance(); // creates calendar
            cal3.setTime(cmstart); // sets calendar time/date
            cal3.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonstart = cal3.getTime();

            Date cmend = aodbDate.parse(ctrend);
            Calendar cal4 = Calendar.getInstance(); // creates calendar
            cal4.setTime(cmend); // sets calendar time/date
            cal4.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonend = cal4.getTime();

            Date cstd = aodbDate.parse(std);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(cstd); // sets calendar time/date
            if (Airline.equals("KL") || Airline.equals("AF") || Airline.equals("WY") || Airline.equals("AY")) {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 4); // เธฅเธ� 4 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            } else {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 3); // เธฅเธ� 3 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            }
            Date cstart = cal.getTime();

            Calendar cal2 = Calendar.getInstance(); // creates calendar
            cal2.setTime(cstd); // sets calendar time/date
            if (Airline.equals("KL") || Airline.equals("AF") || Airline.equals("WY")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 60); // เธฅเธ� 60 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else if (Airline.equals("AY")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 50); // เธฅเธ� 50 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else {
                cal2.add(Calendar.MINUTE, (7 * 60) - 40); // เธฅเธ� 40 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            }
            Date cend = cal2.getTime();

            Date now = new Date();
            if (now.before(cstart)) {// not open yet
                result1 = result1;
                result2 = result2;
                result2TER1 = result2TER1;
                result2TER2 = result2TER2;
                result3 = result3;
                resultHKTDOM = resultHKTDOM;
                resultHKTINT = resultHKTINT;
                resultCNXDOM = resultCNXDOM;
                resultCNXINT = resultCNXINT;
            } else if (now.after(cstart) && (now.before(cend))) {

                if (now.after(commonstart)) {
                    result1 += "|OPEN";
                    result2 += "|OPEN";
                    result2TER1 += "|OPEN";
                    result2TER2 += "|OPEN";
                    result3 += "|OPEN";
                    resultHKTDOM = resultHKTDOM + "|OPEN";
                    resultHKTINT = resultHKTINT + "|OPEN";
                    resultCNXDOM = resultCNXDOM + "|OPEN";
                    resultCNXINT = resultCNXINT + "|OPEN";
                }
            } else if (now.after(cend) || now.after(commonend)) {
                result1 += "|CLOSE";
                result2 += "|CLOSE";
                result2TER1 += "|CLOSE";
                result2TER2 += "|CLOSE";
                result3 += "|CLOSE";
                resultHKTDOM = resultHKTDOM + "|CLOSE";
                resultHKTINT = resultHKTINT + "|CLOSE";
                resultCNXDOM = resultCNXDOM + "|CLOSE";
                resultCNXINT = resultCNXINT + "|CLOSE";
            }
            // System.out.println(result);
        }
        rs.close();
        stmt.close();

        obj.put("ctrstatus1", result1);
        obj.put("ctrstatus2", result2);
        obj.put("ctrstatus2TER1", result2TER1);
        obj.put("ctrstatus2TER2", result2TER2);
        obj.put("ctrstatus3", result3);
        obj.put("ctrstatusHKTINT", resultHKTINT);
        obj.put("ctrstatusHKTDOM", resultHKTDOM);
        obj.put("ctrstatusCNXINT", resultCNXINT);
        obj.put("ctrstatusCNXDOM", resultCNXDOM);
        obj.put("YONLY", YCheck);
        return obj;
    }

    public JSONObject getCommonCounterByAIRLINEBYTERM(String Airline, String std, Connection c, String HOPO,
            JSONObject CKICTERM) throws SQLException, ParseException {

        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        // String SQL = "select min(case when ckba!=' ' then ckba else ckbs end) as
        // ctrstart,min(case when ckba!=' ' then ckba else ckbs end) as ctractstart,"
        // + " max(case when ckea!=' ' then ckea else ckes end) as ctrend,max(case when
        // ckea!=' ' then ckea else ckes end) as ctractend,"
        String SQL = "select min(ckbs) as ctrstart,max(ckes) as ctrend,"
                + " listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNO,min(ckic) as minckic,max(ckic) as maxckic,CKIT "
                +
                " from fids_ccatab  " +
                " where hopo='" + HOPO + "' and ctyp='C' and flno='" + Airline + "' and ckbs<to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(45/1440),'yyyyMMddhh24miss') " +
                " and ckes>to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(3/24),'yyyyMMddhh24miss') and (CKIC !='     ' and CKIC is not null)" +
                " group by FLNO,CKIT";
        // System.out.println(SQL);
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";
        String result2 = "";
        String result2TER1 = "";
        String result2TER2 = "";
        String result3 = "";
        String resultHKTINT = "";
        String resultHKTDOM = "";
        String resultCNXINT = "";
        String resultCNXDOM = "";
        boolean YCheck = false;
        while (rs.next()) {
            String TERM = rs.getString("CKIT") == null ? "" : rs.getString("CKIT");
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");
            HashSet<String> tmp1 = new HashSet<String>();
            HashSet<String> tmp2 = new HashSet<String>();
            // HashSet<String> tmp2TER1=new HashSet<String>();
            HashSet<String> tmpDMK = new HashSet<String>();
            ArrayList<Integer> tmpINT = new ArrayList<Integer>();
            HashSet<String> tmpHKT = new HashSet<String>();
            // HashSet<String> tmpHKTDOM=new HashSet<String>();
            HashSet<String> tmpCNX = new HashSet<String>();
            // HashSet<String> tmpCNXDOM=new HashSet<String>();

            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                if (cname.length() >= 2) {// 01
                    tmp1.add(cname.substring(0, 1));
                    String row2 = cname.substring(0, 2);

                    if (CKICTERM.get(cname) != null) {
                        TERM = TERM.trim();
                        if (TERM.equals("1") || TERM.equals("I")) {
                            // tmp2TER1.add(row2);
                            tmpHKT.add(cname.substring(0, 1));
                        }
                        if (TERM.equals("2") || TERM.equals("D")) {
                            // tmp2TER2.add(row2);
                            tmpHKT.add(row2);

                        }
                    }
                    tmp2.add(row2);
                    tmpCNX.add(cname);
                }
                try {
                    int cint = Integer.parseInt(cname);
                    tmpINT.add(cint);
                } catch (Exception ex) {

                }
            }

            // counterlist = new HashSet<String>(Arrays.asList(counterlist)).toArray(new
            // String[0]);

            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            String[] counter2 = tmp2.toArray(new String[0]);
            Arrays.sort(counter2);
            // String[] counter2 = tmp2TER1.toArray(new String[0]);
            // Arrays.sort(counter2TER1);
            // String[] counter2TER2 = tmp2TER2.toArray(new String[0]);
            // Arrays.sort(counter2TER2);
            String[] counterHKT = tmpHKT.toArray(new String[0]);
            Arrays.sort(counterHKT);
            // String[] counterHKTDOM = tmpHKTDOM.toArray(new String[0]);
            // Arrays.sort(counterHKTDOM);
            String[] counterCNX = tmpCNX.toArray(new String[0]);
            Arrays.sort(counterCNX);
            // String[] counterCNXDOM = tmpCNXDOM.toArray(new String[0]);
            // Arrays.sort(counterCNXDOM);

            for (String counter : counter1) {
                result1 += counter;
            }
            if (result1.equals("Y")) {
                YCheck = true;
            }
            result1 = result1.replace("Y", "");

            for (String counter : counter2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception ex) {

                }
                if (result2.equals("")) {
                    result2 = counter;
                } else {
                    result2 += "," + counter;
                }
            }

            if (TERM.equals("1")) {
                result2TER1 = result2;
            } else if (TERM.equals("2")) {
                result2TER2 = result2;
            }

            // for(String counter:counter2TER1){
            // try {
            // counter = Integer.parseInt(counter)+"";
            // }catch(Exception ex) {
            //
            // }
            // if(result2TER1.equals("")) {
            // result2TER1=counter;
            // }else {
            // result2TER1+=","+counter;
            // }
            // }
            // for(String counter:counter2TER2){
            // try {
            // counter = Integer.parseInt(counter)+"";
            // }catch(Exception ex) {
            //
            // }
            // if(result2TER2.equals("")) {
            // result2TER2=counter;
            // }else {
            // result2TER2+=","+counter;
            // }
            // }

            if (TERM.equals("I")) {
                for (String counter : counterHKT) {
                    resultHKTINT += counter;
                }
            } else if (TERM.equals("D")) {
                ArrayList<Integer> ctrHKT = new ArrayList<Integer>();
                for (String counter : counterHKT) {
                    try {
                        ctrHKT.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultHKTDOM = getSumString(ctrHKT.toArray(new Integer[0]));
            }
            if (TERM.equals("I")) {
                ArrayList<Integer> ctrCNXINT = new ArrayList<Integer>();
                for (String counter : counterCNX) {
                    try {
                        ctrCNXINT.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultCNXINT = getSumString(ctrCNXINT.toArray(new Integer[0]));
            } else if (TERM.equals("D")) {
                ArrayList<Integer> ctrCNXDOM = new ArrayList<Integer>();
                for (String counter : counterCNX) {
                    try {
                        ctrCNXDOM.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultCNXDOM = getSumString(ctrCNXDOM.toArray(new Integer[0]));
            }

            result3 = getSumString(tmpINT.toArray(new Integer[0]));

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);

            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Date cmstart = aodbDate.parse(ctrstart);
            Calendar cal3 = Calendar.getInstance(); // creates calendar
            cal3.setTime(cmstart); // sets calendar time/date
            cal3.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonstart = cal3.getTime();

            Date cmend = aodbDate.parse(ctrend);
            Calendar cal4 = Calendar.getInstance(); // creates calendar
            cal4.setTime(cmend); // sets calendar time/date
            cal4.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonend = cal4.getTime();

            Date cstd = aodbDate.parse(std);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(cstd); // sets calendar time/date
            if (Airline.equals("KL") || Airline.equals("AF") || Airline.equals("WY") || Airline.equals("AY")) {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 4); // เธฅเธ� 4 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            } else {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 3); // เธฅเธ� 3 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            }
            Date cstart = cal.getTime();

            Calendar cal2 = Calendar.getInstance(); // creates calendar
            cal2.setTime(cstd); // sets calendar time/date
            if (Airline.equals("KL") || Airline.equals("AF") || Airline.equals("WY")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 60); // เธฅเธ� 60 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else if (Airline.equals("AY")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 50); // เธฅเธ� 50 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else {
                cal2.add(Calendar.MINUTE, (7 * 60) - 40); // เธฅเธ� 40 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            }
            Date cend = cal2.getTime();

            Date now = new Date();
            if (now.before(cstart)) {// not open yet
                result1 = result1;
                result2 = result2;
                result3 = result3;
                if (TERM.equals("I") || TERM.equals("1")) {
                    result2TER1 = result2TER1;
                    resultHKTINT = resultHKTINT;
                    resultCNXINT = resultCNXINT;
                } else if (TERM.equals("D") || TERM.equals("2")) {
                    result2TER2 = result2TER2;
                    resultHKTDOM = resultHKTDOM;
                    resultCNXDOM = resultCNXDOM;
                }
            } else if (now.after(cstart) && (now.before(cend))) {

                if (now.after(commonstart)) {
                    result1 = result1 + "|OPEN";
                    result2 = result2 + "|OPEN";
                    result3 = result3 + "|OPEN";
                    if (TERM.equals("I") || TERM.equals("1")) {
                        result2TER1 = result2TER1 + "|OPEN";
                        resultHKTINT = resultHKTINT + "|OPEN";
                        resultCNXINT = resultCNXINT + "|OPEN";
                    } else if (TERM.equals("D") || TERM.equals("2")) {
                        result2TER2 = result2TER2 + "|OPEN";
                        resultHKTDOM = resultHKTDOM + "|OPEN";
                        resultCNXDOM = resultCNXDOM + "|OPEN";
                    }
                }
            } else if (now.after(cend) || now.after(commonend)) {
                result1 = result1 + "|CLOSE";
                result2 = result2 + "|CLOSE";
                result3 = result3 + "|CLOSE";
                if (TERM.equals("I") || TERM.equals("1")) {
                    result2TER1 = result2TER1 + "|CLOSE";
                    resultHKTINT = resultHKTINT + "|CLOSE";
                    resultCNXINT = resultCNXINT + "|CLOSE";
                } else if (TERM.equals("D") || TERM.equals("2")) {
                    result2TER2 = result2TER2 + "|CLOSE";
                    resultHKTDOM = resultHKTDOM + "|CLOSE";
                    resultCNXDOM = resultCNXDOM + "|CLOSE";
                }
            }
            // System.out.println(result);
        }
        rs.close();
        stmt.close();

        obj.put("ctrstatus1", result1);
        obj.put("ctrstatus2", result2);
        obj.put("ctrstatus2TER1", result2TER1);
        obj.put("ctrstatus2TER2", result2TER2);
        obj.put("ctrstatus3", result3);
        obj.put("ctrstatusHKTINT", resultHKTINT);
        obj.put("ctrstatusHKTDOM", resultHKTDOM);
        obj.put("ctrstatusCNXINT", resultCNXINT);
        obj.put("ctrstatusCNXDOM", resultCNXDOM);
        obj.put("YONLY", YCheck);
        return obj;
    }

    public JSONObject getCommonCounterByTERM(String Airline, String std, Connection c, String HOPO, String CKIT)
            throws SQLException, ParseException {
        // CKIT I / D
        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select min(ckbs) as ctrstart,max(ckes) as ctrend,"
                + " listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNO,min(ckic) as minckic,max(ckic) as maxckic "
                +
                " from fids_ccatab  " +
                " where hopo='" + HOPO + "' and ctyp='C' and flno='" + Airline + "' and ckbs<to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(45/1440),'yyyyMMddhh24miss') " +
                " and ckes>to_char(to_date('" + std
                + "','yyyyMMddhh24miss')-(3/24),'yyyyMMddhh24miss') and (CKIC !='     ' and CKIC is not null) and CKIT='"
                + CKIT + "'" +
                " group by FLNO";
        // if(Airline.equals("FD") && std.equals("20230615053500")) {
        // System.out.println(SQL);
        // }
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";

        boolean YCheck = false;
        if (rs.next()) {
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");
            HashSet<String> tmp1 = new HashSet<String>();

            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                tmp1.add(cname);
            }

            //// counterlist = new HashSet<String>(Arrays.asList(counterlist)).toArray(new
            //// String[0]);
            //
            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            //
            // ArrayList<Integer> ctrint = new ArrayList<Integer>();
            // for(String counter:counter1){// Counter 10-12
            // try {
            // ctrint.add(Integer.parseInt(counter));
            // }catch(Exception ex) {
            //
            // }
            // }
            // result1=getSumString(ctrint.toArray(new Integer[0]));
            result1 = getSumString2(counter1);

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);

            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Date cmstart = aodbDate.parse(ctrstart);
            Calendar cal3 = Calendar.getInstance(); // creates calendar
            cal3.setTime(cmstart); // sets calendar time/date
            cal3.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonstart = cal3.getTime();

            Date cmend = aodbDate.parse(ctrend);
            Calendar cal4 = Calendar.getInstance(); // creates calendar
            cal4.setTime(cmend); // sets calendar time/date
            cal4.add(Calendar.HOUR_OF_DAY, 7); // adds one hour
            Date commonend = cal4.getTime();

            Date cstd = aodbDate.parse(std);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(cstd); // sets calendar time/date
            // if(Airline.equals("KL")||Airline.equals("AF")||Airline.equals("WY")||Airline.equals("AY"))
            // {
            // cal.add(Calendar.HOUR_OF_DAY, 7-4); // เธฅเธ� 4 เธ�เธฑเน�เธงเน�เธกเธ�
            // เธ�เธฒเธ� STD
            // }else {
            if (CKIT.equals("I")) {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 3); // เธฅเธ� 3 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            } else if (CKIT.equals("D")) {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 2); // เธฅเธ� 2 เธ�เธฑเน�เธงเน� เธกเธ� เธ�เธฒเธ� STD
            } else {
                cal.add(Calendar.HOUR_OF_DAY, 7 - 3); // เธฅเธ� 3 เธ�เธฑเน�เธงเน�เธกเธ� เธ�เธฒเธ� STD
            }
            // }
            Date cstart = cal.getTime();

            Calendar cal2 = Calendar.getInstance(); // creates calendar
            cal2.setTime(cstd); // sets calendar time/date
            // if(Airline.equals("KL")||Airline.equals("AF")||Airline.equals("WY")) {
            // cal2.add(Calendar.MINUTE, (7*60)-60); // เธฅเธ� 60 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            // }else if (Airline.equals("AY")){
            // cal2.add(Calendar.MINUTE, (7*60)-50); // เธฅเธ� 50 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            // }else {
            if (CKIT.equals("I")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 40); // เธฅเธ� 40 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else if (CKIT.equals("D")) {
                cal2.add(Calendar.MINUTE, (7 * 60) - 40); // เธฅเธ� 40 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            } else {
                cal2.add(Calendar.MINUTE, (7 * 60) - 40); // เธฅเธ� 40 เธ�เธฒเธ—เธต เธ�เธฒเธ� STD
            }
            // }
            Date cend = cal2.getTime();

            Date now = new Date();
            // if(Airline.equals("FD") && std.equals("20230615053500")) {
            // System.out.println(now);
            // System.out.println(cstart);
            // System.out.println(cend);
            // }
            if (now.before(cstart)) {// not open yet
                result1 = result1;
            } else if (now.after(cstart) && (now.before(cend))) {
                if (now.after(commonstart)) {
                    result1 += "|OPEN";
                }
            } else if (now.after(cend) || now.after(commonend)) {
                result1 += "|CLOSE";
            }
        }
        rs.close();
        stmt.close();
        obj.put("ctrstatus", result1);
        return obj;
    }

    public JSONObject getDedicateCounterByURNO(String URNO, Connection c, JSONObject CKICTERM)
            throws SQLException, ParseException {
        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select min(nullif(ckba,' ')) as ctrstart,max(case when ckea!='              ' and ckea is not null then ckea else ckes end) as ctrend, listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNU,"
                + " min(ckic) as minckic, max(ckic) as maxckic " +
                " from fids_ccatab " +
                " where (ctyp=' ' or ctyp is null) and FLNU='" + URNO
                + "' and (CKIC !='     ' and CKIC is not null)  and (DISP is null or DISP!='99') " +
                " group by FLNU";
        // System.out.println(SQL);
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";
        String result2 = "";
        String result2TER1 = "";
        String result2TER2 = "";
        String result3 = "";
        String resultHKTINT = "";
        String resultHKTDOM = "";
        String resultCNXINT = "";
        String resultCNXDOM = "";
        boolean YCheck = false;
        if (rs.next()) {
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");

            HashSet<String> tmp1 = new HashSet<String>();
            HashSet<String> tmp2 = new HashSet<String>();
            HashSet<String> tmp2TER1 = new HashSet<String>();
            HashSet<String> tmp2TER2 = new HashSet<String>();
            ArrayList<Integer> tmpINT = new ArrayList<Integer>();
            HashSet<String> tmpHKTINT = new HashSet<String>();
            HashSet<String> tmpHKTDOM = new HashSet<String>();
            HashSet<String> tmpCNXINT = new HashSet<String>();
            HashSet<String> tmpCNXDOM = new HashSet<String>();
            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                if (cname.length() >= 2) {// 01
                    tmp1.add(cname.substring(0, 1));
                    String row2 = cname.substring(0, 2);

                    if (CKICTERM.get(cname) != null) {
                        String TERM = CKICTERM.get(cname) + "";
                        TERM = TERM.trim();
                        if (TERM.equals("1") || TERM.equals("I")) {
                            tmp2TER1.add(row2);
                            tmpHKTINT.add(cname.substring(0, 1));
                            tmpCNXINT.add(cname);
                        }
                        if (TERM.equals("2") || TERM.equals("D")) {
                            tmp2TER2.add(row2);
                            tmpHKTDOM.add(row2);
                            tmpCNXDOM.add(cname);
                        }
                    }
                    tmp2.add(row2);
                }
                try {
                    int cint = Integer.parseInt(cname);
                    tmpINT.add(cint);
                } catch (Exception ex) {

                }
            }

            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            String[] counter2 = tmp2.toArray(new String[0]);
            Arrays.sort(counter2);
            String[] counter2TER1 = tmp2TER1.toArray(new String[0]);
            Arrays.sort(counter2TER1);
            String[] counter2TER2 = tmp2TER2.toArray(new String[0]);
            Arrays.sort(counter2TER2);
            String[] counterHKTINT = tmpHKTINT.toArray(new String[0]);
            Arrays.sort(counterHKTINT);
            String[] counterHKTDOM = tmpHKTDOM.toArray(new String[0]);
            Arrays.sort(counterHKTDOM);
            String[] counterCNXINT = tmpCNXINT.toArray(new String[0]);
            Arrays.sort(counterCNXINT);
            String[] counterCNXDOM = tmpCNXDOM.toArray(new String[0]);
            Arrays.sort(counterCNXDOM);

            // Arrays.sort(counter2);

            for (String counter : counter1) {
                result1 += counter;
            }
            if (result1.equals("Y")) {
                YCheck = true;
            }
            result1 = result1.replace("Y", "");

            for (String counter : counter2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception Ex) {
                }
                if (result2.equals("")) {
                    result2 = counter;
                } else {
                    result2 += "," + counter;
                }
            }

            for (String counter : counter2TER1) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception Ex) {
                }
                if (result2TER1.equals("")) {
                    result2TER1 = counter;
                } else {
                    result2TER1 += "," + counter;
                }
            }
            for (String counter : counter2TER2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception Ex) {
                }
                if (result2TER2.equals("")) {
                    result2TER2 = counter;
                } else {
                    result2TER2 += "," + counter;
                }
            }

            for (String counter : counterHKTINT) {
                resultHKTINT += counter;
            }
            ArrayList<Integer> ctrHKTDOM = new ArrayList<Integer>();
            for (String counter : counterHKTDOM) {
                try {
                    ctrHKTDOM.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultHKTDOM = getSumString(ctrHKTDOM.toArray(new Integer[0]));

            ArrayList<Integer> ctrCNXINT = new ArrayList<Integer>();
            for (String counter : counterCNXINT) {
                try {
                    ctrCNXINT.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultCNXINT = getSumString(ctrCNXINT.toArray(new Integer[0]));

            ArrayList<Integer> ctrCNXDOM = new ArrayList<Integer>();
            for (String counter : counterCNXDOM) {
                try {
                    ctrCNXDOM.add(Integer.parseInt(counter));
                } catch (Exception ex) {

                }
            }

            resultCNXDOM = getSumString(ctrHKTDOM.toArray(new Integer[0]));

            result3 = getSumString(tmpINT.toArray(new Integer[0]));

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);
            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(new Date()); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, -7); // adds one hour
            Date now = cal.getTime();

            if (ctrstart.equals("              ")) {// Not Open
                result1 = result1;
                result2 = result2;
                result2TER1 = result2TER1;
                result2TER2 = result2TER2;
                result3 = result3;
                resultHKTINT = resultHKTINT;
                resultHKTDOM = resultHKTDOM;
                resultCNXINT = resultCNXINT;
                resultCNXDOM = resultCNXDOM;
            } else if (ctrend.equals("              ") && ctrstart.trim().length() == 14) {// Open Now Not Close
                Date cstart = aodbDate.parse(ctrstart);
                if (cstart.before(now)) {
                    result1 += "|OPEN";
                    result2 += "|OPEN";
                    result2TER1 += "|OPEN";
                    result2TER2 += "|OPEN";
                    result3 += "|OPEN";
                    resultHKTINT = resultHKTINT + "|OPEN";
                    resultHKTDOM = resultHKTDOM + "|OPEN";
                    resultCNXINT = resultCNXINT + "|OPEN";
                    resultCNXDOM = resultCNXDOM + "|OPEN";
                } else {
                    result1 = result1;
                    result2 = result2;
                    result2TER1 = result2TER1;
                    result2TER2 = result2TER2;
                    result3 = result3;
                    resultHKTINT = resultHKTINT;
                    resultHKTDOM = resultHKTDOM;
                    resultCNXINT = resultCNXINT;
                    resultCNXDOM = resultCNXDOM;
                }
            } else if (ctrstart.trim().length() == 14 && ctrend.trim().length() == 14) {// Consider by OPEN till End
                Date cstart = aodbDate.parse(ctrstart);
                Date cend = aodbDate.parse(ctrend);
                if (cstart.before(now) && cend.after(now)) {
                    result1 += "|OPEN";
                    result2 += "|OPEN";
                    result2TER1 += "|OPEN";
                    result2TER2 += "|OPEN";
                    result3 += "|OPEN";
                    resultHKTINT = resultHKTINT + "|OPEN";
                    resultHKTDOM = resultHKTDOM + "|OPEN";
                    resultCNXINT = resultCNXINT + "|OPEN";
                    resultCNXDOM = resultCNXDOM + "|OPEN";
                } else {
                    result1 += "|CLOSE";
                    result2 += "|CLOSE";
                    result2TER1 += "|CLOSE";
                    result2TER2 += "|CLOSE";
                    result3 += "|CLOSE";
                    resultHKTINT = resultHKTINT + "|CLOSE";
                    resultHKTDOM = resultHKTDOM + "|CLOSE";
                    resultCNXINT = resultCNXINT + "|CLOSE";
                    resultCNXDOM = resultCNXDOM + "|CLOSE";
                }
            }
        }
        rs.close();
        stmt.close();
        obj.put("ctrstatus", result1);
        obj.put("ctrstatus2", result2);
        obj.put("ctrstatus2TER1", result2TER1);
        obj.put("ctrstatus2TER2", result2TER2);
        obj.put("ctrstatus3", result3);
        obj.put("ctrstatusHKTINT", resultHKTINT);
        obj.put("ctrstatusHKTDOM", resultHKTDOM);
        obj.put("ctrstatusCNXINT", resultCNXINT);
        obj.put("ctrstatusCNXDOM", resultCNXDOM);
        obj.put("YONLY", YCheck);
        return obj;
    }

    public JSONObject getDedicateCounterByURNOBYTERM(String URNO, Connection c, JSONObject CKICTERM)
            throws SQLException, ParseException {
        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select min(nullif(ckba,' ')) as ctrstart,max(case when ckea!='              ' and ckea is not null then ckea else ckes end) as ctrend, listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNU,"
                + " min(ckic) as minckic, max(ckic) as maxckic,CKIT " +
                " from fids_ccatab " +
                " where (ctyp=' ' or ctyp is null) and FLNU='" + URNO
                + "' and (CKIC !='     ' and CKIC is not null)  and (DISP is null or DISP!='99') " +
                " group by FLNU,CKIT";
        // System.out.println(SQL);
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";
        String result2 = "";
        String result2TER1 = "";
        String result2TER2 = "";
        String result3 = "";
        String resultHKTINT = "";
        String resultHKTDOM = "";
        String resultCNXINT = "";
        String resultCNXDOM = "";
        boolean YCheck = false;
        while (rs.next()) {
            String TERM = rs.getString("CKIT") == null ? "" : rs.getString("CKIT");
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");

            HashSet<String> tmp1 = new HashSet<String>();
            HashSet<String> tmp2 = new HashSet<String>();
            // HashSet<String> tmp2TER1=new HashSet<String>();
            // HashSet<String> tmp2TER2=new HashSet<String>();
            ArrayList<Integer> tmpINT = new ArrayList<Integer>();
            HashSet<String> tmpHKT = new HashSet<String>();
            // HashSet<String> tmpHKTDOM=new HashSet<String>();
            HashSet<String> tmpCNX = new HashSet<String>();
            // HashSet<String> tmpCNXDOM=new HashSet<String>();
            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                if (cname.length() >= 2) {// 01
                    tmp1.add(cname.substring(0, 1));
                    String row2 = cname.substring(0, 2);

                    if (CKICTERM.get(cname) != null) {
                        // String TERM = CKICTERM.get(cname)+"";
                        TERM = TERM.trim();
                        if (TERM.equals("1") || TERM.equals("I")) {
                            tmpHKT.add(cname.substring(0, 1));
                        }
                        if (TERM.equals("2") || TERM.equals("D")) {
                            tmpHKT.add(row2);
                        }
                    }
                    tmp2.add(row2);
                    tmpCNX.add(cname);
                }
                try {
                    int cint = Integer.parseInt(cname);
                    tmpINT.add(cint);
                } catch (Exception ex) {

                }
            }

            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            String[] counter2 = tmp2.toArray(new String[0]);
            Arrays.sort(counter2);
            // String[] counter2TER1 = tmp2TER1.toArray(new String[0]);
            // Arrays.sort(counter2TER1);
            // String[] counter2TER2 = tmp2TER2.toArray(new String[0]);
            // Arrays.sort(counter2TER2);
            // String[] counterHKTINT = tmpHKTINT.toArray(new String[0]);
            // Arrays.sort(counterHKTINT);
            String[] counterHKT = tmpHKT.toArray(new String[0]);
            Arrays.sort(counterHKT);
            // String[] counterCNXINT = tmpCNXINT.toArray(new String[0]);
            // Arrays.sort(counterCNXINT);
            String[] counterCNX = tmpCNX.toArray(new String[0]);
            Arrays.sort(counterCNX);

            // Arrays.sort(counter2);

            for (String counter : counter1) {
                result1 += counter;
            }
            if (result1.equals("Y")) {
                YCheck = true;
            }
            result1 = result1.replace("Y", "");

            for (String counter : counter2) {
                try {
                    counter = Integer.parseInt(counter) + "";
                } catch (Exception ex) {

                }
                if (result2.equals("")) {
                    result2 = counter;
                } else {
                    result2 += "," + counter;
                }
            }

            if (TERM.equals("1")) {
                result2TER1 = result2;
            } else if (TERM.equals("2")) {
                result2TER2 = result2;
            }

            // for(String counter:counter2TER1){
            // try {
            // counter = Integer.parseInt(counter)+"";
            // }catch(Exception ex) {
            //
            // }
            // if(result2TER1.equals("")) {
            // result2TER1=counter;
            // }else {
            // result2TER1+=","+counter;
            // }
            // }
            // for(String counter:counter2TER2){
            // try {
            // counter = Integer.parseInt(counter)+"";
            // }catch(Exception ex) {
            //
            // }
            // if(result2TER2.equals("")) {
            // result2TER2=counter;
            // }else {
            // result2TER2+=","+counter;
            // }
            // }

            if (TERM.equals("I")) {
                for (String counter : counterHKT) {
                    resultHKTINT += counter;
                }
            } else if (TERM.equals("D")) {
                ArrayList<Integer> ctrHKT = new ArrayList<Integer>();
                for (String counter : counterHKT) {
                    try {
                        ctrHKT.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultHKTDOM = getSumString(ctrHKT.toArray(new Integer[0]));
            }
            if (TERM.equals("I")) {
                ArrayList<Integer> ctrCNXINT = new ArrayList<Integer>();
                for (String counter : counterCNX) {
                    try {
                        ctrCNXINT.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultCNXINT = getSumString(ctrCNXINT.toArray(new Integer[0]));
            } else if (TERM.equals("D")) {
                ArrayList<Integer> ctrCNXDOM = new ArrayList<Integer>();
                for (String counter : counterCNX) {
                    try {
                        ctrCNXDOM.add(Integer.parseInt(counter));
                    } catch (Exception ex) {

                    }
                }
                resultCNXDOM = getSumString(ctrCNXDOM.toArray(new Integer[0]));
            }

            result3 = getSumString(tmpINT.toArray(new Integer[0]));

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);
            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(new Date()); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, -7); // adds one hour
            Date now = cal.getTime();

            if (ctrstart.equals("              ")) {// Not Open
                result1 = result1;
                result2 = result2;
                result2TER1 = result2TER1;
                result2TER2 = result2TER2;
                result3 = result3;
                resultHKTINT = resultHKTINT;
                resultHKTDOM = resultHKTDOM;
                resultCNXINT = resultCNXINT;
                resultCNXDOM = resultCNXDOM;
            } else if (ctrend.equals("              ") && ctrstart.trim().length() == 14) {// Open Now Not Close
                Date cstart = aodbDate.parse(ctrstart);
                if (cstart.before(now)) {
                    result1 = result1 + "|OPEN";
                    result2 = result2 + "|OPEN";
                    result3 = result3 + "|OPEN";
                    if (TERM.equals("I") || TERM.equals("1")) {
                        result2TER1 = result2TER1 + "|OPEN";
                        resultHKTINT = resultHKTINT + "|OPEN";
                        resultCNXINT = resultCNXINT + "|OPEN";
                    } else if (TERM.equals("D") || TERM.equals("2")) {
                        result2TER2 = result2TER2 + "|OPEN";
                        resultHKTDOM = resultHKTDOM + "|OPEN";
                        resultCNXDOM = resultCNXDOM + "|OPEN";
                    }
                } else {
                    result1 = result1;
                    result2 = result2;
                    result2TER1 = result2TER1;
                    result2TER2 = result2TER2;
                    result3 = result3;
                    resultHKTINT = resultHKTINT;
                    resultHKTDOM = resultHKTDOM;
                    resultCNXINT = resultCNXINT;
                    resultCNXDOM = resultCNXDOM;
                }
            } else if (ctrstart.trim().length() == 14 && ctrend.trim().length() == 14) {// Consider by OPEN till End
                Date cstart = aodbDate.parse(ctrstart);
                Date cend = aodbDate.parse(ctrend);
                if (cstart.before(now) && cend.after(now)) {
                    result1 = result1 + "|OPEN";
                    result2 = result2 + "|OPEN";
                    result3 = result3 + "|OPEN";
                    if (TERM.equals("I") || TERM.equals("1")) {
                        result2TER1 = result2TER1 + "|OPEN";
                        resultHKTINT = resultHKTINT + "|OPEN";
                        resultCNXINT = resultCNXINT + "|OPEN";
                    } else if (TERM.equals("D") || TERM.equals("2")) {
                        result2TER2 = result2TER2 + "|OPEN";
                        resultHKTDOM = resultHKTDOM + "|OPEN";
                        resultCNXDOM = resultCNXDOM + "|OPEN";
                    }
                } else {
                    result1 = result1 + "|CLOSE";
                    result2 = result2 + "|CLOSE";
                    result3 = result3 + "|CLOSE";
                    if (TERM.equals("I") || TERM.equals("1")) {
                        result2TER1 = result2TER1 + "|CLOSE";
                        resultHKTINT = resultHKTINT + "|CLOSE";
                        resultCNXINT = resultCNXINT + "|CLOSE";
                    } else if (TERM.equals("D") || TERM.equals("2")) {
                        result2TER2 = result2TER2 + "|CLOSE";
                        resultHKTDOM = resultHKTDOM + "|CLOSE";
                        resultCNXDOM = resultCNXDOM + "|CLOSE";
                    }
                }
            }
        }
        rs.close();
        stmt.close();
        obj.put("ctrstatus", result1);
        obj.put("ctrstatus2", result2);
        obj.put("ctrstatus2TER1", result2TER1);
        obj.put("ctrstatus2TER2", result2TER2);
        obj.put("ctrstatus3", result3);
        obj.put("ctrstatusHKTINT", resultHKTINT);
        obj.put("ctrstatusHKTDOM", resultHKTDOM);
        obj.put("ctrstatusCNXINT", resultCNXINT);
        obj.put("ctrstatusCNXDOM", resultCNXDOM);
        obj.put("YONLY", YCheck);
        return obj;
    }

    public JSONObject getDedicateCounterByTERM(String URNO, Connection c, String CKIT)
            throws SQLException, ParseException {
        JSONObject obj = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select min(nullif(ckba,' ')) as ctrstart,max(case when ckea!='              ' and ckea is not null then ckea else ckes end) as ctrend, listagg(CKIC,'|') WITHIN GROUP (ORDER BY CKIC) as COUNTER,FLNU,"
                + " min(ckic) as minckic, max(ckic) as maxckic " +
                " from fids_ccatab " +
                " where (ctyp=' ' or ctyp is null) and FLNU='" + URNO
                + "' and (CKIC !='     ' and CKIC is not null) and CKIT='" + CKIT + "'" +
                " group by FLNU";
        // System.out.println(SQL);
        ResultSet rs = stmt.executeQuery(SQL);
        String result1 = "";

        if (rs.next()) {
            String[] counterlist = (rs.getString("COUNTER") == null ? "" : rs.getString("COUNTER")).split("\\|");

            HashSet<String> tmp1 = new HashSet<String>();

            for (int i = 0; i < counterlist.length; i++) {
                String cname = counterlist[i];
                cname = cname.trim();
                tmp1.add(cname);
            }

            String[] counter1 = tmp1.toArray(new String[0]);
            Arrays.sort(counter1);
            //
            //
            // ArrayList<Integer> ctrint = new ArrayList<Integer>();
            // for(String counter:counter1){// Counter 10-12
            // try {
            // ctrint.add(Integer.parseInt(counter));
            // }catch(Exception ex) {
            //
            // }
            // }
            // result1=getSumString(ctrint.toArray(new Integer[0]));
            result1 = getSumString2(counter1);

            String ctrstart = rs.getString("CTRSTART") == null ? "              " : rs.getString("CTRSTART");
            String ctrend = rs.getString("CTREND") == null ? "              " : rs.getString("CTREND");
            obj.put("ctrstart", ctrstart);
            obj.put("ctrend", ctrend);
            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");

            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(new Date()); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, -7); // adds one hour
            Date now = cal.getTime();

            if (ctrstart.equals("              ")) {// Not Open
                result1 = result1;
            } else if (ctrend.equals("              ") && ctrstart.trim().length() == 14) {// Open Now Not Close
                Date cstart = aodbDate.parse(ctrstart);
                if (cstart.before(now)) {
                    result1 += "|OPEN";
                } else {
                    result1 = result1;
                }
            } else if (ctrstart.trim().length() == 14 && ctrend.trim().length() == 14) {// Consider by OPEN till End
                Date cstart = aodbDate.parse(ctrstart);
                Date cend = aodbDate.parse(ctrend);
                if (cstart.before(now) && cend.after(now)) {
                    result1 += "|OPEN";
                } else {
                    result1 += "|CLOSE";
                }
            }
        }
        rs.close();
        stmt.close();

        obj.put("ctrstatus", result1);

        return obj;
    }

    private String GateRemarkChange(String remin, JSONObject remarkobj) throws SQLException {
        String result = remin;
        if (remin.equals("OPEN")) {
            // result="OPEN|เธ—เธฒเธ�เธญเธญเธ�เน€เธ�เธดเธ”";
            result = getRemarkDelay("DEP", "GATEOPEN", remarkobj);
        } else if (remin.equals("BOARDING")) {
            // result="BOARDING|เธ�เธถเน�เธ�เน€เธ�เธฃเธทเน�เธญเธ�";
            result = getRemarkDelay("DEP", "BOARDING", remarkobj);
        } else if (remin.equals("FINAL CALL")) {
            // result="FINAL CALL|เธ�เธถเน�เธ�เน€เธ�เธฃเธทเน�เธญเธ�เธ”เน�เธงเธ�";
            result = getRemarkDelay("DEP", "FINAL CALL", remarkobj);
        } else if (remin.equals("CLOSE")) {
            // result="CLOSE|เธ—เธฒเธ�เธญเธญเธ�เธ�เธดเธ”";
            result = getRemarkDelay("DEP", "GATECLOSE", remarkobj);
        } else if (remin.equals("DEPARTED")) {
            // result="DEPARTED|เน€เธ�เธฃเธทเน�เธญเธ�เธ�เธถเน�เธ�";
            result = getRemarkDelay("DEP", "DEPARTED", remarkobj);
        }
        return result;
    }

    public boolean checkFirstGateChangeByURNO(String URNO, String FromTime, String ToTime, Connection c) {
        try {
            boolean result = false;
            Statement stmt = c.createStatement();
            String SQL = "select * from FIDS_GATE_HISTORY where URNO ='" + URNO + "' and update_time > to_date('"
                    + FromTime
                    + "','yyyymmddhh24miss') and newgate1!=oldgate1 and (oldgate1!='     ' and oldgate1 is not null)";
            // System.out.println(SQL);
            ResultSet rs = stmt.executeQuery(SQL);
            if (rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();
            return result;
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    public boolean checkSecondGateChangeByURNO(String URNO, String FromTime, String ToTime, Connection c)
            throws ClassNotFoundException, SQLException {
        try {
            boolean result = false;
            Statement stmt = c.createStatement();
            String SQL = "select * from FIDS_GATE_HISTORY where URNO ='" + URNO + "' and update_time > to_date('"
                    + FromTime
                    + "','yyyymmddhh24miss') and newgate2!=oldgate2 and (oldgate2!='     ' and oldgate2 is not null)";
            ResultSet rs = stmt.executeQuery(SQL);
            if (rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();
            return result;
        } catch (Exception ex) {
            // ex.printStackTrace();
            return false;
        }
    }

    public JSONObject gePrevFlightByGate1Change(String Gate, String FromTime, String ToTime, Connection c, String HOPO)
            throws ClassNotFoundException, SQLException {
        if (HOPO.equals("BKK")) {
            if (Gate.trim().length() > 2) {
                Gate = Gate.substring(0, 2);
            }
        }
        JSONObject result = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select NEWGATE1,FLNO from FIDS_GATE_HISTORY where OLDGATE1 like '" + Gate
                + "%'  and update_time > to_date('" + FromTime + "','yyyymmddhh24miss') and hopo='" + HOPO
                + "' order by update_time desc";
        ResultSet rs = stmt.executeQuery(SQL);
        String FLNO = "";
        String NEWGATE = "";
        while (rs.next()) {
            if (FLNO.length() > 0) {
                FLNO = FLNO + "|" + rs.getString("FLNO");
                NEWGATE = NEWGATE + "|" + rs.getString("NEWGATE1");
            } else {
                FLNO = rs.getString("FLNO");
                NEWGATE = rs.getString("NEWGATE1");
            }

        }
        if (FLNO.length() > 0) {
            result.put("FLNO", FLNO);
            result.put("NEWGATE", NEWGATE);
        }
        rs.close();
        stmt.close();
        return result;
    }

    public JSONObject gePrevFlightByGate2Change(String Gate, String FromTime, String ToTime, Connection c, String HOPO)
            throws ClassNotFoundException, SQLException {
        if (HOPO.equals("BKK")) {
            if (Gate.trim().length() > 2) {
                Gate = Gate.substring(0, 2);
            }
        }
        JSONObject result = new JSONObject();
        Statement stmt = c.createStatement();
        String SQL = "select NEWGATE2,FLNO from FIDS_GATE_HISTORY where OLDGATE2 like '" + Gate
                + "%'  and update_time > to_date('" + FromTime + "','yyyymmddhh24miss') and hopo='" + HOPO
                + "' order by update_time desc";
        ResultSet rs = stmt.executeQuery(SQL);
        String FLNO = "";
        String NEWGATE = "";
        while (rs.next()) {
            if (FLNO.length() > 0) {
                FLNO = FLNO + "|" + rs.getString("FLNO");
                NEWGATE = NEWGATE + "|" + rs.getString("NEWGATE2");
            } else {
                FLNO = rs.getString("FLNO");
                NEWGATE = rs.getString("NEWGATE2");
            }
        }
        if (FLNO.length() > 0) {
            result.put("FLNO", FLNO);
            result.put("NEWGATE", NEWGATE);
        }
        rs.close();
        stmt.close();
        return result;
    }

    public JSONObject getWeatherInfo(Connection c) throws SQLException {
        String SQL = "select APC3,WEATHER from FIDS_AIRPORT where WEATHER is not null";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(SQL);
        JSONObject result = new JSONObject();
        while (rs.next()) {
            result.put(rs.getString("APC3"), rs.getString("WEATHER"));
        }
        rs.close();
        stmt.close();
        return result;
    }

    public String getCODESHAREByAirline(String Airline, Connection c, String HOPO) throws SQLException {

        String SQL = "select jfno,sobt from FIDS_afttab " +
                " where hopo='" + HOPO + "' and (alc2='" + Airline + "' or alc3='" + Airline
                + "') and adid='D' and SOBT between to_char(sysdate-(7/24)+(30/1440),'yyyymmddhh24miss') and to_char(sysdate-(7/24)+(3/24),'yyyymmddhh24miss')";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(SQL);
        HashSet<String> tmp2 = new HashSet<String>();
        while (rs.next()) {
            String JFNO = rs.getString("JFNO");
            if (JFNO != null && !JFNO.trim().equals("")) {
                JFNO = JFNO.replace("   ", "  ");
                String[] JLIST = JFNO.split("  ");
                for (int i = 0; i < JLIST.length; i++) {
                    String SHAREALC2 = JLIST[i].substring(0, 3).trim();// JLIST[i].split(" ")[0];
                    tmp2.add(SHAREALC2);
                }
            }
        }
        rs.close();
        stmt.close();
        String result = "";
        String[] tmp = (String[]) tmp2.toArray(new String[0]);
        for (int i = 0; i < tmp.length; i++) {
            if (i == 0) {
                result = tmp[i];
            } else {
                result = result + "|" + tmp[i];
            }
        }
        return result;
    }

    public Date getFinalCallTime(String URNO, Connection c) throws SQLException {
        String SQL = "select UPDATE_TIME from fids_finalcall_history where URNO='" + URNO
                + "' order by update_time desc";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(SQL);
        Date result = null;
        if (rs.next()) {
            result = rs.getTimestamp("UPDATE_TIME");
        }
        rs.close();
        stmt.close();
        return result;
    }

    public String getRemarkDelay(String DS, String remark, JSONObject remarkobj) throws SQLException {
        // String SQL = "select DISPLAY_REMARK from FIDS_REMARK where DSNAME='"+DS+"'
        // and REMARK='"+remark+"' and DISPLAY_REMARK is not null";
        // Statement stmt = c.createStatement();
        // ResultSet rs = stmt.executeQuery(SQL);
        // String result = "";
        // if(rs.next()) {
        // result = URLDecoder.decode(rs.getString("DISPLAY_REMARK"));
        // }
        // rs.close();
        // stmt.close();
        String result = remarkobj.get(DS + "_" + remark) + "";
        if (remarkobj.get(DS + "_" + remark) == null) {
            result = "";
        }
        return result;
    }

    public JSONObject initRemark(Connection crep) {
        JSONObject result = new JSONObject();
        try {
            Statement stmt = crep.createStatement();
            String SQL = "select DISPLAY_REMARK, DSNAME, REMARK from FIDS_REMARK where DISPLAY_REMARK is not null";
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                String key = rs.getString("DSNAME") + "_" + rs.getString("REMARK");
                String decoded = URLDecoder.decode(rs.getString("DISPLAY_REMARK"), "UTF-8");
                result.put(key, decoded);
            }
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public JSONObject initClassDB(Connection crep) {
        JSONObject result = new JSONObject();
        try {
            Statement stmt = crep.createStatement();
            String SQL = "select AIRLINE,CLASS,CLASSNAME,HOPO from fids_pax_class";
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                String Key = rs.getString("AIRLINE") + "_" + rs.getString("CLASS") + "_" + rs.getString("HOPO");
                result.put(Key, rs.getString("CLASSNAME"));
                // System.out.println(Key +"="+ rs.getString("CLASSNAME"));
            }
            stmt.close();
            // crep.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public JSONObject getCKICTerminalList(String HOPO, Connection crep) {
        JSONObject result = new JSONObject();
        try {
            Statement stmt = crep.createStatement();
            String SQL = "select CNAM,TERM from Fids_CICTAB where cnam is not null and term is not null and hopo='"
                    + HOPO + "'";
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                String Key = rs.getString("CNAM").trim();
                result.put(Key, rs.getString("TERM").trim());
            }
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public String getSumString(Integer[] namelist) {
        if (namelist.length > 0) {
            Arrays.sort(namelist);
            String result = "";
            for (int i = 0; i < namelist.length; i++) {
                int cur = namelist[i];
                if (result.length() == 0) {
                    result = cur + "";
                } else if (result.length() != 0) {
                    if ((cur - 1) != namelist[i - 1]) {// not continue
                        if (result.endsWith(namelist[i - 1] + "")) {// single values detected!!
                            result += "," + cur;
                        } else {
                            result += "-" + namelist[i - 1] + "," + cur;
                        }
                    }
                }
            }
            if (!result.endsWith(namelist[namelist.length - 1] + "")) {
                result += "-" + namelist[namelist.length - 1];
            }
            return result;
        } else {
            return "";
        }
    }

    public String getSumString2(String[] counterlist) {
        String letter = "";
        int num = 0;
        int previousnum = 0;
        int total = 1;
        String sumcounter = "";
        String result = "";
        ArrayList<Integer> ctrint = new ArrayList<Integer>();
        ArrayList<String> ctrletter = new ArrayList<String>();

        for (int i = 0; i < counterlist.length; i++) {
            try {
                ctrint.add(Integer.parseInt(counterlist[i]));
            } catch (NumberFormatException e) {
                ctrletter.add(counterlist[i]);
            }
        }
        Arrays.sort(ctrint.toArray(new Integer[0]));
        // System.out.println(ctrint);
        // System.out.println(ctrletter);

        for (int i = 0; i < ctrint.size(); i++) {
            int cur = ctrint.get(i);
            if (result.length() == 0) {
                result = cur + "";
            } else if (result.length() != 0) {
                if ((cur - 1) != ctrint.get(i - 1)) {// not continue
                    if (result.endsWith(ctrint.get(i - 1) + "")) {// single values detected!!
                        result += "," + cur;
                    } else {
                        result += "-" + ctrint.get(i - 1) + "," + cur;
                    }
                }
            }
        }
        if (ctrint.size() > 0 && !result.endsWith(ctrint.get(ctrint.size() - 1) + "")) {
            result += "-" + ctrint.get(ctrint.size() - 1);
        }

        for (int i = 0; i < ctrletter.size(); i++) {
            result += "," + ctrletter.get(i);
        }
        if (ctrint.size() == 0) {
            result = result.substring(1);// Remove first comma when no ctrint (,A,B,C > A,B,C)
        }

        // System.out.println(result);
        return result;
    }

    public String UTC2LOCAL(String input) {
        try {
            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Date stddate = aodbDate.parse(input);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(stddate); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 7);
            return aodbDate.format(cal.getTime());
        } catch (Exception ex) {
            return input;
        }
    }

    public String LOCAL2UTC(String input) {
        try {
            SimpleDateFormat aodbDate = new SimpleDateFormat("yyyyMMddHHmmss");
            Date stddate = aodbDate.parse(input);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(stddate); // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, -7);
            return aodbDate.format(cal.getTime());
        } catch (Exception ex) {
            return input;
        }
    }

    public String AddCounterRemarkSuffix(String input, String FillKeyWord) {
        String output = "";
        if (input.startsWith("Ck-in Close") || input.startsWith("Ck-in Open")) {
            String[] remlist = input.split("\\|");
            for (int i = 0; i < remlist.length; i++) {
                String rem = remlist[i];
                if (i <= 2) {
                    rem = rem + FillKeyWord;
                }
                if (i == 0) {
                    output = output + rem;
                } else {
                    output = output + "|" + rem;
                }
            }
            return output;
        } else {
            return input;
        }

    }

    public String substringForNokair(String FLNO) {
        if (FLNO.substring(3, 4).trim().equals("0")) {
            FLNO = FLNO.substring(0, 3) + Integer.parseInt(FLNO.substring(4).trim());
        }
        return FLNO;
    }

}
