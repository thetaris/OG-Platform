/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * Receives messages corresponding to {@link CalculationJob}, invokes them,
 * and then responds with messages corresponding to {@link CalculationJobResult}.
 *
 * @author kirk
 */
public class CalculationNodeRequestReceiver
extends AbstractCalculationNode
implements FudgeRequestReceiver {

  /**
   * @param cacheSource
   * @param functionRepository
   * @param securityMaster
   * @param jobSource
   * @param completionNotifier
   */
  public CalculationNodeRequestReceiver(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      ComputationTargetResolver targetResolver) {
    super(cacheSource, functionRepository, targetResolver);
  }

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeContext fudgeContext,
      FudgeMsgEnvelope requestEnvelope) {
    CalculationJob job = CalculationJob.fromFudgeMsg(new FudgeDeserializationContext (fudgeContext), requestEnvelope.getMessage ());
    CalculationJobResult jobResult = executeJob(job);
    return jobResult.toFudgeMsg(fudgeContext);
  }

}
