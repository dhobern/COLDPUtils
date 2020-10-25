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
public enum NameRelationTypeEnum {

    spelling_correction("spelling correction", "is spelling correction for", "has spelling correction"),
    basionym("basionym", "has basionym", "is basionym for"),
    based_on("based on", "is based on", "is established by"),
    replacement_name("replacement name", "replaces", "is replaced by"),
    conserved("conserved", "is conserved against", "is rejected for"),
    later_homonym("later homonym", "is later homonym of", "has later homonym"),
    superfluous("superfluous", "is superfluous replacement for", "has superfluous replacement"),
    homotypic("homotypic", "is homotypic with", "is homotypic with"),
    type("type", "is type for", "has type");
    
    private static final Logger LOG = LoggerFactory.getLogger(NameRelationTypeEnum.class);
     
    private String _type;
    private String labelFromName;
    private String labelFromRelatedName;
    
    private NameRelationTypeEnum() {
    }
    
    NameRelationTypeEnum(String _type, String labelFromName, String labelFromRelatedName) {
        this._type = _type;
        this.labelFromName = labelFromName;
        this.labelFromRelatedName = labelFromRelatedName;
    }

    public String getStatus() {
        return _type;
    }

    public String getLabelFromName() {
        return labelFromName;
    }

    public String getLabelFromRelatedName() {
        return labelFromRelatedName;
    }
    
    public String toString() {
        return _type;
    }
    
    public static String getLabel(String s, boolean fromRelatedName, boolean uppercaseFirst) {
        if (s != null) {
            try {
                NameRelationTypeEnum v 
                    = Enum.valueOf(NameRelationTypeEnum.class, s.replace(" ", "_"));
                s = (fromRelatedName ? v.getLabelFromRelatedName() : v.getLabelFromName());
            } catch (Exception e) {
                LOG.error("Invalid NameRelation type: " + s);
            }
            if(uppercaseFirst) {
                s = StringUtils.upperFirst(s);
            }
        }
        return s;
    }
}