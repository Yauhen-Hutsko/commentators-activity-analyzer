package com.hutsko.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hutsko.db.MonthlyCount;
import com.hutsko.db.ProfileAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * JSON endpoint with monthly user registration counts.
 * <p>
 * {@code GET /api/analytics/registrations?from=YYYY-MM&to=YYYY-MM} (both parameters optional, inclusive).
 */
@WebServlet(name = "registrationsApi", urlPatterns = "/api/analytics/registrations")
public class RegistrationsApiServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RegistrationsApiServlet.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private transient ProfileAnalyticsRepository repository;

    @Override
    public void init() {
        repository = new ProfileAnalyticsRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        YearMonth from;
        YearMonth to;
        try {
            from = parseMonth(req.getParameter("from"));
            to = parseMonth(req.getParameter("to"));
        } catch (DateTimeParseException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameters 'from' and 'to' must be in YYYY-MM format");
            return;
        }

        List<MonthlyCount> series;
        try {
            series = repository.registrationsByMonth(from, to);
        } catch (RuntimeException e) {
            log.error("Failed to load monthly registrations", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load data from the database");
            return;
        }

        ObjectNode root = mapper.createObjectNode();
        ArrayNode items = root.putArray("series");
        long total = 0;
        for (MonthlyCount row : series) {
            items.addObject().put("month", row.getMonth()).put("count", row.getCount());
            total += row.getCount();
        }
        root.put("total", total);
        mapper.writeValue(resp.getOutputStream(), root);
    }

    private static YearMonth parseMonth(String value) {
        return value == null || value.trim().isEmpty() ? null : YearMonth.parse(value.trim());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        ObjectNode error = mapper.createObjectNode().put("error", message);
        mapper.writeValue(resp.getOutputStream(), error);
    }
}
