package net.sphuta.tms.freelancer.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.header.HeaderWriter;

public class XFrameOptionsHeaderWriter implements HeaderWriter {
    public XFrameOptionsHeaderWriter(Object p0) {
    }

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {

    }
}
