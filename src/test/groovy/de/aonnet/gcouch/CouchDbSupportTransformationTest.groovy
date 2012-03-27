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

import org.junit.BeforeClass
import org.junit.Test

class CouchDbSupportTransformationTest {

    private final static String JSON_STRING = '{"de.aonnet.gcouch.Book":{"title":"Die Entwicklung des Berliner Flaschenbiergeschaefts","author":{"name":"Gustav Stresemann"}}}'
    private final static Map JSON_MAP = ["de.aonnet.gcouch.Book": ["title": "Die Entwicklung des Berliner Flaschenbiergeschaefts", "author": ["name": "Gustav Stresemann"]]]
    private final static Map MAP_FOR_CREATE = [title: "Die Entwicklung des Berliner Flaschenbiergeschaefts", author: new Author(name: "Gustav Stresemann", nickName: "Gusti")]

    @BeforeClass
    static void initCouchDbBeanSupport() {
        CouchDbBeanSupport.groovyCouchDb = new GroovyCouchDb(host: 'localhost', dbName: 'gcouchtest')
        CouchDbBeanSupport.groovyCouchDb.cleanDb()
    }

    @Test
    public void testBookMethods() {
        Book bean = new Book(MAP_FOR_CREATE)

        //assert bean.metaClass.properties.find { it.name == 'id'} != null
        assert bean.metaClass.methods.find { it.name == 'create'} != null
        assert bean.metaClass.methods.find { it.name == 'read'} != null
    }

    @Test
    void testCreate() {

        Book book = new Book(MAP_FOR_CREATE)
        assert book.id == null
        assert book.rev == null

        Book createdBook = book.create()
        assert createdBook.id
        assert createdBook.rev
        assert book.id
        assert book.rev
        assert book.is(createdBook)
    }

    @Test
    void testCreateAndRead() {

        Book book = new Book(MAP_FOR_CREATE)
        assert book.id == null
        assert book.rev == null

        Book createdBook = book.create()
        assert createdBook.id
        assert createdBook.rev
        assert book.id
        assert book.rev
        assert book.is(createdBook)

        Book readBook = Book.read(createdBook.id)
        assert readBook.id
        assert readBook.rev
        assert !createdBook.is(readBook)
        assert createdBook.id == readBook.id
        assert createdBook.rev == readBook.rev

        assert createdBook.properties == readBook.properties
    }
}
