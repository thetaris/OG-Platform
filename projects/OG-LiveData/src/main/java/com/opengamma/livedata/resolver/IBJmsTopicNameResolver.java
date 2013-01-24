/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Produces a JMS topic name simply using the request's underlying uniqueId.  
 */
public class IBJmsTopicNameResolver implements JmsTopicNameResolver {

  @Override
  public String resolve(JmsTopicNameResolveRequest request) {
    String topic = createTopicFromRequest(request);
    return topic;
  }

  @Override
  public Map<JmsTopicNameResolveRequest, String> resolve(Collection<JmsTopicNameResolveRequest> requests) {
    Map<JmsTopicNameResolveRequest, String> returnValue = new HashMap<JmsTopicNameResolveRequest, String>();
    for (JmsTopicNameResolveRequest request : requests) {
      String topic = createTopicFromRequest(request);
      returnValue.put(request, topic);
    }
    return null;
  }

  protected String createTopicFromRequest(JmsTopicNameResolveRequest request) {
    String topic = request.getMarketDataUniqueId().toString();
    return topic;
  }

}
