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

import de.aonnet.lucene.LuceneHelper
import groovy.json.JsonBuilder
import groovy.util.logging.Commons
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.conn.HttpHostConnectException

import static groovyx.net.http.ContentType.JSON

@Commons
class GroovyCouchDb {

    String host
    int port = 5984
    String dbName

    private RESTClient couchDbClient

    Map<String, String> create(def object) {

        checkStorageObject(object)

        def data = postIntern("${dbName}", object)
        return [id: data.id, rev: data.rev]
    }

    def read(String id) {

        if (id == null || id.trim().isEmpty()) {
            String message = "cannot read with id $id"
            throw new DataRetrievalFailureException(message, new IllegalArgumentException(message))
        }

        def data = getIntern("${dbName}/${id}")
        return data
    }

    Map<String, String> update(String version, def object) {

        if (version == null || version.trim().isEmpty()) {
            String message = "cannot update with version $version"
            throw new DataStorageFailureException(message, new IllegalArgumentException(message))
        }

        checkStorageObject(object)

        def data = putIntern("${dbName}/${version}", object)
        return [id: data.id, rev: data.rev]
    }

    void delete(String id, String version) {

        if (id == null || id.trim().isEmpty()) {
            String message = "cannot delete with id $id"
            throw new DataRetrievalFailureException(message, new IllegalArgumentException(message))
        }

        if (version == null || version.trim().isEmpty()) {
            String message = "cannot delete with version $version"
            throw new DataRetrievalFailureException(message, new IllegalArgumentException(message))
        }

        deleteIntern("${dbName}/${id}", [rev: version])
    }

    Map luceneSearchByQuery(String searchName, Map<String, String> query) {

        Map data = luceneSearchByQuery searchName, query, [:]
        return data
    }

    Map luceneSearchByQuery(String searchName, Map<String, String> query, Map<String, String> searchOptions) {

        String queryString = LuceneHelper.createQueryWithAnd(query)
        log.debug "call lucene search $searchName with query: $queryString"

        searchOptions << [q: queryString]
        log.debug "call lucene search $searchName with searchOptions: $searchOptions"

        Map data = luceneSearch searchName, searchOptions
        return data
    }

    Map luceneSearch(String searchName, Map<String, String> searchOptions) {

        log.debug "call lucene search $searchName with options: $searchOptions"

        String path = couchDbVersion() < '1.1' ? "${dbName}/_fti" : "_fti/local/${dbName}"
        Map data = getIntern("${path}/_design/lucene/${searchName}", searchOptions)
        return data
    }

    Map view(String designDoc, String viewName) {

        log.debug "call view $viewName"

        Map data = view(designDoc, viewName, [:])
        return data
    }

    Map view(String designDoc, String viewName, Map<String, String> viewOptions) {

        log.debug "call view $viewName with options: $viewOptions"

        Map data = viewWithJsonKeys(designDoc, viewName, viewOptions)
        return data
    }

    Map viewWithJsonKey(String designDoc, String viewName, Map<String, Object> key) {

        String jsonKey = createJsonKey(key)
        log.debug "call view $viewName with key: $jsonKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [key: jsonKey])
        return data
    }

    Map viewWithJsonStartKey(String designDoc, String viewName, Map<String, Object> startKey) {

        String jsonStartKey = createJsonKey(startKey)
        log.debug "call view $viewName with json startkey: $jsonStartKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [startkey: jsonStartKey])
        return data
    }

    Map viewWithJsonEndKey(String designDoc, String viewName, Map<String, Object> endKey) {

        String jsonEndKey = createJsonKey(endKey)
        log.debug "call view $viewName with json endkey: $jsonEndKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [endkey: jsonEndKey])
        return data
    }

    Map viewWithJsonStartAndEndKey(String designDoc, String viewName, Map<String, Object> startKey, Map<String, Object> endKey) {

        String jsonStartKey = createJsonKey(startKey)
        String jsonEndKey = createJsonKey(endKey)
        log.debug "call view $viewName with json startkey and endkey: $jsonStartKey $jsonEndKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [startkey: jsonStartKey, endkey: jsonEndKey])
        return data
    }

    String couchDbVersion() {
        def data = getIntern('')
        return data.version
    }

    void createDb() {
        putIntern(dbName)
    }

    void dropDb() {
        deleteIntern(dbName)
    }

    void cleanDb() {
        if (existsDb()) {
            dropDb()
        }
        createDb()
    }

    boolean existsDb() {
        try {

            // call without error logging
            def data = getIntern(dbName, false)

            assert data.db_name == dbName
            return true
        } catch (DbNotFoundException e) {

            return false
        }
    }

    void putViewsIntoCouchDb(String viewId, Map<String, Map<String, String>> views) {

        views.each { String viewName, Map<String, String> view ->
            log.debug ">> insert view '${viewName}' into couch: ${view}"
        }

        String fullViewId = "_design/${viewId}"
        putIntern("${dbName}/${fullViewId}", [_id: fullViewId, language: 'javascript', views: views])
    }

