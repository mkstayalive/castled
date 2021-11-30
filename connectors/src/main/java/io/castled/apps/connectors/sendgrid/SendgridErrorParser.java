package io.castled.apps.connectors.sendgrid;

import com.google.inject.Singleton;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.InvalidFieldValueError;
import io.castled.commons.errors.errorclassifications.UnclassifiedError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class SendgridErrorParser {

    public CastledError getPipelineError(SendgridUpsertError sgError) {
        String errorMsg = sgError.getMessage();
        if (errorMsg.contains("error storing")) {
            // Sample error message:
            // "error storing custom field name=custom_date type=Date as value="10-03-2021": custom field value
            // is not in any of the following date formats: RFC3339, MM/DD/YYYY, or M/D/YYYY"
            Pattern regex = Pattern.compile("=[a-zA-Z]+");
            Matcher matcher = regex.matcher(errorMsg);
            // Expecting 2 matches
            // 1 -> field name
            String name = "";
            if (matcher.find()) {
                name = matcher.group();
            }
            // 2 -> field type
            String type = "";
            if (matcher.find()) {
                type = matcher.group();
            }
            return new InvalidFieldValueError(name, type, sgError.getMessage());
        }
        return new UnclassifiedError(sgError.getMessage());
    }
}
