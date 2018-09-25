/**
 */
package de.tesis.dynaware.grapheditor.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.tesis.dynaware.grapheditor.model.GraphFactory
 * @model kind="package"
 * @generated
 */
public interface GraphPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://de.tesis.dynaware.grapheditor.model/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "graph";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GraphPackage eINSTANCE = de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl <em>GModel</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.tesis.dynaware.grapheditor.model.impl.GModelImpl
	 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGModel()
	 * @generated
	 */
	int GMODEL = 0;

	/**
	 * The feature id for the '<em><b>Nodes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL__NODES = 0;

	/**
	 * The feature id for the '<em><b>Connections</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL__CONNECTIONS = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL__TYPE = 2;

	/**
	 * The feature id for the '<em><b>Content Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL__CONTENT_WIDTH = 3;

	/**
	 * The feature id for the '<em><b>Content Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL__CONTENT_HEIGHT = 4;

	/**
	 * The number of structural features of the '<em>GModel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>GModel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GMODEL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl <em>GNode</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.tesis.dynaware.grapheditor.model.impl.GNodeImpl
	 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGNode()
	 * @generated
	 */
	int GNODE = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__ID = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__TYPE = 1;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__X = 2;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__Y = 3;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__WIDTH = 4;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__HEIGHT = 5;

	/**
	 * The feature id for the '<em><b>Connectors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE__CONNECTORS = 6;

	/**
	 * The number of structural features of the '<em>GNode</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>GNode</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GNODE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.tesis.dynaware.grapheditor.model.impl.GConnectionImpl <em>GConnection</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.tesis.dynaware.grapheditor.model.impl.GConnectionImpl
	 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGConnection()
	 * @generated
	 */
	int GCONNECTION = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION__ID = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION__TYPE = 1;

	/**
	 * The feature id for the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION__SOURCE = 2;

	/**
	 * The feature id for the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION__TARGET = 3;

	/**
	 * The feature id for the '<em><b>Joints</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION__JOINTS = 4;

	/**
	 * The number of structural features of the '<em>GConnection</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>GConnection</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl <em>GConnector</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl
	 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGConnector()
	 * @generated
	 */
	int GCONNECTOR = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__ID = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__TYPE = 1;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__PARENT = 2;

	/**
	 * The feature id for the '<em><b>Connections</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__CONNECTIONS = 3;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__X = 4;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__Y = 5;

	/**
	 * The feature id for the '<em><b>Connection Detached On Drag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR__CONNECTION_DETACHED_ON_DRAG = 6;

	/**
	 * The number of structural features of the '<em>GConnector</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>GConnector</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GCONNECTOR_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link de.tesis.dynaware.grapheditor.model.impl.GJointImpl <em>GJoint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.tesis.dynaware.grapheditor.model.impl.GJointImpl
	 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGJoint()
	 * @generated
	 */
	int GJOINT = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT__ID = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT__TYPE = 1;

	/**
	 * The feature id for the '<em><b>Connection</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT__CONNECTION = 2;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT__X = 3;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT__Y = 4;

	/**
	 * The number of structural features of the '<em>GJoint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>GJoint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GJOINT_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link de.tesis.dynaware.grapheditor.model.GModel <em>GModel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GModel</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel
	 * @generated
	 */
	EClass getGModel();

	/**
	 * Returns the meta object for the containment reference list '{@link de.tesis.dynaware.grapheditor.model.GModel#getNodes <em>Nodes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Nodes</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel#getNodes()
	 * @see #getGModel()
	 * @generated
	 */
	EReference getGModel_Nodes();

	/**
	 * Returns the meta object for the containment reference list '{@link de.tesis.dynaware.grapheditor.model.GModel#getConnections <em>Connections</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Connections</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel#getConnections()
	 * @see #getGModel()
	 * @generated
	 */
	EReference getGModel_Connections();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GModel#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel#getType()
	 * @see #getGModel()
	 * @generated
	 */
	EAttribute getGModel_Type();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GModel#getContentWidth <em>Content Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content Width</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel#getContentWidth()
	 * @see #getGModel()
	 * @generated
	 */
	EAttribute getGModel_ContentWidth();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GModel#getContentHeight <em>Content Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content Height</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GModel#getContentHeight()
	 * @see #getGModel()
	 * @generated
	 */
	EAttribute getGModel_ContentHeight();

	/**
	 * Returns the meta object for class '{@link de.tesis.dynaware.grapheditor.model.GNode <em>GNode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GNode</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode
	 * @generated
	 */
	EClass getGNode();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getId()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getType()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_Type();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getX()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_X();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getY()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_Y();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getWidth()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_Width();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GNode#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getHeight()
	 * @see #getGNode()
	 * @generated
	 */
	EAttribute getGNode_Height();

	/**
	 * Returns the meta object for the containment reference list '{@link de.tesis.dynaware.grapheditor.model.GNode#getConnectors <em>Connectors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Connectors</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GNode#getConnectors()
	 * @see #getGNode()
	 * @generated
	 */
	EReference getGNode_Connectors();

	/**
	 * Returns the meta object for class '{@link de.tesis.dynaware.grapheditor.model.GConnection <em>GConnection</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GConnection</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection
	 * @generated
	 */
	EClass getGConnection();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnection#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getId()
	 * @see #getGConnection()
	 * @generated
	 */
	EAttribute getGConnection_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnection#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getType()
	 * @see #getGConnection()
	 * @generated
	 */
	EAttribute getGConnection_Type();

	/**
	 * Returns the meta object for the reference '{@link de.tesis.dynaware.grapheditor.model.GConnection#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getSource()
	 * @see #getGConnection()
	 * @generated
	 */
	EReference getGConnection_Source();

	/**
	 * Returns the meta object for the reference '{@link de.tesis.dynaware.grapheditor.model.GConnection#getTarget <em>Target</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Target</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getTarget()
	 * @see #getGConnection()
	 * @generated
	 */
	EReference getGConnection_Target();

	/**
	 * Returns the meta object for the containment reference list '{@link de.tesis.dynaware.grapheditor.model.GConnection#getJoints <em>Joints</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Joints</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getJoints()
	 * @see #getGConnection()
	 * @generated
	 */
	EReference getGConnection_Joints();

	/**
	 * Returns the meta object for class '{@link de.tesis.dynaware.grapheditor.model.GConnector <em>GConnector</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GConnector</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector
	 * @generated
	 */
	EClass getGConnector();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnector#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getId()
	 * @see #getGConnector()
	 * @generated
	 */
	EAttribute getGConnector_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnector#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getType()
	 * @see #getGConnector()
	 * @generated
	 */
	EAttribute getGConnector_Type();

	/**
	 * Returns the meta object for the container reference '{@link de.tesis.dynaware.grapheditor.model.GConnector#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getParent()
	 * @see #getGConnector()
	 * @generated
	 */
	EReference getGConnector_Parent();

	/**
	 * Returns the meta object for the reference list '{@link de.tesis.dynaware.grapheditor.model.GConnector#getConnections <em>Connections</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Connections</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getConnections()
	 * @see #getGConnector()
	 * @generated
	 */
	EReference getGConnector_Connections();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnector#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getX()
	 * @see #getGConnector()
	 * @generated
	 */
	EAttribute getGConnector_X();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnector#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#getY()
	 * @see #getGConnector()
	 * @generated
	 */
	EAttribute getGConnector_Y();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GConnector#isConnectionDetachedOnDrag <em>Connection Detached On Drag</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Connection Detached On Drag</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GConnector#isConnectionDetachedOnDrag()
	 * @see #getGConnector()
	 * @generated
	 */
	EAttribute getGConnector_ConnectionDetachedOnDrag();

	/**
	 * Returns the meta object for class '{@link de.tesis.dynaware.grapheditor.model.GJoint <em>GJoint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>GJoint</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint
	 * @generated
	 */
	EClass getGJoint();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GJoint#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint#getId()
	 * @see #getGJoint()
	 * @generated
	 */
	EAttribute getGJoint_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GJoint#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint#getType()
	 * @see #getGJoint()
	 * @generated
	 */
	EAttribute getGJoint_Type();

	/**
	 * Returns the meta object for the container reference '{@link de.tesis.dynaware.grapheditor.model.GJoint#getConnection <em>Connection</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Connection</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint#getConnection()
	 * @see #getGJoint()
	 * @generated
	 */
	EReference getGJoint_Connection();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GJoint#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint#getX()
	 * @see #getGJoint()
	 * @generated
	 */
	EAttribute getGJoint_X();

	/**
	 * Returns the meta object for the attribute '{@link de.tesis.dynaware.grapheditor.model.GJoint#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see de.tesis.dynaware.grapheditor.model.GJoint#getY()
	 * @see #getGJoint()
	 * @generated
	 */
	EAttribute getGJoint_Y();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GraphFactory getGraphFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link de.tesis.dynaware.grapheditor.model.impl.GModelImpl <em>GModel</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.tesis.dynaware.grapheditor.model.impl.GModelImpl
		 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGModel()
		 * @generated
		 */
		EClass GMODEL = eINSTANCE.getGModel();

		/**
		 * The meta object literal for the '<em><b>Nodes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GMODEL__NODES = eINSTANCE.getGModel_Nodes();

		/**
		 * The meta object literal for the '<em><b>Connections</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GMODEL__CONNECTIONS = eINSTANCE.getGModel_Connections();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GMODEL__TYPE = eINSTANCE.getGModel_Type();

		/**
		 * The meta object literal for the '<em><b>Content Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GMODEL__CONTENT_WIDTH = eINSTANCE.getGModel_ContentWidth();

		/**
		 * The meta object literal for the '<em><b>Content Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GMODEL__CONTENT_HEIGHT = eINSTANCE.getGModel_ContentHeight();

		/**
		 * The meta object literal for the '{@link de.tesis.dynaware.grapheditor.model.impl.GNodeImpl <em>GNode</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.tesis.dynaware.grapheditor.model.impl.GNodeImpl
		 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGNode()
		 * @generated
		 */
		EClass GNODE = eINSTANCE.getGNode();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__ID = eINSTANCE.getGNode_Id();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__TYPE = eINSTANCE.getGNode_Type();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__X = eINSTANCE.getGNode_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__Y = eINSTANCE.getGNode_Y();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__WIDTH = eINSTANCE.getGNode_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GNODE__HEIGHT = eINSTANCE.getGNode_Height();

		/**
		 * The meta object literal for the '<em><b>Connectors</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GNODE__CONNECTORS = eINSTANCE.getGNode_Connectors();

		/**
		 * The meta object literal for the '{@link de.tesis.dynaware.grapheditor.model.impl.GConnectionImpl <em>GConnection</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.tesis.dynaware.grapheditor.model.impl.GConnectionImpl
		 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGConnection()
		 * @generated
		 */
		EClass GCONNECTION = eINSTANCE.getGConnection();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTION__ID = eINSTANCE.getGConnection_Id();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTION__TYPE = eINSTANCE.getGConnection_Type();

		/**
		 * The meta object literal for the '<em><b>Source</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GCONNECTION__SOURCE = eINSTANCE.getGConnection_Source();

		/**
		 * The meta object literal for the '<em><b>Target</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GCONNECTION__TARGET = eINSTANCE.getGConnection_Target();

		/**
		 * The meta object literal for the '<em><b>Joints</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GCONNECTION__JOINTS = eINSTANCE.getGConnection_Joints();

		/**
		 * The meta object literal for the '{@link de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl <em>GConnector</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.tesis.dynaware.grapheditor.model.impl.GConnectorImpl
		 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGConnector()
		 * @generated
		 */
		EClass GCONNECTOR = eINSTANCE.getGConnector();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTOR__ID = eINSTANCE.getGConnector_Id();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTOR__TYPE = eINSTANCE.getGConnector_Type();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GCONNECTOR__PARENT = eINSTANCE.getGConnector_Parent();

		/**
		 * The meta object literal for the '<em><b>Connections</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GCONNECTOR__CONNECTIONS = eINSTANCE.getGConnector_Connections();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTOR__X = eINSTANCE.getGConnector_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTOR__Y = eINSTANCE.getGConnector_Y();

		/**
		 * The meta object literal for the '<em><b>Connection Detached On Drag</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GCONNECTOR__CONNECTION_DETACHED_ON_DRAG = eINSTANCE.getGConnector_ConnectionDetachedOnDrag();

		/**
		 * The meta object literal for the '{@link de.tesis.dynaware.grapheditor.model.impl.GJointImpl <em>GJoint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.tesis.dynaware.grapheditor.model.impl.GJointImpl
		 * @see de.tesis.dynaware.grapheditor.model.impl.GraphPackageImpl#getGJoint()
		 * @generated
		 */
		EClass GJOINT = eINSTANCE.getGJoint();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GJOINT__ID = eINSTANCE.getGJoint_Id();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GJOINT__TYPE = eINSTANCE.getGJoint_Type();

		/**
		 * The meta object literal for the '<em><b>Connection</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GJOINT__CONNECTION = eINSTANCE.getGJoint_Connection();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GJOINT__X = eINSTANCE.getGJoint_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GJOINT__Y = eINSTANCE.getGJoint_Y();

	}

} //GraphPackage
