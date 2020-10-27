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
"offset",
"limit",
"total",
"result",
"empty",
"last"
})
public class NameUsageSearchResponse {

@JsonProperty("offset")
private Integer offset;
@JsonProperty("limit")
private Integer limit;
@JsonProperty("total")
private Integer total;
@JsonProperty("result")
private List<NameUsageSearchResult> result = null;
@JsonProperty("empty")
private Boolean empty;
@JsonProperty("last")
private Boolean last;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("offset")
public Integer getOffset() {
return offset;
}

@JsonProperty("offset")
public void setOffset(Integer offset) {
this.offset = offset;
}

@JsonProperty("limit")
public Integer getLimit() {
return limit;
}

@JsonProperty("limit")
public void setLimit(Integer limit) {
this.limit = limit;
}

@JsonProperty("total")
public Integer getTotal() {
return total;
}

@JsonProperty("total")
public void setTotal(Integer total) {
this.total = total;
}

@JsonProperty("result")
public List<NameUsageSearchResult> getResult() {
return result;
}

@JsonProperty("result")
public void setResult(List<NameUsageSearchResult> result) {
this.result = result;
}

@JsonProperty("empty")
public Boolean getEmpty() {
return empty;
}

@JsonProperty("empty")
public void setEmpty(Boolean empty) {
this.empty = empty;
}

@JsonProperty("last")
public Boolean getLast() {
return last;
}

@JsonProperty("last")
public void setLast(Boolean last) {
this.last = last;
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
