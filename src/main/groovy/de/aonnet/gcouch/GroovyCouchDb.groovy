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

import groovy.json.JsonBuilder
import groovy.util.logging.Commons
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON

@Commons
class GroovyCouchDb {

    String host
    int port = 5984
    String dbName

    private RESTClient couchDbClient

    Map<String, String> create(def object) {
        def data = postIntern("${dbName}", object)
        return [id: data.id, rev: data.rev]
    }

    def read(String id) {
        def data = getIntern("${dbName}/${id}")
        return data
    }

    Map<String, String> update(String uuid, def object) {
        def data = putIntern("${dbName}/${uuid}", object)
        return [id: data.id, rev: data.rev]
    }

    void delete(String id, String rev) {
        deleteIntern("${dbName}/${id}", [rev: rev])
    }

    Map luceneSearch(String searchName, Map<String, String> searchOptions) {

        Map data = getIntern("_fti/local/${dbName}/_design/lucene/${searchName}", searchOptions)
        return data
    }

    Map view(String designDoc, String viewName) {
        Map data = getIntern("${dbName}/_design/${designDoc}/_view/${viewName}")
        return data
    }

    Map viewWithJsonKey(String designDoc, String viewName, Map<String, Object> key) {

        String jsonKey = createJsonKey(key)
        log.debug "call view with key: $jsonKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [key: jsonKey])
        return data
    }

    Map viewWithJsonStartKey(String designDoc, String viewName, Map<String, Object> startKey) {

        String jsonStartKey = createJsonKey(startKey)
        log.debug "call view with json startkey: $jsonStartKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [startkey: jsonStartKey])
        return data
    }

    Map viewWithJsonEndKey(String designDoc, String viewName, Map<String, Object> endKey) {

        String jsonEndKey = createJsonKey(endKey)
        log.debug "call view with json endkey: $jsonEndKey"

        Map data = viewWithJsonKeys(designDoc, viewName, [endkey: jsonEndKey])
        return data
    }

    Map viewWithJsonStartAndEndKey(String designDoc, String viewName, Map<String, Object> startKey, Map<String, Object> endKey) {

        String jsonStartKey = createJsonKey(startKey)
        String jsonEndKey = createJsonKey(endKey)
        log.debug "call view with json startkey and endkey: $jsonStartKey $jsonEndKey"

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
        } catch (HttpResponseException e) {

            // check exception: db not exists error
            if (e.response.data.error == 'not_found' && e.response.data.reason == 'no_db_file') {
                return false
            }

            // other exception
            log.error "path: $path response: ${e.response.data}", e
            throw e
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
        } catch (HttpResponseException e) {

            log.error "path: $path response: ${e.response.data}", e
            throw e
        }
    }

    private Object putIntern(String path, def body) {
        try {

            log.debug ">> PUT path: ${path} body: ${body}"

            def response = getCouchDbClient().put(path: path, contentType: JSON, requestContentType: JSON, body: body)
            assert response.status == 201
            assert response.data.ok: 'Es ist ein Fehler aufgetreten!'

            log.debug "<< PUT path: ${path} body: ${body} response: ${response.data}"
            return response.data
        } catch (HttpResponseException e) {

            log.error "path: $path body: $body response: ${e.response.data}", e
            throw e
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
        } catch (HttpResponseException e) {

            log.error "path: $path body: $body response: ${e.response.data}", e
            throw e
        }
    }

    private Object getIntern(String path) {
        getIntern(path, true)
    }

    private Object getIntern(String path, boolean errorLog) {
        try {

            log.debug ">> GET path: ${path}"

            def response = getCouchDbClient().get(path: path, contentType: JSON, requestContentType: JSON)
            assert response.status == 200

            log.debug "<< GET path: ${path} response: ${response.data}"
            return response.data
        } catch (HttpResponseException e) {

            if (errorLog) {
                log.error "path: $path response: ${e.response.data}", e
            }
            throw e
        }
    }

    private Object getIntern(String path, def query) {
        try {

            log.debug ">> GET path: ${path} query: ${query}"

            def response = getCouchDbClient().get(path: path, query: query, contentType: JSON, requestContentType: JSON)
            assert response.status == 200

            log.debug "<< GET path: ${path} query: ${query} response: ${response.data}"
            return response.data
        } catch (HttpResponseException e) {

            log.error "path: $path response: ${e.response.data}", e
            throw e
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
        } catch (HttpResponseException e) {

            log.error "path: $path response: ${e.response.data}", e
            throw e
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
        } catch (HttpResponseException e) {

            log.error "path: $path response: ${e.response.data}", e
            throw e
        }
    }

    private RESTClient getCouchDbClient() {
        if (couchDbClient == null) {
            couchDbClient = new RESTClient("http://${host}:${port}")
        }
        return couchDbClient
    }
}
