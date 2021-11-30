package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextBoxProps extends DisplayFieldProps {

    public TextBoxProps(String placeholder, String title, String description, String optionsRef) {
        super(FormFieldType.TEXT_BOX, title, description);
        this.placeholder = placeholder;
        this.optionsRef = optionsRef;
    }

    private String placeholder;
    private String optionsRef;
}
