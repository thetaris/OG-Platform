/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.util.Comparator;

import com.opengamma.util.PublicSPI;

/**
 * Available sort orders for portfolios.
 */
@PublicSPI
public enum PortfolioSearchSortOrder implements Comparator<PortfolioDocument> {
  // this design is simple and perhaps not ideal, but it is effective for most use cases at the moment

  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_ASC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj1.getObjectId().compareTo(obj2.getObjectId());
    }
  },
  /**
   * Sort by object id ascending.
   */
  OBJECT_ID_DESC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj2.getObjectId().compareTo(obj1.getObjectId());
    }
  },
  /**
   * Sort by version from instant ascending.
   */
  VERSION_FROM_INSTANT_ASC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj1.getVersionFromInstant().compareTo(obj2.getVersionFromInstant());
    }
  },
  /**
   * Sort by version from instant descending.
   */
  VERSION_FROM_INSTANT_DESC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj2.getVersionFromInstant().compareTo(obj1.getVersionFromInstant());
    }
  },
  /**
   * Sort by name ascending.
   */
  NAME_ASC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj1.getObject().getName().compareTo(obj2.getObject().getName());
    }
  },
  /**
   * Sort by name descending.
   */
  NAME_DESC {
    @Override
    public int compare(PortfolioDocument obj1, PortfolioDocument obj2) {
      return obj2.getObject().getName().compareTo(obj1.getObject().getName());
    }
  };

}