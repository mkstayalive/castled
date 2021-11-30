package io.castled.models.jobschedule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FrequencyJobSchedule.class, name = "FREQUENCY"),
        @JsonSubTypes.Type(value = CronJobSchedule.class, name = "CRON")})
@NoArgsConstructor
@AllArgsConstructor
public abstract class JobSchedule {
    private JobScheduleType type;

    //in secs
    @JsonIgnore
    public abstract int getExecutionTime();
}
