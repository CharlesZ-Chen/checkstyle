////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

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

    /** Close output stream in auditFinished. */
    private final boolean closeStream;

    private AuditEventHackJsonFormatter formatter;

    /** Helper writer that allows easy encoding and printing. */
    private PrintWriter writer;

    private Map<String, JsonArrayBuilder> issueEventsMap;

    protected enum InterestingIssue {
        
        MULTI_DECL("HackedMultipleVariableDeclarations");
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

    private static boolean isInterestedCheck(String checkShortName) {
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
            if (HackJsonLogger.isInterestedCheck(checkShortName)) {
                JsonObject singleEvent = formatter.jsonFormat(event);
                issueEventsMap.get(checkShortName).add(singleEvent);
            }
        }
    }

    @Override
    public void fileStarted(AuditEvent event) {
        // keep silence, do nothing
    }

    @Override
    public void fileFinished(AuditEvent event) {
        // keep silence, do nothing
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // TODO Auto-generated method stub
        
    }
}
