import React from "react";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import { Col, Container, Row } from "react-bootstrap";

export const DebugPipelineWizContext = () => {
  if (process.env.DEBUG !== "true") return null;
  const { pipelineWizContext } = usePipelineWizContext();
  return (
    <Container fluid="sm" className="container-main mt-3">
      <Row>
        <Col>
          <pre>{JSON.stringify(pipelineWizContext, null, 2)}</pre>
        </Col>
      </Row>
    </Container>
  );
};
