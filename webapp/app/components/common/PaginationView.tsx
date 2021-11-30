import React from "react";
import _ from "lodash";
import classnames from "classnames";
import { Page } from "@/app/common/dtos/Page";
import jsUtils from "@/app/common/utils/jsUtils";

export interface PaginationViewProps {
  page?: Page<any> | null;
  summary?: boolean;
  className?: string;
  onPageChange: (pageNumber: number) => void;
}

const PaginationView: React.SFC<PaginationViewProps> = (props) => {
  if (!props.page || !props.page.size) return null;
  const { page: pageNumber, size: pageSize } = props.page;
  const start = pageNumber * pageSize + 1;
  const end = pageNumber * pageSize + props.page.content.length;
  const total = props.page.totalSize;
  const totalPages = Math.ceil(props.page.totalSize / props.page.size);
  const showPages = getShowPages(props.page);
  return (
    <div className="d-flex flex-row-reverse">
      <nav aria-label="Pagination">
        <ul className={"pagination mb-0 " + props.className}>
          <li
            className={classnames("page-item", {
              disabled: pageNumber === 0,
            })}
          >
            <a
              className="page-link"
              onClick={() => onPageChange(Math.max(0, pageNumber - 1), props)}
              tabIndex={-1}
            >
              <i className="fas fa-angle-left" />
              <span className="sr-only">Previous</span>
            </a>
          </li>
          {!props.summary &&
            _.map(showPages).map((p) => (
              <li
                key={p}
                className={classnames("page-item", {
                  active: pageNumber === p,
                })}
              >
                <a className="page-link" onClick={() => onPageChange(p, props)}>
                  {p + 1}
                </a>
              </li>
            ))}
          <li
            className={classnames("page-item", {
              disabled: pageNumber === props.page.size - 1,
            })}
          >
            <a
              className="page-link"
              onClick={() =>
                onPageChange(Math.min(totalPages - 1, pageNumber + 1), props)
              }
            >
              <i className="fas fa-angle-right" />
              <span className="sr-only">Next</span>
            </a>
          </li>
        </ul>
      </nav>
      {props.summary && (
        <div className="description mb-1 fs-4 text-center">
          {start} - {end} of {total}
        </div>
      )}
    </div>
  );
};
const getShowPages = (page: Page<any>) => {
  const pageNumber = page.page;
  const totalPages = Math.ceil(page.totalSize / page.size);
  let showPages = [0];
  const SPAN_PAGES = 2;
  for (let i = pageNumber - SPAN_PAGES; i <= pageNumber + SPAN_PAGES; i++) {
    if (i > 0 && i < totalPages) {
      showPages.push(i);
    }
  }
  showPages.push(totalPages - 1);
  return Array.from(new Set(showPages));
};
const onPageChange = (pageNumber: number, props: PaginationViewProps) => {
  jsUtils.scrollToTop();
  props.onPageChange(pageNumber);
};

export default PaginationView;
