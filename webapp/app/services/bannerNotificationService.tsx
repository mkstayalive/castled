import { store } from "react-notifications-component";
import React from "react";

let prevNotificationId: any = null; // buggy way to remove prev notifications

export default {
  error: (
    message: string | React.ReactNode | React.FunctionComponent
  ): void => {
    console.error(message);
    store.removeNotification(prevNotificationId);
    prevNotificationId = store.addNotification({
      message,
      type: "danger", // 'default', 'success', 'info', 'warning'
      container: "top-center", // where to position the notifications
      animationIn: ["animate__animated", "animate__bounceIn"],
      animationOut: ["animate__animated", "animate__backOutUp"],
      dismiss: {
        duration: 5000,
      },
    });
  },
  warn: (message: string | React.ReactNode | React.FunctionComponent): void => {
    console.error(message);
    store.removeNotification(prevNotificationId);
    prevNotificationId = store.addNotification({
      message,
      type: "warning", // 'default', 'success', 'info', 'warning'
      container: "top-center", // where to position the notifications
      animationIn: ["animate__animated", "animate__bounceIn"],
      animationOut: ["animate__animated", "animate__backOutUp"],
      dismiss: {
        duration: 3000,
      },
    });
  },
  success: (
    message: string | React.ReactNode | React.FunctionComponent
  ): void => {
    store.removeNotification(prevNotificationId);
    prevNotificationId = store.addNotification({
      message,
      type: "success", // 'default', 'success', 'info', 'warning'
      container: "top-center", // where to position the notifications
      animationIn: ["animate__animated", "animate__bounceIn"],
      animationOut: ["animate__animated", "animate__backOutUp"],
      dismiss: {
        duration: 3000,
      },
    });
  },
};
