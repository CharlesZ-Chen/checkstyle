// checkstyle: Checks Java source code for adherence to a set of rules.

package com.puppycrawl.tools.checkstyle;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.puppycrawl.tools.checkstyle.HackJsonLogger.InterestingIssue;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

/**
 * A hacked version of Formatter that provides Json interface.
 * @author charleszhuochen
 *
 */
public class AuditEventHackJsonFormatter extends AuditEventDefaultFormatter {

    /**
     * Format a {@code event} to json.
     * @param event an audit event
     * @return a {@code JsonObject} that represent the information of this {@code event}
     */
    public JsonObject jsonFormat(AuditEvent event) {
        JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder()
                .add("file", event.getFileName())
                .add("line", event.getLine());
        if (getCheckShortName(event).equals(InterestingIssue.MULTI_DECL.toString())) {
            // Note: this is a hack, to using custom message in
            //       LocalizedMessage to record the declared type,
            // Note: in order to propagated type information to output
            String declaredType = event.getLocalizedMessage().getMessage();
            jsonObjBuilder.add("declared_type", declaredType);
        }
        return jsonObjBuilder.build();
    }
}
