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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GroovyCouchDbTest {

    private final static String HOST = "localhost"

    private final static String TEST_DB = "unittest"

    private final static Map MAP_FOR_CREATE = [title: "Die Entwicklung des Berliner Flaschenbiergeschaefts", author: new Author(name: "Gustav Stresemann", nickName: "Gusti")]

    private final static String DESIGN_DOC_ALL = 'all'

    @Test
    void testConnectionFailWrongHost() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: 'wronghost', dbName: TEST_DB)

        try {
            couchDb.couchDbVersion()
            Assert.fail 'DataAccessResourceFailureException wurde nicht ausgelöst'
        } catch (DataAccessResourceFailureException e) {
            assert e
        }
    }

    @Test
    void testConnectionFailWrongPort() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, port: 9999, dbName: TEST_DB)

        try {
            couchDb.couchDbVersion()
            Assert.fail 'DataAccessResourceFailureException wurde nicht ausgelöst'
        } catch (DataAccessResourceFailureException e) {
            assert e
        }
    }

    @Test
    void testConnectionFailWrongDb() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: 'wronghost', dbName: TEST_DB)

        try {
            couchDb.couchDbVersion()
            Assert.fail 'DataAccessResourceFailureException wurde nicht ausgelöst'
        } catch (DataAccessResourceFailureException e) {
            assert e
        }
    }

    @Test
    void testExistsDb() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: 'wrongdb')

        assert !couchDb.existsDb()
    }

    @Test
    void testCreateAndDestroyDb() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)

        couchDb.createDb()
        couchDb.dropDb()
    }

    @Test
    void testCleanDb() {

        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)

        couchDb.cleanDb()
    }

    @Test
    void testCreateDataIntoCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map result = couchDb.create([
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ])

        assert result
        assert result.id
        assert result.rev

        println couchDb.read(result.id)
    }

    @Test
    void testCreateDataIntoCouchDbWithAttachment() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map object = [
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ]

        CouchDbHelper.setAttachmentAtObject(object, 'test.txt', ContentType.BINARY.toString(), 'test'.bytes.encodeBase64().toString())
        println object
        Map result = couchDb.create(object)

        assert result
        assert result.id
        assert result.rev

        println couchDb.read(result.id)
    }

    @Test
    void testCreateDataWithNull() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        try {
            couchDb.create()
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.create(null)
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.create('  ')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.create([:])
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }
    }

    @Test
    void testReadDataWithNull() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        try {
            couchDb.read()
            Assert.fail 'DataRetrievalFailureException wurde nicht ausgelöst'
        } catch (DataRetrievalFailureException e) {
            assert e
        }

        try {
            couchDb.read('   ')
            Assert.fail 'DataRetrievalFailureException wurde nicht ausgelöst'
        } catch (DataRetrievalFailureException e) {
            assert e
        }

        try {
            couchDb.read(null)
            Assert.fail 'DataRetrievalFailureException wurde nicht ausgelöst'
        } catch (DataRetrievalFailureException e) {
            assert e
        }
    }

    @Test
    void testExistsDataReturnsFalse() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        assert !couchDb.exists('bla')
    }

    @Test
    void testExistsDataReturnsTrue() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map result = couchDb.create([
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ])

        assert result
        assert result.id
        assert result.rev

        assert couchDb.exists(result.id)
    }

    @Test
    void testDeleteDataFromCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map result = couchDb.create([
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ])

        assert result
        assert result.id
        assert result.rev

        couchDb.delete(result.id, result.rev)
    }

    @Test
    void testDeleteDataFromCouchDbWithoutRev() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map result = couchDb.create([
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ])

        assert result
        assert result.id
        assert result.rev

        Map result2 = couchDb.delete(result.id)
        assert result2
        assert result2._id
        assert result2._rev
        assert result.id == result2._id
        assert result.rev == result2._rev
    }

    @Test
    void testDeleteNotExistingDataFromCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map result = couchDb.delete("hello")
        assert result == null
    }

    @Test
    void testUpdateDataWithEmptyVersion() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        try {
            couchDb.update(null, '1')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.update('', '1')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.update(' ', '1')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }
    }

    @Test
    void testUpdateDataWithEmptyObject() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        try {
            couchDb.update('1', null)
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.update('1', '')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.update('1', '  ')
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }

        try {
            couchDb.update('1', [:])
            Assert.fail 'DataStorageFailureException wurde nicht ausgelöst'
        } catch (DataStorageFailureException e) {
            assert e
        }
    }

    @Test
    void testReadDataCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Map data = [
                titel: "Groovy und jcouchdb",
                text: "Maps mit Groovy und jcouchdb in die CouchDb speichern...",
                kommentare: [
                        kommentar1: [email: "thomas.westphal@adesso.de", text: "Reaktion 1"],
                        kommentar2: [email: "thomas.westphal@adesso.de", text: "Reaktion 2"]
                ]
        ]
        Map result = couchDb.create(data)

        assert result
        assert result.id
        assert result.rev

        Map readData = couchDb.read(result.id)

        assert readData
        assert result.id == readData._id
        assert result.rev == readData._rev
        assert data.titel == readData.titel
        assert data.text == readData.text
        assert data.kommentare == readData.kommentare
    }

    @Test
    void testPutJavaObjectsIntoCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        Book book = new Book(MAP_FOR_CREATE)

        couchDb.create(book)
    }

    @Test
    void testPutViewsIntoCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        def viewId = 'testJoin'
        def viewFunctionWithoutJoin = """
// Testview
function(doc) {
    emit(doc._id, doc);
}"""
        def views = [
                viewOhneJoin: [map: viewFunctionWithoutJoin],
                testView2: [map: viewFunctionWithoutJoin]
        ]
        couchDb.putViewsIntoCouchDb viewId, views
    }

    @Test
    void testPutViewsIntoCouchDbFails() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        def viewId = 'testJoin'
        def viewFunctionWithoutJoin = """
// Testview
function(doc) {
    emit(doc._id, doc);
}"""
        def views = [
                viewOhneJoin: [map: viewFunctionWithoutJoin],
                testView2: [map: viewFunctionWithoutJoin]
        ]
        couchDb.putViewsIntoCouchDb viewId, views

        try {
            couchDb.putViewsIntoCouchDb viewId, views
            Assert.fail 'DataAccessException wurde nicht ausgelöst'
        } catch (DataAccessException e) {
            assert e
        }
    }

    @Test
    void testUpdateViews() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        def viewId = 'testJoin'
        def viewFunctionWithoutJoin = """
// Testview
function(doc) {
    emit(doc._id, doc);
}"""
        def views = [
                viewOhneJoin: [map: viewFunctionWithoutJoin],
                testView2: [map: viewFunctionWithoutJoin]
        ]
        couchDb.putViewsIntoCouchDb viewId, views
        couchDb.updateViewsIntoCouchDb viewId, views
    }

    @Test
    void testUpdateNonExistingViews() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        def viewId = 'testJoin'
        def viewFunctionWithoutJoin = """
// Testview
function(doc) {
    emit(doc._id, doc);
}"""
        def views = [
                viewOhneJoin: [map: viewFunctionWithoutJoin],
                testView2: [map: viewFunctionWithoutJoin]
        ]
        couchDb.updateViewsIntoCouchDb viewId, views
    }

    @Test
    void testPutLuceneFulltextSearchIntoCouchDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        def veranstaltungByBeschreibung = """
// Testview
function(doc) {
    if(doc.type == "veranstaltung") {
        var ret = new Document();
        ret.add( doc.titel , {"store": "yes"} );
        ret.add( doc.ort.name , {"store": "yes"} );
        ret.add( doc.beschreibung );
        return ret;
    }
}
"""
        def fulltextSearchFunctions = [
                veranstaltung_by_beschreibung: [index: veranstaltungByBeschreibung],
                veranstaltung_by_beschreibung2: [index: veranstaltungByBeschreibung]
        ]
        couchDb.putLuceneFulltextSearchIntoCouchDb fulltextSearchFunctions
    }

    @Test
    void testCreateAndCallLuceneFulltextSearch() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()
        createData(couchDb)

        def veranstaltungByBeschreibung = """
// Testview
function(doc) {
    if(doc.type == "veranstaltung") {
        var ret = new Document();
        ret.add( doc.titel       , {"field":"titel"   , "store": "yes"} );
        ret.add( doc.ort.name    , {"field":"ort_name", "store": "yes"} );
        ret.add( doc.beschreibung );
        return ret;
    }
}
"""
        def fulltextSearchFunctions = [
                veranstaltung_by_beschreibung: [index: veranstaltungByBeschreibung]
        ]
        couchDb.putLuceneFulltextSearchIntoCouchDb fulltextSearchFunctions

        Map luceneSearchResult = couchDb.luceneSearch('veranstaltung_by_beschreibung', [q: 'Zum'])

        assert 25 == luceneSearchResult.limit
        assert 9 == luceneSearchResult.total_rows
        assert 0 == luceneSearchResult.skip
        assert 'default:zum' == luceneSearchResult.q
        luceneSearchResult.rows.each {
            assert it.id
            assert it.score
            assert ["academixer Keller", "Universität Leipzig, Seminargebäude Raum 420"].contains(it.fields.ort_name)
            assert ["In den Mühlen der Ebene: Unzeitgemäße Erinnerungen", "Ein bisschen unwirklich?"].contains(it.fields.titel)
        }
    }

    @Test
    void testCreateAndCallLuceneFulltextSearchWithQParameter() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()
        createData(couchDb)

        def veranstaltungByBeschreibung = """
// Testview
function(doc) {
    if(doc.type == "veranstaltung") {
        var ret = new Document();
        ret.add( doc.titel       , {"field":"titel"   , "store": "yes"} );
        ret.add( doc.ort.name    , {"field":"ort_name", "store": "yes"} );
        ret.add( doc.beschreibung );
        return ret;
    }
}
"""
        def fulltextSearchFunctions = [
                veranstaltung_by_beschreibung: [index: veranstaltungByBeschreibung]
        ]
        couchDb.putLuceneFulltextSearchIntoCouchDb fulltextSearchFunctions

        Map luceneSearchResult = couchDb.luceneSearch('veranstaltung_by_beschreibung', [q: 'titel:durch'])
        println luceneSearchResult

        assert 25 == luceneSearchResult.limit
        assert 3 == luceneSearchResult.total_rows
        assert 0 == luceneSearchResult.skip
        assert 'titel:durch' == luceneSearchResult.q
        luceneSearchResult.rows.each {
            assert it.id
            assert it.score
            assert "Deutsche Nationalbibliothek" == it.fields.ort_name
            assert "durch die Deutsche Nationalbibliothek" == it.fields.titel
        }
    }


    @Test
    void testCreateAndCallLuceneFulltextSearchByQuery() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()
        createData(couchDb)

        def veranstaltungByBeschreibung = """
// Testview
function(doc) {
    if(doc.type == "veranstaltung") {
        var ret = new Document();
        ret.add( doc.titel       , {"field":"titel"       , "store": "yes"} );
        ret.add( doc.ort.name    , {"field":"ort_name"    , "store": "yes"} );
        ret.add( doc.beschreibung, {"field":"beschreibung", "store": "yes"} );
        return ret;
    }
}
"""
        def fulltextSearchFunctions = [
                veranstaltung_by_beschreibung: [index: veranstaltungByBeschreibung]
        ]
        couchDb.putLuceneFulltextSearchIntoCouchDb fulltextSearchFunctions

        Map luceneSearchResult = couchDb.luceneSearchByQuery('veranstaltung_by_beschreibung', [
                ort_name: ['Nationalbibliothek', 'Universität Leipzig, Seminargebäude Raum 420'],
                beschreibung: ['durch die Deutsche Nationalbibliothek', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.']])

        assert 25 == luceneSearchResult.limit
        assert 6 == luceneSearchResult.total_rows
        assert 0 == luceneSearchResult.skip
        assert '+(ort_name:nationalbibliothek ort_name:"universität leipzig seminargebäude raum 420") +(beschreibung:"durch die deutsche nationalbibliothek" beschreibung:"zum postulat des" default:magischen default:realismus default:"bei daniel kehlmann")' == luceneSearchResult.q
        luceneSearchResult.rows.each {
            assert it.id
            assert it.score
            assert ['Universität Leipzig, Seminargebäude Raum 420'].contains(it.fields.ort_name)
            assert ['Ein bisschen unwirklich?'].contains(it.fields.titel)
            // today the query find both, with and without "
            assert ['Zum Postulat des Magischen Realismus bei Daniel Kehlmann.', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.'].contains(it.fields.beschreibung)
        }
    }


    @Test
    void testCreateAndCallLuceneFulltextSearchByQueryWithSkip() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()
        createData(couchDb)

        def veranstaltungByBeschreibung = """
// Testview
function(doc) {
    if(doc.type == "veranstaltung") {
        var ret = new Document();
        ret.add( doc.titel       , {"field":"titel"       , "store": "yes"} );
        ret.add( doc.ort.name    , {"field":"ort_name"    , "store": "yes"} );
        ret.add( doc.beschreibung, {"field":"beschreibung", "store": "yes"} );
        return ret;
    }
}
"""
        def fulltextSearchFunctions = [
                veranstaltung_by_beschreibung: [index: veranstaltungByBeschreibung]
        ]
        couchDb.putLuceneFulltextSearchIntoCouchDb fulltextSearchFunctions

        Map luceneSearchResult = couchDb.luceneSearchByQuery('veranstaltung_by_beschreibung', [
                ort_name: ['Nationalbibliothek', 'Universität Leipzig, Seminargebäude Raum 420'],
                beschreibung: ['durch die Deutsche Nationalbibliothek', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.']], [skip: 0, limit: 2])

        assert 2 == luceneSearchResult.limit
        assert 6 == luceneSearchResult.total_rows
        assert 2 == luceneSearchResult.rows.size()
        assert 0 == luceneSearchResult.skip

        luceneSearchResult = couchDb.luceneSearchByQuery('veranstaltung_by_beschreibung', [
                ort_name: ['Nationalbibliothek', 'Universität Leipzig, Seminargebäude Raum 420'],
                beschreibung: ['durch die Deutsche Nationalbibliothek', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.']], [skip: 2, limit: 2])

        assert 2 == luceneSearchResult.limit
        assert 6 == luceneSearchResult.total_rows
        assert 2 == luceneSearchResult.rows.size()
        assert 2 == luceneSearchResult.skip

        luceneSearchResult = couchDb.luceneSearchByQuery('veranstaltung_by_beschreibung', [
                ort_name: ['Nationalbibliothek', 'Universität Leipzig, Seminargebäude Raum 420'],
                beschreibung: ['durch die Deutsche Nationalbibliothek', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.']], [skip: 4, limit: 2])

        assert 2 == luceneSearchResult.limit
        assert 6 == luceneSearchResult.total_rows
        assert 2 == luceneSearchResult.rows.size()
        assert 4 == luceneSearchResult.skip
    }

    @Test
    void testCreateAndCallViewWithoutKeys() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        //println 'allOrt'
        Map viewResult = couchDb.view(DESIGN_DOC_ALL, 'allOrt')
        //printView viewResult
        assert 6 == viewResult.total_rows
        assert 0 == viewResult.offset
        viewResult.rows.each {
            assert it.id

            assert ["academixer Keller", "Deutsche Nationalbibliothek", "Universität Leipzig, Seminargebäude Raum 420"].contains(it.key.name)
        }

        //println 'allVeranstaltung'
        viewResult = couchDb.view(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey')
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 0 == viewResult.offset
    }

    @Test
    void testCreateAndCallViewWithPagination() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        Map viewResult = couchDb.view(DESIGN_DOC_ALL, 'allOrt', [limit: 2, skip: 0])
        printView viewResult
        assert 6 == viewResult.total_rows
        assert 0 == viewResult.offset
        assert 2 == viewResult.rows.size()

        viewResult = couchDb.view(DESIGN_DOC_ALL, 'allOrt', [limit: 2, skip: 2])
        printView viewResult
        assert 6 == viewResult.total_rows
        assert 2 == viewResult.offset
        assert 2 == viewResult.rows.size()

        viewResult = couchDb.view(DESIGN_DOC_ALL, 'allOrt', [limit: 2, skip: 4])
        printView viewResult
        assert 6 == viewResult.total_rows
        assert 4 == viewResult.offset
        assert 2 == viewResult.rows.size()
    }

    @Test
    void testCreateAndCallViewWithJsonKey() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        //println 'viewWithJsonKey'
        Map viewResult = couchDb.viewWithJsonKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00', titel: 'durch die Deutsche Nationalbibliothek'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 3 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "10:00"
            assert it.key.titel == "durch die Deutsche Nationalbibliothek"

            assert it.value.ortname == "Deutsche Nationalbibliothek"
            assert it.value.titel == "durch die Deutsche Nationalbibliothek"
            assert it.value.beschreibung == "Treffpunkt: Foyer im historischen Gebäude"
            assert it.value.veranstalter == "Deutsche Nationalbibliothek"
            assert it.value.veranstaltungsart == "Führung"
            assert it.value.eintritt == "freier Eintritt, ohne Voranmeldung"
        }
    }

    @Test
    void testCreateAndCallViewWithJsonStartKey() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        //println 'viewWithJsonStartKey'
        Map viewResult = couchDb.viewWithJsonStartKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 6 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "10:00" || it.key.uhrzeit_von == "20:00"
        }

        //println 'viewWithJsonStartKey2'
        viewResult = couchDb.viewWithJsonStartKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00', titel: 'f'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 12 == viewResult.offset
        assert 3 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "20:00"
            assert it.key.titel == "In den Mühlen der Ebene: Unzeitgemäße Erinnerungen"
        }
    }

    @Test
    void testCreateAndCallViewWithJsonStartAndEndKey() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        //println 'viewWithJsonStartAndEndKey'
        Map viewResult = couchDb.viewWithJsonStartAndEndKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00', titel: 'durch'], [datum: '2012-03-15', uhrzeit_von: '10:00', titel: 'durch die Deutsche Nationalbibliothek'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 3 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "10:00"
            assert it.key.titel == "durch die Deutsche Nationalbibliothek"
        }

        //println 'viewWithJsonStartAndEndKey2'
        viewResult = couchDb.viewWithJsonStartAndEndKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '09:30'], [datum: '2012-03-15', uhrzeit_von: '10:01'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 3 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "10:00"
            assert it.key.titel == "durch die Deutsche Nationalbibliothek"
        }

        //println 'viewWithJsonStartAndEndKey3'
        viewResult = couchDb.viewWithJsonStartAndEndKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00'], [datum: '2012-03-15', uhrzeit_von: '10:00'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 0 == viewResult.rows.size()

        //println 'viewWithJsonStartAndEndKey4'
        viewResult = couchDb.viewWithJsonStartAndEndKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '10:00'], [datum: '2012-03-15', uhrzeit_von: '23:00'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 9 == viewResult.offset
        assert 6 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15"
            assert it.key.uhrzeit_von == "10:00" || it.key.uhrzeit_von == "20:00"
        }
    }

    @Test
    void testCreateAndCallViewWithJsonEndKey() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        couchDb.cleanDb()

        createViews(couchDb)
        createData(couchDb)

        //println 'viewWithJsonEndKey'
        Map viewResult = couchDb.viewWithJsonEndKey(DESIGN_DOC_ALL, 'allVeranstaltungJsonKey', [datum: '2012-03-15', uhrzeit_von: '14:00'])
        //printView viewResult
        assert 15 == viewResult.total_rows
        assert 0 == viewResult.offset
        assert 12 == viewResult.rows.size()
        viewResult.rows.each {
            assert it.id

            assert it.key.datum == "2012-03-15" || it.key.datum == "2012-03-13"
            assert ["09:00", "10:00", "16:30"].contains(it.key.uhrzeit_von)
        }
    }

    private def printView = { Map viewResult ->
        println "${ viewResult.total_rows } Treffer, Offset: ${ viewResult.offset }:"
        viewResult.rows.each {
            println it
        }
    }

    private createData(GroovyCouchDb couchDb) {

        String typeOrt = 'ort'
        String typeVeranstaltung = 'veranstaltung'

        def create = { Map map ->
            map.remove('_id')
            map.remove('_rev')
            Map idAndVersion = couchDb.create(map)
            map.put '_id', idAndVersion.id
            map.put '_rev', idAndVersion.rev
        }

        def ort1 = [
                type: typeOrt,
                name: 'academixer Keller',
                beschreibung: 'Das Kabarett "academixer", schon zu DDR-Zeiten Ort von Satire und Humor, wurde 1966 als Studentenkabarett von Christian Becher, Gunter Böhnke, Jürgen Hart und Bernd-Lutz Lange gegründet und entwickelte es sich über die Jahre zu einer Leipziger Institution. In der seit 1980 ständigen Spielstätte in der Kupfergasse finden zur Leipziger Buchmesse schon lange Lesungen statt. Autoren wie etwa Sebastian Sick, haben hier schon ihre Werke präsentiert, in erster Linie natürlich Kabarettisten, die gleichzeitig Schriftsteller sind, wie die Österreicher Alfred Dorfer ("Indien" zusammen mit Josef Hader) und Werner Schneyder. "Aber auch bekannte Autoren jenseits des Genres sind uns willkommene Gäste", sagt Geschäftsführer Klaus Kitzing. Die Lesung mit dem französischen Bestseller- und Skandalautor Michel Houellebecq ("Elementarteilchen") etwa wurde ein großer Erfolg.',
                oeffentlicher_nahverkehr: 'Linie 2 / 8 / 9 / 10 / 11 bis Wilhelm-Leuschner-Platz',
                adresse: [
                        name: 'ACADEMIXER KELLER',
                        strasse: 'Kupfergasse 2',
                        plz: '04109',
                        ort: 'Leipzig',
                        telefon: '03 41/ 21 78 78 78',
                        fax: '03 41/ 21 78 77 00',
                        email: 'info@academixer.com',
                        internet: 'www.academixer.com'
                ]
        ]

        def ort2 = [
                type: typeOrt,
                name: 'Deutsche Nationalbibliothek',
                beschreibung: 'Das zwischen 1914 und 1916 errichtete Bibliotheksgebäude am Deutschen Platz in Leipzig wurde durch Erweiterungsbauten mehrfach ergänzt. Zuletzt entstand in den 1970er/1980er Jahren der funktionelle Bücherturm. Der nunmehr vierte Leipziger Erweiterungsbau wird im Mai 2011 eröffnet. Der Entwurf der Stuttgarter Architektin Gabriele Glöckler überzeugt mit seiner städtebaulichen, architektonischen und funktionellen Prägnanz. Mit einem Gesamtbestand von insgesamt ca. 26 Millionen Einheiten - davon allein rund 16 Millionen in Leipzig - verfügt die Deutsche Nationalbibliothek über den größten Bestand an Medienwerken in ganz Deutschland. Auf der Leipziger Buchmesse ist die Deutsche Nationalbibliothek seit 1992 mit einem eigenen Messestand vertreten, an "Leipzig liest" beteiligte sie sich fast von Anfang an mit Veranstaltungen. Erster Lesegast war Marcel Reich-Ranicki mit Alfed Döblins "Berlin Alexanderplatz". 1994 las Christa Wolf aus "Auf dem Weg nach Tabou". In den folgenden Jahren nahmen u. a. Armin Mueller-Stahl, Manfred Krug, Gabriele Krone-Schmalz, György Konrád, Lenka Reinerová, Tschingis Aitmatow, Christoph Hein, Wolf Biermann, Arno Lustiger, Władysław Szpilman, Volker Braun, Daniela Dahn, Angela Krauß, Thomas Rosenlöcher, Sigrid Damm, Günter Kunert, Pierre Merle und Adolph Muschg in dem prächtigen Lesesaal mit den grünen Tischlampen Platz. "Die Deutsche Nationalbibliothek ist der Ort, an dem alle diese schriftstellerischen Werke gesammelt und dauerhaft aufbewahrt werden. Wir sehen uns aber auch als einen aktiven Ort für Literatur. Hier ist sie hautnah erlebbar. Auch deshalb ist "Leipzig liest" eine Veranstaltungsreihe, in die wir uns sehr gern einbringen", sagt Annett Koschnick von der Deutschen Nationalbibliothek.',
                oeffentlicher_nahverkehr: 'Straßenbahn: 2, 16 bis Deutsche Nationalbibliothek',
                adresse: [
                        name: 'DEUTSCHE NATIONALBIBLIOTHEK',
                        strasse: 'Deutscher Platz 1',
                        plz: '04103',
                        ort: 'Leipzig',
                        telefon: '03 41 / 2 27 10',
                        email: 'veranstaltungen@dnb.de',
                        internet: 'www.dnb.de'
                ]
        ]

        def ort3 = [
                type: typeOrt,
                name: 'Universität Leipzig, Seminargebäude Raum 420',
                adresse: [
                        name: 'UNIVERSITÄT LEIPZIG, SEMINARGEBÄUDE RAUM 420',
                        strasse: 'Universitätsstraße 1',
                        plz: '04107',
                        ort: 'Leipzig',
                        internet: 'www.uni-leipzig.de'
                ]
        ]

        2.times {
            create ort1
            create ort2
            create ort3
        }

        def veranstaltung11 = [
                type: typeVeranstaltung,
                datum: '2012-03-15',
                uhrzeit_von: '20:00',
                veranstaltungsart: 'Lesung',
                mitwirkende: 'Dietmar Keller',
                titel: 'In den Mühlen der Ebene: Unzeitgemäße Erinnerungen',
                beschreibung: 'Dietmar Keller gehörte zu den profiliertesten DDR-Politikern, lange Zeit auch in Leipzig tätig. Dietmar Keller, Jahrgang 1942, gehörte zu den wenigen SED-Politikern, die ernsthaft eine Öffnung der DDR zur Demokratie verfolgten. Während des Krieges in einer proletarischen Familie geboren, erlebte er 1945 die Zerstörung seiner Heimatstadt Chemnitz, seine Kindheit war von den schwierigen Nachkriegsjahren geprägt. Nach dem Abitur, schon als Schüler arbeitete er als Sportreporter, meldete er sich zum Armeedienst. Da seine Immatrikulation für das Journalistikstudium in Leipzig suspendiert wurde, studierte er Geschichte und machte schon als junger Wissenschaftler mit erfolgreichen Publikationen zur Zeitgeschichte auf sich aufmerksam. Nach der Promotion wurde er in die SED-Kreisleitung der Karl-Marx-Universität Leipzig gewählt – von dort begann sein hauptamtlicher politischer Weg, der ihn bis zum Staatssekretär für Kultur und schließlich in der Modrow-Regierung zum Kulturminister führte. Dietmar Keller galt unter Künstlern und anderen Intellektuellen schon Ende der siebziger Jahre als Hoffnungsträger für eine demokratisierte DDR – was auch der Staatssicherheit nicht entging. In und nach der friedlichen Revolution zählte Keller in der Partei des Demokratischen Sozialismus zu den wenigen profilierten Politikern, die sich uneingeschränkt für den Bruch mit dem Stalinismus und einer radikalen Erneuerung der Partei einsetzten, letztlich aber scheitern mussten.',
                moderation: 'Jörn  Schütrumpf',
                veranstalter: 'Karl Dietz Verlag',
                ort: [id: ort1._id, name: ort1.name],
                reihe: ['Biographie', 'Politik', 'Sachbuch']
        ]
        def veranstaltung21 = [
                type: typeVeranstaltung,
                datum: '2012-03-15',
                uhrzeit_von: '10:00',
                uhrzeit_bis: '11:00',
                veranstaltungsart: 'Führung',
                titel: 'durch die Deutsche Nationalbibliothek',
                beschreibung: 'Treffpunkt: Foyer im historischen Gebäude',
                eintritt: 'freier Eintritt, ohne Voranmeldung',
                veranstalter: 'Deutsche Nationalbibliothek',
                ort: [id: ort2._id, name: ort2.name]
        ]

        def veranstaltung31 = [
                type: typeVeranstaltung,
                datum: '2012-03-13',
                uhrzeit_von: '09:00',
                uhrzeit_bis: '18:30',
                veranstaltungsart: 'Wissenschaftliche Konferenz',
                titel: 'Poetologien des deutschsprachigen Gegenwartromans',
                beschreibung: 'Wissenschaftliche Konferenz an der Universität Leipzig in Kooperation mit der Leipziger Buchmesse, finanziert von der Fritz-Thyssen-Stiftung.',
                veranstalter: 'Universität Leipzig, Institut für Germanistik, Leipziger Buchmesse',
                ort: [id: ort3._id, name: ort3.name],
                eintritt: 'Offen für Interessierte',
                reihe: ['Fachprogramm', 'Wissenschaftliche Konferenz']
        ]
        def veranstaltung32 = [
                type: typeVeranstaltung,
                datum: '2012-03-13',
                uhrzeit_von: '16:30',
                uhrzeit_bis: '16:50',
                veranstaltungsart: 'Wissenschaftliche Konferenz',
                mitwirkende: 'Leonhard Herrmann',
                titel: 'Ein bisschen unwirklich?',
                beschreibung: 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.',
                veranstalter: 'Universität Leipzig, Institut für Germanistik, Fritz-Thyssen-Stiftung, Leipziger Buchmesse',
                ort: [id: ort3._id, name: ort3.name],
                eintritt: 'Offen für Interessierte',
                reihe: ['Fachprogramm', 'Wissenschaftliche Konferenz']
        ]
        def veranstaltung33 = [
                type: typeVeranstaltung,
                datum: '2012-03-13',
                uhrzeit_von: '16:30',
                uhrzeit_bis: '16:50',
                veranstaltungsart: 'Wissenschaftliche Konferenz',
                mitwirkende: 'Leonhard Herrmann',
                titel: 'Ein bisschen unwirklich?',
                beschreibung: 'Zum Postulat des Magischen Realismus bei Daniel Kehlmann.',
                veranstalter: 'Universität Leipzig, Institut für Germanistik, Fritz-Thyssen-Stiftung, Leipziger Buchmesse',
                ort: [id: ort3._id, name: ort3.name],
                eintritt: 'Offen für Interessierte',
                reihe: ['Fachprogramm', 'Wissenschaftliche Konferenz']
        ]

        3.times {
            create veranstaltung11
            create veranstaltung21
            create veranstaltung31
            create veranstaltung32
            create veranstaltung33
        }
    }

    private createViews(GroovyCouchDb couchDb) {

        String typeOrt = 'ort'
        String typeVeranstaltung = 'veranstaltung'

        String allOrt = """
        // Alle Orte anzeigen
        function(doc) {
         if(doc.type == "$typeOrt") {
          emit( {"name": doc.name}, {"beschreibung": doc.beschreibung} );
         }
        }"""

        String allVeranstaltungJsonKey = """
        // Alle Veranstaltungen anzeigen
        function(doc) {
         if(doc.type == "$typeVeranstaltung") {
          emit( {"datum": doc.datum, "uhrzeit_von": doc.uhrzeit_von, "titel": doc.titel}, {"ortname": doc.ort.name, "titel": doc.titel, "beschreibung": doc.beschreibung, "veranstalter": doc.veranstalter, "veranstaltungsart": doc.veranstaltungsart, "eintritt": doc.eintritt, "reihe": doc.reihe});
         }
        }"""

        String allVeranstaltungArrayKey = """
        // Alle Veranstaltungen anzeigen
        function(doc) {
         if(doc.type == "$typeVeranstaltung") {
          emit( [doc.datum, doc.uhrzeit_von, doc.titel], {"ortname": doc.ort.name, "titel": doc.titel, "beschreibung": doc.beschreibung, "veranstalter": doc.veranstalter, "veranstaltungsart": doc.veranstaltungsart, "eintritt": doc.eintritt, "reihe": doc.reihe});
         }
        }"""

        def views = [
                'allOrt': [map: allOrt],
                'allVeranstaltungJsonKey': [map: allVeranstaltungJsonKey],
                'allVeranstaltungArrayKey': [map: allVeranstaltungArrayKey]
        ]
        couchDb.putViewsIntoCouchDb DESIGN_DOC_ALL, views
    }

    @Before
    void prepareDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: HOST, dbName: TEST_DB)
        if (couchDb.existsDb()) {
            couchDb.dropDb()
        }
    }
}
