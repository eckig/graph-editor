/**
 */
package de.tesis.dynaware.grapheditor.model.impl;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>GModel</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getConnections <em>Connections</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getContentWidth <em>Content Width</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getContentHeight <em>Content Height</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl#getSupergraph <em>Supergraph</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GModelImpl extends MinimalEObjectImpl.Container implements GModel {
    /**
     * The cached value of the '{@link #getNodes() <em>Nodes</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNodes()
     * @generated
     * @ordered
     */
    protected EList<GNode> nodes;

    /**
     * The cached value of the '{@link #getConnections() <em>Connections</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConnections()
     * @generated
     * @ordered
     */
    protected EList<GConnection> connections;

    /**
     * The default value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected static final String TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected String type = TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getContentWidth() <em>Content Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContentWidth()
     * @generated
     * @ordered
     */
    protected static final double CONTENT_WIDTH_EDEFAULT = 3000.0;

    /**
     * The cached value of the '{@link #getContentWidth() <em>Content Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContentWidth()
     * @generated
     * @ordered
     */
    protected double contentWidth = CONTENT_WIDTH_EDEFAULT;

    /**
     * The default value of the '{@link #getContentHeight() <em>Content Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContentHeight()
     * @generated
     * @ordered
     */
    protected static final double CONTENT_HEIGHT_EDEFAULT = 2250.0;

    /**
     * The cached value of the '{@link #getContentHeight() <em>Content Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContentHeight()
     * @generated
     * @ordered
     */
    protected double contentHeight = CONTENT_HEIGHT_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GModelImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GraphPackage.Literals.GMODEL;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<GNode> getNodes() {
        if (nodes == null) {
            nodes = new EObjectContainmentEList<GNode>(GNode.class, this, GraphPackage.GMODEL__NODES);
        }
        return nodes;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<GConnection> getConnections() {
        if (connections == null) {
            connections = new EObjectContainmentEList<GConnection>(GConnection.class, this, GraphPackage.GMODEL__CONNECTIONS);
        }
        return connections;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getType() {
        return type;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setType(String newType) {
        String oldType = type;
        type = newType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GMODEL__TYPE, oldType, type));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getContentHeight() {
        return contentHeight;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setContentHeight(double newContentHeight) {
        double oldContentHeight = contentHeight;
        contentHeight = newContentHeight;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GMODEL__CONTENT_HEIGHT, oldContentHeight, contentHeight));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GNode getSupergraph() {
        if (eContainerFeatureID() != GraphPackage.GMODEL__SUPERGRAPH) return null;
        return (GNode)eInternalContainer();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GMODEL__SUPERGRAPH:
                if (eInternalContainer() != null)
                    msgs = eBasicRemoveFromContainer(msgs);
                return eBasicSetContainer(otherEnd, GraphPackage.GMODEL__SUPERGRAPH, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getContentWidth() {
        return contentWidth;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setContentWidth(double newContentWidth) {
        double oldContentWidth = contentWidth;
        contentWidth = newContentWidth;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GMODEL__CONTENT_WIDTH, oldContentWidth, contentWidth));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GMODEL__NODES:
                return ((InternalEList<?>)getNodes()).basicRemove(otherEnd, msgs);
            case GraphPackage.GMODEL__CONNECTIONS:
                return ((InternalEList<?>)getConnections()).basicRemove(otherEnd, msgs);
            case GraphPackage.GMODEL__SUPERGRAPH:
                return eBasicSetContainer(null, GraphPackage.GMODEL__SUPERGRAPH, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
        switch (eContainerFeatureID()) {
            case GraphPackage.GMODEL__SUPERGRAPH:
                return eInternalContainer().eInverseRemove(this, GraphPackage.GNODE__SUBGRAPH, GNode.class, msgs);
        }
        return super.eBasicRemoveFromContainerFeature(msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case GraphPackage.GMODEL__NODES:
                return getNodes();
            case GraphPackage.GMODEL__CONNECTIONS:
                return getConnections();
            case GraphPackage.GMODEL__TYPE:
                return getType();
            case GraphPackage.GMODEL__CONTENT_WIDTH:
                return getContentWidth();
            case GraphPackage.GMODEL__CONTENT_HEIGHT:
                return getContentHeight();
            case GraphPackage.GMODEL__SUPERGRAPH:
                return getSupergraph();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case GraphPackage.GMODEL__NODES:
                getNodes().clear();
                getNodes().addAll((Collection<? extends GNode>)newValue);
                return;
            case GraphPackage.GMODEL__CONNECTIONS:
                getConnections().clear();
                getConnections().addAll((Collection<? extends GConnection>)newValue);
                return;
            case GraphPackage.GMODEL__TYPE:
                setType((String)newValue);
                return;
            case GraphPackage.GMODEL__CONTENT_WIDTH:
                setContentWidth((Double)newValue);
                return;
            case GraphPackage.GMODEL__CONTENT_HEIGHT:
                setContentHeight((Double)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case GraphPackage.GMODEL__NODES:
                getNodes().clear();
                return;
            case GraphPackage.GMODEL__CONNECTIONS:
                getConnections().clear();
                return;
            case GraphPackage.GMODEL__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case GraphPackage.GMODEL__CONTENT_WIDTH:
                setContentWidth(CONTENT_WIDTH_EDEFAULT);
                return;
            case GraphPackage.GMODEL__CONTENT_HEIGHT:
                setContentHeight(CONTENT_HEIGHT_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case GraphPackage.GMODEL__NODES:
                return nodes != null && !nodes.isEmpty();
            case GraphPackage.GMODEL__CONNECTIONS:
                return connections != null && !connections.isEmpty();
            case GraphPackage.GMODEL__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case GraphPackage.GMODEL__CONTENT_WIDTH:
                return contentWidth != CONTENT_WIDTH_EDEFAULT;
            case GraphPackage.GMODEL__CONTENT_HEIGHT:
                return contentHeight != CONTENT_HEIGHT_EDEFAULT;
            case GraphPackage.GMODEL__SUPERGRAPH:
                return getSupergraph() != null;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (type: ");
        result.append(type);
        result.append(", contentWidth: ");
        result.append(contentWidth);
        result.append(", contentHeight: ");
        result.append(contentHeight);
        result.append(')');
        return result.toString();
    }

} //GModelImpl
