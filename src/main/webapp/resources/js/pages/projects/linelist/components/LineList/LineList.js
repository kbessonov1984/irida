import React from "react";
import PropTypes from "prop-types";
import { Loader } from "../Loader";
import { TableContainer } from "./Table";

/**
 * Container class for the higher level states of the page:
 * 1. Loading
 * 2. Table
 * 3. Loading error.
 */
export const LineList = props => {
  const { initializing } = props;
  if (initializing) {
    return <Loader />;
  } else if (props.error) {
    // ERROR STATE
    // TODO: (Josh | 2018-04-11) Create error component
    return <h3>A major error has occurred! Better find a 💣 shelter!</h3>;
  }
  return <TableContainer />;
};

LineList.propTypes = {
  initializing: PropTypes.bool.isRequired,
  error: PropTypes.bool
};
