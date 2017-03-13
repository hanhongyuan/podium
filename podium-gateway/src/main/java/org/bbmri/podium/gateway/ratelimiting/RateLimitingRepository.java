/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.gateway.ratelimiting;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import java.util.Date;

/**
 * Repository storing data used by the gateway's rate limiting filter.
 */
public class RateLimitingRepository {

    private final Session session;

    private PreparedStatement rateLimitingIncrement;

    private PreparedStatement rateLimitingCount;

    public RateLimitingRepository(Session session) {
        this.session = session;
        this.rateLimitingIncrement = session.prepare(
            "UPDATE gateway_ratelimiting\n" +
                "  SET value = value + 1\n" +
                "  WHERE id = :id AND time_unit = :time_unit AND time = :time");

        this.rateLimitingCount = session.prepare(
            "SELECT value\n" +
                "  FROM gateway_ratelimiting\n" +
                "  WHERE id = :id AND time_unit = :time_unit AND time = :time"
        );
    }

    public void incrementCounter(String id, String timeUnit, Date time) {
        BoundStatement stmt = rateLimitingIncrement.bind();
        stmt.setString("id", id);
        stmt.setString("time_unit", timeUnit);
        stmt.setTimestamp("time", time);
        session.executeAsync(stmt);
    }

    public long getCounter(String id, String timeUnit, Date time) {
        BoundStatement stmt = rateLimitingCount.bind();
        stmt.setString("id", id);
        stmt.setString("time_unit", timeUnit);
        stmt.setTimestamp("time", time);
        ResultSet rs = session.execute(stmt);
        if (rs.isExhausted()) {
            return 0;
        }
        return rs.one().getLong(0);
    }
}