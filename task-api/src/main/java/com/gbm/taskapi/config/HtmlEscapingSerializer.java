package com.gbm.taskapi.config;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

@JacksonComponent
public class HtmlEscapingSerializer extends StdSerializer<String> {

    public HtmlEscapingSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
        if (value != null) {
            gen.writeString(HtmlUtils.htmlEscape(value));
        }
    }
}
