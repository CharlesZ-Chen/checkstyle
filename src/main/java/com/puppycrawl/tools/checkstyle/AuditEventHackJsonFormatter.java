package com.puppycrawl.tools.checkstyle;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;

public class AuditEventHackJsonFormatter extends AuditEventDefaultFormatter {

    public JsonObject jsonFormat(AuditEvent event) {
        JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder()
                .add("file", event.getFileName())
                .add("line", event.getLine())
                .add("column", event.getColumn());
        return jsonObjBuilder.build();
    }
}