    void putLuceneFulltextSearchIntoCouchDb(Map<String, Map<String, String>> fulltextSearchFunctions) {

        fulltextSearchFunctions.each { String functionName, Map<String, String> function ->
            log.debug ">> insert fulltext search function '${functionName}' into couch: ${function}"
        }

        putIntern("${dbName}/_design/lucene", [_id: '_design/lucene', language: 'javascript', fulltext: fulltextSearchFunctions])
    }

    private void checkStorageObject(object) {
        if (object == null || (object instanceof String && object.trim().isEmpty()) || (object instanceof Map && object.size() == 0)) {
            String message = "cannot update with object $object"
            throw new DataStorageFailureException(message, new IllegalArgumentException(message))
        }
    }

    private Map viewWithJsonKeys(String designDoc, String viewName, Map<String, Map<String, Object>> keys) {

        log.debug "call view with json keys: $keys"

        Map data = getIntern("${dbName}/_design/${designDoc}/_view/${viewName}", keys)
        return data
    }

    private String createJsonKey(Map<String, Object> key) {

        JsonBuilder builder = new JsonBuilder()
        builder.call(key)
        String jsonKey = builder.toString()

        log.debug "create json key: $jsonKey"
        return jsonKey
    }

    private String createUuid() {
        def response = getIntern('_uuids')
        assert response.data.uuids.size() == 1
        return response.data.uuids.get(0)
    }

    private Object putIntern(String path) {
        try {

            log.debug ">> PUT path: ${path}"

            def response = getCouchDbClient().put(path: path, contentType: JSON, requestContentType: JSON)
            assert response.status == 201
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< PUT path: ${path} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, true)
        }
    }

    private Object putIntern(String path, def body) {
        return putIntern(path, body, JSON)
    }

    private Object putIntern(String path, def body, def contentType) {
        try {

            log.debug ">> PUT path: ${path} body: ${body} contentType: ${contentType}"

            def response = getCouchDbClient().put(path: path, contentType: JSON, requestContentType: JSON, body: body)
            assert response.status == 201
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< PUT path: ${path} body: ${body} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, null, body, true)
        }
    }

    private Object postIntern(String path, def body) {
        try {

            log.debug ">> POST path: ${path} body: ${body}"

            def response = getCouchDbClient().post(path: path, contentType: JSON, requestContentType: JSON, body: body)
            assert response.status == 201
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< POST path: ${path} body: ${body} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, null, body, true)
        }
    }

    private Object getIntern(String path) {
        getIntern(path, true)
    }

    private Object getIntern(String path, boolean logError) {
        try {

            log.debug ">> GET path: ${path}"

            def response = getCouchDbClient().get(path: path, contentType: JSON, requestContentType: JSON)
            assert response.status == 200

            log.debug "<< GET path: ${path} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, logError)
        }
    }

    private Object getIntern(String path, def query) {
        try {

            log.debug ">> GET path: ${path} query: ${query}"

            def response = getCouchDbClient().get(path: path, query: query, contentType: JSON, requestContentType: JSON)
            assert response.status == 200

            log.debug "<< GET path: ${path} query: ${query} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, true)
        }
    }

    private Object deleteIntern(String path) {
        try {

            log.debug ">> DELETE path: ${path}"

            def response = getCouchDbClient().delete(path: path, contentType: JSON, requestContentType: JSON)
            assert response.status == 200
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< DELETE path: ${path} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, true)
        }
    }

    private Object deleteIntern(String path, def query) {
        try {

            log.debug ">> DELETE path: ${path} query: ${query}"

            def response = getCouchDbClient().delete(path: path, query: query, contentType: JSON, requestContentType: JSON)
            assert response.status == 200
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< DELETE path: ${path} query: ${query} response: ${response.data}"
            return response.data
        } catch (Exception e) {

            throw logAndConvertException(e, path, query, true)
        }
    }

    private Exception logAndConvertException(Exception e, String path, boolean logError) {
        return logAndConvertException(e, path, null, null, logError)
    }

    private Exception logAndConvertException(Exception e, String path, def query, boolean logError) {
        return logAndConvertException(e, path, query, null, logError)
    }

    private Exception logAndConvertException(Exception e, String path, def query, def body, boolean logError) {

        String message = 'CouchDb access faild.'
        if (path) {
            " path: $path"
        }
        if (query) {
            message += " query: $query"
        }

        Exception convertedException

        if (e instanceof UnknownHostException || e instanceof HttpHostConnectException) {

            message += " host: ${host}"
            convertedException = new DataAccessResourceFailureException(message, e)
        } else if (e instanceof HttpResponseException) {

            message += " response: ${e.response.data} status code: ${e.statusCode}"

            switch (e.statusCode) {
                case 404:
                    // check exception: db not exists error
                    if (e.response?.data?.error == 'not_found' && e.response?.data?.reason == 'no_db_file') {
                        convertedException = new DbNotFoundException(message, e)
                    } else {
                        convertedException = new DataRetrievalFailureException(message, e)
                    }
                    break;
                default:
                    convertedException = new DataAccessException(message, e)
                    break;
            }
        } else {

            convertedException = new DataAccessException(message, e)
        }

        if (logError) {
            log.error message, convertedException
        }

        return convertedException
    }

    private RESTClient getCouchDbClient() {
        if (couchDbClient == null) {
            couchDbClient = new RESTClient("http://${host}:${port}")
        }
        return couchDbClient
    }
}
