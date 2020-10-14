package org.openhab.support.knx2openhab;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config
{

    @JsonProperty("templateDir")
    String templateDir;

    @JsonProperty("configFile")
    String configFile;

    @JsonProperty("env")
    Map<String, String> env;

}
