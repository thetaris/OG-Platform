/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import com.opengamma.fudge.FudgeMsg;

/**
 * 
 *
 * @author kirk
 */
public interface LiveDataValueUpdate {
  long getRelevantTimestamp();
  
  LiveDataSpecification getSpecification();
  
  FudgeMsg getFields();

}
