package io.castled.models.jobschedule;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class FrequencyJobSchedule extends JobSchedule {

    private int frequency;

    public FrequencyJobSchedule(int frequency) {
        super(JobScheduleType.FREQUENCY);
        this.frequency = frequency;
    }

    @Override
    public int getExecutionTime() {
        return frequency;
    }
}
