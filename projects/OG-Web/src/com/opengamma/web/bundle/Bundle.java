/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;

/**
 * A bundle of CSS/Javascript files.
 * <p>
 * The CSS/Javascript files, stored using {@link Fragment}, are configured using bundles.
 * Bundles consist of an ordered list of fragments and bundles, forming a tree.
 * Each bundle has an ID that can be used to refer to it.
 * <p>
 * This class is mutable and not thread-safe.
 */
@BeanDefinition
public class Bundle extends DirectBean implements BundleNode {

  /**
   * The ID for the bundle.
   */
  @PropertyDefinition
  private String _id;
  /**
   * The type of bundle, such as CSS or Javascript.
   */
  @PropertyDefinition
  private BundleType _type;
  /**
   * The ordered list of nodes, consisting of bundles and fragments.
   */
  @PropertyDefinition
  private final List<BundleNode> _childNodes = new ArrayList<BundleNode>();

  /**
   * Creates a bundle.
   */
  protected Bundle() {
  }

  /**
   * Creates a bundle with an ID.
   * <p>
   * If the ID has a file suffix, then the type will be set.
   * 
   * @param id the bundle name, not null
   */
  public Bundle(final String id) {
    ArgumentChecker.notNull(id, "id");
    _id = id;
    _type = BundleType.getType(id);
  }

  //-------------------------------------------------------------------------
  @Override
  public List<Bundle> getAllBundles() {
    List<Bundle> result = new ArrayList<Bundle>();
    result.add(this);
    for (BundleNode node : getChildNodes()) {
      result.addAll(node.getAllBundles());
    }
    return result;
  }

  @Override
  public List<Fragment> getAllFragments() {
    List<Fragment> result = new ArrayList<Fragment>();
    for (BundleNode node : getChildNodes()) {
      result.addAll(node.getAllFragments());
    }
    return result;
  }

  /**
   * Adds a child node to this node.
   * 
   * @param childNode  the child node, not null
   */
  public void addChildNode(final BundleNode childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    getChildNodes().add(childNode);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Bundle}.
   * @return the meta-bean, not null
   */
  public static Bundle.Meta meta() {
    return Bundle.Meta.INSTANCE;
  }

  @Override
  public Bundle.Meta metaBean() {
    return Bundle.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        return getId();
      case 3575610:  // type
        return getType();
      case 1339293429:  // childNodes
        return getChildNodes();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        setId((String) newValue);
        return;
      case 3575610:  // type
        setType((BundleType) newValue);
        return;
      case 1339293429:  // childNodes
        setChildNodes((List<BundleNode>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Bundle other = (Bundle) obj;
      return JodaBeanUtils.equal(getId(), other.getId()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getChildNodes(), other.getChildNodes());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getChildNodes());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the ID for the bundle.
   * @return the value of the property
   */
  public String getId() {
    return _id;
  }

  /**
   * Sets the ID for the bundle.
   * @param id  the new value of the property
   */
  public void setId(String id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<String> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type of bundle, such as CSS or Javascript.
   * @return the value of the property
   */
  public BundleType getType() {
    return _type;
  }

  /**
   * Sets the type of bundle, such as CSS or Javascript.
   * @param type  the new value of the property
   */
  public void setType(BundleType type) {
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * @return the property, not null
   */
  public final Property<BundleType> type() {
    return metaBean().type().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the ordered list of nodes, consisting of bundles and fragments.
   * @return the value of the property
   */
  public List<BundleNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Sets the ordered list of nodes, consisting of bundles and fragments.
   * @param childNodes  the new value of the property
   */
  public void setChildNodes(List<BundleNode> childNodes) {
    this._childNodes.clear();
    this._childNodes.addAll(childNodes);
  }

  /**
   * Gets the the {@code childNodes} property.
   * @return the property, not null
   */
  public final Property<List<BundleNode>> childNodes() {
    return metaBean().childNodes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Bundle}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<String> _id = DirectMetaProperty.ofReadWrite(
        this, "id", Bundle.class, String.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<BundleType> _type = DirectMetaProperty.ofReadWrite(
        this, "type", Bundle.class, BundleType.class);
    /**
     * The meta-property for the {@code childNodes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<BundleNode>> _childNodes = DirectMetaProperty.ofReadWrite(
        this, "childNodes", Bundle.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "id",
        "type",
        "childNodes");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case 3575610:  // type
          return _type;
        case 1339293429:  // childNodes
          return _childNodes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Bundle> builder() {
      return new DirectBeanBuilder<Bundle>(new Bundle());
    }

    @Override
    public Class<? extends Bundle> beanType() {
      return Bundle.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BundleType> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code childNodes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<BundleNode>> childNodes() {
      return _childNodes;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
