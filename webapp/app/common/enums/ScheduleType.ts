export enum ScheduleType {
  CRON = "CRON",
  FREQUENCY = "FREQUENCY",
}

export enum ScheduleTimeUnit {
  MINUTES,
  HOURS,
  DAYS,
}

export const ScheduleTypeLabel: any = {
  [ScheduleType.CRON]: "Cron",
  [ScheduleType.FREQUENCY]: "Frequency",
};

export const SchedulTimeUnitLabel: any = {
  [ScheduleTimeUnit.MINUTES]: "Minutes",
  [ScheduleTimeUnit.HOURS]: "Hours",
  [ScheduleTimeUnit.DAYS]: "Days",
};
