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
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"created",
"createdBy",
"modified",
"modifiedBy",
"datasetKey",
"id",
"sectorKey",
"verbatimKey",
"homotypicNameId",
"nameIndexId",
"nameIndexMatchType",
"scientificName",
"authorship",
"rank",
"genus",
"specificEpithet",
"combinationAuthorship",
"basionymAuthorship",
"code",
"publishedInId",
"origin",
"type",
"parsed"
})
public class Name {

@JsonProperty("created")
private String created;
@JsonProperty("createdBy")
private Integer createdBy;
@JsonProperty("modified")
private String modified;
@JsonProperty("modifiedBy")
private Integer modifiedBy;
@JsonProperty("datasetKey")
private Integer datasetKey;
@JsonProperty("id")
private String id;
@JsonProperty("sectorKey")
private Integer sectorKey;
@JsonProperty("verbatimKey")
private Integer verbatimKey;
@JsonProperty("homotypicNameId")
private String homotypicNameId;
@JsonProperty("nameIndexId")
private Integer nameIndexId;
@JsonProperty("nameIndexMatchType")
private String nameIndexMatchType;
@JsonProperty("scientificName")
private String scientificName;
@JsonProperty("authorship")
private String authorship;
@JsonProperty("rank")
private String rank;
@JsonProperty("genus")
private String genus;
@JsonProperty("specificEpithet")
private String specificEpithet;
@JsonProperty("combinationAuthorship")
private Authorship combinationAuthorship;
@JsonProperty("basionymAuthorship")
private Authorship basionymAuthorship;
@JsonProperty("code")
private String code;
@JsonProperty("publishedInId")
private String publishedInId;
@JsonProperty("origin")
private String origin;
@JsonProperty("type")
private String type;
@JsonProperty("parsed")
private Boolean parsed;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("created")
public String getCreated() {
return created;
}

@JsonProperty("created")
public void setCreated(String created) {
this.created = created;
}

@JsonProperty("createdBy")
public Integer getCreatedBy() {
return createdBy;
}

@JsonProperty("createdBy")
public void setCreatedBy(Integer createdBy) {
this.createdBy = createdBy;
}

@JsonProperty("modified")
public String getModified() {
return modified;
}

@JsonProperty("modified")
public void setModified(String modified) {
this.modified = modified;
}

@JsonProperty("modifiedBy")
public Integer getModifiedBy() {
return modifiedBy;
}

@JsonProperty("modifiedBy")
public void setModifiedBy(Integer modifiedBy) {
this.modifiedBy = modifiedBy;
}

@JsonProperty("datasetKey")
public Integer getDatasetKey() {
return datasetKey;
}

@JsonProperty("datasetKey")
public void setDatasetKey(Integer datasetKey) {
this.datasetKey = datasetKey;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("sectorKey")
public Integer getSectorKey() {
return sectorKey;
}

@JsonProperty("sectorKey")
public void setSectorKey(Integer sectorKey) {
this.sectorKey = sectorKey;
}

@JsonProperty("verbatimKey")
public Integer getVerbatimKey() {
return verbatimKey;
}

@JsonProperty("verbatimKey")
public void setVerbatimKey(Integer verbatimKey) {
this.verbatimKey = verbatimKey;
}

@JsonProperty("homotypicNameId")
public String getHomotypicNameId() {
return homotypicNameId;
}

@JsonProperty("homotypicNameId")
public void setHomotypicNameId(String homotypicNameId) {
this.homotypicNameId = homotypicNameId;
}

@JsonProperty("nameIndexId")
public Integer getNameIndexId() {
return nameIndexId;
}

@JsonProperty("nameIndexId")
public void setNameIndexId(Integer nameIndexId) {
this.nameIndexId = nameIndexId;
}

@JsonProperty("nameIndexMatchType")
public String getNameIndexMatchType() {
return nameIndexMatchType;
}

@JsonProperty("nameIndexMatchType")
public void setNameIndexMatchType(String nameIndexMatchType) {
this.nameIndexMatchType = nameIndexMatchType;
}

@JsonProperty("scientificName")
public String getScientificName() {
return scientificName;
}

@JsonProperty("scientificName")
public void setScientificName(String scientificName) {
this.scientificName = scientificName;
}

@JsonProperty("authorship")
public String getAuthorship() {
return authorship;
}

@JsonProperty("authorship")
public void setAuthorship(String authorship) {
this.authorship = authorship;
}

@JsonProperty("rank")
public String getRank() {
return rank;
}

@JsonProperty("rank")
public void setRank(String rank) {
this.rank = rank;
}

@JsonProperty("genus")
public String getGenus() {
return genus;
}

@JsonProperty("genus")
public void setGenus(String genus) {
this.genus = genus;
}

@JsonProperty("specificEpithet")
public String getSpecificEpithet() {
return specificEpithet;
}

@JsonProperty("specificEpithet")
public void setSpecificEpithet(String specificEpithet) {
this.specificEpithet = specificEpithet;
}

@JsonProperty("combinationAuthorship")
public Authorship getCombinationAuthorship() {
return combinationAuthorship;
}

@JsonProperty("combinationAuthorship")
public void setCombinationAuthorship(Authorship combinationAuthorship) {
this.combinationAuthorship = combinationAuthorship;
}

@JsonProperty("basionymAuthorship")
public Authorship getBasionymAuthorship() {
return basionymAuthorship;
}

@JsonProperty("basionymAuthorship")
public void setBasionymAuthorship(Authorship basionymAuthorship) {
this.basionymAuthorship = basionymAuthorship;
}

@JsonProperty("code")
public String getCode() {
return code;
}

@JsonProperty("code")
public void setCode(String code) {
this.code = code;
}

@JsonProperty("publishedInId")
public String getPublishedInId() {
return publishedInId;
}

@JsonProperty("publishedInId")
public void setPublishedInId(String publishedInId) {
this.publishedInId = publishedInId;
}

@JsonProperty("origin")
public String getOrigin() {
return origin;
}

@JsonProperty("origin")
public void setOrigin(String origin) {
this.origin = origin;
}

@JsonProperty("type")
public String getType() {
return type;
}

@JsonProperty("type")
public void setType(String type) {
this.type = type;
}

@JsonProperty("parsed")
public Boolean getParsed() {
return parsed;
}

@JsonProperty("parsed")
public void setParsed(Boolean parsed) {
this.parsed = parsed;
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