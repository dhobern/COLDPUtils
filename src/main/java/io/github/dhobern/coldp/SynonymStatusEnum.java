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
public enum SynonymStatusEnum {
    
    accepted("accepted"), 
    provisionally_accepted("provisionally accepted"),
    synonym("synonym"),
    ambiguous_synonym("ambiguous synonym"),
    misapplied("misapplied");

    private String status;
    
    private SynonymStatusEnum() {
    }
    
    SynonymStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
    
    public String toString() {
        return status;
    }
}
