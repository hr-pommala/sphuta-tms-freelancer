package net.sphuta.tms.service.impl;

import net.sphuta.tms.service.TmsService;

public class TmsServiceImpl implements TmsService {
    @Override
    public String getServiceStatus() {
        return "TMS Service is running";
    }
}
