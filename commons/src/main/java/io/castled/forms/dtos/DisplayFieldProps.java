package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisplayFieldProps extends FormFieldProps {

    public DisplayFieldProps(FormFieldType fieldType, String title, String description) {
        super(fieldType);
        this.title = title;
        this.description = description;
    }

    private String title;
    private String description;
}
