import React, { useEffect } from "react";
import { Form, Formik } from "formik";
import authService from "@/app/services/authService";
import InputField from "@/app/components/forminputs/InputField";
import { useSession } from "@/app/common/context/sessionContext";
import { LoggedInUserDto } from "@/app/common/dtos/LoggedInUserDto";
import { NextRouter, useRouter } from "next/router";
import GuestLayout from "@/app/components/layout/GuestLayout";
import ButtonSubmit from "@/app/components/forminputs/ButtonSubmit";
import * as Yup from "yup";
import { Button } from "react-bootstrap";
import { ExternalLoginType } from "@/app/common/enums/ExternalLoginType";
import authUtils from "@/app/common/utils/authUtils";
import buttonHandler from "@/app/common/utils/buttonHandler";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import { GetServerSidePropsContext } from "next";

export async function getServerSideProps(context: GetServerSidePropsContext) {
  return {
    props: {
      appBaseUrl: process.env.APP_BASE_URL
    }
  };
}

interface serverSideProps {
  appBaseUrl: string;
  apiBase: string;
}

function Register(context: GetServerSidePropsContext, props: serverSideProps) {
  const { setUser } = useSession();
  const router = useRouter();
  const formSchema = Yup.object().shape({
    password: Yup.string().required("This field is required"),
    confirmPassword: Yup.string().when("password", {
      is: (val: string) => (val && val.length > 0 ? true : false),
      then: Yup.string().oneOf([Yup.ref("password")], "Passwords need to match")
    })
  });

  useEffect(() => {
    if (!router.isReady) return;

    if (router.query.failure_message) {
      bannerNotificationService.error(router.query.failure_message);
    }
  }, [router.isReady]);

  return (
    <GuestLayout>
      <Formik
        initialValues={{
          firstName: "",
          lastName: "",
          password: "",
          confirmPassword: ""
        }}
        validationSchema={formSchema}
        onSubmit={(values) => handleRegisterUser(values, setUser, router!)}
      >
        <Form>
          <InputField
            type="string"
            name="firstName"
            title="First Name"
            placeholder="Enter first name"
          />
          <InputField
            type="string"
            name="lastName"
            title="Last Name"
            placeholder="Enter last name"
          />
          <InputField
            type="password"
            name="password"
            title="Password"
            placeholder="Enter password"
          />

          <InputField
            type="password"
            name="confirmPassword"
            title="Confirm Password"
            placeholder="Confirm password"
          />
          <ButtonSubmit className="form-control" />
        </Form>
      </Formik>
      <div className="mt-3 text-center">
        <Button
          href={authUtils.getExternalLoginUrl(
            props.appBaseUrl,
            ExternalLoginType.GOOGLE,
            router.pathname
          )}
          onClick={buttonHandler({ id: "signup_with_google" })}
        >
          Sign up with Google
        </Button>
      </div>
    </GuestLayout>
  );
}

export interface RegisterForm {
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
}

const handleRegisterUser = async (
  registerForm: RegisterForm,
  setUser: (session: LoggedInUserDto | null) => void,
  router: NextRouter
) => {
  if (process.browser) {
    await authService.register({
      token: router.query.token as string,
      firstName: registerForm.firstName,
      lastName: registerForm.lastName,
      password: registerForm.password
    });
    await router.push("/auth/login");
  }
};

export default Register;
