/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * Classes for subscribing to market data from Interactive Brokers. 
 * The interface delegates to the IB Java API which utilizes the IB Trader Workstation (TWS). 
 * This means the TWS must be running on the configured target machine for this server to work. 
 * Some events like opening a connection also require manual interaction with TWS atm.
 */
package com.opengamma.livedata.ib.server;
