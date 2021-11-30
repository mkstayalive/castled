package io.castled.forms.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.castled.forms.FormFieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextBoxProps.class, name = "TEXT_BOX"),
        @JsonSubTypes.Type(value = DropDownProps.class, name = "DROP_DOWN"),
        @JsonSubTypes.Type(value = MappingProps.class, name = "MAPPING"),
        @JsonSubTypes.Type(value = HiddenProps.class, name = "HIDDEN"),
        @JsonSubTypes.Type(value = JsonFileProps.class, name = "JSON_FILE"),
        @JsonSubTypes.Type(value = TextFileProps.class, name = "TEXT_FILE"),
        @JsonSubTypes.Type(value = RadioGroupProps.class, name = "RADIO_GROUP")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class FormFieldProps {
    private FormFieldType type;
}
