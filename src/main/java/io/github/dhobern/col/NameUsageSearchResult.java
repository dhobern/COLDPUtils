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
package io.github.dhobern.col;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"classification",
"usage",
"sectorDatasetKey"
})
public class NameUsageSearchResult {

@JsonProperty("id")
private String id;
@JsonProperty("classification")
private List<Classification> classification = null;
@JsonProperty("usage")
private Usage usage;
@JsonProperty("sectorDatasetKey")
private Integer sectorDatasetKey;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("classification")
public List<Classification> getClassification() {
return classification;
}

@JsonProperty("classification")
public void setClassification(List<Classification> classification) {
this.classification = classification;
}

@JsonProperty("usage")
public Usage getUsage() {
return usage;
}

@JsonProperty("usage")
public void setUsage(Usage usage) {
this.usage = usage;
}

@JsonProperty("sectorDatasetKey")
public Integer getSectorDatasetKey() {
return sectorDatasetKey;
}

@JsonProperty("sectorDatasetKey")
public void setSectorDatasetKey(Integer sectorDatasetKey) {
this.sectorDatasetKey = sectorDatasetKey;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}