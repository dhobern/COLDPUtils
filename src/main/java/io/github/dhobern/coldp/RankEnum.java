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

/**
 *
 * @author dhobern@gmail.com
 */
public enum RankEnum {
    
    none(true, false, null),
    unknown(true, false, null),
    kingdom(true, false, null),
    phylum(true, false, null),
    clazz(true, false, null),
    order(true, false, null),
    superfamily(true, false, null),
    family(true, false, null),
    subfamily(true, false, null),
    tribe(true, false, null),
    genus(true, false, null),
    species(false, false, null),
    subspecies(false, true, null),
    variety(false, true, "var."),
    form(false, true, "f."),
    aberration(false, true, "ab.");
    
    private RankEnum() {
    }
    
    private boolean uninomial;
    private boolean infraspecific;
    private String infraspecificMarker;
    
    RankEnum(boolean uninomial, boolean infraspecific, String infraspecificMarker) {
        this.uninomial = uninomial;
        this.infraspecific = infraspecific;
        this.infraspecificMarker = infraspecificMarker;
    }

    public boolean isUninomial() {
        return uninomial;
    }

    public boolean isInfraspecific() {
        return infraspecific;
    }

    public String getInfraspecificMarker() {
        return infraspecificMarker;
    }
    
    public boolean inSpeciesGroup() {
        return this.ordinal() >= species.ordinal();
    }
    
    public boolean infraspecific() {
        return this.ordinal() > species.ordinal();
    }
    
    public String getRankName() {
        return (this == clazz ? "class" : name());
    }
}
