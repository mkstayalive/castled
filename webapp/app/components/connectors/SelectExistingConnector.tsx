import React, { useEffect, useState } from "react";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import { Badge, ListGroup } from "react-bootstrap";
import { ConnectorCategory } from "@/app/common/utils/types";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import appsService from "@/app/services/appsService";
import warehouseService from "@/app/services/warehouseService";

export interface SelectExistingConnectorProps {
  category: ConnectorCategory;
  onCreate: () => void;
  onSelect: (id: number) => void;
  typeOption: ConnectorTypeDto;
}

const SelectExistingConnector = ({
  category,
  onCreate,
  onSelect,
  typeOption,
}: SelectExistingConnectorProps) => {
  const [connectors, setConnectors] = useState<
    ConnectorDto[] | undefined | null
  >();
  useEffect(() => {
    const type = typeOption.value;
    const fetcher =
      category === "App" ? appsService.get(type) : warehouseService.get(type);
    fetcher
      .then(({ data }) => setConnectors(data))
      .catch(() => {
        setConnectors(null);
      });
  }, [category, typeOption.value]);
  return (
    <div className="categories">
      <ListGroup>
        {connectors?.map((connector, i) => (
          <ListGroup.Item
            key={i}
            className="rounded"
            onClick={() => onSelect(connector.id)}
          >
            <img className={connector.name} src={connector.logoUrl}></img>
            <strong>{connector.name} </strong>
            {connector.status !== "OK" && (
              <Badge bg="danger">{connector.status}</Badge>
            )}
          </ListGroup.Item>
        ))}
        {connectors !== undefined && (
          <button
            className="btn list-group-item rounded"
            onClick={() => onCreate()}
          >
            Create New {category}
          </button>
        )}
      </ListGroup>
      {!!connectors && connectors?.length === 0 && (
        <div>
          <p>No connectors found</p>
        </div>
      )}
    </div>
  );
};

export default SelectExistingConnector;
