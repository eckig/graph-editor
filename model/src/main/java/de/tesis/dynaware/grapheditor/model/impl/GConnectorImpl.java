/**
 */
package de.tesis.dynaware.grapheditor.model.impl;

import de.tesis.dynaware.grapheditor.model.GConnectable;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>GConnector</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getConnections <em>Connections</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getX <em>X</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#getY <em>Y</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl#isConnectionDetachedOnDrag <em>Connection Detached On Drag</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GConnectorImpl extends MinimalEObjectImpl.Container implements GConnector {
    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected static final String ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected String id = ID_EDEFAULT;

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
     * The cached value of the '{@link #getConnections() <em>Connections</em>}' reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConnections()
     * @generated
     * @ordered
     */
    protected EList<GConnection> connections;

    /**
     * The default value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected static final double X_EDEFAULT = 0.0;

    /**
     * The cached value of the '{@link #getX() <em>X</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getX()
     * @generated
     * @ordered
     */
    protected double x = X_EDEFAULT;

    /**
     * The default value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected static final double Y_EDEFAULT = 0.0;

    /**
     * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getY()
     * @generated
     * @ordered
     */
    protected double y = Y_EDEFAULT;

    /**
     * The default value of the '{@link #isConnectionDetachedOnDrag() <em>Connection Detached On Drag</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isConnectionDetachedOnDrag()
     * @generated
     * @ordered
     */
    protected static final boolean CONNECTION_DETACHED_ON_DRAG_EDEFAULT = true;

    /**
     * The cached value of the '{@link #isConnectionDetachedOnDrag() <em>Connection Detached On Drag</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isConnectionDetachedOnDrag()
     * @generated
     * @ordered
     */
    protected boolean connectionDetachedOnDrag = CONNECTION_DETACHED_ON_DRAG_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GConnectorImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GraphPackage.Literals.GCONNECTOR;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setId(String newId) {
        String oldId = id;
        id = newId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__ID, oldId, id));
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
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__TYPE, oldType, type));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GConnectable getParent() {
        if (eContainerFeatureID() != GraphPackage.GCONNECTOR__PARENT) return null;
        return (GConnectable)eInternalContainer();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetParent(GConnectable newParent, NotificationChain msgs) {
        msgs = eBasicSetContainer((InternalEObject)newParent, GraphPackage.GCONNECTOR__PARENT, msgs);
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParent(GConnectable newParent) {
        if (newParent != eInternalContainer() || (eContainerFeatureID() != GraphPackage.GCONNECTOR__PARENT && newParent != null)) {
            if (EcoreUtil.isAncestor(this, newParent))
                throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
            NotificationChain msgs = null;
            if (eInternalContainer() != null)
                msgs = eBasicRemoveFromContainer(msgs);
            if (newParent != null)
                msgs = ((InternalEObject)newParent).eInverseAdd(this, GraphPackage.GCONNECTABLE__CONNECTORS, GConnectable.class, msgs);
            msgs = basicSetParent(newParent, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__PARENT, newParent, newParent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<GConnection> getConnections() {
        if (connections == null) {
            connections = new EObjectResolvingEList<GConnection>(GConnection.class, this, GraphPackage.GCONNECTOR__CONNECTIONS);
        }
        return connections;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getX() {
        return x;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setX(double newX) {
        double oldX = x;
        x = newX;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__X, oldX, x));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getY() {
        return y;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setY(double newY) {
        double oldY = y;
        y = newY;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__Y, oldY, y));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isConnectionDetachedOnDrag() {
        return connectionDetachedOnDrag;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setConnectionDetachedOnDrag(boolean newConnectionDetachedOnDrag) {
        boolean oldConnectionDetachedOnDrag = connectionDetachedOnDrag;
        connectionDetachedOnDrag = newConnectionDetachedOnDrag;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GCONNECTOR__CONNECTION_DETACHED_ON_DRAG, oldConnectionDetachedOnDrag, connectionDetachedOnDrag));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GCONNECTOR__PARENT:
                if (eInternalContainer() != null)
                    msgs = eBasicRemoveFromContainer(msgs);
                return basicSetParent((GConnectable)otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GCONNECTOR__PARENT:
                return basicSetParent(null, msgs);
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
            case GraphPackage.GCONNECTOR__PARENT:
                return eInternalContainer().eInverseRemove(this, GraphPackage.GCONNECTABLE__CONNECTORS, GConnectable.class, msgs);
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
            case GraphPackage.GCONNECTOR__ID:
                return getId();
            case GraphPackage.GCONNECTOR__TYPE:
                return getType();
            case GraphPackage.GCONNECTOR__PARENT:
                return getParent();
            case GraphPackage.GCONNECTOR__CONNECTIONS:
                return getConnections();
            case GraphPackage.GCONNECTOR__X:
                return getX();
            case GraphPackage.GCONNECTOR__Y:
                return getY();
            case GraphPackage.GCONNECTOR__CONNECTION_DETACHED_ON_DRAG:
                return isConnectionDetachedOnDrag();
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
            case GraphPackage.GCONNECTOR__ID:
                setId((String)newValue);
                return;
            case GraphPackage.GCONNECTOR__TYPE:
                setType((String)newValue);
                return;
            case GraphPackage.GCONNECTOR__PARENT:
                setParent((GConnectable)newValue);
                return;
            case GraphPackage.GCONNECTOR__CONNECTIONS:
                getConnections().clear();
                getConnections().addAll((Collection<? extends GConnection>)newValue);
                return;
            case GraphPackage.GCONNECTOR__X:
                setX((Double)newValue);
                return;
            case GraphPackage.GCONNECTOR__Y:
                setY((Double)newValue);
                return;
            case GraphPackage.GCONNECTOR__CONNECTION_DETACHED_ON_DRAG:
                setConnectionDetachedOnDrag((Boolean)newValue);
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
            case GraphPackage.GCONNECTOR__ID:
                setId(ID_EDEFAULT);
                return;
            case GraphPackage.GCONNECTOR__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case GraphPackage.GCONNECTOR__PARENT:
                setParent((GConnectable)null);
                return;
            case GraphPackage.GCONNECTOR__CONNECTIONS:
                getConnections().clear();
                return;
            case GraphPackage.GCONNECTOR__X:
                setX(X_EDEFAULT);
                return;
            case GraphPackage.GCONNECTOR__Y:
                setY(Y_EDEFAULT);
                return;
            case GraphPackage.GCONNECTOR__CONNECTION_DETACHED_ON_DRAG:
                setConnectionDetachedOnDrag(CONNECTION_DETACHED_ON_DRAG_EDEFAULT);
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
            case GraphPackage.GCONNECTOR__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
            case GraphPackage.GCONNECTOR__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case GraphPackage.GCONNECTOR__PARENT:
                return getParent() != null;
            case GraphPackage.GCONNECTOR__CONNECTIONS:
                return connections != null && !connections.isEmpty();
            case GraphPackage.GCONNECTOR__X:
                return x != X_EDEFAULT;
            case GraphPackage.GCONNECTOR__Y:
                return y != Y_EDEFAULT;
            case GraphPackage.GCONNECTOR__CONNECTION_DETACHED_ON_DRAG:
                return connectionDetachedOnDrag != CONNECTION_DETACHED_ON_DRAG_EDEFAULT;
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
        result.append(" (id: ");
        result.append(id);
        result.append(", type: ");
        result.append(type);
        result.append(", x: ");
        result.append(x);
        result.append(", y: ");
        result.append(y);
        result.append(", connectionDetachedOnDrag: ");
        result.append(connectionDetachedOnDrag);
        result.append(')');
        return result.toString();
    }

} //GConnectorImpl
