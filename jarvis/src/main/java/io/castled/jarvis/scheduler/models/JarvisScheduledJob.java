package io.castled.jarvis.scheduler.models;

import io.castled.models.jobschedule.JobSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobKey;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JarvisScheduledJob {

    private JobKey jobKey;
    private JobSchedule jobSchedule;
    private Class<? extends Job> jobClazz;
    private Date startTs;
    private Map<String, Object> jobParams;
}
