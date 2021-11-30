import React, { useEffect, useState } from "react";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import { Badge, Col, ListGroup, Row } from "react-bootstrap";
import { ConnectorCategory } from "@/app/common/utils/types";
import appsService from "@/app/services/appsService";
import warehouseService from "@/app/services/warehouseService";

export interface SelectConnectorTypeProps {
  category: ConnectorCategory;
  onSelect: (type: ConnectorTypeDto) => void;
}

const SelectConnectorType = ({
  category,
  onSelect,
}: SelectConnectorTypeProps) => {
  const [typeList, setTypeList] = useState<ConnectorTypeDto[] | undefined>();
  useEffect(() => {
    const fetcher =
      category === "App" ? appsService.types : warehouseService.types;
    fetcher().then(({ data }) => {
      setTypeList(data);
    });
  }, [category]);
  return (
    <>
      <div className="grid-categories">
        <Row xs={3}>
          {typeList?.map((type, i) => (
            <ListGroup>
              <ListGroup.Item
                key={i}
                className="rounded"
                onClick={() => onSelect(type)}
              >
                <Col>
                  <div>
                    <img className={type.title} src={type.logoUrl}></img>
                    <strong>{type.title} </strong>
                    {type.count > 0 && (
                      <Badge bg="secondary">{type.count}</Badge>
                    )}
                  </div>
                </Col>
              </ListGroup.Item>
            </ListGroup>
          ))}
        </Row>
      </div>
    </>
  );
};

export default SelectConnectorType;
