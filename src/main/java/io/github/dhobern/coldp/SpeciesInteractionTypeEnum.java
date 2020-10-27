/*
 * Copyright 2020 dhobern@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhobern@gmail.com
 */
public enum SpeciesInteractionTypeEnum {

    adjacent_to("adjacent to"),
    co_occurs_with("co occurs with"),
    commensalist_of("commensalist of"),
    eaten_by("eaten by"),
    eats("eats"),
    ectoparasite_of("ectoparasite of"),
    endoparasite_of("endoparasite of"),
    epiphyte_of("epiphyte of"),
    flowers_visited_by("flowers visited by"),
    has_ectoparasite("has ectoparasite"),
    has_eggs_layed_on_by("has eggs layed on by"),
    has_endoparasite("has endoparasite"),
    has_epiphyte("has epiphyte"),
    has_host("has host"),
    has_hyperparasite("has hyperparasite"),
    has_hyperparasitoid("has hyperparasitoid"),
    has_kleptoparasite("has kleptoparasite"),
    has_parasite("has parasite"),
    has_parasitoid("has parasitoid"),
    has_pathogen("has pathogen"),
    has_vector("has vector"),
    host_of("host of"),
    hyperparasite_of("hyperparasite of"),
    hyperparasitoid_of("hyperparasitoid of"),
    interacts_with("interacts with"),
    killed_by("killed by"),
    kills("kills"),
    kleptoparasite_of("kleptoparasite of"),
    lays_eggs_on("lays eggs on"),
    mutualist_of("mutualist of"),
    parasite_of("parasite of"),
    parasitoid_of("parasitoid of"),
    pathogen_of("pathogen of"),
    pollinated_by("pollinated by"),
    pollinates("pollinates"),
    preyed_upon_by("preyed upon by"),
    preys_upon("preys upon"),
    related_to("related to"),
    symbiont_of("symbiont of"),
    vector_of("vector of"),
    visited_by("visited by"),
    visits_flowers_of("visits flowers of"),
    visits("visits");
    
    private static final Logger LOG = LoggerFactory.getLogger(SpeciesInteractionTypeEnum.class);
     
    private String _type;
    
    private SpeciesInteractionTypeEnum() {
    }
    
    SpeciesInteractionTypeEnum(String _type) {
        this._type = _type;
    }

    public String getType() {
        return _type;
    }

    public String toString() {
        return _type;
    }
}