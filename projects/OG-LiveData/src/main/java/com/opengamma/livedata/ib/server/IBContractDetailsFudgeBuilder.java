/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ContractDetails}. 
 * <p>
 * Note: this builder is not yet complete; only the most important fields are encoded atm.
 * TODO: write a separate FudgeBuilder for Contract itself.
 */
@FudgeBuilderFor(ContractDetails.class)
public class IBContractDetailsFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ContractDetails> {

  /** Field name for "conId". */
  public static final String CONTRACT_ID_FIELD_NAME = "conId";

  /** Field name for "symbol". */
  public static final String SYMBOL_FIELD_NAME = "symbol";

  /** Field name for "longName". */
  public static final String LONGNAME_FIELD_NAME = "longName";

  /** Field name for "secType". */
  public static final String SECURITY_TYPE_FIELD_NAME = "secType";

  /** Field name for "secIdType". */
  public static final String SECURITY_ID_TYPE_FIELD_NAME = "secIdType";

  /** Field name for "secId". */
  public static final String SECURITY_ID_FIELD_NAME = "secId";

  /** Field name for "currency". */
  public static final String CURRENCY_FIELD_NAME = "currency";

  /** Field name for "exchange". */
  public static final String EXCHANGE_FIELD_NAME = "exchange";

  /** Field name for "primaryExch". */
  public static final String PRIMARY_EXCHANGE_FIELD_NAME = "primaryExch";

  /** Field name for "validExchanges". */
  public static final String VALID_EXCHANGES_FIELD_NAME = "validExchanges";

  /** Field name for "underConId". */
  public static final String UNDERLYING_CONTRACT_ID_FIELD_NAME = "underConId";

  public IBContractDetailsFudgeBuilder() {
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ContractDetails object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CONTRACT_ID_FIELD_NAME, null, object.m_summary.m_conId);
    serializer.addToMessage(msg, SYMBOL_FIELD_NAME, null, object.m_summary.m_symbol);
    serializer.addToMessage(msg, LONGNAME_FIELD_NAME, null, object.m_longName);
    serializer.addToMessage(msg, SECURITY_TYPE_FIELD_NAME, null, object.m_summary.m_secType);
    serializer.addToMessage(msg, SECURITY_ID_TYPE_FIELD_NAME, null, object.m_summary.m_secIdType);
    serializer.addToMessage(msg, SECURITY_ID_FIELD_NAME, null, object.m_summary.m_secId);
    serializer.addToMessage(msg, CURRENCY_FIELD_NAME, null, object.m_summary.m_currency);
    serializer.addToMessage(msg, EXCHANGE_FIELD_NAME, null, object.m_summary.m_exchange);
    serializer.addToMessage(msg, PRIMARY_EXCHANGE_FIELD_NAME, null, object.m_summary.m_primaryExch);
    serializer.addToMessage(msg, VALID_EXCHANGES_FIELD_NAME, null, object.m_validExchanges);
    serializer.addToMessage(msg, UNDERLYING_CONTRACT_ID_FIELD_NAME, null, object.m_underConId);
    return msg;
  }

  @Override
  public ContractDetails buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    ContractDetails cd = fromFudgeMsg(deserializer, message);
    return cd;
  }

  public static ContractDetails fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    ContractDetails cd = new ContractDetails();
    Contract c = cd.m_summary;
    c.m_conId = msg.getInt(CONTRACT_ID_FIELD_NAME);
    c.m_symbol = msg.getString(SYMBOL_FIELD_NAME);
    c.m_secType = msg.getString(SECURITY_TYPE_FIELD_NAME);
    c.m_secIdType = msg.getString(SECURITY_ID_TYPE_FIELD_NAME);
    c.m_secId = msg.getString(SECURITY_ID_FIELD_NAME);
    c.m_currency = msg.getString(CURRENCY_FIELD_NAME);
    c.m_exchange = msg.getString(EXCHANGE_FIELD_NAME);
    c.m_primaryExch = msg.getString(PRIMARY_EXCHANGE_FIELD_NAME);
    cd.m_longName = msg.getString(LONGNAME_FIELD_NAME);
    cd.m_validExchanges = msg.getString(VALID_EXCHANGES_FIELD_NAME);
    cd.m_underConId = msg.getInt(LONGNAME_FIELD_NAME);
    return cd;
  }

  /**
   * Dummy secondary type to force serialization.
   */
  @FudgeSecondaryType
  public static final SecondaryFieldType<ContractDetails, FudgeMsg> SECONDARY_TYPE_INSTANCE = new SecondaryFieldType<ContractDetails, FudgeMsg>(FudgeWireType.SUB_MESSAGE, ContractDetails.class) {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    @Override
    public FudgeMsg secondaryToPrimary(final ContractDetails object) {
      throw new OpenGammaRuntimeException("ContractDetails should be serialized, not added directly to a Fudge message");
    }

    @Override
    public ContractDetails primaryToSecondary(final FudgeMsg message) {
      return fromFudgeMsg(null, message);
    }
  };

}
