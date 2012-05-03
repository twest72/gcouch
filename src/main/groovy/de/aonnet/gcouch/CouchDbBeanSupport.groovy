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

import de.aonnet.json.JsonConverter

@Singleton
class CouchDbBeanSupport {

    static GroovyCouchDb groovyCouchDb

    def create(def bean) {

        Map jsonBean = JsonConverter.toJsonMap(bean)
        Map idAndVersion = groovyCouchDb.create(jsonBean)
        bean.id = idAndVersion.id
        bean.rev = idAndVersion.rev

        return bean
    }

    def read(String id) {
        Map jsonBean = groovyCouchDb.read(id)

        assert jsonBean._id
        assert jsonBean._id == id
        assert jsonBean._rev

        String rev = jsonBean._rev

        jsonBean.remove '_id'
        jsonBean.remove '_rev'

        def bean = JsonConverter.newInstanceFromJsonMap(jsonBean)

        bean.id = id
        bean.rev = rev

        return bean
    }
}
