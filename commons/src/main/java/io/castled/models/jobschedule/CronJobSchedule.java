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
public class CronJobSchedule extends JobSchedule {

    public CronJobSchedule(String cronExpression) {
        super(JobScheduleType.CRON);
        this.cronExpression = cronExpression;
    }

    private String cronExpression;

    @Override
    public int getExecutionTime() {
        throw new UnsupportedOperationException("Get execution time not implemented for cron job schedule");
    }
}
