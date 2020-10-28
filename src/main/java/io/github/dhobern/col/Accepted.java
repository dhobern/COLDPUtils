/*
 * Copyright 2020 Platyptilia.
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
    "name",
    "status",
    "origin",
    "parentId",
    "scrutinizer",
    "scrutinizerDate",
    "extinct",
    "label",
    "labelHtml"
})
public class Accepted {

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
    @JsonProperty("name")
    private Name name;
    @JsonProperty("status")
    private String status;
    @JsonProperty("origin")
    private String origin;
    @JsonProperty("parentId")
    private String parentId;
    @JsonProperty("scrutinizer")
    private String scrutinizer;
    @JsonProperty("scrutinizerDate")
    private String scrutinizerDate;
    @JsonProperty("extinct")
    private Boolean extinct;
    @JsonProperty("label")
    private String label;
    @JsonProperty("labelHtml")
    private String labelHtml;
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

    @JsonProperty("name")
    public Name getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(Name name) {
        this.name = name;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("origin")
    public String getOrigin() {
        return origin;
    }

    @JsonProperty("origin")
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parentId")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("scrutinizer")
    public String getScrutinizer() {
        return scrutinizer;
    }

    @JsonProperty("scrutinizer")
    public void setScrutinizer(String scrutinizer) {
        this.scrutinizer = scrutinizer;
    }

    @JsonProperty("scrutinizerDate")
    public String getScrutinizerDate() {
        return scrutinizerDate;
    }

    @JsonProperty("scrutinizerDate")
    public void setScrutinizerDate(String scrutinizerDate) {
        this.scrutinizerDate = scrutinizerDate;
    }

    @JsonProperty("extinct")
    public Boolean getExtinct() {
        return extinct;
    }

    @JsonProperty("extinct")
    public void setExtinct(Boolean extinct) {
        this.extinct = extinct;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("labelHtml")
    public String getLabelHtml() {
        return labelHtml;
    }

    @JsonProperty("labelHtml")
    public void setLabelHtml(String labelHtml) {
        this.labelHtml = labelHtml;
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
