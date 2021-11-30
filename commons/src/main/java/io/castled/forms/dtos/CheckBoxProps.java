package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;

public class CheckBoxProps extends DisplayFieldProps {

    public CheckBoxProps(String title, String description) {
        super(FormFieldType.CHECK_BOX, title, description);
    }
}
