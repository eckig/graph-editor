/**
 */
package de.tesis.dynaware.grapheditor.model.impl;

import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>GNode</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getX <em>X</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getY <em>Y</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getHeight <em>Height</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl#getSubgraph <em>Subgraph</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GNodeImpl extends GConnectableImpl implements GNode {
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
     * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected static final double WIDTH_EDEFAULT = 151.0;

    /**
     * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected double width = WIDTH_EDEFAULT;

    /**
     * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected static final double HEIGHT_EDEFAULT = 101.0;

    /**
     * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected double height = HEIGHT_EDEFAULT;

    /**
     * The cached value of the '{@link #getSubgraph() <em>Subgraph</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSubgraph()
     * @generated
     * @ordered
     */
    protected GModel subgraph;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GNodeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GraphPackage.Literals.GNODE;
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
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__ID, oldId, id));
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
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__TYPE, oldType, type));
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
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__X, oldX, x));
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
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__Y, oldY, y));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getWidth() {
        return width;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWidth(double newWidth) {
        double oldWidth = width;
        width = newWidth;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__WIDTH, oldWidth, width));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public double getHeight() {
        return height;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setHeight(double newHeight) {
        double oldHeight = height;
        height = newHeight;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__HEIGHT, oldHeight, height));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GModel getSubgraph() {
        return subgraph;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSubgraph(GModel newSubgraph, NotificationChain msgs) {
        GModel oldSubgraph = subgraph;
        subgraph = newSubgraph;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__SUBGRAPH, oldSubgraph, newSubgraph);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSubgraph(GModel newSubgraph) {
        if (newSubgraph != subgraph) {
            NotificationChain msgs = null;
            if (subgraph != null)
                msgs = ((InternalEObject)subgraph).eInverseRemove(this, GraphPackage.GMODEL__SUPERGRAPH, GModel.class, msgs);
            if (newSubgraph != null)
                msgs = ((InternalEObject)newSubgraph).eInverseAdd(this, GraphPackage.GMODEL__SUPERGRAPH, GModel.class, msgs);
            msgs = basicSetSubgraph(newSubgraph, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GraphPackage.GNODE__SUBGRAPH, newSubgraph, newSubgraph));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GraphPackage.GNODE__SUBGRAPH:
                if (subgraph != null)
                    msgs = ((InternalEObject)subgraph).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GraphPackage.GNODE__SUBGRAPH, null, msgs);
                return basicSetSubgraph((GModel)otherEnd, msgs);
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
            case GraphPackage.GNODE__SUBGRAPH:
                return basicSetSubgraph(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                return getId();
            case GraphPackage.GNODE__TYPE:
                return getType();
            case GraphPackage.GNODE__X:
                return getX();
            case GraphPackage.GNODE__Y:
                return getY();
            case GraphPackage.GNODE__WIDTH:
                return getWidth();
            case GraphPackage.GNODE__HEIGHT:
                return getHeight();
            case GraphPackage.GNODE__SUBGRAPH:
                return getSubgraph();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case GraphPackage.GNODE__ID:
                setId((String)newValue);
                return;
            case GraphPackage.GNODE__TYPE:
                setType((String)newValue);
                return;
            case GraphPackage.GNODE__X:
                setX((Double)newValue);
                return;
            case GraphPackage.GNODE__Y:
                setY((Double)newValue);
                return;
            case GraphPackage.GNODE__WIDTH:
                setWidth((Double)newValue);
                return;
            case GraphPackage.GNODE__HEIGHT:
                setHeight((Double)newValue);
                return;
            case GraphPackage.GNODE__SUBGRAPH:
                setSubgraph((GModel)newValue);
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
            case GraphPackage.GNODE__ID:
                setId(ID_EDEFAULT);
                return;
            case GraphPackage.GNODE__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case GraphPackage.GNODE__X:
                setX(X_EDEFAULT);
                return;
            case GraphPackage.GNODE__Y:
                setY(Y_EDEFAULT);
                return;
            case GraphPackage.GNODE__WIDTH:
                setWidth(WIDTH_EDEFAULT);
                return;
            case GraphPackage.GNODE__HEIGHT:
                setHeight(HEIGHT_EDEFAULT);
                return;
            case GraphPackage.GNODE__SUBGRAPH:
                setSubgraph((GModel)null);
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
            case GraphPackage.GNODE__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
            case GraphPackage.GNODE__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case GraphPackage.GNODE__X:
                return x != X_EDEFAULT;
            case GraphPackage.GNODE__Y:
                return y != Y_EDEFAULT;
            case GraphPackage.GNODE__WIDTH:
                return width != WIDTH_EDEFAULT;
            case GraphPackage.GNODE__HEIGHT:
                return height != HEIGHT_EDEFAULT;
            case GraphPackage.GNODE__SUBGRAPH:
                return subgraph != null;
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
        result.append(", width: ");
        result.append(width);
        result.append(", height: ");
        result.append(height);
        result.append(')');
        return result.toString();
    }

} //GNodeImpl
