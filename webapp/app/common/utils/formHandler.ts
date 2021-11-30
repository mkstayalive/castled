import { FormikHelpers } from "formik/dist/types";
import { AxiosError, AxiosResponse } from "axios";
import { StringAnyMap } from "@/app/common/utils/types";
import _ from "lodash";
import eventService from "@/app/services/eventService";

export interface FormMeta {
  id: string;
  pickFieldsForEvent: string[];
  dataLayer?: StringAnyMap;
}

function formHandler<REQ, RES>(
  formMeta: FormMeta,
  service: (values: REQ) => Promise<AxiosResponse<RES>>,
  onSuccess?: (res: RES) => void,
  onError?: (error: AxiosError<RES>) => void,
  onBeforeSubmit?: (values: REQ) => REQ
) {
  return async (
    values: REQ,
    { setSubmitting }: FormikHelpers<REQ>
  ): Promise<void> => {
    const eventProps = {
      formId: formMeta.id,
      ..._.pick(values, formMeta.pickFieldsForEvent),
      ...(formMeta.dataLayer || {}),
    };
    try {
      setSubmitting(true);
      const res = onBeforeSubmit
        ? await service(onBeforeSubmit(values))
        : await service(values);
      if (res.status >= 200 && res.status < 300) {
        eventService.send({
          event: formMeta.id + "_success",
          ...eventProps,
        });
        onSuccess?.(res.data);
      }
    } catch (e: any) {
      eventService.send({
        event: formMeta.id + "_failed",
        ...eventProps,
      });
      onError?.(e.response);
    }
    setSubmitting(false);
  };
}

export default formHandler;
