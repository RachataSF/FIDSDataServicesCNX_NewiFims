package com.cnx.fidsdataservicescnx.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cnx.fidsdataservicescnx.Service.GetDataByDsName;

@RestController
@RequestMapping
public class FidsDataBKKController {
    @Autowired
    private GetDataByDsName getDataByDsName;

    @GetMapping("/getDataByDSName")
    public String getDataByDSName(@RequestParam String DS, @RequestParam String HOPO) {
        return getDataByDsName.getData(DS, HOPO);
    }
}
