import _ from "lodash";

export default {
  selectOptions: (labelMap: { [key: string]: string }) => {
    return _.map(labelMap, (title, value) => ({
      value,
      title,
    }));
  },
  getTimeTakenStr: (timeMillis: number) => {
    let timeSecs = timeMillis / 1000;
    let secsPart = timeSecs % 60;
    let minsPart = 0;
    let hrsPart = 0;
    let runTimeMins = 0;
    let runTimeHrs = 0;
    if (timeSecs >= 60) {
      runTimeMins = Math.floor(timeSecs / 60);
      minsPart = runTimeMins % 60;
    }
    if (runTimeMins >= 60) {
      runTimeHrs = Math.floor(runTimeMins / 60);
      hrsPart = runTimeHrs % 60;
    }
    if (hrsPart > 0) {
      return `${hrsPart} hr ${minsPart} mins`;
    }
    if (minsPart > 0) {
      return `${minsPart} mins ${secsPart} secs`;
    }
    return `${secsPart} secs`;
  },
};
