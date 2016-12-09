package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.AstTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

public class HackedMultipleVariableDeclarationsCheck extends MultipleVariableDeclarationsCheck {
    @Override
    protected void log(DetailAST ast, String key, Object... args) {
        String declaredType = AstTreeStringPrinter.getDeclarationTypeFromVarDecl(ast);
        log(ast.getLineNo(), ast.getColumnNo(), key, declaredType, args);
    }

    protected void log(int line, int col, String key, String declaredType, Object... args) {
        messages.add(
                new LocalizedMessage(
                    line,
                    getMessageBundle(),
                    key,
                    args,
                    getSeverityLevel(),
                    getId(),
                    getClass(),
                    // Note: this is a hack, propagated declaredType info by customMessage in LocalizedMessage
                    declaredType));
    }
}
