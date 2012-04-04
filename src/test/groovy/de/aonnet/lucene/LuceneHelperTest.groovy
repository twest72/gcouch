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

package de.aonnet.lucene

import org.junit.Test

class LuceneHelperTest {

    @Test
    void testCreateQueryWithAndSimple() {
        assert '(beschreibung:"durch die Deutsche Nationalbibliothek")' ==
                LuceneHelper.createQueryWithAnd([beschreibung: 'durch die Deutsche Nationalbibliothek'])
    }

    @Test
    void testCreateQueryWithAndWithTwoElements() {
        assert '(beschreibung:"durch die Deutsche Nationalbibliothek") AND (titel:"Mühlen der Ebene")' ==
                LuceneHelper.createQueryWithAnd([beschreibung: 'durch die Deutsche Nationalbibliothek', titel: 'Mühlen der Ebene'])
    }

    @Test
    void testCreateQueryWithAndWithOneList() {
        assert '(ort_name:"Nationalbibliothek" OR ort_name:"academixer")' ==
                LuceneHelper.createQueryWithAnd([ort_name: ['Nationalbibliothek', 'academixer']])
    }

    @Test
    void testCreateQueryWithAndWithTwoLists() {
        assert '(ort_name:"Nationalbibliothek" OR ort_name:"Universität Leipzig, Seminargebäude Raum 420") AND (beschreibung:"durch die Deutsche Nationalbibliothek" OR beschreibung:"Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.")' ==
                LuceneHelper.createQueryWithAnd([
                        ort_name: ['Nationalbibliothek', 'Universität Leipzig, Seminargebäude Raum 420'],
                        beschreibung: ['durch die Deutsche Nationalbibliothek', 'Zum Postulat des "Magischen Realismus" bei Daniel Kehlmann.']]
                )
    }
}
