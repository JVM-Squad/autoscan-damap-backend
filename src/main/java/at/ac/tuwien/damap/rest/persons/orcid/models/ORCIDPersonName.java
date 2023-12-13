package at.ac.tuwien.damap.rest.persons.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import at.ac.tuwien.damap.rest.persons.orcid.models.base.ORCIDValueType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDPersonName {

    @JsonProperty(value = "given-names")
    @JsonSetter(nulls = Nulls.SKIP)
    ORCIDValueType givenNames = new ORCIDValueType();

    @JsonProperty(value = "family-name")
    @JsonSetter(nulls = Nulls.SKIP)
    ORCIDValueType familyName = new ORCIDValueType();

    @JsonProperty(value = "credit-name")
    @JsonSetter(nulls = Nulls.SKIP)
    ORCIDValueType creditName = new ORCIDValueType();

    @JsonProperty
    String path;
}
