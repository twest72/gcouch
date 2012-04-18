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

import groovyx.net.http.ContentType
import org.junit.Test

class CouchDbHelperTest {

    @Test
    void testCreateViewMap() {

        String allVeranstaltungArrayKey = """
        // Alle Veranstaltungen anzeigen
        function(doc) {
         if(doc.type == "VERANSTALTUNG") {
          emit( [doc.datum, doc.uhrzeit_von, doc.titel], {"ortname": doc.ort.name, "titel": doc.titel, "beschreibung": doc.beschreibung, "veranstalter": doc.veranstalter, "veranstaltungsart": doc.veranstaltungsart, "eintritt": doc.eintritt, "reihe": doc.reihe});
         }
        }"""

        Map<String, Map<String, String>> view = CouchDbHelper.createViewMap('testView', allVeranstaltungArrayKey)

        assert view
        assert view.testView
        assert view.testView.map
        assert view.testView.map == allVeranstaltungArrayKey
    }

    @Test
    void testAddAttachmentFirst() {

        Map object = [:]
        CouchDbHelper.setAttachmentAtObject(object, 'test.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())
        assert object.toMapString() == '[_attachments:[test.txt:[content_type:application/octet-stream, data:dGVzdA==]]]'

    }

    @Test
    void testAddAttachmentSecond() {

        Map object = [:]
        CouchDbHelper.setAttachmentAtObject(object, 'test1.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())
        assert object.toMapString() == '[_attachments:[test1.txt:[content_type:application/octet-stream, data:dGVzdA==]]]'

        CouchDbHelper.setAttachmentAtObject(object, 'test2.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())

        println object
        assert object.toMapString() == '[_attachments:[test1.txt:[content_type:application/octet-stream, data:dGVzdA==], test2.txt:[content_type:application/octet-stream, data:dGVzdA==]]]'
    }

    @Test
    void testAddAttachmentUpdate() {

        Map object = [:]
        CouchDbHelper.setAttachmentAtObject(object, 'test1.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())
        assert object.toMapString() == '[_attachments:[test1.txt:[content_type:application/octet-stream, data:dGVzdA==]]]'

        CouchDbHelper.setAttachmentAtObject(object, 'test2.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())

        println object
        assert object.toMapString() == '[_attachments:[test1.txt:[content_type:application/octet-stream, data:dGVzdA==], test2.txt:[content_type:application/octet-stream, data:dGVzdA==]]]'

        CouchDbHelper.setAttachmentAtObject(object, 'test2.txt', ContentType.BINARY.toString(), 'testupdate'.bytes.encodeBase64().toString())

        println object
        assert object.toMapString() == '[_attachments:[test1.txt:[content_type:application/octet-stream, data:dGVzdA==], test2.txt:[content_type:application/octet-stream, data:dGVzdHVwZGF0ZQ==]]]'
    }
}
