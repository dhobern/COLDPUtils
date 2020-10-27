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

/**
 *
 * @author dhobern@gmail.com
 */
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"match",
"parentOrAcceptedName",
"usageId",
"rank",
"status",
"nomCode",
"score",
"suggestion"
})
public class Suggestion {

@JsonProperty("match")
private String match;
@JsonProperty("parentOrAcceptedName")
private String parentOrAcceptedName;
@JsonProperty("usageId")
private String usageId;
@JsonProperty("rank")
private String rank;
@JsonProperty("status")
private String status;
@JsonProperty("nomCode")
private String nomCode;
@JsonProperty("score")
private Double score;
@JsonProperty("suggestion")
private String suggestion;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("match")
public String getMatch() {
return match;
}

@JsonProperty("match")
public void setMatch(String match) {
this.match = match;
}

@JsonProperty("parentOrAcceptedName")
public String getParentOrAcceptedName() {
return parentOrAcceptedName;
}

@JsonProperty("parentOrAcceptedName")
public void setParentOrAcceptedName(String parentOrAcceptedName) {
this.parentOrAcceptedName = parentOrAcceptedName;
}

@JsonProperty("usageId")
public String getUsageId() {
return usageId;
}

@JsonProperty("usageId")
public void setUsageId(String usageId) {
this.usageId = usageId;
}

@JsonProperty("rank")
public String getRank() {
return rank;
}

@JsonProperty("rank")
public void setRank(String rank) {
this.rank = rank;
}

@JsonProperty("status")
public String getStatus() {
return status;
}

@JsonProperty("status")
public void setStatus(String status) {
this.status = status;
}

@JsonProperty("nomCode")
public String getNomCode() {
return nomCode;
}

@JsonProperty("nomCode")
public void setNomCode(String nomCode) {
this.nomCode = nomCode;
}

@JsonProperty("score")
public Double getScore() {
return score;
}

@JsonProperty("score")
public void setScore(Double score) {
this.score = score;
}

@JsonProperty("suggestion")
public String getSuggestion() {
return suggestion;
}

@JsonProperty("suggestion")
public void setSuggestion(String suggestion) {
this.suggestion = suggestion;
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