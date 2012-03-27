/*
 * Copyright (c) 2012, Thomas Westphal
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.aonnet.gcouch

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class CouchDbSupportTransformation implements ASTTransformation {

    String annotationType = CouchDbSupport.class.name

    SourceUnit sourceUnit
    ASTNode annotation

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
        addMessage('test 1')

        if (!checkClassNode(astNodes, annotationType)) {
            // add an error message or a warning
            addError("Internal error on annotation");
            return
        }
        addMessage('test 2')

        addMethods((ClassNode) astNodes[1]);
        addMessage('test 3')
    }

    private boolean checkClassNode(ASTNode[] astNodes, String annotationType) {
        addMessage('- test 1')
        if (!astNodes) return false
        addMessage('- test 2')
        if (!astNodes[0]) return false
        annotation = astNodes[0]

        addMessage('- test 3')
        if (!astNodes[1]) return false
        addMessage('- test 4')

        if (!(astNodes[0] instanceof AnnotationNode)) return false
        addMessage('- test 5')
        if (!astNodes[0].classNode?.name == annotationType) return false
        addMessage('- test 6')
        if (!(astNodes[1] instanceof ClassNode)) return false
        addMessage('- test 7')

//        addError('- test 7')
        true
    }

    private void addError(String msg) {
        def line = annotation?.lineNumber
        def col = annotation?.columnNumber
        SyntaxException se = new SyntaxException(msg + '\n', line, col)
        SyntaxErrorMessage sem = new SyntaxErrorMessage(se, sourceUnit)
        sourceUnit.errorCollector.addErrorAndContinue(sem)
    }

    private void addMessage(String msg) {
        //def line = annotation?.lineNumber
        //def col = annotation?.columnNumber
        WarningMessage sem = new WarningMessage(WarningMessage.PARANOIA, msg, null, sourceUnit)
        sourceUnit.errorCollector.addWarning(sem)
    }

    private void addMethods(ClassNode classNode) {

        addMessage('--- test 1')
        def phase = CompilePhase.SEMANTIC_ANALYSIS
        addMessage('--- test 2')
        List<ASTNode> ast = new AstBuilder().buildFromString(phase, false, """

         package ${classNode.packageName}

         class ${classNode.nameWithoutPackage} {

             String id
             String rev

             def create() {
                return de.aonnet.gcouch.CouchDbBeanSupport.getInstance().create(this)
             }

             static def read(String id) {
                return de.aonnet.gcouch.CouchDbBeanSupport.getInstance().read(id)
             }
         }
         """)
        addMessage('--- test 3')

        ast[1].methods.each { classNode.addMethod(it) }
        ast[1].fields.each { classNode.addField(it) }
        addMessage('--- test 4')
    }
}
