package com.cmbellis.caffeevento.lib.api.lib;

import com.cmbellis.caffeevento.lib.annotation.CEExport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chris on 7/18/16.
 */
@CEExport
public interface ServerHandler {
    void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;
}
