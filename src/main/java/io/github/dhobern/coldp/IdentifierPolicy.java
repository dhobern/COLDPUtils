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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhobern@gmail.com
 */
public class IdentifierPolicy {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPDistribution.class);

    public enum IdentifierType {
        Int, UUID, String;
    }
    
    private final EnumSet<IdentifierType> matchingTypes = EnumSet.of(IdentifierType.Int, IdentifierType.UUID);
    private IdentifierType selectedType = null;
    
    private final Set<String> usedIds = new HashSet<>();
    private int maxInt = 0;
    
    public IdentifierPolicy() {
    }
    
    public IdentifierPolicy(IdentifierType type) {
        selectedType = type;
    }
    
    public void processInstance(String id) {
        if (usedIds.contains(id)) {
            LOG.error("ID " + id + " used more than once");
        } else {
            usedIds.add(id);
        }
        
        if (selectedType == null) {
            if (matchingTypes.contains(IdentifierType.Int)) {
                if (matchesInt(id)) {
                    setMaxInt(id);
                } else {
                    matchingTypes.remove(IdentifierType.Int);
                }
            }
            if (matchingTypes.contains(IdentifierType.UUID) && !matchesUUID(id)) {
                matchingTypes.remove(IdentifierType.UUID);
            }
            if (matchingTypes.isEmpty()) {
                selectedType = IdentifierType.String;
                maxInt = 10000;
            }
        } else {
            switch(selectedType) {
                case Int: 
                    if (matchesInt(id)) { 
                    } else {
                        LOG.error("Supplied ID " + id + " does not match current policy (Int)");
                    }
                    break;
                case UUID: 
                    if (!matchesUUID(id)) { 
                        LOG.error("Supplied ID " + id + " does not match current policy (UUID)");
                    }
                    break;
            }
        }
    }
    
    public IdentifierType getType() {
        if (selectedType == null) {
            if (matchingTypes.contains(IdentifierType.Int)) {
                selectedType = IdentifierType.Int;
            } else if (matchingTypes.contains(IdentifierType.UUID)) {
                selectedType = IdentifierType.UUID;
            } else {
                selectedType = IdentifierType.String;
                maxInt = 10000;
            }
        }

        return selectedType;
    }
    
    public String nextIdentifier() {
        String id = null;
        
        do {
            id = switch (getType()) {
                case Int -> String.valueOf(++maxInt);
                case UUID -> UUID.randomUUID().toString();
                case String -> String.valueOf(++maxInt);
            };
        } while (usedIds.contains(id));
        
        return id;
    }

    private void setMaxInt(String id) {
        int idValue = Integer.parseInt(id);
        if (idValue > maxInt) {
            maxInt = idValue;
        }
    }
    
    private boolean matchesInt(String id) {
        return id.matches("^[0-9]+$");
    }
    
    private boolean matchesUUID(String id) {
        return id.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
    
    public String toString() {
        return "IdentifierPolicy { IdentifierType: " + ((selectedType == null) ? "null" : selectedType.name()) + " MaxInt: " + maxInt + " }";
    }
}
