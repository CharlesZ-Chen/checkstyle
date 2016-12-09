package com.puppycrawl.tools.checkstyle;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public class HackJsonLogger extends AutomaticBean
implements AuditListener {

    private AuditEventHackJsonFormatter formatter;

    /** Helper writer that allows easy encoding and printing. */
    private PrintWriter writer;

    /** Close output stream in auditFinished. */
    private final boolean closeStream;

    private HashMap<String, JsonArrayBuilder> issueEventsMap;

    private enum InterestingIssue {
        
        MULTI_DECL("MultipleVariableDeclarations");
        private String checkShortName;

        private InterestingIssue(String checkShortName) {
            this.checkShortName = checkShortName;
        }

        @Override
        public String toString() {
            return this.checkShortName;
        }
    }

    public HackJsonLogger(OutputStream outputStream, boolean closeStream) {
        final OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        this.writer = new PrintWriter(osw);
        this.closeStream = closeStream;
        this.formatter = new AuditEventHackJsonFormatter();
        issueEventsMap = new HashMap<>();
        for (InterestingIssue issue : InterestingIssue.values()) {
            JsonArrayBuilder eventsArrayBuilder = Json.createArrayBuilder();
            issueEventsMap.put(issue.toString(), eventsArrayBuilder);
        }
    }

    @Override
    public void auditStarted(AuditEvent event) {
        // do nothing, keep silence
    }

    @Override
    public void auditFinished(AuditEvent event) {
        JsonObjectBuilder output = Json.createObjectBuilder();
        for (Map.Entry<String, JsonArrayBuilder> entry : issueEventsMap.entrySet()) {
            output.add(entry.getKey(), entry.getValue());
        }
        writer.println(output.build());
        if (closeStream) {
            writer.close();
        }
        else {
            writer.flush();
        }
    }

    @Override
    public void addError(AuditEvent event) {
        final SeverityLevel severityLevel = event.getSeverityLevel();
        if (severityLevel != SeverityLevel.IGNORE) {
            String checkShortName = AuditEventHackJsonFormatter.getCheckShortName(event);
            if (isInterestedCheck(checkShortName)) {
                JsonObject singleEvent = formatter.jsonFormat(event);
                issueEventsMap.get(checkShortName).add(singleEvent);
            }
        }
    }

    private boolean isInterestedCheck(String checkShortName) {
        boolean isInterested = false;
        for (InterestingIssue issue : InterestingIssue.values()) {
            if (issue.toString().equals(checkShortName)) {
                isInterested = true;
                break;
            }
        }
        return isInterested;
    }

    @Override
    public void fileStarted(AuditEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fileFinished(AuditEvent event) {
        
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // TODO Auto-generated method stub
        
    }
}
